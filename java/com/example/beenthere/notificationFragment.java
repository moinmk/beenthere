package com.example.beenthere;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.spec.ECField;
import java.util.ArrayList;


public class notificationFragment extends Fragment {
    ListView listView;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;

    ArrayList<String> notification;


    private OnFragmentInteractionListener mListener;

    public notificationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_notification, container, false);
        listView=view.findViewById(R.id.listview);
        getNotifications();
        return view;
    }

    private void getNotifications() {

            db.collection("notification").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot documument=task.getResult();
                            try {
                                notification = (ArrayList<String>) documument.getData().get("notification");
                                discpalyNotifications(notification);
                            }
                            catch (Exception e){

                            }
                    }
                }
            });



    }

    private void discpalyNotifications(ArrayList<String> notifications) {
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),R.layout.notifcation_listview_layout,notifications);
        listView.setAdapter(arrayAdapter);
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
