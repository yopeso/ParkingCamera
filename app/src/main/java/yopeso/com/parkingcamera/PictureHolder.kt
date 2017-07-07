package yopeso.com.parkingcamera

import android.graphics.Bitmap
import com.flurgle.camerakit.Size
import java.lang.ref.WeakReference

/**
 * @author james on 7/6/17.
 */
object PictureHolder {

    private var image: WeakReference<Bitmap>? = null
    var nativeCaptureSize: Size? = null
    var timeToCallback: Long = 0


    fun setImage(image: Bitmap?) {
        PictureHolder.image = if (image != null) WeakReference(image) else null
    }

    fun getImage(): Bitmap? {
        return if (image != null) image!!.get() else null
    }

    fun dispose() {
        setImage(null)
        nativeCaptureSize = null
        timeToCallback = 0
    }

}
