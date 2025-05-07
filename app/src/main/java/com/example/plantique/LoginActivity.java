package com.example.plantique;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ImageView togglePasswordVisibility;
    private ProgressBar progressBar;
    private boolean passwordVisible = false;

    // Firebase Authentication
    private FirebaseAuth mAuth;
    // Firebase Database
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Inisialisasi Firebase Database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Inisialisasi komponen UI
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);

        // Inisialisasi ProgressBar jika ada di layout
        progressBar = findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Setup toggle password visibility
        setupPasswordToggle();

        // Setup button login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Setup text register
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pindah ke RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User is already signed in and verified, redirect to MainActivity
            goToMainActivity();
        }
    }

    private void setupPasswordToggle() {
        togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordVisible = !passwordVisible;

                if (passwordVisible) {
                    // Menampilkan password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
                } else {
                    // Menyembunyikan password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);
                }

                // Mempertahankan posisi kursor di akhir teks
                etPassword.setSelection(etPassword.getText().length());
            }
        });
    }

    private void loginUser() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        // Validasi input
        if (username.isEmpty()) {
            etUsername.setError("Username diperlukan");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password diperlukan");
            etPassword.requestFocus();
            return;
        }

        // Show loading indicator
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Disable login button to prevent multiple clicks
        btnLogin.setEnabled(false);

        // Cari email berdasarkan username
        findEmailByUsername(username, password);
    }

    private void findEmailByUsername(String username, String password) {
        Log.d(TAG, "Looking up username: " + username);

        // Look in the usernames mapping node instead of querying the users node
        DatabaseReference usernamesRef = FirebaseDatabase.getInstance().getReference("users/usernames");
        usernamesRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Username found");
                    // Username exists, get the associated email
                    String email = dataSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        Log.d(TAG, "Email found: " + email);
                        // Use the email to sign in with Firebase Authentication
                        signInWithEmail(email, password);
                    } else {
                        Log.e(TAG, "Email not found for username: " + username);
                        hideLoading();
                        Toast.makeText(LoginActivity.this, "Data pengguna rusak. Silakan hubungi admin.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Username not found: " + username);
                    hideLoading();
                    Toast.makeText(LoginActivity.this, "Username tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                hideLoading();
                Toast.makeText(LoginActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithEmail(String email, String password) {
        Log.d(TAG, "Attempting to sign in with email: " + email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideLoading();

                        if (task.isSuccessful()) {
                            // Login success, check if email is verified
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user.isEmailVerified()) {
                                // Email is verified, proceed to MainActivity
                                Log.d(TAG, "Login successful and email verified");
                                Toast.makeText(LoginActivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();
                                goToMainActivity();
                            } else {
                                // Email is not verified
                                Log.d(TAG, "Email not verified");
                                Toast.makeText(LoginActivity.this,
                                        "Silakan verifikasi email Anda terlebih dahulu. Cek kotak masuk email Anda.",
                                        Toast.LENGTH_LONG).show();

                                // Send verification email again
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(LoginActivity.this,
                                                            "Email verifikasi baru telah dikirim ke " + user.getEmail(),
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                // Sign out as the user is not verified
                                mAuth.signOut();
                            }
                        } else {
                            // Login failed
                            Log.e(TAG, "Login failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Password salah atau akun tidak ada",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void hideLoading() {
        // Hide loading indicator
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        // Re-enable login button
        btnLogin.setEnabled(true);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}