package com.wordlfochazz.localchat.objects;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class PresenceObject implements Serializable {

    protected String clientName;
    protected String clientID;
    protected InetAddress address;
    protected int status;


    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        byte[] bytes = clientID.getBytes();

        Checksum checksum = new CRC32();
        checksum.update(bytes,0,bytes.length);

        long checksumValue = checksum.getValue();

        this.clientID = String.valueOf(checksumValue);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "PresenceObject{" +
                "clientName='" + clientName + '\'' +
                ", clientID='" + clientID + '\'' +
                '}';
    }
}
