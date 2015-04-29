package arbell.research.network;

import android.util.Log;
import arbell.research.util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

class UdpNetwork
{
    private DatagramSocket mSocket;

    private ReceiveThread mReceiveThread;
    private boolean isRunning;

    private Listener mListener;
    public int mPort;

    public UdpNetwork()
    {
        String TG = "TG";
        int port = TG.charAt(0) |(TG.charAt(1) << 8);
        mPort = port;
        try
        {
            mSocket = new DatagramSocket(port);
        } catch (SocketException e)
        {
            Log.e(Constants.TAG, "Socket error", e);
        }
    }

    public void start()
    {
        if(mSocket == null)
            throw new RuntimeException("Socket can not setup");
        mReceiveThread = new ReceiveThread();
        isRunning = true;
        mReceiveThread.start();
    }

    public void stop()
    {
        isRunning = false;
        if(mReceiveThread != null)
            mReceiveThread.interrupt();
        if(mSocket != null)
            mSocket.close();
    }

    public void send(DatagramPacket packet)
    {
        if(packet != null)
        {
            try
            {
                mSocket.send(packet);
            } catch (IOException e)
            {
                Log.e(Constants.TAG, "Send error", e);
            }
        }
    }

    class ReceiveThread extends Thread
    {
        @Override
        public void run()
        {
            while (isRunning)
            {
                DatagramPacket packet = new DatagramPacket(new byte[128], 128);
                try
                {
                    mSocket.receive(packet);
                    if(mListener != null)
                        mListener.onReceive(packet);
                }
                catch (IOException e)
                {
                    Log.e(Constants.TAG, "Receive failed", e);
                }
            }
        }
    }

    public void setListener(Listener listener)
    {
        mListener = listener;
    }

    public static interface Listener{
        void onReceive(DatagramPacket packet);
    }
}