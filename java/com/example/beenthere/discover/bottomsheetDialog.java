package com.example.beenthere.discover;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.beenthere.R;
import com.example.beenthere.profile.EditProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class bottomsheetDialog extends BottomSheetDialogFragment {
    TextView placeName;
    Button favorite,visitedFriendsname;
    RatingBar rating;
    LinearLayout friendsImageContainer;
    CardView cv;

    Place place;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private StorageReference imagesRef;

    String placename,currentUserUid;
    ArrayList<String> visitedusersid;
    float placeratings;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view=inflater.inflate(R.layout.discover_bottomsheet_layout,container,false);

            db=FirebaseFirestore.getInstance();
            mAuth=FirebaseAuth.getInstance();
            firebaseStorage=FirebaseStorage.getInstance();
            storageRef= firebaseStorage.getReference();

            placeName=view.findViewById(R.id.placeName);
            visitedFriendsname=view.findViewById(R.id.visitedFriendsname);
            favorite=view.findViewById(R.id.favorite);
            rating=view.findViewById(R.id.Ratings);
            friendsImageContainer=view.findViewById(R.id.friendsImagesContainer);

            final float pxfor25dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,25, getResources().getDisplayMetrics());
            final float pxfor12dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,12, getResources().getDisplayMetrics());

            if(visitedusersid.size()>0){
                db.collection("usersdata").document(visitedusersid.get(0)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot=task.getResult();
                            if(visitedusersid.size()>1){
                                visitedFriendsname.setText(documentSnapshot.getData().get("username").toString()+" and "+(visitedusersid.size()-1)+" others have beenthere");
                            }
                            else{
                                visitedFriendsname.setText(documentSnapshot.getData().get("username").toString()+" have beenthere");
                            }
                        }
                    }
                });

                for(String fIds:visitedusersid){
                    int margin=10;
                    imagesRef = storageRef.child("user_profile_image/"+fIds);
                    try {
                        imagesRef.getBytes(5120 * 1024).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                            @Override
                            public void onComplete(@NonNull Task<byte[]> task) {
                                if (task.isSuccessful() && task.getResult().length>0) {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                                    /*userImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, userImage.getWidth(),
                                            userImage.getHeight(), false));*/
                                    ImageView userImage=new ImageView(getContext());

                                    userImage.setImageBitmap(Bitmap.createScaledBitmap(bmp,30,
                                            30, false));

                                    cv=new CardView(getContext());
                                    cv.setMinimumHeight(100);
                                    cv.setMinimumWidth(100);
                                    cv.setLayoutParams(new CardView.LayoutParams((int)pxfor25dp,(int)pxfor25dp));
                                    cv.setRadius(pxfor12dp);
                                    cv.addView(userImage);
                                    friendsImageContainer.addView(cv);
                                }
                            }
                        });
                    }catch(Exception e){
                    }
                }
            }
            else{
                visitedFriendsname.setText("non of your friend have beenthere yet");

            }

            placeName.setText(placename);
            rating.setRating(placeratings);

            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("usersdata").document(currentUserUid).update("favoriteplaces", FieldValue.arrayUnion(place.getId())).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(), "place added to favourite list", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            visitedFriendsname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(getActivity(),FriendsList.class);
                    i.putExtra("userids",visitedusersid);
                    startActivity(i);
                }
            });

            return view;
    }

    public bottomsheetDialog(Place place, float ratings, ArrayList<String> visitedUsersid, String currentUserUid) {
        this.place=place;
        this.placename=place.getName();
        this.placeratings=ratings;
        this.visitedusersid=visitedUsersid;
        this.currentUserUid=currentUserUid;
    }

}

