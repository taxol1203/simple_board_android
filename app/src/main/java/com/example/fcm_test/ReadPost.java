package com.example.fcm_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Objects;

public class ReadPost extends AppCompatActivity {

    TextView mTitle;
    TextView mWriter;
    TextView mDate;
    TextView mHit;
    TextView mContent;
    int idx;
    String title, date, content, writer, hit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_post);
        mTitle = findViewById(R.id.read_title);
        mWriter = findViewById(R.id.read_writer);
        mDate = findViewById(R.id.read_date);
        mHit = findViewById(R.id.read_hit);
        mContent = findViewById(R.id.read_content);

        getIntentData();

        mTitle.setText(title);
        mDate.setText(date);
        mWriter.setText(writer);
        mHit.setText(hit);
        mContent.setText(content);
    }

    void getIntentData(){
        Intent intent = getIntent();
        idx = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(intent.getExtras()).getString("idx")));
        hit = Objects.requireNonNull(intent.getExtras()).getString("hit");
        title = Objects.requireNonNull(intent.getExtras()).getString("title");
        writer = Objects.requireNonNull(intent.getExtras()).getString("writer");
        date = Objects.requireNonNull(intent.getExtras()).getString("date");
        content = Objects.requireNonNull(intent.getExtras()).getString("content");
    }
}