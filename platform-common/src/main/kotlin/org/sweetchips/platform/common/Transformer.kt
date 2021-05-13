package org.sweetchips.platform.common

import kotlinx.coroutines.*
import org.sweetchips.utility.FilesUtil
import java.lang.Runnable

internal class Transformer(
    private val mUnits: Collection<RootUnit>,
) {

    companion object {
        private const val TAG = "Transformer"

        internal fun Collection<Transformer>.doWork(
            prepareBefore: Runnable,
            prepareAfter: Runnable,
            transformBefore: Runnable,
            transformAfter: Runnable,
            logger: ContextLogger,
        ) {
            logger.d(TAG, "doWork: begin")
            prepareBefore.run()
            work(logger, "doPrepare") { doPrepare() }
            prepareAfter.run()
            transformBefore.run()
            work(logger, "doTransform") { doTransform() }
            transformAfter.run()
            logger.d(TAG, "doWork: end")
        }

        private fun Collection<Transformer>.work(
            logger: ContextLogger,
            tag: String,
            work: suspend Transformer.() -> Unit
        ) {
            logger.d(TAG, "$tag: begin")
            runBlocking {
                forEach {
                    launch {
                        it.work()
                    }
                }
            }
            logger.d(TAG, "$tag: end")
        }
    }

    private suspend fun doPrepare() = supervisorScope {
        mUnits.forEach {
            when (it.status) {
                RootUnit.Status.REMOVED, RootUnit.Status.NOTCHANGED -> Unit
                RootUnit.Status.ADDED, RootUnit.Status.CHANGED -> launch {
                    it.onPrepare()
                }
            }
        }
    }

    private suspend fun doTransform() = supervisorScope {
        mUnits.forEach {
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