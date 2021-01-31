package org.sweetchips.test;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.sweetchips.annotation.Uncheckcast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Uncheckcast
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Main.main(null);
        try {
            String str = (String) new Object();
        } catch (ClassCastException e) {
            Log.println(Log.ASSERT, TAG, e.toString());
        }
    }
}
