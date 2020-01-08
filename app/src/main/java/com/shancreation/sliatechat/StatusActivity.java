package com.shancreation.sliatechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {


    private Toolbar mAppbar;
    private Button mSaveStatus;
    private TextInputLayout mStatus;
    //Firebase
    private DatabaseReference mStatusDB;
    private FirebaseUser mCurrentUser;
    //Progress dialog
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //fireBase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID=mCurrentUser.getUid();
        mStatusDB = FirebaseDatabase.getInstance().getReference().child("Users").child(UID);
        mAppbar =(Toolbar) findViewById(R.id.statusAppBar);
        setSupportActionBar(mAppbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSaveStatus=(Button) findViewById(R.id.btn_Save_change);
        mStatus =(TextInputLayout)findViewById(R.id.til_status);

        String current_status =getIntent().getStringExtra("status");
        mStatus.getEditText().setText(current_status);



        mSaveStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Progress dialog
                mProgress =new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait...");
                mProgress.show();
                String status = mStatus.getEditText().getText().toString();
                mStatusDB.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                        }else
                        {
                            Toast.makeText(getApplicationContext(),"Cannot Update Status",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

    }
}
