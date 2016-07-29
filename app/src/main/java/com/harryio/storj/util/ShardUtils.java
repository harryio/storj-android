package com.harryio.storj.util;

import android.util.Log;

import com.harryio.storj.model.ShardModel;

import org.spongycastle.pqc.math.linearalgebra.ByteUtils;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShardUtils {
    private static final String TAG = "ShardUtils";
    private static final int NUM_OF_CHALLENGES = 2;
    private static final int SHARD_SIZE = 1024 * 1024 * 8; //8MB

    public static List<ShardModel> getShards(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[SHARD_SIZE];
        int length;
        int shardIndex = 0;

        List<ShardModel> shards = new ArrayList<>();
        while((length = fileInputStream.read(buffer)) > 0) {
            File shardFile = File.createTempFile(file.getName() + shardIndex, ".shard");
            FileOutputStream fos = new FileOutputStream(shardFile);
            fos.write(buffer, 0, length);
            fos.close();

            final byte[] shardBytes = Arrays.copyOf(buffer, length);
            String hash = new String(Crypto.hexRmd160Digest(Crypto.sha256Digest(shardBytes)));
            Log.i(TAG, "Hash: " + hash);

            String[] challenges = new String[NUM_OF_CHALLENGES];
            final String[] tree = new String[NUM_OF_CHALLENGES];

            for (int i = 0; i < NUM_OF_CHALLENGES; ++i) {
                String randomChallenge = Crypto.randomHexString(32);
                byte[] challengeBytes = randomChallenge.getBytes("UTF-8");
                challenges[i] = randomChallenge;

                byte[] dataToHash = Hex.encode(ByteUtils.
                        concatenate(challengeBytes, shardBytes));
                byte[] treeBytes = Crypto.hexRmd160Sha256Digest(
                        Crypto.hexRmd160Sha256Digest(dataToHash));
                tree[i] = new String(treeBytes, "UTF-8");
            }

            ShardModel shard = new ShardModel(hash, length, shardIndex, challenges, tree);
            shard.setShardPath(shardFile.getAbsolutePath());
            shards.add(shard);
            shardIndex++;
        }

        return shards;
    }
}
