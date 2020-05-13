package com.example.beenthere.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.example.beenthere.R;
import com.example.beenthere.discover.FriendsList;
import com.example.beenthere.discover.rvAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ChatFriendList extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView heading;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    int i;
    ArrayList<String> uIds= new ArrayList<>();
    ArrayList<String> uNames= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_friend_list);
        heading=findViewById(R.id.heading);
        heading.setText("chat");
        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        db.collection("usersdata").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document=task.getResult();
                    uIds=(ArrayList<String>)document.getData().get("following");
                    fetchFriendsData(uIds);
                }
            }
        });
    }

    public void fetchFriendsData(ArrayList<String> uids){
        i=0;
        for(String ids:uids){
            db.collection("usersdata").document(ids).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document=task.getResult();
                        uNames.add(document.getData().get("username").toString());
                        if(i==uIds.size()-1){
                            RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(ChatFriendList.this);
                            recyclerView.setLayoutManager(layoutManager);
                            RecyclerView.Adapter adapter=new rvAdapter(uIds,uNames,ChatFriendList.this);
                            recyclerView.setAdapter(adapter);
                        }
                        i++;
                    }
                }
            });
        }
    }
}
