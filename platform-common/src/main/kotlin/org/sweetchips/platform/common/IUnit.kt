package org.sweetchips.platform.common

import java.nio.file.Path

interface IUnit {

    val input: Path
    val output: Path

    suspend fun onPrepare()
    suspend fun onTransform()
}