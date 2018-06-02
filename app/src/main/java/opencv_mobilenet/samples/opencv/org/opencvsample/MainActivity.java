package opencv_mobilenet.samples.opencv.org.opencvsample;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.AssetManager;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Toolbar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int OBJECT_DETECTOR = 1;
    OpenCVDetector mOpenCVDetector;
    private Mat                    mRgba;
    private Mat                    mGray;
    private CascadeClassifier mJavaDetector;
    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;
    private float                  mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private MenuItem               mButtonClose;
    private MenuItem               mDetectorFace;
    private MenuItem               mDetectorObject;
    private Menu                   mMenu;
    private boolean is_detection_on = true;
    private Rect[] mRectFaces;
    private Mat faceMat;
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean CameraIndex = true;
    private String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

    Switch detector_state = null;


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    try {

                       mOpenCVDetector.createFaceDetector(mAppContext);
                       mOpenCVDetector.createAIDetector(mAppContext);


                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    public void FdActivity() {
        mDetectorName = new String[1];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[OBJECT_DETECTOR] = "Deep Network";
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    /** Called when the activity is first created. */


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        Log.i(TAG, "started" + String.valueOf(mDetectorName));
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCVDetector = new OpenCVDetector();

        final Button button1 = findViewById(R.id.CameraButton);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TakePhoto();
            }
        });




    }

    public void cameraSwap(){
        if (CameraIndex == true) {
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.setCameraIndex(1);
            mOpenCvCameraView.enableView();
            Toast.makeText(getApplication().getBaseContext(), "Changing to front camera!",
                    Toast.LENGTH_LONG).show();

            CameraIndex = false;
        } else {
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.setCameraIndex(0);
            mOpenCvCameraView.enableView();
            Toast.makeText(getApplication().getBaseContext(), "Changing to back camera!",
                    Toast.LENGTH_LONG).show();
            CameraIndex = true;
        }
    }
    public void DetectorSwap(String detection){
        if(detection == "object"){
            Toast.makeText(this, "Changing to object detection!", Toast.LENGTH_LONG).show();
            mDetectorType = OBJECT_DETECTOR;
            Log.i(TAG, "Changing to object detection");
            Log.i(TAG, "started" + String.valueOf(mDetectorName));
        }
        if (detection == "face"){
            Toast.makeText(this, "Changing to face detection!", Toast.LENGTH_LONG).show();
            mDetectorType = JAVA_DETECTOR;
            Log.i(TAG, "Changing to face detection");
            Log.i(TAG, "started" + String.valueOf(mDetectorName));
        }
    }
    public void faceSwitch(Menu menu){
        if (is_detection_on == true) {
            Toast.makeText(this, "Turning off face detection!", Toast.LENGTH_LONG).show();
            is_detection_on = false;
        } else {
            Toast.makeText(this, "Turning on face detection", Toast.LENGTH_LONG).show();
            is_detection_on = true;
        }
    }
    public void TakePhoto(){
        Log.i(TAG, "Camera button clicked");
        if(is_detection_on == false){
            Toast.makeText(getBaseContext(), "You need to turn on the face detector!",
                    Toast.LENGTH_LONG).show();
        } else {
            if (is_detection_on == true && mRectFaces.length > 0) {
                Log.i(TAG, "Making a photo!");
                List<Mat> cropped_objects = mOpenCVDetector.cropObjects(mRgba, mRectFaces);
                mOpenCVDetector.saveCroppedRois(cropped_objects);
                Toast.makeText(getBaseContext(), "I see a face over there!",
                        Toast.LENGTH_LONG).show();

            } else if(mRectFaces.length == 0 ) {
                Log.i(TAG, "Not making a photo!");
                Toast.makeText(getBaseContext(), "You need a face to take a photo!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {

    }



    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        Mat image_vis  = mRgba;
        if (is_detection_on && mDetectorType == JAVA_DETECTOR){
            mRectFaces = mOpenCVDetector.detectFaces(mRgba);
            image_vis = mOpenCVDetector.drawFaces(image_vis, mRectFaces);
        } else if(is_detection_on && mDetectorType == OBJECT_DETECTOR){
            mOpenCVDetector.detectFaces(mRgba);
        }

        return image_vis;


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i(TAG, "called onCreateOptionsMenu");
        mDetectorFace = menu.add("Face Detector");
        mDetectorObject = menu.add("Object Detector");


        mButtonClose = menu.add("Close app");
        mMenu = menu;
        getMenuInflater();
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_action, menu);
        return super.onCreateOptionsMenu(menu);


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mButtonClose)
            System.exit(0);
        else if (item == mDetectorFace)
            DetectorSwap("face");
        else if (item == mDetectorObject)
            DetectorSwap("object");


        switch (item.getItemId()) {
            case R.id.camera_change:
                cameraSwap();
                return true;
            case R.id.face_switch:
                faceSwitch(mMenu);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }


    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }



}