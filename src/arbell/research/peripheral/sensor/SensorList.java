package arbell.research.peripheral.sensor;

import android.app.ListActivity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SensorList extends ListActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);

        initAdapters(sensors);
    }

    private void initAdapters(List<Sensor> sensors)
    {
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>(sensors.size());
        final String infoTemplate =
                "Type: %s\n" +
                        "Vendor: %s\n" +
                        "MinDelay: %d us\n" +
                        "Resolution: %f\n" +
                        "Power: %f";
        final String KEY_NAME = "name";
        final String KEY_INFO = "info";
        for (Sensor sensor : sensors)
        {
            HashMap<String, String> entry = new HashMap<String, String>(2);
            entry.put(KEY_NAME, sensor.getName());
            entry.put(KEY_INFO, String.format(infoTemplate,
                    resolveType(sensor.getType()),
                    sensor.getVendor(),
                    sensor.getMinDelay(),
                    sensor.getResolution(),
                    sensor.getPower()));
            data.add(entry);
        }

        String[] from = {KEY_NAME, KEY_INFO};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        SimpleAdapter sa = new SimpleAdapter(this,
                data,
                android.R.layout.simple_list_item_2,
                from,
                to);
        setListAdapter(sa);
    }

    @SuppressWarnings("deprecation")
    private String resolveType(int typeCode)
    {
        String type = null;
        switch (typeCode)
        {
            case Sensor.TYPE_ACCELEROMETER:
                type = "ACCELEROMETER";
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                type = "AMBIENT_TEMPERATURE";
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                type = "GAME_ROTATION_VECTOR";
                break;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                type = "GEOMAGNETIC_ROTATION_VECTOR";
                break;
            case Sensor.TYPE_GRAVITY:
                type = "GRAVITY";
                break;
            case Sensor.TYPE_GYROSCOPE:
                type = "GYROSCOPE";
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                type = "GYROSCOPE UNCALIBRATED";
                break;
            case Sensor.TYPE_LIGHT:
                type = "LIGHT";
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                type = "LINEAR_ACCELERATION";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                type = "MAGNETIC_FIELD";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                type = "MAGNETIC_FIELD_UNCALIBRATED";
                break;
            case Sensor.TYPE_ORIENTATION:
                type = "ORIENTATION";
                break;
            case Sensor.TYPE_PRESSURE:
                type = "PRESSURE";
                break;
            case Sensor.TYPE_PROXIMITY:
                type = "PROXIMITY";
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                type = "ROTATION_VECTOR";
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                type = "HUMIDITY";
                break;
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                type = "SIGNIFICANT_MOTION";
                break;
            case Sensor.TYPE_STEP_COUNTER:
                type = "STEP_COUNTER";
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                type = "STEP_DETECTOR";
                break;
            case Sensor.TYPE_TEMPERATURE:
                type = "TEMPERATURE";
                break;
        }


        return type;
    }
}
