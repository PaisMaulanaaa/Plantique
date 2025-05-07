package com.example.plantique;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddActivity extends AppCompatActivity {
    private static final String TAG = "AddActivity";

    private TextInputEditText editPlantName, editPlantType, editPlantDescription;
    private TextInputEditText editWatering, editLighting, editSoilMedia;
    private TextInputEditText editHumidity, editFertilizing, editTemperature;
    private TextInputEditText editImageUrl; // Field untuk URL gambar
    private Button btnAddImage, btnSave;
    private ImageView imgPlantPreview;

    private FirebaseHelper firebaseHelper;
    private String currentImageUrl = "";

    // Mode penambahan tanaman (admin/user)
    private boolean isUserPlant = true; // Default mode untuk user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        firebaseHelper = new FirebaseHelper();

        // Periksa intent untuk mode penambahan tanaman
        if (getIntent().hasExtra("mode")) {
            String mode = getIntent().getStringExtra("mode");
            isUserPlant = "user".equals(mode);
            Log.d(TAG, "Plant creation mode: " + mode);
        }

        // Inisialisasi komponen UI
        initializeViews();
        setupToolbar();
        setupClickListeners();
    }

    private void initializeViews() {
        // Informasi dasar
        editPlantName = findViewById(R.id.edit_plant_name);
        editPlantType = findViewById(R.id.edit_plant_type);
        editPlantDescription = findViewById(R.id.edit_plant_description);
        editImageUrl = findViewById(R.id.edit_image_url); // EditText untuk URL gambar
        btnAddImage = findViewById(R.id.btn_add_image); // Menggunakan ID sesuai di layout XML
        imgPlantPreview = findViewById(R.id.img_plant_preview);

        // Detail perawatan
        editWatering = findViewById(R.id.edit_watering);
        editLighting = findViewById(R.id.edit_lighting);
        editSoilMedia = findViewById(R.id.edit_soil_media);
        editHumidity = findViewById(R.id.edit_humidity);
        editFertilizing = findViewById(R.id.edit_fertilizing);
        editTemperature = findViewById(R.id.edit_temperature);

        // Tombol simpan
        btnSave = findViewById(R.id.btn_save);

        Log.d(TAG, "Views initialized successfully");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(isUserPlant ? "Tambah Tanaman Pribadi" : "Tambah Tanaman Admin");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Log.d(TAG, "Toolbar setup complete");
    }

    private void setupClickListeners() {
        btnAddImage.setOnClickListener(v -> {
            Log.d(TAG, "Preview image button clicked");
            previewImage();
        });

        btnSave.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");
            savePlantCare();
        });

        Log.d(TAG, "Click listeners setup complete");
    }

    private void previewImage() {
        String imageUrl = editImageUrl.getText().toString().trim();
        if (imageUrl.isEmpty()) {
            Toast.makeText(this, "URL gambar tidak boleh kosong", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Image URL is empty");
            return;
        }

        // Load gambar dengan Picasso
        try {
            Log.d(TAG, "Loading image from URL: " + imageUrl);
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_plant_placeholder)
                    .error(R.drawable.ic_plant_placeholder)
                    .into(imgPlantPreview);

            imgPlantPreview.setVisibility(View.VISIBLE);
            currentImageUrl = imageUrl;
            Log.d(TAG, "Image loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            Toast.makeText(this, "Error memuat gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePlantCare() {
        Log.d(TAG, "Starting plant save process");

        if (!validateForm()) {
            Log.w(TAG, "Form validation failed");
            return;
        }

        // Show progress indicator
        View progressOverlay = findViewById(R.id.progress_overlay);
        if (progressOverlay != null) {
            progressOverlay.setVisibility(View.VISIBLE);
        }

        // Disable save button to prevent double submission
        btnSave.setEnabled(false);

        Toast.makeText(this, "Menyimpan data tanaman...", Toast.LENGTH_SHORT).show();

        // Simpan data langsung dengan URL gambar yang disediakan
        saveDataToFirebase(currentImageUrl);
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (editPlantName.getText().toString().trim().isEmpty()) {
            editPlantName.setError("Nama tanaman tidak boleh kosong");
            isValid = false;
        }

        if (editWatering.getText().toString().trim().isEmpty()) {
            editWatering.setError("Informasi penyiraman harus diisi");
            isValid = false;
        }

        if (editLighting.getText().toString().trim().isEmpty()) {
            editLighting.setError("Informasi pencahayaan harus diisi");
            isValid = false;
        }

        // Validasi kolom lainnya
        if (editSoilMedia.getText().toString().trim().isEmpty()) {
            editSoilMedia.setError("Informasi media tanam harus diisi");
            isValid = false;
        }

        if (editHumidity.getText().toString().trim().isEmpty()) {
            editHumidity.setError("Informasi kelembaban harus diisi");
            isValid = false;
        }

        if (editFertilizing.getText().toString().trim().isEmpty()) {
            editFertilizing.setError("Informasi pemupukan harus diisi");
            isValid = false;
        }

        if (editTemperature.getText().toString().trim().isEmpty()) {
            editTemperature.setError("Informasi suhu harus diisi");
            isValid = false;
        }

        if (currentImageUrl.isEmpty()) {
            editImageUrl.setError("URL gambar harus diisi dan dipreview");
            Toast.makeText(this, "Tambahkan dan preview gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        Log.d(TAG, "Form validation result: " + (isValid ? "Valid" : "Invalid"));
        return isValid;
    }

    private void saveDataToFirebase(String imageUrl) {
        // Buat objek tanaman baru
        String plantId = UUID.randomUUID().toString();
        Log.d(TAG, "Creating new plant with ID: " + plantId);

        // Ambil nilai dari input fields
        String plantName = editPlantName.getText().toString().trim();
        String plantDescription = editPlantDescription.getText().toString().trim();
        String plantType = editPlantType.getText().toString().trim();
        String wateringInfo = editWatering.getText().toString().trim();
        String lightingInfo = editLighting.getText().toString().trim();
        String soilMediaInfo = editSoilMedia.getText().toString().trim();
        String humidityInfo = editHumidity.getText().toString().trim();
        String fertilizingInfo = editFertilizing.getText().toString().trim();
        String temperatureInfo = editTemperature.getText().toString().trim();

        // Buat objek tanaman dengan instruksi perawatan
        Map<String, Object> careInstructions = new HashMap<>();
        careInstructions.put("id", plantId);
        careInstructions.put("name", plantName);
        careInstructions.put("description", plantDescription);
        careInstructions.put("category", plantType);
        careInstructions.put("imageUrl", imageUrl);
        careInstructions.put("waterSchedule", wateringInfo);
        careInstructions.put("sunlightNeed", lightingInfo);
        careInstructions.put("source", isUserPlant ? "user" : "admin");

        // Tambahkan info tambahan untuk soil, humidity, fertilizing, dan temperature
        careInstructions.put("soilMedia", soilMediaInfo);
        careInstructions.put("humidity", humidityInfo);
        careInstructions.put("fertilizing", fertilizingInfo);
        careInstructions.put("temperature", temperatureInfo);

        // Buat tips perawatan
        Map<String, Object> tips = new HashMap<>();

        // Tambah tip penyiraman
        Map<String, Object> wateringTip = new HashMap<>();
        wateringTip.put("id", UUID.randomUUID().toString());
        wateringTip.put("title", "Penyiraman");
        wateringTip.put("description", wateringInfo);
        wateringTip.put("icon", "ic_water_drop");
        wateringTip.put("iconColor", "blue_primary");
        tips.put("watering", wateringTip);

        // Tambah tip pencahayaan
        Map<String, Object> lightingTip = new HashMap<>();
        lightingTip.put("id", UUID.randomUUID().toString());
        lightingTip.put("title", "Pencahayaan");
        lightingTip.put("description", lightingInfo);
        lightingTip.put("icon", "ic_sun");
        lightingTip.put("iconColor", "yellow_primary");
        tips.put("lighting", lightingTip);

        // Tambah tip media tanam
        Map<String, Object> soilTip = new HashMap<>();
        soilTip.put("id", UUID.randomUUID().toString());
        soilTip.put("title", "Media Tanam");
        soilTip.put("description", soilMediaInfo);
        soilTip.put("icon", "ic_dirt");
        soilTip.put("iconColor", "brown");
        tips.put("soil", soilTip);

        // Tambah tip kelembaban
        Map<String, Object> humidityTip = new HashMap<>();
        humidityTip.put("id", UUID.randomUUID().toString());
        humidityTip.put("title", "Kelembaban");
        humidityTip.put("description", humidityInfo);
        humidityTip.put("icon", "ic_humidity");
        humidityTip.put("iconColor", "green_light");
        tips.put("humidity", humidityTip);

        // Tambah tip pemupukan
        Map<String, Object> fertilizingTip = new HashMap<>();
        fertilizingTip.put("id", UUID.randomUUID().toString());
        fertilizingTip.put("title", "Pemupukan");
        fertilizingTip.put("description", fertilizingInfo);
        fertilizingTip.put("icon", "ic_fertilizer");
        fertilizingTip.put("iconColor", "purple");
        tips.put("fertilizing", fertilizingTip);

        // Tambah tip suhu
        Map<String, Object> temperatureTip = new HashMap<>();
        temperatureTip.put("id", UUID.randomUUID().toString());
        temperatureTip.put("title", "Suhu & Iklim");
        temperatureTip.put("description", temperatureInfo);
        temperatureTip.put("icon", "ic_temperature");
        temperatureTip.put("iconColor", "red");
        tips.put("temperature", temperatureTip);

        // Tambahkan tips ke objek tanaman
        careInstructions.put("tips", tips);

        // Buat objek tanaman untuk dimasukkan ke Firebase
        Map<String, Object> plantData = new HashMap<>();
        plantData.put("careInstructions", careInstructions);

        Log.d(TAG, "Plant data prepared, saving to " + (isUserPlant ? "user" : "admin") + " collection");

        // Simpan tanaman ke Firebase sesuai mode (admin/user)
        if (isUserPlant) {
            // Simpan ke database user
            firebaseHelper.saveUserPlantData(plantId, plantData, new FirebaseHelper.SaveCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "User plant saved successfully");
                    // Hide progress indicator
                    hideProgressAndFinish("Tanaman pribadi berhasil disimpan");
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to save user plant", e);
                    // Hide progress indicator
                    hideProgressAndEnableSave("Gagal menyimpan: " + e.getMessage());
                }
            });
        } else {
            // Add code for saving admin plants
            firebaseHelper.saveAdminPlantData(plantId, plantData, new FirebaseHelper.SaveCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Admin plant saved successfully");
                    // Hide progress indicator
                    hideProgressAndFinish("Tanaman admin berhasil disimpan");
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to save admin plant", e);
                    // Hide progress indicator
                    hideProgressAndEnableSave("Gagal menyimpan: " + e.getMessage());
                }
            });
        }
    }

    private void hideProgressAndFinish(String message) {
        runOnUiThread(() -> {
            // Hide progress indicator
            View progressOverlay = findViewById(R.id.progress_overlay);
            if (progressOverlay != null) {
                progressOverlay.setVisibility(View.GONE);
            }

            // Re-enable save button
            btnSave.setEnabled(true);

            // Show success message
            Toast.makeText(AddActivity.this, message, Toast.LENGTH_SHORT).show();

            // Finish activity
            finish();
        });
    }

    private void hideProgressAndEnableSave(String errorMessage) {
        runOnUiThread(() -> {
            // Hide progress indicator
            View progressOverlay = findViewById(R.id.progress_overlay);
            if (progressOverlay != null) {
                progressOverlay.setVisibility(View.GONE);
            }

            // Re-enable save button
            btnSave.setEnabled(true);

            // Show error message
            Toast.makeText(AddActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        });
    }
}