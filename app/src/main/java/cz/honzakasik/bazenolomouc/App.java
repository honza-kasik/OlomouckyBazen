package cz.honzakasik.bazenolomouc;

import android.app.Application;

import com.anupcowkur.reservoir.Reservoir;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Reservoir.init(this, 2048);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
