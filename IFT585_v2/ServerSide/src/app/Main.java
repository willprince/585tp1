package app;

import app.Server;

import java.net.InetAddress;
import java.net.SocketException;

public class Main {

    public static void main(String[] args) {
        //init
    	try {
    		InetAddress iPAddress = InetAddress.getByName("localhost");
            int port = 18836;

            Server server = new Server(port, iPAddress);
            server.start();
    	}catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
