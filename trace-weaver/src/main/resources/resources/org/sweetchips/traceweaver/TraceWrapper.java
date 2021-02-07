package org.sweetchips.traceweaver;

final class TraceWrapper {

    private static final ThreadLocal<Integer> sCounter = new ThreadLocal<>();

    private static int count() {
        Integer count = sCounter.get();
        return count != null ? count : 0;
    }

    public static void begin(int depth, String sectionName) {
        int count = count();
        if (count < depth) {
            beginSection(sectionName);
        }
        sCounter.set(count + 1);
    }

    public static void end(int depth) {
        int count = count();
        if (count <= depth) {
            if (count <= 0) {
                throw new IllegalStateException();
            }
            endSection();
        }
        sCounter.set(count - 1);
    }

    private static void beginSection(String sectionName) {
        throw new RuntimeException("Stub!");
    }

    private static void endSection() {
        throw new RuntimeException("Stub!");
    }

    private TraceWrapper() {
        throw new UnsupportedOperationException();
    }
}
