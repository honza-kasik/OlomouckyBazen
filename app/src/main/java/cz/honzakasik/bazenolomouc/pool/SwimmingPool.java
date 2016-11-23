package cz.honzakasik.bazenolomouc.pool;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing swimming pool - count of its tracks, orientation, etc.
 */
public class SwimmingPool implements Parcelable {

    private List<Track> tracks;
    private TrackOrientation orientation;

    private SwimmingPool(Builder builder) {
        this.tracks = builder.tracks;
        this.orientation = builder.orientation;
    }

    /**
     * Orientation of tracks in swimming pool
     */
    public enum TrackOrientation {
        /**
         * Tracks are parallel with shorter dimension of swimming pool.
         */
        VERTICAL,

        /**
         * Tracks are parallel with longer dimension of swimming pool.
         */
        HORIZONTAL
    }

    /**
     * Individual track in swimming pool
     */
    public static final class Track {

        private boolean isForPublic;

        public Track(boolean isForPublic) {
            this.isForPublic = isForPublic;
        }
    }

    public static final class Builder {

        private List<Track> tracks;
        private TrackOrientation orientation;

        public Builder() {
            this.orientation = TrackOrientation.HORIZONTAL;
        }

        /**
         * Sets orientation which is HORIZONTAL by default
         * @param orientation {@link TrackOrientation}
         * @return this
         */
        public Builder orientation(TrackOrientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder track(Track track) {
            if (this.tracks == null) {
                this.tracks = new LinkedList<>();
            }
            this.tracks.add(track);
            return this;
        }

        public SwimmingPool build() {
            return new SwimmingPool(this);
        }

    }

    /****************************
        Parcelable impl follows
     ****************************/

    protected SwimmingPool(Parcel in) {
        if (in.readByte() == 0x01) {
            tracks = new ArrayList<>();
            in.readList(tracks, Track.class.getClassLoader());
        } else {
            tracks = null;
        }
        orientation = (TrackOrientation) in.readValue(TrackOrientation.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (tracks == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(tracks);
        }
        dest.writeValue(orientation);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SwimmingPool> CREATOR = new Parcelable.Creator<SwimmingPool>() {
        @Override
        public SwimmingPool createFromParcel(Parcel in) {
            return new SwimmingPool(in);
        }

        @Override
        public SwimmingPool[] newArray(int size) {
            return new SwimmingPool[size];
        }
    };
}