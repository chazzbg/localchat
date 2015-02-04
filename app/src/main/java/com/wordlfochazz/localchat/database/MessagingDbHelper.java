package com.wordlfochazz.localchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wordlfochazz.localchat.database.contracts.MessageContract;
import com.wordlfochazz.localchat.database.contracts.ThreadsContract;
import com.wordlfochazz.localchat.listeners.MessagingDataSetChangedListener;
import com.wordlfochazz.localchat.objects.MessageObject;
import com.wordlfochazz.localchat.objects.PresenceObject;

public class MessagingDbHelper  extends SQLiteOpenHelper {
    public static final String TAG = MessagingDbHelper.class.getSimpleName();
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Messaging.db";

    private static MessagingDbHelper helper;

    private MessagingDataSetChangedListener dataSetChangedListener;
    private static final String SQL_CREATE_THREAD_ENTRIES =
            "CREATE TABLE " + ThreadsContract.TABLE_NAME + " ( " +
                    ThreadsContract._ID + " INTEGER PRIMARY KEY, " +
                    ThreadsContract.COL_CLIENT_NAME + " TEXT, " +
                    ThreadsContract.COL_CLIENT_ID +" TEXT "+
            " );";

    private static final String SQL_DELETE_THREAD_ENTRIES =
            "DROP TABLE IF EXISTS " + ThreadsContract.TABLE_NAME;

    private static final String SQL_CREATE_MESSAGE_ENTRIES =
            "CREATE TABLE " + MessageContract.TABLE_NAME + " (" +
                    MessageContract._ID + " INTEGER PRIMARY KEY," +
                    MessageContract.COL_THREAD_ID +" INTEGER, " +
                    MessageContract.COL_TYPE +" INTEGER, " +
                    MessageContract.COL_MESSAGE_CONTENT + " TEXT, "+
                    MessageContract.COL_TIME + " INTEGER, "+
                    MessageContract.COL_HAS_FILE + " INTEGER, "+
                    MessageContract.COL_SENT_FILE + " TEXT "+
                    " );";

    private static final String SQL_DELETE_MESSAGE_ENTRIES =
            "DROP TABLE IF EXISTS " + MessageContract.TABLE_NAME;

    public MessagingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);


    }

    public static MessagingDbHelper getInstance(Context context){
        if(helper==null)
            helper = new MessagingDbHelper(context);

        return helper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_THREAD_ENTRIES);
        Log.d(TAG,SQL_CREATE_THREAD_ENTRIES);
        db.execSQL(SQL_CREATE_MESSAGE_ENTRIES);
        Log.d(TAG,SQL_CREATE_MESSAGE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_THREAD_ENTRIES);
        db.execSQL(SQL_DELETE_MESSAGE_ENTRIES);
    }

    public Cursor getThread(PresenceObject object){


        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
            ThreadsContract._ID,
            ThreadsContract.COL_CLIENT_ID,
            ThreadsContract.COL_CLIENT_NAME
        };
        String whereClause = ThreadsContract.COL_CLIENT_ID+"=?";
        String [] whereArgs = {
                object.getClientID()
        };
        Cursor c = db.query(
                ThreadsContract.TABLE_NAME,
                projection,
                whereClause,
                whereArgs,
                null,null,null
        );

        if(c.getCount() == 0) {
            createThread(object);
            return  getThread(object);
        }

        return c;
    }


    public long getThreadId(PresenceObject object){
        Cursor c= getThread(object);


        c.moveToFirst();

      return   c.getLong(c.getColumnIndex(ThreadsContract._ID));
    }
    public long createThread(PresenceObject object){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ThreadsContract.COL_CLIENT_ID,object.getClientID());
        values.put(ThreadsContract.COL_CLIENT_NAME,object.getClientName());

        return db.insert(
                ThreadsContract.TABLE_NAME,
                null,
                values
        );
    }

    public Cursor getThreadMessages(long thread_id){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                MessageContract._ID,
                MessageContract.COL_TYPE,
                MessageContract.COL_MESSAGE_CONTENT,
                MessageContract.COL_TIME,
                MessageContract.COL_HAS_FILE,
                MessageContract.COL_SENT_FILE
        };
        String whereClause = MessageContract.COL_THREAD_ID+"=?";
        String [] whereArgs = {
                String.valueOf(thread_id),
        };
//        Cursor c = db.query(
//                MessageContract.TABLE_NAME,
//                projection,
//                whereClause,
//                whereArgs,
//                null,null,null
//        );

        Cursor c = db.rawQuery("SELECT *,time as unixtime, datetime(time, 'unixepoch') as time " +
                "FROM "+MessageContract.TABLE_NAME +" " +
                "CROSS JOIN "+ThreadsContract.TABLE_NAME +" " +
                "WHERE  "+MessageContract.COL_THREAD_ID+" = ? " +
                "ORDER BY "+MessageContract.COL_TIME+ " ASC",
                whereArgs);
        c.moveToFirst();
        return c;
    }


    public long putMessage(long thread_id, MessageObject message){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MessageContract.COL_TYPE, message.getType());
        values.put(MessageContract.COL_THREAD_ID,thread_id);
        values.put(MessageContract.COL_MESSAGE_CONTENT, message.getMessage());
        values.put(MessageContract.COL_TIME, message.getTime());
        if(message.hasFile())
            values.put(MessageContract.COL_HAS_FILE, 1);
        else
            values.put(MessageContract.COL_HAS_FILE, 0);

        if(message.getSent_file() != null){
            values.put(MessageContract.COL_SENT_FILE,message.getSent_file());
        } else {
            values.put(MessageContract.COL_SENT_FILE,"");
        }

        long insert_id = db.insert(
                MessageContract.TABLE_NAME,
                null,
                values
        );

        if(dataSetChangedListener != null) {
            dataSetChangedListener.dataSetChanged();
        }
        return insert_id;


    }

    public void setDataSetChangedListener(MessagingDataSetChangedListener dataSetChangedListener) {
        this.dataSetChangedListener = dataSetChangedListener;
    }
}


