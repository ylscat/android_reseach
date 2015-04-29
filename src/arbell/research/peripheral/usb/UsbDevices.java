package arbell.research.peripheral.usb;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import arbell.research.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: YinLanShan
 * Date: 14-1-23
 * Time: 15:45
 */
public class UsbDevices extends Activity
{
    private UsbManager mManager;
    private ArrayList<HashMap<String, String>> mDevices = new ArrayList<HashMap<String, String>>();
    private SimpleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        setContentView(R.layout.usb_devices);
        mAdapter = new SimpleAdapter(this, mDevices,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "detail"},
                new int[]{android.R.id.text1, android.R.id.text2});
        ((ListView) findViewById(android.R.id.list)).setAdapter(mAdapter);
        refresh(null);
    }

    public void refresh(View view)
    {
        HashMap<String, UsbDevice> map = mManager.getDeviceList();
        mDevices.clear();
        for (String key : map.keySet())
        {
            StringBuilder sb = new StringBuilder();
            UsbDevice dev = map.get(key);
            int vid = dev.getVendorId();
            int pid = dev.getProductId();
            HashMap<String, String> entry = new HashMap<String, String>(2);
            entry.put("name", String.format("[0x%x:0x%x]", vid, pid));
            String detail = String.format("Class:0x%x SubClass 0x%x",
                    dev.getDeviceClass(), dev.getDeviceSubclass());
            sb.append(detail);
            for (int i = 0; i < dev.getInterfaceCount(); i++)
            {
                UsbInterface interf = dev.getInterface(i);
                sb.append("\n  ");
                sb.append(String.format("Interface%d EP[%d]", i, interf.getEndpointCount()));
            }
            entry.put("detail", sb.toString());
            mDevices.add(entry);
        }
        mAdapter.notifyDataSetChanged();
    }
}
