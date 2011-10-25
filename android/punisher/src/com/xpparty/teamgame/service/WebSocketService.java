package com.xpparty.teamgame.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.os.*;
import com.xpparty.teamgame.core.WebSockets.WebSocket;
import com.xpparty.teamgame.core.WebSockets.WebSocketEventsListener;

import java.net.URI;
import java.util.Random;

public class WebSocketService extends Service implements WebSocketEventsListener {

    public static final String START_TEAM_GAME_SERVICE_INTENT = "com.xpparty.teamgame.START_SERVICE";
    public static final String STOP_TEAM_GAME_SERVICE_INTENT = "com.xpparty.teamgame.STOP_SERVICE";

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SEND_COMMAND = 3;
    public static final int MSG_INCOMING = 4;

    private WebSocket socket;
    private Thread thread;

    private Messenger client;

    final Messenger messenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {

    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                client = msg.replyTo;
                break;
            case MSG_UNREGISTER_CLIENT:
                if (client == msg.replyTo){
                    client = null;
                }
                break;
            case MSG_SEND_COMMAND:
                SendCommand(msg.getData());
            default:
                super.handleMessage(msg);
            }
        }
    }

    private void SendCommand(Bundle data) {
        socket.send(data.getString("text"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");

        try {
            socket = new WebSocket(new URI(url), WebSocket.Draft.DRAFT76,
                    "WEBSOCKET." + new Random().nextInt(100), this);
            thread = socket.connect();
        } catch (Exception e) {
            e.printStackTrace();
            if(thread != null) {
                thread.interrupt();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        socket.close();
        socket = null;
        thread = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onMessage(String msg) {
        try {
            Bundle bundle= new Bundle();
            bundle.putString("text", msg);
            Message reply = Message.obtain(null, MSG_INCOMING);
            reply.setData(bundle);
            client.send(reply);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClose() {

    }

    @Override
    public void onError(Throwable t) {

    }
}
