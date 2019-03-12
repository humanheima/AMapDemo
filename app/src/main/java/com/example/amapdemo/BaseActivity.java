package com.example.amapdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Crete by dumingwei on 2019/2/28
 * Desc:
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getName(), "onCreate: ");
    }
}
