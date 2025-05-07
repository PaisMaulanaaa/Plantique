package com.example.plantique;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // Firebase Auth
    private FirebaseAuth mAuth;

    private CircleImageView ivProfile;
    private TextView tvUsername, tvBio, tvPostCount, tvFollowersCount, tvFollowingCount;
    private Button btnEditProfile;
    private ImageView ivTabGrid, ivTabBookmark;
    private ImageView ivNavHome, ivNavSearch, ivNavCamera, ivNavProfile;
    private ImageView btnBack, btnLogout;
    private FloatingActionButton fabAdd;
    private RecyclerView rvPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        try {
            // Inisialisasi view
            initViews();

            // Set data profil dari Firebase atau SharedPreferences
            setProfileData();

            // Set click listeners
            setClickListeners();

            // Setup RecyclerView jika tersedia
            if (rvPosts != null) {
                setupRecyclerView();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error di onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        try {
            // Profile views
            ivProfile = findViewById(R.id.iv_profile);
            tvUsername = findViewById(R.id.tv_username);
            tvBio = findViewById(R.id.tv_bio);
            tvPostCount = findViewById(R.id.tv_post_count);
            tvFollowersCount = findViewById(R.id.tv_followers_count);
            tvFollowingCount = findViewById(R.id.tv_following_count);
            btnEditProfile = findViewById(R.id.btn_edit_profile);

            // Tab views
            ivTabGrid = findViewById(R.id.iv_tab_grid);
            ivTabBookmark = findViewById(R.id.iv_tab_bookmark);

            // Navigation views - dengan pengecekan null
            btnBack = findViewById(R.id.btn_back);
            btnLogout = findViewById(R.id.btn_logout);
            fabAdd = findViewById(R.id.fab_add);

            // Bottom navigation
            ivNavHome = findViewById(R.id.iv_nav_home);
            ivNavSearch = findViewById(R.id.iv_nav_explore);
            ivNavCamera = findViewById(R.id.iv_nav_robot);
            ivNavProfile = findViewById(R.id.iv_nav_profile);

            // RecyclerView - dengan pengecekan null
            try {
                rvPosts = findViewById(R.id.rv_posts);
            } catch (Exception e) {
                Log.w(TAG, "RecyclerView tidak ditemukan", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saat inisialisasi view", e);
            throw e; // Re-throw untuk ditangkap di onCreate
        }
    }

    private void setProfileData() {
        // Cek apakah user login
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Set data dari Firebase user
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvUsername.setText(displayName);
            } else {
                tvUsername.setText("User Plantique");
            }

            String email = user.getEmail();
            if (email != null) {
                tvBio.setText(email);
            } else {
                tvBio.setText("Pengguna aplikasi Plantique");
            }
        } else {
            // Default data jika tidak ada user
            tvUsername.setText("Guest User");
            tvBio.setText("Silakan login untuk melihat profil Anda");
        }

        // Set stats default
        tvPostCount.setText("0");
        tvFollowersCount.setText("0");
        tvFollowingCount.setText("0");
    }

    private void setClickListeners() {
        // Tombol edit profil
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Edit Profil", Toast.LENGTH_SHORT).show();
        });

        // Tombol tab grid/bookmark
        ivTabGrid.setOnClickListener(v -> {
            ivTabGrid.setColorFilter(getResources().getColor(R.color.green));
            ivTabBookmark.setColorFilter(getResources().getColor(R.color.gray));
        });

        ivTabBookmark.setOnClickListener(v -> {
            ivTabBookmark.setColorFilter(getResources().getColor(R.color.green));
            ivTabGrid.setColorFilter(getResources().getColor(R.color.gray));
        });

        // Tombol floating action
        fabAdd.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Tambah Postingan", Toast.LENGTH_SHORT).show();
        });

        // Tombol back
        btnBack.setOnClickListener(v -> {
            // Kembali ke activity sebelumnya
            finish();
        });

        // Tombol logout
        btnLogout.setOnClickListener(v -> {
            if (mAuth != null) {
                mAuth.signOut();
                Toast.makeText(ProfileActivity.this, "Logout berhasil", Toast.LENGTH_SHORT).show();
                // Kembali ke LoginActivity
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Listener untuk tombol navigasi bawah
        ivNavHome.setOnClickListener(v -> {
            // Navigate to MainActivity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        ivNavSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Feed belum tersedia", Toast.LENGTH_SHORT).show();
        });

        ivNavCamera.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur AI belum tersedia", Toast.LENGTH_SHORT).show();
        });

        ivNavProfile.setOnClickListener(v -> {
            // Sudah berada di ProfileActivity
            Toast.makeText(this, "Anda sudah berada di halaman Profil", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        // Menggunakan GridLayoutManager dengan 3 kolom
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rvPosts.setLayoutManager(layoutManager);

        // Membuat adapter dummy sementara
        // Ini hanya implementasi minimal untuk menghindari crash
        List<String> dummyItems = new ArrayList<>();
        // Placeholder items - nanti bisa diganti dengan data real
        dummyItems.add("");
        dummyItems.add("");
        dummyItems.add("");

        // Set adapter dengan implementasi minimal
        rvPosts.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
                // Create empty view dengan ukuran 1dp untuk mencegah crash
                View view = new View(parent.getContext());
                view.setLayoutParams(new RecyclerView.LayoutParams(1, 1));
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                // Tidak ada yang perlu di-bind
            }

            @Override
            public int getItemCount() {
                return dummyItems.size();
            }
        });
    }
}