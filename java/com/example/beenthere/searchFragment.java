package com.example.beenthere;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beenthere.chat.ChatFriendList;
import com.example.beenthere.chat.ChatRoom;
import com.example.beenthere.discover.bottomsheetDialog;
import com.example.beenthere.profile.OthersProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class searchFragment extends Fragment {

    private String TAG="beenthere";
    private OnFragmentInteractionListener mListener;
    private Button search,viewProfile,chat;
    private EditText userName;
    private TextInputLayout inputLayoutSearch;
    private TextView userNameTextView;
    private ImageView userImage;
    private CardView profileInfoContainer,imageContainer;
    String friendUserId;

    private FirebaseFirestore db;
    FirebaseUser user;
    private StorageReference storageRef;
    private StorageReference imagesRef;

    boolean isExist;

    public searchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_search, container, false);

        search=view.findViewById(R.id.search);
        userName=view.findViewById(R.id.userName);
        inputLayoutSearch=view.findViewById(R.id.inputLayoutSearch);
        userNameTextView=view.findViewById(R.id.name);
        userImage=view.findViewById(R.id.userImage);
        imageContainer=view.findViewById(R.id.imageContainer);
        profileInfoContainer=view.findViewById(R.id.profileInfoContainer);
        viewProfile=view.findViewById(R.id.viewProfile);
        chat=view.findViewById(R.id.chat);


        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageRef= firebaseStorage.getReference();


        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChatFriendList.class));
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //=====hide keyboard=======
                /*InputMethodManager inputManager = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);*/

                if(TextUtils.isEmpty(userName.getText().toString())){
                    userName.setError("field can't be empty");
                    return;
                }
                else if(userFound()){
                }
            }
        });

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment othersProfilefragment=new OthersProfileFragment();
                //sending user id through bundle
                Bundle args = new Bundle();
                args.putString("userid",friendUserId);
                othersProfilefragment.setArguments(args);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container,othersProfilefragment)
                        .addToBackStack("search")
                        .commit();
            }
        });

        return view;
    }

    private boolean userFound() {
        db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("usersdata");
        Query query = usersRef.whereEqualTo("username", userName.getText().toString());
        query.get().addOnCompleteListener(getActivity(),new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        String user = documentSnapshot.getString("username");
                        friendUserId=documentSnapshot.getId();
                        if(task.getResult().size()!=0){
                            Log.d(TAG, "User found");
                            isExist=true;
                            //set edittext in upper portion
                            RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0,100,0,0);
                            inputLayoutSearch.setLayoutParams(lp);
                            inputLayoutSearch.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
                            userNameTextView.setText(user);

                            //fetch user profile image
                            imagesRef = storageRef.child("user_profile_image/"+documentSnapshot.getId());
                            try {
                                imagesRef.getBytes(5120 * 1024).addOnCompleteListener(new OnCompleteListener<byte[]>() {
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
                            }catch(Exception e){
                            }

                            profileInfoContainer.setVisibility(View.VISIBLE);
                        }
                    }
                }

                if(task.getResult().size() == 0 ){
                    //user with this username does not exist
                    Log.d(TAG, "User not Exists");
                    Toast.makeText(getActivity(), "user not exist", Toast.LENGTH_SHORT).show();
                    userName.setError("user not exist");
                    isExist=false;
                    profileInfoContainer.setVisibility(View.INVISIBLE);
                }
            }
        });
        return isExist;
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
