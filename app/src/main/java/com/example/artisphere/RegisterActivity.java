package com.example.artisphere;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    //views
    EditText usernametv, nametv, phonetv, emailID, passWord, repeatPassWord;
    Button reg_butt;
    TextView haveAccAlready;
    //progressbar to display while registering users
    ProgressDialog progressDialog;

    //declare an instance of Firebase auth
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        //enable back button
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init
        usernametv = findViewById(R.id.username);
        nametv = findViewById(R.id.name);
        phonetv = findViewById(R.id.phone);
        emailID = findViewById(R.id.emailID);
        passWord = findViewById(R.id.passWord);
        repeatPassWord = findViewById(R.id.repeatPassWord);
        reg_butt = findViewById(R.id.reg_butt);
        haveAccAlready = findViewById(R.id.haveAccAlready);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User....");

        //in the oncreate method, initialize the FirebaseAuth object.
        mAuth = FirebaseAuth.getInstance();

        //handling register button click
        reg_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernametv.getText().toString().trim();
                String name = nametv.getText().toString().trim();
                String phone = phonetv.getText().toString().trim();
                String email = emailID.getText().toString().trim();
                String password = passWord.getText().toString().trim();
                String repeatPassword = repeatPassWord.getText().toString().trim();
                //validate
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword) ){
                    Toast.makeText(RegisterActivity.this, "Required fields need to be filled with correct inputs",Toast.LENGTH_SHORT).show();


                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //fix error and set focus to email field
                    emailID.setError("Invalid Email");
                    emailID.setFocusable(true);
                }
                else if (password.length()<6){
                    //fix error and set focus to password field
                    passWord.setError("Password leangth at least 6 characters");
                    passWord.setFocusable(true);
                }
                else if (!password.equals(repeatPassword)){
                    repeatPassWord.setError("Passwords do not match");
                    repeatPassWord.setFocusable(true);
                }
                else {
                    //register the user
                    registerUser(email,password,username,name,phone);
                }
            }
        });

        //handle "have account already? Log In" text view, click listener
        haveAccAlready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

    }

    private void registerUser(String email, String password, final String username, final String name, final String phone) {

        //email and password is valid, show process dialog and start registering users
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            // Sign in success, dismiss dialog and start Register Activity
                            progressDialog.dismiss();
                            Log.d("tag","createUserWithEmail:s");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            //when user is registered, store user details in firebase realtime database too
                            HashMap<Object, String> hashMap = new HashMap<>();
                            //put info in hashmap
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("username",username);
                            hashMap.put("name",name);
                            hashMap.put("phone",phone);
                            hashMap.put("bio","");
                            hashMap.put("dp","https://firebasestorage.googleapis.com/v0/b/artisphere-5df13.appspot.com/o/placeholder.png?alt=media&token=f39f015b-53cd-47ec-98e5-c4af9e7f07b8"); //will add later in edit profile
                            //firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //path to atore user data name "Users"
                            DatabaseReference reference = database.getReference("Users");
                            //put data within hashmap to database
                            reference.child(uid).setValue(hashMap);


                            Toast.makeText(RegisterActivity.this,"Registered\n"+user.getEmail(),Toast.LENGTH_SHORT);
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        }
                        else {
                            // If sign in fails, dismiss dialog and display a message to the user.
                            progressDialog.dismiss();
                            Log.w("tag","createUserWithEmail:f", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dismiss process dialog and get and show the error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    public boolean onSupportNavigateUp() {
        //go previous screen
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
