/**
 * A simple program demonstrating server sockets.
 *
 * @author Greg Gagne.
 */

package res;

import java.net.*;

public class SimpleServer
{
	public static final int PORT = 6500;
	
	public static void main(String[] args) throws java.io.IOException {
		// create a server socket listening to port 2500
		ServerSocket server = new ServerSocket(PORT);
		System.out.println("Waiting for connections ....");

		while (true) {
			// we block here until there is a client connection
			Socket client = server.accept();
			
			/**
			 * we have a connection!
			 * Let's get some information about it. 
			 * An InetAddress is an IP address
			 */ 	
			
			// get the server-side info
			System.out.print(InetAddress.getLocalHost() + " : ");
			System.out.println(server.getLocalPort());
			
			// get the client-side info
			InetAddress ipAddr = client.getInetAddress();
			System.out.print(ipAddr.getHostAddress() + " : ");
			System.out.println(client.getPort());
			
			// close the socket
			client.close();
		}
	}
}
