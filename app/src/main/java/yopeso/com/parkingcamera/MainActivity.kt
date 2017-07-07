package yopeso.com.parkingcamera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.flurgle.camerakit.CameraKit
import com.flurgle.camerakit.CameraListener
import com.flurgle.camerakit.CameraView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.yopeso.parkingclient.model.User
import yopeso.com.parkingcamera.BuildConfig.CAMERA_EMAIL
import yopeso.com.parkingcamera.BuildConfig.CAMERA_PWD
import org.joda.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), OnClickListener {

    private val CAPTURE_INTERVAL: Long = 10;
    private val CAPTURE_DELAY_INTERVAL: Long = 0;
    private val CAPTURE_START_TIME: LocalTime = LocalTime.parse("08:00:00.000");
    private val CAPTURE_END_TIME: LocalTime = LocalTime.parse("11:00:00.000");

    private var isStartStopUsed: Boolean = false

    private lateinit var storageRef: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    lateinit var cameraView: CameraView
    lateinit var previewImageView: ImageView
    lateinit var startStopButton: ImageView

    lateinit var scheduler: ScheduledExecutorService
    lateinit var cameraScheduledFuture: Future<*>
    private val schedulerRunnable: Runnable = Runnable {
        val nowTime = LocalTime.now()
        cameraView.captureImage()
        if (nowTime > CAPTURE_END_TIME && !isStartStopUsed) {
            stopCapturingPhotos()
            startCapturingPhotos((DateUtils.DAY_IN_MILLIS - nowTime.millisOfDay + CAPTURE_START_TIME.millisOfDay) /
                DateUtils.SECOND_IN_MILLIS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main)

        scheduler = Executors.newSingleThreadScheduledExecutor()

        cameraView = findViewById(R.id.camera_view) as CameraView
        previewImageView = findViewById(R.id.image_view_preview) as ImageView
        startStopButton = findViewById(R.id.start_stop_button) as ImageView
        startStopButton.setOnClickListener(this)

        cameraView.setMethod(CameraKit.Constants.METHOD_STANDARD)
        setCameraListener()
    }

    override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance().reference
        if (auth.currentUser == null) {
            signIn()
        }
    }

    override fun onDestroy() {
        scheduler.shutdown()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.start_stop_button ->
                if (!isStartStopUsed) {
                    isStartStopUsed = true
                    stopCapturingPhotos()
                    startCapturingPhotos(CAPTURE_DELAY_INTERVAL)
                } else {
                    isStartStopUsed = false
                    stopCapturingPhotos()
                }
        }
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
                auth.currentUser?.let {
                    val photoRef = storageRef.child("parking/omg.png")
                    val uploadTask = photoRef.putBytes(jpeg)
                    uploadTask.addOnFailureListener {
                        it.printStackTrace()
                        Toast.makeText(this@MainActivity, "Error to update", Toast.LENGTH_SHORT).show()
                    }.addOnSuccessListener {
                        Toast.makeText(this@MainActivity, "Picture updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCameraOpened() {
                super.onCameraOpened()
                startCapturingPhotos(CAPTURE_DELAY_INTERVAL)
            }

            override fun onCameraClosed() {
                super.onCameraClosed()
                stopCapturingPhotos()
            }
        })
    }

    private fun signIn() {
        auth.signInWithEmailAndPassword(CAMERA_EMAIL, CAMERA_PWD)
                .addOnFailureListener { it.printStackTrace() }
                .addOnCompleteListener {
                    task ->
                    writeNewUser(task.result.user.uid, task.result.user.email!!, task.result.user.email!!)
                }
    }

    private fun writeNewUser(userId: String, @Nullable name: String, email: String) {
        val user = User(name, email)
        database.child("users")?.child(userId)?.setValue(user)
    }

    fun startCapturingPhotos(startInterval: Long) {
        cameraScheduledFuture = scheduler.scheduleWithFixedDelay(schedulerRunnable, startInterval, CAPTURE_INTERVAL,
            TimeUnit.SECONDS)
    }

    private fun stopCapturingPhotos() {
        cameraScheduledFuture.cancel(true)
    }

}
