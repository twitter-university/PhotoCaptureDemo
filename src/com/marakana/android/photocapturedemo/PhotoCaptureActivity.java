
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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

public class PhotoCaptureActivity extends Activity implements PictureCallback, LocationListener {
    private static final String TAG = "PhotoCaptureActivity";

    Camera camera;

    ImageButton takePictureButton;

    FrameLayout cameraPreviewFrame;

    CameraPreview cameraPreview;

    LocationManager locationManager;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.camera);
        this.cameraPreviewFrame = (FrameLayout)super.findViewById(R.id.camera_preview);
        this.takePictureButton = (ImageButton)super.findViewById(R.id.takePictureButton);
        this.takePictureButton.setEnabled(false);
        this.locationManager = (LocationManager)super.getSystemService(LOCATION_SERVICE);
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
                    Toast.makeText(PhotoCaptureActivity.this, "Failed to open camera",
                            Toast.LENGTH_SHORT);
                } else {
                    PhotoCaptureActivity.this.initCamera(camera);
                }
            }
        }.execute();
    }

    // gets called from onResume()'s AsyncTask
    void initCamera(Camera camera) {
        // we now have the camera
        this.camera = camera;
        // create a preview for our camera
        this.cameraPreview = new CameraPreview(PhotoCaptureActivity.this, this.camera);
        // add the preview to our preview frame
        this.cameraPreviewFrame.addView(this.cameraPreview, 0);
        this.takePictureButton.setEnabled(true);

        this.camera.getParameters().setJpegQuality(75);
        // optionally, use camera.setPreviewCallback(PreviewCallback) to get
        // each preview frame

        // we also want to configure location details with our camera, but we
        // first need to request location data
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            Log.d(TAG, "No good location provider is available");
        } else {
            Log.d(TAG, "Set the location provider to " + provider);
            this.onLocationChanged(this.locationManager.getLastKnownLocation(provider));
            this.locationManager.requestLocationUpdates(provider, 1000, 100, this);
            // the updates will be given to us via onLocationChanged
        }
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
        this.locationManager.removeUpdates(this);
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
        File file = this.getFile();
        try {
            OutputStream out = new FileOutputStream(file);
            try {
                out.write(data);
            } finally {
                out.close();
            }
            Log.d(TAG, "Wrote picture to: " + file.getAbsolutePath());
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(this, PhotoViewActivity.class);
            intent.setData(uri);
            super.startActivity(intent);
        } catch (IOException e) {
            Log.d(TAG, "Failed to save picture", e);
        }
    }

    private File getFile() {
        File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), this
                        .getClass().getPackage().getName());
        if (!dir.exists() && !dir.mkdirs()) {
            Log.d(TAG, "Failed to create storage directory");
            return null;
        } else {
            return new File(dir.getAbsolutePath(), new SimpleDateFormat(
                    "'IMG_'yyyyMMddHHmmss'.jpg'").format(new Date()));
        }
    }

    public void onLocationChanged(Location location) {
        if (this.camera != null && location != null) {
            if (location.hasAccuracy() && location.getAccuracy() < 500
                    && location.getTime() < System.currentTimeMillis() - (30 * 60 * 1000)) {
                Log.d(TAG, "Ignoring inaccurate or stale location: " + location);
            } else {
                Log.d(TAG, "Setting camera location: " + location);
                this.camera.getParameters().setGpsLatitude(location.getLatitude());
                this.camera.getParameters().setGpsLongitude(location.getLongitude());
                this.camera.getParameters().setGpsAltitude(location.getAltitude());
                this.camera.getParameters().setGpsTimestamp(location.getTime());
            }
        } else {
            Log.d(TAG, "No camera or location. Cannot configure the camera.");
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // ignored
    }

    public void onProviderEnabled(String provider) {
        // ignored
    }

    public void onProviderDisabled(String provider) {
        // ignored
    }
}
