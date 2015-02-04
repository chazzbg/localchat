package com.wordlfochazz.localchat.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.wordlfochazz.localchat.R;
import com.wordlfochazz.localchat.SettingsActivity;
import com.wordlfochazz.localchat.listeners.PresenceListener;
import com.wordlfochazz.localchat.objects.PresenceObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class DiscoverProtocol {

    public static final int PRESENCE_FIRST = 0;
    public static final int PRESENCE_ECHO = 1;
    public static final int PRESENCE_BYE = 2;
    private Context mContext;
    private PresenceObject mPresenceObject;
    private WifiManager mWifiManager;

    private String TAG = "LC"+DiscoverProtocol.class.getSimpleName();
    private DatagramSocket mDatagramSocket;

    private PresenceListener presenceListener;

    public static  final int PORT = 44555;



        public DiscoverProtocol(Context context) {
            mContext = context;
            if(this.mContext != null){
                mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            }
            preparePresence();

        }

    /**
     * Calculate the broadcast IP we need to send the packet along. If we send it
     * to 255.255.255.255, it never gets sent. I guess this has something to do
     * with the mobile network not wanting to do broadcast.
     */
    private InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = mWifiManager.getDhcpInfo();
        if (dhcp == null) {
            Log.d(TAG, "Could not get dhcp info");
            return null;
        }

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    public void openSocket() throws SocketException {
        mDatagramSocket = new DatagramSocket(PORT);
        mDatagramSocket.setBroadcast(true);
    }

    public void closeSocket() {
        if(mDatagramSocket != null)
            mDatagramSocket.close();
    }


    public void preparePresence(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String clientName = prefs.getString(mContext.getString(R.string.pref_username_key), SettingsActivity.generateNewName());

        mPresenceObject = new PresenceObject();
        mPresenceObject.setClientName(clientName);

        WifiInfo mWifiConnectionInfo = mWifiManager.getConnectionInfo();

        mPresenceObject.setClientID(mWifiConnectionInfo.getMacAddress());
    }


    public void sendPresence (int status) throws IOException {
            mPresenceObject.setStatus(status);
            ByteArrayOutputStream byteArray  = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(byteArray);

            oos.writeObject(mPresenceObject);
            oos.flush();

            byte[] sendBuf = byteArray.toByteArray();

            DatagramPacket packet = new DatagramPacket(sendBuf,sendBuf.length, getBroadcastAddress(),PORT);
            mDatagramSocket.send(packet);

            oos.close();


    }


    public PresenceObject getPreparedPresenceObject() {
        preparePresence();
        return  mPresenceObject;
    }

    public void listenForPresence() throws IOException, ClassNotFoundException {

            byte[] buff = new byte[5000];
            DatagramPacket packet = new DatagramPacket(buff,buff.length);

            mDatagramSocket.receive(packet);

            ByteArrayInputStream byteStream  =new ByteArrayInputStream(buff);
            ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
            PresenceObject presenceObject = (PresenceObject) is.readObject();
//            Log.d(TAG, "Received presence: " + presenceObject.toString());


            if(presenceObject.getStatus() == PRESENCE_FIRST)
                sendPresence(PRESENCE_ECHO);

            if(!presenceObject.getClientID().equals(mPresenceObject.getClientID()) ){
                presenceObject.setAddress(packet.getAddress());
                if(presenceListener != null)
                    presenceListener.onPresenceReceived(presenceObject);
            }
            is.close();

    }


    public void setPresenceListener(PresenceListener presenceListener) {
        this.presenceListener = presenceListener;
    }
}
