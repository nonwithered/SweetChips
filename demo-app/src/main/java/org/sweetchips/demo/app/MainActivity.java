package org.sweetchips.demo.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.sweetchips.demo.main.Main;

public final class MainActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Main.main();
    }
}
