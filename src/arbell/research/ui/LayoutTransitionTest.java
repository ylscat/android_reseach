package arbell.research.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import arbell.research.R;

/**
 * Author: YinLanShan
 * Date: 14-2-13
 * Time: 18:52
 */
public class LayoutTransitionTest extends Activity implements View.OnClickListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_list);
        ViewGroup vg = (ViewGroup) findViewById(R.id.container);
        vg.getLayoutTransition().setAnimateParentHierarchy(false);
        for (int i = vg.getChildCount() - 1; i >= 0; i--)
        {
            View child = vg.getChildAt(i);
            if (child instanceof TextView)
            {
                child.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        ViewGroup vg = (ViewGroup) v.getParent();
        vg.removeView(v);
    }
}
