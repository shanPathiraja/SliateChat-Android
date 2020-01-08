package com.shancreation.sliatechat;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

class FriendsViewHalder extends RecyclerView.ViewHolder {

    public static View mView;

    public FriendsViewHalder(@NonNull View itemView) {
        super(itemView);
        mView =itemView;

    }

    public void  setDate(String date){
        TextView sates = (TextView) mView.findViewById(R.id.tv_userStatus);
        sates.setText(date);
    }
    public void  setName(String Name){
        TextView mUName=(TextView)mView.findViewById(R.id.tv_userName);
        mUName.setText(Name);
    }
    public void setThumb(final String url){
        Log.e("URL",url);
        final CircleImageView mProfileImage=(CircleImageView)mView.findViewById(R.id.circleImageView);
        if(!url.equals("defalt")) {
            // Picasso.get().load(users.getThumb_Image()).placeholder(R.drawable.dp).into(mProfileImage);
            Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(mProfileImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(url).placeholder(R.drawable.dp).into(mProfileImage);
                }
            });

        }
    }
    public void setOnline(String online_state){
        ImageView usrOnline = (ImageView) mView.findViewById(R.id.imgView_Online);

        if (online_state.equals("true")) {
            usrOnline.setVisibility(View.VISIBLE);
        }else {
            usrOnline.setVisibility(View.INVISIBLE);
        }
    }
}
