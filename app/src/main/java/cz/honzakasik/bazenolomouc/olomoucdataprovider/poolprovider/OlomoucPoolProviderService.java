package cz.honzakasik.bazenolomouc.olomoucdataprovider.poolprovider;

import android.content.Intent;

import com.anupcowkur.reservoir.Reservoir;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import androidx.legacy.content.WakefulBroadcastReceiver;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cz.honzakasik.bazenolomouc.R;
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
        final long datetime = intent.getLongExtra(DATETIME_EXTRA_IDENTIFIER, -1);
        final Date date = new Date(datetime);
        final String dateKey = String.valueOf(datetime);

        logger.info("Obtaining pool data for time '{}'", dateKey);

        try {
            if (Reservoir.contains(dateKey)) {
                logger.debug("Cache hit! Pool found!");
                SwimmingPool swimmingPool = null;
                try {
                    swimmingPool = Reservoir.get(dateKey, SwimmingPool.class);
                } catch (NullPointerException e) {
                    logger.error("Cached value for key '{}' is null!", dateKey);
                }
                sendSwimmingPoolAsBroadcast(swimmingPool, datetime);
            } else {
                logger.debug("Cache miss!");
                SwimmingPool swimmingPool = downloadAndParseSwimmingPool(date);
                sendSwimmingPoolAsBroadcast(swimmingPool, datetime);
                saveToCacheIfNotNull(date, swimmingPool);
            }
        } catch (UnknownHostException e) {
            logger.error("Could not resolve host!\n", e);
            sendErrorMessageAsBroadcast(getString(R.string.connection_error_message));
        } catch (SocketTimeoutException e) {
            logger.error("Socket timed out when  downloading pool data!\n", e);
            sendErrorMessageAsBroadcast(getString(R.string.connection_error_message));
        } catch (IOException e) {
            logger.error("Unexpected error when trying to obtain data for time '{}'\n", dateKey, e);
            sendErrorMessageAsBroadcast(getString(R.string.obtaining_data_unexpected_error_message));
        } catch (NoPoolParsedException e) {
            logger.error("No pool was parsed!\n", e);
            sendErrorMessageAsBroadcast(getString(R.string.parsing_pool_unexpected_error_message));
        } finally {
            logger.debug("Calling complete wakeful intent!");
            if (WakefulBroadcastReceiver.completeWakefulIntent(intent)) {
                logger.debug("Successfully completed wakeful service!");
            }
        }
    }

    private SwimmingPool downloadAndParseSwimmingPool(Date datetime) throws NoPoolParsedException, IOException {
        String url = parseURLForDatetime(datetime);
        logger.debug("Connecting to url: '{}'", url);
        Connection connection = Jsoup.connect(url);
        Document doc = connection.get();

        SwimmingPool swimmingPool = null;
        swimmingPool = new OlomoucSwimmingPoolParser(doc).parseSwimmingPool();
        logger.debug("Obtained '{}' swimming pool!", swimmingPool.getOrientation());

        return swimmingPool;
    }

    private String parseURLForDatetime(Date date) {
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

    private void saveToCacheIfNotNull(Date date, SwimmingPool swimmingPool) throws IOException {
        if (swimmingPool == null) {
            return;
        }
        Reservoir.put(String.valueOf(date.getTime()), swimmingPool);
    }

    private void sendSwimmingPoolAsBroadcast(SwimmingPool swimmingPool, long datetime) {
        Intent dataIntent = new Intent();
        dataIntent.putExtra(SWIMMING_POOL_EXTRA_IDENTIFIER, swimmingPool);
        dataIntent.putExtra(DATETIME_EXTRA_IDENTIFIER, datetime);
        dataIntent.setAction(ACTION_SWIMMING_POOL_DOWNLOADED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);
        logger.debug("Broadcast with swimming pool sent!");
    }

    private void sendErrorMessageAsBroadcast(String errorMessage) {
        Intent dataIntent = new Intent();
        dataIntent.putExtra(ERROR_MESSAGE_EXTRA_IDENTIFIER, errorMessage);
        dataIntent.setAction(ACTION_ERROR_OCCURRED_IN_PROVIDER_SERVICE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);
        logger.debug("Error broadcast sent!");

    }
}
