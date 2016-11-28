package cz.honzakasik.bazenolomouc;

import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Helper class for activity displaying swimming pool
 */
public class DatetimeDisplay {

    private static final Logger logger = LoggerFactory.getLogger(DatetimeDisplay.class);

    private TextView dateTextView;
    private TextView clockTextView;

    private Calendar currentlyDisplayedDate;

    private DatetimeDisplay(Builder builder) {
        this.dateTextView = builder.dateTextView;
        this.clockTextView = builder.clockTextView;
        this.currentlyDisplayedDate = getClosestValidDateFromNow();
    }

    /**
     * Sets time and date to display
     * @param datetime this will be set
     */
    public void setDatetimeToDisplay(Calendar datetime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        clockTextView.setText(dateFormat.format(datetime.getTime()));
        dateTextView.setText(datetime.get(Calendar.DAY_OF_MONTH) + "." +
                (datetime.get(Calendar.MONTH) + 1) + "." +
                datetime.get(Calendar.YEAR));
        currentlyDisplayedDate = datetime;
    }

    /**
     * Retrieves closest valid time from now.
     * @return closes valid time from now
     */
    public Calendar getClosestValidDateFromNow() {
        return getClosestValidDateFrom(Calendar.getInstance());
    }

    /**
     * Find out if time is valid - 0 minutes or 30 minutes
     * @param datetime time to check for validity
     * @return true if time is valid, false otherwise
     */
    public boolean isTimeValid(Calendar datetime) {
        long currentTime = getClosestValidDateFrom(Calendar.getInstance()).getTimeInMillis();
        return datetime.getTimeInMillis() >= currentTime;
    }

    /**
     * Adds minutes to currently displayed date
     * @param minutes amount of minutes to add
     * @return new {@link Calendar} with date 'currently displayed' + minutes
     */
    public Calendar addMinutesToCurrentlyDisplayedDatetime(int minutes) {
        Calendar newDate = getCurrentlyDisplayedDate();
        newDate.add(Calendar.MINUTE, minutes);
        return newDate;
    }

    /**
     * Retrieves a copy of currently displayed date.
     * @return copy of currently displayed datetime
     */
    public Calendar getCurrentlyDisplayedDate() {
        return (Calendar) currentlyDisplayedDate.clone();
    }

    /**
     * Gets closest valid time - 0 minutes or 30 minutes from passed time
     * @param datetime datetime to get closest valid date from
     * @return closes valid date
     */
    public Calendar getClosestValidDateFrom(Calendar datetime) {
        logger.debug("Right now is {}", datetime.toString());
        if (datetime.get(Calendar.MINUTE) < 15 || datetime.get(Calendar.MINUTE) > 45) {
            datetime.set(Calendar.MINUTE, 0);
        } else {
            datetime.set(Calendar.MINUTE, 30);
        }
        datetime.set(Calendar.SECOND, 0);
        datetime.set(Calendar.MILLISECOND, 0);
        logger.debug("Closest valid time is {}", datetime.toString());
        return datetime;
    }

    /**
     * Find out if invalid time is displayed
     * @return true if closest valid time is grater than displayed, false itherwise
     */
    public boolean isDisplayedValidTime() {
        return isTimeValid(currentlyDisplayedDate);
    }

    /**
     * Find out if closest valid time is displayed
     * @return true if closest valid time is displayed, false otherwise
     */
    public boolean isDisplayedClosestValidTime() {
        return getClosestValidDateFromNow().getTime().equals(currentlyDisplayedDate.getTime());
    }

    public static final class Builder {

        private TextView dateTextView;
        private TextView clockTextView;

        public Builder() {

        }

        public Builder dateTextView(TextView dateTextView) {
            this.dateTextView = dateTextView;
            return this;
        }

        public Builder clockTextView(TextView clockTextView) {
            this.clockTextView = clockTextView;
            return this;
        }

        private void validate() {
            if (dateTextView == null || clockTextView == null) {
                throw new IllegalStateException();
            }
        }

        public DatetimeDisplay build() {
            validate();
            return new DatetimeDisplay(this);
        }

    }

}
