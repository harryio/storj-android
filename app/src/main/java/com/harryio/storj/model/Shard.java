package com.harryio.storj.model;

public class Shard {
    String token;
    String hash;
    String operation;
    Farmer farmer;

    public String getToken() {
        return token;
    }

    public String getHash() {
        return hash;
    }

    public String getOperation() {
        return operation;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    @Override
    public String toString() {
        return "Shard{" +
                "token='" + token + '\'' +
                ", hash='" + hash + '\'' +
                ", operation='" + operation + '\'' +
                ", farmer='" + farmer.toString() + '\'' +
                '}';
    }

    public class Farmer {
        String protocol;
        String address;
        String port;
        String nodeID;
        long lastSeen;

        public String getProtocol() {
            return protocol;
        }

        public String getAddress() {
            return address;
        }

        public String getPort() {
            return port;
        }

        public String getNodeID() {
            return nodeID;
        }

        public long getLastSeen() {
            return lastSeen;
        }

        @Override
        public String toString() {
            return "Farmer{" +
                    "protocol='" + protocol + '\'' +
                    ", address='" + address + '\'' +
                    ", port='" + port + '\'' +
                    ", nodeID='" + nodeID + '\'' +
                    ", lastSeen=" + lastSeen +
                    '}';
        }
    }
}
