package com.example.beenthere.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.beenthere.chat.ChatFriendList;
import com.example.beenthere.chat.ChatRoom;
import com.example.beenthere.signup_login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class profileFragment extends Fragment {

    private String TAG="beenthere";

    private ImageView userImage;
    private TextView userName,userBio,followers,following;
    private Button profileButton,chat,logOut;
    private RelativeLayout profilelayout;
    private LinearLayout progressbar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    StorageReference storageRef;
    StorageReference imagesRef;

    private OnFragmentInteractionListener mListener;

    public profileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();//initializing firebaseauth
        firebaseStorage=FirebaseStorage.getInstance();
        storageRef= firebaseStorage.getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        imagesRef = storageRef.child("user_profile_image/"+currentUser.getUid());

    }

    //setting up all data in textViews and imageview
    private void setData() {
        //============fetching data from firebase firestore=============
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentReference documentReference=db.collection("usersdata").document(currentUser.getUid());
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
                        e.printStackTrace();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        // Inflate the layout for this fragment
        userImage=view.findViewById(R.id.userImage);
        userName=view.findViewById(R.id.userName);
        userBio=view.findViewById(R.id.userBio);
        followers=view.findViewById(R.id.followers);
        following=view.findViewById(R.id.following);
        profilelayout=view.findViewById(R.id.profilelayout);
        profileButton=view.findViewById(R.id.profileButton);
        logOut=view.findViewById(R.id.logOut);
        chat=view.findViewById(R.id.chat);
        progressbar=view.findViewById(R.id.progressbar);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),EditProfile.class));
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChatFriendList.class));
            }
        });


        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), Login.class));
            }
        });

        return  view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setData();
        super.onActivityCreated(savedInstanceState);
    }

    //update data after came back from edit profile activity
    @Override
    public void onStart() {
        setData();
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
