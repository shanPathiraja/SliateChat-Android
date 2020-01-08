package com.shancreation.sliatechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
private Button mRegistor;
private Button mLoging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mRegistor = (Button) findViewById(R.id.btnRegister);
        mLoging=(Button)findViewById(R.id.btnLoging);
        mLoging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent =new Intent(StartActivity.this,LogingActivity.class);
                startActivity(loginIntent);
            }
        });
        mRegistor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent =new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(regIntent);
            }
        });
    }
}
