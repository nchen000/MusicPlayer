package com.nan.musicplayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{

    //Button
    Button playBtn;
    Button nextBtn;
    Button preBtn;
    Button repeatBtn;

    //Bar
    SeekBar positionBar;
    SeekBar volumeBar;

    //TextView
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    TextView currentSongLabel;

    MediaPlayer mp;
    int totalTime;
    int songId[] = {R.raw.kai_engel_irsens_tale,
            R.raw.kai_engel_nothing_lasts_forever, R.raw.kai_engel_changing_reality};

    //keep track of current playing song
    int currentId = 0;
    Uri currentUri = Uri.parse("android.resource://com.nan.musicplayer/" + songId[currentId]);

    String title = "";
    String artist = "";

    //object that retrieve current song's information
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button
        nextBtn = (Button) findViewById(R.id.nextBtn);
        preBtn = (Button) findViewById(R.id.preBtn);
        playBtn = (Button) findViewById(R.id.playBtn);
        repeatBtn = (Button) findViewById(R.id.repeatBtn);

        //textView
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);
        currentSongLabel = (TextView) findViewById(R.id.currentSongLabel);

        //get current song's title and artist
        getSongDetail();

        //display title and artist on the app
        setSongLabel();

        //display title and artist on notification bar
        showNotification(title, artist);

        // Media Player
        mp = MediaPlayer.create(this, songId[currentId]);
        mp.setLooping(false);
        mp.seekTo(0);
        mp.setVolume(0.0f, 1f);
        totalTime = mp.getDuration();


        // Position Bar
        positionBar = (SeekBar) findViewById(R.id.positionBar);
        setPositionBar();

        // Volume Bar
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        setVolumeBar();

        // Thread (Update positionBar & timeLabel)
        createThread();

    }//end of onCreate

    //-------------Helper function begins here----------

    public void setPositionBar(){
        positionBar.setMax(totalTime);
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mp.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

    }


    public void setVolumeBar(){
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        mp.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

    private void createThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            positionBar.setProgress(currentPosition);

            // Update Labels.
            String elapsedTime = createTimeLabel(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);

            String remainingTime = createTimeLabel(totalTime - currentPosition);
            remainingTimeLabel.setText("- " + remainingTime);
        }
    };

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }
    //-------------Helper functions end here----------

    //-------------Click functions for buttons begins here--------------
    public void playBtnClick(View view) {

        if (!mp.isPlaying()) {
            // Stopping
            mp.start();
            playBtn.setBackgroundResource(R.drawable.stop);
            getSongDetail();

        } else {
            // Playing
            getSongDetail();
            showNotification(title, artist);
            mp.pause();
            playBtn.setBackgroundResource(R.drawable.play);
        }

    }

    //next button
    public void nextBtnClick(View view) {
        mp.release();

        if(currentId == songId.length-1) currentId = 0;
        else ++currentId;

        mp = MediaPlayer.create(this, songId[currentId]);
        currentUri = Uri.parse("android.resource://com.nan.musicplayer/" + songId[currentId]);
        totalTime = mp.getDuration();

        //update all the UI
        getSongDetail();
        setSongLabel();
        showNotification(title, artist);
        setPositionBar();
        setVolumeBar();
        createThread();

        mp.start();
    }

    //previous button
    public void preBtnClick(View view) {
        mp.release();

        if(currentId == 0) currentId = songId.length -1;
        else --currentId;

        mp = MediaPlayer.create(this, songId[currentId]);
        currentUri = Uri.parse("android.resource://com.nan.musicplayer/" + songId[currentId]);
        totalTime = mp.getDuration();

        //update all the UI
        getSongDetail();
        setSongLabel();
        showNotification(title, artist);
        setPositionBar();
        setVolumeBar();
        createThread();

        mp.start();
    }

    //repeat button
    public void repeatBtnClick(View view) {

        if (!mp.isLooping()) {
            //looping
            repeatBtn.setBackgroundResource(R.drawable.repeaton);
            mp.setLooping(true);

        } else {
            //not looping
            repeatBtn.setBackgroundResource(R.drawable.repeatoff);
            mp.setLooping(false);

        }
    }
    //-------------Click functions for buttons ends here--------------

    void setSongLabel(){
        currentSongLabel.setText(title + " by " + artist);
    }

    void showNotification(String title, String content) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notification channel required for Android 8.0 and higher
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "music player",
                    NotificationManager.IMPORTANCE_DEFAULT);
            //turn off default notification sound
            channel.setSound(null, null);
            channel.setDescription("current playing music");
            mNotificationManager.createNotificationChannel(channel);
        }

        //notification bar
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.drawable.notification_icon) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(content);// message for notification

        //send out the notification
        mNotificationManager.notify(0, mBuilder.build());
    }

    //retrieve current song's title and artist
    void getSongDetail() {
        mmr.setDataSource(this, currentUri);
        title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
    }
}
