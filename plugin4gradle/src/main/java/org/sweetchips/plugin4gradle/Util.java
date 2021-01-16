package org.sweetchips.plugin4gradle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Util {

    String NAME = "SweetChips";

    Map<String, UnionContext> CONTEXTS = new ConcurrentHashMap<>();
}
