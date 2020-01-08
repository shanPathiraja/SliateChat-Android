package com.shancreation.sliatechat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shancreation.sliatechat.Model.Messages;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
private List<Messages> messagesList;
private FirebaseAuth mAuth;
private DatabaseReference UserDataBase;

public MessageAdapter(List<Messages> mMessageList){
    this.messagesList = mMessageList;
}

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
           mAuth =FirebaseAuth.getInstance();
           String mCurrentUser = mAuth.getCurrentUser().getUid();
            UserDataBase= FirebaseDatabase.getInstance().getReference().child("Users");

            Messages messages = messagesList.get(position);

            String FromUser = messages.getFrom();
            if(FromUser.equals(mCurrentUser))
            {
                holder.messageText.setVisibility(View.INVISIBLE);
                holder.timeText.setVisibility(View.INVISIBLE);
                holder.reciveProfile.setVisibility(View.INVISIBLE);

                holder.msgSentText.setVisibility(View.VISIBLE);
                holder.sentProfile.setVisibility(View.VISIBLE);


                holder.msgSentTime.setVisibility(View.VISIBLE);



                UserDataBase.child(mCurrentUser).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       final String url =dataSnapshot.child("thumb_Image").getValue().toString();

                        if(!url.equals("defalt")) {
                            // Picasso.get().load(users.getThumb_Image()).placeholder(R.drawable.dp).into(mProfileImage);
                            Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(holder.sentProfile, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(url).placeholder(R.drawable.dp).into(holder.sentProfile);
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }else {

                holder.msgSentText.setVisibility(View.INVISIBLE);
                holder.sentProfile.setVisibility(View.INVISIBLE);
                holder.msgSentTime.setVisibility(View.INVISIBLE);

                holder.messageText.setVisibility(View.VISIBLE);
                holder.timeText.setVisibility(View.VISIBLE);
                holder.reciveProfile.setVisibility(View.VISIBLE);
            }
            holder.messageText.setText(messages.getMessage());

            holder.msgSentText.setText(messages.getMessage());
        SimpleDateFormat formater = new SimpleDateFormat("hh:mma dd,MM");
        String date = formater.format(new Date(Long.parseLong(messages.getTime().toString())));
        holder.timeText.setText(date);
            holder.msgSentTime.setText(date);
        Log.d("FROM",messages.getFrom());

        UserDataBase.child(messages.getFrom()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String url =dataSnapshot.child("thumb_Image").getValue().toString();

                if(!url.equals("defalt")) {
                    // Picasso.get().load(users.getThumb_Image()).placeholder(R.drawable.dp).into(mProfileImage);
                    Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(holder.reciveProfile, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(url).placeholder(R.drawable.dp).into(holder.reciveProfile);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

            public TextView messageText;
            public  TextView timeText;
            public  TextView msgSentText;
            public TextView msgSentTime;
            public CircleImageView reciveProfile;
            public CircleImageView sentProfile;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText =(TextView) itemView.findViewById(R.id.message_text_layout);
            timeText =(TextView) itemView.findViewById(R.id.message_time_text);
            msgSentText=(TextView) itemView.findViewById(R.id.msgSentText);
            msgSentTime=(TextView) itemView.findViewById(R.id.msg_sent_time_text);
            reciveProfile=(CircleImageView) itemView.findViewById(R.id.messageProfile_layout);
            sentProfile=(CircleImageView)itemView.findViewById(R.id.msgSentProfile_layout);

        }
    }
}
