package com.example.beenthere.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.beenthere.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.listener.OnSelectedListener;

public class EditProfile extends AppCompatActivity {

    private String TAG="beenthere";

    EditText userName,userBio;
    Button changeProfileImage,cancel,save;
    ImageView userImage;
    Uri imageUri;
    boolean isImageChaged;
    private RelativeLayout editProfileLayout;
    private LinearLayout progressbar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    StorageReference storageRef;
    StorageReference imagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        isImageChaged=false;
        userName=findViewById(R.id.userName);
        userBio=findViewById(R.id.userBio);
        changeProfileImage=findViewById(R.id.changeImage);
        cancel=findViewById(R.id.cancel);
        save=findViewById(R.id.save);
        userImage=findViewById(R.id.userImage);
        editProfileLayout=findViewById(R.id.editProfileLayout);
        progressbar=findViewById(R.id.progressbar);

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageRef= firebaseStorage.getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        imagesRef = storageRef.child("user_profile_image/"+currentUser.getUid());


        loadData();//load all userdata from firestore

        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //image picker
                TedImagePicker.with(EditProfile.this).zoomIndicator(false)
                        .title("select image for profile")
                        .start(new OnSelectedListener() {
                            @Override
                            public void onSelected(Uri uri) {
                                userImage.setImageURI(uri);//set image in the imageview
                                imageUri=uri;
                                isImageChaged=true;
                            }
                        });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });
    }

    private void loadData() {

        //============fetching data from firebase firestore=============
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentReference documentReference=db.collection("usersdata").document(currentUser.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        //username
                        userName.setText(document.getData().get("username").toString());
                        //userbio
                        if (document.getData().containsKey("userbio")) {
                            userBio.setText(document.getData().get("userbio").toString());
                        } else {
                            userBio.setText("");
                        }
                        //userimage
                            try {
                                imagesRef.getBytes(5120 * 1024).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                    @Override
                                    public void onComplete(@NonNull Task<byte[]> task) {
                                        if (task.isSuccessful() && task.getResult().length>0) {
                                            Bitmap bmp = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                                            userImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, userImage.getWidth(),
                                                    userImage.getHeight(), false));
                                        } else {
                                            Toast.makeText(EditProfile.this, "user profile image not available", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }catch(Exception e){

                            }
                            progressbar.setVisibility(View.GONE);
                            editProfileLayout.setVisibility(View.VISIBLE);

                        } else{
                            Log.d(TAG, "No such document");
                        }


                }
                else{
                    Toast.makeText(EditProfile.this, "error while fetchign data", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });



    }

    private void saveChanges() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentReference documentReference=db.collection("usersdata").document(currentUser.getUid());
        Map<String,Object> userData=new HashMap<>();
        userData.put("username",userName.getText().toString());
        userData.put("userbio",userBio.getText().toString());
        documentReference.update(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(EditProfile.this, "data updated", Toast.LENGTH_SHORT).show();
                    if(isImageChaged)uploadImage();
                finish();
                }
                else{
                    Toast.makeText(EditProfile.this, "error while updating data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImage() {
        imagesRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Toast.makeText(EditProfile.this, "image added", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(EditProfile.this, "error adding image", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
