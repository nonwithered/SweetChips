package org.sweetchips.test;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class MainApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Main.setLog((tag, msg) -> Log.println(Log.ASSERT, tag, msg));
        Main.setTrace(20, 200, () ->
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getApplicationContext(), "finally", Toast.LENGTH_SHORT).show()));
    }
}
