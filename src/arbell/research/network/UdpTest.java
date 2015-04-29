package arbell.research.network;

import android.app.Activity;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import arbell.research.R;
import arbell.research.util.Constants;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Author: YinLanShan
 * Date: 14-8-4
 * Time: 18:17
 */
public class UdpTest extends Activity implements View.OnClickListener
{
    private DhcpInfo mDhcpInfo;
    private DatagramSocket mSocket;
    private static int sPort;
    private Receiver mReceiverThread;
    private Scanner mScanner;
    private Transmitter mTransmitter;

    private DatagramPacket mSending;
    private TextView mRxRate;

    private final byte[] ACK = "ACK".getBytes();
    private final byte[] PING = "PING".getBytes();

    private Handler mHandler = new Handler();
    private int mPacketCount;

    private Runnable mSpeedMeter = new Runnable()
    {
        private int mCount;

        @Override
        public void run()
        {
            mHandler.postDelayed(this, 1000);
            mRxRate.setText(String.format("%d / %d", mPacketCount - mCount,
                    mPacketCount));
            mCount = mPacketCount;
        }
    };

    static
    {
        String s = "ivory";
        int low = 0, high = 0;
        for(int i = 0; i < s.length(); i++)
        {
            if((i&1) == 0)
                low += s.charAt(i);
            else
                high += s.charAt(i);
        }
        sPort = (high<<8)|low;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_udp_test);

        TextView self = (TextView)findViewById(R.id.self);
        Button scanButton = (Button)findViewById(R.id.scan);
        scanButton.setOnClickListener(this);
        Button sendButton = (Button)findViewById(R.id.send);
        sendButton.setOnClickListener(this);
        sendButton.setEnabled(false);
        mRxRate = (TextView)findViewById(R.id.rx_rate);

        WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo inf = wm.getConnectionInfo();
        if(inf == null || inf.getIpAddress() == 0)
        {
            self.setText("No wifi connection");
            scanButton.setEnabled(false);
            sendButton.setEnabled(false);
            return;
        }

        DhcpInfo info = wm.getDhcpInfo();
        String selfIp = "Self: " + ipIntToAddress(info.ipAddress) + ":" + sPort;
        self.setText(selfIp);
        mDhcpInfo = info;
        try
        {
            mSocket = new DatagramSocket(sPort);
        } catch (SocketException e)
        {
            Log.e(Constants.TAG, "Socket creating error", e);
            scanButton.setEnabled(false);
            return;
        }
        mReceiverThread = new Receiver();
        mReceiverThread.mListener = new ConnectionListener();
        mReceiverThread.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(mScanner != null)
        {
            if(!mScanner.isInterrupted())
                mScanner.interrupt();
        }
        if(mTransmitter != null)
        {
            mTransmitter.interrupt();
            mTransmitter = null;
        }
        if(mSocket != null)
        {
            mReceiverThread.interrupt();
            mSocket.close();
        }

        mHandler.removeCallbacks(mSpeedMeter);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.scan:
                findViewById(R.id.scan).setEnabled(false);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                mScanner = new Scanner();
                mScanner.start();
                break;
            case R.id.send:
                mTransmitter = new Transmitter();
                mTransmitter.start();
                break;
        }
    }

    public void onConnected()
    {
        if(mSending == null)
            return;
        TextView remote = (TextView)findViewById(R.id.remote);
        remote.setText(mSending.getAddress().getHostAddress());
        findViewById(R.id.send).setEnabled(true);
        findViewById(R.id.scan).setVisibility(View.GONE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        Log.d(Constants.TAG, "Connected");
        mHandler.post(mSpeedMeter);
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

    private static boolean dataEquals(byte[] d1, byte[] d2, int d2Len)
    {
        if(d1.length != d2Len)
            return false;
        for(int i = 0; i < d2Len; i++)
            if(d1[i] != d2[i])
                return false;
        return true;
    }

    class Receiver extends Thread
    {
        public Listener mListener;

        @Override
        public void run()
        {
            byte[] data = new byte[64];
            while (!isInterrupted())
            {
                try
                {
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    mSocket.receive(packet);
                    if(mListener != null)
                        mListener.onReceive(packet);
                } catch (IOException e)
                {
                    Log.i(Constants.TAG, "Socket receiving error", e);
                }
            }
            Log.d(Constants.TAG, "Receiver stopped");
        }
    }

    class ConnectionListener implements Listener
    {
        Runnable mNotifier = new Runnable()
        {
            @Override
            public void run()
            {
                onConnected();
            }
        };

        public void onReceive(DatagramPacket packet)
        {
            byte[] buf = packet.getData();
            int length = packet.getLength();
            if(dataEquals(PING, buf, length))
            {
                DatagramPacket ack = new DatagramPacket(new byte[64], 64);
                System.arraycopy(ACK, 0, ack.getData(), 0, ACK.length);
                ack.setLength(ACK.length);
                ack.setAddress(packet.getAddress());
                ack.setPort(packet.getPort());
                mSending = ack;
                runOnUiThread(mNotifier);
                Log.d(Constants.TAG, "Ping by " + packet.getAddress().getHostAddress());
                try
                {
                    mSocket.send(ack);
                } catch (IOException e)
                {
                    Log.e(Constants.TAG, "Acknowledge error", e);
                }
            }
            else if(dataEquals(ACK, buf, length))
            {
                DatagramPacket p = new DatagramPacket(new byte[64], 64);
                p.setAddress(packet.getAddress());
                p.setPort(packet.getPort());
                mSending = p;
                if(mScanner != null)
                {
                    mScanner.interrupt();
                    mScanner = null;
                }
                runOnUiThread(mNotifier);
                Log.d(Constants.TAG, "Got server " + packet.getAddress().getHostAddress());
            }
            else
            {
                Log.d(Constants.TAG, "Rx len:" + length);
                mPacketCount++;
            }
        }
    }

    class Scanner extends Thread
    {
        @Override
        public void run()
        {
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
            ping.setPort(sPort);
            while(downward > 0 || upward < count)
            {
                if(isInterrupted())
                {
                    Log.d(Constants.TAG, "Scanner is interrupted");
                    return;
                }
                if(downward > 0)
                {
                    addr[3] = (byte)downward;
                    try
                    {
                        netAdd = InetAddress.getByAddress(addr);
                        ping.setAddress(netAdd);
                        mSocket.send(ping);
                    } catch (IOException e)
                    {
                        Log.e(Constants.TAG, "Ping error", e);
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
                        mSocket.send(ping);
//                        Log.d(Constants.TAG, "Ping " + netAdd.getHostAddress());
                    } catch (IOException e)
                    {
                        Log.e(Constants.TAG, "Ping error", e);
                    }
                    upward++;
                }
            }
            if(isInterrupted())
            {
                Log.d(Constants.TAG, "Scanner is interrupted");
                return;
            }
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    findViewById(R.id.scan).setEnabled(true);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            });
        }
    }

    class Transmitter extends Thread
    {
        @Override
        public void run()
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    findViewById(R.id.send).setEnabled(false);
                }
            });
            DatagramPacket packet = mSending;
            byte[] data = packet.getData();
            Arrays.fill(data, (byte)0);
            packet.setLength(data.length);
            Log.d(Constants.TAG, "Tx len:" + packet.getLength());
            ByteBuffer bb = ByteBuffer.wrap(data);
            bb.order(ByteOrder.nativeOrder());
            try
            {
                for(int i = 0; i < 1000; i++)
                {
                    if(!isInterrupted())
                    {
                        long t = System.currentTimeMillis();
                        bb.putInt(0, i);
                        mSocket.send(packet);
                        while(System.currentTimeMillis() - t < 8);
                    }
                }
            }
            catch (IOException e)
            {
                Log.e(Constants.TAG, "Transmitting error");
            }

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    findViewById(R.id.send).setEnabled(true);
                }
            });
            mTransmitter = null;
        }
    }

    interface Listener
    {
        void onReceive(DatagramPacket packet);
    }
}
