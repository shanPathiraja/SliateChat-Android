package com.shancreation.sliatechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.shancreation.sliatechat.Model.CallService;
import com.shancreation.sliatechat.Model.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser,mChatUserName,mChatUserThumb;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private ImageButton mVideoChat;
    private ImageButton mVoiceChat;

    private CircleImageView mProfileImage;

    private ImageButton mChatAttach;
    private ImageButton mChatSend;
    private EditText mChatMessage;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mMessageList;
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private DatabaseReference mMessageDatabase;

    private static final int TORAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage =1;

    private int itemPosition =0;

    private String mlastKey="";
    private String mPrevKey="";
    CallService callService;



private final List <Messages> messageList =new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatUser =getIntent().getStringExtra("UID");
        mChatUserName =getIntent().getStringExtra("Uname");
        mChatUserThumb=getIntent().getStringExtra("thumb");
        mRootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();



        mChatToolbar =(Toolbar) findViewById(R.id.Chat_app_bar);
        setSupportActionBar(mChatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(null);

        final LayoutInflater inflater =(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_coustom_bar,null);
        ;

        getSupportActionBar().setCustomView(actionBarView);

         callService =new CallService(ChatActivity.this);

        mTitleView = (TextView) findViewById(R.id.tv_coustomChat_Name);
        mLastSeenView=(TextView) findViewById(R.id.tv_coustomChat_LastSeen);
        mVideoChat =(ImageButton) findViewById(R.id.btn_video_call);
        mVoiceChat =(ImageButton) findViewById(R.id.btn_voice_call);




        mChatAttach =(ImageButton) findViewById(R.id.btn_chatAttach);
        mChatSend = (ImageButton) findViewById(R.id.btn_chatSend);
        mChatMessage=(EditText) findViewById(R.id.et_chatMessage);

        mAdapter = new MessageAdapter(messageList);

        mMessageList =(RecyclerView) findViewById(R.id.Messages_list);
        mRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.swip_message_layout);
        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mLinearLayout.setStackFromEnd(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mAdapter);


        loadMessages();

        mVoiceChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                callService.callUser(mChatUser, ChatActivity.this);
            }
        });


        mTitleView.setText(mChatUserName);
mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        String online = dataSnapshot.child("Online").getValue().toString();
        String img = dataSnapshot.child("thumb_Image").getValue().toString();
        if(online.equals("true"))
        {
            mLastSeenView.setText("Online");

        }else {

            mLastSeenView.setText(GetTimeAgo.getTimeAgo(Long.parseLong(online),ChatActivity.this));
        }

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});

mRootRef.child("Chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if(!dataSnapshot.hasChild(mChatUser)){
            Map ChatAddMap = new HashMap();
            ChatAddMap.put("Seen",false);
            ChatAddMap.put("Time", ServerValue.TIMESTAMP);

            Map ChatUserMap =new HashMap();
            ChatUserMap.put("Chat/"+mCurrentUserID+"/"+mChatUser,ChatAddMap);
            ChatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUserID,ChatAddMap);

            mRootRef.updateChildren(ChatUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError !=null)
                        {
                            Log.d("CHAT LOG",databaseError.getMessage().toString());
                        }
                }
            });
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});






mChatSend.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {

        sendMessage();
    }
});

 mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
     @Override
     public void onRefresh() {
         itemPosition=0;
         mCurrentPage++;
        loadMoreMessage();
     }
 });




    }



    private void loadMoreMessage(){
        DatabaseReference messageRef =mRootRef.child("Messages").child(mCurrentUserID).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mlastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);

                if(!mPrevKey.equals(dataSnapshot.getKey()))
                {
                    messageList.add(itemPosition++,messages);
                }else {
                        mPrevKey =mlastKey;
                }
                if(itemPosition==1)
                {
                    mlastKey =dataSnapshot.getKey();

                }

                Log.d("TOTALKEYS","LASTKEY:"+mlastKey+"|PREVKEY:"+mPrevKey+"|messageKey:"+dataSnapshot.getKey());
                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void loadMessages() {
        DatabaseReference messageRef =mRootRef.child("Messages").child(mCurrentUserID).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage *TORAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                itemPosition++;
                if(itemPosition==1){
                    mlastKey = dataSnapshot.getKey();
                    mPrevKey=dataSnapshot.getKey();

                }
                Messages messages = dataSnapshot.getValue(Messages.class);
                messageList.add(messages);
                mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(messageList.size()-1);
                mRefreshLayout.setRefreshing(false);



            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {
        String message = mChatMessage.getText().toString();
        if(!TextUtils.isEmpty(message))
        {
            String current_usr_ref ="Messages/"+mCurrentUserID+"/"+mChatUser;
            String chat_usr_ref    ="Messages/"+mChatUser+"/"+mCurrentUserID;

            DatabaseReference User_message_push = mRootRef.child("Messages").child(mCurrentUserID).child(mChatUser).push();

            String push_id = User_message_push.getKey();


            Map messageMap = new HashMap();
            messageMap.put("Message",message);
            messageMap.put("Seen",false);
            messageMap.put("Type", "text");
            messageMap.put("Time",ServerValue.TIMESTAMP);
            messageMap.put("From",mCurrentUserID);

            Map MessageUserMap = new HashMap();
            MessageUserMap.put(current_usr_ref+"/"+push_id,messageMap);
            MessageUserMap.put(chat_usr_ref+"/"+push_id,messageMap);

            mChatMessage.setText("");
            mMessageList.scrollToPosition(messageList.size()-1);

            mRootRef.updateChildren(MessageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                   // Log.d("CHAT LOG",databaseError.getMessage().toString());
                }
            });
        }
    }

}
