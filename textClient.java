import java.io.*; import java.net.*;
import java.util.Scanner;

public class textClient
{
    public static void main (String[] args) throws Exception
    {
        // Create Socket
        Scanner scan = new Scanner(System.in);
        System.out.println("Input Server Address");
        String addr = scan.nextLine();

        Socket socket;
        while (true) {
            try {
                socket = new Socket(addr, 7070); // IP of Server
                break;
            } catch (IOException e) {
                System.out.println("Connection Failed");
            }
        }
        
        // Streams
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        PrintWriter outp = new PrintWriter(os, true);
        BufferedReader inp = new BufferedReader(new InputStreamReader(is));
        
        // Send and Receive
        while (true){
            System.out.println("Input: ");
            String msg = scan.nextLine();
            
            outp.println(msg);
            String received = inp.readLine();
            System.out.println(received);
        }
        
        // outp.close(); inp.close(); socket.close();
    }
}