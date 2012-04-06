
package com.marakana.android.photocapturedemo;

import java.io.File;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class PhotoViewActivity extends Activity {
    private static final String TAG = "PhotoViewActivity";

    private Uri uri;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.photo);
        this.uri = super.getIntent().getData();
        ImageView photo = (ImageView)super.findViewById(R.id.photo);
        Log.d(TAG, "Loading photo: " + this.uri);
        photo.setImageURI(this.uri);
    }

    // gets called by the button press
    public void back(View v) {
        Log.d(TAG, "Going back");
        super.finish();
    }

    // gets called by the button press
    public void delete(View v) {
        if (new File(this.uri.getPath()).delete()) {
            Log.d(TAG, "Deleted: " + this.uri);
        } else {
            Log.d(TAG, "Failed to delete: " + this.uri);
        }
        Log.d(TAG, "Going back");
        super.finish();
    }
}
