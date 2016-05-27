package com.harryio.storj.model;

import java.util.Arrays;

public class FrameDetail {
    String created;
    String id;
    Shard[] shards;

    @Override
    public String toString() {
        return "FrameDetail{" +
                "created='" + created + '\'' +
                ", id='" + id + '\'' +
                ", shards=" + Arrays.toString(shards) +
                '}';
    }

    public class Shard {
        String hash;
        int size;
        int index;

        public String getHash() {
            return hash;
        }

        public int getSize() {
            return size;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return "Shard{" +
                    "hash='" + hash + '\'' +
                    ", size=" + size +
                    ", index=" + index +
                    '}';
        }
    }
}
