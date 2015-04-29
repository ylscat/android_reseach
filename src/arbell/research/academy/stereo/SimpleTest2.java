package arbell.research.academy.stereo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

/**
 * Author: YinLanShan
 * Date: 14-8-26
 * Time: 17:26
 */
public class SimpleTest2 extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(new InterlacedView(this));
    }

    class InterlacedView extends View
    {
        private Paint mPaint = new Paint();

        public InterlacedView(Context context)
        {
            super(context);
        }

        public InterlacedView(Context context, AttributeSet attrs)
        {
            super(context, attrs);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            int w = getWidth();
            int h = getHeight();
            for(int i = 0; i < h; i++)
            {
                int color = (i&1) == 0 ? Color.RED : Color.BLUE;
                mPaint.setColor(color);
                canvas.drawLine(0, i, w, i, mPaint);
            }
        }
    }
}
