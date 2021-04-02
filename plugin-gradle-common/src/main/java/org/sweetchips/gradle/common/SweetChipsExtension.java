package org.sweetchips.gradle.common;

import org.sweetchips.platform.jvm.JvmContext;

import java.util.HashMap;
import java.util.Map;

import jdk.internal.org.objectweb.asm.Opcodes;

public class SweetChipsExtension extends AbstractExtension<SweetChipsGradlePlugin> {

    private int mAsmApi = Opcodes.ASM5;

    public SweetChipsExtension(SweetChipsGradlePlugin plugin) {
        super(plugin);
    }

    public void setAsmApi(int asmApi) {
        mAsmApi = asmApi;
    }

    public int getAsmApi() {
        return mAsmApi;
    }

    public class TransformExt {
        private final WorkflowExtension mExtension;
        private TransformExt(WorkflowExtension extension) {
            mExtension = extension;
        }
        public void sameExtra(String name) {
            WorkflowExtension extension = (WorkflowExtension) getPlugin().getProject().getExtensions().getByName(name);
            mExtension.setExtra(extension.getExtra());
        }
        public void setIncremental(boolean b) {
            JvmContext context = mExtension.mContext.get();
            if (context != null) {
                context.setIncremental(b);
            }
        }
    }

    public TransformExt addTransform(String name) {
        JvmContext context = new JvmContext();
        context.setApi(mAsmApi);
        getPlugin().registerTransform(name, context);
        WorkflowExtension extension = getPlugin().getProject().getExtensions().create(name, WorkflowExtension.class, context);
        return new TransformExt(extension);
    }

    public TransformExt addTransform(Map<String, ?> opt, String name) {
        TransformExt ext = addTransform(name);
        Map<String, Object> map = new HashMap<>();
        opt.entrySet().forEach(it -> {
            String key = it.getKey();
            if (key.equals("sameExtra")) {
                if (map.get(key) != null) {
                    throw new IllegalArgumentException(key);
                }
                map.put(key, Boolean.TRUE);
                ext.sameExtra((String) it.getValue());
            } else if (key.equals("incremental")) {
                if (map.get(key) != null) {
                    throw new IllegalArgumentException(key);
                }
                ext.setIncremental((Boolean) it.getValue());
            } else {
                throw new IllegalArgumentException(key);
            }
        });
        return ext;
    }
}
