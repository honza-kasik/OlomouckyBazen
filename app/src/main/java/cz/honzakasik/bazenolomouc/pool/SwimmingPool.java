package cz.honzakasik.bazenolomouc.pool;

import java.util.List;

/**
 * Class representing swimming pool - count of its tracks, orientation, etc.
 */
public class SwimmingPool {

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
        VERTICAL, HORIZONTAL
    }

    /**
     * Individual track in swimming pool
     */
    public final class Track {

        private boolean isForPublic;

        public Track(boolean isForPublic) {
            this.isForPublic = isForPublic;
        }
    }

    public static final class Builder {

        private List<Track> tracks;
        private TrackOrientation orientation;

        public Builder(List<Track> tracks) {
            this.tracks = tracks;
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

        public SwimmingPool build() {
            return new SwimmingPool(this);
        }

    }

}
