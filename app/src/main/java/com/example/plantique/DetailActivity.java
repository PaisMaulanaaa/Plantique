package com.example.plantique;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";

    private String plantId;
    private String plantSource; // Untuk menentukan sumber tanaman (admin/user)

    private ImageView plantImage;
    private TextView plantDescription;
    private ProgressBar progressBar;
    private RecyclerView recyclerViewTips;
    private TipAdapter tipAdapter;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Inisialisasi UI components
        setupViews();

        // Mendapatkan plant ID dan source dari intent
        plantId = getIntent().getStringExtra("plant_id");
        plantSource = getIntent().getStringExtra("plant_source");

        if (plantId == null || plantSource == null) {
            Toast.makeText(this, "Invalid plant data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseHelper = new FirebaseHelper();
        loadPlantDetails();
    }

    private void setupViews() {
        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        // View initialization
        plantImage = findViewById(R.id.iv_plant_image);
        plantDescription = findViewById(R.id.tv_plant_description);
        progressBar = findViewById(R.id.progress_circular);

        // RecyclerView for tips
        recyclerViewTips = findViewById(R.id.recycler_view_tips);
        recyclerViewTips.setLayoutManager(new LinearLayoutManager(this));

        tipAdapter = new TipAdapter(new ArrayList<>(), this);
        recyclerViewTips.setAdapter(tipAdapter);
    }

    private void loadPlantDetails() {
        // Menampilkan loading state
        progressBar.setVisibility(View.VISIBLE);

        // Menggunakan FirebaseHelper untuk mendapatkan data tanaman
        firebaseHelper.getPlantById(plantId, plantSource, new FirebaseHelper.PlantLoadedListener() {
            @Override
            public void onPlantLoaded(Plant plant) {
                progressBar.setVisibility(View.GONE);
                displayPlantDetails(plant);
                loadPlantTips();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayPlantDetails(Plant plant) {
        // Set title
        getSupportActionBar().setTitle(plant.getName());

        // Load image
        if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(plant.getImageUrl())
                    .placeholder(R.drawable.ic_plant_placeholder)
                    .error(R.drawable.ic_plant_placeholder)
                    .into(plantImage);
        } else {
            plantImage.setImageResource(R.drawable.ic_plant_placeholder);
        }

        // Set description
        if (plant.getDescription() != null && !plant.getDescription().isEmpty()) {
            plantDescription.setText(plant.getDescription());
        } else {
            plantDescription.setText("Tidak ada deskripsi tersedia");
        }

        // Tambahkan indikator sumber tanaman jika perlu
        TextView sourceIndicator = findViewById(R.id.source_indicator);
        if (sourceIndicator != null) {
            if (plantSource.equals("user_plants")) {
                sourceIndicator.setText("Tanaman Pribadi");
                sourceIndicator.setVisibility(View.VISIBLE);
            } else if (plantSource.equals("admin_plants")) {
                sourceIndicator.setText("Tanaman Admin");
                sourceIndicator.setVisibility(View.VISIBLE);
            } else {
                sourceIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void loadPlantTips() {
        String path;
        if (plantSource.equals("admin_plants")) {
            path = "tips/admin_plants/" + plantId;
        } else if (plantSource.equals("user_plants")) {
            path = "tips/user_plants/" + plantId;
        } else {
            Toast.makeText(this, "Invalid plant source for tips: " + plantSource, Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference tipsRef = FirebaseDatabase.getInstance().getReference(path);

        tipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Tip> tipsList = new ArrayList<>();

                for (DataSnapshot tipSnapshot : dataSnapshot.getChildren()) {
                    Tip tip = tipSnapshot.getValue(Tip.class);
                    if (tip != null) {
                        tip.setId(tipSnapshot.getKey());
                        tipsList.add(tip);
                    }
                }

                tipAdapter.updateData(tipsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DetailActivity.this,
                        "Error loading tips: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}