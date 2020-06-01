package app;

import java.io.IOException;
import java.net.*;

public class Server {

    /* Server info */
    private DatagramSocket socket;
    private int port;
    private InetAddress inet;
    private final static int BUFFER_SIZE = 1024;
    private final static int ACK_SIZE = 1024;
    private final static int TIMEOUT = 0;
    private final static int MAX_CLIENTS = 5;


    public Server(int port, InetAddress inet) throws UnknownHostException {
        this.port = port;
        this.inet = inet;
        try {
            socket = new DatagramSocket(port, inet);
            socket.setReceiveBufferSize(BUFFER_SIZE);
            socket.setSendBufferSize(BUFFER_SIZE);
            socket.setSoTimeout(TIMEOUT);
        } catch (SocketException e) {
            System.out.println("Erreur d'initialisation du serveur.");
            e.printStackTrace();
        }
    }


    public int start() {
        for(int i = 0; i < MAX_CLIENTS; i++){
            Handler handler = new Handler(socket, port, inet);
            //handler.start();        //pars un thread
            handler.run();
        }
        return 0;
    }
}
