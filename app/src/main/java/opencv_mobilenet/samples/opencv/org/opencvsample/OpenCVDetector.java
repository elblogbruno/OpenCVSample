package opencv_mobilenet.samples.opencv.org.opencvsample;

import android.app.Activity;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;


import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


import org.opencv.core.Rect;

import static android.os.Environment.DIRECTORY_PICTURES;

public class OpenCVDetector implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::OpenCVDetector";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;


    private OpenCVDetector mOpenCVDetector;
    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;
    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private boolean is_detection_on = true;
    private Rect[] mRectFaces;
    private CameraBridgeViewBase mOpenCvCameraView;
    private String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());




    public void FdActivity() {
        mDetectorName = new String[1];
        mDetectorName[JAVA_DETECTOR] = "Java";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }



    public Rect[] detectFaces(Mat image) {
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
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        }

        Rect[] facesArray = faces.toArray();
        return facesArray;
    }

    public Mat drawFaces(Mat image, Rect[] faces) {
        //Just a function that draws rectangles on the faces
        for (int i = 0; i < faces.length; i++) {
            //Log.i(TAG, "Drawing rectangles");
            Imgproc.rectangle(image, faces[i].tl(), faces[i].br(), FACE_RECT_COLOR, 3);
        }
        return image;
    }

    public List<Mat> cropObjects(Mat image, Rect[] rois) {
        Log.i(TAG, "Cropping objects");
        System.out.println(rois.length);

        List<Mat> list_rois = new ArrayList<>();
        for (int i = 0; i < rois.length; i++) {
            Mat cropped_roi = cropROI(image, rois[i], i);
            list_rois.add(cropped_roi);

        }
        return list_rois;
    }

    public void saveCroppedRois(List<Mat> list_rois) {
        int id = 0;
        for (Mat roi : list_rois) {
            Boolean bool = Imgcodecs.imwrite(Environment.getExternalStorageDirectory() + "/Images/" + timeStamp.toString() + "_Face_Crop" + id + ".png", roi);

            if (bool)
                Log.i(TAG, "SUCCESS writing image to external storage");
            else
                Log.i(TAG, "Fail writing image to external storage");
            id++;
        }
    }

    public Mat cropROI(Mat image, Rect roi, int id) {
        Rect rectCrop = new Rect(roi.x, roi.y, roi.width, roi.height);  //Crops the face with x,y,width and height
        Mat image_roi_bgr = new Mat(image, rectCrop); //Saves the crop to a new mat called imageROI
        Mat image_roi_rgb = new Mat();
        Imgproc.cvtColor(image_roi_bgr, image_roi_rgb, Imgproc.COLOR_BGR2RGB); //change crop to rgba
        return image_roi_rgb;
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        Mat image_vis = mRgba;
        if (is_detection_on) {
            mRectFaces = detectFaces(mRgba);
            image_vis = drawFaces(image_vis, mRectFaces);
        }

        return image_vis;


    }

    public void onCameraViewStarted(int width, int height) {

    }
    public void onCameraViewStopped() {

    }

}

