package org.sweetchips.platform.jvm;

import org.sweetchips.platform.common.FileUnit;
import org.sweetchips.platform.common.IUnit;
import org.sweetchips.platform.common.PathUnit;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;

public final class JvmContextCallbacks {

    private final JvmContext mContext;
    private final List<Function<ZipEntry, Consumer<byte[]>>> mPrepareZip = new ArrayList<>();
    private final List<Function<ZipEntry, Function<byte[], byte[]>>> mTransformZip = new ArrayList<>();
    private final List<Function<Path, Consumer<byte[]>>> mPrepareFile = new ArrayList<>();
    private final List<Function<Path, Function<byte[], byte[]>>> mTransformFile = new ArrayList<>();
    private final List<BiFunction<Path, Path, IUnit>> mPreparePath = new ArrayList<>();
    private final List<BiFunction<Path, Path, IUnit>> mTransformPath = new ArrayList<>();

    public JvmContextCallbacks(JvmContext context) {
        mContext = context;
        mPrepareZip.add(it -> !it.getName().endsWith(".class") ? b -> {} : null);
        mPrepareZip.add(it -> mContext.onPrepare());
        mTransformZip.add(it -> !it.getName().endsWith(".class") ? b -> b : null);
        mTransformZip.add(it -> mContext.onTransform());
        mPrepareFile.add(it -> !FilesUtil.getFileName(it).endsWith(".class") ? b -> {} : null);
        mPrepareFile.add(it -> mContext.onPrepare());
        mTransformFile.add(it -> !FilesUtil.getFileName(it).endsWith(".class") ? b -> b : null);
        mTransformFile.add(it -> mContext.onTransform());
        mPreparePath.add((f, t) -> FilesUtil.isDirectory(f) ? new PathUnit(f, t, mPreparePath, null) : null);
        mPreparePath.add((f, t) -> new FileUnit(f, t, mPrepareFile, null));
        mTransformPath.add((f, t) -> FilesUtil.isDirectory(f) ? new PathUnit(f, t, null, mTransformPath) : null);
        mTransformPath.add((f, t) -> new FileUnit(f, t, null, mTransformFile));
    }

    public List<Function<ZipEntry, Consumer<byte[]>>> onPrepareZip() {
        return mPrepareZip;
    }

    public List<Function<ZipEntry, Function<byte[], byte[]>>> onTransformZip() {
        return mTransformZip;
    }

    public List<Function<Path, Consumer<byte[]>>> onPrepareFile() {
        return mPrepareFile;
    }

    public List<Function<Path, Function<byte[], byte[]>>> onTransformFile() {
        return mTransformFile;
    }

    public List<BiFunction<Path, Path, IUnit>> onPreparePath() {
        return mPreparePath;
    }

    public List<BiFunction<Path, Path, IUnit>> onTransformPath() {
        return mTransformPath;
    }
}
