package app;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client extends Thread {

    /* Global variables */
    //TODO : actually les rendre globales
    private int BUFFER_SIZE = 1024;
    private int THREAD_SLEEP = 0;
    private int PACKET_SIZE = 1024;

    /* Client info */
    private int port;
    InetAddress iPAddress;
    private DatagramSocket socket;


    public Client(int port, InetAddress address) throws SocketException {
        this.port = port;
        this.iPAddress = address;
        socket = new DatagramSocket();
        socket.connect(address, port);
    }

    private ArrayList<DatagramPacket> makePackets(int fileSize, FileInputStream targetStream) throws IOException {
        ArrayList<DatagramPacket> packetList = new ArrayList();

        DatagramPacket packet;
        byte[] tempSendData;
        
        int count = 0;
        
        while(fileSize > 0){
            if(fileSize > PACKET_SIZE){
                tempSendData = new byte[PACKET_SIZE];
                // add the index
                byte[] index = (count + "<->").getBytes();
                
                for(int i = 0; i<index.length; i++) {
                    tempSendData[i] = index[i];
                }
                
                
                // add the file data
                for(int i = index.length; i < PACKET_SIZE ; i++) {
                    tempSendData[i] = (byte)targetStream.read();
                    fileSize--;
                }
                
                
                packet = new DatagramPacket(tempSendData, PACKET_SIZE, socket.getInetAddress(), socket.getPort());

                packetList.add(packet);
                
                count++;
            } else {    //le fichier est plus petit que le buffer
                
                System.out.println("filesize is :" + fileSize);
                // add the index
                byte[] index = (count + "<->").getBytes();
                byte[] ending = ("<-end->").getBytes();
                
                tempSendData = new byte[fileSize + index.length + ending.length];
                
                for(int i = 0; i<index.length; i++) {
                    tempSendData[i] = index[i];
                }
                
                int initialSize = fileSize + index.length;
                for(int i = index.length; i < initialSize ;i++){
                    tempSendData[i] = (byte)targetStream.read();
                    fileSize--;
                }
                
                int countEnd = 0;
                for(int i = initialSize; i < (initialSize + ending.length); i++) {
                    tempSendData[i] = ending[countEnd];
                    countEnd ++;
                }
                
                System.out.println("filesize is :" + fileSize);
                
                
                packet = new DatagramPacket(tempSendData, tempSendData.length, socket.getInetAddress(), socket.getPort());
                
                packetList.add(packet);
                
                return packetList;
            }
        }
        return packetList;
    }
    
    public int sendFile(String fileName) throws IOException {
        System.out.println("sending file " + fileName + " to port : " + port + ", on address : " + iPAddress);

        String s = "CONNECTIONREQUEST";
        byte[] data = s.getBytes();

        DatagramPacket connectionRequest = new DatagramPacket(data, s.length(), iPAddress, port);
        socket.send(connectionRequest);
        System.out.println("--Sending connection request");

        byte[] answerData = new byte[BUFFER_SIZE];
        DatagramPacket answer = new DatagramPacket(answerData, answerData.length, iPAddress, port);
        System.out.println("--Receiving confirmation by server");
        socket.receive(answer);

        byte[] sendRequestData = new byte[BUFFER_SIZE];
        String transactionString = "SENDFILE" + " " + fileName;
        sendRequestData = transactionString.getBytes();
        DatagramPacket sendRequest = new DatagramPacket(sendRequestData, sendRequestData.length, iPAddress, port);
        System.out.println("--Sending file request");
        socket.send(sendRequest);

        FileInputStream targetStream = new FileInputStream(new File("fileToSend/" + fileName.trim()));
        int fileSize = targetStream.available();

        ArrayList<DatagramPacket> packetList = makePackets(fileSize, targetStream);
        System.out.println("fileSize = " + fileSize + " / number of packets : " + packetList.size());

        goBackN(packetList);
        
        return 0;
    }
    
private void goBackN(ArrayList<DatagramPacket> packetList) throws IOException {
    	
	//initate the window
	int WINDOW_SIZE = 4;
	
	int next_ACK = 0;
	int last_sent = 0;
	
	int totalPackets = packetList.size();
	
	while(next_ACK < totalPackets) 
	{
		// send window
		for(int i = next_ACK; (i < next_ACK + WINDOW_SIZE) && (i < totalPackets - 1); i++)
		{
			//System.out.println("i is: " + i);
			socket.send(packetList.get(i));
			last_sent = i;
		}
		
		// receive client ACK
		DatagramPacket ack = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
		String ackString;
		
		//System.out.println("waiting for: " + next_ACK);
		
		socket.receive(ack); 
		ackString = new String(ack.getData());
		
		int ackInt = Integer.parseInt(ackString.trim());
		
		if( ackInt  >= totalPackets ) 
		{
			System.out.println("File was sent to the server");
			return;
		}
		
		
		//check if it is the required ACK and send the next frame
		while(next_ACK == ackInt)
		{
    		
			next_ACK++;
			
			if(last_sent + 1 >= totalPackets ) {
				System.out.println("File was sent to the client");
				return;
			}
			
			socket.send(packetList.get(last_sent + 1));
			
			last_sent = last_sent + 1;
			
			ack = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
			
			//System.out.println("waiting for: " + next_ACK);


    		socket.receive(ack); 
    		ackString = new String(ack.getData());
			
		}
		//else resend window
		
	}

    }


    public int requestFile(String fileName) throws IOException {
        System.out.println("requesting file " + fileName + " to port : " + port + ", on address : " + iPAddress);

        String s = "CONNECTIONREQUEST";
        byte[] data = s.getBytes();

        DatagramPacket connectionRequest = new DatagramPacket(data, s.length(), iPAddress, port);
        socket.send(connectionRequest);
        System.out.println("--Sending connection request");

        byte[] answerData = new byte[BUFFER_SIZE];
        DatagramPacket answer = new DatagramPacket(answerData, answerData.length, iPAddress, port);
        System.out.println("--Receiving confirmation by server");
        socket.receive(answer);

        byte[] fileRequestData = new byte[BUFFER_SIZE];
        String transactionString = "GETFILE" + " " + fileName;
        fileRequestData = transactionString.getBytes();
        DatagramPacket fileRequest = new DatagramPacket(fileRequestData, fileRequestData.length, iPAddress, port);
        System.out.println("--Sending file request");
        socket.send(fileRequest);

        //TODO : IMPLEMENTER GO-BACK-N COTE CLIENT
        byte[] receiveData = new byte[1024];
        byte[] sendData;
        System.out.println("filename: " + fileName);
        FileOutputStream  FOS = new FileOutputStream(new File("receivedFiles/" + fileName.trim()));
        
        int lastFrame = 0;
        String theFile = "";
        
        byte[] newPacketData;
        
        while(true)
        {
        	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        	try
        	{
        		//RECEIVE DATA FROM SERVER
        		socket.receive(receivePacket);
        		String receiveString = new String(receivePacket.getData());
        		String[] receiveStringList = receiveString.split("<->");
        		String index = receiveStringList[0];
        		String packetData = receiveStringList[1];
        		int packetSize = receivePacket.getLength();
        		
        		//System.out.println("size == " + packetSize);

        		//make sure it is the next frame
        		int indexString = Integer.parseInt(index);
        		if( (indexString == lastFrame)) {
        			//System.out.println("index is: " + index);
        			//System.out.println(packetData);
        			if(packetSize < 1024)
        			{
        				packetData = packetData.split("<-end->")[0];
        				theFile = theFile + packetData;
        				System.out.println(theFile);
        			
        				newPacketData = new byte[packetSize];
            			newPacketData = packetData.getBytes();
            			
            			//insert in file
            			for(int i = 0; i<newPacketData.length;i++)
                        {        
                            FOS.write(newPacketData[i]);
                        }
            			FOS.close();
            			
            			//SEND final ACK TO SERVER WITH THE HIGHEST FRAME NUMBER
                		sendData = new byte[index.getBytes().length];
                		sendData = index.getBytes();
                		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, iPAddress, port);
                        
                		socket.send(sendPacket);
        				
        				return 0;
        			}
        			
        			newPacketData = new byte[1024];
        			newPacketData = packetData.getBytes();
        			
        			//insert in file
        			for(int i = 0; i<newPacketData.length;i++)
                    {        
                        FOS.write(newPacketData[i]);
                    }
        			
        			
        			theFile = theFile + packetData;
        			
        			lastFrame++;
        			
        		}
        		
        		
        		
        		//SEND ACK TO SERVER WITH THE HIGHEST FRAME NUMBER
        		sendData = new byte[index.getBytes().length];
        		sendData = index.getBytes();
        		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, iPAddress, port);
                
        		socket.send(sendPacket);

        	}catch (SocketTimeoutException e) 
            {
                System.out.println("waited for 10 sec: " + e.toString());
              
                break;
            }
        
        }
        FOS.close();
        return 0;
    }


    private void sendDataList(ArrayList<byte[]> dataList) throws IOException {
        for(int i =0; i < dataList.size(); i++){
            byte[] data = dataList.get(i);
            DatagramPacket packet = new DatagramPacket(data, data.length, iPAddress, port);

            socket.send(packet);
            receiveAnswer();
        }
    }


    private void receiveAnswer() throws IOException {
        DatagramPacket answerPacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        try {
            socket.receive(answerPacket);    //waiting to receive a packet from server
            String modifiedSentence = new String(answerPacket.getData());
            System.out.println("FROM SERVER: " + modifiedSentence);
        } catch (SocketTimeoutException e) {
            System.out.println("no answer form the server " + e.toString());
        }
    }


    //run
    public int receiveFile(){
        DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        try {
            socket.setSoTimeout(10000);       // 10 sec waiting for responce
            //read
            socket.receive(packet);    //waiting to receive a packet from server

            String modifiedSentence = new String(packet.getData());
            System.out.println("FROM SERVER: " + modifiedSentence);

        } catch (SocketTimeoutException | SocketException e) {
            System.out.println("no answer form the server " + e.toString());
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

}
