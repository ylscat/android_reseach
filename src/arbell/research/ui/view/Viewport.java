package arbell.research.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

/**
 * Created on 2015/8/19.
 */
public class Viewport extends ViewGroup {
    private Rect mScrollBounds = new Rect();
    private int mOverScrollLength;
    private OverScroller mScroller;

    public Viewport(Context context) {
        this(context, null);
    }

    public Viewport(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new OverScroller(context, new DecelerateInterpolator());
        mOverScrollLength = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                50, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(getChildCount() == 0)
            return;
        View child = getChildAt(0);
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        child.measure(spec, spec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(getChildCount() == 0)
            return;
        View child = getChildAt(0);
        int w = r - l;
        int h = b - t;
        int left = (w - child.getMeasuredWidth())/2;
        int top = (h - child.getMeasuredHeight())/2;
        child.layout(left, top,
                left + child.getMeasuredWidth(),
                top + child.getMeasuredHeight());
        int paddingX = Math.min(left, 0);
        int paddingY = Math.min(top, 0);
        mScrollBounds.set(paddingX, paddingY, -paddingX, -paddingY);
    }

    @Override
    public void scrollBy(int x, int y) {
        int newX = x + getScrollX();
        int newY = y + getScrollY();
        Rect bounds = mScrollBounds;
        if(newX < bounds.left || newX > bounds.right) {
            x /= 2;
        }
        if(newY < bounds.top || newY > bounds.bottom) {
            y /= 2;
        }
        super.scrollBy(x, y);
    }

    public void setScale(float scale, int pivotX, int pivotY) {
        View child = getChildAt(0);

        int w = getWidth();
        int h = getHeight();
        int left = (w - (int)(child.getMeasuredWidth()*scale))/2;
        int top = (h - (int)(child.getMeasuredHeight()*scale))/2;
        int paddingX = Math.min(left, 0);
        int paddingY = Math.min(top, 0);
        mScrollBounds.set(paddingX, paddingY, -paddingX, -paddingY);

        float oldScale = child.getScaleX();
        child.setScaleX(scale);
        child.setScaleY(scale);
        float sx = (getScrollX() + pivotX - w/2)*scale/oldScale - (pivotX - w/2);
        float sy = (getScrollY() + pivotY - h/2)*scale/oldScale - (pivotY - h/2);

        scrollTo((int) (sx ), (int) (sy ));
    }

    public void fling(float vx , float vy) {
        int sx = getScrollX();
        int sy = getScrollY();
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
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            scrollTo(x, y);
            postInvalidate();
        }
    }

    public void stopFling() {
        if(!mScroller.isFinished())
            mScroller.abortAnimation();
    }

    public void checkOutOfEdge() {
        int sx = getScrollX();
        int sy = getScrollY();
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
            postInvalidate();
        }
    }
}
