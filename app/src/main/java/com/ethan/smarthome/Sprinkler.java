package com.ethan.smarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.model.ChannelFeed;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sprinkler extends AppCompatActivity implements View.OnClickListener, MyDialogFragment.NumberPick{
    private int sprinkChan = 1453835;
    private String sprinkAPI = "G57IMR4WLK4THDZG";
    private ScheduledExecutorService scheduleTaskExecutor;
    private int i, change = 0;
    private boolean livVal = false, bedVal = false;
    private String urlStr;
    private int ldelay, bdelay;

    database db = new database();
    MyDialogFragment df = new MyDialogFragment();
    ThingSpeakChannel sChannel = new ThingSpeakChannel(sprinkChan, sprinkAPI);

    public void dataUpdate(){
        db.init(this, "data");
        String ans = db.readFile();
        try {
            JSONObject jb = new JSONObject(ans);
            String gender = jb.get("Gender").toString();

            ImageView avatar = findViewById(R.id.avatar);
            if(gender.equals("Male")){
                avatar.setImageResource(R.drawable.man);
            }
            else if(gender.equals("Female")){
                avatar.setImageResource(R.drawable.woman);
            }
            else{
                avatar.setImageResource(R.drawable.profile);
            }
            TextView city = findViewById(R.id.city);
            city.setText(jb.get("Location").toString());
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            String name = "Human";
            ((ImageView)findViewById(R.id.avatar)).setImageResource(R.drawable.profile);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        dataUpdate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sprinkler);
        dataUpdate();

        scheduleTaskExecutor= Executors.newScheduledThreadPool(2);


        ImageView avatar = findViewById(R.id.avatar);
        ImageView navSet = findViewById(R.id.navSet);
        ImageView navPro = findViewById(R.id.navPro);
        ImageView navHome = findViewById(R.id.navHome);
        CardView bedCard = findViewById(R.id.bedCard);
        CardView livCard = findViewById(R.id.livCard);
        CardView emStop = findViewById(R.id.emCard);
        CardView upCard = findViewById(R.id.upCard);
        TextView livStatus = findViewById(R.id.livStat);
        TextView bedStatus = findViewById(R.id.bedStat);
        TextView livDelay = findViewById(R.id.livDel);
        TextView bedDelay = findViewById(R.id.bedDel);

        bedCard.setOnClickListener(this);
        livCard.setOnClickListener(this);
        upCard.setOnClickListener(this);
        emStop.setOnClickListener(this);
        avatar.setOnClickListener(this);
        navSet.setOnClickListener(this);
        navPro.setOnClickListener(this);
        navHome.setOnClickListener(this);


        Runnable statUpdate = new Runnable() {
            @Override
            public void run() {
                if(change!=1){
                    System.out.println("Updated");
                    System.out.println(change);
                    sChannel.loadChannelFeed();
                }
            }
        };

        try {
            sChannel.setChannelFeedUpdateListener(new ThingSpeakChannel.ChannelFeedUpdateListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onChannelFeedUpdated(long channelId, String channelName, ChannelFeed channelFeed) {
                    int i = Math.min((int) (channelFeed.getChannel().getLastEntryId()) - 1, 99);
                    if(i>=1) {
                        try {
                            int livStat = Integer.parseInt(channelFeed.getFeeds().get(i).getField2());
                            int livDel = Integer.parseInt(channelFeed.getFeeds().get(i).getField4());
                            System.out.println(livStat);
                            if (livStat == 1) {
                                livStatus.setText("Active, Duration - " + (livDel / 60000) + "mins");
                                livDelay.setText((livDel / 60000) + "mins");
                            } else {
                                livStatus.setText("Inactive");
                                livDelay.setText("");
                            }
                        }catch(NumberFormatException e) {
                            livStatus.setText("Inactive");
                            livDelay.setText("");
                        }
                        try{
                            int bedStat = Integer.parseInt(channelFeed.getFeeds().get(i).getField3());
                            int bedDel = Integer.parseInt(channelFeed.getFeeds().get(i).getField5());
                            System.out.println(bedStat);
                            if (bedStat == 1) {
                                bedStatus.setText("Active, Duration - " + (bedDel / 60000) + "mins");
                                bedDelay.setText((bedDel / 60000) + "mins");
                            } else {
                                bedStatus.setText("Inactive");
                                bedDelay.setText("");
                            }
                        } catch (NumberFormatException e){
                            bedStatus.setText("Inactive");
                            bedDelay.setText("");
                        }
                    }
                }
            });
        } catch(NullPointerException ignored){}

        scheduleTaskExecutor.scheduleAtFixedRate(statUpdate,0,2, TimeUnit.SECONDS);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Intent setIntent = new Intent(this, Settings.class);
        Intent proIntent = new Intent(this, Profile.class);
        Intent homeIntent = new Intent(this, MainActivity.class);

        switch (v.getId()){
            case R.id.avatar:
            case R.id.navPro:
                startActivity(proIntent);
                break;
            case R.id.navHome:
                startActivity(homeIntent);
                break;
            case R.id.navSet:
                startActivity(setIntent);
                break;
            case R.id.bedDel:
            case R.id.bedCard:
                change = 1;
                i=0;
                getDelay();
                break;
            case R.id.livDel:
            case R.id.livCard:
                change = 1;
                i=1;
                getDelay();
                break;
            case R.id.emCard:
                ((TextView)findViewById(R.id.livStat)).setText("Stopping...");
                ((TextView)findViewById(R.id.bedStat)).setText("Stopping...");
                change = 1;
                update(getURL(false, false, false));
                break;
            case R.id.upCard:
                if(livVal||bedVal){
                    ((TextView)findViewById(R.id.livStat)).setText("Updating...");
                    ((TextView)findViewById(R.id.bedStat)).setText("Updating...");
                    update(getURL(livVal, bedVal, true));
                }
                else{
                    Toast.makeText(this, "Set Duration", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public void update(String str) {
        Runnable up = new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    URL url = new URL(str);

                    BufferedReader in;
                    in = new BufferedReader(
                            new InputStreamReader(
                                    url.openStream()));

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        sb.append(inputLine);
                    }
                    in.close();
                    System.out.println(sb);
                    System.out.println(".");
                    if(sb.toString().equals("0")){
                        update(str);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    change = 0;
                }
            }
        };

        scheduleTaskExecutor.execute(up);
    }

    private void getDelay() {
        MyDialogFragment myDialogFragment = new MyDialogFragment();
        myDialogFragment.show(getSupportFragmentManager(), "MyFragment");
    }

    private String getURL(boolean a, boolean b, boolean c){
        String str="https://api.thingspeak.com/update?api_key=KQTELJ7WCK1CJTYN&";
        int f1, f2, f3, f4, f5;
        if(c){
            f1=0;
            if(a){
                f2=1;
                f4=ldelay;
            }
            else{
                f2=0;
                f4=0;
            }
            if(b) {
                f3=1;
                f5=bdelay;
            }
            else{
                f3=0;
                f5=0;
            }
            str = str+"field1="+f1+"&field2="+f2+"&field3="+f3+"&field4="+f4+"&field5="+f5;

        }
        else{
            str = "https://api.thingspeak.com/update?api_key=KQTELJ7WCK1CJTYN&field1=1&field2=0&field3=0&field4=0&field5=0";
        }
        return str;

    }

    @Override
    public void onFinishNumberPick(String inputText) {
        TextView livDelay = findViewById(R.id.livDel);
        TextView bedDelay = findViewById(R.id.bedDel);
        if(!(inputText.equals("0"))) {
            if (i == 0) {
                bedDelay.setText(inputText + "mins");
                bdelay = Integer.parseInt(inputText) * 60 * 1000;
                bedVal = true;
            } else if (i == 1) {
                livDelay.setText(inputText + "mins");
                ldelay = Integer.parseInt(inputText) * 60 * 1000;
                livVal = true;
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            if (i == 0) {
                bedDelay.setText("");
                bdelay = 0;
                bedVal = false;
            } else if (i == 1) {
                livDelay.setText("");
                ldelay =0;
                livVal = false;
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}