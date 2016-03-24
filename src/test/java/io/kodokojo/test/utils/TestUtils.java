package io.kodokojo.test.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class TestUtils {

    public static int getEphemeralPort() {
        int port = 0;
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            port = ((InetSocketAddress)serverSocket.getLocalSocketAddress()).getPort();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }

}
