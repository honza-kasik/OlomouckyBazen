package cz.honzakasik.bazenolomouc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.support.v4.content.WakefulBroadcastReceiver;
import cz.honzakasik.bazenolomouc.olomoucdataprovider.occupancy.OlomoucOccupancyProviderService;
import cz.honzakasik.bazenolomouc.pool.SwimmingPool;
import cz.honzakasik.bazenolomouc.pool.SwimmingPoolProviderService;
import cz.honzakasik.bazenolomouc.olomoucdataprovider.poolprovider.OlomoucPoolProviderService;

public class MainActivity extends AppCompatActivity {

    private static final String DATETIME_SAVED_INSTANCE_IDENTIFIER = "SAVED_DATETIME";

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private SwimmingPoolView swimmingPoolView;
    private ProgressBar swimmingPoolProgressBar;

    private TextView occupancyTextView;
    private ProgressBar occupancyTextProgressBar;

    private DatetimeDisplay datetimeDisplay;

    private SwipeRefreshLayout swipeRefreshLayout;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            logger.debug("Broadcast received!");

            String action = intent.getAction();
            if (action.equals(SwimmingPoolProviderService.ACTION_SWIMMING_POOL_DOWNLOADED)) {
                logger.debug("Swimming pool broadcast received");
                SwimmingPool swimmingPool = intent.getParcelableExtra(SwimmingPoolProviderService.SWIMMING_POOL_EXTRA_IDENTIFIER);
                long datetime = intent.getLongExtra(SwimmingPoolProviderService.DATETIME_EXTRA_IDENTIFIER, -1);

                //if timestamp is corresponding to currently set time
                if (datetime == datetimeDisplay.getCurrentlyDisplayedDate().getTimeInMillis()) {
                    swimmingPoolView.setSwimmingPool(swimmingPool);

                    CyclicLayoutLoadManager.loadFinished(swipeRefreshLayout);
                    swimmingPoolProgressBar.setVisibility(View.INVISIBLE);
                    swimmingPoolView.setVisibility(View.VISIBLE);

                    swimmingPoolView.invalidate();
                } else {
                    if (datetime == -1) {
                        logger.error("Datetime was not attached!");
                    } else {
                        logger.warn("Received swimming poool for different time!");
                    }
                }

            } else if (action.equals(OlomoucOccupancyProviderService.ACTION_OCCUPANCY_PROVIDED)) {
                if (intent.hasExtra(OlomoucOccupancyProviderService.OCCUPANCY_EXTRA_KEY)) {
                    int occupancy = intent.getIntExtra(OlomoucOccupancyProviderService.OCCUPANCY_EXTRA_KEY, -1);
                    logger.debug("Occupancy broadcast received with value '{}'.", occupancy);
                    updateOccupancyLabel(occupancy);
                } else {
                    logger.debug("Occupancy intent received with no occupancy, not updating!");
                }

            } else if (action.equals(SwimmingPoolProviderService.ACTION_ERROR_OCCURRED_IN_PROVIDER_SERVICE)) {
                showErrorMessage(intent.getStringExtra(SwimmingPoolProviderService.ERROR_MESSAGE_EXTRA_IDENTIFIER));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swimmingPoolView = (SwimmingPoolView) findViewById(R.id.swimming_pool);
        swimmingPoolProgressBar = (ProgressBar) findViewById(R.id.swimming_pool_progress_bar);
        occupancyTextView = (TextView) findViewById(R.id.occupancy_text_view);
        occupancyTextProgressBar = (ProgressBar) findViewById(R.id.current_occupancy_message);

        final ImageButton arrowLeft = (ImageButton) findViewById(R.id.swimming_pool_arrow_left);
        final ImageButton arrowRight = (ImageButton) findViewById(R.id.swimming_pool_arrow_right);

        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMinutesAndUpdateSwimmingPoolIfNewDatetimeIsValid(30);
                arrowLeft.setClickable(true);
                arrowLeft.setEnabled(true);
            }
        });

        arrowLeft.setClickable(false);
        arrowLeft.setEnabled(false);
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMinutesAndUpdateSwimmingPoolIfNewDatetimeIsValid(-30);
                if (datetimeDisplay.isDisplayedClosestValidTime()) {
                    //next is invalid time
                    arrowLeft.setClickable(false);
                    arrowLeft.setEnabled(false);
                }
            }
        });

        //opens a time picker when clicked on time
        TextView clockTextView = (TextView) findViewById(R.id.swimming_pool_time);
        clockTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar newTime = datetimeDisplay.getCurrentlyDisplayedDate();
                        newTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        newTime.set(Calendar.MINUTE, minute);
                        setNewDatetimeAndUpdateSwimmingPoolIfIsValid(newTime);
                        if (datetimeDisplay.isTimeValid(newTime)) {
                            arrowLeft.setClickable(true);
                            arrowLeft.setEnabled(true);
                        }
                    }
                }, datetimeDisplay.getCurrentlyDisplayedDate().get(Calendar.HOUR_OF_DAY),
                        datetimeDisplay.getCurrentlyDisplayedDate().get(Calendar.MINUTE),
                        true);
                dialog.show();
            }
        });

        //opens a date picker when clicked on date
        TextView dateTextView = (TextView) findViewById(R.id.swimming_pool_date);
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Calendar newDate = datetimeDisplay.getCurrentlyDisplayedDate();
                                newDate.set(Calendar.YEAR, year);
                                newDate.set(Calendar.MONTH, month);
                                newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                setNewDatetimeAndUpdateSwimmingPoolIfIsValid(newDate);
                            }
                        },
                        datetimeDisplay.getCurrentlyDisplayedDate().get(Calendar.YEAR),
                        datetimeDisplay.getCurrentlyDisplayedDate().get(Calendar.MONTH),
                        datetimeDisplay.getCurrentlyDisplayedDate().get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        //swipe to refresh listener
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateOccupancy();
                setNewDatetimeAndUpdateSwimmingPoolIfIsValid(datetimeDisplay.getClosestValidDateFromNow());
                arrowLeft.setClickable(false);
                arrowLeft.setEnabled(false);
            }
        });

        datetimeDisplay = new DatetimeDisplay.Builder()
                .clockTextView(clockTextView)
                .dateTextView(dateTextView)
                .build();

        occupancyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOccupancy();
            }
        });

        //load datetime from saved instance if for example rotation changed and this method was called again
        Calendar initialDateTime = datetimeDisplay.getClosestValidDateFromNow();
        if (savedInstanceState != null) { //if saved instance state is present
            logger.debug("Saved instance is present!");
            final long savedDatetime = savedInstanceState.getLong(DATETIME_SAVED_INSTANCE_IDENTIFIER);
            if (savedDatetime != 0) {
                logger.debug("There was set datetime in saved instance!");
                Calendar newDatetime = Calendar.getInstance();
                newDatetime.setTimeInMillis(savedDatetime);
                initialDateTime = newDatetime;
            }
        }

        setDatetimeToViewAndUpdateSwimmingPoolForDatetime(initialDateTime);
        updateOccupancy();
    }

    /**
     * Adds amount of minutes (even negative value) to currently displayed time and updates both
     * time display and swimming pool view
     * @param minutes amount of minutes to add
     */
    private void addMinutesAndUpdateSwimmingPoolIfNewDatetimeIsValid(int minutes) {
        Calendar newDate = datetimeDisplay.addMinutesToCurrentlyDisplayedDatetime(minutes);
        setNewDatetimeAndUpdateSwimmingPoolIfIsValid(newDate);
    }

    /**
     * Sets new time to time display and updates swimming pool view accordingly.
     * @param newDatetime time to set
     */
    private void setNewDatetimeAndUpdateSwimmingPoolIfIsValid(Calendar newDatetime) {
        if (datetimeDisplay.isTimeValid(newDatetime)) {
            setDatetimeToViewAndUpdateSwimmingPoolForDatetime(newDatetime);
        } else {
            if (!datetimeDisplay.isDisplayedValidTime()) {
                setDatetimeToViewAndUpdateSwimmingPoolForDatetime(datetimeDisplay.getClosestValidDateFromNow());
            } else {
                showErrorMessageInvalidDate();
            }
        }
    }

    private void setDatetimeToViewAndUpdateSwimmingPoolForDatetime(Calendar datetime) {
        datetimeDisplay.setDatetimeToDisplay(datetime);
        updateSwimmingPoolViewForDatetime(datetimeDisplay.getClosestValidDateFrom(datetime));
    }

    /**
     * Starts service which downloads and parses swimming pool for passed datetime. Updating date
     * itself is responsibility of broadcast receiver.
     * @param datetime date and time for which the swimming pool will be downloaded
     */
    private void updateSwimmingPoolViewForDatetime(Calendar datetime) {
        updateSwimmingPoolViewForDatetime(datetime, false);
    }


    private void updateSwimmingPoolViewForDatetime(Calendar datetime, boolean progressbarAlreadyShown) {

        if (!progressbarAlreadyShown) {
            swimmingPoolProgressBar.setVisibility(View.VISIBLE);
            swimmingPoolView.setVisibility(View.INVISIBLE);
        }

        Intent poolProviderServiceIntent = new Intent(this, OlomoucPoolProviderService.class);
        poolProviderServiceIntent.putExtra(SwimmingPoolProviderService.DATETIME_EXTRA_IDENTIFIER, datetime.getTime().getTime());

        logger.debug("Starting service!");
        WakefulBroadcastReceiver.startWakefulService(this, poolProviderServiceIntent);
    }



    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SwimmingPoolProviderService.ACTION_SWIMMING_POOL_DOWNLOADED);
        intentFilter.addAction(OlomoucOccupancyProviderService.ACTION_OCCUPANCY_PROVIDED);
        intentFilter.addAction(SwimmingPoolProviderService.ACTION_ERROR_OCCURRED_IN_PROVIDER_SERVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        if (swimmingPoolProgressBar.getVisibility() == View.VISIBLE) {
            updateSwimmingPoolViewForDatetime(datetimeDisplay.getCurrentlyDisplayedDate(), true);
        }

        if (occupancyTextProgressBar.getVisibility() == View.VISIBLE) {
            updateOccupancy(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DATETIME_SAVED_INSTANCE_IDENTIFIER,
                datetimeDisplay.getCurrentlyDisplayedDate().getTimeInMillis());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_app_menu_item:
                logger.debug("Selected about option.");
                showAbout();
                return true;
            case R.id.update_menu_item:
                logger.debug("Selected update option.");
                updateOccupancy();
                updateSwimmingPoolViewForDatetime(datetimeDisplay.getClosestValidDateFromNow());
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Shows dialog with information about application
     */
    private void showAbout() {
        logger.debug("Opening about dialog.");
        View messageView = getLayoutInflater().inflate(R.layout.dialog_about, null, false);

        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    /**
     * Retrieves new information from server about occupancy
     */
    private void updateOccupancy() {
        updateOccupancy(false);
    }

    private void updateOccupancy(boolean progressbarAlreadyShown) {
        Intent intent = new Intent(this, OlomoucOccupancyProviderService.class);
        WakefulBroadcastReceiver.startWakefulService(this, intent);
        logger.debug("Started occupancy provider service!");

        if (!progressbarAlreadyShown) {
            occupancyTextProgressBar.setVisibility(View.VISIBLE);
            occupancyTextView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Updates occupancy label with new value
     * @param occupancy new value
     */
    private void updateOccupancyLabel(int occupancy) {
        String text = String.valueOf(getResources().getQuantityText(R.plurals.pool_occupancy, occupancy));

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        occupancyTextView.setText(String.format(text, dateFormat.format(new Date()), occupancy));

        CyclicLayoutLoadManager.loadFinished(swipeRefreshLayout);
        occupancyTextProgressBar.setVisibility(View.INVISIBLE);
        occupancyTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows error message as toast
     */
    private void showErrorMessageInvalidDate() {
        showErrorMessage(getResources().getString(R.string.invalid_date_error_message));
    }

    /**
     * Shows error message as toast
     */
    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * This class is just a wrapper for managing progress bar and swipe load layout
     */
    private static final class CyclicLayoutLoadManager {

        private static final int INITIAL_COUNT = 2;

        private static int countOfItemsToWaitFor = INITIAL_COUNT;

        private static void loadFinished(SwipeRefreshLayout swipeRefreshLayout) {
            if (swipeRefreshLayout.isRefreshing()) {
                countOfItemsToWaitFor--;
                if (countOfItemsToWaitFor == 0) {
                    swipeRefreshLayout.setRefreshing(false);
                    countOfItemsToWaitFor = INITIAL_COUNT;
                }
            }
        }

    }
}
