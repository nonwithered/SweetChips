package org.sweetchips.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.sweetchips.test.Main;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Main.main();
    }
}
