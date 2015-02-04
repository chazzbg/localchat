package com.wordlfochazz.localchat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.wordlfochazz.localchat.database.contracts.MessageContract;
import com.wordlfochazz.localchat.database.contracts.ThreadsContract;
import com.wordlfochazz.localchat.objects.MessageObject;
import com.wordlfochazz.localchat.objects.PresenceObject;

public class MessagesAdapter extends CursorAdapter {
    private static final String TAG = MessagesAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;

    private Context context;
//    private static HashMap<int,int>
    private PresenceObject personalPresence;
    public MessagesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

            return mInflater.inflate(R.layout.message_left,parent,false);

    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        int messageType = cursor.getInt(cursor.getColumnIndex(MessageContract.COL_TYPE));

        TextView txtSender = (TextView) view.findViewById(R.id.txtSender);
        TextView txtDate= (TextView) view.findViewById(R.id.txtDate);
        TextView txtMessage = (TextView) view.findViewById(R.id.txtMessage);
        Button btnattachment = (Button) view.findViewById(R.id.btnAttachment);

        final String clientName = cursor.getString(cursor.getColumnIndex(ThreadsContract.COL_CLIENT_NAME));
        final String time = cursor.getString(cursor.getColumnIndex(MessageContract.COL_TIME));
        final String unixtime = cursor.getString(cursor.getColumnIndex(MessageContract.COL_UNIXTIME));
        final String message = cursor.getString(cursor.getColumnIndex(MessageContract.COL_MESSAGE_CONTENT));
        final int has_file = cursor.getInt(cursor.getColumnIndex(MessageContract.COL_HAS_FILE));
        final String send_file = cursor.getString(cursor.getColumnIndex(MessageContract.COL_SENT_FILE));

        if(messageType == MessageObject.TYPE_INCOMING){
            if(has_file ==1 ) {
                btnattachment.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "File: " + MainActivity.STORAGE_DIR.getAbsolutePath() + "/" + unixtime);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.parse("file://" +
                                        MainActivity.STORAGE_DIR.getAbsolutePath() + "/" + unixtime
                        ), "image/*");
                        context.startActivity(intent);
                    }
                });
            }

            txtSender.setText(clientName);
            view.setBackgroundColor(view.getResources().getColor(R.color.accent_material_light));


        } else {
            if(!send_file.equals("")){
                btnattachment.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "File: " + send_file);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.parse("file://" + send_file), "image/*");
                        context.startActivity(intent);
                    }
                });
            }
            txtSender.setText(personalPresence.getClientName());
            view.setBackgroundColor(view.getResources().getColor(R.color.background_floating_material_light));


        }

        if( has_file == 1 )
            btnattachment.setVisibility(View.VISIBLE);
        else
            btnattachment.setVisibility(View.GONE);

        txtDate.setText(time);

        txtMessage.setText(message);

    }

    public void setPersonalPresence(PresenceObject personalPresence) {
        this.personalPresence = personalPresence;
    }
}
