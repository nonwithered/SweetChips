package org.sweetchips.test;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.sweetchips.annotation.Uncheckcast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler mHandler;

    @Uncheckcast
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        Main.main(null);
        try {
            String str = (String) new Object();
        } catch (ClassCastException e) {
            Log.println(Log.ASSERT, TAG, e.toString());
        }
        new TestThread(20, 400).start();
    }

    private final class TestThread extends Thread {

        private final int mDepth;
        private final long mTime;

        private TestThread(int depth, long time) {
            mDepth = depth;
            mTime = time;
            setName(getClass().getName());
        }

        @Override
        public void run() {
            try {
                r(mDepth);
            } catch (Throwable e) {
                Log.println(Log.ASSERT, TAG, e.toString());
            } finally {
                mHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "finally", Toast.LENGTH_SHORT).show();
                });
            }
        }

        private void r(int depth) throws InterruptedException {
            if (depth > 0) {
                Thread.sleep(mTime);
                r(depth - 1);
            }
        }
    }
}
