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

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;
import cz.honzakasik.bazenolomouc.pool.SwimmingPoolProviderService;
import cz.honzakasik.bazenolomouc.pool.olomoucpoolprovider.OlomoucPoolProviderService;

public class MainActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private SwimmingPoolView swimmingPoolView;

    private ProgressBar progressBar;

    private TextView dateTextView;
    private TextView clockTextView;

    private Calendar currentlyDisplayedDate;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            SwimmingPool swimmingPool = intent.getParcelableExtra(SwimmingPoolProviderService.SWIMMING_POOL_EXTRA_IDENTIFIER);

            swimmingPoolView.setSwimmingPool(swimmingPool);

            progressBar.setVisibility(View.INVISIBLE);
            swimmingPoolView.setVisibility(View.VISIBLE);

            swimmingPoolView.invalidate();
            logger.info("Broadcast received!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swimmingPoolView = (SwimmingPoolView) findViewById(R.id.swimming_pool);
        progressBar = (ProgressBar) findViewById(R.id.swimming_pool_progress_bar);
        ImageButton arrowLeft = (ImageButton) findViewById(R.id.swimming_pool_arrow_left);
        ImageButton arrowRight = (ImageButton) findViewById(R.id.swimming_pool_arrow_right);
        dateTextView = (TextView) findViewById(R.id.swimming_pool_date);
        clockTextView = (TextView) findViewById(R.id.swimming_pool_time);

        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar newDate = (Calendar) currentlyDisplayedDate.clone();
                newDate.add(Calendar.MINUTE, 30);
                setSwimmingPoolViewForDate(newDate);
            }
        });

        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar newDate = (Calendar) currentlyDisplayedDate.clone();
                newDate.add(Calendar.MINUTE, -30);
                setSwimmingPoolViewForDate(newDate);
            }
        });

        clockTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar newTime = (Calendar) currentlyDisplayedDate.clone();
                        newTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        newTime.set(Calendar.MINUTE, minute);
                        setSwimmingPoolViewForDate(getClosestValidDateFrom(newTime));
                    }
                }, currentlyDisplayedDate.get(Calendar.HOUR_OF_DAY), currentlyDisplayedDate.get(Calendar.MINUTE), true);
                dialog.show();
            }
        });

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Calendar newDate = (Calendar) currentlyDisplayedDate.clone();
                                newDate.set(Calendar.YEAR, year);
                                newDate.set(Calendar.MONTH, month);
                                newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                setSwimmingPoolViewForDate(newDate);
                            }
                        },
                        currentlyDisplayedDate.get(Calendar.YEAR),
                        currentlyDisplayedDate.get(Calendar.MONTH),
                        currentlyDisplayedDate.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        currentlyDisplayedDate = getClosestValidDateFromNow();
        setSwimmingPoolViewForDate(currentlyDisplayedDate);
    }

    /**
     * Starts service which downloads and parses swimming pool for passed datetime, it also makes a
     * check if passed date is not behind current date
     * @param datetime date and time for which the swimming pool will be downloaded
     */
    private void setSwimmingPoolViewForDate(Calendar datetime) {
        if (!isTimeValid(datetime)) {
            showErrorMessageInvalidDate();
            return;
        }

        setTimeToDisplay(datetime);
        progressBar.setVisibility(View.VISIBLE);
        swimmingPoolView.setVisibility(View.INVISIBLE);

        Intent poolProviderServiceIntent = new Intent(this, OlomoucPoolProviderService.class);
        poolProviderServiceIntent.putExtra(SwimmingPoolProviderService.DATETIME_EXTRA_IDENTIFIER, datetime.getTime().getTime());

        logger.debug("Starting service!");
        startService(poolProviderServiceIntent);
    }

    /**
     * Sets time and date to display
     * @param datetime this will be set
     */
    private void setTimeToDisplay(Calendar datetime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        clockTextView.setText(dateFormat.format(datetime.getTime()));
        dateTextView.setText(datetime.get(Calendar.DAY_OF_MONTH) + "." +
                (datetime.get(Calendar.MONTH) + 1) + "." +
                datetime.get(Calendar.YEAR));
        currentlyDisplayedDate = datetime;
    }

    private Calendar getClosestValidDateFromNow() {
        return getClosestValidDateFrom(Calendar.getInstance());
    }

    private boolean isTimeValid(Calendar datetime) {
        long currentTime = getClosestValidDateFrom(Calendar.getInstance()).getTimeInMillis();
        return datetime.getTimeInMillis() >= currentTime;
    }

    private Calendar getClosestValidDateFrom(Calendar datetime) {
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

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SwimmingPoolProviderService.ACTION_DONE);
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

    private void showErrorMessageInvalidDate() {
        Toast.makeText(this, getResources().getString(R.string.invalid_date_error_message),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_app_menu_item:
                logger.debug("Selected about option.");
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showAbout() {
        logger.debug("Opening about dialog.");
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.dialog_about, null, false);

        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.app_icon);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

}
