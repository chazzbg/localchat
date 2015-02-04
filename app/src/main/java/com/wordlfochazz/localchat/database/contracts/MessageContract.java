package com.wordlfochazz.localchat.database.contracts;

import android.provider.BaseColumns;

/**
 * Created by dilkov on 30.01.15.
 */
public class MessageContract implements BaseColumns {

    public static final String TABLE_NAME = "messages";

    public static final String COL_THREAD_ID = "thread_id";
    public static final String COL_TYPE = "message_type";
    public static final String COL_MESSAGE_CONTENT = "message_content";
    public static final String COL_TIME = "time";
    public static final String COL_HAS_FILE = "has_file";
    public static final String COL_UNIXTIME = "unixtime";
    public static final String COL_SENT_FILE = "sent_file";

}
