package arbell.research.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

/**
 * Author: YinLanShan
 * Date: 14-2-7
 * Time: 13:55
 */
public class ScaleGesture extends Activity implements View.OnTouchListener
{
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

        ViewConfiguration configuration = ViewConfiguration.get(tv.getContext());
        int slop = configuration.getScaledTouchSlop();
        mTouchSlopSquare = slop * slop;
    }

    //Implementation of OnTouchListener
    enum State
    {
        INACTIVE,
        SCROLL,
        TWIST
    }

    State mState = State.INACTIVE;
    private int mTouchSlopSquare;
    int mPointerID0 = -1, mPointerID1 = -1;
    private float mX0, mY0, mX1, mY1, mInitialAngle;

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int id = event.getPointerId(index);

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                mPointerID0 = id;
                mState = State.INACTIVE;
                mX0 = event.getX(index);
                mY0 = event.getY(index);
                mTextView.setText("Inactive");
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
                mTextView.setText("Inactive");
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mPointerID0 = mPointerID1 = -1;
                mState = State.INACTIVE;
                mTextView.setText(null);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (id == mPointerID0)
                    mPointerID0 = -1;
                else if (id == mPointerID1)
                    mPointerID1 = -1;
                mState = State.INACTIVE;
                mTextView.setText("Inactive");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mState == State.INACTIVE)
                {
                    if (mPointerID0 != -1)
                    {
                        index = event.findPointerIndex(mPointerID0);
                        float x = event.getX(index);
                        float y = event.getY(index);
                        float deltaX = x - mX0;
                        float deltaY = y - mY0;
                        float distanceSquare = deltaX * deltaX + deltaY * deltaY;
                        if (distanceSquare > mTouchSlopSquare)
                        {
                            if (mPointerID1 == -1)
                                mState = State.SCROLL;
                            else
                                mState = State.TWIST;
                            mX0 = x;
                            mY0 = y;
                        }
                    }

                    if (mPointerID1 != -1)
                    {
                        index = event.findPointerIndex(mPointerID1);
                        float x = event.getX(index);
                        float y = event.getY(index);
                        float deltaX = x - mX1;
                        float deltaY = y - mY1;
                        float distanceSquare = deltaX * deltaX + deltaY * deltaY;
                        if (distanceSquare > mTouchSlopSquare)
                        {
                            if (mPointerID0 == -1)
                                mState = State.SCROLL;
                            else
                                mState = State.TWIST;
                            mX1 = x;
                            mY1 = y;
                        }
                    }
                    if (mState == State.TWIST)
                    {
                        mInitialAngle = (float) Math.atan2(mY0 - mY1, mX0 - mX1);
                    }
                } else if (mState == State.SCROLL)
                {
                    if (id == mPointerID0)
                    {
                        index = event.findPointerIndex(mPointerID0);
                        mTextView.setText(String.format("S: P0[%d %d]", (int) event.getX(index), (int) event.getY(index)));
                    } else if (id == mPointerID1)
                    {
                        index = event.findPointerIndex(mPointerID1);
                        mTextView.setText(String.format("S: P1[%d %d]", (int) event.getX(index), (int) event.getY(index)));
                    }
                } else if (mState == State.TWIST)
                {
                    int index0 = event.findPointerIndex(mPointerID0);
                    int index1 = event.findPointerIndex(mPointerID1);
                    mX0 = event.getX(index0);
                    mX1 = event.getX(index1);
                    mY0 = event.getY(index0);
                    mY1 = event.getY(index1);
                    float angle = (float) Math.atan2(mY1 - mY0, mX1 - mX0);
                    angle = angle - mInitialAngle;

                    if (angle < -Math.PI)
                        angle += Math.PI * 2;
                    else if (angle > Math.PI)
                        angle -= Math.PI * 2;
                    mTextView.setText(String.format("T: %f", angle * 180 / 3.14f));
                }
        }

        return true;
    }
}

