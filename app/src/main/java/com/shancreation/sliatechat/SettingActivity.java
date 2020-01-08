package com.shancreation.sliatechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {


    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusbtn;
    private Button mImgUpload;

    private  static final int GALLERY_PICK=1;

    //firebase storage

    private StorageReference mImageStorage;
    private StorageReference mThumbStorage;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    int state =0;

    private List <UploadTask> taskList;

    //Progress

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mDisplayImage =(CircleImageView) findViewById(R.id.SettingsImage);
        mName=(TextView) findViewById(R.id.tv_setting_DName);
        mStatus =(TextView) findViewById(R.id.tv_Setting_Status);
        mStatusbtn =(Button)findViewById(R.id.btnChangeStatus);
        mImgUpload =(Button) findViewById(R.id.btnChangeImage);

        //firebase
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID =mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(UID);
        mUserDatabase.keepSynced(true);



        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name =dataSnapshot.child("name").getValue().toString();
                String status =dataSnapshot.child("status").getValue().toString();
                final String image =dataSnapshot.child("image").getValue().toString();

                    String thumb_Image = dataSnapshot.child("thumb_Image").getValue().toString();


                mName.setText(name);
                mStatus.setText(status);
                if(!image.equals("defalt")) {
                //Picasso.get().load(image).placeholder(R.drawable.dp).into(mDisplayImage);
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.dp).into(mDisplayImage);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent updateStatus= new Intent(SettingActivity.this,StatusActivity.class);
                String status =mStatus.getText().toString();
                updateStatus.putExtra("status",status);
                startActivity(updateStatus);
            }
        });

mImgUpload.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
       Intent galleryIntent =new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);


    }
});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK)
        {
            Uri imageUri =data.getData();
           // Toast.makeText(SettingActivity.this,imageUri,Toast.LENGTH_LONG).show();
           // Log.d("URI",imageUri);
            CropImage.activity(imageUri).setAspectRatio(1,1).start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(SettingActivity.this);
                mProgressDialog.setTitle("Uploading Image..");
                mProgressDialog.setMessage("Please wait");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Uri resultUri = result.getUri();
                final String  CUID =mCurrentUser.getUid();

                final File imgPath = new File(resultUri.getPath());



                final StorageReference filePath =mImageStorage.child("profile_images").child(CUID+".jpg");


               // UploadTask imgUpload=filePath.putFile(resultUri);


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            mImageStorage.child("profile_images").child(CUID+".jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String DownUrl =task.getResult().toString();

                                    mUserDatabase.child("image").setValue(DownUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {

                                                //Start creating and uploading thumb image

                                                try { final StorageReference thumb_Path = mImageStorage.child("profile_images").child("thumb").child(CUID+".jpg");
                                                    Bitmap thumb_bitmap = new Compressor(SettingActivity.this)
                                                            .setMaxHeight(200)
                                                            .setMaxWidth(200)
                                                            .setQuality(60)
                                                            .compressToBitmap(imgPath);
                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                    byte[] thumb_byte = baos.toByteArray();

                                                        //start to upload thumb image
                                                    thumb_Path.putBytes(thumb_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                            if(task.isSuccessful()){
                                                                //After uploading get thumb image url


                                                                mImageStorage.child("profile_images").child("thumb").child(CUID+".jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Uri> task) {

                                                                        if(task.isSuccessful()){
                                                                            String thumURL = task.getResult().toString();


                                                                            //Save thumb url in database

                                                                            mUserDatabase.child("thumb_Image").setValue(thumURL).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {



                                                                                                        if(task.isSuccessful()){


                                                                                                            mProgressDialog.dismiss();

                                                                                                            Toast.makeText(SettingActivity.this,"upload Compleate",Toast.LENGTH_LONG).show();
                                                                                                        }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });

                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }
                                    });
                                }
                            });


                        }
                    }
                });



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }



}
