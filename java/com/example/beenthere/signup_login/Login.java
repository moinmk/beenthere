package com.example.beenthere.signup_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.beenthere.AllActivityContainer;
import com.example.beenthere.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    String TAG="beenthere";
    Button login,signup;
    EditText email,password;
//    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login=findViewById(R.id.buttonLogin);
        signup=findViewById(R.id.buttonSignup);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
//        progressBar=findViewById(R.id.progressbar);
        mAuth=FirebaseAuth.getInstance();//initializing firebaseauth



        //=========check if signuplogin__user is logged in=============
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null){
                    startActivity(new Intent(Login.this, AllActivityContainer.class));
                    }
            }
        };


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                progressBar.setVisibility(View.VISIBLE);
                validateData(email,password);
            }
        });

        //========don't have account?signup clicked============
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,SignUp.class));
            }
        });

    }


    //=========validating user inserted data==========
    private void validateData(EditText email, EditText password) {
        Boolean allValid=true;
        if(TextUtils.isEmpty(email.getText().toString())){
            email.setError("field can't be empty");
            allValid=false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            email.setError("invalid email address");
            allValid=false;
        }
        if(TextUtils.isEmpty(password.getText().toString())){
            password.setError("field can't be empty");
            allValid=false;
        }
        /*else if(password.getText().toString().length()<6){
            password.setError("password should contain minimum 6 characters");
            allValid=false;
        }*/

        if(allValid){
            getLoggedIn(email,password);
        }
//        progressBar.setVisibility(View.GONE);
    }


    //======logging in user with firebase ============
    private void getLoggedIn(final EditText email, final EditText password) {

        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(Login.this, "logged in successfull", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, AllActivityContainer.class));
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            //handling exceptio throw by firebase
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                password.setError("wrong password");
                            } catch(FirebaseAuthInvalidUserException e) {
                                email.setError("account does not exist with this email address");
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                             Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
