package arbell.research.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import arbell.research.R;
import arbell.research.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: YinLanShan
 * Date: 14-7-23
 * Time: 14:12
 */
public class WifiP2P extends Activity implements WifiP2pManager.PeerListListener
{
    private WifiP2pManager mWifiP2pMgr;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDevice mMe;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private boolean isWifiP2pEnabled;

    private TextView mText;
    private List<Map<String, Object>> mDevices = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_p2p);
        mText = (TextView)findViewById(R.id.text);
        ListView lv = (ListView)findViewById(R.id.list_view);
        SimpleAdapter adapter = new SimpleAdapter(this, mDevices,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "detail"},
                new int[]{android.R.id.text1, android.R.id.text2});
        lv.setAdapter(adapter);

        mWifiP2pMgr = (WifiP2pManager)getSystemService(WIFI_P2P_SERVICE);
        mChannel = mWifiP2pMgr.initialize(this, getMainLooper(), null);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mReceiver = new WifiP2pBroadcastReceiver();
        mWifiP2pMgr.createGroup(mChannel, mActionListener);
    }

    private void refreshText()
    {
        StringBuilder sb = new StringBuilder();
        if(isWifiP2pEnabled)
        {

        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private String interpretDeviceStatus(int status)
    {
        switch (status)
        {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.INVITED:
                return "Invited";
            default:
                return "Known " + status;
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers)
    {
        Log.d(Constants.TAG, "peers coming " + peers.getDeviceList().size());
        mDevices.clear();
        for(WifiP2pDevice device : peers.getDeviceList())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name", device.deviceName);
            map.put("detail", device.deviceAddress);
            map.put("device", device);
            mDevices.add(map);
        }
        ListView lv = (ListView)findViewById(R.id.list_view);
        ((SimpleAdapter)lv.getAdapter()).notifyDataSetChanged();
    }

    private WifiP2pManager.ActionListener mActionListener =
            new WifiP2pManager.ActionListener()
    {
        @Override
        public void onSuccess() {
            Toast.makeText(WifiP2P.this, "Discovery Initiated",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(int reasonCode) {
            Toast.makeText(WifiP2P.this, "Discovery Failed : " + reasonCode,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private class WifiP2pBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED;
                if(!isWifiP2pEnabled)
                {
                    mText.setText("Wifi P2P disabled");
                    mDevices.clear();
                    ListView lv = (ListView)findViewById(R.id.list_view);
                    ((SimpleAdapter)lv.getAdapter()).notifyDataSetInvalidated();
                }
                Log.d(Constants.TAG, "P2P state changed - " + state);
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                Log.d(Constants.TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
                mWifiP2pMgr.requestPeers(mChannel, WifiP2P.this);
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                Log.d(Constants.TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo != null)
                {
                    Log.d(Constants.TAG, networkInfo.isConnected() ? "Connected" : "Disconnected");
                    Log.d(Constants.TAG, networkInfo.getDetailedState().toString());
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                WifiP2pDevice me = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                Log.d(Constants.TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
                if(me != null)
                {
                    mMe = me;
                    mText.setText(String.format("%s [%s] %s",
                            me.deviceName, me.deviceAddress, interpretDeviceStatus(me.status)));
                }
            }
        }
    }
}
