package com.ethan.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class SplashScreen extends AppCompatActivity {
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView logo = findViewById(R.id.mylogo);
        ProgressBar spinner;

        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.startup);
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
        Intent intent = new Intent(this,MainActivity.class);

        mp.setVolume(150,150);
        logo.setVisibility(View.VISIBLE);
        logo.startAnimation(myFadeInAnimation);
        mp.start();

        myFadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logo.setVisibility(View.INVISIBLE);
                spinner.setVisibility(View.INVISIBLE);
                mp.stop();
                mp.reset();
                mp.release();
                startActivity(intent);
                finish();
            }
        }, 7000);
    }
}
