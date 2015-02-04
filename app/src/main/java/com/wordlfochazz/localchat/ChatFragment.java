package com.wordlfochazz.localchat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wordlfochazz.localchat.database.MessagingDbHelper;
import com.wordlfochazz.localchat.listeners.MessagingDataSetChangedListener;
import com.wordlfochazz.localchat.objects.MessageObject;
import com.wordlfochazz.localchat.objects.PresenceObject;
import com.wordlfochazz.localchat.connection.MessagingClient;
import com.wordlfochazz.localchat.tasks.MessagingClientTask;
import com.wordlfochazz.localchat.utils.FileUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public  class ChatFragment extends Fragment implements MessagingDataSetChangedListener{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    public static final String ARG_CLIENT_ID = "client_id";

    public static final String TAG = "LC"+ChatFragment.class.getSimpleName();
    private PresenceObject presenceObject;
    private TextView textView;
    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath;
    private View rootView;
    private MessagesAdapter adapter;
    private MessagingDbHelper dbHelper;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ChatFragment newInstance(Bundle bundle) {
        ChatFragment fragment = new ChatFragment();

        fragment.setArguments(bundle);
        return fragment;
    }

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Button btn = (Button) rootView.findViewById(R.id.sendButton);


        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        dbHelper = MessagingDbHelper.getInstance(getActivity().getApplicationContext());
        if(presenceObject != null ) {
            Cursor c = dbHelper.getThreadMessages(dbHelper.getThreadId(presenceObject));

            ListView messages = (ListView) rootView.findViewById(R.id.listMessages);
            adapter = new MessagesAdapter(getActivity().getApplicationContext(), c, CursorAdapter.FLAG_AUTO_REQUERY);
//            adapter.setActivity(getActivity());
            adapter.setPersonalPresence(((MainActivity) getActivity()).getPersonalPresence());
            messages.setAdapter(adapter);

            dbHelper.setDataSetChangedListener(this);

        }


       ImageButton btn_att =  (ImageButton) rootView.findViewById(R.id.sendAttachmentButton);
        if(btn_att!= null) {
            btn_att.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).setSocketLock(true);
                    // in onCreate or any event where your want the user to
                    // select a file
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), SELECT_PICTURE);
                }
            });
        }


        return rootView;
    }
    @Override
    public void dataSetChanged() {
        adapter.changeCursor(dbHelper.getThreadMessages(dbHelper.getThreadId(presenceObject)));
        adapter.notifyDataSetChanged();
    }
    private void sendMessage() {
        EditText messageText = (EditText) rootView.findViewById(R.id.messageBodyField);
//        if(selectedImagePath == null && messageText.getText().toString().equals("")){
//            Toast.makeText(rootView.getContext(), "No message is composed", Toast.LENGTH_SHORT).show();
//            return;
//        }


        if(presenceObject != null ){
            if(isClientActive() != null) {
                MessageObject message = new MessageObject();

                message.setReceiverId(presenceObject.getClientID());

                message.setSenderId(((MainActivity) getActivity()).getPersonalPresence().getClientID());
                message.setMessage(messageText.getText().toString());
                message.setHasFile(false);
                message.setTime(System.currentTimeMillis() / 1000);

                message.setType(MessageObject.TYPE_OUTGOING);
                messageText.setText("");
                if(selectedImagePath != null)
                    message.setHasFile(true);
                MessagingClient client = new MessagingClient();
                client.setMessageObject(message);
                client.setPresenceObject(presenceObject);
                if(selectedImagePath != null)
                    client.setSelectedImagePath(selectedImagePath);
                MessagingClientTask task = new MessagingClientTask(client);
                task.setActivity((MainActivity) getActivity());
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                Toast.makeText(rootView.getContext(), presenceObject.getClientName()+ " is offline", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(rootView.getContext(), "There is no one to send this message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ((MainActivity) getActivity()).setSocketLock(false);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();

                 selectedImagePath = FileUtils.getPath(getActivity().getApplicationContext(), selectedImageUri);


                sendMessage();

                selectedImagePath = null;
            }
        }
    }

    private PresenceObject isClientActive() {
        return ((MainActivity) getActivity()).getClient(presenceObject.getClientID());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

        String selectedClientId = String.valueOf(getArguments().getCharSequence(ARG_CLIENT_ID));
        presenceObject = ((MainActivity) activity).getClient(selectedClientId);

    }
}