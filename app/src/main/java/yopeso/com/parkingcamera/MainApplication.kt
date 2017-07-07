package yopeso.com.parkingcamera

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid

/**
 * Created by andreibacalu on 07/07/2017.
 */

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }
}
