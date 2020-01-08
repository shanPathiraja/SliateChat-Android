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
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
   //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

        private TextInputLayout mDName;
        private TextInputLayout mEmail;
        private TextInputLayout mPassword;
        private Button mCreate;

      private Toolbar mtoolbar;
        //progress dialog
    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Assign Firebase
        mAuth =FirebaseAuth.getInstance();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mtoolbar =(Toolbar) findViewById(R.id.Loging_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         mRegProgress =new ProgressDialog(this);


        mDName =(TextInputLayout) findViewById(R.id.txtLRegDName);
        mEmail =(TextInputLayout) findViewById((R.id.txtILEmail));
        mPassword =(TextInputLayout) findViewById(R.id.txtILPassword);
        mCreate=(Button) findViewById(R.id.btnCreate);

        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name =mDName.getEditText().getText().toString();
                String email =mEmail.getEditText().getText().toString();
                String password =mPassword.getEditText().getText().toString();
                Log.d("error",name);
                Log.d("email:",email);
                Log.d("password",password);
                if(!TextUtils.isEmpty(name)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password))
                {
                  mRegProgress.setTitle("Registering user");
                  mRegProgress.setMessage("Please wait while we creating account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(name,email,password);
                }


            }
        });
    }

    private void register_user(final String name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //get current user
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

                            String UID =current_user.getUid();
                            mDatabase =FirebaseDatabase.getInstance().getReference().child("Users").child(UID);

                            HashMap<String,String> userMap =new HashMap<>();
                            userMap.put("name",name);
                            userMap.put("status","Hi There im using SLIATE Chat APP");
                            userMap.put("image","default");
                            userMap.put("thumb_Image","default");
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mRegProgress.dismiss();
                                        Intent mainIntent =new Intent(RegisterActivity.this,MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();

                                    }
                                }
                            });


                        } else {
                              mRegProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Cannot Register Please Check Your Details.!",
                                    Toast.LENGTH_LONG).show();
                            task.getException();
                        }

                        // ...
                    }
                });

    }
}
