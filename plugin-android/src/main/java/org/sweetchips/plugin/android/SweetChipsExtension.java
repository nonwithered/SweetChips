package org.sweetchips.plugin.android;

import org.sweetchips.common.jvm.JvmContext;

import java.util.HashMap;
import java.util.Map;

public class SweetChipsExtension extends AbstractExtension {

    private int mAsmApi;

    public void setAsmApi(int asmApi) {
        mAsmApi = asmApi;
    }

    public static class TransformExt {
        private final WorkflowExtension mExtension;
        private TransformExt(WorkflowExtension extension) {
            mExtension = extension;
        }
        public void sameExtra(String name) {
            WorkflowExtension extension = (WorkflowExtension) SweetChipsPlugin.INSTANCE.getProject().getExtensions().getByName(name);
            mExtension.setExtra(extension.getExtra());
        }
    }

    public TransformExt addTransform(String name) {
        JvmContext context = new JvmContext();
        context.setApi(mAsmApi);
        SweetChipsPlugin.INSTANCE.android.registerTransform(new WorkflowTransform(name, context));
        WorkflowExtension extension = SweetChipsPlugin.INSTANCE.getProject().getExtensions().create(name, WorkflowExtension.class, context);
        return new TransformExt(extension);
    }

    public TransformExt addTransform(String name, Map<String, ?> opt) {
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
            }
            throw new IllegalArgumentException(key);
        });
        return ext;
    }
}
