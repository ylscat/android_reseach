package arbell.research.peripheral.sensor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Author: YinLanShan
 * Date: 13-12-20
 * Time: 14:52
 */
public class Light extends Activity implements SensorEventListener
{
    private TextView mTextView;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTextAppearance(this, android.R.style.TextAppearance_Large);
        mTextView = tv;
        setContentView(tv);

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager = sm;
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensor = sensor;
        if (sensor == null)
        {
            tv.setText("Light sensor is no available!");
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mSensor != null)
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mSensor != null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        mTextView.setText(String.format("%f lux", event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        //Nothing to do
    }
}
