package nov.chang.spotifystreamer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import nov.chang.spotifystreamer.Player;
import nov.chang.spotifystreamer.containers.ArtistContainer;
import nov.chang.spotifystreamer.containers.TrackContainer;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    public static final int NOTIFICATION_ID = 1;
    public static final String UPDATE = "nov.chang.spotifystreamer.backend.service.PLAYER_UPDATE";
    public static final String UPDATE_MAIN = "nov.chang.spotifystreamer.backend.service.MAIN_UPDATE";
    public static final String NOW_PLAYING = "nov.chang.spotifystreamer.backend.service.MAIN_NOW_PLAYING";
    public static final String ACTION_PLAY = "nov.chang.spotifystreamer.action.PLAY";
    public static final String ACTION_UPDATE = "nov.chang.spotifystreamer.action.UPDATE";
    public static State playerState = State.none;
    final String DEBUG = "myDebug";
    public enum State {none, idle, initialized, preparing, prepared, started, paused, stopped, completed, end, error};
    MediaPlayer mMediaPlayer;
    WifiManager.WifiLock wifiLock;
    int pos;
    ArrayList<TrackContainer> tracks;
    LocalBroadcastManager broadcaster;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public boolean pause() {
            if (playerState.equals(State.started)) {
                mMediaPlayer.pause();
                stopForeground(true);
                try {
                    wifiLock.release();
                } catch (Exception e) {
                }
                playerState = State.paused;
                return true;
            }
            return false;
        }
        public boolean play() {
            if (playerState.equals(State.prepared) || playerState.equals(State.paused)) {
                makeForeground();
                wifiLock.acquire();
                mMediaPlayer.start();
                playerState = State.started;
                return true;
            }
            return false;
        }
        public void  prevTrack() {
            playPrevious();
        }
        public void  nextTrack() {
            playNext();
        }
        public boolean setProgress(int time) {
            if (!playerState.equals(State.idle) && !playerState.equals(State.initialized)
                    && !playerState.equals(State.stopped) && !playerState.equals(State.error)) {
                mMediaPlayer.seekTo(time);
                return true;
            }
            return false;
        }
        public State getState() {
            return playerState;
        }
        public MediaPlayer getPlayer() {
            return mMediaPlayer;
        }
        public boolean isNewTrack(int position) {
            return (pos != position);
        }
        public int getPosition() {
            return pos;
        }
        public ArrayList<TrackContainer> getTracks() {
            return tracks;
        }
    }

    private void makeForeground() {
        String songName = tracks.get(pos).name;
        String artistName = "";
        for (ArtistContainer artistContainer : tracks.get(pos).artists) {
            artistName += ", " + artistContainer.name;
        }
        Intent[] intents = {new Intent(getApplicationContext(), Player.class)};
        intents[0].putExtra("track", tracks.get(pos));
        intents[0].putParcelableArrayListExtra("tracks", tracks);
        intents[0].putExtra("pos", pos);
        PendingIntent pendingIntent = PendingIntent.getActivities(getApplicationContext(), 0, intents, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setTicker("Spotify Streamer playing").setContentTitle(songName).setContentText(artistName.substring(2))
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //pos = intent.getIntExtra("pos", -1);
        //String source = ((TrackContainer)intent.getParcelableExtra("track")).uri;
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        broadcaster = LocalBroadcastManager.getInstance(this);
        if (intent != null && intent.getAction() == null) {
            Intent intent1 = new Intent(UPDATE_MAIN);
            intent1.putExtra("pos", pos);
            intent1.putParcelableArrayListExtra("tracks", tracks);
            broadcaster.sendBroadcast(intent1);
        } else if (intent != null && intent.getAction().equals(ACTION_PLAY)) {
            mMediaPlayer = new MediaPlayer();
            playerState = State.idle;
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            String source = ((TrackContainer)intent.getParcelableExtra("track")).uri;
            Log.v(DEBUG, "setting source: " + source);
            Uri sourceUri = Uri.parse(source);
            try {
                mMediaPlayer.setDataSource(getApplicationContext(),sourceUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            playerState = State.initialized;
            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
            wifiLock.acquire();
            mMediaPlayer.prepareAsync();
            playerState = State.preparing;
            pos = intent.getIntExtra("pos", -1);
            tracks = intent.getParcelableArrayListExtra("tracks");
        } else if (intent != null && intent.getAction().equals(ACTION_UPDATE)) {
            Intent intent1 = new Intent(UPDATE);
            intent.putExtra("direction", pos);
            broadcaster.sendBroadcast(intent1);
            Intent i = new Intent(NOW_PLAYING);
            broadcaster.sendBroadcast(i);
        }
        return mBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playerState = State.prepared;
        makeForeground();
        mp.start();
        Intent i = new Intent(NOW_PLAYING);
        broadcaster.sendBroadcast(i);
        Intent intent = new Intent(UPDATE_MAIN);
        intent.putExtra("track", tracks.get(pos));
        intent.putExtra("pos", pos);
        intent.putParcelableArrayListExtra("tracks", tracks);
        broadcaster.sendBroadcast(intent);
        playerState = State.started;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playerState = State.completed;
        playNext();
    }

    private void playNext() {
        Intent intent = new Intent(UPDATE);
        intent.putExtra("direction", "next");
        broadcaster.sendBroadcast(intent);
        mMediaPlayer.reset();
        playerState = State.idle;
        TrackContainer next;
        try {
            next = tracks.get(++pos);
        } catch (IndexOutOfBoundsException e) {
            pos = 0;
            next = tracks.get(pos);
        }
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(next.uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        playerState = State.initialized;
        mMediaPlayer.prepareAsync();
        playerState = State.preparing;
    }

    private void playPrevious() {
        Intent intent = new Intent(UPDATE);
        intent.putExtra("direction", "prev");
        broadcaster.sendBroadcast(intent);
        mMediaPlayer.reset();
        playerState = State.idle;
        TrackContainer prev;
        try {
            prev = tracks.get(--pos);
        } catch (IndexOutOfBoundsException e) {
            pos = tracks.size() - 1;
            prev = tracks.get(pos);
        }
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(prev.uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        playerState = State.initialized;
        mMediaPlayer.prepareAsync();
        playerState = State.preparing;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        playerState = State.error;
        return false;
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.release();
        playerState = State.end;
        mMediaPlayer = null;
        try {
            wifiLock.release();
        } catch (Exception e) {
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}
