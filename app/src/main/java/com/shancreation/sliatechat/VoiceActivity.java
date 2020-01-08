package com.shancreation.sliatechat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;

public class VoiceActivity extends AppCompatActivity {
    Call call;
    FirebaseAuth mAuth;
    FirebaseUser mCUser;
    SinchClient sinchClient;
    DatabaseReference dbRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth =FirebaseAuth.getInstance();
        mCUser = mAuth.getCurrentUser();

        sinchClient = Sinch.getSinchClientBuilder()
                        .context(this)
                .userId(mCUser.getUid())
                .applicationKey("")
                .applicationSecret("")
                .environmentHost("")
                .build();
        sinchClient.setSupportCalling(true);





    }
}
