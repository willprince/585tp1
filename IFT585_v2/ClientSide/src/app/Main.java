package app;

import java.net.InetAddress;
import app.Client;

public class Main {

    public static void main(String[] args) {
		try 
		{	
			
			
			
			if(args.length > 0) 
			{
				String command = args[0];
				if(command.equals("request")) 
				{
					String fileRequested = args[1];
					int port = Integer.parseInt(args[2]);
					
					System.out.println("You are requesting the file: " + fileRequested);
					
					InetAddress iPAddress = InetAddress.getByName("localhost");
			        Client client = new Client(port, iPAddress);
			        client.requestFile(fileRequested);
			        System.out.println("You have receive your file");  
				}
				else if(command.equals("send")) 
				{
					String fileToSend = args[1];
					int port = Integer.parseInt(args[2]);
					
					System.out.println("You are requesting the file: " + fileToSend);
					InetAddress iPAddress = InetAddress.getByName("localhost");
			        Client client = new Client(port, iPAddress);
			        client.sendFile(fileToSend);   
				}
				else {
					System.out.println("Enter the command and the server port");
				}
			}
					
		}
		catch (Exception e) 
		{
            e.printStackTrace();
        }
    }
}
