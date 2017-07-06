package yopeso.com.parkingcamera

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.flurgle.camerakit.CameraKit
import com.flurgle.camerakit.CameraView

class MainActivity : AppCompatActivity() {

    lateinit var cameraView: CameraView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraView = findViewById(R.id.camera_view) as CameraView
        cameraView.setMethod(CameraKit.Constants.METHOD_STANDARD);
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

}
