package org.sweetchips.test;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MainApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        TestLogger.log((tag, msg) -> Log.println(Log.ASSERT, tag, msg));
    }
}
