package com.shancreation.sliatechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
private TextView mdisID;
private ImageView mPimg;
private TextView mPStatus;
private TextView mPFriendCount;
private Button mPSendReq;
private ProgressDialog mprogressDialog;
private Button mRejectReq;
private int mCurrent_State;
//Firebase
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqestDatabase;
    private DatabaseReference mFriendDatabase;

    private DatabaseReference mNotificationDatabase;
    private FirebaseUser mCurrentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String UID =getIntent().getStringExtra("UID");
        final boolean isnotify =getIntent().getBooleanExtra("isnotify",false);



        Log.e("UID",UID);

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(UID);
        mUserDatabase.keepSynced(true);
        mFriendReqestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_Req");
        mFriendDatabase =FirebaseDatabase.getInstance().getReference().child("Friend");
        mNotificationDatabase =FirebaseDatabase.getInstance().getReference().child("Notifications");


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        mdisID =(TextView) findViewById(R.id.tv_name);
        mPimg=(ImageView) findViewById(R.id.imgV_Profile);
        mPStatus =(TextView)findViewById(R.id.tv_UStatus);
        mPFriendCount= (TextView) findViewById(R.id.tv_UFriends);
        mPSendReq = (Button) findViewById(R.id.btn_SendRequest);
        mRejectReq=(Button) findViewById(R.id.btn_rejectRequest);

        //0= not Friend;
        mCurrent_State=0;

        mprogressDialog =new ProgressDialog(this);
        mprogressDialog.setTitle("Profile is Loading");
       mprogressDialog.setMessage("Please wait...");
        mprogressDialog.setCanceledOnTouchOutside(false);
        mprogressDialog.show();
        if(isnotify){
            removeNotification(UID);
        }

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name= dataSnapshot.child("name").getValue().toString();
                String display_status= dataSnapshot.child("status").getValue().toString();
                final String display_image= dataSnapshot.child("image").getValue().toString();
                //String display_name= dataSnapshot.child("name").getValue().toString();
               // Log.d("Status",display_status);
                //Log.d("Image",display_image);
                //Log.d("Name",display_name);
               mdisID.setText(display_name);
                mPStatus.setText(display_status);
                //Picasso.get().load(display_image).placeholder(R.drawable.dp).into(mPimg);
                Picasso.get().load(display_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(mPimg, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(display_image).placeholder(R.drawable.dp).into(mPimg);
                    }
                });


                //-------------------Friend List / Request -------------------------------------------

                mFriendReqestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(UID))
                        {
                            String req_Type = dataSnapshot.child(UID).child("Request_Type").getValue().toString();
                            if (req_Type.equals("Received")) {
                                mCurrent_State = 3;
                                mPSendReq.setText("Accept Friend Request");
                                mPSendReq.setBackgroundColor(getResources().getColor(R.color.sucess));
                                mRejectReq.setVisibility(View.VISIBLE);
                                mRejectReq.setEnabled(true);

                            }
                            else if (req_Type.equals("Sent"))
                            {
                                mCurrent_State = 1;
                                mPSendReq.setText("Cancel Friend Request");
                                mPSendReq.setBackgroundColor(getResources().getColor(R.color.danger));
                                mRejectReq.setVisibility(View.INVISIBLE);
                                mRejectReq.setEnabled(false);

                            }
                        }
                        else
                        {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(UID)){
                                        mCurrent_State = 4;
                                        mPSendReq.setText("Unfriend");
                                        mPSendReq.setBackgroundColor(getResources().getColor(R.color.gray));
                                        mRejectReq.setVisibility(View.INVISIBLE);
                                        mRejectReq.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        mprogressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mPSendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //----------------------------------------NOT FRIEND STATE---------------------------------------------
                mPSendReq.setEnabled(false);
                //check Persion is not friend
                if(mCurrent_State==0){

                        mFriendReqestDatabase.child(mCurrentUser.getUid()).child(UID).child("Request_Type")
                                .setValue("Sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mFriendReqestDatabase.child(UID).child(mCurrentUser.getUid()).child("Request_Type")
                                            .setValue("Received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            HashMap<String,String> notificationData =new HashMap<>();
                                            notificationData.put("from",mCurrentUser.getUid());
                                            notificationData.put("type","Request");

                                            mNotificationDatabase.child(UID).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mRejectReq.setVisibility(View.VISIBLE);
                                                    mRejectReq.setEnabled(true);
                                                    mPSendReq.setText("Cancel Friend Request");
                                                    mPSendReq.setBackgroundColor(getResources().getColor(R.color.danger));

                                                    mCurrent_State=1;
                                                    Toast.makeText(ProfileActivity.this,"Request Sent Success",Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                           }
                                    });

                                }else {
                                    Toast.makeText(ProfileActivity.this,"Request Sent Fail",Toast.LENGTH_SHORT).show();
                                }
                                mPSendReq.setEnabled(true);
                            }
                        });




                }
                if(mCurrent_State==1){
                    //----------------------------------CANCEL FRIEND REQUEST--------------------------------------------------------


                    mFriendReqestDatabase.child(mCurrentUser.getUid()).child(UID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqestDatabase.child(UID).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mRejectReq.setVisibility(View.INVISIBLE);
                                    mRejectReq.setEnabled(false);
                                    mPSendReq.setText("Send Friend Request");
                                    mPSendReq.setBackgroundColor(getResources().getColor(R.color.sucess));
                                    mPSendReq.setEnabled(true);
                                    mCurrent_State=0;
                                    Toast.makeText(ProfileActivity.this,"Request is Canceled",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }
                if(mCurrent_State==3)
                {
                    //---------------------------------Request Received State------------------------------------------
                    final String CurrentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(mCurrentUser.getUid()).child(UID).child("date").setValue(CurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendDatabase.child(UID).child(mCurrentUser.getUid()).child("date").setValue(CurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mFriendReqestDatabase.child(mCurrentUser.getUid()).child(UID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mFriendReqestDatabase.child(UID).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mRejectReq.setVisibility(View.INVISIBLE);
                                                        mRejectReq.setEnabled(false);
                                                        mPSendReq.setText("Unfriend");
                                                        mPSendReq.setBackgroundColor(getResources().getColor(R.color.gray));

                                                        mCurrent_State=4;

                                                    }
                                                });
                                            }
                                        });


                                    }
                                });

                            }
                        });
                }

                if(mCurrent_State==4)
                {
                    mFriendDatabase.child(mCurrentUser.getUid()).child(UID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(UID).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mRejectReq.setVisibility(View.INVISIBLE);
                                    mRejectReq.setEnabled(false);
                                    mPSendReq.setText("Send Friend Request");
                                    mPSendReq.setBackgroundColor(getResources().getColor(R.color.sucess));
                                    mPSendReq.setEnabled(true);
                                    mCurrent_State=0;
                                    Toast.makeText(ProfileActivity.this,"Unfriend Success",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }

            }


        });
    }

    private void removeNotification(String uid) {

        mNotificationDatabase.child(mCurrentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this,"Notification Remove Success",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
