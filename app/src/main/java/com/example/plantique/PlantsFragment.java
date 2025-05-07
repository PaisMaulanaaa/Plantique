package com.example.plantique;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class PlantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlantAdapter plantAdapter;
    private FirebaseHelper firebaseHelper;
    private FloatingActionButton fabAddPlant;

    // Filter mode untuk memilih jenis tanaman yang ditampilkan
    private static final int FILTER_ALL = 0;
    private static final int FILTER_ADMIN = 1;
    private static final int FILTER_USER = 2;
    private int currentFilter = FILTER_ALL;

    private List<Plant> allPlants = new ArrayList<>();

    public PlantsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseHelper = new FirebaseHelper();

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_plants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter dengan list kosong
        plantAdapter = new PlantAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(plantAdapter);

        // Setup FAB for adding plants
        fabAddPlant = view.findViewById(R.id.fab_add_plant);
        fabAddPlant.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Intent intent = new Intent(getContext(), AddActivity.class);
                // Default mode adalah "user"
                intent.putExtra("mode", "user");
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup filter buttons
        setupFilterButtons(view);

        // Load plants
        loadAllPlants();
    }

    private void setupFilterButtons(View view) {
        View btnFilterAll = view.findViewById(R.id.btn_filter_all);
        View btnFilterAdmin = view.findViewById(R.id.btn_filter_admin);
        View btnFilterUser = view.findViewById(R.id.btn_filter_user);

        // Set initial selection
        updateFilterSelection(view);

        // Set click listeners
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = FILTER_ALL;
            updateFilterSelection(view);
            applyFilter();
        });

        btnFilterAdmin.setOnClickListener(v -> {
            currentFilter = FILTER_ADMIN;
            updateFilterSelection(view);
            applyFilter();
        });

        btnFilterUser.setOnClickListener(v -> {
            currentFilter = FILTER_USER;
            updateFilterSelection(view);
            applyFilter();
        });
    }

    private void updateFilterSelection(View view) {
        // Highlight the selected filter button
        View btnFilterAll = view.findViewById(R.id.btn_filter_all);
        View btnFilterAdmin = view.findViewById(R.id.btn_filter_admin);
        View btnFilterUser = view.findViewById(R.id.btn_filter_user);

        TextView txtFilterAll = view.findViewById(R.id.txt_filter_all);
        TextView txtFilterAdmin = view.findViewById(R.id.txt_filter_admin);
        TextView txtFilterUser = view.findViewById(R.id.txt_filter_user);

        // Reset all to unselected state
        btnFilterAll.setBackgroundResource(R.drawable.bg_filter_unselected);
        btnFilterAdmin.setBackgroundResource(R.drawable.bg_filter_unselected);
        btnFilterUser.setBackgroundResource(R.drawable.bg_filter_unselected);

        txtFilterAll.setTextColor(getResources().getColor(R.color.text_secondary));
        txtFilterAdmin.setTextColor(getResources().getColor(R.color.text_secondary));
        txtFilterUser.setTextColor(getResources().getColor(R.color.text_secondary));

        // Set selected state
        switch (currentFilter) {
            case FILTER_ALL:
                btnFilterAll.setBackgroundResource(R.drawable.bg_filter_selected);
                txtFilterAll.setTextColor(getResources().getColor(R.color.white));
                break;
            case FILTER_ADMIN:
                btnFilterAdmin.setBackgroundResource(R.drawable.bg_filter_selected);
                txtFilterAdmin.setTextColor(getResources().getColor(R.color.white));
                break;
            case FILTER_USER:
                btnFilterUser.setBackgroundResource(R.drawable.bg_filter_selected);
                txtFilterUser.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    private void loadAllPlants() {
        // Show loading state
        View progressBar = getView().findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        firebaseHelper.getAllPlants(new FirebaseHelper.PlantsLoadedListener() {
            @Override
            public void onPlantsLoaded(List<Plant> plants) {
                // Hide loading
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                // Save all plants
                allPlants = plants;

                // Apply current filter
                applyFilter();
            }

            @Override
            public void onError(String errorMessage) {
                // Hide loading
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                Toast.makeText(getContext(),
                        "Error loading plants: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        List<Plant> filteredPlants = new ArrayList<>();

        switch (currentFilter) {
            case FILTER_ALL:
                filteredPlants.addAll(allPlants);
                break;
            case FILTER_ADMIN:
                for (Plant plant : allPlants) {
                    if (plant.isAdminPlant()) {
                        filteredPlants.add(plant);
                    }
                }
                break;
            case FILTER_USER:
                for (Plant plant : allPlants) {
                    if (plant.isUserPlant()) {
                        filteredPlants.add(plant);
                    }
                }
                break;
        }

        plantAdapter.updateData(filteredPlants);

        // Show empty state if needed
        View emptyStateView = getView().findViewById(R.id.empty_state);
        if (emptyStateView != null) {
            if (filteredPlants.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyStateView.setVisibility(View.VISIBLE);

                // Set message based on filter
                TextView emptyStateMessage = getView().findViewById(R.id.txt_empty_state);
                if (emptyStateMessage != null) {
                    switch (currentFilter) {
                        case FILTER_ALL:
                            emptyStateMessage.setText("Belum ada tanaman tersedia");
                            break;
                        case FILTER_ADMIN:
                            emptyStateMessage.setText("Belum ada tanaman admin tersedia");
                            break;
                        case FILTER_USER:
                            emptyStateMessage.setText("Anda belum menambahkan tanaman pribadi");
                            break;
                    }
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment resumes
        loadAllPlants();
    }
}