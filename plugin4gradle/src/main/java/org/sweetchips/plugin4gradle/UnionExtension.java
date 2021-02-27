package org.sweetchips.plugin4gradle;

import org.objectweb.asm.Opcodes;
import org.sweetchips.plugin4gradle.util.ClassesUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UnionExtension {

    private boolean mIsIncremental = false;

    private int mAsmApi = Opcodes.ASM5;

    boolean isIncremental() {
        return mIsIncremental;
    }

    int getAsmApi() {
        return mAsmApi;
    }

    public void asmApi(int asmApi) {
        mAsmApi = asmApi;
    }

    public void incremental(boolean isIncremental) {
        mIsIncremental = isIncremental;
    }

    public void addTask(String... name) {
        Arrays.stream(name).forEach(UnionPlugin.getInstance()::addTransform);
    }

    public void addPrepare(Map<String, List<String>> map) {
        map.forEach((k, v) -> {
            switch (k) {
                case Util.DO_FIRST:
                    v.forEach(it -> UnionContext.addClassVisitor(AbstractPlugin.ActionType.PREPARE, AbstractPlugin.ActionMode.FIRST, null, ClassesUtil.forName(it)));
                    break;
                case Util.DO_LAST:
                    v.forEach(it -> UnionContext.addClassVisitor(AbstractPlugin.ActionType.PREPARE, AbstractPlugin.ActionMode.LAST, null, ClassesUtil.forName(it)));
                    break;
                default:
                    throw new IllegalArgumentException(k);
            }
        });
    }

    public void addTransform(Map<String, List<String>> map) {
        map.forEach((k, v) -> {
            switch (k) {
                case Util.DO_FIRST:
                    v.forEach(it -> UnionContext.addClassVisitor(AbstractPlugin.ActionType.TRANSFORM, AbstractPlugin.ActionMode.FIRST, null, ClassesUtil.forName(it)));
                    break;
                case Util.DO_LAST:
                    v.forEach(it -> UnionContext.addClassVisitor(AbstractPlugin.ActionType.TRANSFORM, AbstractPlugin.ActionMode.LAST, null, ClassesUtil.forName(it)));
                    break;
                default:
                    throw new IllegalArgumentException(k);
            }
        });
    }
}
