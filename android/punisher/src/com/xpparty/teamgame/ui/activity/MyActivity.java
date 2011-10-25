package com.xpparty.teamgame.ui.activity;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.xpparty.teamgame.R;
import com.xpparty.teamgame.service.WebSocketService;

public class MyActivity extends Activity
{
    private Button btnPush;
    private TextView incoming;

    private Messenger serviceMessenger;
    final Messenger messenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case WebSocketService.MSG_INCOMING:
                String text = msg.getData().getString("text");
                incoming.setText("Incoming Message: " + text);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            serviceMessenger = new Messenger(service);
            try {
                Message msg = Message.obtain(null, WebSocketService.MSG_REGISTER_CLIENT);
                msg.replyTo = messenger;
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            serviceMessenger = null;
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        incoming = (TextView)findViewById(R.id.incoming);
        btnPush = (Button)findViewById(R.id.btn_push);

        Intent intent = new Intent(WebSocketService.START_TEAM_GAME_SERVICE_INTENT);
        intent.putExtra("url", "ws://192.168.38.12:8081");
        //startService(new Intent(this, WebSocketService.class));
        startService(intent);
        bindService(new Intent(this, WebSocketService.class), connection, Context.BIND_AUTO_CREATE);

        btnPush.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bundle bundle= new Bundle();
                    bundle.putString("text", "bugoga!");
                    Message msg = Message.obtain(null, WebSocketService.MSG_SEND_COMMAND);
                    msg.replyTo = messenger;
                    msg.setData(bundle);
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onResume()
    {
        super.onResume();
    }

    public void onPause()
    {
        super.onPause();
    }
}
