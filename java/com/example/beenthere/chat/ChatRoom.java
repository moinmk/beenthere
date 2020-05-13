package com.example.beenthere.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beenthere.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatRoom extends AppCompatActivity {
    LinearLayout progressbar;
    RelativeLayout activitycontainer;
    Button send;
    EditText writeMessage;
    TextView justASec,username;
    ImageView userImage;
    String friendUid,chatDocumentId,friendName;
    LinearLayout displayMessageBlock;
    ScrollView scrollview;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    String user1id,user2id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        progressbar=findViewById(R.id.progressbar);
        justASec=findViewById(R.id.justASec);
        activitycontainer=findViewById(R.id.activitycontainer);
        send=findViewById(R.id.send);
        writeMessage=findViewById(R.id.writeMessage);
        userImage=findViewById(R.id.userImage);
        username=findViewById(R.id.username);
        displayMessageBlock=findViewById(R.id.displayMessageBlock);
        scrollview=findViewById(R.id.scrollview);
        scrollview.fullScroll(View.FOCUS_DOWN);

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();

        friendUid=getIntent().getStringExtra("uid");
        friendName=getIntent().getStringExtra("uname");
        username.setText(friendName);
        if(getIntent().hasExtra("image")) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("image"),0,getIntent().getByteArrayExtra("image").length);
            userImage.setImageBitmap(bitmap);
        }


        //in firestore user1 will have id which is grater
        if(currentUser.getUid().compareTo(friendUid)>0){
            user1id=currentUser.getUid();
            user2id=friendUid;
        }
        else{
            user1id=friendUid;
            user2id=currentUser.getUid();
        }



        //fetching chatroom data if already exist or create new otherwise
        try {

            final CollectionReference chat = db.collection("chat");
            chat.whereEqualTo("user1",user1id).whereEqualTo("user2",user2id).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful() && !task.getResult().isEmpty()){
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    chatDocumentId=document.getId();
                                }
                                //Toast.makeText(ChatRoom.this, "already exist= \n"+chatDocumentId, Toast.LENGTH_SHORT).show();
                                displayMessages();
                            } else {
                                 Map<String,Object> chatMetaData=new HashMap<>();
                                chatMetaData.put("user1",user1id);
                                chatMetaData.put("user2",user2id);
                                chatMetaData.put("messages",0);
                                final String documentId=chat.document().getId();
                                chat.document(documentId).set(chatMetaData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            chatDocumentId=documentId;
                                            displayMessages();
                                            justASec.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }
                        }
                    });
        }
        catch (Exception e){
            Toast.makeText(ChatRoom.this, ""+e, Toast.LENGTH_SHORT).show();
        }


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(writeMessage.getText().toString())){
                    sendMessage();
                }
            }
        });
    }

    private void displayMessages() {
        final CollectionReference collectionReference=db.collection("chat").document(chatDocumentId).collection("chatdata");
        collectionReference.orderBy("time").addSnapshotListener(this,new EventListener<QuerySnapshot>() {
            ArrayList<String> dIds=new ArrayList<>();
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                String id="";
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {

                    //add message in ui only after it is added in server not only in local
                    String source = queryDocumentSnapshots != null && queryDocumentSnapshots.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";

                    if(source.compareTo("Server")==0){
                        dIds.add(dc.getDocument().getId());
                        id=id+" "+dc.getDocument().getId();
                    }
                }
                addMessageUi(dIds);
                dIds.clear();
            }
        });
        /*scrollview.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },1000);*/
    }

    public void addMessageUi(ArrayList<String> dIds) {
        if(dIds.size()==0){
            justASec.setVisibility(View.GONE);
        }
        Log.d("beenthere",""+dIds.toString());
        final CollectionReference collectionReference=db.collection("chat").document(chatDocumentId).collection("chatdata");
        for(int i=0;i<dIds.size();i++){
            collectionReference.document(dIds.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    //designing textview
                    TextView message=new TextView(ChatRoom.this);
                    message.setText(document.getData().get("message").toString());
                    message.setTextSize(18);
                    message.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    message.setTextColor(getResources().getColor(R.color.colorText));
                    message.setBackground(getResources().getDrawable(R.drawable.rounded_button));
                    final float scale = getResources().getDisplayMetrics().density;
                    int tendps = (int) (10 * scale + 0.5f);//converting pixels in dp
                    int twentydps = (int) (20 * scale + 0.5f);
                    message.setPadding(twentydps,tendps,twentydps,tendps);
                    int two50pixels = (int) (250 * scale + 0.5f);
                    message.setMaxWidth(two50pixels);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) message.getLayoutParams();
                    lp.setMargins(0,tendps,0,0);
                    lp.weight=1.0f;
                    if(document.getData().get("sender").equals(currentUser.getUid())){
                        lp.gravity= Gravity.RIGHT;
                    }
                    message.setLayoutParams(lp);
                    displayMessageBlock.addView(message);
                    justASec.setVisibility(View.GONE);
                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    //adding message in firestore
    private void sendMessage() {
        Map<String,Object> data=new HashMap<>();
        data.put("message",writeMessage.getText().toString());
        data.put("sender",currentUser.getUid());
        data.put("time", FieldValue.serverTimestamp());

        CollectionReference collectionReference=db.collection("chat").document(chatDocumentId).collection("chatdata");
        collectionReference.add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){
                    writeMessage.setText("");
                    Toast.makeText(ChatRoom.this, "message sent", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(ChatRoom.this, "error sending message"+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
