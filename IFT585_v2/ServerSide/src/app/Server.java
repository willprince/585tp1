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


    public void start() throws IOException {
        while(true){
            DatagramPacket connectionRequest = new DatagramPacket(new byte[17], 17);
            System.out.println("WAITING FOR CONNECTION PACKET ON PORT : " + port + " / ON ADDRESS : " + inet);
            socket.receive(connectionRequest);

            //verifie la connection
            if(connection(connectionRequest)){
                //pars un thread
                Handler handler = new Handler(socket, port, inet, connectionRequest);
                handler.run();
            }
        }
    }

    private boolean connection(DatagramPacket packet) throws IOException {
        String s = read(packet);
        System.out.println("FROM CLIENT: " + s);
        if(s.equals("CONNECTIONREQUEST")){
            try{
                socket.connect(packet.getAddress(), packet.getPort());
                return true;
            } catch (Exception e) {
                System.out.println("ERROR GETTING CONNECTION");
            }
        }
        return false;
    }

    /** Converti un paquet en String. */
    public String read(DatagramPacket packet) throws IOException {
        return new String(packet.getData());
    }
}

