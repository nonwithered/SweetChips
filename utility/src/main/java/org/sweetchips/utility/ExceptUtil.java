package org.sweetchips.utility;

import java.util.concurrent.Callable;

interface ExceptUtil {

    interface RunnableThrows {

        void run() throws Exception;
    }

    static <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void run(RunnableThrows runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
