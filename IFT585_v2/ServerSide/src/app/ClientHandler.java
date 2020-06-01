package app;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
/**
 * Eventuellement lui qui handle les clients pour en avoir plusieurs en meme temps.
 */
public class ClientHandler implements Runnable {

    /* Global variables */
    //TODO : actually les rendre globales
    private int BUFFER_SIZE = 2014;
    private int THREAD_SLEEP = 0;

    /* Handler info */
    private DatagramSocket socket;

    public ClientHandler(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        DatagramPacket packet;
        //String sentence;
        while(true){
            try{
                packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                read(packet);
                Thread.sleep(THREAD_SLEEP * 1000);
                //writeToFile(packet, outputFileName);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeToFile(DatagramPacket packet, String fileName) throws IOException {
        FileOutputStream targetStream = new FileOutputStream(new File(fileName));
        targetStream.write(packet.getData());
    }

    public byte[] read(DatagramPacket packet) throws IOException {
        socket.receive(packet);
        return packet.getData();
    }

}
