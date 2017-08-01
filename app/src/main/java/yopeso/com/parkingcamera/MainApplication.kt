package yopeso.com.parkingcamera

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import net.danlew.android.joda.JodaTimeAndroid



/**
 * Created by andreibacalu on 07/07/2017.
 */

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
        Fabric.with(this, Crashlytics())
    }
}
