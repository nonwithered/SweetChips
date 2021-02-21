package org.sweetchips.test;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.sweetchips.annotation.Uncheckcast;

public class MainActivity extends AppCompatActivity {

    @Uncheckcast
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Main.main();
    }
}
