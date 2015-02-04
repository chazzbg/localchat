package com.wordlfochazz.localchat.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.wordlfochazz.localchat.MainActivity;
import com.wordlfochazz.localchat.database.MessagingDbHelper;
import com.wordlfochazz.localchat.listeners.IncomingMessageListener;
import com.wordlfochazz.localchat.objects.MessageObject;
import com.wordlfochazz.localchat.objects.PresenceObject;
import com.wordlfochazz.localchat.connection.MessagingServer;

public class MessagingServerTask extends AsyncTask<Void , Object , Void> implements IncomingMessageListener {
    public static final String TAG  = "LC"+MessagingServerTask.class.getSimpleName();
    private MessagingServer server;
    public   MainActivity activity;

    public MessagingServerTask(MessagingServer server, final MainActivity activity) {
        this.activity = activity;
        this.server = server;
        this.server.setOnIncomingMessageListener(this);
    }
    @Override
    public void onIncomingMessage(MessageObject messageObject) {
        Log.d(TAG, messageObject.toString());
        PresenceObject object = activity.getClient(messageObject.getSenderId());

        messageObject.setType(MessageObject.TYPE_INCOMING);


        Object[] obj = {
                messageObject,
                object
        };
        publishProgress(obj);

    }
    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG,"Running messaging task");
        server.startServer();
        if(server.isRunning() ){

            while (server.isRunning() && !isCancelled())
                server.listenForMessages();
        } else {
            Log.d(TAG,"messaging server is not running");
        }
        server.stopServer();
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        MessageObject messageObject = (MessageObject) values[0];
        PresenceObject object = (PresenceObject) values[1];
        MessagingDbHelper dbHelper = MessagingDbHelper.getInstance(activity.getApplicationContext());
        long thread_id = dbHelper.getThreadId(object);
        dbHelper.putMessage(thread_id,messageObject);
    }
}
