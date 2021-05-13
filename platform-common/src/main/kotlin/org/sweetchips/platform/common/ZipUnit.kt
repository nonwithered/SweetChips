package org.sweetchips.platform.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.sweetchips.utility.FilesUtil
import java.nio.file.Path
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipUnit(
    input: Path,
    output: Path,
    private val prepare: List<Function<ZipEntry, Consumer<ByteArray>?>>?,
    private val transform: List<Function<ZipEntry, Function<ByteArray, ByteArray?>?>>?,
) : AbstractUnit(input, output) {

    override suspend fun onPrepare() {
        if (prepare?.isEmpty() != false) {
            return
        }
        withContext(Dispatchers.IO) { ZipFile(input.toFile()) }.use { zip ->
            if (zip.size() == 0) {
                return
            }
            Collections.list(zip.entries()).forEach {
                var action: Consumer<ByteArray>? = null
                for (filter in prepare) {
                    action = filter.apply(it)
                    if (action != null) {
                        break
                    }
                }
                if (action != null) {
                    val bytes = withContext(Dispatchers.IO) {
                        FilesUtil.newZipReader(zip).readFrom(it)
                    }
                    withContext(Dispatchers.Default) {
                        action.accept(bytes)
                    }
                }
            }
        }
    }

    override suspend fun onTransform() {
        if (transform?.isEmpty() != false) {
            return
        }
        withContext(Dispatchers.IO) { ZipFile(input.toFile()) }.use { zip ->
            if (zip.size() == 0) {
                return
            }
            val monitor = FilesUtil.newZipWriter(output, zip.size())
            supervisorScope {
                launch(Dispatchers.IO) { monitor.run() }
                Collections.list(zip.entries()).forEach {
                    var action: Function<ByteArray, ByteArray?>? = null
                    for (filter in transform) {
                        action = filter.apply(it)
                        if (action != null) {
                            break
                        }
                    }
                    var bytes = withContext(Dispatchers.IO) {
                        FilesUtil.newZipReader(zip).readFrom(it)
                    }
                    if (action != null) {
                        bytes = withContext(Dispatchers.Default) {
                            action.apply(bytes)
                        }
                    }
                    withContext(Dispatchers.Default) {
                        monitor.writeTo(it.name, bytes)
                    }
                }
            }
        }
    }
}