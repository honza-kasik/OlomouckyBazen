package cz.honzakasik.bazenolomouc.olomoucdataprovider.occupancy;

import android.app.IntentService;
import android.content.Intent;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class OlomoucOccupancyProviderService extends IntentService {

    private static final String URL = "http://olterm.cz/get_obsazenost.php";

    private static final String CURRENT_OCCUPANCY_KEY = "";

    public OlomoucOccupancyProviderService() {
        this("Olomouc occupancy provider service");
    }

    public OlomoucOccupancyProviderService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Connection connection = Jsoup.connect(URL);
        try {
            Document document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
