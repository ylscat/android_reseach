package arbell.research.peripheral.usb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.hardware.usb.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import arbell.research.R;

import java.util.HashMap;

/**
 * Author: YinLanShan
 * Date: 14-1-23
 * Time: 17:18
 */
public class UsbReceiver extends Activity implements View.OnClickListener
{
    private UsbManager mManager;
    private UsbDevice mDevice;
    private UsbEndpoint mInEP;
    private UsbEndpoint mOutEP;
    private UsbInterface mInterface;
    private UsbDeviceConnection mConnection;

    private TextView mText;

    private static final String TAG = "USB_COM";
    private static final String ACTION_USB_PERMISSION =
            "research.ivory.usb.USB_PERMISSION";

    private Receiver mReceiverThread = new Receiver();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usb_receiver);
        mText = (TextView)findViewById(R.id.text);
        mText.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        mText.setTextSize(26);

        Button button = (Button)findViewById(R.id.send);
        button.setOnClickListener(this);
        mManager = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> map = mManager.getDeviceList();
        UsbDevice dev = null;
        for (String key : map.keySet())
        {
            UsbDevice d = map.get(key);
            if (d.getVendorId() == 0x1915 && d.getProductId() == 0x0100)
            {
                dev = d;
                break;
            }
        }
        if (dev == null)
        {
            mText.setText("No target device");
            return;
        }
        mDevice = dev;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        mManager.requestPermission(dev, pendingIntent);
        mText.setText("Requesting Permission");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mReceiverThread.isStopped = true;
        if (mConnection != null)
        {
            mConnection.releaseInterface(mInterface);
            mConnection.close();
        }

        if (mDevice != null)
            unregisterReceiver(mUsbReceiver);
    }

    private StringBuilder mMessage = new StringBuilder(200);
    private int lineCount = 0;
    private static final int MAX_LINE_COUNT = 20;
    private final Runnable mTextWriter = new Runnable()
    {
        @Override
        public void run()
        {
            mText.setText(mMessage);
        }
    };

    private class Receiver extends Thread
    {
        public boolean isStopped = false;

        @Override
        public void run()
        {
            if (mConnection != null && mInEP != null)
            {
                byte[] data = new byte[40];
                while (!isStopped)
                {
                    int len = mConnection.bulkTransfer(mInEP, data, 32, 1000); // 1000ms timeout
                    //android.util.Log.d("usb", "Rx len:"+len);
                    if (len > 0)
                    {
                        mMessage.delete(0, mMessage.length());
                        for(int i = 0; i < len; i++)
                        {
                            mMessage.append(String.format("%02X ", data[i]));
                        }
                        mText.post(mTextWriter);
                    }
                }
            }
            isStopped = true;
        }
    }

    private void setupConn(UsbDevice device)
    {
        if (device != null)
        {
            mDevice = device;
            mConnection = mManager.openDevice(device);
            mInterface = device.getInterface(0);
            mConnection.claimInterface(mInterface, true);
            UsbEndpoint ep = mInterface.getEndpoint(0);
            if (ep.getDirection() == UsbConstants.USB_DIR_IN)
                mInEP = ep;
            else
                mOutEP = ep;
            ep = mInterface.getEndpoint(1);
            if (ep.getDirection() == UsbConstants.USB_DIR_IN)
                mInEP = ep;
            else
                mOutEP = ep;
            mText.setText("get Connected");
            mReceiverThread.start();
        }
    }

    @Override
    public void onClick(View v)
    {
        if (mConnection != null && mOutEP != null)
        {
            byte[] data = new byte[1];
            EditText et = (EditText)findViewById(R.id.input_text);
            CharSequence cs = et.getText();
            try{
                data[0] = Byte.parseByte(cs.toString(), 10);
            } catch (NumberFormatException e)
            {
                Toast.makeText(this, "Bad num format", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("usb", String.format("sent %d", data[0]));
            mConnection.bulkTransfer(mOutEP, data, data.length, 1000);
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                synchronized (this)
                {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        setupConn(device);
                    } else
                    {
                        mText.setText("Permission Denied");
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };
}
