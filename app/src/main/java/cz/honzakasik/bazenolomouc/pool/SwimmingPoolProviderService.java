package cz.honzakasik.bazenolomouc.pool;

import android.app.IntentService;

public abstract class SwimmingPoolProviderService extends IntentService {

    public static final String
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
