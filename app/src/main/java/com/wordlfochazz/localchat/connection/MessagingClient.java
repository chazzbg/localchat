package com.wordlfochazz.localchat.connection;

import android.util.Log;

import com.wordlfochazz.localchat.objects.MessageObject;
import com.wordlfochazz.localchat.objects.PresenceObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MessagingClient {
    public static final String TAG = "LC"+MessagingClient.class.getSimpleName();

    private PresenceObject presenceObject;
    private MessageObject messageObject;

    public PresenceObject getPresenceObject() {
        return presenceObject;
    }

    private String selectedImagePath;
    public void setPresenceObject(PresenceObject presenceObject) {
        this.presenceObject = presenceObject;
    }

    public MessageObject getMessageObject() {
        return messageObject;
    }

    public void setMessageObject(MessageObject messageObject) {
        this.messageObject = messageObject;
    }

    public String getSelectedImagePath() {
        return selectedImagePath;
    }

    public void setSelectedImagePath(String selectedImagePath) {
        this.selectedImagePath = selectedImagePath;
    }

    public void sendMessage(){
        try {
            Log.d(TAG,"Trying to send message");
            Socket socket = new Socket(presenceObject.getAddress(),MessagingServer.PORT);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            if(selectedImagePath == null){
                messageObject.setHasFile(false);
            }
            oos.writeObject(messageObject);
            oos.flush();
            if(messageObject.hasFile()){
                OutputStream os = socket.getOutputStream();
                FileInputStream fin= new FileInputStream(new File(selectedImagePath));

                byte[] buffer = new byte[128];
                while (fin.read(buffer) != -1){
                    os.write(buffer);
                    os.flush();
                }

                os.close();
                fin.close();
            }
            oos.close();
            socket.close();

            messageObject.setSent_file(selectedImagePath);
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }

    }
}
