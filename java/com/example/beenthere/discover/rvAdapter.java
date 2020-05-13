package com.example.beenthere.discover;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beenthere.R;
import com.example.beenthere.chat.ChatRoom;
import com.example.beenthere.profile.EditProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class rvAdapter extends RecyclerView.Adapter<rvAdapter.MyViewHolder>{
    ArrayList<String> userIds;
    ArrayList<String> uNames;
    Context context;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private StorageReference imagesRef;

    public rvAdapter(ArrayList<String> userIds, ArrayList<String> userNames, Context context) {
        this.userIds=userIds;
        this.uNames=userNames;
        this.context=context;

        firebaseStorage=FirebaseStorage.getInstance();
        storageRef= firebaseStorage.getReference();

    }

    @NonNull
    @Override
    public rvAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.discover_cardview_content,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final rvAdapter.MyViewHolder holder, final int position) {
        holder.username.setText(uNames.get(position));
        imagesRef = storageRef.child("user_profile_image/"+userIds.get(position));

        //displaying userimage
        try {
            imagesRef.getBytes(5120 * 1024).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    if (task.isSuccessful() && task.getResult().length>0) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                        holder.userImage.setImageBitmap(Bitmap.createScaledBitmap(bmp,holder.userImage.getWidth(),
                                holder.userImage.getHeight(), false));
                    } else {

                    }
                }
            });
        }catch(Exception e){

        }


        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("beenthere","button clicked at"+position);
                Intent i=new Intent(context, ChatRoom.class);
                i.putExtra("uid",userIds.get(position));
                i.putExtra("uname",uNames.get(position));
                Bitmap bitmap = ((BitmapDrawable) holder.userImage.getDrawable()).getBitmap();
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50,bs);
                i.putExtra("image",bs.toByteArray());

                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userIds.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        Button chat;
        ImageView userImage;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.username=itemView.findViewById(R.id.username);
            this.chat=itemView.findViewById(R.id.chat);
            this.userImage=itemView.findViewById(R.id.userImage);
        }
    }
}
