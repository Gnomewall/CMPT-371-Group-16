import java.io.*; import java.net.*;

public class serverThread extends Thread{
    private final Socket socket;
    
    public serverThread(Socket s) {
        socket = s;
    }

    public void run() {

        OutputStream os = null;
        try {
            os = socket.getOutputStream();
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

		InputStream is = null;
        try {
            is = socket.getInputStream();
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
		PrintWriter outp = new PrintWriter(os, true);
		BufferedReader inp = new BufferedReader(new InputStreamReader(is));

        while (true) {

		    // Get message; blocking
		    String temp = null;
            try {
                temp = inp.readLine();
            } catch(Exception e) {
                e.printStackTrace();
            }

            System.out.println(temp);
            
            outp.println("ACK: " + temp);
        }
    }
}
