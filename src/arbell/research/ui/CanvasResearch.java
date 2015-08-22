package arbell.research.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

/**
 * @author YinLanshan
 *         creation time 2015/3/30.
 */
public class CanvasResearch extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new TempView(this));
    }

    class TempView extends View {
        private Paint mPaint;

        public TempView(Context context) {
            super(context);
            mPaint = new Paint();

        }

        @Override
        protected void onDraw(Canvas canvas) {
//            drawRect(canvas);
            int count = canvas.save();
            final int width = getWidth();
            final int height = getHeight();
            canvas.translate(80, 40);
            canvas.rotate(180);
            drawRect(canvas);
            canvas.restoreToCount(count);
            android.util.Log.d("CR", "onDraw", new Exception());
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            android.util.Log.d("CR", "onLayout", new Exception());
        }

        private void drawRect(Canvas canvas) {
            mPaint.setColor(android.graphics.Color.RED);
            canvas.drawRect(0, 0, 40, 40, mPaint);
            mPaint.setColor(android.graphics.Color.BLUE);
            canvas.drawRect(40, 0, 80, 40, mPaint);
        }
    }
}
