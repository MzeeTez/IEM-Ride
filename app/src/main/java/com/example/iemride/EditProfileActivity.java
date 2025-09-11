package com.example.iemride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.iemride.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // If the user is editing (not signing up), pre-fill the form
        binding.nameEditText.setText(getIntent().getStringExtra("CURRENT_NAME"));
        binding.phoneEditText.setText(getIntent().getStringExtra("CURRENT_PHONE"));
        binding.vehicleNumberEditText.setText(getIntent().getStringExtra("CURRENT_VEHICLE_NUMBER"));
        binding.vehicleModelEditText.setText(getIntent().getStringExtra("CURRENT_VEHICLE_MODEL"));

        binding.backButton.setOnClickListener(v -> handleBackButton());
        binding.saveButton.setOnClickListener(v -> saveProfileChanges());
    }

    // Handle the back button press. If the user is coming from the profile page, just finish.
    // If they are coming from signup, you might want to prevent them from going back,
    // but for now, we will let it close.
    private void handleBackButton() {
        finish();
    }

    private void saveProfileChanges() {
        String name = binding.nameEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String vehicleNumber = binding.vehicleNumberEditText.getText().toString().trim();
        String vehicleModel = binding.vehicleModelEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        User userProfile = new User(name, phone, null, vehicleNumber, vehicleModel);

        db.collection("users").document(userId)
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

                    // ==> CHANGE HERE: Decide where to go after saving <==
                    // If this activity was launched from the ProfileFragment, it will have this flag.
                    if (getIntent().hasExtra("CURRENT_NAME")) {
                        setResult(RESULT_OK);
                        finish(); // Just go back to the profile screen
                    } else {
                        // Otherwise, the user just signed up, so go to the Home screen
                        Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}