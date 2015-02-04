package com.wordlfochazz.localchat.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.wordlfochazz.localchat.MainActivity;
import com.wordlfochazz.localchat.database.MessagingDbHelper;
import com.wordlfochazz.localchat.objects.PresenceObject;
import com.wordlfochazz.localchat.connection.MessagingClient;

public class MessagingClientTask extends AsyncTask<Void,Void,Void> {
    public static final String TAG = "LC"+MessagingClientTask.class.getSimpleName();
    private MessagingClient messagingClient;
    private MainActivity activity;

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public MessagingClientTask(MessagingClient messagingClient) {
        this.messagingClient = messagingClient;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG,"Sending message");




        messagingClient.sendMessage();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        MessagingDbHelper dbHelper = MessagingDbHelper.getInstance(activity.getApplicationContext());
        PresenceObject client = activity.getClient(messagingClient.getMessageObject().getReceiverId());
        long thread_id = dbHelper.getThreadId(client);
        dbHelper.putMessage(thread_id,messagingClient.getMessageObject());
    }
}
