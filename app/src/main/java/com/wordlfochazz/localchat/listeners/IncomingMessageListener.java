package com.wordlfochazz.localchat.listeners;

import com.wordlfochazz.localchat.objects.MessageObject;

public interface IncomingMessageListener {
    public abstract void onIncomingMessage(MessageObject messageObject);

}
