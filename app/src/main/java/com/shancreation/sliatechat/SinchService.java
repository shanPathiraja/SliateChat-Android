package com.shancreation.sliatechat;

import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.MissingPermissionException;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;


import androidx.annotation.Nullable;

public class SinchService extends Service {
    private static final String APP_KEY = "enter-application-key";
    private static final String APP_SECRET = "enter-application-secret";
    private static final String ENVIRONMENT = "clientapi.sinch.com";

    public static final int MESSAGE_PERMISSIONS_NEEDED = 1;
    public static final String REQUIRED_PERMISSION = "REQUIRED_PESMISSION";
    public static final String MESSENGER = "MESSENGER";
    private Messenger messenger;

    public static final String CALL_ID = "CALL_ID";
    static final String TAG = SinchService.class.getSimpleName();

    private SinchServiceInterface mSinchServiceInterface = new SinchServiceInterface();
    private SinchClient mSinchClient;
    private String mUserId;

    private StartFailedListener mListener;
    private PersistedSettings mSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = new PersistedSettings(getApplicationContext());
        attempAutoStart();
    }

    private void attempAutoStart() {
        String userName = mSettings.getUserName();
        if(!userName.isEmpty()&&messenger!=null)
        {
            start(userName);
        }
    }

    private void start(String userName) {
    boolean PermisionGaranted =true;
    if(mSinchClient ==null)
    {
        mSettings.setUserName(userName);
        createClient(userName);
    }

    }

    private void createClient(String userName) {

        mUserId = userName;
        mSinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext()).userId(userName)
                .applicationKey(APP_KEY).applicationSecret(APP_SECRET).environmentHost(ENVIRONMENT).build();

        mSinchClient.setSupportCalling(true);
        mSinchClient.startListeningOnActiveConnection();
        mSinchClient.addSinchClientListener(new MySinchClientListener());
        mSinchClient.getCallClient().setRespectNativeCalls(false);
        mSinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
    }
    private void stop(){

        if(mSinchClient!=null)
        {
            mSinchClient.terminateGracefully();
            mSinchClient=null;
        }
        mSettings.setUserName("");
            }

            private boolean isStarted(){
            return (mSinchClient !=null && mSinchClient.isStarted());
            }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        messenger =intent.getParcelableExtra(MESSENGER);

        return mSinchServiceInterface;
    }


    public class SinchServiceInterface extends Binder{
        public Call callPhoneNumber(String phoneNumber) {
            return mSinchClient.getCallClient().callPhoneNumber(phoneNumber);
        }

        public Call callUser(String userId) {
            if (mSinchClient == null) {
                return null;
            }
            return mSinchClient.getCallClient().callUser(userId);
        }

        public String getUserName() {
            return mUserId;
        }

        public boolean isStarted() {
            return SinchService.this.isStarted();
        }

        public void retryStartAfterPermissionGranted() {
            SinchService.this.attempAutoStart();
        }

        public void startClient(String userName) {
            start(userName);
        }

        public void stopClient() {
            stop();
        }

        public void setStartListener(StartFailedListener listener) {
            mListener = listener;
        }

        public Call getCall(String callId) {
            return mSinchClient != null ? mSinchClient.getCallClient().getCall(callId) : null;
        }

        public AudioController getAudioController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getAudioController();
        }



    }


    public interface StartFailedListener {
        void onStartFailed(SinchError error);

        void onStarted();
    }




    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientStarted(SinchClient sinchClient) {

        }

        @Override
        public void onClientStopped(SinchClient sinchClient) {

        }

        @Override
        public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {

        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {

        }

        @Override
        public void onLogMessage(int i, String s, String s1) {

        }
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call call) {

        }
    }
    private class PersistedSettings {
        private SharedPreferences mStore;
        private static final String PREF_KEY ="Sinch";

        public PersistedSettings(Context context){
            mStore = context.getSharedPreferences(PREF_KEY,MODE_PRIVATE);
        }

       public  String getUserName(){
            return  mStore.getString("Username","");
       }
        public void setUserName(String username){
            SharedPreferences.Editor editor = mStore.edit();
            editor.putString("Username",username);
            editor.commit();
        }

    }
}
