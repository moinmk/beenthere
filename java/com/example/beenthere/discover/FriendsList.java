package com.example.beenthere.discover;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.beenthere.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FriendsList extends AppCompatActivity {
    RecyclerView recyclerView;

    private FirebaseFirestore db;
    int i;
    ArrayList<String> uIds= new ArrayList<>();
    ArrayList<String> uNames= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discover_friends_list);

        db=FirebaseFirestore.getInstance();

        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        uIds=getIntent().getStringArrayListExtra("userids");
        i=0;
        for(String ids:uIds){
            db.collection("usersdata").document(ids).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document=task.getResult();
                        uNames.add(document.getData().get("username").toString());
                        if(i==uIds.size()-1){
                            RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(FriendsList.this);
                            recyclerView.setLayoutManager(layoutManager);
                            RecyclerView.Adapter adapter=new rvAdapter(uIds,uNames,FriendsList.this);
                            recyclerView.setAdapter(adapter);
                        }
                        i++;
                    }

                }
            });
        }

    }
}
