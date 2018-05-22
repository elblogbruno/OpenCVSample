package opencv_mobilenet.samples.opencv.org.opencvsample;

import android.content.Context;
import android.content.res.AssetManager;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

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

public class MainActivity extends Activity implements CvCameraViewListener2,CompoundButton.OnCheckedChangeListener{

    private static final String TAG = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;

    OpenCVDetector mOpenCVDetector;
    private Mat                    mRgba;
    private Mat                    mGray;
    private CascadeClassifier mJavaDetector;
    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;
    private float                  mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mButtonClose;
    private boolean is_detection_on = true;
    private Rect[] mRectFaces;
    private Mat faceMat;
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean CameraIndex = true;
    private String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

    Switch mySwitch = null;


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    try {

                       mOpenCVDetector.createFaceDetector(mAppContext);


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

        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    /** Called when the activity is first created. */




    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
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
                Log.i(TAG, "Camera button clicked");
                if(is_detection_on == false){
                    Toast.makeText(getApplication().getBaseContext(), "You need to turn on the face detector!",
                            Toast.LENGTH_LONG).show();
                } else {
                    if (is_detection_on == true) {
                        Log.i(TAG, "Making a photo!");
                        List<Mat> cropped_objects = mOpenCVDetector.cropObjects(mRgba, mRectFaces);
                        mOpenCVDetector.saveCroppedRois(cropped_objects);
                    } else {
                        Log.i(TAG, "Not making a photo!");
                        Toast.makeText(getApplication().getBaseContext(), "You need a face to take a photo!",
                                Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
        final Button button2 = findViewById(R.id.camera_change);
            button2.setOnClickListener(new View.OnClickListener() {
                                           public void onClick(View v) {
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

                                       });

        mySwitch = (Switch) findViewById(R.id.face_switch);
        mySwitch.setOnCheckedChangeListener( this);

    }


    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Toast.makeText(this, "Turning on face detection!", Toast.LENGTH_LONG).show();
            is_detection_on = true;
        } else {
            Toast.makeText(this, "Turning off face detection", Toast.LENGTH_LONG).show();
            is_detection_on = false;
        }
    }


    /*public Rect[] detectFaces(Mat image) {
        // run detector
        // store detected faces into -> mRectFaces
        // return false if no faces, true one or more faces
        MatOfRect faces = new MatOfRect();
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        }

        Rect[] facesArray = faces.toArray();
        return facesArray;
    }*/

    /*public Mat drawFaces(Mat image, Rect[] faces) {
        //Just a function that draws rectangles on the faces
        for (int i = 0; i < faces.length; i++) {
            //Log.i(TAG, "Drawing rectangles");
            Imgproc.rectangle(image, faces[i].tl(), faces[i].br(), FACE_RECT_COLOR, 3);
        }
        return image;
    }*/

    /*public List<Mat> cropObjects(Mat image, Rect [] rois) {
        Log.i(TAG, "Cropping objects");
        System.out.println(rois.length);

        List<Mat> list_rois = new ArrayList<>();
        for (int i = 0; i < rois.length; i++) {
            Mat cropped_roi = cropROI(image, rois[i],i);
            list_rois.add(cropped_roi);

        }
        return list_rois;
    }*/

    /*public void saveCroppedRois(List<Mat> list_rois){
        int id = 0;
        for (Mat roi : list_rois){
            Toast.makeText(getApplication().getBaseContext(), "Taking photo!",
                    Toast.LENGTH_LONG).show();

            Boolean bool = Imgcodecs.imwrite(Environment.getExternalStorageDirectory() + "/Images/" + timeStamp.toString() + "_Face_Crop" + id  + ".png", roi);

            if (bool)
                Log.i(TAG, "SUCCESS writing image to external storage");
            else
                Log.i(TAG, "Fail writing image to external storage");
            id++;
        }
    }*/

    /*public Mat cropROI(Mat image, Rect roi , int id) {
        Rect rectCrop = new Rect(roi.x, roi.y , roi.width, roi.height);  //Crops the face with x,y,width and height
        Mat image_roi_bgr = new Mat(image,rectCrop); //Saves the crop to a new mat called imageROI

        Mat image_roi_rgb = new Mat();
        Imgproc.cvtColor(image_roi_bgr, image_roi_rgb, Imgproc.COLOR_BGR2RGB); //change crop to rgba

        return image_roi_rgb;
    }*/


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
        if (is_detection_on){
            mRectFaces = mOpenCVDetector.detectFaces(mRgba);
            image_vis = mOpenCVDetector.drawFaces(image_vis, mRectFaces);
        }

        return image_vis;


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mButtonClose = menu.add("Close app");

        getMenuInflater();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mButtonClose)
            System.exit(0);
        Log.i(TAG, "Exit button clicked");

        return true;
    }


    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }



}