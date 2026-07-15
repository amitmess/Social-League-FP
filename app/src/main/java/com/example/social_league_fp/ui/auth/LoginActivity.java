package com.example.social_league_fp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.social_league_fp.R;
import com.example.social_league_fp.ui.matches.MatchesListActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Handles the Google Sign-In authentication flow, establishing a secure Firebase 
 * user session and redirecting authenticated users to the main matches portal.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private FirebaseAnalytics mFirebaseAnalytics;

    private ProgressBar progressBar;
    private MaterialButton btnGoogleSignIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Session Persistence: Skip the login screen if a valid Firebase session already exists.
        if (mAuth.getCurrentUser() != null) {
            navigateToMatches();
            return;
        }

        progressBar = findViewById(R.id.progressBar);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // Configure Google Sign-In options requesting tokens to enable exchange for Firebase Auth.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize ActivityResultLauncher to handle Google Sign-In intent outcomes.
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Extract the Google account and initiate token exchange with Firebase.
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign in failed", e);
                            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                            showLoading(false);
                            logLoginFailure("Google API exception: " + e.getMessage());
                        }
                    } else {
                        // User cancelled the login flow or network request failed.
                        Toast.makeText(this, "Sign-In cancelled", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                        logLoginFailure("User cancelled sign-in");
                    }
                }
        );

        btnGoogleSignIn.setOnClickListener(v -> signIn());
    }

    // Launch the Google Sign-In client selection screen.
    private void signIn() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    // Exchange the authenticated Google ID token for Firebase Auth credentials.
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Welcome " + (user != null ? user.getDisplayName() : ""), Toast.LENGTH_SHORT).show();
                        logLoginSuccess();
                        navigateToMatches();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                        logLoginFailure(task.getException() != null ? task.getException().getMessage() : "Unknown");
                    }
                });
    }

    // Redirect to the matches list, resetting the task backstack so back operations do not return here.
    private void navigateToMatches() {
        Intent intent = new Intent(LoginActivity.this, MatchesListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Toggle ProgressBar visibility to indicate active network operations.
    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGoogleSignIn.setEnabled(!loading);
    }

    // Log login success events to Firebase Analytics for usage metrics.
    private void logLoginSuccess() {
        Bundle bundle = new Bundle();
        bundle.putString("method", "google");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
        
        Bundle customBundle = new Bundle();
        customBundle.putString("status", "success");
        mFirebaseAnalytics.logEvent("login_success", customBundle);
    }

    // Record login failures to Firebase Analytics for monitoring authentication issues.
    private void logLoginFailure(String reason) {
        Bundle customBundle = new Bundle();
        customBundle.putString("status", "failure");
        customBundle.putString("reason", reason);
        mFirebaseAnalytics.logEvent("login_failure", customBundle);
    }
}
