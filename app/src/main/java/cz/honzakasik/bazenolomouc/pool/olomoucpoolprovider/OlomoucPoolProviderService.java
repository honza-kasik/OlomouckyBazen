package cz.honzakasik.bazenolomouc.pool.olomoucpoolprovider;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;
import cz.honzakasik.bazenolomouc.pool.SwimmingPoolProviderService;

public class OlomoucPoolProviderService extends SwimmingPoolProviderService {

    private static final Logger logger = LoggerFactory.getLogger(OlomoucPoolProviderService.class);

    private static final String
            URL = "http://www.olterm.cz/plavecky-bazen/rozpis-plavani?den={DAY}&mesic={MONTH}&rok={YEAR}&hodina={HOURS}&minuta={MINUTES}",
            DAY = Pattern.quote("{DAY}"),
            MONTH = Pattern.quote("{MONTH}"),
            YEAR = Pattern.quote("{YEAR}"),
            HOURS = Pattern.quote("{HOURS}"),
            MINUTES = Pattern.quote("{MINUTES}");

    public OlomoucPoolProviderService() {
        this("");
    }

    public OlomoucPoolProviderService(String name) {
        super("Olomouc pool provider service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        logger.debug("Started onHandleIntent");
        long datetime = intent.getLongExtra(DATETIME_EXTRA_IDENTIFIER, -1);
        try {
            String url = parseURLForDatetime(new Date(datetime));
            logger.debug("Connecting to url: '{}'...", url);
            Connection connection = Jsoup.connect(url);
            Document doc = connection.get();
            //logger.debug(doc.outerHtml());
            SwimmingPool swimmingPool = new OlomoucSwimmingPoolParser(doc).parseSwimmingPool();
            logger.debug("Obtained '{}' swimming pool!", swimmingPool.getOrientation());

            Intent dataIntent = new Intent();
            dataIntent.putExtra(SWIMMING_POOL_EXTRA_IDENTIFIER, swimmingPool);
            dataIntent.setAction(ACTION_DONE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);
            logger.debug("Broadcast sent!");

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
                .replaceAll(MONTH, String.valueOf(month + 1))
                .replaceAll(DAY, String.valueOf(day))
                .replaceAll(HOURS, String.valueOf(hours))
                .replaceAll(MINUTES, String.valueOf(minutes));
    }


}
