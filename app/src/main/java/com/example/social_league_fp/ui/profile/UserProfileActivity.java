package com.example.social_league_fp.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.social_league_fp.R;
import com.example.social_league_fp.ui.auth.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;

    private ShapeableImageView ivProfilePhoto;
    private TextView tvDisplayName;
    private TextView tvEmail;
    private MaterialButton btnLogout;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navigateToLogin();
            return;
        }

        setContentView(R.layout.activity_user_profile);

        // Track Screen View
        Bundle bundle = new Bundle();
        bundle.putString("screen_name", "user_profile");
        mFirebaseAnalytics.logEvent("open_profile", bundle);

        initViews();
        setupToolbar();
        populateUserData(user);
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvEmail = findViewById(R.id.tvEmail);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> logout());

        tvDisplayName.setOnClickListener(v -> {
            throw new RuntimeException("Test Crash - Social League FP");
        });
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void populateUserData(FirebaseUser user) {
        tvDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Social League Player");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
        if (photoUrl != null) {
            new Thread(() -> {
                try {
                    URL url = new URL(photoUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    runOnUiThread(() -> ivProfilePhoto.setImageBitmap(myBitmap));
                } catch (Exception e) {
                    Log.e(TAG, "Error loading profile image", e);
                }
            }).start();
        }
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Track Event
        Bundle logoutBundle = new Bundle();
        logoutBundle.putString("status", "success");
        mFirebaseAnalytics.logEvent("logout", logoutBundle);

        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
