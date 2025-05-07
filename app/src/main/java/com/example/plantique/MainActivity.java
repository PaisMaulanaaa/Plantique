package com.example.plantique;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Navigation Views
    private ImageView ivHome, ivFeed, ivRobot, ivProfile;
    private FloatingActionButton fabAdd, fabAdmin;

    // Plant Containers
    private LinearLayout plantContainer1, plantContainer2, plantContainer3;
    private CardView cardMonstera, cardLidahBuaya, cardSukulen;

    // Tip Cards
    private CardView tipPenyiramanCard, tipCahayaCard;

    // Text Views
    private TextView tvLihatSemua, tvWelcomeTitle;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();

        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();

        Log.d(TAG, "MainActivity created");
    }

    private void initializeViews() {
        // Make sure setContentView is called first
        setContentView(R.layout.activity_main);

        // Navigation
        ivHome = findViewById(R.id.iv_nav_home);
        ivFeed = findViewById(R.id.iv_nav_explore);
        ivRobot = findViewById(R.id.iv_nav_robot);
        ivProfile = findViewById(R.id.iv_nav_profile);
        fabAdd = findViewById(R.id.fab_add);
        fabAdmin = findViewById(R.id.fab_admin);

        // Plant Containers - add null checks
        View monsteraLayout = findViewById(R.id.plant_monstera_layout);
        if (monsteraLayout instanceof LinearLayout) {
            plantContainer1 = (LinearLayout) monsteraLayout;
        } else {
            Log.e(TAG, "plant_monstera_layout is not a LinearLayout");
        }

        View lidahBuayaLayout = findViewById(R.id.plant_lidah_buaya_layout);
        if (lidahBuayaLayout instanceof LinearLayout) {
            plantContainer2 = (LinearLayout) lidahBuayaLayout;
        } else {
            Log.e(TAG, "plant_lidah_buaya_layout is not a LinearLayout");
        }

        View sukulenLayout = findViewById(R.id.plant_sukulen_layout);
        if (sukulenLayout instanceof LinearLayout) {
            plantContainer3 = (LinearLayout) sukulenLayout;
        } else {
            Log.e(TAG, "plant_sukulen_layout is not a LinearLayout");
        }

        // Cards
        cardMonstera = findViewById(R.id.card_monstera);
        cardLidahBuaya = findViewById(R.id.card_lidah_buaya);
        cardSukulen = findViewById(R.id.card_sukulen);

        // Tips
        tipPenyiramanCard = findViewById(R.id.tip_penyiraman_card);
        tipCahayaCard = findViewById(R.id.tip_cahaya_card);

        // Text Views
        tvWelcomeTitle = findViewById(R.id.tv_welcome_title);
        tvLihatSemua = findViewById(R.id.tv_lihat_semua);

        // Progress Bar
        progressBar = findViewById(R.id.progress_bar);
        if (progressBar == null) {
            progressBar = new ProgressBar(this);
            progressBar.setId(View.generateViewId());

            ConstraintLayout rootLayout = findViewById(R.id.root_layout);
            if (rootLayout != null) {
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT);
                rootLayout.addView(progressBar, params);

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(rootLayout);
                constraintSet.connect(progressBar.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(progressBar.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                constraintSet.connect(progressBar.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(progressBar.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.applyTo(rootLayout);
            }
        }

        Log.d(TAG, "Views initialized");
    }

    private void setupClickListeners() {
        // Navigation
        ivHome.setOnClickListener(v -> {
            Log.d(TAG, "Home icon clicked");
        });

        ivFeed.setOnClickListener(v -> {
            Log.d(TAG, "Feed icon clicked");
            startActivity(new Intent(MainActivity.this, FeedActivity.class));
        });

        ivRobot.setOnClickListener(v -> {
            Log.d(TAG, "Robot icon clicked");
            startActivity(new Intent(MainActivity.this, AIActivity.class));
        });

        ivProfile.setOnClickListener(v -> {
            Log.d(TAG, "Profile icon clicked");
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        fabAdd.setOnClickListener(v -> {
            Log.d(TAG, "Add FAB clicked");
            startActivity(new Intent(MainActivity.this, AddActivity.class));
        });

        tvLihatSemua.setOnClickListener(v -> {
            Log.d(TAG, "Lihat semua clicked");
            Toast.makeText(this, "Lihat semua tanaman akan segera tersedia", Toast.LENGTH_SHORT).show();
        });

        Log.d(TAG, "Click listeners set up");
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in
            Log.d(TAG, "User is signed in: " + currentUser.getEmail());
            String welcomeText = "Welcome";
            if (currentUser.getDisplayName() != null) {
                welcomeText = "Hi, " + currentUser.getDisplayName();
            }
            tvWelcomeTitle.setText(welcomeText);

            // Check if admin
            firebaseHelper.getCurrentUser(new FirebaseHelper.UserLoadedListener() {
                @Override
                public void onUserLoaded(User user) {
                    if (user != null && user.isAdmin()) {
                        fabAdmin.setVisibility(View.VISIBLE);
                        fabAdmin.setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, AddActivity.class);
                            intent.putExtra("mode", "admin");
                            startActivity(intent);
                        });
                    } else {
                        fabAdmin.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Error checking admin status: " + errorMessage);
                    fabAdmin.setVisibility(View.GONE);
                }
            });

            loadPlantData();
            loadTips();
        } else {
            // No user is signed in
            Log.d(TAG, "No user is signed in, redirecting to login");
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void loadPlantData() {
        Log.d(TAG, "Loading plant data...");
        progressBar.setVisibility(View.VISIBLE);

        // Set a timeout in case Firebase doesn't respond
        new Handler().postDelayed(() -> {
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
                Log.w(TAG, "Firebase data loading timeout - displaying default message");
                Toast.makeText(MainActivity.this, "Gagal memuat data tanaman. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
            }
        }, 15000); // 15 seconds timeout

        firebaseHelper.getPlantsWithCareInstructions(new FirebaseHelper.PlantsLoadedListener() {
            @Override
            public void onPlantsLoaded(List<Plant> plants) {
                progressBar.setVisibility(View.GONE);

                if (plants != null && !plants.isEmpty()) {
                    // Add detailed logging
                    Log.d(TAG, "Plants loaded successfully: " + plants.size() + " plants");
                    for (Plant plant : plants) {
                        Log.d(TAG, "Plant data: " + plant.getName() + ", imageUrl: " + plant.getImageUrl());
                    }
                    updatePlantViews(plants);
                } else {
                    Log.w(TAG, "No plants loaded from Firebase");
                    // Show empty state or default plants
                    Toast.makeText(MainActivity.this, "Tidak ada data tanaman", Toast.LENGTH_SHORT).show();

                    // Optionally show placeholder plants instead
                    showPlaceholderPlants();
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading plants: " + errorMessage);
                Toast.makeText(MainActivity.this, "Gagal memuat data: " + errorMessage, Toast.LENGTH_SHORT).show();

                // Show placeholder plants on error
                showPlaceholderPlants();
            }
        });
    }

    private void showPlaceholderPlants() {
        // Create placeholder plants for testing
        List<Plant> placeholderPlants = new ArrayList<>();

        Plant monstera = new Plant();
        monstera.setId("placeholder1");
        monstera.setName("Monstera");
        monstera.setImageUrl("https://i.ibb.co/h7LNDzZ/monstera.jpg"); // Sample URL - replace with your actual test URL
        placeholderPlants.add(monstera);

        Plant aloe = new Plant();
        aloe.setId("placeholder2");
        aloe.setName("Lidah Buaya");
        aloe.setImageUrl("https://i.ibb.co/KxxsJvL/aloevera.jpg"); // Sample URL - replace with your actual test URL
        placeholderPlants.add(aloe);

        Plant succulent = new Plant();
        succulent.setId("placeholder3");
        succulent.setName("Sukulen");
        succulent.setImageUrl("https://i.ibb.co/M9k7c6Z/succulent.jpg"); // Sample URL - replace with your actual test URL
        placeholderPlants.add(succulent);

        updatePlantViews(placeholderPlants);
    }

    private void updatePlantViews(List<Plant> plants) {
        // Clear previous data
        clearPlantViews();

        // Update plant 1
        if (plants.size() > 0) {
            Log.d(TAG, "Setting up plant 1: " + plants.get(0).getName());
            setupPlantView(plantContainer1, plants.get(0));
            cardMonstera.setVisibility(View.VISIBLE); // Explicitly set card to visible
        }

        // Update plant 2
        if (plants.size() > 1) {
            Log.d(TAG, "Setting up plant 2: " + plants.get(1).getName());
            setupPlantView(plantContainer2, plants.get(1));
            cardLidahBuaya.setVisibility(View.VISIBLE); // Explicitly set card to visible
        }

        // Update plant 3
        if (plants.size() > 2) {
            Log.d(TAG, "Setting up plant 3: " + plants.get(2).getName());
            setupPlantView(plantContainer3, plants.get(2));
            cardSukulen.setVisibility(View.VISIBLE); // Explicitly set card to visible
        }
    }

    private void clearPlantViews() {
        Log.d(TAG, "Clearing plant views");

        // Hide containers first to prevent flashing old content
        if (plantContainer1 != null) plantContainer1.setVisibility(View.INVISIBLE);
        if (plantContainer2 != null) plantContainer2.setVisibility(View.INVISIBLE);
        if (plantContainer3 != null) plantContainer3.setVisibility(View.INVISIBLE);

        // Hide cards
        if (cardMonstera != null) cardMonstera.setVisibility(View.INVISIBLE);
        if (cardLidahBuaya != null) cardLidahBuaya.setVisibility(View.INVISIBLE);
        if (cardSukulen != null) cardSukulen.setVisibility(View.INVISIBLE);
    }

    private void setupPlantView(LinearLayout container, Plant plant) {
        if (container == null || plant == null) {
            Log.e(TAG, "Container or plant is null");
            return;
        }

        try {
            // Find views in the container with improved approach
            ImageView plantImage = null;
            TextView plantName = null;

            // Determine correct views based on container
            if (container == plantContainer1) {
                plantImage = container.findViewById(R.id.img_monstera);
                plantName = container.findViewById(R.id.txt_monstera);
            } else if (container == plantContainer2) {
                plantImage = container.findViewById(R.id.img_lidah_buaya);
                plantName = container.findViewById(R.id.txt_lidah_buaya);
            } else if (container == plantContainer3) {
                plantImage = container.findViewById(R.id.img_sukulen);
                plantName = container.findViewById(R.id.txt_sukulen);
            }

            // Fallback method if specific IDs don't work
            if (plantImage == null) {
                // Find first ImageView in container
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof ImageView) {
                        plantImage = (ImageView) child;
                        break;
                    }
                }
            }

            if (plantName == null) {
                // Find first TextView in container
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof TextView) {
                        plantName = (TextView) child;
                        break;
                    }
                }
            }

            if (plantImage == null || plantName == null) {
                Log.e(TAG, "Could not find ImageView or TextView in container");
                return;
            }

            // Set plant name
            plantName.setText(plant.getName());

            // Load plant image with detailed logging
            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                Log.d(TAG, "Loading image URL: " + plant.getImageUrl());

                // Clear any previous image first to prevent old images from showing
                plantImage.setImageResource(R.drawable.ic_plant_placeholder);

                // Make plantImage final (effectively) by using a final reference
                final ImageView finalPlantImage = plantImage;

                Picasso.get()
                        .load(plant.getImageUrl())
                        .placeholder(R.drawable.ic_plant_placeholder)
                        .error(R.drawable.ic_plant_placeholder)
                        .into(finalPlantImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Image loaded successfully: " + plant.getName());
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Error loading image for " + plant.getName() + ": " + e.getMessage(), e);
                                finalPlantImage.setImageResource(R.drawable.ic_plant_placeholder);
                            }
                        });
            } else {
                Log.d(TAG, "No image URL for " + plant.getName() + ", using placeholder");
                plantImage.setImageResource(R.drawable.ic_plant_placeholder);
            }

            // Set click listener
            container.setOnClickListener(v -> openPlantDetail(plant));

            // Make container visible
            container.setVisibility(View.VISIBLE);

            Log.d(TAG, "Plant view set up successfully for: " + plant.getName());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up plant view: " + e.getMessage(), e);
        }
    }

    private void openPlantDetail(Plant plant) {
        if (plant == null || plant.getId() == null) {
            Log.e(TAG, "Invalid plant data for detail view");
            Toast.makeText(this, "Data tanaman tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Opening detail for plant: " + plant.getName() + ", ID: " + plant.getId());
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("plant_id", plant.getId());
        intent.putExtra("plant_source", plant.getSource());
        startActivity(intent);
    }

    private void loadTips() {
        Log.d(TAG, "Loading tips...");
        firebaseHelper.getAllGeneralTips(new FirebaseHelper.TipsLoadedListener() {
            @Override
            public void onTipsLoaded(List<Tip> tips) {
                if (tips != null && tips.size() >= 2) {
                    Log.d(TAG, "Tips loaded successfully: " + tips.size() + " tips");
                    setupTipCard(tipPenyiramanCard, tips.get(0));
                    setupTipCard(tipCahayaCard, tips.get(1));
                } else {
                    Log.w(TAG, "Not enough tips loaded");
                    // Create default tips
                    List<Tip> defaultTips = new ArrayList<>();

                    Tip wateringTip = new Tip();
                    wateringTip.setTitle("Penyiraman Yang Tepat");
                    wateringTip.setDescription("Siram tanaman hanya ketika tanah terasa kering saat disentuh. Penyiraman berlebihan dapat menyebabkan pembusukan akar.");
                    wateringTip.setIcon("ic_watering");
                    defaultTips.add(wateringTip);

                    Tip lightTip = new Tip();
                    lightTip.setTitle("Cahaya yang Cukup");
                    lightTip.setDescription("Pastikan tanaman Anda mendapatkan cahaya yang cukup sesuai dengan kebutuhannya. Beberapa tanaman membutuhkan sinar matahari langsung.");
                    lightTip.setIcon("ic_light");
                    defaultTips.add(lightTip);

                    setupTipCard(tipPenyiramanCard, defaultTips.get(0));
                    setupTipCard(tipCahayaCard, defaultTips.get(1));
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading tips: " + errorMessage);
            }
        });
    }

    private void setupTipCard(CardView card, Tip tip) {
        if (card == null || tip == null) {
            Log.e(TAG, "Card or tip is null");
            return;
        }

        try {
            TextView title = card.findViewById(R.id.tip_title);
            TextView desc = card.findViewById(R.id.tip_description);
            ImageView icon = card.findViewById(R.id.tip_icon);

            if (title != null && desc != null) {
                title.setText(tip.getTitle());
                desc.setText(tip.getDescription());

                if (icon != null && tip.getIcon() != null) {
                    int iconRes = getResources().getIdentifier(tip.getIcon(), "drawable", getPackageName());
                    if (iconRes != 0) {
                        icon.setImageResource(iconRes);
                    }
                }

                Log.d(TAG, "Tip card set up successfully for: " + tip.getTitle());
            } else {
                Log.e(TAG, "Could not find title or description TextView");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up tip card: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to activity
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "onResume: refreshing data");
            loadPlantData();
        }
    }
}