package com.wordlfochazz.localchat.listeners;

import com.wordlfochazz.localchat.objects.PresenceObject;

public interface PresenceListener {

    public void onPresenceReceived(PresenceObject object);
}
