package com.wordlfochazz.localchat.connection;

import android.util.Log;

import com.wordlfochazz.localchat.MainActivity;
import com.wordlfochazz.localchat.listeners.IncomingMessageListener;
import com.wordlfochazz.localchat.objects.MessageObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class MessagingServer {
    public static  final int PORT = 44556;
    public static final String TAG  = "LC"+MessagingServer.class.getSimpleName();
    private boolean running = false;
    private Socket mSocket;
    private ServerSocket  mServer;
    private MessageObject message;

    private IncomingMessageListener messageListener;
    public MessagingServer() {

    }

    public void startServer() {
        Log.d(TAG,"Staring messaging server");
        try {
            mServer = new ServerSocket(PORT);
            running = true;
            Log.d(TAG,"Messaging server is started");
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }



    public void listenForMessages() {
        if (!isRunning()) return;

        Log.d(TAG,"Should try to listen for messages");
        try {
            while (isRunning()) {

                mSocket = mServer.accept();
                ObjectInputStream oin = new ObjectInputStream(mSocket.getInputStream());

                message = (MessageObject) oin.readObject();

                message.setTime(System.currentTimeMillis() / 1000);
                if(message.hasFile())
                    processMessageFile();

                if (messageListener != null)
                    messageListener.onIncomingMessage(message);
                oin.close();

            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private void processMessageFile() throws IOException {
        if(!MainActivity.STORAGE_DIR.exists()){
            if(!MainActivity.STORAGE_DIR.mkdirs())
                throw new IOException("Could not create directory "+MainActivity.STORAGE_DIR.getAbsoluteFile());
        }
        InputStream in = mSocket.getInputStream();

        FileOutputStream fout = new FileOutputStream(new File(MainActivity.STORAGE_DIR.getAbsolutePath()+"/"+message.getTime()));
        byte[] buffer = new byte[128];
        while (in.read(buffer) != -1) {
            fout.write(buffer);
            fout.flush();
        }

        fout.close();
        in.close();
    }

    public void setOnIncomingMessageListener(IncomingMessageListener listener){
        messageListener = listener;
    }

    public void stopServer(){
        try {
            if(mSocket != null)
                mSocket.close();
        } catch (IOException e) {
           Log.d(TAG,e.getMessage());
        } finally {
            running = false;
        }

    }
}
