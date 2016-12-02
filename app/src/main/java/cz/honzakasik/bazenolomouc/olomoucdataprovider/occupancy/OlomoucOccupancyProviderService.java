package cz.honzakasik.bazenolomouc.olomoucdataprovider.occupancy;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import cz.honzakasik.bazenolomouc.olomoucdataprovider.OlomoucUniversalTableParser;
import cz.honzakasik.bazenolomouc.olomoucdataprovider.SimpleWakefulBroadcastReceiver;

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
        Connection connection = Jsoup.connect(URL);
        try {
            Document document = connection.get();
            int occupancy = new OlomoucUniversalTableParser(document, CURRENT_OCCUPANCY_ROW_INDEX).parse();

            Intent dataIntent = new Intent();
            dataIntent.setAction(ACTION_OCCUPANCY_PROVIDED);
            dataIntent.putExtra(OCCUPANCY_EXTRA_KEY, occupancy);
            LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SimpleWakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }
}
