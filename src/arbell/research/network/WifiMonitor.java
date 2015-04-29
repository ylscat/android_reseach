package arbell.research.network;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: YinLanShan
 * Date: 14-7-30
 * Time: 13:29
 */
public class WifiMonitor extends ListActivity
{
    private SimpleAdapter mAdapter;
    private List<Map<String, Object>> mData;
    private IntentFilter mIntentFilter;
    private Monitor mMonitor;

    private static final String KEY_TEXT = "text";
    private static final String KEY_TIME = "time";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mData = new ArrayList<Map<String, Object>>();
        SimpleAdapter adapter = new SimpleAdapter(this, mData,
                android.R.layout.simple_list_item_2,
                new String[]{KEY_TEXT, KEY_TIME},
                new int[]{android.R.id.text1, android.R.id.text2});
        mAdapter = adapter;
        setListAdapter(adapter);
        mIntentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mMonitor = new Monitor();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mMonitor, mIntentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mMonitor);
    }

    class Monitor extends BroadcastReceiver
    {
        private SimpleDateFormat sFormatter =
                new SimpleDateFormat("HH:mm:ss.SSS");


        @Override
        public void onReceive(Context context, Intent intent)
        {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            String text = null;
            if(info != null)
            {
                text = info.getState().toString();
            }
            Calendar c = Calendar.getInstance();
            HashMap<String, Object> map = new HashMap<String, Object>(2);
            map.put(KEY_TEXT, text);
            map.put(KEY_TIME, sFormatter.format(c.getTime()));
            mData.add(map);
            mAdapter.notifyDataSetChanged();
        }
    }
}
