package org.sweetchips.platform.jvm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.sweetchips.platform.common.ContextLogger
import org.sweetchips.platform.common.PlatformContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

class JvmContext(val logger: ContextLogger) : PlatformContext {

    private companion object {
        const val TAG = "JvmContext"
    }

    var api: Int = 0
    var isIncremental: Boolean = false
    var extra: MutableMap<Any, Any>? = ConcurrentHashMap()

    private var bytesWriter: BiConsumer<String, ByteArray>? = null

    private var prepare: Deque<ClassVisitorFactory>? = ArrayDeque()
    private var transform: Deque<ClassVisitorFactory>? = ArrayDeque()

    private var prepareBefore: MutableList<Consumer<Map<Any, Any>>>? = mutableListOf()
    private var prepareAfter: MutableList<Consumer<Map<Any, Any>>>? = mutableListOf()
    private var transformBefore: MutableList<Consumer<Map<Any, Any>>>? = mutableListOf()
    private var transformAfter: MutableList<Consumer<Map<Any, Any>>>? = mutableListOf()

    private var additions: MutableCollection<Supplier<ClassNode>>? = ConcurrentLinkedQueue()
    private var classes: MutableCollection<ClassNode>? = null

    fun setBytesWriter(consumer: BiConsumer<String, ByteArray>) {
        bytesWriter = consumer
    }

    fun addPrepareBefore(action: Consumer<Map<Any, Any>>) {
        prepareBefore?.add(action)
    }

    fun addPrepareAfter(action: Consumer<Map<Any, Any>>) {
        prepareAfter?.add(action)
    }

    fun addTransformBefore(action: Consumer<Map<Any, Any>>) {
        transformBefore?.add(action)
    }

    fun addTransformAfter(action: Consumer<Map<Any, Any>>) {
        transformAfter?.add(action)
    }

    fun addPrepareFirst(action: ClassVisitorFactory?) {
        prepare?.addFirst(action)
    }

    fun addPrepareLast(action: ClassVisitorFactory?) {
        prepare?.addLast(action)
    }

    fun addTransformFirst(action: ClassVisitorFactory?) {
        transform?.addFirst(action)
    }

    fun addTransformLast(action: ClassVisitorFactory?) {
        transform?.addLast(action)
    }

    fun addClass(action: Supplier<ClassNode>) {
        additions?.add(action)
    }

    override fun onPrepareBefore() = Runnable {
        prepareBefore!!.also { prepareBefore = null }.phase("prepareBefore")
    }

    override fun onPrepareAfter() = Runnable {
        onPrepareAdditions()
        prepareAfter!!.also { prepareAfter = null }.phase("prepareAfter")
    }

    override fun onTransformBefore() = Runnable {
        transformBefore!!.also { transformBefore = null }.phase("transformBefore")
        onTransformAdditions()
    }

    override fun onTransformAfter() = Runnable {
        transform = null
        transformAfter!!.also { transformAfter = null }.phase("transformAfter")
        extra = null
    }

    fun onPrepare() = Consumer<ByteArray> { bytes ->
        val actions = prepare!!
        if (actions.size == 0) {
            return@Consumer
        }
        val cr = ClassReader(bytes)
        var cv = object : ClassVisitor(api) {} as ClassVisitor
        actions.forEach {
            cv = it.newInstance(api, cv, extra)
        }
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
    }

    fun onTransform() = Function<ByteArray, ByteArray?> { bytes ->
        val actions = transform!!
        if (actions.size == 0) {
            return@Function bytes
        }
        val cr = ClassReader(bytes)
        val cw = ClassWriter(api)
        var cv = cw as ClassVisitor
        actions.forEach {
            cv = it.newInstance(api, cv, extra)
        }
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        if (ClassesSetting.checkDeleteFlag()) {
            val tag = "onTransform"
            logger.i(TAG, "$tag: delete a class")
            return@Function null
        }
        return@Function cw.toByteArray()
    }

    private fun List<Consumer<Map<Any, Any>>>.phase(tag: String) {
        logger.d(TAG, "$tag: begin")
        forEach {
            it.accept(extra!!)
        }
        logger.d(TAG, "$tag: end")
    }

    private fun onPrepareAdditions() {
        val tag = "onPrepareAdditions"
        logger.d(TAG, "$tag: begin")
        val actions = prepare!!.also { prepare = null }
        val nodes = ConcurrentLinkedQueue<ClassNode>().also { classes = it }
        runBlocking {
            additions!!.also { additions = null }.forEach {
                launch {
                    val node = withContext(Dispatchers.IO) { it.get() }
                    logger.i(TAG, "$tag: create ${node.name}")
                    if (actions.size == 0) {
                        nodes.add(node)
                        return@launch
                    }
                    val cn = ClassNode(api)
                    var cv = cn as ClassVisitor
                    actions.forEach {
                        cv = it.newInstance(api, cv, extra)
                    }
                    withContext(Dispatchers.Default) {
                        node.accept(cv)
                    }
                    nodes.add(cn)
                }
            }
        }
        logger.d(TAG, "$tag: end")
    }

    private fun onTransformAdditions() {
        val tag = "onTransformAdditions"
        logger.d(TAG, "$tag: begin")
        val actions = transform!!
        val writeNode = bytesWriter?.also { bytesWriter = null }?.run {
            { cn: ClassNode ->
                val name = cn.name
                val cw = ClassWriter(api)
                cn.accept(cw)
                accept(name, cw.toByteArray())
            }
        } ?: {
            Unit
        }
        runBlocking {
            classes!!.also { classes = null }.forEach {
                launch {
                    if (actions.size == 0) {
                        writeNode(it)
                        return@launch
                    }
                    val cn = ClassNode(api)
                    var cv = cn as ClassVisitor
                    actions.forEach {
                        cv = it.newInstance(api, cv, extra)
                    }
                    withContext(Dispatchers.Default) {
                        it.accept(cv)
                    }
                    if (ClassesSetting.checkDeleteFlag()) {
                        logger.i(TAG, "$tag: delete ${cn.name}")
                        return@launch
                    }
                    writeNode(cn)
                }
            }
        }
        logger.d(TAG, "$tag: end")
    }
}