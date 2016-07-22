package arbell.research.ui;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import arbell.research.util.GestureListener;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created on 2015/8/20.
 */
public class ViewportActivity extends Activity implements GestureListener.Callback {
    private arbell.research.ui.view.Viewport mViewport;
    private int mLastX, mLastY;
    private float mScale = 1, mLastScale;

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
//        bitmap = Bitmap.createBitmap(6400, 4800, Bitmap.Config.ARGB_8888);
//        bitmap.eraseColor(Color.MAGENTA);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(bitmap);
        iv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        arbell.research.ui.view.Viewport vp = new arbell.research.ui.view.Viewport(this);
        vp.addView(iv, WRAP_CONTENT, WRAP_CONTENT);
        setContentView(vp);
        mViewport = vp;
        GestureListener gl = new GestureListener(this);
        gl.setCallback(this);
        vp.setOnTouchListener(gl);
    }

    @Override
    public void down(int x, int y) {
        mLastX = -1;
        mLastY = -1;
        mViewport.stopFling();
    }

    @Override
    public void fling(float vx, float vy) {
        mViewport.fling(-vx, -vy);
    }

    @Override
    public void up() {
        mViewport.checkOutOfEdge();
        if(mLastScale != 0) {
            mScale = mScale*mLastScale;
            mLastScale = 0;
        }
    }

    @Override
    public void scale(float startDistance, float currentDistance, int pivotX, int pivotY) {
        float scale = currentDistance/startDistance;
        mLastScale = scale;
        mViewport.setScale(mScale*scale, pivotX, pivotY);
    }

    @Override
    public void scroll(int startX, int startY, int x, int y) {
        if(startX == x && startY == y) {
            mLastX = x;
            mLastY = y;
            return;
        }

        if(mLastX != x || mLastY != y) {
            int dx = x - mLastX;
            int dy = y - mLastY;
            mLastX = x;
            mLastY = y;
            mViewport.scrollBy(-dx, -dy);
        }
    }
}
