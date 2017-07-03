package ua.a5.newnotes;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by A5 Android Intern 2 on 30.06.2017.
 */

public class MyApplication extends MultiDexApplication {

    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }


    public static synchronized MyApplication getInstance() {
        return myApplication;
    }
}