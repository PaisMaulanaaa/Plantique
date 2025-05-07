package com.example.plantique;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar; // Tambahkan ProgressBar

    // Firebase Authentication
    private FirebaseAuth mAuth;
    // Firebase Database
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Inisialisasi Firebase Database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Inisialisasi views
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        // Inisialisasi ProgressBar (pastikan ada di layout)
        progressBar = findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Set click listener untuk tombol Register
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Set click listener untuk text login
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Metode untuk memeriksa koneksi internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void registerUser() {
        final String username = etUsername.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validasi input
        if (username.isEmpty()) {
            etUsername.setError("Username diperlukan");
            etUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email diperlukan");
            etEmail.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Masukkan email yang valid");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password diperlukan");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Konfirmasi password diperlukan");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password tidak sama");
            etConfirmPassword.requestFocus();
            return;
        }

        // Periksa koneksi internet sebelum melanjutkan
        if (!isNetworkAvailable()) {
            Toast.makeText(RegisterActivity.this,
                    "Tidak ada koneksi internet. Silakan periksa koneksi Anda.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Tampilkan indikator loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Nonaktifkan tombol register untuk mencegah klik ganda
        btnRegister.setEnabled(false);

        // Register dengan Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Sembunyikan indikator loading
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        // Aktifkan kembali tombol register
                        btnRegister.setEnabled(true);

                        if (task.isSuccessful()) {
                            // Registrasi berhasil
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserData(user, username, email);
                        } else {
                            // Registrasi gagal
                            String errorMessage = "Registrasi gagal: ";
                            if (task.getException() != null) {
                                // Tambahkan pesan error spesifik berdasarkan exception
                                if (task.getException() instanceof FirebaseNetworkException) {
                                    errorMessage += "Masalah koneksi internet. Silakan coba lagi nanti.";
                                } else if (task.getException() instanceof FirebaseAuthException) {
                                    // Cek kode error untuk Firebase Auth
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                    if (errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE")) {
                                        errorMessage += "Email ini sudah terdaftar. Silakan gunakan email lain.";
                                    } else {
                                        errorMessage += task.getException().getMessage();
                                    }
                                } else {
                                    errorMessage += task.getException().getMessage();
                                }
                            }
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Metode terpisah untuk menyimpan data pengguna
    private void saveUserData(FirebaseUser user, String username, String email) {
        // Set nama tampilan ke nilai username
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileUpdates);

        // Buat data user yang akan disimpan
        String userId = user.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);

        // Simpan data user ke satu lokasi dulu, kemudian rantai operasi lainnya
        usersRef.child(userId).setValue(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Sekarang simpan mapping username
                        Map<String, Object> usernameMap = new HashMap<>();
                        usernameMap.put("userId", userId);
                        usernameMap.put("email", email);

                        usersRef.child("usernames").child(username).setValue(usernameMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Kirim email verifikasi setelah semua data tersimpan
                                        sendVerificationEmail(user);

                                        Toast.makeText(RegisterActivity.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();

                                        // Sign out dan pergi ke halaman login
                                        mAuth.signOut();
                                        goToLoginActivity();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this,
                                                "Gagal menyimpan username mapping: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        // Tetap lanjutkan ke verifikasi
                                        sendVerificationEmail(user);
                                        mAuth.signOut();
                                        goToLoginActivity();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this,
                                "Gagal menyimpan data pengguna: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        // Tetap kirim email verifikasi
                        sendVerificationEmail(user);
                        mAuth.signOut();
                        goToLoginActivity();
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Email verifikasi telah dikirim ke " + user.getEmail(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Gagal mengirim email verifikasi",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}