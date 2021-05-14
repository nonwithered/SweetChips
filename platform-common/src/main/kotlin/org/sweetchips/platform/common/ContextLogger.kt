package org.sweetchips.platform.common

import java.io.PrintWriter
import java.io.StringWriter

open class ContextLogger {

    enum class Level {

        DEBUG {
            override fun log(logger: ContextLogger, tag: String, msg: String) {
                logger.d(tag, msg)
            }
        },
        INFO {
            override fun log(logger: ContextLogger, tag: String, msg: String) {
                logger.i(tag, msg)
            }
        },
        WARN {
            override fun log(logger: ContextLogger, tag: String, msg: String) {
                logger.w(tag, msg)
            }
        },
        ERROR {
            override fun log(logger: ContextLogger, tag: String, msg: String) {
                logger.e(tag, msg)
            }
        };

        abstract fun log(logger: ContextLogger, tag: String, msg: String)
    }

    companion object {

        private fun Throwable.logStackTrace(): String {
            return StringWriter().also {
                printStackTrace(PrintWriter(it))
            }.toString()
        }
    }

    open fun d(tag: String, msg: String) {
        print("${Level.DEBUG}/$tag: $msg")
    }

    open fun d(tag: String, e: Throwable) {
        d(tag, e.logStackTrace())
    }

    open fun i(tag: String, msg: String) {
        print("${Level.INFO}/$tag: $msg")
    }

    open fun i(tag: String, e: Throwable) {
        i(tag, e.logStackTrace())
    }

    open fun w(tag: String, msg: String) {
        print("${Level.WARN}/$tag: $msg")
    }

    open fun w(tag: String, e: Throwable) {
        w(tag, e.logStackTrace())
    }

    open fun e(tag: String, msg: String) {
        print("${Level.ERROR}/$tag: $msg")
    }

    open fun e(tag: String, e: Throwable) {
        e(tag, e.logStackTrace())
    }

    open fun log(level: Level, tag: String, msg: String) {
        level.log(this, tag, msg)
    }

    open fun log(level: Level, tag: String, e: Throwable) {
        log(level, tag, e.logStackTrace())
    }
}