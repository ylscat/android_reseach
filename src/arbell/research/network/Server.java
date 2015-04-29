package arbell.research.network;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import arbell.research.util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Author: YinLanShan
 * Date: 14-7-24
 * Time: 11:33
 */
public class Server extends Activity
{
    private DatagramSocket mServerSocket;
    private TextView mTextView;
    private Listener mListener = new Listener();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mTextView = new TextView(this);
        mTextView.setTextAppearance(this, android.R.style.TextAppearance_Large);
        setContentView(mTextView);
        try
        {
            final String TG = "TG";
            int port = 0;
            byte[] data = TG.getBytes();
            int len = data.length > 4 ? 4 : data.length;
            for(int i = 0; i < len; i++)
                port |= data[i]<<(i*8);
            mTextView.setText("port:" + port);
            mServerSocket = new DatagramSocket(port);
        }
        catch (IOException e)
        {
            Log.e(Constants.TAG, "Server error", e);
            return;
        }

        mListener.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mListener.isRunning = false;

        if(mServerSocket != null)
        {
            mServerSocket.disconnect();
            mServerSocket.close();
        }
    }

    class Listener extends Thread
    {
        public boolean isRunning = true;
        private String message;

        private Runnable mMessenger = new Runnable()
        {
            @Override
            public void run()
            {
                mTextView.setText(message);
            }
        };

        @Override
        public void run()
        {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

            while (isRunning)
            {
                try{
                    mServerSocket.receive(packet);
                    if(packet.getLength() == 2)
                    {
                        byte[] data = packet.getData();
                        if(data[0] == 0 && data[1] == 0)
                        {
                            data = new byte[2];
                            data[0] = 0;
                            data[1] = 1;
                            Log.d(Constants.TAG, "port: " + mServerSocket.getLocalPort());
                            DatagramPacket resp = new DatagramPacket(
                                    data, data.length, packet.getAddress(),
                                    mServerSocket.getLocalPort());
                            mServerSocket.send(resp);
                            message = "Client: " + packet.getAddress().getHostAddress();
                            runOnUiThread(mMessenger);
                        }
                    }
                } catch (IOException e)
                {
                    Log.e(Constants.TAG, "IO error", e);
                }
            }
        }
    }
}
