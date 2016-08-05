package com.harryio.storj.util.network;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class DownloadSocketAdapter extends WebSocketAdapter {
    private static final String TAG = "DownloadSocketAdapter";

    private String authJson;
    private CountDownLatch latch;

    public DownloadSocketAdapter(String authJson, CountDownLatch latch) {
        this.authJson = authJson;
        this.latch = latch;
    }

    @Override
    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        super.onBinaryFrame(websocket, frame);

        Log.i(TAG, "onBinaryFrame: " + frame.toString());
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);

        Log.i(TAG, "onTextMessage: " + text);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        Log.i(TAG, "Download Socket disconnected");

        latch.countDown();
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        Log.i(TAG, "Download Socket connected");

        websocket.sendText(authJson);
    }
}
