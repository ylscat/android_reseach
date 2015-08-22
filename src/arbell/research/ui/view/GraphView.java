package arbell.research.ui.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View
{
    private Bitmap mBitmap;
    private Canvas mCanvas = new Canvas();
    private int mOffset = 0;
    private Paint mPaint = new Paint();
    private Rect mSrc = new Rect(), mDst = new Rect();

    private static final int COLOR = 0xFF0000AA;

    public GraphView(Context context)
    {
        super(context);
        mPaint.setColor(COLOR);
    }

    public GraphView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mPaint.setColor(COLOR);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        if(changed)
        {
            int width = right - left;
            int height = bottom - top;
            if(width == 0 || height == 0)
                return;

            if(mBitmap != null)
                mBitmap.recycle();
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mOffset = 0;
            mSrc.set(0, 0, width, height);
            mDst.set(0, 0, 0, height);
            mBitmap.eraseColor(Color.WHITE);
            mCanvas.setBitmap(mBitmap);
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int w = getWidth(), h = getHeight();
        mSrc.left = mOffset;
        mSrc.right = w;
        mDst.left = 0;
        mDst.right = w - mOffset;
        canvas.drawBitmap(mBitmap, mSrc, mDst, mPaint);
        mSrc.left = 0;
        mSrc.right = mOffset;
        mDst.left = w - mOffset;
        mDst.right = w;
        canvas.drawBitmap(mBitmap, mSrc, mDst, mPaint);
    }

    public void push(float value)
    {
        if(value > 1)
            value = 1;
        else if(value < 0)
            value = 0;
        int h = getHeight();
        int s = (int)(h*(1 - value));
        mPaint.setColor(Color.WHITE);
        mCanvas.drawRect(mOffset, 0, mOffset + 1, s, mPaint);
        mPaint.setColor(COLOR);
        mCanvas.drawRect(mOffset, s, mOffset + 1, h, mPaint);
        mOffset += 2;
        if(mOffset >= getWidth())
            mOffset = 0;
    }
}