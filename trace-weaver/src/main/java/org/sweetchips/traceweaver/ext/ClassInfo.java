package org.sweetchips.traceweaver.ext;

public final class ClassInfo {

    public int version;
    public int access;
    public String name;
    public String signature;
    public String superName;
    public String[] interfaces;

    public ClassInfo(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
    }
}
