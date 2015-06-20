package nov.chang.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

public class TrackContainer implements Parcelable {
    public String name;
    public String album;
    public String image;

    public TrackContainer(String name, String album, String image) {
        this.name = name;
        this.album = album;
        this.image = image;
    }

    protected TrackContainer(Parcel in) {
        name = in.readString();
        album = in.readString();
        image = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(album);
        dest.writeString(image);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TrackContainer> CREATOR = new Parcelable.Creator<TrackContainer>() {
        @Override
        public TrackContainer createFromParcel(Parcel in) {
            return new TrackContainer(in);
        }

        @Override
        public TrackContainer[] newArray(int size) {
            return new TrackContainer[size];
        }
    };
}