package org.sweetchips.platform.common

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.sweetchips.utility.FilesUtil
import java.nio.file.Path
import java.util.function.BiFunction
import kotlin.streams.toList

class PathUnit(
    input: Path,
    output: Path,
    private val prepare: List<BiFunction<Path, Path, IUnit?>>?,
    private val transform: List<BiFunction<Path, Path, IUnit?>>?,
) : AbstractUnit(input, output) {

    override suspend fun onPrepare() {
        if (prepare?.isEmpty() != false) {
            return
        }
        supervisorScope {
            FilesUtil.list(input).toList().forEach {
                if (!FilesUtil.exists(it)) {
                    return@forEach
                }
                val path = FilesUtil.lookupPathFromTo(it, input, output)
                var unit: IUnit? = null
                for (filter in prepare) {
                    unit = filter.apply(it, path)
                    if (unit != null) {
                        break
                    }
                }
                if (unit == null && FilesUtil.isDirectory(it)) {
                    unit = PathUnit(it, path, prepare, transform)
                }
                launch {
                    unit?.onPrepare()
                }
            }
        }
    }

    override suspend fun onTransform() {
        if (transform?.isEmpty() != false) {
            return
        }
        supervisorScope {
            FilesUtil.list(input).toList().forEach {
                if (!FilesUtil.exists(it)) {
                    return@forEach
                }
                val path = FilesUtil.lookupPathFromTo(it, input, output)
                var unit: IUnit? = null
                for (filter in transform) {
                    unit = filter.apply(it, path)
                    if (unit != null) {
                        break
                    }
                }
                if (unit == null) {
                    if (FilesUtil.isDirectory(it)) {
                        unit = PathUnit(it, path, prepare, transform)
                    } else {
                        unit = FileUnit(it, path, null, null)
                    }
                }
                launch {
                    unit.onTransform()
                }
            }
        }
    }
}