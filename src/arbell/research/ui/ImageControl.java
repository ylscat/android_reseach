package arbell.research.ui;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import arbell.research.util.GestureListener;
import arbell.research.util.ZoomController;

/**
 * Created on 2015/8/22.
 */
public class ImageControl extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AssetManager am = getAssets();
        Bitmap bitmap = null;
        try {
            InputStream is = am.open("images/periodic_table.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.MATRIX);
        iv.setImageBitmap(bitmap);
        setContentView(iv);
        GestureListener listener = new GestureListener(this);
        iv.setOnTouchListener(listener);
        listener.setCallback(new ZoomController(iv));
    }
}
