package arbell.research.network;

import android.app.Activity;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import arbell.research.R;
import arbell.research.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: YinLanShan
 * Date: 14-7-24
 * Time: 00:43
 */
public class Scanner extends Activity implements View.OnClickListener
{
    private Button mScanButton;
    private TextView mTextView;
    private DhcpInfo mDhcpInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_scanner);
        mScanButton = (Button)findViewById(R.id.scan);
        mTextView = (TextView)findViewById(R.id.text);
        WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo inf = wm.getConnectionInfo();
        if(inf == null || inf.getIpAddress() == 0)
        {
            mTextView.setText("No wifi connection");
            mScanButton.setEnabled(false);
            return;
        }
        DhcpInfo info = wm.getDhcpInfo();
        mTextView.setText(String.format("IP:%s\nMask:%s\nGateway:%s",
                Formatter.formatIpAddress(info.ipAddress),
                Formatter.formatIpAddress(info.netmask),
                Formatter.formatIpAddress(info.gateway)));
        mDhcpInfo = info;
        mScanButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if(mDhcpInfo == null)
            return;
        int mask = mDhcpInfo.netmask;
        int n = 0;
        while((mask<<n) > 0)
            n++;
        int count = (1<<n);
        if(count > 20)
            count = 20;
        int start = ((mDhcpInfo.ipAddress>>24)&0xff) - count/2;
        if(start < 0)
            start = 0;
        start = (mDhcpInfo.gateway & mask) | start<<24;
        Integer[] params = new Integer[]{start, count, mDhcpInfo.gateway, mDhcpInfo.ipAddress};
        ScanTask task = new ScanTask();
        task.execute(params);
    }

    class ScanTask extends AsyncTask<Integer, Integer, Void>
    {
        private ArrayList<String> mActives = new ArrayList<String>();

        @Override
        protected Void doInBackground(Integer... params)
        {
            Integer[] progress = new Integer[2];
            int count = params[1];
            int start = params[0];
            int gateway = params[2];
            int self = params[3];
            Runtime runtime = Runtime.getRuntime();
            final String PING_CMD = "ping -c 1 -w 1 ";
            progress[1] = count;

            for(int i = 1; i < count; i++)
            {
                int minor = i&0xff;
                int sup = i>>8;
                int ip = (minor<<24) + (sup<<16) + start;
                if(ip == gateway || ip == self || minor == 255)
                    continue;
                String ipArg = Formatter.formatIpAddress(ip);

                try
                {
                    Process p = runtime.exec(PING_CMD+ipArg);
                    int r = p.waitFor();
                    if(r == 0)
                        mActives.add(ipArg);
                    android.util.Log.d(Constants.TAG, "[" + r +"] cmd:"+PING_CMD+ipArg);
                } catch (Exception e)
                {
                    android.util.Log.e(Constants.TAG, e.getMessage());
                }
                progress[0] = i;
                publishProgress(progress);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            mTextView.setText(String.format("%d / %d", values[0], values[1]));
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
            for(String active : mActives)
            {
                HashMap<String, String> map = new HashMap<String, String>(1);
                map.put("ip", active);
                data.add(map);
            }
            SimpleAdapter adapter = new SimpleAdapter(Scanner.this, data,
                    android.R.layout.simple_list_item_1, new String[]{"ip"},
                    new int[]{android.R.id.text1});
            ListView lv = (ListView)findViewById(R.id.list_view);
            lv.setAdapter(adapter);
            mTextView.setText("discovered: "+mActives.size());
        }
    }
}
