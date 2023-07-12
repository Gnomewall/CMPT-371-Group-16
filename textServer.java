import java.io.*; import java.net.*;
import java.util.Enumeration;

public class textServer
{
    public static void main (String[] args) throws Exception {
    
    	// Create Socket
    	ServerSocket server;
    	try {
    		server = new ServerSocket(7070);
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw new Exception("Socket Creation Failed");
    	}
    
    	while (true) {
            Socket client = null;
            try {
    		    client = server.accept();

                Thread thread = new serverThread(client);
                thread.start();
            } catch(Exception ex) {
                client.close();
                ex.printStackTrace();
                return;
            }
    	}
    }
}
