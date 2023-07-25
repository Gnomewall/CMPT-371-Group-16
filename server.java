import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;


public class server {
	/*
	 * References: https://www.youtube.com/watch?v=rd272SCl-XE
	 * 			   https://www.youtube.com/watch?v=ZzZeteJGncY
	 * */

	private static final long serialVersionUID = 1L;

	private static Map<String, Socket> allUsersList = new ConcurrentHashMap<>(); // Stores usernames and their associated socket
	private static Set<String> activeUserSet = new HashSet<>(); // Stores usernames of active users
	private static int port = 7070;  // Port Number
	private JFrame frame;
	private ServerSocket serverSocket; // Server's Socket
	private JTextArea serverMessageBoard; // Server's message board (used to print to GUI)
	private JList allUserNameList;
	private JList activeClientList;
	private DefaultListModel<String> activeDlm = new DefaultListModel<String>(); // List of active users (for GUI)
	private DefaultListModel<String> allDlm = new DefaultListModel<String>(); // List of users (for GUI)
	private String drawingPlayer;
	private String keyWord;					//define chosen keyword string by player to draw
	private boolean isGameOver = false;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					server window = new server(); // Create window
					window.frame.setVisible(true); // Make window visible
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public server() {
		initialize();  // Swing components initialized here
		try {
			serverSocket = new ServerSocket(port);  // Server Socket Creation
			serverMessageBoard.append("Server started on port: " + port + "\n"); // Print to Server Message Board
			serverMessageBoard.append("Waiting for the clients...\n");
			new ClientAccept().start(); // Client thread
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class ClientAccept extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Socket clientSocket = serverSocket.accept();  // Client's socket
					String uName = new DataInputStream(clientSocket.getInputStream()).readUTF(); // this will receive the username sent from client register view
					DataOutputStream cOutStream = new DataOutputStream(clientSocket.getOutputStream()); // create an output stream for client
					if (activeUserSet != null && activeUserSet.contains(uName)) { // if username is in use then we need to prompt user to enter new name
						cOutStream.writeUTF("Username already taken");
					} else {
						allUsersList.put(uName, clientSocket); 
						activeUserSet.add(uName);
						cOutStream.writeUTF("");
						activeDlm.addElement(uName); 
						if (!allDlm.contains(uName)) 
							allDlm.addElement(uName);
						activeClientList.setModel(activeDlm); 
						allUserNameList.setModel(allDlm);
						serverMessageBoard.append("Client " + uName + " Connected...\n"); 

						new MsgRead(clientSocket, uName).start(); // Read Messages in new thread
						new PrepareClientList().start(); // Update active clients list in new thread
					}
				} catch (IOException ioex) {
					ioex.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Read incoming messages (This handles chat broadcasts and private messages to the server)
	class MsgRead extends Thread { 
		Socket s;
		String Id;

		private MsgRead(Socket s, String uname) { 
			this.s = s;
			this.Id = uname;
		}

		@Override
		public void run() {
			while (allUserNameList != null && !allUsersList.isEmpty()) { 
				try {
					String message = new DataInputStream(s.getInputStream()).readUTF(); // Read client message
					System.out.println("message read ==> " + message); 
					String[] msgList = message.split(":"); // Identifier; used to differentiate between public / private chat messages

					// === [THIS IS WHERE THE SERVER HANDLES PRIVATE MESSAGES FOR DECLARING A WORD TO DRAW] ===
					if (msgList[0].equalsIgnoreCase("multicast")) { 
							try {
								
								if (drawingPlayer != null && drawingPlayer.equalsIgnoreCase(Id) && keyWord == null) {
									keyWord = msgList[1];
									new DataOutputStream(s.getOutputStream())
												.writeUTF("Your word is: " + msgList[1] + ". You may begin drawing!\n");
									serverMessageBoard.append("Client " + drawingPlayer + " is drawing: " + keyWord + "\n");
								} else if (drawingPlayer != null && drawingPlayer.equalsIgnoreCase(Id) && keyWord != null) {
									new DataOutputStream(s.getOutputStream())
												.writeUTF("ERROR: You have already declared a word.\n");
								} else {
									new DataOutputStream(s.getOutputStream())
												.writeUTF("ERROR: It is not your turn to draw.\n");
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

					// === [THIS IS WHERE THE SERVER HANDLES PUBLIC CHAT BROADCAST] ===
					} else if (msgList[0].equalsIgnoreCase("broadcast")) {

						// Iterate over all clients
						Iterator<String> itr1 = allUsersList.keySet().iterator(); 
						while (itr1.hasNext()) {
							String usrName = (String) itr1.next();

								//added code from here, check/msg all users when guessed correctly 
								if(keyWord != null && msgList[1].equalsIgnoreCase(keyWord)) {						//check if guess word is correct 
									isGameOver = true;																	
									if(Id.equals(usrName) && activeUserSet.contains(usrName)) {					  		//compare if user equals to user who guessed right 
										new DataOutputStream(((Socket) allUsersList.get(usrName)).getOutputStream()) 	//retrieve user information and cast to socket 
										.writeUTF("\nYou guessed it correctly");								 	//send string to outpustream of client who won
									} else if(activeUserSet.contains(usrName)) {										//check for active clients 
										new DataOutputStream(((Socket) allUsersList.get(usrName)).getOutputStream()) 	//send msg to all other clients saying who was winner 
										.writeUTF("<"+Id+">" + msgList[1]+ "\n\n"+Id+" is winner!");				
									} else {
										new DataOutputStream(s.getOutputStream())
										.writeUTF("Message couldn't be delivered to user " + usrName + " because it is disconnected.\n");
									}					
									//to here 

								} else if (!usrName.equalsIgnoreCase(Id)) { // we don't need to send message to ourself, so we check for our Id
								try {
									if (activeUserSet.contains(usrName)) { 
										new DataOutputStream(((Socket) allUsersList.get(usrName)).getOutputStream())
												.writeUTF("< " + Id + " >" + msgList[1]);
									} else {
										new DataOutputStream(s.getOutputStream())
												.writeUTF("Message couldn't be delivered to user " + usrName + " because it is disconnected.\n");
									}
								} catch (Exception e) {
									e.printStackTrace(); 
								}
							}
						}
						// A client disconnects
					} else if (msgList[0].equalsIgnoreCase("exit")) { 
						activeUserSet.remove(Id); // Remove client from active user set
						serverMessageBoard.append(Id + " disconnected....\n");

						new PrepareClientList().start(); // Update active user list on UI

						Iterator<String> itr = activeUserSet.iterator(); 
						while (itr.hasNext()) {
							String usrName2 = (String) itr.next();
							if (!usrName2.equalsIgnoreCase(Id)) { 
								try {
									if(!keyWord.isEmpty()) {					//send msg to disconnected client 
										new DataOutputStream(((Socket) allUsersList.get(usrName2)).getOutputStream())
										.writeUTF(Id + " disconnected..."); // notify all other active user for disconnection of a user
									} 
								} catch (Exception e) {
									e.printStackTrace();
								}
								new PrepareClientList().start(); // Update all client's user lists after disconnection
							}
						}
						//added code here, Remove all users from allUsersList
						if(keyWord.isEmpty()) {						 
						allUsersList.remove(Id);					//remove the user from the list from all user
						allDlm.removeElement(Id);					//GUI representation of the list 
						}		
						//to here 

						activeDlm.removeElement(Id); // Remove client from JList
						activeClientList.setModel(activeDlm); // Update active user list
						allUserNameList.setModel(allDlm);		

					}
					
					//added code here, reset the values when game restarts 
					if(isGameOver) {				
						isGameOver = false;			
						keyWord = "";
					}		
					//to here 
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class PrepareClientList extends Thread { // Prepare list of active users to be displayed on GUI
		@Override
		public void run() {
			try {
				String ids = "";
				Iterator itr = activeUserSet.iterator(); // For all active users
				while (itr.hasNext()) { // Prepare string of all the users
					String key = (String) itr.next();
					ids += key + ",";
				}
				if (ids.length() != 0) { // Trim list
					ids = ids.substring(0, ids.length() - 1);
				}
				itr = activeUserSet.iterator(); 
				while (itr.hasNext()) { // Send list of all users
					String key = (String) itr.next();
					try {
						new DataOutputStream(((Socket) allUsersList.get(key)).getOutputStream())
								.writeUTF(":;.,/=" + ids); // set output stream and send the list of active users with identifier prefix :;.,/=
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() { //here components of Swing App UI are initilized
		frame = new JFrame();
		frame.setBounds(100, 100, 796, 530);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Server View");

		serverMessageBoard = new JTextArea();
		serverMessageBoard.setEditable(false);
		serverMessageBoard.setBounds(12, 29, 489, 435);
		frame.getContentPane().add(serverMessageBoard);
		serverMessageBoard.setText("Starting the Server...\n");

		allUserNameList = new JList();
		allUserNameList.setBounds(526, 324, 218, 140);
		frame.getContentPane().add(allUserNameList);

		activeClientList = new JList();
		activeClientList.setBounds(526, 128, 218, 156);
		frame.getContentPane().add(activeClientList);

		JLabel lblNewLabel = new JLabel("All Usernames");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(530, 295, 127, 16);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Active Users");
		lblNewLabel_1.setBounds(526, 103, 98, 23);
		frame.getContentPane().add(lblNewLabel_1);

		// === [THIS IS WHERE THE SERVER CHOOSES A PLAYER TO DRAW] ===
		JButton chooseDrawer = new JButton("Choose a Player to Draw");
		chooseDrawer.addActionListener(new ActionListener() { // action to be taken on send message button
			public void actionPerformed(ActionEvent e) {

				// Choose random player to draw
				if (activeUserSet.size() == 0) {
					serverMessageBoard.append("No active users!\n");
					return;
				}
				String[] sarr = activeUserSet.toArray(new String[activeUserSet.size()]);
				Random rn = new Random();
				int rnInt = rn.nextInt(activeUserSet.size());
				serverMessageBoard.append("Chosen: " + sarr[rnInt] + "\n");
				drawingPlayer = sarr[rnInt];

				// Inform chosen player
				// Socket tempSock = allUsersList.get(sarr[rnInt]);
				// PrintWriter tempPw = null;
				// try {
				// 	tempPw = new PrintWriter(tempSock.getOutputStream());
				// 	tempPw.println("You were selected to draw! Please declare your word.");
				// } catch (Exception exc) {exc.printStackTrace();}

				Iterator<String> itr1 = allUsersList.keySet().iterator(); // iterate over all users
				while (itr1.hasNext()) {
					String usrName = (String) itr1.next();
					try {
						if (activeUserSet.contains(usrName)) {
							new DataOutputStream(((Socket) allUsersList.get(usrName)).getOutputStream())
									.writeUTF("\nPLAYER " + drawingPlayer + " WILL BE DRAWING NEXT!\n" + drawingPlayer + ", PLEASE DECLARE A WORD.\n");
						}
					} catch (Exception excep) {
						excep.printStackTrace(); // throw exceptions
					}
				}
			}
		});
		chooseDrawer.setBounds(526, 30, 218, 60);
		frame.getContentPane().add(chooseDrawer);
	}
}