package arbell.research.network;

import android.app.Activity;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;
import arbell.research.util.Constants;

import java.io.IOException;
import java.net.*;

/**
 * Author: YinLanShan
 * Date: 14-7-24
 * Time: 11:59
 */
@SuppressWarnings("deprecation")
public class Client extends Activity
{
    private TextView mTextView;
    private DhcpInfo mDhcpInfo;
    private Socket mServer;
    private Scanner mScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mTextView = new TextView(this);
        mTextView.setTextAppearance(this, android.R.style.TextAppearance_Large);
        setContentView(mTextView);
        WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo inf = wm.getConnectionInfo();
        if(inf == null || inf.getIpAddress() == 0)
        {
            mTextView.setText("No wifi connection");
            return;
        }
        DhcpInfo info = wm.getDhcpInfo();
        mTextView.setText(String.format("IP:%s\nMask:%s\nGateway:%s",
                Formatter.formatIpAddress(info.ipAddress),
                Formatter.formatIpAddress(info.netmask),
                Formatter.formatIpAddress(info.gateway)));
        mDhcpInfo = info;

        new Thread(){
            @Override
            public void run()
            {
                int port = getPort();
                try
                {
                    DatagramSocket socket = new DatagramSocket(port);
                    DatagramPacket packet = new DatagramPacket(new byte[2], 2,
                            InetAddress.getByName("192.168.1.104"), port);
                    DatagramPacket receive = new DatagramPacket(new byte[1024], 1024);
                    socket.setSoTimeout(500);
                    socket.send(packet);
                    socket.receive(receive);
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mTextView.setText("OK");
                        }
                    });
                }
                catch (IOException e)
                {
                    Log.e(Constants.TAG, "IO Exception", e);
                }
            }
        }.start();

//        mScanner = new Scanner();
//        mScanner.start();
    }

    private int getPort()
    {
        final String TG = "TG";
        int port = 0;
        byte[] data = TG.getBytes();
        int len = data.length > 4 ? 4 : data.length;
        for(int i = 0; i < len; i++)
            port |= data[i]<<(i*8);
        return port;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(mScanner != null)
            mScanner.isRunning = false;
        if(mServer != null)
            try
            {
                mServer.close();
            } catch (IOException e)
            {
                //ignore
            }
    }

    class Scanner extends Thread
    {
        public boolean isRunning = true;

        @Override
        public void run()
        {
            int mask = mDhcpInfo.netmask;
            int n = 0;
            while((mask<<n) > 0)
                n++;
            int count = (1<<n);
            if(count > 256)
                count = 254;
            int lower = ((mDhcpInfo.ipAddress>>24)&0xff) - 1;
            int upper = ((mDhcpInfo.ipAddress>>24)&0xff) + 1;
            int subnet = mDhcpInfo.ipAddress&(0xffffff);

            int port = getPort();
            byte[] addressBytes = new byte[4];
            InetAddress address;
            Socket server = null;
            while(isRunning && lower != 0 && upper != 255)
            {
                int ip = (lower<<24) | subnet;
                addressBytes[0] = (byte)(0xff & ip);
                addressBytes[1] = (byte)(0xff & (ip >> 8));
                addressBytes[2] = (byte)(0xff & (ip >> 16));
                addressBytes[3] = (byte)(0xff & (ip >> 24));

                try
                {
                    address = InetAddress.getByAddress(addressBytes);
                    server = connectTo(address, port);
                } catch (UnknownHostException e)
                {
                    Log.e(Constants.TAG, e.getMessage());
                }
                lower--;
                if(server != null)
                    break;

                ip = (upper<<24) | subnet;
                addressBytes[0] = (byte)(0xff & ip);
                addressBytes[1] = (byte)(0xff & (ip >> 8));
                addressBytes[2] = (byte)(0xff & (ip >> 16));
                addressBytes[3] = (byte)(0xff & (ip >> 24));

                try
                {
                    address = InetAddress.getByAddress(addressBytes);
                    server = connectTo(address, port);
                } catch (UnknownHostException e)
                {
                    Log.e(Constants.TAG, e.getMessage());
                }
                upper++;
                if(server != null)
                    break;
            }

            mServer = server;
            if(mServer != null)
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mTextView.setText("Server: " + mServer.getInetAddress().getHostAddress());
                    }
                });
        }

        private Socket connectTo(InetAddress addr, int port)
        {
            try
            {
                return new Socket(addr, port);
            } catch (UnknownHostException e)
            {
                Log.e(Constants.TAG, addr.getHostAddress() + " failed 0");
                return null;
            } catch (IOException e)
            {
                Log.e(Constants.TAG, addr.getHostAddress() + " failed 1");
                return null;
            }
        }
    }
}
