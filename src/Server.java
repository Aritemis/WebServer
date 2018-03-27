/**
 *	@author Ariana Fairbanks
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server 
{

	public static final int DEFAULT_PORT = 8080;
	private static final Executor executor = Executors.newCachedThreadPool();
	
	public static void main(String[] args) throws IOException 
	{
		ServerSocket socket = null;
		try 
		{
			socket = new ServerSocket(DEFAULT_PORT);
			while (true) 
			{
				Runnable task = new Connection(socket.accept());
				executor.execute(task);
			}
		}
		catch (IOException ioe) { }
		finally 
		{
			if (socket != null)
			{	socket.close();	}
		}
	}
}
