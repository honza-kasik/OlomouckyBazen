package cz.honzakasik.bazenolomouc.olomoucdataprovider.occupancy;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;

import cz.honzakasik.bazenolomouc.olomoucdataprovider.OlomoucUniversalTableParser;
import cz.honzakasik.bazenolomouc.olomoucdataprovider.RowIndexOutOfTableBoundsException;

public class OlomoucOccupancyProviderService extends IntentService {

    private static final Logger logger = LoggerFactory.getLogger(OlomoucOccupancyProviderService.class);

    public static final String
            ACTION_OCCUPANCY_PROVIDED = "ACTION_OCCUPANCY_PROVIDED",
            OCCUPANCY_EXTRA_KEY = "OCCUPANCY_KEY";

    private static final String
            URL = "http://olterm.cz/get_obsazenost.php";

    private static final int CURRENT_OCCUPANCY_ROW_INDEX = 1;

    public OlomoucOccupancyProviderService() {
        this("Olomouc occupancy provider service");
    }

    public OlomoucOccupancyProviderService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        logger.debug("Started!");
        Intent dataIntent = new Intent();
        dataIntent.setAction(ACTION_OCCUPANCY_PROVIDED);
        Connection connection = Jsoup.connect(URL);
        try {
            Document document = connection.get();
            int occupancy = 0;
            try {
                occupancy = new OlomoucUniversalTableParser(document, CURRENT_OCCUPANCY_ROW_INDEX).parse();
            } catch (RowIndexOutOfTableBoundsException e) {
                logger.error("Unable to parse the occupancy from page.", e);
            }
            dataIntent.putExtra(OCCUPANCY_EXTRA_KEY, occupancy);
        } catch (SocketTimeoutException e) {
            logger.error("Socket timed out when obtaining pool occupancy!\n", e);
        } catch (IOException e) {
            logger.error("There was an error when obtaining pool occupancy!\n", e);
        } finally {
            LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }
}
