package org.sweetchips.platform.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.sweetchips.utility.FilesUtil
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

class Workflow(private val logger: ContextLogger) {

    private companion object {

        private const val TAG = "Workflow"

        suspend fun Collection<RootUnit>.doPrepare() = supervisorScope {
            forEach {
                when (it.status) {
                    RootUnit.Status.REMOVED, RootUnit.Status.NOTCHANGED -> Unit
                    RootUnit.Status.ADDED, RootUnit.Status.CHANGED -> launch {
                        it.onPrepare()
                    }
                }
            }
        }

        suspend fun Collection<RootUnit>.doTransform() = supervisorScope {
            forEach {
                when (it.status) {
                    RootUnit.Status.REMOVED -> launch(Dispatchers.IO) {
                        FilesUtil.deleteIfExists(it.output)
                    }
                    RootUnit.Status.NOTCHANGED -> Unit
                    RootUnit.Status.ADDED, RootUnit.Status.CHANGED -> launch {
                        it.onTransform()
                    }
                }
            }
        }
    }

    private var workSet: MutableList<Collection<RootUnit>>? = mutableListOf()

    private var prepareBefore: MutableList<Runnable>? = mutableListOf()
    private var prepareAfter: MutableList<Runnable>? = mutableListOf()
    private var transformBefore: MutableList<Runnable>? = mutableListOf()
    private var transformAfter: MutableList<Runnable>? = mutableListOf()

    fun addWork(collection: Collection<RootUnit>) = workSet?.add(collection)

    fun addPrepareBefore(runnable: Runnable) = prepareBefore?.add(runnable)
    fun addPrepareAfter(runnable: Runnable) = prepareAfter?.add(runnable)
    fun addTransformBefore(runnable: Runnable) = transformBefore?.add(runnable)
    fun addTransformAfter(runnable: Runnable) = transformAfter?.add(runnable)

    fun attach(context: PlatformContext) {
        addPrepareBefore(context.onPrepareBefore())
        addPrepareAfter(context.onPrepareAfter())
        addTransformBefore(context.onTransformBefore())
        addTransformAfter(context.onTransformAfter())
    }

    fun start(executor: Executor): Future<Unit> {
        logger.d(TAG, "start: begin")
        val future = workSet!!.also { workSet = null }.let {
            FutureTask { it.doRun() }
        }
        executor.execute(future)
        logger.d(TAG, "start: end")
        return future
    }

    private fun Collection<Collection<RootUnit>>.doRun(
    ) {
        logger.d(TAG, "doWork: begin")
        prepareBefore!!.also { prepareBefore = null }.phase("prepareBefore")
        doWork("doPrepare") { doPrepare() }
        prepareAfter!!.also { prepareBefore = null }.phase("prepareAfter")
        transformBefore!!.also { prepareBefore = null }.phase("transformBefore")
        doWork("doTransform") { doTransform() }
        transformAfter!!.also { prepareBefore = null }.phase("transformAfter")
        logger.d(TAG, "doWork: end")
    }

    private fun List<Runnable>.phase(tag: String) {
        logger.d(TAG, "$tag: begin")
        forEach { it.run() }
        logger.d(TAG, "$tag: end")
    }

    private fun Collection<Collection<RootUnit>>.doWork(
        tag: String,
        work: suspend Collection<RootUnit>.() -> Unit
    ) {
        logger.d(TAG, "$tag: begin")
        runBlocking {
            forEach {
                launch { it.work() }
            }
        }
        logger.d(TAG, "$tag: end")
    }
}