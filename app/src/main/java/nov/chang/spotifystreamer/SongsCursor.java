package nov.chang.spotifystreamer;

import android.database.MatrixCursor;

import java.io.Serializable;

public class SongsCursor extends MatrixCursor implements Serializable {

    public SongsCursor(String[] clmns) {
        super(clmns);
    }

}
