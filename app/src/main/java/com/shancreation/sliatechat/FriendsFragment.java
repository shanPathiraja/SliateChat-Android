package com.shancreation.sliatechat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shancreation.sliatechat.Model.Friends;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment<FriendsHolder> extends Fragment {


    //Firebase
    private String mCUID;
    private DatabaseReference mdbref;
    private RecyclerView mFriendList;
    private View mMainView;
    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter adapter;
    private  DatabaseReference mUserDatabase;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mdbref = FirebaseDatabase.getInstance().getReference().child("Friend").child(mAuth.getCurrentUser().getUid());
        mdbref.keepSynced(true);
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendList = (RecyclerView) mMainView.findViewById(R.id.friend_List);
        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));


        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();


        Query query = mdbref.orderByKey();

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();

       adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHalder>(options) {

            @NonNull
            @Override
            public FriendsViewHalder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout,parent,false);
                return new FriendsViewHalder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHalder holder, int position, @NonNull Friends model) {
                    holder.setDate(model.getDate());
                    final String lst_usr_id = getRef(position).getKey();
                    mUserDatabase.child(lst_usr_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final String usrName = dataSnapshot.child("name").getValue().toString();
                            final String usrThumb = dataSnapshot.child("thumb_Image").getValue().toString();


                            holder.setName(usrName);
                            holder.setThumb(usrThumb);
                            if(dataSnapshot.hasChild("Online")) {
                                String usr_Online = dataSnapshot.child("Online").getValue().toString();
                                holder.setOnline(usr_Online);
                            }

                            FriendsViewHalder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CharSequence option[] =new CharSequence[]{"Open Profile","Send message"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle(usrName);
                                    builder.setItems(option, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if(i==0){
                                                Intent profileIntent =new Intent(getContext(),ProfileActivity.class);
                                                profileIntent.putExtra("UID",lst_usr_id);
                                                startActivity(profileIntent);
                                            }else if (i==1){
                                                Intent ChatIntent =new Intent(getContext(),ChatActivity.class);
                                                ChatIntent.putExtra("UID",lst_usr_id);
                                                ChatIntent.putExtra("Uname",usrName);
                                                ChatIntent.putExtra("thumb",usrThumb);
                                                startActivity(ChatIntent);
                                            }


                                        }
                                    });
                                    builder.show();


                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            }
        };

        mFriendList.setAdapter(adapter);

        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}