package cz.honzakasik.bazenolomouc.pool.olomoucpoolprovider;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;
import cz.honzakasik.bazenolomouc.pool.SwimmingPoolProviderService;

public class OlomoucPoolProviderService extends SwimmingPoolProviderService {

    private static final String
            ACTION_DONE = "DOWNLOAD_DONE",
            URL = "http://www.olterm.cz/plavecky-bazen/rozpis-plavani?den={DAY}&mesic={MONTH}&rok={YEAR}&hodina={HOURS}&minuta={MINUTES}",
            DAY = Pattern.quote("{DAY}"),
            MONTH = Pattern.quote("{MONTH}"),
            YEAR = Pattern.quote("{YEAR}"),
            HOURS = Pattern.quote("{HOURS}"),
            MINUTES = Pattern.quote("{MINUTES}");


    public OlomoucPoolProviderService(String name) {
        super("Olomouc pool provider service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long datetime = intent.getLongExtra(DATETIME_EXTRA_IDENTIFIER, -1);
        try {
            Document doc = Jsoup.connect(parseURLForDatetime(new Date(datetime))).get();
            SwimmingPool swimmingPool = new OlomoucSwimmingPoolParser(doc).parseSwimmingPool();

            Intent dataIntent = new Intent();
            dataIntent.putExtra(SWIMMING_POOL_EXTRA_IDENTIFIER, swimmingPool);
            dataIntent.setAction(ACTION_DONE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            //TODO
        } catch (IOException e) {
            e.printStackTrace();
            //TODO
        }
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
