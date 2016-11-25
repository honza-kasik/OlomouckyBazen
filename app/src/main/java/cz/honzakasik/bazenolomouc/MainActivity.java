package cz.honzakasik.bazenolomouc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.regex.Pattern;

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;
import cz.honzakasik.bazenolomouc.pool.SwimmingPoolProviderService;
import cz.honzakasik.bazenolomouc.pool.olomoucpoolprovider.OlomoucPoolProviderService;

public class MainActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private SwimmingPoolView swimmingPoolView;

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

        Intent poolProviderServiceIntent = new Intent(this, OlomoucPoolProviderService.class);
        poolProviderServiceIntent.putExtra(SwimmingPoolProviderService.DATETIME_EXTRA_IDENTIFIER, getDesiredDateAndTime());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.debug("Starting service!");
        //startService(poolProviderServiceIntent);


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
