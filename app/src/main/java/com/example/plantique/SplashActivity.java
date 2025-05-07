package com.example.plantique; // Sesuaikan dengan package aplikasi Anda

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SplashActivity extends AppCompatActivity {

    private ConstraintLayout containerLogo;
    private ImageView ivLogo, ivLogoCircle;
    private TextView tvAppName, tvTagline;
    private ProgressBar progressBar;
    private final int SPLASH_DURATION = 3500; // 3.5 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Inisialisasi Views
        containerLogo = findViewById(R.id.container_logo);
        ivLogo = findViewById(R.id.iv_logo);
        ivLogoCircle = findViewById(R.id.iv_logo_circle);
        tvAppName = findViewById(R.id.tv_app_name);
        tvTagline = findViewById(R.id.tv_tagline);
        progressBar = findViewById(R.id.progressBar);

        // Memulai animasi secara berurutan
        startAnimation();

        // Handler untuk berpindah ke activity berikutnya setelah animasi selesai
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

                // Transisi animasi ketika berpindah activity
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DURATION);
    }

    private void startAnimation() {
        // Menyiapkan animasi logo
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(containerLogo, "scaleX", 0.0f, 1.0f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(containerLogo, "scaleY", 0.0f, 1.0f);
        ObjectAnimator rotateCircle = ObjectAnimator.ofFloat(ivLogoCircle, "rotation", 0f, 360f);

        // Animasi untuk teks dan progress bar
        ObjectAnimator fadeInAppName = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
        ObjectAnimator fadeInTagline = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f);
        ObjectAnimator fadeInProgress = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f);

        // Set durasi dan interpolator untuk animasi yang halus
        scaleDownX.setDuration(1000);
        scaleDownY.setDuration(1000);
        rotateCircle.setDuration(1500);
        fadeInAppName.setDuration(800);
        fadeInTagline.setDuration(800);
        fadeInProgress.setDuration(600);

        scaleDownX.setInterpolator(new DecelerateInterpolator());
        scaleDownY.setInterpolator(new DecelerateInterpolator());
        rotateCircle.setInterpolator(new AccelerateDecelerateInterpolator());

        // Membuat AnimatorSet untuk logo scaling
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.play(scaleDownX).with(scaleDownY);

        // Membuat AnimatorSet untuk semua animasi
        AnimatorSet finalSet = new AnimatorSet();
        finalSet.play(scaleSet).with(rotateCircle);
        finalSet.play(fadeInAppName).after(scaleSet);
        finalSet.play(fadeInTagline).after(fadeInAppName);
        finalSet.play(fadeInProgress).after(fadeInTagline);

        finalSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                // Tambahkan animasi pulse pada logo setelah animasi utama selesai
                ObjectAnimator pulseX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 1.0f, 1.1f, 1.0f);
                ObjectAnimator pulseY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 1.0f, 1.1f, 1.0f);

                pulseX.setDuration(1000);
                pulseY.setDuration(1000);
                pulseX.setRepeatCount(1);
                pulseY.setRepeatCount(1);

                AnimatorSet pulseSet = new AnimatorSet();
                pulseSet.play(pulseX).with(pulseY);
                pulseSet.start();
            }
        });

        // Mulai animasi
        finalSet.start();
    }
}