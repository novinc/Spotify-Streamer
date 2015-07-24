package nov.chang.spotifystreamer.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class MusicIntentReceiver extends android.content.BroadcastReceiver {
    public static final String STOP = "nov.chang.spotifystreamer.backend.STOP";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Intent intent1 = new Intent(STOP);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent1);
        }
    }
}