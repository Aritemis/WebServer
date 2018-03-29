/**
 *	@author Ariana Fairbanks
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class Connection implements Runnable
{
	private Socket client;
	private Configuration configFile;
	private BufferedReader fromClient = null;
	private DataOutputStream toClient = null;
	private String logRequest = null;
	private boolean isAFile = false;
	
	public Connection(Socket client, Configuration configFile) 
	{	
		this.client = client;
		this.configFile = configFile;
	}

	public void run() 
	{ 
		//TODO XML, maybe fixing client IP
		try 
		{
			fromClient = new BufferedReader(new InputStreamReader (client.getInputStream()));
			toClient = new DataOutputStream(client.getOutputStream());
			
			String clientRequest = fromClient.readLine();
			StringTokenizer tokenizer = new StringTokenizer(clientRequest);
			String method = tokenizer.nextToken();
			String resource = tokenizer.nextToken();

			logRequest = "\"" + clientRequest.trim() + "\" ";

			if (method.equals("GET")) 
			{
				String fileName = resource;//.replaceFirst("/", "");
				fileName = configFile.getDocumentRoot() + decode(fileName);
				if (resource.equals("/")) 
				{	
					fileName = configFile.getDefaultDocument();	
				} 
				if (new File(fileName).isFile())
				{	
					sendResponse(200, fileName, true);	
				}
				else if(fileName.equals(configFile.getDefaultDocument()))
				{
					File newDefault = new File(fileName);
					OutputStream outputStream = null;
					try 
				    {
				        outputStream = new FileOutputStream(newDefault);
				        String newFileContents = "<html><title>" + configFile.getServerName() + "</title><body>This is my default web page.</body></html>";
				        int length = newFileContents.length();
				        outputStream.write(newFileContents.getBytes(), 0, length);
				        outputStream.flush();
				    }
				    finally 
				    {	outputStream.close();	}
					sendResponse(200, fileName, true);	
				}
				else 
				{	
					sendResponse(404, "", false);	
				}
			}
			else
			{	
				sendResponse(405, "", false);	
			}
		}
		catch (java.io.IOException ioe) 
		{	System.err.println(ioe);	}

	}
	
	public void sendResponse (int statusCode, String fileString, boolean isFile) throws IOException
	{
		String status = "HTTP/1.1 ";
		String statusMessage = null;
		String date = getServerTime();
		String serverName = "Server: " + configFile.getServerName() +"\r\n";
		String contentType = "Content-Type: text/html\r\n";
		String contentLength = null;
		String fileName = null;
		FileInputStream fileData = null;
		int length = 0;

		if (statusCode == 200)
		{	
			statusMessage = "200 OK\r\n";
			status += statusMessage;
		}
		else if (statusCode == 404)
		{	
			statusMessage = "404 Not Found\r\n";
			status += statusMessage;
		}
		else
		{
			statusMessage = "405 Method Not Allowed\r\n";
			status += statusMessage;
		}

		if (isFile) 
		{
			fileName = fileString;
			fileData = new FileInputStream(fileName);
			length = fileData.available();
			contentLength = "Content-Length: " + length + "\r\n";
			if (!fileName.endsWith(".html"))
			{	
				if(fileName.endsWith(".gif"))
				{	contentType = "Content-Type: image/gif\r\n";	}
				else if(fileName.endsWith(".jpg"))
				{	contentType = "Content-Type: image/jpeg\r\n";	}
				else if(fileName.endsWith(".jpg"))
				{	contentType = "Content-Type: image/jpeg\r\n";	}
				else if(fileName.endsWith(".png"))
				{	contentType = "Content-Type: image/png\r\n";	}
				else if(fileName.endsWith(".txt"))
				{	contentType = "Content-Type: text/plain\r\n";	}
			}
		}
		else 
		{
			fileString = "<html><title>HTTP Server</title><body>" + statusMessage + "</body></html>";
			length = fileString.length();
			contentLength = "Content-Length: " + length + "\r\n";
		}

		String logString = client.getInetAddress().getHostAddress() + " [" + date + "] " + logRequest + statusCode + " " + length + "\n";
		System.out.println(logString);

		File newDefault = new File(configFile.getDocumentRoot() + configFile.getLogFile());
		OutputStream outputStream = null;
		try 
	    {
	        outputStream = new FileOutputStream(newDefault);
	        String newFileContents = logString;
	        int logFileLength = newFileContents.length();
	        outputStream.write(newFileContents.getBytes(), 0, logFileLength);
	        outputStream.flush();
	    }
	    finally 
	    {	outputStream.close();	}
		
		toClient.writeBytes(status);
		toClient.writeBytes(date);
		toClient.writeBytes(serverName);
		toClient.writeBytes(contentType);
		toClient.writeBytes(contentLength);
		toClient.writeBytes("Connection: close\r\n");
		toClient.writeBytes("\r\n");

		if (isFile) 
		{	sendFile(fileData, toClient);	}
		else 
		{	toClient.writeBytes(fileString);	}
		toClient.close();
		fromClient.close();
	}
	
	public void sendFile (FileInputStream fileData, DataOutputStream out) throws IOException 
	{
		byte[] buffer = new byte[1024] ;
		int bytesRead;

		while ((bytesRead = fileData.read(buffer)) != -1 ) 
		{	out.write(buffer, 0, bytesRead);	}
		fileData.close();
	}
	
	private String getServerTime() 
	{
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}
	
	private String decode(String value) 
	{
		String result = "";
		try
		{	result = URLDecoder.decode(value, StandardCharsets.UTF_8.toString());	}
		catch(UnsupportedEncodingException e)
		{	System.out.println("Something went wrong.");	}
	    return result;
	}
}
