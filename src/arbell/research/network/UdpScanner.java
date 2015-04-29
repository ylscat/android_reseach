package arbell.research.network;

import android.app.Activity;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import arbell.research.R;
import arbell.research.util.Constants;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Author: YinLanShan
 * Date: 14-7-25
 * Time: 10:00
 */
public class UdpScanner extends Activity implements View.OnClickListener,
        UdpNetwork.Listener
{

    private TextView mTextView;
    private DhcpInfo mDhcpInfo;
    private String mSelfNetwork;

    private UdpNetwork mNetwork;
    private Scanner mScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_udp_scanner);
        mTextView = (TextView)findViewById(R.id.text);
        Button scanButton = (Button)findViewById(R.id.scan);
        scanButton.setOnClickListener(this);

        WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo inf = wm.getConnectionInfo();
        if(inf == null || inf.getIpAddress() == 0)
        {
            mTextView.setText("No wifi connection");
            scanButton.setEnabled(false);
            return;
        }

        DhcpInfo info = wm.getDhcpInfo();
        mSelfNetwork = String.format("IP:%s\nMask:%s\nGateway:%s",
                ipIntToAddress(info.ipAddress),
                ipIntToAddress(info.netmask),
                ipIntToAddress(info.gateway));
        mTextView.setText(mSelfNetwork);
        mDhcpInfo = info;
        mNetwork = new UdpNetwork();
        mNetwork.setListener(this);
        mNetwork.start();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mNetwork.stop();
    }

    @Override
    public void onClick(View v)
    {
        if(mDhcpInfo == null)
            return;
        mTextView.setText(mSelfNetwork+"\nScanning ...");
        mScanner = new Scanner();
        mScanner.start();
    }

    private final byte[] ACK = "ACK".getBytes();
    private final byte[] PING = "PING".getBytes();

    @Override
    public void onReceive(DatagramPacket packet)
    {
        if(packet == null)
            return;
        if(dataEquals(PING, packet.getData(), packet.getLength()))
        {
            DatagramPacket ack = new DatagramPacket(ACK, ACK.length,
                    packet.getAddress(), packet.getPort());
            mNetwork.send(ack);
            final DatagramPacket p = packet;
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mTextView.setText(mSelfNetwork + "\n" +
                            "Ping by: " + p.getAddress().getHostAddress());
                }
            });
        }
        else if(dataEquals(ACK, packet.getData(), packet.getLength()))
        {
            final DatagramPacket p = packet;
            if(mScanner != null && mScanner.isRunning)
            {
                mScanner.isRunning = false;
                mScanner.interrupt();
            }

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mTextView.setText(mSelfNetwork + "\n" +
                            "Got server: " + p.getAddress().getHostAddress());
                }
            });
        }
    }

    private static boolean dataEquals(byte[] d1, byte[] d2, int d2Len)
    {
        if(d1.length != d2Len)
            return false;
        for(int i = 0; i < d2Len; i++)
            if(d1[i] != d2[i])
                return false;
        return true;
    }

    public static InetAddress ipIntToAddress(int ip)
    {
        byte[] addr = new byte[4];
        for(int i = 0; i < 4; i++)
            addr[i] = (byte)((ip>>(i*8))&0xff);
        try
        {
            return InetAddress.getByAddress(addr);
        } catch (UnknownHostException e)
        {
            return null;
        }
    }

    class Scanner extends Thread
    {
        public boolean isRunning;

        @Override
        public void run()
        {
            isRunning = true;
            int mask = mDhcpInfo.netmask;
            int n = 0;
            while((mask<<n) > 0)
                n++;
            int count = (1<<n);
            if(count >= 256)
                count = 255;
            InetAddress netAdd = ipIntToAddress(mDhcpInfo.ipAddress);
            byte[] addr = netAdd.getAddress();
            int downward = addr[3] - 1;
            int upward = addr[3] + 1;
            DatagramPacket ping = new DatagramPacket(PING, PING.length);
            ping.setPort(mNetwork.mPort);
            while(downward > 0 || upward < count)
            {
                if(!isRunning)
                    return;
                if(downward > 0)
                {
                    addr[3] = (byte)downward;
                    try
                    {
                        netAdd = InetAddress.getByAddress(addr);
                        ping.setAddress(netAdd);
                        mNetwork.send(ping);
//                        Log.d(Constants.TAG, "Ping " + netAdd.getHostAddress());
                    } catch (UnknownHostException e)
                    {
                        Log.e(Constants.TAG, "Address error", e);
                    }
                    downward--;
                }

                if(upward < count)
                {
                    addr[3] = (byte)upward;
                    try
                    {
                        netAdd = InetAddress.getByAddress(addr);
                        ping.setAddress(netAdd);
                        mNetwork.send(ping);
//                        Log.d(Constants.TAG, "Ping " + netAdd.getHostAddress());
                    } catch (UnknownHostException e)
                    {
                        Log.e(Constants.TAG, "Address error", e);
                    }
                    upward++;
                }
            }
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mTextView.setText(mSelfNetwork);
                }
            });
        }
    }
}
