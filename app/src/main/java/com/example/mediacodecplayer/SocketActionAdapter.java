package com.example.mediacodecplayer;


import android.content.Context;
import android.util.Log;

import com.example.mediacodecplayer.bean.MsgDataBean;
import com.example.mediacodecplayer.utils.ClientConstants;
import com.example.mediacodecplayer.utils.EventCode;
import com.example.mediacodecplayer.utils.EventMessage;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.dispatcher.IRegister;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClient;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClientIOCallback;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IClientPool;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerActionListener;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerManager;
import com.xuhao.didi.socket.server.action.ServerActionAdapter;
import com.xuhao.didi.socket.server.impl.OkServerOptions;

import org.json.JSONObject;

import java.nio.charset.Charset;

import static com.example.mediacodecplayer.utils.EventBusUtils.post;

public class SocketActionAdapter extends ServerActionAdapter {
    private final Context context;
    private final IRegister<IServerActionListener, IServerManager> server;
    private IServerManager manager;
    //状态监听

    public SocketActionAdapter(Context context, IRegister<IServerActionListener, IServerManager> server) {
        this.context = context;
        this.server = server;
    }

    @Override
    public void onServerListening(int serverPort) {
        super.onServerListening(serverPort);
        Log.e("client", "端口$" + serverPort + " 开启监听成功!");
    }

    @Override
    public void onClientConnected(IClient client, int serverPort, IClientPool clientPool) {
        super.onClientConnected(client, serverPort, clientPool);
        Log.e("client", "连接成功");
        client.setReaderProtocol(OkServerOptions.getDefault().getReaderProtocol());
        client.addIOCallback(new IClientIOCallback() {
            @Override
            public void onClientRead(OriginalData originalData, IClient client, IClientPool<IClient, String> clientPool) {
                String str = new String(originalData.getBodyBytes(), Charset.forName("utf-8"));
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    final int cmd = jsonObject.getInt("cmd");
                    switch (cmd) {
                        case ClientConstants.CLIENT_SUCCESS:
                            Log.e("info", "链接成功");
                        case ClientConstants.STAUTS_HEART:
                            //心跳
                            JSONObject jo = new JSONObject();
                            jo.put("cmd", ClientConstants.STAUTS_HEART);
                            MsgDataBean msgDataBean = new MsgDataBean(jo.toString());
                            clientPool.sendToAll(msgDataBean);
                            break;
                        case ClientConstants.CMD_STAUTS:
                            Log.e("info", str);
                            //获取到流
                            String info = jsonObject.getString("info");
                            //发送连接成功消息
                            EventMessage eventMessage = new EventMessage(EventCode.EVENT_A,info);
                            post(eventMessage);
                            break;
                    }
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
            }

            @Override
            public void onClientWrite(ISendable sendable, IClient client, IClientPool<IClient, String> clientPool) {
            }
        });
    }


    @Override
    public void onClientDisconnected(IClient client, int serverPort, IClientPool clientPool) {
        super.onClientDisconnected(client, serverPort, clientPool);
        Log.e("client", "连接失败");
    }

    public IServerManager getManager() {
        manager = server.registerReceiver(this);
        return manager;
    }


}
