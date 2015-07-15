package nov.chang.spotifystreamer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

import nov.chang.spotifystreamer.containers.TrackContainer;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    final String DEBUG = "myDebug";
    public static final String ACTION_PLAY = "nov.chang.spotifystreamer.action.PLAY";
    MediaPlayer mMediaPlayer = null;
    WifiManager.WifiLock wifiLock;
    int pos;
    ArrayList<TrackContainer> tracks;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction().equals(ACTION_PLAY)) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            String source = ((TrackContainer)intent.getParcelableExtra("track")).uri;
            Uri sourceUri = Uri.parse(source);
            try {
                mMediaPlayer.setDataSource(getApplicationContext(),sourceUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
            wifiLock.acquire();
            mMediaPlayer.prepareAsync();
            pos = intent.getIntExtra("pos", -1);
            tracks = intent.getParcelableArrayListExtra("tracks");
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent intentService = new Intent(getApplicationContext(), PlayerService.class);
        intentService.setAction(PlayerService.ACTION_PLAY);
        intentService.putExtra("track", tracks.get(pos + 1));
        intentService.putParcelableArrayListExtra("tracks", tracks);
        intentService.putExtra("pos", pos + 1);
        getApplicationContext().startService(intentService);
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.release();
        mMediaPlayer = null;
        wifiLock.release();
    }
}
