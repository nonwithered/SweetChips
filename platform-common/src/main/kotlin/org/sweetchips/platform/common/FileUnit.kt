package org.sweetchips.platform.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sweetchips.utility.FilesUtil
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Function

class FileUnit(
    input: Path,
    output: Path,
    private val prepare: List<Function<Path, Consumer<ByteArray>?>>?,
    private val transform: List<Function<Path, Function<ByteArray, ByteArray?>?>>?,
) : AbstractUnit(input, output) {

    override suspend fun onPrepare() {
        if (prepare?.isEmpty() != false) {
            return
        }
        var action: Consumer<ByteArray>? = null
        for (filter in prepare) {
            action = filter.apply(input)
            if (action != null) {
                break
            }
        }
        if (action != null) {
            val bytes = withContext(Dispatchers.IO) {
                FilesUtil.readFrom(input)
            }
            withContext(Dispatchers.Default) {
                action.accept(bytes)
            }
        }
    }

    override suspend fun onTransform() {
        if (transform?.isEmpty() != false) {
            return
        }
        var action: Function<ByteArray, ByteArray?>? = null
        for (it in transform) {
            action = it.apply(input)
            if (action != null) {
                break
            }
        }
        var bytes = withContext(Dispatchers.IO) {
            FilesUtil.readFrom(input)
        }
        if (action != null) {
            bytes = withContext(Dispatchers.Default) {
                action.apply(bytes)
            }
        }
        withContext(Dispatchers.IO) {
            FilesUtil.writeTo(output, bytes)
        }
    }
}