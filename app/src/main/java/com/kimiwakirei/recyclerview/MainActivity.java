package com.kimiwakirei.recyclerview;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class MainActivity extends AppCompatActivity implements Serializable, View.OnClickListener {
    private static final String FILE_NAME = "songs.txt";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private String userDisplayName, userDisplayEmail;
    private String title, artist, fileLink, coverArt, url, internalID;

    private int popularity, songLength, trackedPosition;



    private ImageButton btnPlayPause, btnPrev, btnForw;
    private SeekBar seekBar;
    private ProgressBar miniPlayerSeekbar;
    private TextView currentSongDuration, songDuration, profileName, tvHome, displayName, searchQuery,
            miniPlayerSongName, txtTitle, txtArtist;
    private BottomNavigationView bottomNavigationView;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Button btnUpdateProfileFromSettings, btnSearchForSong, btnLogout, miniPlayerBtn, clearAllSongsBtn, creditsButton;
    private ViewFlipper viewFlipper;
    private ImageView ivCoverArt, songInfoBtn, loopBtn, shuffleBtn, miniPlayerBtnPlayPause, backButton;
    private ConstraintLayout miniPlayerConstraint;
    private long backPressedTime;
    private boolean isShuffle = false;


    private RecyclerView mRecyclerView;
    private SongItemAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    VollyApi vollyApi = new VollyApi();

    private ArrayList<Song> songList = new ArrayList<>();
    private ArrayList<Song> shuffleList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setupUI();

        refreshUser();
        getCurrentPeriod();
        prepareBottomNavigation();

        firebaseAuth = FirebaseAuth.getInstance();

        if (loadSongs() != null && loadSongs().size() != 0){
            songList = loadSongs();
            buildRecyclerView();
        }

        showMiniPlayer(false);

        buildRecyclerView();


    }

    public void removeSong(int position){

        songList.remove(position);
        mAdapter.notifyItemRemoved(position);
        saveSongs();
    }

    public void addSong(String title, String artist, String fileLink, int songLength, String coverArt, boolean liked, String internalID, int popularity){
        Song newSong = new Song(title, artist,fileLink,songLength,coverArt,liked,internalID,popularity);
        songList.add(newSong);
        mAdapter.notifyItemChanged(songList.indexOf(newSong));
    }



    public void buildRecyclerView(){
        mRecyclerView = findViewById(R.id.songList);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new SongItemAdapter(songList, this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.onItemClickListener(new SongItemAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                sendDataToPlayer(position);
            }

            @Override
            public void onDeleteClick(int position) {
                removeSong(position);
            }

        });
    }

    public void sendDataToPlayer(int position) {

        Song song = songList.get(position);

        trackedPosition = position;

        title = song.getTitle();
        artist = song.getArtist();
        fileLink = song.getFileLink();
        coverArt = song.getCoverArt();

        internalID = song.getInternalID();

        popularity = song.getPopularity();
        songLength = song.getSongLength();

        displaySong(title,artist,coverArt);
        showMiniPlayer(true);
        disableButtons(trackedPosition);
        loopBtn.setImageResource(R.drawable.loop_icon);
        btnPlayPause.setImageResource(R.drawable.pause_button);
        prepareMedia();
    }

    private void showMiniPlayer(boolean showState) {
        if (showState){
            miniPlayerBtnPlayPause.setVisibility(View.VISIBLE);
            miniPlayerBtn.setVisibility(View.VISIBLE);
            miniPlayerSongName.setVisibility(View.VISIBLE);
            miniPlayerConstraint.setVisibility(View.VISIBLE);
            miniPlayerSeekbar.setVisibility(View.VISIBLE);
        }
        else {
            miniPlayerBtnPlayPause.setVisibility(View.INVISIBLE);
            miniPlayerBtn.setVisibility(View.INVISIBLE);
            miniPlayerSongName.setVisibility(View.INVISIBLE);
            miniPlayerConstraint.setVisibility(View.INVISIBLE);
            miniPlayerSeekbar.setVisibility(View.INVISIBLE);
        }

    }

    public void prepareBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        getCurrentPeriod();

                        saveSongs();
                        showMenu(0);
                        break;
                    case R.id.navigation_search:
                        showMenu(1);
                        break;

                    case R.id.navigation_settings:
                        prepareProfile();
                        showMenu(3);
                        break;
                }
                return true;
            }
        });
    }

    public void showMenu(int menuID){
        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
        viewFlipper.setDisplayedChild(menuID);
    }

    public void makeNewOnlineSong(String fileLink){
        vollyApi.getJsonInformation(fileLink, MainActivity.this);
    }

    public void saveSongs(){
        try {
            FileOutputStream fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(songList);
            os.close();


            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Song> loadSongs(){
        try {
            FileInputStream fis = openFileInput(FILE_NAME);

            ObjectInputStream is = new ObjectInputStream(fis);
            ArrayList<Song> loadedArray = (ArrayList<Song>) is.readObject();

//            /Toast.makeText(this,"ArrayOutput: " + loadedArray.size(), Toast.LENGTH_SHORT).show();

            is.close();
            fis.close();
            return loadedArray;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void prepareProfile(){

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                profileName.setText(userProfile.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                profileName.setText("Error getting data :(");
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Database: " ,"Message: " + databaseError.getMessage() + " Details: " + databaseError.getDetails());
            }
        });
    }

    private void refreshUser(){
        userDisplayEmail = "Retrieving Data...";
        userDisplayName = "Retrieving Data...";

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                userDisplayName = userProfile.getUserName();
                userDisplayEmail = userProfile.getUserEmail();

                displayName.setText(userProfile.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userDisplayName = "";
                userDisplayEmail = "";
            }
        });
    }

    private int getCurrentPeriod(){
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH");

        int localTime =  Integer.parseInt(dateFormat.format(currentTime));

        if (localTime >= 5 && localTime < 12){
            tvHome.setText("Good morning, ");
            //returns 0: morning:
            return 0;
        }
        else if (localTime >= 12 && localTime < 17){
            tvHome.setText("Afternoon, ");
            //returns 1: afternoon
            return 1;
        }
        else if(localTime >= 17 && localTime < 19){
            tvHome.setText("Good evening, ");
            //returns 2: evening
            return 2;
        }
        else {
            tvHome.setText("Good night, ");
            return 3;
        }
    }

    public void changeSearchStatus(boolean status){
        btnSearchForSong.setEnabled(status);
        btnSearchForSong.setText("Search Song");

        if (status){
            searchQuery.setText("");
        }
    }

    private void setupUI(){
        btnPlayPause = findViewById(R.id.playPauseBtn);
        btnPrev = findViewById(R.id.previousButton);
        btnForw = findViewById(R.id.nextButton);
        btnUpdateProfileFromSettings = findViewById(R.id.changeInfoBtn);
        btnSearchForSong = findViewById(R.id.searchBtn);
        btnLogout = findViewById(R.id.logoutButton);
        miniPlayerBtn = findViewById(R.id.miniPlayerBtn);
        miniPlayerBtnPlayPause = findViewById(R.id.miniPlayerPausePlay);
        miniPlayerSeekbar = findViewById(R.id.miniPlayerSeekbar);
        txtTitle = findViewById(R.id.txtSongName);
        txtArtist = findViewById(R.id.txtArtist);
        shuffleBtn = findViewById(R.id.shuffleBtn);
        clearAllSongsBtn = findViewById(R.id.clearAllSongsBtn);
        creditsButton = findViewById(R.id.creditsBtn);

        seekBar = findViewById(R.id.seekBar);
        currentSongDuration = findViewById(R.id.songStartDuration);
        songDuration = findViewById(R.id.songDuration);
        profileName = findViewById(R.id.loggedInUserName);

        tvHome = findViewById(R.id.tvHome);
        displayName = findViewById(R.id.displayName);
        searchQuery = findViewById(R.id.search_query);
        miniPlayerSongName = findViewById(R.id.miniPlayerSongName);
        backButton = findViewById(R.id.backBtn);

        ivCoverArt = findViewById(R.id.art);
        songInfoBtn = findViewById(R.id.songInfoBtn);
        loopBtn = findViewById(R.id.loopBtn);

        mRecyclerView = findViewById(R.id.songList);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewFlipper = findViewById(R.id.viewFlipper);
        miniPlayerConstraint = findViewById(R.id.miniPlayerConst);

        btnSearchForSong.setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);
        btnUpdateProfileFromSettings.setOnClickListener(this);
        btnForw.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        songInfoBtn.setOnClickListener(this);
        loopBtn.setOnClickListener(this);
        clearAllSongsBtn.setOnClickListener(this);
        shuffleBtn.setOnClickListener(this);
        creditsButton.setOnClickListener(this);

        miniPlayerBtnPlayPause.setOnClickListener(this);
        miniPlayerBtn.setOnClickListener(this);

        miniPlayerConstraint.setOnClickListener(this);
        backButton.setOnClickListener(this);

        txtTitle.setSelected(true);
        txtArtist.setSelected(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.playPauseBtn:
                playOrPause();
                break;

            case R.id.nextButton:
                playNext(trackedPosition);
                disableButtons(trackedPosition);
                break;

            case R.id.previousButton:
                playPrevious(trackedPosition);
                disableButtons(trackedPosition);
                break;

            case R.id.searchBtn:
                searchForSong();
                break;

            case R.id.changeInfoBtn:
                startActivity(new Intent(MainActivity.this, UpdateProfile.class));
                break;

            case R.id.logoutButton:
                logOut();
                break;
            case R.id.songInfoBtn:
                Toast.makeText(MainActivity.this, "Title: " + title + "\nArtist: " + artist + "\nInternal ID: " + internalID +
                        "\nPopularity: " + popularity + "\nActual Song Length: " + createTimerLabel(songLength), Toast.LENGTH_LONG).show();
                break;
            case R.id.loopBtn:
                Toast.makeText(MainActivity.this, "Looping", Toast.LENGTH_SHORT).show();

                if (!mediaPlayer.isLooping()){
                    //Enable Loop:
                    loopBtn.setImageResource(R.drawable.loop_on_icon);
                    mediaPlayer.setLooping(true);
                }
                else {
                    loopBtn.setImageResource(R.drawable.loop_icon);
                    mediaPlayer.setLooping(false);
                }

                break;

            case R.id.miniPlayerBtn:

                break;
            case R.id.miniPlayerPausePlay:
                playOrPause();

                break;

            case R.id.miniPlayerConst:
                showMiniPlayer(false);
                bottomNavigationView.setVisibility(View.INVISIBLE);
                showMenu(2);
                break;
            case R.id.backBtn:
                showMiniPlayer(true);

                bottomNavigationView.setVisibility(View.VISIBLE);
                showMenu(0);
                break;

            case R.id.shuffleBtn:
                if (isShuffle)
                {
                    isShuffle = false;
                    shuffleSongs(trackedPosition, isShuffle);
                    shuffleBtn.setImageResource(R.drawable.shuffle_icon_off);
                }else {

                    isShuffle = true;
                    shuffleSongs(trackedPosition, isShuffle);
                    shuffleBtn.setImageResource(R.drawable.shuffle_icon_on);
                }
                break;
            case R.id.clearAllSongsBtn:
                songList.clear();
                Toast.makeText(MainActivity.this, "Cleared songs!", Toast.LENGTH_SHORT).show();
                saveSongs();
                break;
            case R.id.creditsBtn:
                startActivity(new Intent(MainActivity.this, CreditsScreen.class));
                break;
        }
    }

    private void searchForSong(){
        String test = searchQuery.getText().toString();

        if (test.startsWith("https://open.spotify.com/track/")){
            makeNewOnlineSong(test.replaceAll("https://open.spotify.com/track/",""));
            btnSearchForSong.setText("Seaching for song...");
            btnSearchForSong.setEnabled(false);

        }
        else if (test.startsWith("spotify:track:")){
            makeNewOnlineSong(test.replaceAll("spotify:track:",""));
            btnSearchForSong.setText("Seaching for song...");
            btnSearchForSong.setEnabled(false);
        }
        else{
            Toast.makeText(this,"Invalid Link detected", Toast.LENGTH_SHORT).show();
        }
    }

    private void logOut(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(MainActivity.this, LoginPage.class));
    }


    //Imported From PlaySongActivity:
    public void playNext(int currentSongPosition){
        Song nextSong = null;

        if (isShuffle){
            if ((currentSongPosition + 1) < shuffleList.size()){
                nextSong = shuffleList.get(currentSongPosition + 1);
                disableButtons(shuffleList.indexOf(nextSong));

            }
        }else {
            if ((currentSongPosition + 1) < songList.size()){
                nextSong = songList.get(currentSongPosition + 1);
                disableButtons(songList.indexOf(nextSong));
            }
        }

        if (nextSong != null){
            title = nextSong.getTitle();
            artist = nextSong.getArtist();
            coverArt = nextSong.getCoverArt();
            fileLink = nextSong.getFileLink();


            internalID = nextSong.getInternalID();

            songLength = nextSong.getSongLength();
            popularity = nextSong.getPopularity();

            url = fileLink;
            Log.d("MyApp", "NextSongLink: " + url);

            displaySong(title,artist,coverArt);
            loopBtn.setImageResource(R.drawable.loop_icon);
            btnPlayPause.setImageResource(R.drawable.pause_button);
            prepareMedia();
            trackedPosition++;

        }
    }

    public void playPrevious(int currentSongPosition){
        Song previousSong = null;

        if (isShuffle){
            if (shuffleList.get(currentSongPosition - 1) != null && currentSongPosition >= 0){
                previousSong = shuffleList.get(currentSongPosition - 1);
                disableButtons(0);
            }
        }else {
            if (songList.get(currentSongPosition - 1) != null && currentSongPosition >= 0){
                previousSong = songList.get(currentSongPosition - 1);
                disableButtons(songList.indexOf(previousSong));
            }
        }

        int songPosition = mediaPlayer.getCurrentPosition();

        if (songPosition < 2500){
            if (previousSong != null){

                title = previousSong.getTitle();
                artist = previousSong.getArtist();
                coverArt = previousSong.getCoverArt();
                fileLink = previousSong.getFileLink();


                internalID = previousSong.getInternalID();

                songLength = previousSong.getSongLength();
                popularity = previousSong.getPopularity();

                url = fileLink;
                loopBtn.setImageResource(R.drawable.loop_icon);

                Log.d("MyApp", "NextSongLink: " + url);
                trackedPosition--;
                btnPlayPause.setImageResource(R.drawable.pause_button);
                loopBtn.setImageResource(R.drawable.loop_icon);

                displaySong(title,artist,coverArt);
                prepareMedia();
            }
        }else {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
            updateSeekBar();
        }

       // disableButtons(previousSong.getId());
    }

    private String createTimerLabel(int durationInMilisec){
        String timerLabel = "";

        int min = durationInMilisec / 60000;
        int sec = durationInMilisec / 1000 % 60;

        timerLabel += min + ":";

        if (sec < 10) timerLabel += "0";
        timerLabel += sec;
        return timerLabel;
    }

    private void updateSeekBar() {
        Runnable runnable;
        Handler handler;
        handler = new Handler();
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        miniPlayerSeekbar.setProgress(mediaPlayer.getCurrentPosition());

        String timerLabel = createTimerLabel(mediaPlayer.getCurrentPosition());
        Log.d("MyApp", "updateSeekBar: " + timerLabel);

        if (mediaPlayer.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            };

            handler.postDelayed(runnable,75);
        }

        currentSongDuration.setText(timerLabel);
    }

    //gets song data into all the components of the Layout file:
    private void displaySong(String title, String artist, String coverArt) {
        txtTitle.setText(title);

        txtArtist.setText(artist);

        //displaying album art with rounded corners
        String imageId = coverArt;
        Glide.with(this).asBitmap().apply(bitmapTransform(new RoundedCornersTransformation(128,0))).
                load(imageId).placeholder(R.drawable.spinner).into(ivCoverArt);

        miniPlayerSongName.setText(title + " ◦ " + artist);
        miniPlayerBtnPlayPause.setImageResource(R.drawable.pause_button);
    }

    //stops music
    private void stopActivities(){
        mediaPlayer.pause();
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    //disables forward and backward buttons depending on start/end of song:
    private void disableButtons(int contextId){
        Log.d("MyApp", "disableButtons: " + contextId + " - " + songList.size());

        if (contextId == 0){
            btnPrev.setEnabled(false);
            btnPrev.setImageResource(R.drawable.previous_button_greyed);

            btnForw.setEnabled(true);
            btnForw.setImageResource(R.drawable.next_button);
        }
        else if (contextId == songList.size() - 1){
            btnForw.setImageResource(R.drawable.next_button_greyed);
            btnForw.setEnabled(false);

            btnPrev.setEnabled(true);
            btnPrev.setImageResource(R.drawable.previous_button);
        }
        else {
            btnForw.setEnabled(true);
            btnPrev.setEnabled(true);
            btnForw.setImageResource(R.drawable.next_button);
            btnPrev.setImageResource(R.drawable.previous_button);
        }

    }

    private void prepareMedia() {

        miniPlayerSeekbar.setIndeterminate(true);

        if (mediaPlayer.isPlaying()){
            stopActivities();
        }

        mediaPlayer = new MediaPlayer();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        try {
            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setDataSource(fileLink);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(mediaPlayer.getDuration());
                miniPlayerSeekbar.setMax(mediaPlayer.getDuration());

                miniPlayerSeekbar.setIndeterminate(false);
                mediaPlayer.start();
                songDuration.setText(createTimerLabel(mediaPlayer.getDuration()));
                updateSeekBar();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNext(trackedPosition);

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean change) {
                if(change){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void playOrPause(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.play_button);
            miniPlayerBtnPlayPause.setImageResource(R.drawable.play_button);
        }
        else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.pause_button);
            miniPlayerBtnPlayPause.setImageResource(R.drawable.pause_button);
            updateSeekBar();
        }
    }

    @Override
    public void onBackPressed() {

        if (viewFlipper.getCurrentView().getId() == R.id.playSongMenu){
            showMiniPlayer(true);
            bottomNavigationView.setVisibility(View.VISIBLE);
            showMenu(0);
        }else {

            if (backPressedTime + 2000 > System.currentTimeMillis()){
                super.onBackPressed();
                stopActivities();
                return;
            }else {
                Toast.makeText(MainActivity.this,"Press back again to exit. \nPress Home for song to continue playing in background", Toast.LENGTH_SHORT).show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }

    public void shuffleSongs(int currentSongID, boolean enabled)
    {
        Song song = null;

        if (enabled){
            trackedPosition = 0;
            song = songList.get(currentSongID);
            Log.d("CurrentSong", "shuffle: " + song.getTitle());

            Log.d("ShuffleMode", "shuffle: " + isShuffle);
            shuffleList = (ArrayList<Song>) songList.clone();
            shuffleList.remove(song);
            Collections.shuffle(shuffleList);
            shuffleList.add(currentSongID, song);

            shuffleList.addAll(shuffleList.subList(0, currentSongID));

            shuffleList.subList(0, currentSongID).clear();

            disableButtons(shuffleList.indexOf(song));
        }else {
            song = shuffleList.get(currentSongID);
            trackedPosition = songList.indexOf(song);
            disableButtons(songList.indexOf(song));
        }

    }
}