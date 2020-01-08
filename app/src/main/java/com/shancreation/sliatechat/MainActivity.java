package com.shancreation.sliatechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.shancreation.sliatechat.Model.CallService;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mtoolBar;
    private ViewPager mViewPager;
    private SectionPage mSectionPager;
    private TabLayout mTabLayout;
    private DatabaseReference mUserRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {
            sendToStart();
        }else {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }
        mtoolBar=(Toolbar) findViewById(R.id.main_page_tool_bar);
        setSupportActionBar(mtoolBar);
        getSupportActionBar().setTitle("SLIATE chat");



        //Tabs
        mViewPager=(ViewPager) findViewById(R.id.Main_Tab_Pager);
        mSectionPager =new SectionPage(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPager);
        mTabLayout = (TabLayout) findViewById(R.id.main_Tabs);
        mTabLayout.setupWithViewPager(mViewPager);



    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if(currentUser==null)
        {
           sendToStart();
        }else {
            mUserRef.child("Online").setValue("true");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        MainActivity.this.startForegroundService(new Intent(this, NotificationHelper.class));

                        MainActivity.this.startForegroundService(new Intent(this, CallService.class));


                }

            else {
                startService(new Intent(this, NotificationHelper.class));
                startService(new Intent(this, CallService.class));
            }
        }
    }

    public static boolean isServiceRun(Context context, Class<?> clas )
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service:manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (service.foreground){
                return true;
            }
        }
        return false;

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(currentUser==null)
        {
            sendToStart();
        }else {
            mUserRef.child("Online").setValue(ServerValue.TIMESTAMP);


        }

    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if(item.getItemId()==R.id.main_setting)
        {
            Intent SettingInstance = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(SettingInstance);
        }
        if(item.getItemId()==R.id.main_all_btn)
        {
            Intent allUSer = new Intent(MainActivity.this,UserActivity.class);
            startActivity(allUSer);
        }
        return true;

    }
}
