package cz.honzakasik.bazenolomouc.pool.olomoucpoolprovider;

import android.app.IntentService;
import android.content.Intent;

class OlomoucDownloadService extends IntentService {

    static final String URL_EXTRA_IDENTIFIER = "URL";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public OlomoucDownloadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        intent.getStringExtra(URL_EXTRA_IDENTIFIER);
    }
}
