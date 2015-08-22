package arbell.research.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Author: YinLanShan
 * Date: 13-6-17
 * Time: 16:36
 */
public class GestureListener implements View.OnTouchListener
{
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;

    private Callback mCallback;

    enum State
    {
        INACTIVE,
        SCROLL,
        SCALE,
        TWIST
    }

    private State mState = State.INACTIVE;
    private int mTouchSlopSquare;
    private int mPointerID0 = -1, mPointerID1 = -1;
    private float[] P0 = new float[2], P1 = new float[2], mStart = new float[2];
    private PointersHistory mHistory = new PointersHistory();
    private float mStartDistance;
    private int mPivotX, mPivotY;

    public GestureListener(Context context)
    {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        int slop = configuration.getScaledTouchSlop();
        mTouchSlopSquare = slop * slop;
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int id = event.getPointerId(index);
        if(mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                mPointerID0 = id;
                mState = State.INACTIVE;
                P0[0] = event.getX(index);
                P0[1] = event.getY(index);
                if(mCallback != null)
                    mCallback.down((int)P0[0], (int)P0[1]);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mPointerID1 == -1)
                {
                    mPointerID1 = id;
                    P1[0] = event.getX(index);
                    P1[1] = event.getY(index);
                } else if (mPointerID0 == -1)
                {
                    mPointerID0 = id;
                    P0[0] = event.getX(index);
                    P0[1] = event.getY(index);
                }

                mState = State.INACTIVE;
                mHistory.reset();
                mHistory.add(P0[0], P0[1], P1[0], P1[1]);
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                if(mCallback != null) {
                    float vx = mVelocityTracker.getXVelocity();
                    float vy = mVelocityTracker.getYVelocity();
                    float vel = (float) Math.sqrt(vx * vx + vy * vy);
                    if (vel > mMinimumVelocity && mState == State.SCROLL) {
                        mCallback.fling(vx, vy);
                    } else {
                        mCallback.up();
                    }
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            case MotionEvent.ACTION_CANCEL:
                mPointerID0 = mPointerID1 = -1;
                mState = State.INACTIVE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (id == mPointerID0)
                    mPointerID0 = -1;
                else if (id == mPointerID1)
                    mPointerID1 = -1;
                if(mState == State.SCROLL)
                    mState = State.INACTIVE;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mState) {
                    case INACTIVE:
                        if(mPointerID0 != -1 && checkScroll(event, mPointerID0, P0)) {
                            mState = decideStatus();
                            P0[0] = mStart[0];
                            P0[1] = mStart[1];
                            if(mCallback != null) {
                                int x = (int)mStart[0];
                                int y = (int)mStart[1];
                                mCallback.scroll(x, y, x, y);
                            }
                        }
                        else if(mPointerID1 != -1 && checkScroll(event, mPointerID1, P1)) {
                            mState = decideStatus();
                            P1[0] = mStart[0];
                            P1[1] = mStart[1];
                            if(mCallback != null) {
                                int x = (int)mStart[0];
                                int y = (int)mStart[1];
                                mCallback.scroll(x, y, x, y);
                            }
                        }

                        if(mPointerID0 != -1 && mPointerID1 != -1) {
                            mHistory.add(P0[0], P0[1], P1[0], P1[1]);
                        }
                        if(mState == State.SCALE) {
                            float[] avg = mHistory.getAverage();
                            float dx = avg[0] - avg[2];
                            float dy = avg[1] - avg[3];
                            mStartDistance = (float)Math.sqrt(dx*dx + dy*dy);
                            mPivotX = (int)(avg[0] + avg[2])/2;
                            mPivotY = (int)(avg[1] + avg[3])/2;
                        }
                        break;
                    case SCROLL:
                        if (mPointerID0 != -1) {
                            scroll(event, mPointerID0, P0);
                        } else if (mPointerID1 != -1) {
                            scroll(event, mPointerID1, P1);
                        }
                        break;
                    case SCALE:
                        if(mPointerID0 == -1 || mPointerID1 == -1)
                            break;
                        int index0 = event.findPointerIndex(mPointerID0);
                        int index1 = event.findPointerIndex(mPointerID1);
                        P0[0] = event.getX(index0);
                        P1[0] = event.getX(index1);
                        P0[1] = event.getY(index0);
                        P1[1] = event.getY(index1);
                        mHistory.add(P0[0], P0[1], P1[0], P1[1]);

                        if(mCallback != null) {
                            float[] avg = mHistory.getAverage();
                            float dx = avg[0] - avg[2];
                            float dy = avg[1] - avg[3];
                            float d = (float)Math.sqrt(dx*dx + dy*dy);
                            mCallback.scale(mStartDistance, d, mPivotX, mPivotY);
                        }
                        break;
                }
        }

        return true;
    }

    private boolean checkScroll(MotionEvent event, int id, float[] pos) {
        int index = event.findPointerIndex(id);
        float x = event.getX(index);
        float y = event.getY(index);
        float deltaX = x - pos[0];
        float deltaY = y - pos[1];
        float distanceSquare = deltaX * deltaX + deltaY * deltaY;
        if (distanceSquare > mTouchSlopSquare) {
            mStart[0] = x;
            mStart[1] = y;
            return true;
        }
        return  false;
    }

    private State decideStatus() {
        if(mPointerID0 != -1 && mPointerID1 != -1)
            return State.SCALE;
        else
            return State.SCROLL;
    }

    private void scroll(MotionEvent event, int id, float[] pos) {
        int index = event.findPointerIndex(id);
        pos[0] = event.getX(index);
        pos[1] = event.getY(index);
        if(mCallback != null) {
            mCallback.scroll((int) mStart[0], (int) mStart[1], (int)pos[0], (int)pos[1]);
        }
    }

    public interface Callback {
        void down(int x, int y);
        void fling(float vx, float vy);
        void up();
        void scale(float startDistance, float currentDistance, int pivotX, int pivotY);
        void scroll(int startX, int startY, int x, int y);
    }

    class PointersHistory {
        static final int HISTORY_DEPTH = 6;
        private LinkedList<float[]> mHistory = new LinkedList<>();
        private float[] sum = new float[4];

        public void add(float ... values) {
            float[] old = null;
            if(mHistory.size() == HISTORY_DEPTH) {
                old = mHistory.removeFirst();
            }
            mHistory.addLast(values);
            float[] s = sum;
            for(int i = 0; i < s.length; i++) {
                if(old == null)
                    s[i] += values[i];
                else
                    s[i] += values[i] - old[i];
            }
        }

        public void reset() {
            mHistory.clear();
            Arrays.fill(sum, 0);
        }

        public float[] getAverage() {
            int count = mHistory.size();
            float[] avg = new float[sum.length];
            if(count == 0)
                return avg;
            for(int i = 0; i < avg.length; i++) {
                avg[i] = sum[i]/count;
            }
            return avg;
        }
    }
}
