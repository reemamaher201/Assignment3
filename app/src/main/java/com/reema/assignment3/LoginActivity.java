package com.reema.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private Button LoginButton;
    private EditText UserEmail,UserPassword;
    private TextView Signup;
    private ProgressDialog progressDialog;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth=FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog=new ProgressDialog(this);

        LoginButton=findViewById(R.id.login_button);
        UserEmail=findViewById(R.id.login_email);
        UserPassword=findViewById(R.id.login_password);
        Signup=findViewById(R.id.need_new_account_link);

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });


        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=UserEmail.getText().toString();
                String password=UserPassword.getText().toString();

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(LoginActivity.this,"Please enter email...",Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(LoginActivity.this,"Please enter password...",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.setTitle("Log In");
                    progressDialog.setMessage("please wait...");
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.show();
                    firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                String currentUserId=firebaseAuth.getCurrentUser().getUid();
                                String deviceToken= task.getResult().toString();
                                userRef.child(currentUserId).child("device_token").setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    sendUserToMainActivity();
                                                    Toast.makeText(LoginActivity.this,"Logged in Successfully...",Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                            }
                            else
                            {
                                String errormessage=task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error :"+errormessage,Toast.LENGTH_SHORT).show();
                                Log.e("error",errormessage);
                            }
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent newuserIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(newuserIntent);
    }



}