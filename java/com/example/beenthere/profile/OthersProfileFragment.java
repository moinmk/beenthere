package com.example.beenthere.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beenthere.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OthersProfileFragment extends Fragment {

    private String TAG="beenthere";

    private ImageView userImage;
    private TextView userName,userBio,followers,following;
    private Button profileButton,logOut;
    private RelativeLayout profilelayout;
    private LinearLayout progressbar;

    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageRef;
    private StorageReference imagesRef;
    private DocumentReference documentReference;

    private OnFragmentInteractionListener mListener;
    String userId;
    String currentUserName;

    public OthersProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        db=FirebaseFirestore.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageRef= firebaseStorage.getReference();
        mAuth=FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //imagesRef = storageRef.child("user_profile_image/"+currentUser.getUid());
        userId=getArguments().getString("userid");
        setData();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_others_profile, container, false);
        userImage=view.findViewById(R.id.userImage);
        userName=view.findViewById(R.id.userName);
        userBio=view.findViewById(R.id.userBio);
        followers=view.findViewById(R.id.followers);
        following=view.findViewById(R.id.following);
        profilelayout=view.findViewById(R.id.profilelayout);
        profileButton=view.findViewById(R.id.profileButton);
        progressbar=view.findViewById(R.id.progressbar);
        logOut=view.findViewById(R.id.logOut);
        logOut.setVisibility(View.GONE);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (profileButton.getText().toString()){
                    case "follow":
                        //add user in current users follwing list
                        documentReference=db.collection("usersdata").document(currentUser.getUid());
                        documentReference.update("following", FieldValue.arrayUnion(userId));

                        //add current user in user's followers list
                        documentReference=db.collection("usersdata").document(userId);
                        documentReference.update("followers", FieldValue.arrayUnion(currentUser.getUid())).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    profileButton.setText("following");
                                    //**add setdata() if want to update followers number
                                }
                            }
                        });

                        db.collection("usersdata").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    currentUserName=task.getResult().getData().get("username").toString();
                                    db.collection("notification").document(userId).update("notification",FieldValue.arrayUnion(currentUserName+" started following you"));//useing set with merge insted of update to create document if not exist

                                }
                            }
                        });


                        break;

                    case "following":
                        AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
                        adb.setTitle("confirmation");
                        adb.setMessage("are you sure you want to unfollow the user");
                        adb.setPositiveButton("yes,unfollow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //removing user from current users follwing list
                                documentReference=db.collection("usersdata").document(currentUser.getUid());
                                documentReference.update("following", FieldValue.arrayRemove(userId));
                                //removing current user from user's followers list
                                documentReference=db.collection("usersdata").document(userId);
                                documentReference.update("followers", FieldValue.arrayRemove(currentUser.getUid())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            profileButton.setText("follow");
                                            //**add setdata() if want to update followers number
                                        }
                                    }
                                });
                            }
                        });
                        adb.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        adb.show();
                        break;
                }
            }
        });

        return view;
    }


    //setting up all data in textViews and imageview
    private void setData() {
        //============fetching data from firebase firestore=============
        imagesRef = storageRef.child("user_profile_image/"+userId);
        DocumentReference documentReference=db.collection("usersdata").document(userId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        progressbar.setVisibility(View.GONE);
                        profilelayout.setVisibility(View.VISIBLE);
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        //Toast.makeText(getActivity(), ""+document.getData(), Toast.LENGTH_SHORT).show();
                        //username
                        userName.setText(document.getData().get("username").toString());
                        //userbio
                        if(document.getData().containsKey("userbio")){
                            userBio.setText(document.getData().get("userbio").toString());
                        }else{
                            userBio.setText("");
                        }
                        //followers
                        List<String> totalFollowers=(List<String>)document.get("followers");
                        followers.setText(String.valueOf(totalFollowers.size()));
                        //check if current user follows the user
                        if(totalFollowers.contains(currentUser.getUid())){
                            profileButton.setText("following");
                        }
                        else{
                            profileButton.setText("follow");
                        }
                        //following
                        List<String> totalFollowing=(List<String>)document.get("following");
                        following.setText(String.valueOf(totalFollowing.size()));
                        //userimage
                        try {
                            imagesRef.getBytes(5120 * 1024)
                                    .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                        @Override
                                        public void onComplete(@NonNull Task<byte[]> task) {
                                            if (task.isSuccessful()) {
                                                Bitmap bmp = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                                                userImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, userImage.getWidth(),
                                                        userImage.getHeight(), false));
                                            } else {
                                                Toast.makeText(getActivity(), "user profile image not available", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }catch (Exception e){
                        }

                    } else {
                        Log.d(TAG, "No such document");
                    }
                }
                else{
                    Toast.makeText(getActivity(), "error while fetching data", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "get failed with ", task.getException());

                }
            }
        });
    }


    //**add onresume if require for refresh

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
