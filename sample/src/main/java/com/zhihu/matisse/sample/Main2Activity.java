package com.zhihu.matisse.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.sobey.matisse.ui.CustomVideoActivity;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        TextView text = findViewById(R.id.text);
        String url = getIntent().getStringExtra(CustomVideoActivity.EXTRA_RESULT_VIDEO_URI);

        String title = getIntent().getStringExtra("title");
        text.setText(title + ":"+url);
    }
}
