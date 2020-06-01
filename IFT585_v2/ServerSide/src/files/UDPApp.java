package files;

import java.io.*;
import java.net.*;
import java.util.*;

class UDPApp
{
    public static void main(String args[]) throws Exception
    {
        int SERVER_PORT = 64000;

        if(args.length > 0)
        {
            if(args[0].equals("c"))
            {
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

                DatagramSocket clientSocket = new DatagramSocket();
        
                InetAddress IPAddress = InetAddress.getByName("localhost"); //e.g will's computer = localhost/127.0.0.1

                byte[] receiveData = new byte[1024];
        
                // System.out.println("Enter file name: ");
                // String fileName = inFromUser.readLine();
        
                //Configuring file to send
                File fileToTransfer = new File("../files/GOPR0508.MP4");
                
                FileInputStream targetStream = new FileInputStream(fileToTransfer);

                int fileSize = targetStream.available();

                byte[] sendData = new byte[1024];

                System.out.println("fileSize = " + fileSize);

                ArrayList<byte[]> sendDataList = new ArrayList<byte[]>();

                byte[] tempSendData;
                while(fileSize > 0)
                {   
                    if(fileSize > 1024)
                    {
                        tempSendData = new byte[1024];
                        for(int i = 0; i<1024;i++)
                        {        
                            tempSendData[i] = (byte)targetStream.read();
                            fileSize--;
                        } 
                    }
                    else
                    {
                        tempSendData = new byte[fileSize];
                        int initialSize = fileSize;
                        for(int i = 0; i < initialSize ;i++)
                        {        
                            tempSendData[i] = (byte)targetStream.read();
                            fileSize--;
                        }
                    }
   
                    sendDataList.add(tempSendData);
                }

                System.out.println(sendDataList.size());

                for(int i =0; i < sendDataList.size(); i++){
                    // On configure le packet
                    sendData = sendDataList.get(i);

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SERVER_PORT);

                    clientSocket.send(sendPacket);  //On envoie le packet
                    
                    // Receive answer packet
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        
                    try {
                        //clientSocket.setSoTimeout(10000);       // 10 sec waiting for responce
                        clientSocket.receive(receivePacket);    //waiting to receive a packet from server
                    
                        String modifiedSentence = new String(receivePacket.getData());
                        System.out.println("FROM SERVER: " + modifiedSentence);
                    
                    } catch (SocketTimeoutException e) 
                    {
                    System.out.println("no answer form the server " + e.toString());
                    }
                }
        
                //================= Recevoir =====================
        
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        
                try {
                    clientSocket.setSoTimeout(10000);       // 10 sec waiting for responce
                    clientSocket.receive(receivePacket);    //waiting to receive a packet from server
                
                    String modifiedSentence = new String(receivePacket.getData());
                    System.out.println("FROM SERVER: " + modifiedSentence);
                
                } catch (SocketTimeoutException e) 
                {
                System.out.println("no answer form the server " + e.toString());
                }

                clientSocket.close();
            }
            else if(args[0].equals("s"))
            {
                
                DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);
                System.out.println("listening to Port: "+ SERVER_PORT);
        
                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];
                FileOutputStream  FOS = new FileOutputStream("videoOutput.txt");
        
                while(true)
                {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    
                    try 
                    {
                        //serverSocket.setSoTimeout(10000);
                        serverSocket.receive(receivePacket);  
                        String sentence = new String(receivePacket.getData());
                        

                        receiveData = receivePacket.getData();

                        System.out.println("receive packet length: " + receivePacket.getLength());
                        
                        byte[] modifiedData = new byte[receivePacket.getLength()];

                        modifiedData = receivePacket.getData();

                        InetAddress IPAddress =  receivePacket.getAddress();
                        int clientPort = receivePacket.getPort();
            
                        String capitalizeSentence = sentence.toUpperCase();
            
                        sendData = capitalizeSentence.getBytes();
                        
                        System.out.println(modifiedData.length);


                        for(int i = 0; i<receivePacket.getLength();i++)
                        {        
                            FOS.write(receiveData[i]);
                        }
            
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, clientPort);
                        
                        serverSocket.send(sendPacket);
                    }
                    catch (SocketTimeoutException e) 
                    {
                        System.out.println("waited for 10 sec: " + e.toString());
                        break;
                    }
                }
                FOS.close();
            }
            
        }
        else
            {
                System.out.println("the only arguments available are: [c, s]");
            }
    }
}