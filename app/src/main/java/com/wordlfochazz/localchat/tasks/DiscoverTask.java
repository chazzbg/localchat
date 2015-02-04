package com.wordlfochazz.localchat.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.wordlfochazz.localchat.MainActivity;
import com.wordlfochazz.localchat.listeners.PresenceListener;
import com.wordlfochazz.localchat.objects.PresenceObject;
import com.wordlfochazz.localchat.connection.DiscoverProtocol;

import java.io.IOException;
import java.net.SocketException;

public class DiscoverTask extends AsyncTask<Void,PresenceObject,Void> implements PresenceListener {
    private String TAG = "LC"+DiscoverTask.class.getSimpleName();
    private MainActivity activity;
    private DiscoverProtocol protocol;
    public DiscoverTask(DiscoverProtocol protocol,MainActivity activity) {
        this.activity = activity;
        this.protocol = protocol;
        this.protocol.setPresenceListener(this);
    }
    @Override
    public void onPresenceReceived(PresenceObject object) {
        PresenceObject[] arr= new PresenceObject[1];
        arr[0] = object;
        publishProgress(arr);
    }
    @Override
    protected Void doInBackground(Void... params) {
        try {
            openSocket();
            sendPresence(DiscoverProtocol.PRESENCE_FIRST);
            while (!isCancelled()){
              protocol.listenForPresence();
            }


        }  catch ( IOException e){
            Log.e(TAG,e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }


        return null;
    }

    public void sendPresence(int status) {

        try {
            protocol.sendPresence(status);
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    public void openSocket() throws SocketException {
        protocol.openSocket();
    }


    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        closeSocket();
    }




    @Override
    protected void onProgressUpdate(PresenceObject... objects) {
        super.onProgressUpdate(objects);

        PresenceObject object = objects[0];


        if(object.getStatus() == DiscoverProtocol.PRESENCE_BYE){
            activity.clearClient(object);
        } else {
            activity.pushClient(object);
        }

    }



    public void closeSocket() {
        protocol.closeSocket();
    }
}
