package com.jifalops.toolbox;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by jacob.phillips on 11/6/15.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView) findViewById(R.id.text);
//        String java = new JavaToolboxTest().getString();
//        String android = new AndroidToolboxTest().getString();
//        tv.setText(android);
    }
}
