package org.sweetchips.plugin4gradle;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.stream.Collectors;

public class UnionExtension {

    public UnionExtension() {
    }

    private boolean mIsIncremental = false;

    boolean isIncremental() {
        return mIsIncremental;
    }

    private int mAsmApi = Opcodes.ASM5;

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
        Arrays.stream(name).forEach(UnionContext.getPlugin()::addTransform);
    }

    public void addPrepare(String... name) {
        UnionContext.addLastPrepare(null,
                Arrays.stream(name)
                        .map(Util::forName)
                        .collect(Collectors.toList())
        );
    }

    public void addTransform(String... name) {
        UnionContext.addLastTransform(null,
                Arrays.stream(name)
                        .map(Util::forName)
                        .collect(Collectors.toList())
        );
    }
}
