import java.io.*;
import java.net.*;

class UDPApp
{
    public static void main(String args[]) throws Exception
    {
        if(args.length > 0)
        {
            if(args[0].equals("c"))
            {
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

                DatagramSocket clientSocket = new DatagramSocket();
        
                InetAddress IPAddress = InetAddress.getByName("localhost"); //e.g will's computer = localhost/127.0.0.1
        
                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];
        
                System.out.println("Enter message: ");
                String sentence = inFromUser.readLine();
        
                sendData = sentence.getBytes();
        
                // On configure le packet
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 64000);
        
                clientSocket.send(sendPacket);  //On envoie le packet
        
                //================= Recevoir =====================
        
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        
                try {
                    //clientSocket.setSoTimeout(10000);
                    clientSocket.receive(receivePacket);  //waiting to receive a packet from server
                
                    String modifiedSentence = new String(receivePacket.getData());
                    System.out.println("FROM SERVER: " + modifiedSentence);
                
                } catch (SocketTimeoutException e) 
                {
                System.out.println("waited for 10 sec: " + e.toString());
                }
            }
            else if(args[0].equals("s"))
            {
                DatagramSocket serverSocket = new DatagramSocket(64000);
        
                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];
        
                while(true)
                {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        
                    serverSocket.receive(receivePacket);  
                    String sentence = new String(receivePacket.getData());
        
                    InetAddress IPAddress =  receivePacket.getAddress();
                    int port = receivePacket.getPort();
        
                    String capitalizeSentence = sentence.toUpperCase();
        
                    sendData = capitalizeSentence.getBytes();
        
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        
                    serverSocket.send(sendPacket);
                }
            }
            
        }
        else
            {
                System.out.println("the only arguments available are: [c, s]");
            }
    }
}