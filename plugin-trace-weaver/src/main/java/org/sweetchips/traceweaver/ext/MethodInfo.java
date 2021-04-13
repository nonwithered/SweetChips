package org.sweetchips.traceweaver.ext;

public final class MethodInfo {

    public int access;
    public String name;
    public String desc;
    public String signature;
    public String[] exceptions;

    public MethodInfo(int access, String name, String desc, String signature, String[] exceptions) {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = exceptions;
    }
}
