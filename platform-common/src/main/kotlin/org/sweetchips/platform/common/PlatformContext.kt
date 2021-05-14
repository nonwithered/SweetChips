package org.sweetchips.platform.common

interface PlatformContext {

    fun onPrepareBefore(): Runnable
    fun onPrepareAfter(): Runnable
    fun onTransformBefore(): Runnable
    fun onTransformAfter(): Runnable
}