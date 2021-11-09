package com.ethan.smarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.ThingSpeakLineChart;
import com.macroyau.thingspeakandroid.model.ChannelFeed;
import com.scwang.wave.MultiWaveHeader;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener{
    private String greet;
    private int currentime, greetflag;
    private final String[] greetings = new String[]{"Enjoy your day!", "How's your day?", "Rise & Shine!",
                                                "You're a Smart Cookie", "You're strong!", "Believe."};
    private double temperature;
    private String humidity;
    private int livStat, bedStat;
    private String name;
    database db = new database();

    public void dataUpdate(){
        db.init(this, "data");
        String ans = db.readFile();
        try {
            JSONObject jb = new JSONObject(ans);
            name = jb.get("Name").toString();
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            name = "Kiddo";
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        dataUpdate();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Elements
        TextClock tclock = findViewById(R.id.timeclock);
        TextClock dclock = findViewById(R.id.dateclock);
        TextView greeting = findViewById(R.id.greeting);
        TextView tempVal = findViewById(R.id.tempVal);
        TextView humVal = findViewById(R.id.humVal);
        TextView sprinkStat = findViewById(R.id.sprVal);
        CardView tempCard = findViewById(R.id.tempCard);
        CardView humCard = findViewById(R.id.humCard);
        CardView sprCard = findViewById(R.id.sprCard);
        ImageView navSet = findViewById(R.id.navSet);
        ImageView navPro = findViewById(R.id.navPro);
        greetflag = 0;

        //Classes
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(2);
        @SuppressLint("SimpleDateFormat") DateFormat HF = new SimpleDateFormat("HH");
        @SuppressLint("SimpleDateFormat") DateFormat MF = new SimpleDateFormat("mm");
        @SuppressLint("SimpleDateFormat") DateFormat DF = new SimpleDateFormat("dd");
        DecimalFormat f = new DecimalFormat("##.00");
        Random generator = new Random();
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        int sprinkChan = 1453835;
        ThingSpeakChannel sChannel = new ThingSpeakChannel(sprinkChan);
        int weatherChan = 1454212;
        ThingSpeakChannel tChannel = new ThingSpeakChannel(weatherChan);


        //Wave
        MultiWaveHeader waveHeader = findViewById(R.id.wave_header);
        waveHeader.setVelocity(2f);
        boolean stat = waveHeader.isRunning();

        //Clock
        tclock.setFormat12Hour(null);
        tclock.setFormat24Hour("kk:mm");
        dclock.setFormat12Hour(null);
        dclock.setFormat24Hour("MMM dd, yyyy");

        //Database
        dataUpdate();

        //Animations
        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                greeting.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                greeting.setVisibility(View.INVISIBLE);
                if(greetflag==0){
                    greeting.setText(greet+", "+name+"!");
                    greeting.startAnimation(fadein);
                    greetflag = 1;
                }
                else{
                    greeting.setText(greetings[generator.nextInt(greetings.length)]);
                    greeting.startAnimation(fadein);
                    greetflag = 0;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //Runnables
        Runnable greetingUpdate = () -> {
            currentime = Integer.parseInt(HF.format(Calendar.getInstance().getTime()));
            if(currentime < 12 && currentime >= 5){
                greet = "Good Morning";
            }
            else if(currentime >= 12 && currentime < 16){
                greet = "Good Afternoon";
            }
            else if(currentime >= 16 && currentime < 22){
                greet = "Good Evening";
            }
            else {
                greet = "Good Night";
            }

            runOnUiThread(() -> {
                // update your UI component here.
                greeting.startAnimation(fadeout);
            });

        };

        Runnable statUpdate = () -> {
            tChannel.loadChannelFeed();
            sChannel.loadChannelFeed();
        };

        try {
            tChannel.setChannelFeedUpdateListener((channelId, channelName, channelFeed) -> {
                int i = Math.min((int) (channelFeed.getChannel().getLastEntryId()) - 1, 99);
                try{
                    temperature = Double.parseDouble(channelFeed.getFeeds().get(i).getField6());
                }
                catch(NullPointerException ignored){}
                try{
                    humidity = channelFeed.getFeeds().get(i).getField7();
                    humidity = humidity.substring(0, humidity.indexOf('.'));
                }
                catch (NullPointerException ignored){}
                tempVal.setText(f.format(temperature) + "°C");
                humVal.setText(humidity + "%");
            });
        } catch(NullPointerException ignored){}

        try {
            sChannel.setChannelFeedUpdateListener((channelId, channelName, channelFeed) -> {
                int i = Math.min((int) (channelFeed.getChannel().getLastEntryId()) - 1, 99);
                if(i>=1) {
                    try {
                        livStat = Integer.parseInt(channelFeed.getFeeds().get(i).getField2());
                        bedStat = Integer.parseInt(channelFeed.getFeeds().get(i).getField3());
                        if (livStat == 1 || bedStat == 1) {
                            sprinkStat.setText("On");
                        } else {
                            sprinkStat.setText("Off");
                        }
                    }catch (NumberFormatException ignored){}
                }
            });
        } catch(NullPointerException|NumberFormatException ignored){}

        

        scheduleTaskExecutor.scheduleAtFixedRate(greetingUpdate,0,15, TimeUnit.SECONDS);
        scheduleTaskExecutor.scheduleAtFixedRate(statUpdate,0,2, TimeUnit.SECONDS);

        tempCard.setOnClickListener(this);
        humCard.setOnClickListener(this);
        sprCard.setOnClickListener(this);
        navSet.setOnClickListener(this);
        navPro.setOnClickListener(this);

    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view)
    {
        Intent tempIntent = new Intent(this, Temperature.class);
        Intent profIntent = new Intent(this,Profile.class);
        Intent humIntent = new Intent(this, Humidity.class);
        Intent sprIntent = new Intent(this, Sprinkler.class);
        Intent setIntent = new Intent(this, Settings.class);
        switch (view.getId()) {
            case R.id.tempCard:
                startActivity(tempIntent);
                break;
            case R.id.humCard:
                startActivity(humIntent);
                break;
            case R.id.sprCard:
                startActivity(sprIntent);
                break;
            case R.id.navSet:
                startActivity(setIntent);
                break;
            case R.id.navPro:
                startActivity(profIntent);
                break;
            default:
                break;
        }
    }


}