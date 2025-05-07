package com.example.plantique;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private User currentUser;

    // Collection paths in Firestore
    private static final String USERS_COLLECTION = "users";
    private static final String ADMIN_PLANTS_COLLECTION = "admin_plants";
    private static final String USER_PLANTS_COLLECTION = "user_plants";
    private static final String GENERAL_TIPS_COLLECTION = "general_tips";

    // Interfaces
    public interface UserLoadedListener {
        void onUserLoaded(User user);
        void onError(String errorMessage);
    }

    public interface PlantLoadedListener {
        void onPlantLoaded(Plant plant);
        void onError(String errorMessage);
    }

    public interface PlantsLoadedListener {
        void onPlantsLoaded(List<Plant> plants);
        void onError(String errorMessage);
    }

    public interface TipsLoadedListener {
        void onTipsLoaded(List<Tip> tips);
        void onError(String errorMessage);
    }

    public interface SaveCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loadCurrentUser();
    }

    // User Management
    private void loadCurrentUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            db.collection(USERS_COLLECTION).document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUser = documentSnapshot.toObject(User.class);
                        } else {
                            createBasicUser(firebaseUser, userId);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading user data", e));
        }
    }

    private void createBasicUser(FirebaseUser firebaseUser, String userId) {
        if (firebaseUser.getEmail() != null) {
            String fullName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User";
            currentUser = new User(userId, fullName, firebaseUser.getEmail());

            db.collection(USERS_COLLECTION).document(userId).set(currentUser)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "New user created successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error creating new user", e));
        }
    }

    public void getCurrentUser(UserLoadedListener listener) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            db.collection(USERS_COLLECTION).document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            currentUser = user;
                            listener.onUserLoaded(user);
                        } else {
                            createAndReturnNewUser(firebaseUser, userId, listener);
                        }
                    })
                    .addOnFailureListener(e -> listener.onError("Failed to load user: " + e.getMessage()));
        } else {
            listener.onError("No user is signed in");
        }
    }

    private void createAndReturnNewUser(FirebaseUser firebaseUser, String userId, UserLoadedListener listener) {
        String fullName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User";
        User newUser = new User(userId, fullName, firebaseUser.getEmail());

        db.collection(USERS_COLLECTION).document(userId).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    currentUser = newUser;
                    listener.onUserLoaded(newUser);
                })
                .addOnFailureListener(e -> listener.onError("Failed to create user: " + e.getMessage()));
    }

    // Plant Management
    public void getAllPlants(PlantsLoadedListener listener) {
        Log.d(TAG, "Loading plants from Firestore...");
        List<Plant> allPlants = new ArrayList<>();

        db.collection(ADMIN_PLANTS_COLLECTION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        try {
                            String plantId = documentSnapshot.getId();
                            Log.d(TAG, "Processing plant: " + plantId);

                            Plant plant = new Plant();
                            plant.setId(plantId);
                            plant.setSource("admin");

                            // Extract core plant data
                            if (documentSnapshot.contains("name")) {
                                plant.setName(documentSnapshot.getString("name"));
                            } else {
                                plant.setName(plantId);
                            }

                            if (documentSnapshot.contains("imageUrl")) {
                                plant.setImageUrl(documentSnapshot.getString("imageUrl"));
                            }

                            if (documentSnapshot.contains("category")) {
                                plant.setCategory(documentSnapshot.getString("category"));
                            }

                            if (documentSnapshot.contains("description")) {
                                plant.setDescription(documentSnapshot.getString("description"));
                            }

                            if (documentSnapshot.contains("sunlightNeed")) {
                                plant.setSunlightNeed(documentSnapshot.getString("sunlightNeed"));
                            }

                            if (documentSnapshot.contains("waterSchedule")) {
                                plant.setWaterSchedule(documentSnapshot.getString("waterSchedule"));
                            }

                            // Extract tips data
                            if (documentSnapshot.contains("tips")) {
                                Map<String, Object> tipsRaw = (Map<String, Object>) documentSnapshot.get("tips");
                                Map<String, Map<String, Object>> tipsMap = new HashMap<>();

                                if (tipsRaw != null) {
                                    for (Map.Entry<String, Object> entry : tipsRaw.entrySet()) {
                                        String tipId = entry.getKey();
                                        Map<String, Object> tipData = (Map<String, Object>) entry.getValue();
                                        tipsMap.put(tipId, tipData);
                                    }
                                }

                                plant.setTips(tipsMap);
                            }

                            allPlants.add(plant);
                            Log.d(TAG, "Added plant: " + plant.getName());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing plant data: " + e.getMessage(), e);
                        }
                    }

                    if (allPlants.isEmpty()) {
                        listener.onError("No plants found in database");
                    } else {
                        listener.onPlantsLoaded(allPlants);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Database error: " + e.getMessage());
                    listener.onError("Failed to load plants: " + e.getMessage());
                });
    }

    /**
     * Gets a plant by ID from the default admin_plants collection
     * (Original method kept for backward compatibility)
     *
     * @param plantId Plant ID to retrieve
     * @param listener Callback to handle the loaded plant
     */
    public void getPlantById(String plantId, PlantLoadedListener listener) {
        getPlantById(plantId, "admin_plants", listener);
    }

    /**
     * Gets a plant by ID from the specified source (admin_plants or user_plants)
     *
     * @param plantId Plant ID to retrieve
     * @param plantSource Source of the plant ("admin_plants" or "user_plants")
     * @param listener Callback to handle the loaded plant
     */
    public void getPlantById(String plantId, String plantSource, PlantLoadedListener listener) {
        if (plantId == null || plantSource == null) {
            listener.onError("Invalid plant ID or source");
            return;
        }

        String collectionPath;
        if (plantSource.equals("admin_plants")) {
            collectionPath = ADMIN_PLANTS_COLLECTION;
        } else if (plantSource.equals("user_plants")) {
            collectionPath = USER_PLANTS_COLLECTION;
        } else {
            listener.onError("Invalid plant source: " + plantSource);
            return;
        }

        db.collection(collectionPath).document(plantId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Plant plant = new Plant();
                            plant.setId(plantId);
                            plant.setSource(plantSource.equals("admin_plants") ? "admin" : "user");

                            // Extract core plant data
                            if (documentSnapshot.contains("name")) {
                                plant.setName(documentSnapshot.getString("name"));
                            } else {
                                plant.setName(plantId);
                            }

                            if (documentSnapshot.contains("imageUrl")) {
                                plant.setImageUrl(documentSnapshot.getString("imageUrl"));
                            }

                            if (documentSnapshot.contains("category")) {
                                plant.setCategory(documentSnapshot.getString("category"));
                            }

                            if (documentSnapshot.contains("description")) {
                                plant.setDescription(documentSnapshot.getString("description"));
                            }

                            if (documentSnapshot.contains("sunlightNeed")) {
                                plant.setSunlightNeed(documentSnapshot.getString("sunlightNeed"));
                            }

                            if (documentSnapshot.contains("waterSchedule")) {
                                plant.setWaterSchedule(documentSnapshot.getString("waterSchedule"));
                            }

                            // Extract tips data
                            if (documentSnapshot.contains("tips")) {
                                Map<String, Object> tipsRaw = (Map<String, Object>) documentSnapshot.get("tips");
                                Map<String, Map<String, Object>> tipsMap = new HashMap<>();

                                if (tipsRaw != null) {
                                    for (Map.Entry<String, Object> entry : tipsRaw.entrySet()) {
                                        String tipId = entry.getKey();
                                        Map<String, Object> tipData = (Map<String, Object>) entry.getValue();
                                        tipsMap.put(tipId, tipData);
                                    }
                                }

                                plant.setTips(tipsMap);
                            }

                            listener.onPlantLoaded(plant);
                        } catch (Exception e) {
                            listener.onError("Error parsing plant data: " + e.getMessage());
                        }
                    } else {
                        listener.onError("Plant not found");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error getting plant: " + e.getMessage()));
    }

    // Tips Management
    public void getTipsForPlant(String plantId, TipsLoadedListener listener) {
        db.collection(ADMIN_PLANTS_COLLECTION).document(plantId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("tips")) {
                        Map<String, Object> tipsRaw = (Map<String, Object>) documentSnapshot.get("tips");
                        List<Tip> tipsList = new ArrayList<>();

                        if (tipsRaw != null && !tipsRaw.isEmpty()) {
                            for (Map.Entry<String, Object> entry : tipsRaw.entrySet()) {
                                try {
                                    String tipId = entry.getKey();
                                    Map<String, Object> tipData = (Map<String, Object>) entry.getValue();

                                    String title = (String) tipData.get("title");
                                    String description = (String) tipData.get("description");
                                    String icon = (String) tipData.get("icon");
                                    String iconColor = (String) tipData.get("iconColor");

                                    Tip tip = new Tip(tipId, title, description, icon, iconColor);
                                    tipsList.add(tip);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing tip: " + e.getMessage(), e);
                                }
                            }

                            listener.onTipsLoaded(tipsList);
                        } else {
                            // If no tips in tips field, create default tips
                            createDefaultTips(documentSnapshot, listener);
                        }
                    } else {
                        // If no tips field, create default tips
                        createDefaultTips(documentSnapshot, listener);
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error loading tips: " + e.getMessage()));
    }

    private void createDefaultTips(DocumentSnapshot plantDocument, TipsLoadedListener listener) {
        List<Tip> defaultTips = new ArrayList<>();

        String waterSchedule = plantDocument.getString("waterSchedule");
        if (waterSchedule != null && !waterSchedule.isEmpty()) {
            defaultTips.add(new Tip(
                    "water_" + plantDocument.getId(),
                    "Penyiraman",
                    waterSchedule,
                    "ic_water_drop",
                    "#4285F4"
            ));
        }

        String sunlightNeed = plantDocument.getString("sunlightNeed");
        if (sunlightNeed != null && !sunlightNeed.isEmpty()) {
            defaultTips.add(new Tip(
                    "sun_" + plantDocument.getId(),
                    "Cahaya",
                    sunlightNeed,
                    "ic_sun",
                    "#FBBC05"
            ));
        }

        listener.onTipsLoaded(defaultTips);
    }

    // General Tips
    public void getAllGeneralTips(TipsLoadedListener listener) {
        db.collection(GENERAL_TIPS_COLLECTION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Tip> tipsList = new ArrayList<>();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot tipSnapshot : queryDocumentSnapshots) {
                            try {
                                String tipId = tipSnapshot.getId();
                                String title = tipSnapshot.getString("title");
                                String description = tipSnapshot.getString("description");
                                String icon = tipSnapshot.getString("icon");
                                String iconColor = tipSnapshot.getString("iconColor");

                                Tip tip = new Tip(tipId, title, description, icon, iconColor);
                                tipsList.add(tip);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing general tip: " + e.getMessage(), e);
                            }
                        }
                        listener.onTipsLoaded(tipsList);
                    } else {
                        // Create default general tips if none exist
                        createDefaultGeneralTips(listener);
                    }
                })
                .addOnFailureListener(e -> listener.onError("Failed to load general tips: " + e.getMessage()));
    }

    private void createDefaultGeneralTips(TipsLoadedListener listener) {
        List<Tip> defaultGeneralTips = new ArrayList<>();

        Tip wateringTip = new Tip();
        wateringTip.setId("tip1");
        wateringTip.setTitle("Penyiraman");
        wateringTip.setDescription("Siram tanaman secara teratur sesuai kebutuhan jenisnya");
        wateringTip.setIcon("ic_watering");
        wateringTip.setIconColor("blue_primary");

        Tip lightTip = new Tip();
        lightTip.setId("tip2");
        lightTip.setTitle("Cahaya");
        lightTip.setDescription("Tempatkan tanaman di area dengan cahaya yang sesuai");
        lightTip.setIcon("ic_light");
        lightTip.setIconColor("yellow_primary");

        defaultGeneralTips.add(wateringTip);
        defaultGeneralTips.add(lightTip);

        listener.onTipsLoaded(defaultGeneralTips);
    }

    // Plant saving methods
    public void savePlant(Plant plant, SaveCallback callback) {
        if (plant == null || plant.getId() == null) {
            callback.onFailure(new Exception("Invalid plant data"));
            return;
        }

        String collectionPath = "admin".equals(plant.getSource()) ?
                ADMIN_PLANTS_COLLECTION : USER_PLANTS_COLLECTION;

        Map<String, Object> plantData = new HashMap<>();

        // Add all plant properties
        if (plant.getName() != null) plantData.put("name", plant.getName());
        if (plant.getImageUrl() != null) plantData.put("imageUrl", plant.getImageUrl());
        if (plant.getCategory() != null) plantData.put("category", plant.getCategory());
        if (plant.getDescription() != null) plantData.put("description", plant.getDescription());
        if (plant.getSunlightNeed() != null) plantData.put("sunlightNeed", plant.getSunlightNeed());
        if (plant.getWaterSchedule() != null) plantData.put("waterSchedule", plant.getWaterSchedule());

        // Add tips if available
        if (plant.getTips() != null && !plant.getTips().isEmpty()) {
            plantData.put("tips", plant.getTips());
        }

        // Save to Firestore
        db.collection(collectionPath).document(plant.getId())
                .set(plantData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Plant document saved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving plant document", e);
                    callback.onFailure(e);
                });
    }

    // Save a tip to a plant
    public void saveTipToPlant(String plantId, Tip tip, SaveCallback callback) {
        if (plantId == null || tip == null || tip.getId() == null) {
            callback.onFailure(new Exception("Invalid tip data"));
            return;
        }

        Map<String, Object> tipData = new HashMap<>();
        tipData.put("title", tip.getTitle());
        tipData.put("description", tip.getDescription());
        tipData.put("icon", tip.getIcon());
        tipData.put("iconColor", tip.getIconColor());

        // First check which collection this plant belongs to
        db.collection(ADMIN_PLANTS_COLLECTION).document(plantId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String collectionPath = documentSnapshot.exists() ?
                            ADMIN_PLANTS_COLLECTION : USER_PLANTS_COLLECTION;

                    // Update the tips field using dot notation for Firestore
                    db.collection(collectionPath).document(plantId)
                            .update("tips." + tip.getId(), tipData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Tip saved successfully to plant");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error saving tip to plant", e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    /**
     * Save user plant data to Firestore
     *
     * @param plantId Unique ID for the plant
     * @param plantData Map containing plant data
     * @param callback Callback to handle success/failure
     */
    public void saveUserPlantData(String plantId, Map<String, Object> plantData, SaveCallback callback) {
        Log.d(TAG, "Saving user plant data with ID: " + plantId);

        db.collection(USER_PLANTS_COLLECTION)
                .document(plantId)
                .set(plantData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User plant document saved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user plant document", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Save admin plant data to Firestore
     *
     * @param plantId Unique ID for the plant
     * @param plantData Map containing plant data
     * @param callback Callback to handle success/failure
     */
    public void saveAdminPlantData(String plantId, Map<String, Object> plantData, SaveCallback callback) {
        Log.d(TAG, "Saving admin plant data with ID: " + plantId);

        db.collection(ADMIN_PLANTS_COLLECTION)
                .document(plantId)
                .set(plantData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Admin plant document saved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving admin plant document", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Gets plants with care instructions (water schedule and sunlight needs)
     * This method retrieves plants that have care instruction data populated
     *
     * @param listener Callback to handle the loaded plants
     */
    public void getPlantsWithCareInstructions(PlantsLoadedListener listener) {
        Log.d(TAG, "Loading plants with care instructions...");
        List<Plant> plantsWithCare = new ArrayList<>();

        db.collection(ADMIN_PLANTS_COLLECTION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        try {
                            String plantId = documentSnapshot.getId();
                            // Only include plants that have either water schedule or sunlight need defined
                            if (documentSnapshot.contains("waterSchedule") || documentSnapshot.contains("sunlightNeed")) {
                                Plant plant = new Plant();
                                plant.setId(plantId);
                                plant.setSource("admin");

                                // Extract core plant data
                                if (documentSnapshot.contains("name")) {
                                    plant.setName(documentSnapshot.getString("name"));
                                } else {
                                    plant.setName(plantId);
                                }

                                if (documentSnapshot.contains("imageUrl")) {
                                    plant.setImageUrl(documentSnapshot.getString("imageUrl"));
                                }

                                if (documentSnapshot.contains("category")) {
                                    plant.setCategory(documentSnapshot.getString("category"));
                                }

                                if (documentSnapshot.contains("description")) {
                                    plant.setDescription(documentSnapshot.getString("description"));
                                }

                                if (documentSnapshot.contains("sunlightNeed")) {
                                    plant.setSunlightNeed(documentSnapshot.getString("sunlightNeed"));
                                }

                                if (documentSnapshot.contains("waterSchedule")) {
                                    plant.setWaterSchedule(documentSnapshot.getString("waterSchedule"));
                                }

                                // If plant has care instructions, add it to the list
                                if (plant.getWaterSchedule() != null || plant.getSunlightNeed() != null) {
                                    plantsWithCare.add(plant);
                                    Log.d(TAG, "Added plant with care instructions: " + plant.getName());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing plant data: " + e.getMessage(), e);
                        }
                    }

                    // Also check user plants if needed
                    checkUserPlantsForCareInstructions(plantsWithCare, listener);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Database error: " + e.getMessage());
                    listener.onError("Failed to load plants with care instructions: " + e.getMessage());
                });
    }

    /**
     * Helper method to check user plants for care instructions
     *
     * @param existingPlants List to add plants to
     * @param listener Callback for final result
     */
    private void checkUserPlantsForCareInstructions(List<Plant> existingPlants, PlantsLoadedListener listener) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            // Return only admin plants if user is not logged in
            if (existingPlants.isEmpty()) {
                listener.onError("No plants with care instructions found");
            } else {
                listener.onPlantsLoaded(existingPlants);
            }
            return;
        }

        db.collection(USER_PLANTS_COLLECTION)
                .whereEqualTo("userId", firebaseUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        try {
                            String plantId = documentSnapshot.getId();
                            // Only include plants that have either water schedule or sunlight need defined
                            if (documentSnapshot.contains("waterSchedule") || documentSnapshot.contains("sunlightNeed")) {
                                Plant plant = new Plant();
                                plant.setId(plantId);
                                plant.setSource("user");

                                // Extract core plant data
                                if (documentSnapshot.contains("name")) {
                                    plant.setName(documentSnapshot.getString("name"));
                                } else {
                                    plant.setName(plantId);
                                }

                                if (documentSnapshot.contains("imageUrl")) {
                                    plant.setImageUrl(documentSnapshot.getString("imageUrl"));
                                }

                                if (documentSnapshot.contains("category")) {
                                    plant.setCategory(documentSnapshot.getString("category"));
                                }

                                if (documentSnapshot.contains("description")) {
                                    plant.setDescription(documentSnapshot.getString("description"));
                                }

                                if (documentSnapshot.contains("sunlightNeed")) {
                                    plant.setSunlightNeed(documentSnapshot.getString("sunlightNeed"));
                                }

                                if (documentSnapshot.contains("waterSchedule")) {
                                    plant.setWaterSchedule(documentSnapshot.getString("waterSchedule"));
                                }

                                // If plant has care instructions, add it to the list
                                if (plant.getWaterSchedule() != null || plant.getSunlightNeed() != null) {
                                    existingPlants.add(plant);
                                    Log.d(TAG, "Added user plant with care instructions: " + plant.getName());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user plant data: " + e.getMessage(), e);
                        }
                    }

                    // Return combined results
                    if (existingPlants.isEmpty()) {
                        listener.onError("No plants with care instructions found");
                    } else {
                        listener.onPlantsLoaded(existingPlants);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Database error loading user plants: " + e.getMessage());
                    // Still return admin plants even if user plants fail to load
                    if (existingPlants.isEmpty()) {
                        listener.onError("No plants with care instructions found");
                    } else {
                        listener.onPlantsLoaded(existingPlants);
                    }
                });
    }
}