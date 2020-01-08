package com.shancreation.sliatechat.Model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shancreation.sliatechat.MainActivity;
import com.shancreation.sliatechat.PopUps.CallDialog;
import com.shancreation.sliatechat.PopUps.Incoming_popup;
import com.shancreation.sliatechat.R;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

public class CallService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";


    static Call call;
    SinchClient sinchClient;
    FirebaseUser mCUser;
    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    String name="";
    Context cntx;
   public static Context callserv;
    boolean isincoming =false;

    @Override
    public void onCreate() {
        super.onCreate();
        int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, new Notification.Builder(this).build());
        }

        callserv=this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public CallService() {
    }

    public CallService(Context context) {

        cntx =context;

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mCUser = mAuth.getCurrentUser();

        sinchClient = Sinch.getSinchClientBuilder()
                .context(context)
                .userId(mCUser.getUid())
                .applicationKey("c4cdd327-b456-4e1e-9428-861696c6c9a6")
                .applicationSecret("Js5Flo8kQEe0n3galX92qw==")
                .environmentHost("clientapi.sinch.com")
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListner(){

        });
        sinchClient.start();
    }

    public void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel serviceChanel =new NotificationChannel(
                    CHANNEL_ID,
                    "Forground service Chane"
                    , NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChanel);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            createNotificationChannel();
            Intent notificationIntent = new Intent(this , MainActivity.class);
            PendingIntent pendingIntent =PendingIntent.getActivity(this,0,notificationIntent,0);
            Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setContentTitle("ForgroundService")
                    .setContentText("SLIATE CHAT NOTOFICATION")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1,notification);
        }

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mCUser = mAuth.getCurrentUser();

        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(mCUser.getUid())
                .applicationKey("c4cdd327-b456-4e1e-9428-861696c6c9a6")
                .applicationSecret("Js5Flo8kQEe0n3galX92qw==")
                .environmentHost("clientapi.sinch.com")
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListner(){

        });
        sinchClient.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public class SinchCallClientListner implements CallClientListener{

        @Override
        public void onIncomingCall(CallClient callClient, Call incomingcall) {
            isincoming =true;
            call =incomingcall;
            Log.d("INCOMING CALL ID",incomingcall.getRemoteUserId());
                dbRef.child(call.getRemoteUserId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        name = dataSnapshot.child("name").getValue().toString();
                        Intent intent =new Intent(getApplicationContext(), Incoming_popup.class);
                        intent.putExtra("name",name);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        }
    }
    public void callUser(String id, Context context)
    {
        isincoming =false;
        if(call==null)
        {
            call= sinchClient.getCallClient().callUser(id);
            call.addCallListener(new SinvhCallListner());

            openCallDialog(call,context);
        }
    }

    private void openCallDialog(Call call,Context context) {
        AlertDialog alertDialog =new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Calling");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "HangUp", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            call.hangup();
        });

        alertDialog.show();

    }

    public static void CallAnswer(){
        call.answer();
        call.addCallListener(new SinvhCallListner());
    }
    public void CallHangup(){
        call.hangup();
    }

    private static class SinvhCallListner implements CallListener {
        @Override
        public void onCallProgressing(Call call) {
            Log.e("INCOMING","progress");
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.e("INCOMING","establish");
        }

        @Override
        public void onCallEnded(Call call) {
            Log.e("INCOMING","end");


        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }
}
