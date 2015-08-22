package arbell.research.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import arbell.research.R;
import arbell.research.ui.view.GraphView;
import arbell.research.util.Constants;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: YinLanShan
 * Date: 14-9-23
 * Time: 11:33
 */
public class NetworkProfiler extends Activity implements View.OnClickListener
{
    private static final int PORT = 'C'|('A' << 8);

    private WifiBroadcastReceiver mBroadcastReceiver =
            new WifiBroadcastReceiver();

    private static final int
            CONNECTION_DISABLED = 0,
            CONNECTION_WAITING = 1,
            CONNECTION_CONNECTING = 2,
            CONNECTION_AS_SERVER = 3,
            CONNECTION_AS_CLIENT = 4;
    private int mConnectionState = -1;
    private TextView mStateView;
    private ColorDrawable mStateIcon;
    private IconBlinker mBlinker = new IconBlinker();
    private InetAddress mRemote;

    private UdpService mUdp;
    private DiscoveringService mDiscover;
    private ProfilerService mProfiler;

    private static Handler sHandler = new Handler();

    private GraphView mGraph;
    private TextView mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_profile);
        mStateView = (TextView)findViewById(R.id.connection_state);
        mStateIcon = new ColorDrawable();
        int size = (int)(15*getResources().getDisplayMetrics().density);
        mStateIcon.setBounds(0, 0, size, size);
        mStateView.setCompoundDrawables(mStateIcon, null, null, null);
        setState(CONNECTION_DISABLED);
        mStateView.setOnClickListener(this);

        mUdp = new UdpService(PORT, null);
        mUdp.start();

        mGraph = (GraphView)findViewById(R.id.graphView);
        mMessage = (TextView)findViewById(R.id.text);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mBroadcastReceiver,
                new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        setState(CONNECTION_DISABLED);
        if(mUdp != null)
        {
            mUdp.stop();
        }
    }

    public void setState(int state)
    {
        if(state == mConnectionState)
            return;
        if(mConnectionState == CONNECTION_CONNECTING)
        {
            sHandler.removeCallbacks(mBlinker);
        }
        switch (state)
        {
            case CONNECTION_DISABLED:
                mStateIcon.setColor(Color.RED);
                mStateView.setText("No Wifi");
                if(mProfiler != null)
                {
                    mProfiler.stop();
                    mProfiler = null;
                }
                if(mDiscover != null)
                {
                    mDiscover = null;
                }
                if(mUdp != null)
                    mUdp.setReceiver(null);
                break;
            case CONNECTION_WAITING:
                mStateIcon.setColor(Color.YELLOW);
                mStateView.setText("Waiting");
                if(mDiscover == null)
                {
                    mDiscover = new DiscoveringService(mUdp);
                }
                if(mProfiler != null)
                {
                    mProfiler.stop();
                    mProfiler = null;
                }
                break;
            case CONNECTION_CONNECTING:
                sHandler.post(mBlinker);
                break;
            case CONNECTION_AS_SERVER:
                mStateIcon.setColor(Color.GREEN);
                mStateView.setText("Server for " + mRemote.getHostAddress());
                mProfiler = new ProfilerService(mRemote);
                mUdp.setReceiver(mProfiler);
                mProfiler.start();
                mDiscover = null;
                break;
            case CONNECTION_AS_CLIENT:
                mStateIcon.setColor(Color.BLUE);
                mStateView.setText("Client for " + mRemote.getHostAddress());
                mProfiler = new ProfilerService(mRemote);
                mUdp.setReceiver(mProfiler);
                mProfiler.start();
                mDiscover = null;
                break;
        }
        mConnectionState = state;
    }

    public void postState(final int state)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                setState(state);
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (mConnectionState)
        {
            case CONNECTION_WAITING:
                setState(CONNECTION_CONNECTING);
                mDiscover.broadcast();
                break;
            case CONNECTION_AS_CLIENT:
            case CONNECTION_AS_SERVER:
                setState(CONNECTION_WAITING);
                break;
        }
    }

    private InetAddress ipIntToAddress(int ip)
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

    public class WifiBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if(info != null)
            {
                if(info.getState() == NetworkInfo.State.CONNECTED)
                {
                    setState(CONNECTION_WAITING);
                }
                else
                {
                    setState(CONNECTION_DISABLED);
                }
            }
        }
    }

    public class IconBlinker implements Runnable
    {
        @Override
        public void run()
        {
            if(mStateIcon.getColor() == Color.YELLOW)
                mStateIcon.setColor(Color.TRANSPARENT);
            else
                mStateIcon.setColor(Color.YELLOW);
            sHandler.postDelayed(this, 500);
        }
    }

    class DiscoveringService implements Receiver
    {
        private UdpService udp;
        private byte TYPE_COMMAND = 'C';
        private byte ACTION_PING = 'P';
        private byte ACTION_ACK = 'A';

        public DiscoveringService(UdpService udp)
        {
            this.udp = udp;
            udp.setReceiver(this);
        }

        @Override
        public void onDataReceived(byte[] data, int length, InetAddress remote)
        {
            if(length >= 2 && data[0] == TYPE_COMMAND)
            {
                if(data[1] == ACTION_ACK)
                {
                    mRemote = remote;
                    postState(CONNECTION_AS_CLIENT);
                }
                else if(data[1] == ACTION_PING)
                {
                    mRemote = remote;
                    byte[] payload = new byte[]{TYPE_COMMAND, ACTION_ACK};
                    DatagramPacket ack = new DatagramPacket(payload, payload.length, remote, PORT);
                    udp.send(ack);
                    postState(CONNECTION_AS_SERVER);
                }
                //ignore others
            }
        }

        public void broadcast()
        {
            WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
            final DhcpInfo info = wm.getDhcpInfo();

            Thread scan = new Thread()
            {
                @Override
                public void run()
                {
                    int mask = info.netmask;
                    int n = 0;
                    while ((mask << n) > 0)
                        n++;
                    int max = (1 << n);
                    if (max >= 256)
                        max = 255;
                    InetAddress local = ipIntToAddress(info.ipAddress);

                    byte[] ip = local.getAddress();
                    int downward = (0xff&ip[3]) - 1;
                    int upward = (0xff&ip[3]) + 1;
                    InetAddress target;
                    byte[] ping = new byte[]{TYPE_COMMAND, ACTION_PING};
                    DatagramPacket packet = new DatagramPacket(ping, ping.length);
                    packet.setLength(ping.length);
                    packet.setPort(PORT);
                    while (downward > 0 || upward < max)
                    {
                        if (mConnectionState != CONNECTION_CONNECTING)
                            break;
                        if (downward > 0)
                        {
                            ip[3] = (byte) downward;
                            try
                            {
                                target = InetAddress.getByAddress(ip);
                                packet.setAddress(target);
                                udp.send(packet);
                            } catch (UnknownHostException e)
                            {
                                Log.e(Constants.TAG, "Address error", e);
                            }
                            downward--;
                        }

                        if (upward < max)
                        {
                            ip[3] = (byte) upward;
                            try
                            {
                                target = InetAddress.getByAddress(ip);
                                packet.setAddress(target);
                                udp.send(packet);
                            } catch (UnknownHostException e)
                            {
                                Log.e(Constants.TAG, "Address error", e);
                            }
                            upward++;
                        }
                    }
                    if(mConnectionState == CONNECTION_CONNECTING)
                        postState(CONNECTION_WAITING);
                }
            };
            scan.start();
        }
    }

    class ProfilerService implements Receiver
    {
        private DatagramPacket mTxPayload;
        private long timestamp;
        private ScheduledThreadPoolExecutor mExecutor;
        private int max, min = Integer.MAX_VALUE;

        private Runnable mStat = new Runnable()
        {
            @Override
            public void run()
            {
                sHandler.postDelayed(this, 1000);
                mMessage.setText(String.format("[%d %d]", min, max));
                max = 0;
                min = Integer.MAX_VALUE;
            }
        };

        private Runnable mSender = new Runnable()
        {
            @Override
            public void run()
            {
                mUdp.send(mTxPayload);
            }
        };

        public ProfilerService(InetAddress remote)
        {
            byte[] data = new byte[64];
            for(int i = 0; i < data.length; i++)
                data[i] = (byte)i;
            mTxPayload = new DatagramPacket(data, data.length);
            mTxPayload.setPort(PORT);
            mTxPayload.setAddress(remote);
            mTxPayload.setLength(data.length);
            mExecutor = new ScheduledThreadPoolExecutor(1);
        }

        public void start()
        {
            mExecutor.scheduleAtFixedRate(mSender, 15, 15, TimeUnit.MILLISECONDS);
            sHandler.postDelayed(mStat, 1000);
        }

        public void stop()
        {
            mExecutor.shutdownNow();
            sHandler.removeCallbacks(mStat);
        }

        @Override
        public void onDataReceived(byte[] data, int length, InetAddress remote)
        {
            long t = System.currentTimeMillis();
            if(timestamp != 0)
            {
                long d = t - timestamp;
                if(d < min)
                    min = (int)d;
                if(d > max)
                    max = (int)d;
                float v = (t - timestamp)/50f;
                mGraph.push(v);
                mGraph.postInvalidate();
            }
            timestamp = t;
        }
    }
}

interface Receiver
{
    void onDataReceived(byte[] data, int length, InetAddress remote);
}

class UdpService
{
    private DatagramSocket mSocket;
    private Receiver mReceiver;

    private TxThread mTx;
    private RxThread mRx;

    private LinkedBlockingQueue<DatagramPacket> mTxFifo =
            new LinkedBlockingQueue<DatagramPacket>(5);

    public UdpService(int port, Receiver receiver)
    {
        try
        {
            mSocket = new DatagramSocket(port);
        } catch (SocketException e)
        {
            throw new RuntimeException(e);
        }
        mReceiver = receiver;
    }

    public void setReceiver(Receiver receiver)
    {
        mReceiver = receiver;
    }

    public void start()
    {
        mTx = new TxThread();
        mRx = new RxThread();
        mTx.start();
        mRx.start();
    }

    public void stop()
    {
        mTx.interrupt();
        mRx.interrupt();
        mSocket.close();
    }

    public void queueSending(DatagramPacket packet)
    {
        mTxFifo.add(packet);
    }

    public void send(DatagramPacket packet)
    {
        try
        {
            mSocket.send(packet);
        } catch (IOException e)
        {
            Log.e(Constants.TAG, "Sending failure", e);
        }
    }

    private class TxThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                while (!isInterrupted())
                {
                    DatagramPacket packet = mTxFifo.take();
                    mSocket.send(packet);
                }
            } catch (InterruptedException e)
            {
                Log.d(Constants.TAG, "TxThread stopped");
            } catch (IOException e)
            {
                Log.e(Constants.TAG, "TxThread IOException", e);
            }
        }
    }

    private class RxThread extends Thread
    {
        @Override
        public void run()
        {
            byte[] data = new byte[64];
            DatagramPacket packet;
            try
            {
                while (!isInterrupted())
                {
                    packet = new DatagramPacket(data, data.length);
                    mSocket.receive(packet);
                    if(mReceiver != null)
                    {
                        mReceiver.onDataReceived(data, packet.getLength(), packet.getAddress());
                    }
                }
            }
            catch (IOException e)
            {
                Log.e(Constants.TAG, "RxThread IOException", e);
            }
        }
    }
}

