package nov.chang.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import kaaes.spotify.webapi.android.models.Image;

public class ImageContainer implements Parcelable, Serializable {
    public Integer width;
    public Integer height;
    public String url;

    public ImageContainer(Integer width, Integer height, String url) {
        this.width = width;
        this.height = height;
        this.url = url;
    }

    public ImageContainer(Image image) {
        this(image.width, image.height, image.url);
    }

    protected ImageContainer(Parcel in) {
        width = in.readByte() == 0x00 ? null : in.readInt();
        height = in.readByte() == 0x00 ? null : in.readInt();
        url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (width == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(width);
        }
        if (height == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(height);
        }
        dest.writeString(url);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ImageContainer> CREATOR = new Parcelable.Creator<ImageContainer>() {
        @Override
        public ImageContainer createFromParcel(Parcel in) {
            return new ImageContainer(in);
        }

        @Override
        public ImageContainer[] newArray(int size) {
            return new ImageContainer[size];
        }
    };
}