package cz.honzakasik.bazenolomouc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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

    private TextView dateTextView;
    private TextView clockTextView;

    private Calendar currentlyDisplayedDate;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            swimmingPoolView.setSwimmingPool((SwimmingPool) intent.getParcelableExtra(SwimmingPoolProviderService.SWIMMING_POOL_EXTRA_IDENTIFIER));
            swimmingPoolView.invalidate();
            logger.info("Broadcast received!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swimmingPoolView = (SwimmingPoolView) findViewById(R.id.swimming_pool);
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

        currentlyDisplayedDate = getClosestValidDateFromNow();
        setSwimmingPoolViewForDate(currentlyDisplayedDate);
    }

    private void setSwimmingPoolViewForDate(Calendar datetime) {
        setTimeToDisplay(datetime);

        Intent poolProviderServiceIntent = new Intent(this, OlomoucPoolProviderService.class);
        poolProviderServiceIntent.putExtra(SwimmingPoolProviderService.DATETIME_EXTRA_IDENTIFIER, datetime.getTime().getTime());

        logger.debug("Starting service!");
        startService(poolProviderServiceIntent);
    }

    private void setTimeToDisplay(Calendar datetime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        clockTextView.setText(dateFormat.format(datetime.getTime()));
        dateTextView.setText(datetime.get(Calendar.DAY_OF_MONTH) + "." + datetime.get(Calendar.MONTH) + "." + datetime.get(Calendar.YEAR));
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

    private long getDesiredDateAndTime() {
        Calendar datetime = Calendar.getInstance();
        datetime.set(2016, 11, 30, 6, 0);
        return datetime.getTimeInMillis();
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
}
