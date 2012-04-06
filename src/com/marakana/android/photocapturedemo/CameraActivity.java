
package com.marakana.android.photocapturedemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

public class CameraActivity extends Activity implements PictureCallback {
    private static final String TAG = "CameraRecorderDemoActivity";

    Camera camera;

    ImageButton takePictureButton;

    FrameLayout cameraPreviewFrame;

    CameraPreview cameraPreview;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.camera);
        this.cameraPreviewFrame = (FrameLayout)super.findViewById(R.id.camera_preview);
        this.takePictureButton = (ImageButton)super.findViewById(R.id.takePictureButton);
        this.takePictureButton.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // initialize the camera in background, as this may take a while
        new AsyncTask<Void, Void, Camera>() {

            @Override
            protected Camera doInBackground(Void... params) {
                try {
                    Camera camera = Camera.open();
                    return camera == null ? Camera.open(0) : camera;
                } catch (RuntimeException e) {
                    Log.wtf(TAG, "Failed to get camera", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Camera camera) {
                if (camera == null) {
                    Log.wtf(TAG, "Failed to get camera");
                    Toast.makeText(CameraActivity.this, "Failed to open camera", Toast.LENGTH_SHORT);
                } else {
                    CameraActivity.this.initCamera(camera);
                }
            }
        }.execute();
    }

    void initCamera(Camera camera) {
        // we now have the camera
        this.camera = camera;
        // create a preview for our camera
        this.cameraPreview = new CameraPreview(CameraActivity.this, this.camera);
        // add the preview to our preview frame
        this.cameraPreviewFrame.addView(this.cameraPreview, 0);
        this.takePictureButton.setEnabled(true);

        this.camera.getParameters().setJpegQuality(75);
        // optionally, use camera.setPreviewCallback(PreviewCallback) to get
        // each preview frame
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.camera != null) {
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
            this.cameraPreviewFrame.removeView(this.cameraPreview);
        }
    }

    // gets called by the button press
    public void takePicture(View v) {
        Log.d(TAG, "takePicture()");
        // record the picture as jpeg and notify us when done via onPictureTaken
        this.camera.takePicture(null, null, this);
    }

    // the data will come back in jpeg format
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "onPictureTaken()");
        String file = this.getFile();
        try {
            OutputStream out = new FileOutputStream(file);
            try {
                out.write(data);
            } finally {
                out.close();
            }
            Log.d(TAG, "Wrote picture to: " + file);
            Intent intent = new Intent(this, PhotoActivity.class);
            intent.putExtra("file", file);
            super.startActivity(intent);
        } catch (IOException e) {
            Log.d(TAG, "Failed to save picture", e);
        }
    }

    private String getFile() {
        File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), this
                        .getClass().getPackage().getName());
        if (!dir.exists() && !dir.mkdirs()) {
            Log.d(TAG, "Failed to create storage directory");
            return null;
        } else {
            return dir.getAbsolutePath() + File.separator
                    + new SimpleDateFormat("'IMG_'yyyyMMddHHmmss'.jpg'").format(new Date());
        }
    }
}
