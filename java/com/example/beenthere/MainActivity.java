package com.example.beenthere;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.beenthere.permissions_activity.LocationPermission;
import com.example.beenthere.profile.EditProfile;
import com.example.beenthere.signup_login.Login;
import com.example.beenthere.signup_login.SignUp;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();

        Thread backgroud=new Thread(){
          public void run(){
              try {
                  sleep(1000);

                  //check if user is already signed in
                  if(mAuth.getCurrentUser()!=null) {
                      Intent i=new Intent(MainActivity.this, LocationPermission.class);
                      startActivity(i);
                  }else{
                      startActivity(new Intent(MainActivity.this,Login.class));
                      Toast.makeText(MainActivity.this, "signed uot", Toast.LENGTH_SHORT).show();
                  }
              }catch(Exception e){
              }
          }
        };

        backgroud.start();


    }
}
