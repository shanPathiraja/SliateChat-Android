package com.shancreation.sliatechat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class NotificationHelper extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    Timer timer;
    TimerTask timerTask;
    String Tag ="timers";
    int sec =60;
    DatabaseReference notyfyDBRef;
    FirebaseAuth mAuthDB;
    DatabaseReference mUserDB;
    int mNotifyID=1;

    @Override
    public void onCreate() {
        super.onCreate();
        startServiceOreoCondition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        }
    }
    private void startServiceOreoCondition(){
        if (Build.VERSION.SDK_INT >= 26) {


            String CHANNEL_ID = "my_service";
            String CHANNEL_NAME = "My Background Service";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,NotificationManager.IMPORTANCE_NONE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_SERVICE).setSmallIcon(R.mipmap.ic_launcher).setPriority(PRIORITY_MIN).build();

            startForeground(101, notification);
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel serviceChanel =new NotificationChannel(
                    CHANNEL_ID,
                    "Forground service Chane"
                    ,NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChanel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            createNotificationChannel();
            Intent notificationIntent = new Intent(this ,MainActivity.class);
            PendingIntent pendingIntent =PendingIntent.getActivity(this,0,notificationIntent,0);
            Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setContentTitle("ForgroundService")
                    .setContentText("SLIATE CHAT NOTOFICATION")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1,notification);
        }

        mAuthDB=FirebaseAuth.getInstance();
        notyfyDBRef = FirebaseDatabase.getInstance().getReference().child("Notifications").child(mAuthDB.getCurrentUser().getUid());
        mUserDB=FirebaseDatabase.getInstance().getReference().child("Users");

        ReqLisner();
        return super.onStartCommand(intent, flags, startId);

    }

        private void ReqLisner(){
        notyfyDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e("TRY",dataSnapshot.child("from").getValue().toString());
                final String reqid=dataSnapshot.child("from").getValue().toString();
                final String type =dataSnapshot.child("type").getValue().toString();
                mUserDB.child(reqid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String ReqName = dataSnapshot.child("name").getValue().toString();

                        if(type.equals("Request")){


                           Intent notyfyIntent = new Intent(NotificationHelper.this,ProfileActivity.class);
                           notyfyIntent.putExtra("UID",reqid);
                           notyfyIntent.putExtra("isnotify",true);
                            PendingIntent pendingIntent =PendingIntent.getActivity(NotificationHelper.this,0,notyfyIntent,PendingIntent.FLAG_UPDATE_CURRENT);



                          NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(NotificationHelper.this)
                                  .setContentIntent(pendingIntent)
                                    .setSmallIcon(android.R.drawable.stat_notify_error)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                                    .setContentTitle("New Friend Request")
                                    .setContentText("From "+ReqName)
                                  .setAutoCancel(true);



                            notificationBuilder.setDefaults(
                                    Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(NotificationHelper.this);
                            notificationManager.notify(mNotifyID,notificationBuilder.build());

                            mNotifyID++;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


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



}
