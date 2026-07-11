package dev.omar.termo;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import dev.omar.common.utils.XEnvironment;
import dev.omar.termo.crash.CrashActivity;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashActivity.initCrashHandler(this);
        Utils.init(this);
        XEnvironment.init(this);
    }
}
