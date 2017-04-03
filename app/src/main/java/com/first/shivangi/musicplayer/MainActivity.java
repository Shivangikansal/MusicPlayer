package com.first.shivangi.musicplayer;


import android.content.Intent;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController.MediaPlayerControl;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Playlist implements MediaPlayerControl, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private boolean playbackPaused = false;
    private MusicController controller;
    private SeekBar seekbar;
    private Utilities utils = new Utilities();
    private TextView songTotalDurationLabel;
    private TextView songCurrentDurationLabel;
    private ImageButton btnPlay;
    private TextView title;
    public int currentSongIndex;
    private String songTitle;
    private int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setController();
        isPlaying();
        getInit();
        title = (TextView) findViewById(R.id.textView);
        title.setText(songTitle);
        songTotalDurationLabel = (TextView) findViewById(R.id.total);
        songCurrentDurationLabel = (TextView) findViewById(R.id.current);
        //displaying total duration
        songTotalDurationLabel.setText(" " + utils.milliSecondsToTimer(duration));
        //displaying time completed playing
        songCurrentDurationLabel.setText(" " + utils.milliSecondsToTimer(0));
        // Updating progress bar
        int progress = (int) (utils.getProgressPercentage(0, 100));
        //Log.d("Progress", ""+progress);
        seekbar.setProgress(progress);

        //play button
        btnPlay = (ImageButton) findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check for already playing
                if (isPlaying()) {
                    if (musicSrv != null) {
                        pause();
                        btnPlay.setImageResource(R.drawable.btn_play);
                    }
                } else {
                    //resume song
                    if (musicSrv != null) {
                        resume();
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }
                }
            }
        });
    }

    public void onStart() {
        super.onStart();
        if (isPlaying()) {
            btnPlay.setImageResource(R.drawable.btn_pause);
        }
    }

    public void onResume() {
        super.onResume();
        title.setText(songTitle);
        songTotalDurationLabel.setText(" " + utils.milliSecondsToTimer(duration));
        if (musicSrv != null) {
            play();
        }
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }


    //display menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_playlist:
                startActivityForResult(new Intent(this, Playlist.class), 1);            //open playlist
                return true;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv = null;
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getInit() {
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        // Listeners
        seekbar.setOnSeekBarChangeListener(this); // Important
    }

    static Handler mHandler = new Handler();

    //update timer on seekbar
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    //background runnable thread
    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            long totalDuration;
            long currentDuration;
            if (musicSrv != null) {
                totalDuration = musicSrv.getDur();
                currentDuration = musicSrv.getPosn();

            } else {
                totalDuration = 0;
                currentDuration = 0;
            }

            //displaying total duration
            songTotalDurationLabel.setText("-" + utils.milliSecondsToTimer(totalDuration - currentDuration));
            //displaying time completed playing
            songCurrentDurationLabel.setText(" " + utils.milliSecondsToTimer(currentDuration));


            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            seekbar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    //When user starts moving the progress handler
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    //When user stops moving the progress hanlder
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = musicSrv.getDur();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
        // forward or backward to certain seconds
        seekTo(currentPosition);
        // update timer progress again
        updateProgressBar();
    }

    private void setController() {
        //set the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            currentSongIndex = data.getExtras().getInt("songIndex");
            songTitle = data.getStringExtra("title");
            duration = data.getExtras().getInt("songDur");
        }
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound) {
            title.setText(musicSrv.getSongTitle());
            return musicSrv.isPng();
        }
        return false;
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    public void play() {
        title.setText(musicSrv.getSongTitle());
        btnPlay.setImageResource(R.drawable.btn_pause);
        updateProgressBar();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    public void resume() {
        playbackPaused = false;
        musicSrv.resumePlayer();
    }

    //play next
    private void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
            btnPlay.setImageResource(R.drawable.btn_pause);
        }
        controller.show(0);
    }

    //play previous
    private void playPrev() {
        musicSrv.playPrev();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
            btnPlay.setImageResource(R.drawable.btn_pause);
        }
        controller.show(0);
    }

    public void previous(View view) {
        playPrev();
    }

    public void next(View view) {
        playNext();
    }

    public void rewind(View view) {

    }

    public void forward(View view) {

    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {}

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }
}

