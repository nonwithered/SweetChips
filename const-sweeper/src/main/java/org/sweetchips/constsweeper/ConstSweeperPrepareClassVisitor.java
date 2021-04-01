package org.sweetchips.constsweeper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import java.util.Map;

public final class ConstSweeperPrepareClassVisitor extends ClassVisitor {

    private ConstSweeperPlugin mPlugin;

    ConstSweeperPrepareClassVisitor withPlugin(ConstSweeperPlugin plugin) {
        mPlugin = plugin;
        return this;
    }

    private final Map<String, Object> mExtra;
    private String mName;

    public ConstSweeperPrepareClassVisitor(int api, ClassVisitor cv, Map<Object, Object> extra) {
        super(api, cv);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = extra == null
                ? null : (Map<String, Object>) extra.get(Util.NAME);
        mExtra = map != null ? map : Util.sConstantValues;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (!mPlugin.getExtension().isIgnored(mName, name)
                && Util.unusedField(access, name, desc, signature, value)) {
            mExtra.put(Util.getKey(mName, name, desc), value);
        }
        return super.visitField(access, name, desc, signature, value);
    }
}
