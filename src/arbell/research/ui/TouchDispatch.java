package arbell.research.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;
import arbell.research.R;
import arbell.research.ui.view.MyHorizontalScrollView;

import com.baidu.mapapi.map.MapView;

/**
 * @author YinLanshan
 *         creation time 2015/4/20.
 */
public class TouchDispatch extends Activity
        implements CompoundButton.OnCheckedChangeListener {
    private MyHorizontalScrollView mHsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touch_dispatch);
        MapView map = (MapView)findViewById(R.id.map_view);
        for(int i = map.getChildCount() - 1; i >= 0; i--) {
            if(map.getChildAt(i) instanceof ImageView) {
                map.removeViewAt(i);
                break;
            }
        }
        mHsv = (MyHorizontalScrollView)findViewById(R.id.hsv);
        ToggleButton tb = (ToggleButton)findViewById(R.id.switcher);
        tb.setOnCheckedChangeListener(this);
        onCheckedChanged(tb, tb.isChecked());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mHsv.setScrollable(isChecked);
    }
}
