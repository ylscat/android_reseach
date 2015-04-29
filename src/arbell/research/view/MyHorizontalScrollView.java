package arbell.research.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * @author YinLanshan
 *         creation time 2015/4/20.
 */
public class MyHorizontalScrollView extends HorizontalScrollView {
    private boolean mScrollable = true;

    public MyHorizontalScrollView(Context context) {
        super(context);
    }

    public MyHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mScrollable && super.onInterceptTouchEvent(ev);
    }

    public void setScrollable(boolean scrollable) {
        mScrollable = scrollable;
    }
}
