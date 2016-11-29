package cz.honzakasik.bazenolomouc.pool;

import android.app.IntentService;

public abstract class SwimmingPoolProviderService extends IntentService {

    public static final String
            ACTION_SWIMMING_POOL_DOWNLOADED = "DOWNLOAD_DONE",
            ACTION_ERROR_OCCURRED_IN_PROVIDER_SERVICE = "ERROR_OCCURRED_IN_POOL_PROVIDER_SERVICE",
            ERROR_MESSAGE_EXTRA_IDENTIFIER = "ERROR_MESSAGE",
            SWIMMING_POOL_EXTRA_IDENTIFIER = "SWIMMING_POOL",
            DATETIME_EXTRA_IDENTIFIER = "DATETIME";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SwimmingPoolProviderService(String name) {
        super(name);
    }
}
