package com.example.plantique;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView rvFeed;
    private FeedAdapter feedAdapter;
    private List<FeedPost> feedPosts;
    private ImageView ivNavHome, ivNavExplore, ivNavRobot, ivNavProfile, ivSearch;

    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();

        // Initialize views
        rvFeed = findViewById(R.id.rv_feed);
        ivNavHome = findViewById(R.id.iv_nav_home);
        ivNavExplore = findViewById(R.id.iv_nav_explore);
        ivNavRobot = findViewById(R.id.iv_nav_robot);
        ivNavProfile = findViewById(R.id.iv_nav_profile);
        ivSearch = findViewById(R.id.iv_search);

        // Setup RecyclerView
        feedPosts = new ArrayList<>();
        feedAdapter = new FeedAdapter(this, feedPosts);
        rvFeed.setLayoutManager(new LinearLayoutManager(this));
        rvFeed.setAdapter(feedAdapter);

        // Load feed data
        loadFeedData();

        // Set click listeners for navigation
        ivNavHome.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        ivNavExplore.setOnClickListener(v -> {
            // Already on Explore/Feed screen
        });

        ivNavRobot.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, AIActivity.class);
            startActivity(intent);
        });

        ivNavProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        ivSearch.setOnClickListener(v -> {
            Toast.makeText(FeedActivity.this, "Fitur pencarian belum tersedia", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFeedData() {
        // Demo data - Nanti bisa diganti dengan data dari Firebase
        List<FeedPost> demoData = createDemoData();
        feedAdapter.updateData(demoData);
    }

    private List<FeedPost> createDemoData() {
        List<FeedPost> demoData = new ArrayList<>();

        // Post 1
        FeedPost post1 = new FeedPost(
                "1",
                "user1",
                "Brian O'Connor",
                "",
                "Osirian Rose",
                "https://example.com/plant1.jpg",
                "Osirian Rose adalah tanaman yang indah dengan warna merah tua yang khas.",
                System.currentTimeMillis()
        );
        demoData.add(post1);

        // Post 2
        FeedPost post2 = new FeedPost(
                "2",
                "user2",
                "Ihsan Mahardika",
                "",
                "Mini Moth Orchid",
                "https://example.com/plant2.jpg",
                "Mini Moth Orchid adalah anggrek mini yang cantik dengan bunga berwarna pink.",
                System.currentTimeMillis() - 86400000 // 1 day ago
        );
        demoData.add(post2);

        // Post 3
        FeedPost post3 = new FeedPost(
                "3",
                "user2",
                "Erik Manurung",
                "",
                "SunFlower",
                "https://example.com/plant3.jpg",
                "Bunga matahari yang cerah dan menyegarkan.",
                System.currentTimeMillis() - 172800000 // 2 days ago
        );
        demoData.add(post3);

        return demoData;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLoginActivity();
        }
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(FeedActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}