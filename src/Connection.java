/**
 *	@author Ariana Fairbanks
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection implements Runnable
{
	private Socket client;
	private static final int BUFFER_SIZE = 1024;
	
	public Connection(Socket client) 
	{
		this.client = client;
	}

	public void run() 
	{ 
		try 
		{
			byte[] buffer = new byte[BUFFER_SIZE];
			InputStream  fromClient = null;
			OutputStream toClient = null;
			
			try 
			{
				fromClient = new BufferedInputStream(client.getInputStream());
				toClient = new BufferedOutputStream(client.getOutputStream());
				int numBytes;
				
				
				while ( (numBytes = fromClient.read(buffer)) != -1) 
				{
					String hostName = new String(buffer).trim();
					System.out.println(hostName);
					byte[] hostAddressBytes = null;
					try
					{
						String hostAddress = InetAddress.getByName(hostName).getHostAddress() + "\n";
						System.out.println("Address " + hostAddress);
						hostAddressBytes = hostAddress.getBytes();
					}
					catch (UnknownHostException uhe) 
					{
						System.err.println("Unknown Host");
						hostAddressBytes = "Unknown Host\n".getBytes();
						System.out.println("reached catch");
					}
					finally
					{
						int length = hostAddressBytes.length;
						toClient.write(hostAddressBytes, 0, length);
						toClient.flush();	
					}
				}	
	   		}
			catch (IOException | NumberFormatException e) 
			{	System.err.println(e);	}
			finally 
			{
				buffer = null;
				if (fromClient != null) 
				{	fromClient.close();	}
				if (toClient != null)
				{	toClient.close();	}
			}
		}
		catch (java.io.IOException ioe) 
		{	System.err.println(ioe);	}
	}
}
