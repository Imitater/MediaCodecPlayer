package com.example.mediacodecplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.mediacodecplayer.utils.EventBusUtils;
import com.example.mediacodecplayer.utils.EventCode;
import com.example.mediacodecplayer.utils.EventMessage;
import com.example.mediacodecplayer.utils.NetWorkUtils;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;
import com.xuhao.didi.socket.client.sdk.client.connection.NoneReconnect;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.dispatcher.IRegister;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerActionListener;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private IRegister<IServerActionListener, IServerManager> server;
    private SocketActionAdapter adapter;
    private Surface surface;
    H265Player h265Player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBusUtils.register(this);
        Log.e("ip", NetWorkUtils.getIPAddress(this));

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
        initSocket();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                surface = surfaceHolder.getSurface();
                h265Player = new H265Player(surface);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });
    }

    private void initSocket() {
        server = OkSocket.server(8080);
        adapter = new SocketActionAdapter(getApplicationContext(), server);
        IServerManager manager = adapter.getManager();
        if (!manager.isLive()) {
            manager.listen();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveEvent(EventMessage event) {
        if (event != null) {
            if (event.getCode() == EventCode.EVENT_A) {
                String info= (String) event.getData();
                byte [] byteArray = Base64.decode(info, Base64.URL_SAFE);
                h265Player.dealInfo(byteArray);
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }
}
