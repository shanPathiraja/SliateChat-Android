package com.shancreation.sliatechat;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shancreation.sliatechat.Model.Users;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUserHelper {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDataRef;
    private List<Users> users =new ArrayList<>();
    public interface DataStatus{
        void DataIsLoaded(List<Users> users,List<String> UID);
        void  DataIsInserted();
        void DataIsDeleted();
        void DataIsUpdated();

    }

    public FirebaseUserHelper() {
        mDatabase =FirebaseDatabase.getInstance();
        mDataRef =mDatabase.getReference("Users");
        mDataRef.keepSynced(true);

    }
    public void loadUser(final DataStatus dataStatus)
    {
        mDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                List<String> UID =new ArrayList<>();
                for(DataSnapshot UIDNode:dataSnapshot.getChildren())
                {
                    UID.add(UIDNode.getKey());
                    Users user = UIDNode.getValue(Users.class);
                    users.add(user);

                }
                dataStatus.DataIsLoaded(users,UID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

