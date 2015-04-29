package arbell.research.academy.stereo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Author: YinLanShan
 * Date: 14-8-26
 * Time: 17:20
 */
public class SimpleTest extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        View left = new View(this);
        left.setBackgroundColor(Color.RED);
        ll.addView(left);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)left.getLayoutParams();
        lp.width = 0;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.weight = 1;
        View right = new View(this);
        right.setBackgroundColor(Color.BLUE);
        ll.addView(right);
        lp = (LinearLayout.LayoutParams)right.getLayoutParams();
        lp.width = 0;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.weight = 1;
        setContentView(ll);
    }
}
