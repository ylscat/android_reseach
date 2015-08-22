package arbell.research.util;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.OverScroller;

/**
 * Created on 2015/8/22.
 */
public class ZoomController implements GestureListener.Callback, View.OnLayoutChangeListener {
    private Matrix mMatrix = new Matrix();
    private float[] mValues = new float[9];
    private ImageView mView;
    private Handler mHandler = new Handler();
    private Animator mAnim = new Animator();

    private Rect mScrollBounds = new Rect();
    private int mOverScrollLength;
    private int mViewWidth, mViewHeight, mDrawableWidth, mDrawableHeight;
    private OverScroller mScroller;
    private int mLastX, mLastY;
    private float mScale = 1, mLastScale;

    public ZoomController(ImageView view) {
        mView = view;
        view.addOnLayoutChangeListener(this);

        Context context = view.getContext();
        mScroller = new OverScroller(context, new DecelerateInterpolator());
        mOverScrollLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                50, context.getResources().getDisplayMetrics());
        mMatrix.getValues(mValues);
    }

    @Override
    public void down(int x, int y) {
        mView.getImageMatrix().getValues(mValues);
        mLastX = -1;
        mLastY = -1;
        stopFling();
    }

    @Override
    public void fling(float vx, float vy) {
        int sx = (int)mValues[Matrix.MTRANS_X];
        int sy = (int)mValues[Matrix.MTRANS_Y];
        int overX = mOverScrollLength;
        int overY = mOverScrollLength;
        Rect bounds = mScrollBounds;
        if(sx < bounds.left)
            overX += bounds.left - sx;
        else if(sx > bounds.right)
            overX += sx - bounds.right;

        if(sy < bounds.top)
            overY += bounds.top - sy;
        else if(sy > bounds.bottom)
            overY += sy - bounds.bottom;

        mScroller.fling(sx, sy,
                (int)vx, (int)vy,
                bounds.left,bounds.right, bounds.top, bounds.bottom,
                overX, overY);
        mHandler.post(mAnim);
    }

    @Override
    public void up() {
        checkOutOfEdge();
        if(mLastScale != 0) {
            mScale = mScale*mLastScale;
            mLastScale = 0;
        }
    }

    @Override
    public void scale(float startDistance, float currentDistance, int pivotX, int pivotY) {
        float scale = currentDistance/startDistance;
        mLastScale = scale;
        setScale(mScale * scale, pivotX, pivotY);
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
            scrollBy(dx, dy);
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        Drawable d = mView.getDrawable();
        int w = right - left;
        int h = bottom - top;

        mViewWidth = w;
        mViewHeight = h;
        mDrawableWidth = d.getBounds().width();
        mDrawableHeight = d.getBounds().height();

        int r = mDrawableWidth - w;
        int b = mDrawableHeight - h;

        int minX, maxX;
        if(r > 0) {
            minX = -r;
            maxX = 0;
        }
        else {
            minX = -r/2;
            maxX = -r/2;
        }

        int minY, maxY;
        if(b > 0) {
            minY = -b;
            maxY = 0;
        }
        else {
            minY = -b/2;
            maxY = -b/2;
        }

        mScrollBounds.set(minX, minY, maxX, maxY);
//        mMatrix.setTranslate(-r/2, -b/2);
//        mView.setImageMatrix(mMatrix);
        scrollTo(-r/2, -b/2);
    }

    public void scrollBy(int x, int y) {
        int newX = x + (int)mValues[Matrix.MTRANS_X];
        int newY = y + (int)mValues[Matrix.MTRANS_Y];
        Rect bounds = mScrollBounds;
        if(newX < bounds.left || newX > bounds.right) {
            newX -= x/2;
        }
        if(newY < bounds.top || newY > bounds.bottom) {
            newY -= y/2;
        }
        scrollTo(newX, newY);
    }

    public void setScale(float scale, int pivotX, int pivotY) {

        int w = mViewWidth;
        int h = mViewHeight;
        int r = (int)(mDrawableWidth*scale - w);
        int b = (int)(mDrawableHeight*scale - h);

        int minX, maxX;
        if(r > 0) {
            minX = -r;
            maxX = 0;
        }
        else {
            minX = -r/2;
            maxX = -r/2;
        }

        int minY, maxY;
        if(b > 0) {
            minY = -b;
            maxY = 0;
        }
        else {
            minY = -b/2;
            maxY = -b/2;
        }

        mScrollBounds.set(minX, minY, maxX, maxY);

        float oldScale = mValues[Matrix.MSCALE_X];
        mValues[Matrix.MSCALE_X] = scale;
        mValues[Matrix.MSCALE_Y] = scale;
        float sx = (mValues[Matrix.MTRANS_X] - pivotX)*scale/oldScale + (pivotX);
        float sy = (mValues[Matrix.MTRANS_Y] - pivotY)*scale/oldScale + (pivotY);

        scrollTo((int)sx, (int)sy);
    }

    public void stopFling() {
        if(!mScroller.isFinished())
            mScroller.abortAnimation();
    }

    public void checkOutOfEdge() {
        int sx = (int)mValues[Matrix.MTRANS_X];
        int sy = (int)mValues[Matrix.MTRANS_Y];
        int dx = 0, dy = 0;
        Rect bounds = mScrollBounds;
        if(sx < bounds.left)
            dx = bounds.left - sx;
        else if(sx > bounds.right)
            dx = bounds.right - sx;
        if(sy < bounds.top)
            dy = bounds.top - sy;
        else if(sy > bounds.bottom)
            dy = bounds.bottom - sy;
        if(dx != 0 || dy != 0) {
            double d = Math.sqrt(dx*dx + dy*dy);
            int duration = (int)(1000*Math.sqrt(2*d/2000f));
            mScroller.startScroll(sx, sy, dx, dy, duration);
            mHandler.post(mAnim);
        }
    }

    private void scrollTo(int x, int y) {
        mValues[Matrix.MTRANS_X] = x;
        mValues[Matrix.MTRANS_Y] = y;
        mMatrix.setValues(mValues);
        mView.setImageMatrix(mMatrix);
    }

    class Animator implements Runnable {
        @Override
        public void run() {
            if(mScroller.computeScrollOffset()) {
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();
                scrollTo(x, y);
                mHandler.post(this);
            }
        }
    }
}
