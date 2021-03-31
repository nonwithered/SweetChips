package org.sweetchips.demo.app;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.sweetchips.demo.main.Main;

public final class MainApplication extends Application {

    @Override
    protected final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Main.setLog((tag, msg) -> Log.println(Log.ASSERT, tag, msg));
        Main.setTrace(20, 200, () ->
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getApplicationContext(), "finally", Toast.LENGTH_SHORT).show()));
    }
}
