package com.shancreation.sliatechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LogingActivity extends AppCompatActivity {
private Toolbar mtoolbar;
private TextInputLayout mLogingEmail;
private TextInputLayout mLogingPassword;
private Button mLogin_btn;
private ProgressDialog mLoginProgress;
private DatabaseReference mUserDatabase;

private FirebaseAuth mAuth;


   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth =FirebaseAuth.getInstance();
        setContentView(R.layout.activity_loging);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mtoolbar =(Toolbar) findViewById(R.id.Loging_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Login");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLoginProgress =new ProgressDialog(this);
        mLogingEmail =(TextInputLayout) findViewById(R.id.txtILEmail);
        mLogingPassword=(TextInputLayout) findViewById(R.id.txtILPassword);
        mLogin_btn =(Button) findViewById(R.id.btnLoging);
        mLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mLogingEmail.getEditText().getText().toString();
                String password = mLogingPassword.getEditText().getText().toString();
                if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password))
                {
                    Log.d("Email:",email);
                    Log.d("Pass:",password);
                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("please wait while we check your credentials");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    logingUser(email,password);
                }

            }
        });
    }

    private void logingUser(String email, String password) {

    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {

            if(task.isSuccessful())
            { String deviceToken = FirebaseInstanceId.getInstance().getToken();
                String user_id = mAuth.getCurrentUser().getUid();
                mUserDatabase.child(user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mLoginProgress.dismiss();
                        Intent mainIntent =new Intent(LogingActivity.this,MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    }
                });


            }else
            {
                Log.w("error", "signInWithEmail:failure", task.getException());
                mLoginProgress.hide();
                Toast.makeText(LogingActivity.this, "Cannot Logging Please Check Your Details.!",
                        Toast.LENGTH_LONG).show();

            }

        }
    });

    }
}
