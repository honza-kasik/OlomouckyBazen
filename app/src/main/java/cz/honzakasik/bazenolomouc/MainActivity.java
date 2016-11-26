package cz.honzakasik.bazenolomouc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

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
                currentlyDisplayedDate.add(Calendar.MINUTE, 30);
                setSwimmingPoolViewForDate(currentlyDisplayedDate);
            }
        });

        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentlyDisplayedDate.add(Calendar.MINUTE, -30);
                setSwimmingPoolViewForDate(currentlyDisplayedDate);
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
                        setSwimmingPoolViewForDate(newTime);
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
        if (datetime.getTimeInMillis() < currentlyDisplayedDate.getTimeInMillis()) {
            showErrorMessageInvalidDate();
            logger.debug("Invalid date passed!");
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
        Calendar datetime = Calendar.getInstance();
        logger.debug("Right now is {}", datetime.toString());
        if (datetime.get(Calendar.MINUTE) < 15 || datetime.get(Calendar.MINUTE) > 45) {
            datetime.set(Calendar.MINUTE, 0);
        } else {
            datetime.set(Calendar.MINUTE, 30);
        }
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

    private void showErrorMessageInvalidDate() {
        Toast.makeText(this, getResources().getString(R.string.invalid_date_error_message),
                Toast.LENGTH_LONG).show();
    }
}
