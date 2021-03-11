package org.sweetchips.foundation;

import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;

public final class PathUnit extends AbstractUnit {

    private final List<BiFunction<Path, Path, AbstractUnit>> mPrepare;
    private final List<BiFunction<Path, Path, AbstractUnit>> mTransform;

    public PathUnit(Path input, Path output,
                    List<BiFunction<Path, Path, AbstractUnit>> prepare,
                    List<BiFunction<Path, Path, AbstractUnit>> transform) {
        super(input, output);
        mPrepare = prepare;
        mTransform = transform;
    }

    @Override
    protected void onPrepare() {
        if (mPrepare == null || mPrepare.size() == 0) {
            super.onPrepare();
            return;
        }
        AsyncUtil.with(FilesUtil.list(getInput()))
                .forkJoin(it -> {
                    if (!FilesUtil.exists(it)) {
                        return;
                    }
                    Path path = FilesUtil.lookupPathFromTo(it, getInput(), getOutput());
                    AbstractUnit unit = null;
                    for (BiFunction<Path, Path, AbstractUnit> filter : mPrepare) {
                        if ((unit = filter.apply(it, path)) != null) {
                            break;
                        }
                    }
                    if (unit == null) {
                        if (FilesUtil.isDirectory(it)) {
                            unit = new PathUnit(it, path, mPrepare, mTransform);
                        }
                    }
                    if (unit != null) {
                        unit.onPrepare();
                    }
                });
    }

    @Override
    protected void onTransform() {
        if (mTransform == null || mTransform.size() == 0) {
            super.onTransform();
            return;
        }
        AsyncUtil.with(FilesUtil.list(getInput()))
                .forkJoin(it -> {
                    if (!FilesUtil.exists(it)) {
                        return;
                    }
                    Path path = FilesUtil.lookupPathFromTo(it, getInput(), getOutput());
                    AbstractUnit abstractUnit = null;
                    for (BiFunction<Path, Path, AbstractUnit> filter : mTransform) {
                        if ((abstractUnit = filter.apply(it, path)) != null) {
                            break;
                        }
                    }
                    if (abstractUnit == null) {
                        if (FilesUtil.isDirectory(it)) {
                            abstractUnit = new PathUnit(it, path, mPrepare, mTransform);
                        } else {
                            abstractUnit = new FileUnit(it, path, null, null);
                        }
                    }
                    abstractUnit.onTransform();
                });
    }
}
