package com.reema.assignment3;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView userNameTextView, et_token, userDateTextView, userAddressTextView, userPhoneTextView;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        et_token = findViewById(R.id.et_token);
        profileImageView = findViewById(R.id.visit_profile_image);
        userNameTextView = findViewById(R.id.visit_user_name);
        userDateTextView = findViewById(R.id.visit_user_date);
        userAddressTextView=findViewById(R.id.visit_user_address);
        userPhoneTextView=findViewById(R.id.visit_user_phone);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast

                        Log.d("TAG", token);
                        Toast.makeText(ProfileActivity.this, "your device token is : " + token, Toast.LENGTH_SHORT).show();
                        et_token.setText(token);
                    }
                });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile pictures").child(currentUser.getUid() + ".jpg");

        // Load user data and update the views
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method will be called every time the data at the specified
//                // reference changes. You can use the dataSnapshot parameter to
//                // retrieve the data.
//
//                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
//
//
//                    String userName = dataSnapshot.child("name").getValue(String.class);
//                    String date = dataSnapshot.child("date").getValue(String.class);
//                    String address = dataSnapshot.child("address").getValue(String.class);
//                    String phone = dataSnapshot.child("phone").getValue(String.class);
//
//
//
//                    int color = Color.BLACK;
//                    SpannableString spannableString = new SpannableString(userName);
//                    ForegroundColorSpan foregroundSpan = new ForegroundColorSpan(color);
//                    spannableString.setSpan(foregroundSpan, 0, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                    userNameTextView.setText("User Name: " + spannableString);
//                    userDateTextView.setText("BirthDate: "+date);
//                    userAddressTextView.setText("Address: "+address);
//                    userPhoneTextView.setText("Phone: " +phone);
//
//
//
//
//                }
//            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method will be called every time the data at the specified
                // reference changes. You can use the dataSnapshot parameter to
                // retrieve the data.

                String userName = dataSnapshot.child("name").getValue(String.class);
                String date = dataSnapshot.child("date").getValue(String.class);
                String address = dataSnapshot.child("address").getValue(String.class);
                String phone = dataSnapshot.child("phone").getValue(String.class);


                userNameTextView.setText("User Name: " + userName);

                // Set the user's date, address, and phone normally without changing the color
                userDateTextView.setText("BirthDate: " + date);
                userAddressTextView.setText("Address: " + address);
                userPhoneTextView.setText("Phone: " + phone);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Load the user's profile image from Firebase Storage using Picasso
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(profileImageView));

    }
}
