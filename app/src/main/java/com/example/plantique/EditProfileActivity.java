package com.example.plantique;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private CircleImageView ivProfilePhoto;
    private FloatingActionButton fabEditPhoto;
    private TextInputEditText etFullName, etUsername, etBio;
    private Button btnCancel, btnSave;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);  // Using your existing edit profile layout

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        }

        // Initialize views
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        fabEditPhoto = findViewById(R.id.fab_edit_photo);
        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        etBio = findViewById(R.id.et_bio);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);

        // Load user data
        loadUserProfile();

        // Set click listeners
        fabEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoOptions();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Just close the activity
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            // Load basic info from FirebaseUser
            String displayName = currentUser.getDisplayName();
            if (displayName != null) {
                etFullName.setText(displayName);
                etUsername.setText(displayName); // Default to same as display name
            }

            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Picasso.get().load(photoUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(ivProfilePhoto);
            }

            // Load additional info from Firebase Database
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("username")) {
                            String username = dataSnapshot.child("username").getValue(String.class);
                            etUsername.setText(username);
                        }

                        if (dataSnapshot.hasChild("bio")) {
                            String bio = dataSnapshot.child("bio").getValue(String.class);
                            etBio.setText(bio);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(EditProfileActivity.this,
                            "Error loading profile: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveUserProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Nama dan username tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        btnSave.setEnabled(false);
        btnSave.setText("Menyimpan...");

        // Update display name in FirebaseAuth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Update additional info in Firebase Database
                            userRef.child("username").setValue(username);
                            userRef.child("bio").setValue(bio)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            btnSave.setEnabled(true);
                                            btnSave.setText("Simpan Perubahan");

                                            if (task.isSuccessful()) {
                                                Toast.makeText(EditProfileActivity.this,
                                                        "Profil berhasil diperbarui",
                                                        Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(EditProfileActivity.this,
                                                        "Gagal memperbarui profil",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            btnSave.setEnabled(true);
                            btnSave.setText("Simpan Perubahan");
                            Toast.makeText(EditProfileActivity.this,
                                    "Gagal memperbarui nama",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showPhotoOptions() {
        String[] options = {"Ambil foto", "Pilih dari galeri", "Hapus foto"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Foto Profil")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Ambil foto
                            Toast.makeText(EditProfileActivity.this,
                                    "Fitur ambil foto belum tersedia",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case 1: // Pilih dari galeri
                            Toast.makeText(EditProfileActivity.this,
                                    "Fitur pilih foto belum tersedia",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // Hapus foto
                            Toast.makeText(EditProfileActivity.this,
                                    "Fitur hapus foto belum tersedia",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }
}