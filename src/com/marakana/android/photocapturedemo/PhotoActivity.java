
package com.marakana.android.photocapturedemo;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class PhotoActivity extends Activity {
    private static final String TAG = "PhotoActivity";

    private String file;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.photo);
        this.file = super.getIntent().getStringExtra("file");
        ImageView photo = (ImageView)super.findViewById(R.id.photo);
        Log.d(TAG, "Loading photo: " + file);
        Bitmap bitmap = BitmapFactory.decodeFile(this.file);
        photo.setImageBitmap(bitmap);
    }

    // gets called by the button press
    public void back(View v) {
        Log.d(TAG, "back()");
        super.finish();
    }

    // gets called by the button press
    public void delete(View v) {
        Log.d(TAG, "delete()");
        new File(file).delete();
        super.finish();
    }
}
