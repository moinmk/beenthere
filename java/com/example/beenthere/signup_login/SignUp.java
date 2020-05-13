package com.example.beenthere.signup_login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beenthere.AllActivityContainer;
import com.example.beenthere.R;
import com.example.beenthere.getstarted.GetStarted;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

//    SignInButton signInWithGoogle;//signin button
    Button signInWithGoogle;
    String TAG="beenthere";
    int RC_SIGN_IN=1001;
    GoogleApiClient mGoogleSignInClient;
    GoogleSignInClient mGSIC;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseFirestore db;
    FirebaseUser user;

    Button signup,login;
    EditText email,username,password,confirmPassword;
    boolean allValid,usernameChecked,isExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signInWithGoogle=findViewById(R.id.signinWithGoogle);

        mAuth=FirebaseAuth.getInstance();//initializing firebaseauth

        signup=findViewById(R.id.buttonSignup);
        login=findViewById(R.id.buttonLogin);
        email=findViewById(R.id.email);
        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        confirmPassword=findViewById(R.id.confirmPassword);


        //=========check if user is logged in=============
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null){
                    startActivity(new Intent(SignUp.this, GetStarted.class));
                   // startActivity(new Intent(Si.this,AccountActivity.class));
                }
            }
        };


       /* if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(SignUp.this,GetStarted.class));
        }*/

        //==========Configure Google Sign In==========
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGSIC = GoogleSignIn.getClient(this, gso);

        signInWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });
        //=========================

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Boolean valid=FieldsValidation(email,username,password,confirmPassword);
                    if(valid){
                        //Toast.makeText(SignUp.this, "allvalid"+valid, Toast.LENGTH_SHORT).show();
                        isUserExist();
                    }
                    else{
                        Toast.makeText(SignUp.this, "allvalid"+valid, Toast.LENGTH_SHORT).show();

                    }
            }
        });

        // "=========already have account? login button clicked=========="

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUp.this,Login.class));
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    //============method to signin with google account===============
    public void signInGoogle(){
        Intent signInIntent =mGSIC.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    //==========authencating google account with firebase=================
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(SignUp.this, "signin success", Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();
                            final String userEmail=user.getEmail();
                            //check if user already exist then don't create account
                            db=FirebaseFirestore.getInstance();
                            DocumentReference documentReference=db.collection("usersdata").document(user.getUid());
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        if(!task.getResult().exists()){
                                            //adding username in firestore with userid
                                            db = FirebaseFirestore.getInstance();
                                            String userId=user.getUid();
                                            String uname=userEmail.substring(0,userEmail.indexOf("@"));
                                            Map<String,Object> userData=new HashMap<>();
                                            userData.put("username",uname);
                                            userData.put("followers", Arrays.asList());
                                            userData.put("following",Arrays.asList());
                                            userData.put("visitedplaces",Arrays.asList());
                                            db.collection("usersdata")
                                                    .document(userId)
                                                    .set(userData).addOnCompleteListener(SignUp.this,new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(SignUp.this, "data added", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else{
                                                        Toast.makeText(SignUp.this, "error while adding in data", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        else{
                                            Toast.makeText(SignUp.this, "data exist", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        Toast.makeText(SignUp.this, "error while fetching data", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                        else{
                            //sign in fails
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //=======checking field validations==========
    private boolean FieldsValidation(EditText email, final EditText username, EditText password, EditText confirmPassword){
        allValid=true;
        usernameChecked=false;
        if(TextUtils.isEmpty(email.getText().toString())){
            email.setError("email can't be empty");
            allValid=false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            email.setError("invalid email address");
            allValid=false;
        }

        if(TextUtils.isEmpty(username.getText().toString())){
            username.setError("username can't be empty");
            allValid=false;
        }
        else if(username.getText().toString().length()<4){
            username.setError("username can't be less than 4 characters");
            allValid=false;
        }

        if(TextUtils.isEmpty(password.getText().toString())){
            password.setError("password can't be empty");
            allValid=false;
        }
        else if(password.getText().toString().length()<6){
            password.setError("password should contain minimum 6 characters");
            allValid=false;
        }
        if(TextUtils.isEmpty(confirmPassword.getText().toString())){
            confirmPassword.setError("confirm password can't be empty");
            allValid=false;
        }
        else if(!confirmPassword.getText().toString().equals(password.getText().toString())){
            confirmPassword.setError("incorrect confirm password");
            allValid=false;
        }


        return allValid;

    }


    private void isUserExist() {

        db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("usersdata");
        Query query = usersRef.whereEqualTo("username", username.getText().toString());
        query.get().addOnCompleteListener(SignUp.this,new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        String user = documentSnapshot.getString("username");
                        if(task.getResult().size()!=0){
                            Log.d(TAG, "User Exists");
                            Toast.makeText(SignUp.this, "Username already exists", Toast.LENGTH_SHORT).show();
                            username.setError("username already exist");
                        }
                    }
                }

                if(task.getResult().size() == 0 ){
                    //user with this username does not exist
                    Log.d(TAG, "User not Exists");
                    Toast.makeText(SignUp.this, "user not exist", Toast.LENGTH_SHORT).show();
                    createAccount(email,password);
                }
            }
        });

    }


    //==============sign up with email and password===================

    private void createAccount(final EditText email, final EditText password){
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                // Sign in success
                                Toast.makeText(SignUp.this, "signup with email password successfull", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "createUserWithEmail:success");
                                //=========adding additional data in firebase firestore============
                                FirebaseUser user = mAuth.getCurrentUser();
                                db = FirebaseFirestore.getInstance();
                                String userId=user.getUid();
                                Map<String,Object> userData=new HashMap<>();
                                userData.put("username",username.getText().toString());
                                userData.put("followers", Arrays.asList());
                                userData.put("following",Arrays.asList());
                                userData.put("visitedplaces",Arrays.asList());
                                db.collection("usersdata")
                                        .document(userId)
                                        .set(userData).addOnCompleteListener(SignUp.this,new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(SignUp.this, "data added", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(SignUp.this, "error while adding in data", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());

                                //handling exceptio throw by firebase
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthUserCollisionException e) {
                                    email.setError("account with this email address already exist");
                                } catch(Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }

                                Toast.makeText(SignUp.this, "Authentication failed." ,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

}


