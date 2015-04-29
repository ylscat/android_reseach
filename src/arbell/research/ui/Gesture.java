package arbell.research.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * Author: YinLanShan
 * Date: 14-2-7
 * Time: 13:55
 */
public class Gesture extends Activity implements View.OnTouchListener,
        Detector.OnGestureListener
{
    private Detector mGestureDetector;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTextAppearance(this, android.R.style.TextAppearance_Large);
        setContentView(tv);
        tv.setOnTouchListener(this);
        mTextView = tv;

        mGestureDetector = new Detector(this, this);
    }

    //Implementation of OnTouchListener
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        return mGestureDetector.onTouchEvent(event);
    }

    enum GestureState
    {
        onDown,
        onShowPress,
        onSingleTapUp,
        onScroll,
        onLongPress,
        onFling
    }

    private LinkedList<Integer> mStateHistory = new LinkedList<Integer>();
    private StringBuilder mStateHistoryText = new StringBuilder();
    private static final int HISTORY_TRACE_DEPTH = 10;
    private GestureState mPreviousState1 = null;

    //Implementation of OnGestureListener
    @Override
    public boolean onDown(MotionEvent e)
    {
        if (mPreviousState1 != GestureState.onDown)
        {
            mPreviousState1 = GestureState.onDown;
            if (mStateHistory.size() == HISTORY_TRACE_DEPTH)
            {
                int len = mStateHistory.removeFirst();
                mStateHistoryText.delete(0, len);
            }

            final String TEXT = "onDown\n";
            mStateHistory.add(TEXT.length());
            mStateHistoryText.append(TEXT);
            mTextView.setText(mStateHistoryText);
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {
        if (mPreviousState1 != GestureState.onShowPress)
        {
            mPreviousState1 = GestureState.onShowPress;
            if (mStateHistory.size() == HISTORY_TRACE_DEPTH)
            {
                int len = mStateHistory.removeFirst();
                mStateHistoryText.delete(0, len);
            }

            final String TEXT = "onShowPress\n";
            mStateHistory.add(TEXT.length());
            mStateHistoryText.append(TEXT);
            mTextView.setText(mStateHistoryText);
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        if (mPreviousState1 != GestureState.onSingleTapUp)
        {
            mPreviousState1 = GestureState.onSingleTapUp;
            if (mStateHistory.size() == HISTORY_TRACE_DEPTH)
            {
                int len = mStateHistory.removeFirst();
                mStateHistoryText.delete(0, len);
            }

            final String TEXT = "onSingleTapUp\n";
            mStateHistory.add(TEXT.length());
            mStateHistoryText.append(TEXT);
            mTextView.setText(mStateHistoryText);
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        if (mPreviousState1 != GestureState.onScroll)
        {
            mPreviousState1 = GestureState.onScroll;
            if (mStateHistory.size() == HISTORY_TRACE_DEPTH)
            {
                int len = mStateHistory.removeFirst();
                mStateHistoryText.delete(0, len);
            }

            final String TEXT = "onScroll\n";
            mStateHistory.add(TEXT.length());
            mStateHistoryText.append(TEXT);
            mTextView.setText(mStateHistoryText);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
        if (mPreviousState1 != GestureState.onLongPress)
        {
            mPreviousState1 = GestureState.onLongPress;
            if (mStateHistory.size() == HISTORY_TRACE_DEPTH)
            {
                int len = mStateHistory.removeFirst();
                mStateHistoryText.delete(0, len);
            }

            final String TEXT = "onLongPress\n";
            mStateHistory.add(TEXT.length());
            mStateHistoryText.append(TEXT);
            mTextView.setText(mStateHistoryText);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        if (mPreviousState1 != GestureState.onFling)
        {
            mPreviousState1 = GestureState.onFling;
            if (mStateHistory.size() == HISTORY_TRACE_DEPTH)
            {
                int len = mStateHistory.removeFirst();
                mStateHistoryText.delete(0, len);
            }

            final String TEXT = "onFling\n";
            mStateHistory.add(TEXT.length());
            mStateHistoryText.append(TEXT);
            mTextView.setText(mStateHistoryText);
        }
        return true;
    }
}

class Detector
{
    /**
     * The listener that is used to notify when gestures occur.
     * If you want to listen for all the different gestures then implement
     * this interface. If you only want to listen for a subset it might
     * be easier to extend {@link SimpleOnGestureListener}.
     */
    public interface OnGestureListener
    {

        /**
         * Notified when a tap occurs with the down {@link MotionEvent}
         * that triggered it. This will be triggered immediately for
         * every down event. All other events should be preceded by this.
         *
         * @param e The down motion event.
         */
        boolean onDown(MotionEvent e);

        /**
         * The user has performed a down {@link MotionEvent} and not performed
         * a move or up yet. This event is commonly used to provide visual
         * feedback to the user to let them know that their action has been
         * recognized i.e. highlight an element.
         *
         * @param e The down motion event
         */
        void onShowPress(MotionEvent e);

        /**
         * Notified when a tap occurs with the up {@link MotionEvent}
         * that triggered it.
         *
         * @param e The up motion event that completed the first tap
         * @return true if the event is consumed, else false
         */
        boolean onSingleTapUp(MotionEvent e);

        /**
         * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the
         * current move {@link MotionEvent}. The distance in x and y is also supplied for
         * convenience.
         *
         * @param e1        The first down motion event that started the scrolling.
         * @param e2        The move motion event that triggered the current onScroll.
         * @param distanceX The distance along the X axis that has been scrolled since the last
         *                  call to onScroll. This is NOT the distance between {@code e1}
         *                  and {@code e2}.
         * @param distanceY The distance along the Y axis that has been scrolled since the last
         *                  call to onScroll. This is NOT the distance between {@code e1}
         *                  and {@code e2}.
         * @return true if the event is consumed, else false
         */
        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

        /**
         * Notified when a long press occurs with the initial on down {@link MotionEvent}
         * that trigged it.
         *
         * @param e The initial on down motion event that started the longpress.
         */
        void onLongPress(MotionEvent e);

        /**
         * Notified of a fling event when it occurs with the initial on down {@link MotionEvent}
         * and the matching up {@link MotionEvent}. The calculated velocity is supplied along
         * the x and y axis in pixels per second.
         *
         * @param e1        The first down motion event that started the fling.
         * @param e2        The move motion event that triggered the current onFling.
         * @param velocityX The velocity of this fling measured in pixels per second
         *                  along the x axis.
         * @param velocityY The velocity of this fling measured in pixels per second
         *                  along the y axis.
         * @return true if the event is consumed, else false
         */
        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
    }

    /**
     * The listener that is used to notify when a double-tap or a confirmed
     * single-tap occur.
     */
    public interface OnDoubleTapListener
    {
        /**
         * Notified when a single-tap occurs.
         * <p/>
         * Unlike {@link OnGestureListener#onSingleTapUp(MotionEvent)}, this
         * will only be called after the detector is confident that the user's
         * first tap is not followed by a second tap leading to a double-tap
         * gesture.
         *
         * @param e The down motion event of the single-tap.
         * @return true if the event is consumed, else false
         */
        boolean onSingleTapConfirmed(MotionEvent e);

        /**
         * Notified when a double-tap occurs.
         *
         * @param e The down motion event of the first tap of the double-tap.
         * @return true if the event is consumed, else false
         */
        boolean onDoubleTap(MotionEvent e);

        /**
         * Notified when an event within a double-tap gesture occurs, including
         * the down, move, and up events.
         *
         * @param e The motion event that occurred during the double-tap gesture.
         * @return true if the event is consumed, else false
         */
        boolean onDoubleTapEvent(MotionEvent e);
    }

    /**
     * A convenience class to extend when you only want to listen for a subset
     * of all the gestures. This implements all methods in the
     * {@link OnGestureListener} and {@link OnDoubleTapListener} but does
     * nothing and return {@code false} for all applicable methods.
     */
    public static class SimpleOnGestureListener implements OnGestureListener, OnDoubleTapListener
    {
        public boolean onSingleTapUp(MotionEvent e)
        {
            return false;
        }

        public void onLongPress(MotionEvent e)
        {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY)
        {
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY)
        {
            return false;
        }

        public void onShowPress(MotionEvent e)
        {
        }

        public boolean onDown(MotionEvent e)
        {
            return false;
        }

        public boolean onDoubleTap(MotionEvent e)
        {
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent e)
        {
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            return false;
        }
    }

    private int mTouchSlopSquare;
    private int mDoubleTapTouchSlopSquare;
    private int mDoubleTapSlopSquare;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;

    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private static final int DOUBLE_TAP_MIN_TIME = DOUBLE_TAP_TIMEOUT;

    // constants for Message.what used by GestureHandler below
    private static final int SHOW_PRESS = 1;
    private static final int LONG_PRESS = 2;
    private static final int TAP = 3;

    private final Handler mHandler;
    private final OnGestureListener mListener;
    private OnDoubleTapListener mDoubleTapListener;

    private boolean mStillDown;
    private boolean mDeferConfirmSingleTap;
    private boolean mInLongPress;
    private boolean mAlwaysInTapRegion;
    private boolean mAlwaysInBiggerTapRegion;

    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;

    /**
     * True when the user is still touching for the second tap (down, move, and
     * up events). Can only be true if there is a double tap listener attached.
     */
    private boolean mIsDoubleTapping;

    private float mLastFocusX;
    private float mLastFocusY;
    private float mDownFocusX;
    private float mDownFocusY;

    private boolean mIsLongpressEnabled;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    private class GestureHandler extends Handler
    {
        GestureHandler()
        {
            super();
        }

        GestureHandler(Handler handler)
        {
            super(handler.getLooper());
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case SHOW_PRESS:
                    mListener.onShowPress(mCurrentDownEvent);
                    break;

                case LONG_PRESS:
                    dispatchLongPress();
                    break;

                case TAP:
                    // If the user's finger is still down, do not count it as a tap
                    if (mDoubleTapListener != null)
                    {
                        if (!mStillDown)
                        {
                            mDoubleTapListener.onSingleTapConfirmed(mCurrentDownEvent);
                        } else
                        {
                            mDeferConfirmSingleTap = true;
                        }
                    }
                    break;

                default:
                    throw new RuntimeException("Unknown message " + msg); //never
            }
        }
    }

    /**
     * Creates a GestureDetector with the supplied listener.
     * You may only use this constructor from a {@link android.os.Looper} thread.
     *
     * @param context  the application's context
     * @param listener the listener invoked for all the callbacks, this must
     *                 not be null.
     * @throws NullPointerException if {@code listener} is null.
     * @see Handler#Handler()
     */
    public Detector(Context context, OnGestureListener listener)
    {
        this(context, listener, null);
    }

    /**
     * Creates a GestureDetector with the supplied listener that runs deferred events on the
     * thread associated with the supplied {@link Handler}.
     *
     * @param context  the application's context
     * @param listener the listener invoked for all the callbacks, this must
     *                 not be null.
     * @param handler  the handler to use for running deferred listener events.
     * @throws NullPointerException if {@code listener} is null.
     * @see Handler#Handler()
     */
    public Detector(Context context, OnGestureListener listener, Handler handler)
    {
        if (handler != null)
        {
            mHandler = new GestureHandler(handler);
        } else
        {
            mHandler = new GestureHandler();
        }
        mListener = listener;
        if (listener instanceof OnDoubleTapListener)
        {
            setOnDoubleTapListener((OnDoubleTapListener) listener);
        }
        init(context);
    }

    /**
     * Creates a GestureDetector with the supplied listener that runs deferred events on the
     * thread associated with the supplied {@link Handler}.
     *
     * @param context  the application's context
     * @param listener the listener invoked for all the callbacks, this must
     *                 not be null.
     * @param handler  the handler to use for running deferred listener events.
     * @param unused   currently not used.
     * @throws NullPointerException if {@code listener} is null.
     * @see Handler#Handler()
     */
    public Detector(Context context, OnGestureListener listener, Handler handler,
                    boolean unused)
    {
        this(context, listener, handler);
    }

    private void init(Context context)
    {
        if (mListener == null)
        {
            throw new NullPointerException("OnGestureListener must not be null");
        }
        mIsLongpressEnabled = true;

        // Fallback to support pre-donuts releases
        int touchSlop, doubleTapSlop, doubleTapTouchSlop;
        if (context == null)
        {
            //noinspection deprecation
            touchSlop = ViewConfiguration.getTouchSlop();
            doubleTapTouchSlop = touchSlop; // Hack rather than adding a hiden method for this
            doubleTapSlop = touchSlop;//ViewConfiguration.getDoubleTapSlop();
            //noinspection deprecation
            mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
        } else
        {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            touchSlop = configuration.getScaledTouchSlop();
            doubleTapTouchSlop = touchSlop;//configuration.getScaledDoubleTapTouchSlop();
            doubleTapSlop = configuration.getScaledDoubleTapSlop();
            mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
            mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        }
        mTouchSlopSquare = touchSlop * touchSlop;
        mDoubleTapTouchSlopSquare = doubleTapTouchSlop * doubleTapTouchSlop;
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
    }

    /**
     * Sets the listener which will be called for double-tap and related
     * gestures.
     *
     * @param onDoubleTapListener the listener invoked for all the callbacks, or
     *                            null to stop listening for double-tap gestures.
     */
    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener)
    {
        mDoubleTapListener = onDoubleTapListener;
    }

    /**
     * Set whether longpress is enabled, if this is enabled when a user
     * presses and holds down you get a longpress event and nothing further.
     * If it's disabled the user can press and hold down and then later
     * moved their finger and you will get scroll events. By default
     * longpress is enabled.
     *
     * @param isLongpressEnabled whether longpress should be enabled.
     */
    public void setIsLongpressEnabled(boolean isLongpressEnabled)
    {
        mIsLongpressEnabled = isLongpressEnabled;
    }

    /**
     * @return true if longpress is enabled, else false.
     */
    public boolean isLongpressEnabled()
    {
        return mIsLongpressEnabled;
    }

    /**
     * Analyzes the given motion event and if applicable triggers the
     * appropriate callbacks on the {@link OnGestureListener} supplied.
     *
     * @param ev The current motion event.
     * @return true if the {@link OnGestureListener} consumed the event,
     * else false.
     */
    public boolean onTouchEvent(MotionEvent ev)
    {

        final int action = ev.getAction();

        if (mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final boolean pointerUp =
                (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? ev.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int count = ev.getPointerCount();
        for (int i = 0; i < count; i++)
        {
            if (skipIndex == i) continue;
            sumX += ev.getX(i);
            sumY += ev.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        final float focusX = sumX / div;
        final float focusY = sumY / div;

        boolean handled = false;
        int index = ev.getActionIndex();
        int id = ev.getPointerId(index);

        switch (action & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_POINTER_DOWN:
                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;
                // Cancel long press and taps
                cancelTaps();
                android.util.Log.d("touch", String.format("touch PD index:%d id:%d", index, id));
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;
                android.util.Log.d("touch", String.format("touch PU index:%d id:%d", index, id));

                // Check the dot product of current velocities.
                // If the pointer that left was opposing another velocity vector, clear.
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final int upIndex = ev.getActionIndex();
                final int id1 = ev.getPointerId(upIndex);
                final float x1 = mVelocityTracker.getXVelocity(id1);
                final float y1 = mVelocityTracker.getYVelocity(id1);
                for (int i = 0; i < count; i++)
                {
                    if (i == upIndex) continue;

                    final int id2 = ev.getPointerId(i);
                    final float x = x1 * mVelocityTracker.getXVelocity(id2);
                    final float y = y1 * mVelocityTracker.getYVelocity(id2);

                    final float dot = x + y;
                    if (dot < 0)
                    {
                        mVelocityTracker.clear();
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_DOWN:
                if (mDoubleTapListener != null)
                {
                    boolean hadTapMessage = mHandler.hasMessages(TAP);
                    if (hadTapMessage) mHandler.removeMessages(TAP);
                    if ((mCurrentDownEvent != null) && (mPreviousUpEvent != null) && hadTapMessage &&
                            isConsideredDoubleTap(mCurrentDownEvent, mPreviousUpEvent, ev))
                    {
                        // This is a second tap
                        mIsDoubleTapping = true;
                        // Give a callback with the first tap of the double-tap
                        handled |= mDoubleTapListener.onDoubleTap(mCurrentDownEvent);
                        // Give a callback with down event of the double-tap
                        handled |= mDoubleTapListener.onDoubleTapEvent(ev);
                    } else
                    {
                        // This is a first tap
                        mHandler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT);
                    }
                }

                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;
                android.util.Log.d("touch", String.format("touch D index:%d id:%d", index, id));
                if (mCurrentDownEvent != null)
                {
                    mCurrentDownEvent.recycle();
                }
                mCurrentDownEvent = MotionEvent.obtain(ev);
                mAlwaysInTapRegion = true;
                mAlwaysInBiggerTapRegion = true;
                mStillDown = true;
                mInLongPress = false;
                mDeferConfirmSingleTap = false;

                if (mIsLongpressEnabled)
                {
                    mHandler.removeMessages(LONG_PRESS);
                    mHandler.sendEmptyMessageAtTime(LONG_PRESS, mCurrentDownEvent.getDownTime()
                            + TAP_TIMEOUT + LONGPRESS_TIMEOUT);
                }
                mHandler.sendEmptyMessageAtTime(SHOW_PRESS, mCurrentDownEvent.getDownTime() + TAP_TIMEOUT);
                handled |= mListener.onDown(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mInLongPress)
                {
                    break;
                }
                final float scrollX = mLastFocusX - focusX;
                final float scrollY = mLastFocusY - focusY;
                if (mIsDoubleTapping)
                {
                    // Give the move events of the double-tap
                    handled |= mDoubleTapListener.onDoubleTapEvent(ev);
                } else if (mAlwaysInTapRegion)
                {
                    final int deltaX = (int) (focusX - mDownFocusX);
                    final int deltaY = (int) (focusY - mDownFocusY);
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    if (distance > mTouchSlopSquare)
                    {
                        handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY);
                        mLastFocusX = focusX;
                        mLastFocusY = focusY;
                        mAlwaysInTapRegion = false;
                        mHandler.removeMessages(TAP);
                        mHandler.removeMessages(SHOW_PRESS);
                        mHandler.removeMessages(LONG_PRESS);
                    }
                    if (distance > mDoubleTapTouchSlopSquare)
                    {
                        mAlwaysInBiggerTapRegion = false;
                    }
                } else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1))
                {
                    handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY);
                    mLastFocusX = focusX;
                    mLastFocusY = focusY;
                }
                break;

            case MotionEvent.ACTION_UP:
                mStillDown = false;
                MotionEvent currentUpEvent = MotionEvent.obtain(ev);
                if (mIsDoubleTapping)
                {
                    // Finally, give the up event of the double-tap
                    handled |= mDoubleTapListener.onDoubleTapEvent(ev);
                } else if (mInLongPress)
                {
                    mHandler.removeMessages(TAP);
                    mInLongPress = false;
                } else if (mAlwaysInTapRegion)
                {
                    handled = mListener.onSingleTapUp(ev);
                    if (mDeferConfirmSingleTap && mDoubleTapListener != null)
                    {
                        mDoubleTapListener.onSingleTapConfirmed(ev);
                    }
                } else
                {

                    // A fling must travel the minimum tap distance
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    final int pointerId = ev.getPointerId(0);
                    android.util.Log.d("touch", String.format("touch U index:%d id:%d", index, id));
                    velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                    final float velocityY = velocityTracker.getYVelocity(pointerId);
                    final float velocityX = velocityTracker.getXVelocity(pointerId);

                    if ((Math.abs(velocityY) > mMinimumFlingVelocity)
                            || (Math.abs(velocityX) > mMinimumFlingVelocity))
                    {
                        handled = mListener.onFling(mCurrentDownEvent, ev, velocityX, velocityY);
                    }
                }
                if (mPreviousUpEvent != null)
                {
                    mPreviousUpEvent.recycle();
                }
                // Hold the event we obtained above - listeners may have changed the original.
                mPreviousUpEvent = currentUpEvent;
                if (mVelocityTracker != null)
                {
                    // This may have been cleared when we called out to the
                    // application above.
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mIsDoubleTapping = false;
                mDeferConfirmSingleTap = false;
                mHandler.removeMessages(SHOW_PRESS);
                mHandler.removeMessages(LONG_PRESS);
                break;

            case MotionEvent.ACTION_CANCEL:
                cancel();
                break;
        }

        return handled;
    }

    private void cancel()
    {
        mHandler.removeMessages(SHOW_PRESS);
        mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mIsDoubleTapping = false;
        mStillDown = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        mDeferConfirmSingleTap = false;
        if (mInLongPress)
        {
            mInLongPress = false;
        }
    }

    private void cancelTaps()
    {
        mHandler.removeMessages(SHOW_PRESS);
        mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mIsDoubleTapping = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        mDeferConfirmSingleTap = false;
        if (mInLongPress)
        {
            mInLongPress = false;
        }
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp,
                                          MotionEvent secondDown)
    {
        if (!mAlwaysInBiggerTapRegion)
        {
            return false;
        }

        final long deltaTime = secondDown.getEventTime() - firstUp.getEventTime();
        if (deltaTime > DOUBLE_TAP_TIMEOUT || deltaTime < DOUBLE_TAP_MIN_TIME)
        {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare);
    }

    private void dispatchLongPress()
    {
        mHandler.removeMessages(TAP);
        mDeferConfirmSingleTap = false;
        mInLongPress = true;
        mListener.onLongPress(mCurrentDownEvent);
    }
}


