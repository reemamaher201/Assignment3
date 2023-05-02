package com.reema.assignment3;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;


import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar settingsToolbar;
    private EditText userNameEditText,userPhoneEditText,userAddressEditText,userDateEditText;
    private RoundedImageView profileImageView;
    private Button updateSettingsButton;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference storageProfilePicsRef;

    private String currentUserID;
    private static final int GALLERY_PICK = 1;
    private static final int GALLERY_REQUEST_CODE = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        userNameEditText = findViewById(R.id.set_user_name);
        userDateEditText =findViewById(R.id.set_user_date);
        userAddressEditText = findViewById(R.id.set_user_address);
        userPhoneEditText = findViewById(R.id.set_user_phone);
        profileImageView = findViewById(R.id.set_profile_image);
        updateSettingsButton = findViewById(R.id.update_settings_button);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        updateSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = userNameEditText.getText().toString();
                String date = userDateEditText.getText().toString();
                String address = userAddressEditText.getText().toString();
                String phone = userPhoneEditText.getText().toString();
                //&&TextUtils.isEmpty(date)&&TextUtils.isEmpty(address)&&TextUtils.isEmpty(phone)
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(SettingsActivity.this, "Please fill all the fields" , Toast.LENGTH_SHORT).show();
                } else {//
                    updateAccountInfo(username,date,address,phone);
                }
            }
        });
        RetrieveUserInfo();
    }
    private void updateAccountInfo(final String username, final String date, final String address, final String phone) {
        if (imageUri != null) {
            final StorageReference fileRef = storageProfilePicsRef.child(currentUserID + ".jpg");
            UploadTask uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        String myUrl = downloadUrl.toString();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", currentUserID);
                        userMap.put("name", username);
                        if (!TextUtils.isEmpty(date)) {
                            userMap.put("date", date);
                        }
                        if (!TextUtils.isEmpty(address)) {
                            userMap.put("address", address);
                        }
                        if (!TextUtils.isEmpty(phone)) {
                            userMap.put("phone", phone);
                        }
                        userMap.put("image", myUrl);

                        rootRef.child("Users").child(currentUserID).updateChildren(userMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingsActivity.this, "Profile info updated successfully!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(SettingsActivity.this, "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(SettingsActivity.this, "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }



    // Override onActivityResult to retrieve the image URI and update the imageUri variable
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void RetrieveUserInfo() {

        // Get a reference to the user's information in the database
        DatabaseReference userRef = rootRef.child("Users").child(currentUserID);

// Add a ValueEventListener to retrieve the user's information from the database
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the user's information from the dataSnapshot
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);
                    String address = dataSnapshot.child("address").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String image = dataSnapshot.child("image").getValue(String.class);

                    // Update the UI elements with the retrieved data
                    userNameEditText.setText(name);
                    userDateEditText.setText(date);
                    userAddressEditText.setText(address);
                    userPhoneEditText.setText(phone);
                    if (!TextUtils.isEmpty(image)) {
                        Picasso.get().load(image).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });

    }
}