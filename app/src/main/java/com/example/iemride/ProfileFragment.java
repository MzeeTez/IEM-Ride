package com.example.iemride;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.iemride.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User currentUserProfile;

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadUserProfile();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadUserProfile();
        binding.editProfileButton.setOnClickListener(v -> openEditProfile());
        binding.logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // If document exists, load the data
                                currentUserProfile = document.toObject(User.class);
                                updateUI(currentUserProfile);
                            } else {
                                // If document does NOT exist, create a temporary empty profile
                                currentUserProfile = new User(); // Empty user object
                                updateUI(currentUserProfile); // Update UI with default values
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateUI(User user) {
        if (user.getName() != null && !user.getName().isEmpty()) {
            binding.nameTextView.setText(user.getName());
        } else {
            binding.nameTextView.setText("User");
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            binding.phoneTextView.setText(user.getPhoneNumber());
        } else {
            binding.phoneTextView.setText("No Number entered");
        }
        if (user.getJoinedDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
            binding.joinedDateTextView.setText("Joined " + sdf.format(user.getJoinedDate()));
        } else {
            // Can't show a join date if the profile was never saved
            binding.joinedDateTextView.setText("Not yet saved");
        }
        binding.profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
    }

    private void openEditProfile() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        if (currentUserProfile != null) {
            intent.putExtra("CURRENT_NAME", currentUserProfile.getName());
            intent.putExtra("CURRENT_PHONE", currentUserProfile.getPhoneNumber());
            intent.putExtra("CURRENT_VEHICLE_NUMBER", currentUserProfile.getVehicleNumber());
            intent.putExtra("CURRENT_VEHICLE_MODEL", currentUserProfile.getVehicleModel());
        }
        editProfileLauncher.launch(intent);
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}