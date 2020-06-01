package app;

import java.net.InetAddress;
import app.Client;

public class Main {

    public static void main(String[] args) {
		try 
		{			
			InetAddress iPAddress = InetAddress.getByName("localhost");
	        Client client = new Client(18819, iPAddress);
	        client.requestFile("verySmallFile.txt");
	        System.out.println("File as been transfered");
			
			//InetAddress iPAddress = InetAddress.getByName("localhost");
	        //Client client = new Client(18818, iPAddress);
	        //client.sendFile("verySmallFile.txt");

		}
		catch (Exception e) 
		{
            e.printStackTrace();
        }
    }
}
