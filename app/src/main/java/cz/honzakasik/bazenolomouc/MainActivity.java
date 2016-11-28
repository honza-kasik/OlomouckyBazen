package cz.honzakasik.bazenolomouc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
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

import cz.honzakasik.bazenolomouc.olomoucdataprovider.occupancy.OlomoucOccupancyProviderService;
import cz.honzakasik.bazenolomouc.pool.SwimmingPool;
import cz.honzakasik.bazenolomouc.pool.SwimmingPoolProviderService;
import cz.honzakasik.bazenolomouc.olomoucdataprovider.poolprovider.OlomoucPoolProviderService;

public class MainActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private SwimmingPoolView swimmingPoolView;
    private ProgressBar progressBar;

    private TextView occupancyTextView;

    private DatetimeDisplay datetimeDisplay;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            logger.debug("Broadcast received!");

            String action = intent.getAction();
            if (action.equals(SwimmingPoolProviderService.ACTION_DONE)) {
                logger.debug("Swimming pool broadcast recieved");
                SwimmingPool swimmingPool = intent.getParcelableExtra(SwimmingPoolProviderService.SWIMMING_POOL_EXTRA_IDENTIFIER);

                swimmingPoolView.setSwimmingPool(swimmingPool);

                progressBar.setVisibility(View.INVISIBLE);
                swimmingPoolView.setVisibility(View.VISIBLE);

                swimmingPoolView.invalidate();
            } else if (action.equals(OlomoucOccupancyProviderService.ACTION_OCCUPANCY_PROVIDED)) {
                int occupancy = intent.getIntExtra(OlomoucOccupancyProviderService.OCCUPANCY_EXTRA_KEY, -1);
                logger.debug("Occupancy broadcast received with value '{}'.", occupancy);
                updateOccupancyLabel(occupancy);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swimmingPoolView = (SwimmingPoolView) findViewById(R.id.swimming_pool);
        progressBar = (ProgressBar) findViewById(R.id.swimming_pool_progress_bar);
        occupancyTextView = (TextView) findViewById(R.id.occupancy_text_view);

        final ImageButton arrowLeft = (ImageButton) findViewById(R.id.swimming_pool_arrow_left);
        final ImageButton arrowRight = (ImageButton) findViewById(R.id.swimming_pool_arrow_right);

        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMinutesAndUpdateSwimmingPoolIfNewDatetimeIsValid(30);
                arrowLeft.setEnabled(true);
                arrowLeft.setClickable(true);
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

        ImageButton refreshButton = (ImageButton) findViewById(R.id.swimming_pool_clock_refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Load nearest time
                updateOccupancy();
                setNewDatetimeAndUpdateSwimmingPoolIfIsValid(datetimeDisplay.getClosestValidDateFromNow());
                arrowLeft.setClickable(false);
                arrowLeft.setEnabled(false);
            }
        });

        setDatetimeToViewAndUpdateSwimmingPoolForDatetime(datetimeDisplay.getCurrentlyDisplayedDate());
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
        progressBar.setVisibility(View.VISIBLE);
        swimmingPoolView.setVisibility(View.INVISIBLE);

        Intent poolProviderServiceIntent = new Intent(this, OlomoucPoolProviderService.class);
        poolProviderServiceIntent.putExtra(SwimmingPoolProviderService.DATETIME_EXTRA_IDENTIFIER, datetime.getTime().getTime());

        logger.debug("Starting service!");
        startService(poolProviderServiceIntent);
    }



    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SwimmingPoolProviderService.ACTION_DONE);
        intentFilter.addAction(OlomoucOccupancyProviderService.ACTION_OCCUPANCY_PROVIDED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_app_menu_item:
                logger.debug("Selected about option.");
                showAbout();
                return true;
            case R.id.update_menu_item:
                logger.debug("Selected update option.");
                updateOccupancy();
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
        Intent intent = new Intent(this, OlomoucOccupancyProviderService.class);
        startService(intent);
        logger.debug("Started occupancy provider service!");
    }

    /**
     * Updates occupancy label with new value
     * @param occupancy new value
     */
    private void updateOccupancyLabel(int occupancy) {
        String text = getResources().getString(R.string.pool_occupancy);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        occupancyTextView.setText(String.format(text, dateFormat.format(new Date()), occupancy));
    }

    /**
     * Shows error message as toast
     */
    private void showErrorMessageInvalidDate() {
        Toast.makeText(this, getResources().getString(R.string.invalid_date_error_message),
                Toast.LENGTH_LONG).show();
    }

}
