package arbell.research.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.HashSet;

/**
 * Author: YinLanShan
 * Date: 14-2-7
 * Time: 13:55
 */
public class Twist extends Activity implements View.OnTouchListener
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
    }

    private HashSet<Integer> mIndex = new HashSet<Integer>(2);

    //Implementation of OnTouchListener
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        int index = event.getActionIndex();
        int action = event.getActionMasked();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mIndex.size() < 2)
                    mIndex.add(index);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIndex.clear();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mIndex.remove(index);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIndex.size() < 2)
                    break;
                if (!mIndex.contains(index))
                    break;
                int i = 0;
                float[] coord = new float[4];

                for (int idx : mIndex)
                {
                    coord[i] = event.getX(idx);
                    coord[i + 1] = event.getY(idx);
                    i += 2;
                }
                float angle = (float) (180 / Math.PI * Math.atan2(coord[3] - coord[1], coord[2] - coord[0]));
                mTextView.setText(String.valueOf(angle));
        }
        return true;
    }
}

