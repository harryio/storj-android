package com.harryio.storj.util.network;

import android.util.Log;

import com.harryio.storj.util.FileUtils;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class StorjWebSocketAdapter extends WebSocketAdapter {
    private static final String TAG = "StorjWebSocketAdapter";

    private String shardFilePath;
    private String authJson;
    private CountDownLatch latch;

    public StorjWebSocketAdapter(String shardFilePath, String authJson, CountDownLatch latch) {
        this.shardFilePath = shardFilePath;
        this.authJson = authJson;
        this.latch = latch;
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        super.onTextFrame(websocket, frame);
        Log.i(TAG, "onTextFrame: " + frame.toString());
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);
        Log.i(TAG, "Text received from socket:\n" + text);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        Log.i(TAG, "Server disconnected. Is closed by server: " + closedByServer);
        latch.countDown();
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onError(websocket, cause);
        Log.e(TAG, "Web Socket error", cause);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        Log.i(TAG, "onConnected: ");
        websocket.sendText(authJson, true);

        File shardFile = new File(shardFilePath);
        byte[] shardBytes = FileUtils.fileToByteArray(shardFile);
        Log.i(TAG, "Shard bytes length: " + shardBytes.length);

        websocket.sendBinary(shardBytes, true);
    }
}
