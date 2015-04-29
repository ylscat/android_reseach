package arbell.research.util;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;

/**
 * Author: YinLanShan
 * Date: 13-6-17
 * Time: 16:36
 */
public class GestureControl implements View.OnTouchListener
{
    protected GLSurfaceView mView;
    protected float[] mRotationMatrix;

    private RotationComputer mComputer;
    private RotationListener mListener;

    private float mTouchScaleFactor;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;
    private static Handler sHandler = new Handler();
    private Scroller mScroller;
    private Flinger mFlinger;

    enum State
    {
        INACTIVE,
        SCROLL,
        TWIST
    }

    private State mState = State.INACTIVE;
    private int mTouchSlopSquare;
    private int mPointerID0 = -1, mPointerID1 = -1;
    private float mX0, mY0, mX1, mY1, mLastAngle;

    public GestureControl(GLSurfaceView view, float[] matrix)
    {
        mView = view;
        mRotationMatrix = matrix;
        ViewConfiguration configuration = ViewConfiguration.get(view.getContext());
        int slop = configuration.getScaledTouchSlop();
        mTouchSlopSquare = slop * slop;
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        int screenWidth = view.getResources().getDisplayMetrics().widthPixels;
        mTouchScaleFactor = (float) (2*Math.PI/screenWidth);
        mComputer = new DCM_Computer();
        mScroller = new Scroller(view.getContext(), new OvershootInterpolator());
    }

    public void setRotationMatrix(float[] matrix)
    {
        mRotationMatrix = matrix;
    }

    public void setRotationComputer(RotationComputer computer)
    {
        mComputer = computer;
    }

    public void setRotationListener(RotationListener listener)
    {
        mListener = listener;
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
                mX0 = event.getX(index);
                mY0 = event.getY(index);
                if(mFlinger != null)
                    sHandler.removeCallbacks(mFlinger);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mPointerID1 == -1)
                {
                    mPointerID1 = id;
                    mX1 = event.getX(index);
                    mY1 = event.getY(index);
                } else if (mPointerID0 == -1)
                {
                    mPointerID0 = id;
                    mX0 = event.getX(index);
                    mY0 = event.getY(index);
                }
                mState = State.INACTIVE;
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                float vx = mVelocityTracker.getXVelocity();
                float vy = mVelocityTracker.getYVelocity();
                float vel = (float)Math.sqrt(vx*vx + vy*vy);
                if(vel > mMinimumVelocity) {
                    mScroller.fling(0, 0, (int)vx, (int)vy,
                            Integer.MIN_VALUE, Integer.MAX_VALUE,
                            Integer.MIN_VALUE, Integer.MAX_VALUE);
//                    float dur = (float)Math.sqrt(vel/1000);
//                    android.util.Log.d("GC", "vel:"+vel+" dur:"+dur);
//                    mScroller.startScroll(0, 0, (int)(vx*dur/2), (int)(vy*dur/2), (int)(dur*1000));
                    mFlinger = new Flinger();
                    sHandler.post(mFlinger);
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
                mState = State.INACTIVE;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mState) {
                    case INACTIVE:
                        if (mPointerID0 != -1) {
                            index = event.findPointerIndex(mPointerID0);
                            float x = event.getX(index);
                            float y = event.getY(index);
                            float deltaX = x - mX0;
                            float deltaY = y - mY0;
                            float distanceSquare = deltaX * deltaX + deltaY * deltaY;
                            if (distanceSquare > mTouchSlopSquare) {
                                if (mPointerID1 == -1)
                                    mState = State.SCROLL;
                                else
                                    mState = State.TWIST;

                                mX0 = x;
                                mY0 = y;
                            }
                        }

                        if (mPointerID1 != -1) {
                            index = event.findPointerIndex(mPointerID1);
                            float x = event.getX(index);
                            float y = event.getY(index);
                            float deltaX = x - mX1;
                            float deltaY = y - mY1;
                            float distanceSquare = deltaX * deltaX + deltaY * deltaY;
                            if (distanceSquare > mTouchSlopSquare) {
                                if (mPointerID0 == -1)
                                    mState = State.SCROLL;
                                else
                                    mState = State.TWIST;

                                mX1 = x;
                                mY1 = y;
                            }
                        }
                        if (mState == State.TWIST) {
                            mLastAngle = (float) Math.atan2(mY1 - mY0, mX1 - mX0);
                        }
                        break;
                    case SCROLL:
                        if (mPointerID0 != -1) {
                            index = event.findPointerIndex(mPointerID0);
                            if (mComputer.computeRotation((event.getY(index) - mY0) * mTouchScaleFactor,
                                    (event.getX(index) - mX0) * mTouchScaleFactor, 0, mRotationMatrix)) {
                                if (mListener != null)
                                    mListener.onRotation(mRotationMatrix);
                                mView.requestRender();
                                mX0 = event.getX(index);
                                mY0 = event.getY(index);
                            }
                        } else if (mPointerID1 != -1) {
                            index = event.findPointerIndex(mPointerID1);
                            if (mComputer.computeRotation((event.getY(index) - mY1) * mTouchScaleFactor,
                                    (event.getX(index) - mX1) * mTouchScaleFactor, 0, mRotationMatrix)) {
                                if (mListener != null)
                                    mListener.onRotation(mRotationMatrix);
                                mView.requestRender();
                                mX1 = event.getX(index);
                                mY1 = event.getY(index);
                            }
                        }
                        break;
                    case TWIST:
                        int index0 = event.findPointerIndex(mPointerID0);
                        int index1 = event.findPointerIndex(mPointerID1);
                        mX0 = event.getX(index0);
                        mX1 = event.getX(index1);
                        mY0 = event.getY(index0);
                        mY1 = event.getY(index1);
                        /*android.util.Log.d("touch", String.format("(%d %d) (%d %d)",
                                (int) mX0, (int) mY0, (int) mX1, (int) mY1));*/

                        float angle = (float) Math.atan2(mY1 - mY0, mX1 - mX0);
                        if (mComputer.computeRotation(0, 0, mLastAngle - angle, mRotationMatrix)) {
                            if (mListener != null)
                                mListener.onRotation(mRotationMatrix);
                            mView.requestRender();
                            mLastAngle = angle;
                        }
                        break;
                }
        }

        return true;
    }

    public static class DCM_Computer implements RotationComputer
    {
        @Override
        public boolean computeRotation(float ax, float ay, float az, float[] R)
        {
            double len = Math.sqrt((ax * ax + ay * ay + az * az));
            if (len < 0.002f)
                return false;
            float k1 = (float) (Math.sin(len) / len);
            float k2 = (float) ((1 - Math.cos(len)) / len / len);
            float[] B = new float[16], B2 = new float[16], dR = new float[16];
            dR[15] = 1;

            /**      0  -az   ay
             *  B = az   0   -ax
             *     -ay   ax   0
             */
            B[1] = az;
            B[2] = -ay;
            B[4] = -az;
            B[6] = ax;
            B[8] = ay;
            B[9] = -ax;

            Matrix.multiplyMM(B2, 0, B, 0, B, 0);

            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    int index = i * 4 + j;
                    dR[index] = k1 * B[index] + k2 * B2[index];
                    if (i == j)
                        dR[index] += 1;
                }
            }

            System.arraycopy(R, 0, B, 0, R.length);
            Matrix.multiplyMM(R, 0, dR, 0, B, 0);
            return true;
        }
    }

    public static interface RotationComputer
    {
        boolean computeRotation(float ax, float ay, float az, float[] result_matrix);
    }

    public static interface RotationListener
    {
        void onRotation(float[] R);
    }

    class Flinger implements Runnable {
        int x, y;

        @Override
        public void run() {
            if(mScroller.computeScrollOffset()) {
                int dx = mScroller.getCurrX() - x;
                int dy = mScroller.getCurrY() - y;
                x = mScroller.getCurrX();
                y = mScroller.getCurrY();
                mComputer.computeRotation(dy * mTouchScaleFactor,
                        dx * mTouchScaleFactor, 0, mRotationMatrix);
                if (mListener != null)
                    mListener.onRotation(mRotationMatrix);
                mView.requestRender();
                sHandler.postDelayed(this, 10);
            }
            else {
                mFlinger = null;
            }
        }
    }
}
