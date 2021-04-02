package org.sweetchips.constsweeper;

import org.sweetchips.platform.jvm.BasePluginContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConstSweeperContext extends BasePluginContext {

    @Override
    public synchronized boolean isIgnored(String clazz, String member) {
        return super.isIgnored(clazz, member);
    }

    public static final String NAME = "ConstSweeper";

    static String getKey(String className, String fieldName, String fieldType) {
        return className + "->" + fieldName + ":" + fieldType;
    }

    private final Map<String, Object> mConstants = new ConcurrentHashMap<>();

    Map<String, Object> getConstants() {
        return mConstants;
    }
}
