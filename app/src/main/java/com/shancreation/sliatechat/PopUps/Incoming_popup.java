package com.shancreation.sliatechat.PopUps;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.shancreation.sliatechat.Model.CallService;
import com.shancreation.sliatechat.R;


public class Incoming_popup extends AppCompatActivity {


    String name ="";

    private View mContentView;
    public static Activity incom;
    private Button mAnswer,mReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_incoming_popup);
        name =getIntent().getStringExtra("name");
        incom =this;



        mContentView = findViewById(R.id.fullscreen_content);
        mAnswer =findViewById(R.id.btn_answer);

        mAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }









}
