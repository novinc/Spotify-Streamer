package nov.chang.spotifystreamer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import com.squareup.picasso.Picasso;

import java.io.Serializable;

import nov.chang.spotifystreamer.R;

public class SongsListAdapter extends SimpleCursorAdapter implements Serializable {
    public SongsListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to, 0);
    }

    @Override
    public void bindView(@NonNull View view, Context context, @NonNull Cursor cursor) {
        super.bindView(view, context, cursor);
        String image = cursor.getString(cursor.getColumnIndexOrThrow("image"));
        if (image != null) {
            ImageView songBox = (ImageView) view.findViewById(R.id.song_image);
            Picasso.with(context).load(Uri.parse(image)).into(songBox);
        }
    }


}
