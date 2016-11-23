package cz.honzakasik.bazenolomouc.pool.olomoucpoolprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;
import cz.honzakasik.bazenolomouc.pool.SwimmingPoolProvider;

/**
 * Specific provider for Olomouc swimming pool
 */
public class OlomoucSwimmingPoolProvider implements SwimmingPoolProvider {

    private final Context context;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    private static final String
            URL = "http://www.olterm.cz/plavecky-bazen/rozpis-plavani?den={DAY}&mesic={MONTH}&rok={YEAR}&hodina={HOURS}&minuta={MINUTES}",
            DAY = Pattern.quote("{DAY}"),
            MONTH = Pattern.quote("{MONTH}"),
            YEAR = Pattern.quote("{YEAR}"),
            HOURS = Pattern.quote("{HOURS}"),
            MINUTES = Pattern.quote("{MINUTES}");

    public OlomoucSwimmingPoolProvider(Context context) {
        this.context = context;
    }

    @Override
    public SwimmingPool obtainSwimmingPoolForDatetime(Date date) {
        Intent intent = new Intent(context, OlomoucDownloadService.class);
        try {
            intent.putExtra(OlomoucDownloadService.URL_EXTRA_IDENTIFIER, parseURLForDatetime(date))
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        context.startService(intent);
    }

    private String parseURLForDatetime(Date date) throws MalformedURLException {
        Calendar time = Calendar.getInstance();
        time.setTime(date);

        int year = time.get(Calendar.YEAR);
        int month = time.get(Calendar.MONTH);
        int day = time.get(Calendar.DAY_OF_MONTH);
        int hours = time.get(Calendar.HOUR_OF_DAY);
        int minutes = time.get(Calendar.MINUTE);

        return URL.replaceAll(YEAR, String.valueOf(year))
                .replaceAll(MONTH, String.valueOf(month))
                .replaceAll(DAY, String.valueOf(day))
                .replaceAll(HOURS, String.valueOf(hours))
                .replaceAll(MINUTES, String.valueOf(minutes));
    }
}
