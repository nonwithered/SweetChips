package org.sweetchips.gradle.common;

import org.objectweb.asm.Opcodes;
import org.sweetchips.platform.jvm.JvmContext;

import java.util.HashMap;
import java.util.Map;

public class SweetChipsExtension {

    private int mAsmApi = Opcodes.ASM5;
    private final SweetChipsGradlePlugin mPlugin;

    public SweetChipsExtension(SweetChipsGradlePlugin plugin) {
        mPlugin = plugin;
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
            WorkflowExtension extension = (WorkflowExtension) mPlugin.getProject().getExtensions().getByName(name);
            mExtension.setExtra(extension.getExtra());
        }
        public void setIncremental(boolean b) {
            JvmContext context = mExtension.mContext.get();
            if (context != null) {
                context.setIncremental(b);
            }
        }
    }

    public TransformExt newWorkflow(String name) {
        JvmContext context = new JvmContext(mPlugin.getLogger());
        context.setApi(mAsmApi);
        mPlugin.registerTransform(name, context);
        WorkflowExtension extension = mPlugin.getProject().getExtensions().create(name, WorkflowExtension.class, context);
        return new TransformExt(extension);
    }

    public TransformExt newWorkflow(Map<String, ?> opt, String name) {
        TransformExt ext = newWorkflow(name);
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
