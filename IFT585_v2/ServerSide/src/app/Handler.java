package app;

import java.io.*;
import java.net.*;
import java.util.*;

public class Handler extends Thread {

    /* Global variables */
    //TODO : actually les rendre globales
    private int PACKET_SIZE = 1024;
    private int THREAD_SLEEP = 1000;
    private int WINDOWS = 5;

    /* Handler info */
    private DatagramSocket socket;
    private int port;
    private InetAddress inet;
    HashMap implementedRequests;
    
    private InetAddress inetC;
    private int clientPort;

    public Handler(DatagramSocket socket, int port, InetAddress inet) {
        this.socket = socket;
        this.port = port;
        this.inet = inet;
        implementedRequests = makeTransactions();
    }

    @Override
    public void run() {
        DatagramPacket connectionRequest = new DatagramPacket(new byte[17], 17);
        while(true){
            try{
                while(true){                                            //waiting for connectionRequest
                    if(connection(connectionRequest)) break;            //establish connection
                }
                sendAnswer(connectionRequest);

                DatagramPacket requestPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                System.out.println("WAITING FOR TRANSACTION PACKET ON PORT : " + port + " / ON ADDRESS : " + inet);
                socket.receive(requestPacket);
                
                inetC = requestPacket.getAddress();
                clientPort = requestPacket.getPort();
                		
                String request = read(requestPacket);                  //waiting for request
                handleRequest(request);                                //serve request
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** Repond au client pour lui confirmer que la connection est bien etablie. */
    private void sendAnswer(DatagramPacket packet) throws IOException {
        DatagramPacket answer = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        String answerString = "Server connection established";
        answer.setData(answerString.getBytes());
        answer.setAddress(packet.getAddress());
        answer.setPort(packet.getPort());
        socket.send(packet);
    }

    private boolean connection(DatagramPacket packet) throws IOException {
        System.out.println("SERVER LISTENING TO PORT : " + port + " / ON ADDRESS : " + inet);
        socket.receive(packet);
        String s = read(packet);
        System.out.println(s);
        System.out.println(s.equals("CONNECTIONREQUEST"));
        System.out.println("FROM CLIENT: " + s);
        if(s.equals("CONNECTIONREQUEST")){
            try{
                socket.connect(packet.getAddress(), packet.getPort());
                return true;
            } catch (Exception e) {
                System.out.println("ERROR GETTING CONNECTION");
            }
        } else {
            return false;
        }
        return false;
    }

    /** Converti un paquet en String. */
    public String read(DatagramPacket packet) throws IOException {
        return new String(packet.getData());
    }

    /** Traitement des requetes */
    public void handleRequest(String request) throws Exception
    {
        Reader inputString = new StringReader(request);
        BufferedReader reader = new BufferedReader(inputString);
        String transaction = readTransaction(reader);
        while (!transactionDone(transaction))
        {
            StringTokenizer tokenizer = new StringTokenizer(transaction, " ");
            if (tokenizer.hasMoreTokens())
                executeTransaction(tokenizer);
                transaction = readTransaction(reader);
        }
    }

    /**
     * Envoie un fichier en fonction d'un nom de fichier. Pour envoyer les fichiers, ils doivent etre mis dans le
     * dossier "files" → "\\src\\files\\".
     */
    public int sendFile(String fileName) throws IOException 
    {
        FileInputStream targetStream = new FileInputStream(new File("fileToSend/verySmallFile.txt"));
        int fileSize = targetStream.available();

        ArrayList<DatagramPacket> packetList = makePackets(fileSize, targetStream);
        System.out.println("fileSize = " + fileSize + " / number of packets : " + packetList.size());

        goBackN(packetList);
        return 0;
    }
    
    public int receiveFile(String fileName) throws IOException 
    {
    	System.out.println("receiving file " + fileName + " to port : " + clientPort + ", from address : " + inetC);
    	
    	byte[] receiveData = new byte[1024];
        byte[] sendData;
        System.out.println("filename: " + fileName);
        FileOutputStream  FOS = new FileOutputStream(new File("test.txt"));
        
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
            			
            			//SEND ACK TO SERVER WITH THE HIGHEST FRAME NUMBER
                		sendData = new byte[index.getBytes().length];
                		sendData = index.getBytes();
                		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inetC, clientPort);
                        
                		socket.send(sendPacket);
            		
                		FOS.close();
                		System.out.println("file transfered");
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
        		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inetC, clientPort);
                
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
    		
    		if( ackInt == totalPackets - 1) 
    		{
    			System.out.println("File was sent to the client");
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
    	
    	
//        int lastReceived = 0;
//        int lastSent = 0;
//        boolean[] receivedMap = new boolean[packetList.size()];     //tout est false par defaut
//
//        while(true){
//            while(((lastSent - lastReceived) < WINDOWS) && (lastSent < packetList.size())){
//                System.out.println("sending paquet no :" + lastSent + " / size : " + packetList.get(lastSent).getLength());
//                socket.setSoTimeout(1);
//                socket.send(packetList.get(lastSent));
//                lastSent++;
//            }
//
//            DatagramPacket ack = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
//            String ackString;
//            try{
//                //on essaye de recevoir le ack du client.
//                socket.setSoTimeout(THREAD_SLEEP);
//                //ackString = read(ack);
//                socket.receive(ack); 
//                ackString = new String(ack.getData());
//                
//                System.out.println("received ack :" + ackString);
//
//                //termine s'il s'agit du dernier paquet a recevoir un ack
//                if(ackString.equals(packetList.size()) && lastSent == packetList.size()){
//                    break;
//                }
//            } catch(SocketTimeoutException e) {
//                // On envoye l'ensemble des paquets
//                for(int i = lastReceived + 1; i < lastSent; i++ ){
//                    socket.setSoTimeout(1);
//                    System.out.println("resending paquet no :" + i + " / size : " + packetList.get(lastSent).getLength());
//                    socket.send(packetList.get(i));
//                }
//            }
//        }
    }

    //---------------------------------------------

    /** Décodage et appel d'une transaction */
    private void executeTransaction(StringTokenizer tokenizer) {
        try {
            String command = tokenizer.nextToken();

            /* Général */
            if (command.equals("GETFILE"))
            {
                String fileName = (String) read(tokenizer, "string");
                sendFile(fileName);
            }
            if (command.equals("SENDFILE"))
            {
            		System.out.println("helloWorld!!!");
                    String fileName = (String) read(tokenizer, "string");
            		receiveFile(fileName);
            }
            else System.out.println("  Requete invalide.  Essayer \"help\"");
        }
        catch (Exception e) {
            System.out.println("** " + e.toString());
        }
    }

    /** Vérifie s'il reste des transactions à executer. */
    private boolean transactionDone(String transaction){
        if (transaction == null) return true;
        StringTokenizer tokenizer = new StringTokenizer(transaction, " ");

        if (!tokenizer.hasMoreTokens()) return false;
        String commande = tokenizer.nextToken();
        return commande.equals("exit");
    }

    /** Liste des actions que le handler est capable d'effectuer */
    private HashMap makeTransactions(){
        HashMap map = new HashMap();

        map.put("GETFILE", "String fileName");
        map.put("CONNECTIONREQUEST", null);
        return map;
    }

    /** Converti les chaînes de caractères en objet. */
    private Object read(StringTokenizer tokenizer, String type) throws Exception {
        Object value = null;
        if(tokenizer.hasMoreElements()){
            switch (type){
                case "string" :
                    value = tokenizer.nextToken();
                    break;

                case "int" :
                    String token = tokenizer.nextToken();
                    try {
                        value = Integer.valueOf(token);
                    } catch (NumberFormatException e) {
                        throw new Exception("Nombre attendu à la place de \"" + token + "\"");
                    }
                    break;
            }
        } else throw new Exception ("Nombre de paramètres insuffisant");
        return value;
    }

    /** Lecture d'une transaction */
    private String readTransaction(BufferedReader reader) throws IOException {
        System.out.print("> ");
        String transaction = reader.readLine();
        return transaction;
    }
}
