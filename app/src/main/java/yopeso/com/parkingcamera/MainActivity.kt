package yopeso.com.parkingcamera

import android.app.job.JobScheduler
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.flurgle.camerakit.CameraKit
import com.flurgle.camerakit.CameraListener
import com.flurgle.camerakit.CameraView
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var cameraView: CameraView

    lateinit var previewImageView: ImageView

    lateinit var scheduler: ScheduledExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main)

        scheduler = Executors.newSingleThreadScheduledExecutor()

        cameraView = findViewById(R.id.camera_view) as CameraView
        previewImageView = findViewById(R.id.image_view_preview) as ImageView
        //todo : add button stop/start scheduler functionality

        cameraView.setMethod(CameraKit.Constants.METHOD_STANDARD);
        setCameraListener()
    }

    override fun onStart() {
        super.onStart()
    }

    fun schedulePicture() {
        scheduler.scheduleAtFixedRate({
            cameraView.captureImage()
        }, 10, 10, TimeUnit.SECONDS)
    }

    fun setCameraListener() {
        cameraView.setCameraListener(object : CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                super.onPictureTaken(jpeg)
                val bitmap: Bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg?.size as Int)
                PictureHolder.dispose()
                PictureHolder.setImage(bitmap)
                PictureHolder.nativeCaptureSize = cameraView.captureSize
                previewImageView.setImageBitmap(bitmap)
                Toast.makeText(this@MainActivity, "Picture taken!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
        schedulePicture()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

}
