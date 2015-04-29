package arbell.research.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextView4Debug extends TextView
{

    public TextView4Debug(Context context)
    {
        super(context);
    }

    public TextView4Debug(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public TextView4Debug(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void draw(Canvas canvas)
    {
        super.draw(canvas);
        if (getText().equals("Fifth Line"))
        {
            android.util.Log.d("anim", "here");
        }
    }
}