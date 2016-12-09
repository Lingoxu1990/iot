package com.iot.socketUtil;

import com.iot.dbUtil.PropsUtil;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by xulingo on 16/3/24.
 */
public class SocketPool {


    private static BlockingQueue<Socket> sockets = new LinkedBlockingDeque<Socket>();
    private static String gatewayIP;
    private static String gatewayPort;

    private static void init() throws IOException {

        String number = "";
//        String gatewayIP="";
//        String gatewayPort="";

        PropsUtil configProps = new PropsUtil("config.properties");

        try {
            number = configProps.get("socketpoll");
            gatewayIP = configProps.get("gatewayIP");
            gatewayPort = configProps.get("gatewayPort");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int number_ = Integer.parseInt(number);

        for (int i = 0; i < number_; i++) {
            Socket socket = new Socket(gatewayIP, Integer.parseInt(gatewayPort));
            socket.setSoTimeout(3000);
            socket.setSendBufferSize(10000000);
            sockets.offer(socket);
        }
    }


    static {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Socket getSoket() {
        return sockets.poll();
    }

    public static void release(Socket socket) {

        Socket socket_r = null;
        try {
            if (socket != null) {
                socket.close();
                socket_r = new Socket(gatewayIP, Integer.parseInt(gatewayPort));
                sockets.offer(socket_r);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }


    }

}
