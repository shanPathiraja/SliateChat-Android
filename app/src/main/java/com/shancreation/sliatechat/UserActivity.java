package com.shancreation.sliatechat;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shancreation.sliatechat.Model.Users;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private RecyclerView mUsersList;


    private UserAdapter mUserAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mToolBar =(Toolbar) findViewById(R.id.allUserAppbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUsersList =(RecyclerView) findViewById(R.id.userList);



        new FirebaseUserHelper().loadUser(new FirebaseUserHelper.DataStatus() {
            @Override
            public void DataIsLoaded(List<Users> users, List<String> UID) {
                findViewById(R.id.pb_AllUser).setVisibility(View.GONE);
                mUserAdapter =new UserAdapter(users,UID);
                mUsersList.setLayoutManager(new LinearLayoutManager(UserActivity.this));
                mUsersList.setAdapter(mUserAdapter);
                //new UserRecyclerViewConfig().SetConfig(mUsersList,UserActivity.this,users,UID);
            }

            @Override
            public void DataIsInserted() {

            }

            @Override
            public void DataIsDeleted() {

            }

            @Override
            public void DataIsUpdated() {

            }
        });


    }

    class UserItemView extends RecyclerView.ViewHolder
    {
        private TextView mUName;
        private TextView mStatus;
        private CircleImageView mProfileImage;
        private String UID;


        public UserItemView(ViewGroup parent) {
            super(LayoutInflater.from(UserActivity.this).inflate(R.layout.users_layout,parent,false));
            mUName=(TextView)itemView.findViewById(R.id.tv_userName);
            mStatus=(TextView)itemView.findViewById(R.id.tv_userStatus);
            mProfileImage=(CircleImageView)itemView.findViewById(R.id.circleImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
        public void bind(final Users users, String UID)
        {
            mUName.setText(users.getName());
            mStatus.setText(users.getStatus());
            if(!users.getThumb_Image().equals("defalt")) {
                // Picasso.get().load(users.getThumb_Image()).placeholder(R.drawable.dp).into(mProfileImage);
                Picasso.get().load(users.getThumb_Image()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(users.getThumb_Image()).placeholder(R.drawable.dp).into(mProfileImage);
                    }
                });

            }
            this.UID =UID;
        }
    }

    class UserAdapter extends RecyclerView.Adapter<UserItemView> {
        private List<Users> mUserList;
        private List<String> mUID;

        public UserAdapter(List<Users> mUserList, List<String> mUID) {
            this.mUserList = mUserList;
            this.mUID = mUID;
            Log.d("mUID", mUID.toString());
        }

        @NonNull
        @Override
        public UserItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new UserItemView(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull UserItemView holder, int position) {
            holder.bind(mUserList.get(position), mUID.get(position));
            final String UserID = mUID.get(position);
            final Users usr =mUserList.get(position);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profileIntent =new Intent(UserActivity.this,ProfileActivity.class);
                    profileIntent.putExtra("UID",UserID);
                    startActivity(profileIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mUserList.size();
        }
    }


}
