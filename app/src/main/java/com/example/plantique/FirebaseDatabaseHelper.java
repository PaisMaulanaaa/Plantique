package com.example.plantique;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseHelper {
    private static final String TAG = "FirebaseDatabaseHelper";
    private DatabaseReference mDatabase;
    private static FirebaseDatabaseHelper instance;

    public interface DataStatus {
        void DataIsLoaded(List<Plant> plants);
        void DataIsInserted();
        void DataIsUpdated();
        void DataIsDeleted();
    }

    private FirebaseDatabaseHelper() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static FirebaseDatabaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseHelper();
        }
        return instance;
    }

    public void readPlants(final DataStatus dataStatus) {
        mDatabase.child("plants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Plant> plantList = new ArrayList<>();
                for (DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                    Plant plant = plantSnapshot.getValue(Plant.class);
                    if (plant != null) {
                        plant.setId(plantSnapshot.getKey());
                        plantList.add(plant);
                    }
                }
                dataStatus.DataIsLoaded(plantList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading plants: " + databaseError.getMessage());
            }
        });
    }

    public void getPlantById(String plantId, final DataStatus dataStatus) {
        mDatabase.child("plants").child(plantId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Plant plant = dataSnapshot.getValue(Plant.class);
                if (plant != null) {
                    plant.setId(dataSnapshot.getKey());
                    List<Plant> singlePlantList = new ArrayList<>();
                    singlePlantList.add(plant);
                    dataStatus.DataIsLoaded(singlePlantList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading plant: " + databaseError.getMessage());
            }
        });
    }

    // Methods for adding, updating and deleting plants can be added here
    public void addPlant(Plant plant, final DataStatus dataStatus) {
        String key = mDatabase.child("plants").push().getKey();
        if (key != null) {
            mDatabase.child("plants").child(key).setValue(plant)
                    .addOnSuccessListener(aVoid -> dataStatus.DataIsInserted())
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding plant", e));
        }
    }

    public void updatePlant(String key, Plant plant, final DataStatus dataStatus) {
        mDatabase.child("plants").child(key).setValue(plant)
                .addOnSuccessListener(aVoid -> dataStatus.DataIsUpdated())
                .addOnFailureListener(e -> Log.e(TAG, "Error updating plant", e));
    }

    public void deletePlant(String key, final DataStatus dataStatus) {
        mDatabase.child("plants").child(key).removeValue()
                .addOnSuccessListener(aVoid -> dataStatus.DataIsDeleted())
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting plant", e));
    }
}