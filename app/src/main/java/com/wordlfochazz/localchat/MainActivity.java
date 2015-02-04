package com.wordlfochazz.localchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.wordlfochazz.localchat.database.MessagingDbHelper;
import com.wordlfochazz.localchat.objects.PresenceObject;
import com.wordlfochazz.localchat.connection.DiscoverProtocol;
import com.wordlfochazz.localchat.connection.MessagingServer;
import com.wordlfochazz.localchat.tasks.DiscoverTask;
import com.wordlfochazz.localchat.tasks.MessagingServerTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public DiscoverTask discoverTask;
    public DiscoverProtocol discoverProtocol;
    public MessagingServerTask messagingServerTask;
    public MessagingServer messagingServer;
    private String TAG = "LC"+MainActivity.class.getSimpleName();

    private HashMap<String,PresenceObject> clients = new HashMap<String , PresenceObject>();
    private  ArrayList<String> clientNames = new ArrayList<String>();
    private  ArrayList<String> clientIds = new ArrayList<String>();

    private MessagingDbHelper dbHelper;

    private PresenceObject personalPresence;
    private ChatFragment fragment;

    private boolean socketLock = false;
    public static File STORAGE_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/chatty/attachments");
    //    private HashMap<>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        SettingsActivity.firstSetup(getApplicationContext());

        dbHelper = MessagingDbHelper.getInstance(getApplicationContext());

        preparePersonalPresence();


    }

    public PresenceObject preparePersonalPresence() {
        if(personalPresence == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            String clientName = prefs.getString(getString(R.string.pref_username_key), SettingsActivity.generateNewName());
            personalPresence = new PresenceObject();
            personalPresence.setClientName(clientName);
            WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            WifiInfo mWifiConnectionInfo = mWifiManager.getConnectionInfo();

            personalPresence.setClientID(mWifiConnectionInfo.getMacAddress());
        }
        return personalPresence;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        if(clients == null ) return;
        else if ( clients.size() ==0 ) return;
        Bundle bundle = new Bundle();

        bundle.putInt(ChatFragment.ARG_SECTION_NUMBER, position + 1);
        bundle.putCharSequence(ChatFragment.ARG_CLIENT_ID, clients.get(clientIds.get(position)).getClientID());

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragment = ChatFragment.newInstance(bundle);

        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        if(clients.size() == 0) {
            mTitle = getString(R.string.app_name);
            return;
        }
        String id= clientIds.get(number-1);
        mTitle = clients.get(id).getClientName();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if(id == R.id.action_send_presence){
            discoverTask.sendPresence(DiscoverProtocol.PRESENCE_ECHO);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        Log.d(TAG,"Stoped");
        super.onStop();

        if(socketLock) return;

        if(discoverTask != null && discoverProtocol != null) {
            discoverTask.sendPresence(DiscoverProtocol.PRESENCE_BYE);
            discoverTask.closeSocket();
            discoverTask.cancel(true);
            discoverProtocol = null;
            discoverTask = null;
        }

        if(messagingServer != null && messagingServerTask != null) {
            messagingServer.stopServer();
            messagingServerTask.cancel(true);
            messagingServer = null;
            messagingServerTask = null;
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        runDiscovery();
        runMessaging();
    }

    private void runMessaging() {
        if(messagingServer == null && messagingServerTask == null) {
            messagingServer = new MessagingServer();
            messagingServerTask = new MessagingServerTask(messagingServer, this);
            messagingServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    private void runDiscovery() {
        if(discoverTask == null && discoverProtocol == null) {
            discoverProtocol = new DiscoverProtocol(getApplicationContext());
            personalPresence = discoverProtocol.getPreparedPresenceObject();
            discoverTask = new DiscoverTask(discoverProtocol, this);
            discoverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void pushClient(PresenceObject presenceObject){
        clients.put(presenceObject.getClientID(), presenceObject);
        Toast toast = Toast.makeText(this, presenceObject.getClientName()+" came online", Toast.LENGTH_SHORT);
        toast.show();
        updateDrawerAdapter();
    }
    public void clearClient(PresenceObject presenceObject){
        clients.remove(presenceObject.getClientID());
        Toast toast = Toast.makeText(this, presenceObject.getClientName()+" has gone away", Toast.LENGTH_SHORT);
        toast.show();
        updateDrawerAdapter();
    }
    public void updateClientsMap(){
        clientNames.clear();
        clientIds.clear();
        for (PresenceObject p : clients.values() ){
            clientNames.add(p.getClientName());
            clientIds.add(p.getClientID());
        }
    }

    public void updateDrawerAdapter(){
        updateClientsMap();

        if(clientNames.size()==0)
            clientNames.add(getString(R.string.no_active_clients));

        mNavigationDrawerFragment.setClients(clientNames);
    }

    public PresenceObject getClient(String id){
        return clients.get(id);
    }

    public PresenceObject getPersonalPresence() {
        return personalPresence;
    }


    public boolean isSocketLock() {
        return socketLock;
    }

    public void setSocketLock(boolean socketLock) {
        this.socketLock = socketLock;
    }
}
