import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class client extends JFrame {

	/*
	 * References: https://www.youtube.com/watch?v=rd272SCl-XE
	 * https://www.youtube.com/watch?v=ZzZeteJGncY
	 */
	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JTextField clientTypingBoard;
	private JList clientActiveUsersList;
	private JTextArea clientMessageBoard;
	private JButton clientKillProcessBtn;
	private JRadioButton oneToNRadioBtn;
	private JRadioButton broadcastBtn;

	private JPanel whiteboard_panel;
	private drawing_board whiteboard;

	private boolean buzzing = false;
	private boolean buzzerEnabled = true;

	DataInputStream inputStream;
	DataOutputStream outStream;
	DefaultListModel<String> dm;
	String id, clientIds = "";

	/**
	 * Launch the application.
	 */
	// public static void main(String[] args) {
	// EventQueue.invokeLater(new Runnable() {
	// public void run() {
	// try {
	// ClientView window = new ClientView();
	// window.frame.setVisible(true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	// }

	/**
	 * Create the application.
	 */

	public client() {
		initialize();
	}

	public client(String id, Socket s) { // constructor call, it will initialize required variables
		initialize(); // initilize UI components
		whiteboard.drawArea.setSocket(s); // TEST
		whiteboard.toolbar.setSocket(s);
		this.id = id;
		try {
			frame.setTitle("Client View - " + id); // set title of UI
			dm = new DefaultListModel<String>(); // default list used for showing active users on UI
			clientActiveUsersList.setModel(dm);// show that list on UI component JList named clientActiveUsersList
			inputStream = new DataInputStream(s.getInputStream()); // initilize input and output stream
			outStream = new DataOutputStream(s.getOutputStream());
			new Read().start(); // create a new thread for reading the messages
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	class Read extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					String m = inputStream.readUTF(); // read message from server, this will contain :;.,/=<comma
														// seperated clientsIds>
					System.out.println("inside read thread : " + m); // print message for testing purpose

					if (m.equals("buzz")){
						buzzerEnabled = false; // disable our buzzer
						clientTypingBoard.setVisible(buzzerEnabled);
					}else if (m.equals("unbuzz")) {
						buzzerEnabled = true; // reenable our buzzer
					}

					else if (m.contains(":;.,/=")) { // prefix(i know its random)
						m = m.substring(6); // comma separated all active user ids
						dm.clear(); // clear the list before inserting fresh elements
						StringTokenizer st = new StringTokenizer(m, ","); // split all the clientIds and add to dm below
						while (st.hasMoreTokens()) {
							String u = st.nextToken();
							if (!id.equals(u)) // we do not need to show own user id in the active user list pane
								dm.addElement(u); // add all the active user ids to the defaultList to display on active
													// user pane on client view
						}
					} else { // ==============[ THIS IS WHERE WE CHECK FOR DRAWING COMMANDS
								// ]==================
						String mtemp = m.substring(m.lastIndexOf(">") + 1);
						System.out.println("mtemp: " + mtemp);
						if (mtemp != null && mtemp.matches("paint,-?\\d+,-?\\d+,-?\\d+,-?\\d+")) {
							// TESTING
							System.out.println("True!");
							String[] mSplit = mtemp.split(",");
							whiteboard.drawArea.drawHelper(Integer.parseInt(mSplit[1]), Integer.parseInt(mSplit[2]),
									Integer.parseInt(mSplit[3]), Integer.parseInt(mSplit[4]));
						} else if (mtemp != null && mtemp.matches("erase,-?\\d+,-?\\d+")) {
							// TESTING FOR ERASER
							System.out.println("Erasing!");
							String[] mSplit = mtemp.split(",");
							whiteboard.drawArea.eraseHelper(Integer.parseInt(mSplit[1]), Integer.parseInt(mSplit[2]));
						} else if (mtemp != null && mtemp.equals("erase,all,clear")) {
							// TESTING FOR ERASER
							System.out.println("Clearing!");
							whiteboard.drawArea.clear();
						}
						else {
							clientMessageBoard.append("" + m + "\n"); // otherwise print on the clients message board
						}
						// TESTING
						// whiteboard.drawArea.drawHelper(12, 25, 100,250);

						// added code here, disconnect all users and close client frame
						if (m.endsWith("winner!") || m.endsWith("correctly")) {
							JOptionPane.showMessageDialog(frame, m); // show message received from server
							outStream.writeUTF("exit"); // closes the thread and show the message on server and client's
														// message board
							clientMessageBoard.append("You are disconnected now.\n");
							frame.dispose(); // close the frame
						}
						// to here
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() { // initialize all the components of UI
		frame = new JFrame();
		frame.setBounds(100, 100, 1526, 705);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client View");

		clientMessageBoard = new JTextArea();
		clientMessageBoard.setEditable(false);
		clientMessageBoard.setBounds(612, 25, 530, 495);
		frame.getContentPane().add(clientMessageBoard);

		// chat box field
		clientTypingBoard = new JTextField();
		clientTypingBoard.setHorizontalAlignment(SwingConstants.LEFT);
		clientTypingBoard.setBounds(612, 533, 530, 84);
		frame.getContentPane().add(clientTypingBoard);
		clientTypingBoard.setColumns(10);
		// should only show the text box if a person is buzzing in
		clientTypingBoard.setVisible(buzzing);

		// buzzer button
		JButton buzzerBtn = new JButton("Buzzer");
		buzzerBtn.setBounds(612, 533, 530, 84);
		buzzerBtn.setBackground(Color.RED);
		frame.getContentPane().add(buzzerBtn);
		buzzerBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (buzzerEnabled) {
					buzzing = true; // toggles the buzzing flag so that the text box will show
					clientTypingBoard.setVisible(buzzing); // makes the text box visible
					try {
						outStream.writeUTF("buzz"); // sends a message to server that someone is buzzing
					} catch (Exception ex) {
						// TODO: handle exception
					}
					
				}

			}
		});

		JButton clientSendMsgBtn = new JButton("Send");
		clientSendMsgBtn.addActionListener(new ActionListener() { // action to be taken on send message button
			public void actionPerformed(ActionEvent e) {
				buzzing = false;
				try {
						outStream.writeUTF("unbuzz"); // sends a message to server that someone is buzzing
					} catch (Exception ex) {
						// TODO: handle exception
					}
				// hides the text field again and shows the buzzer if the user sends a guess
				// maybe add a timer?
				clientTypingBoard.setVisible(buzzing);
				buzzerBtn.setVisible(!buzzing);
				String textAreaMessage = clientTypingBoard.getText(); // get the message from textbox
				if (textAreaMessage != null && !textAreaMessage.isEmpty()) { // only if message is not empty then send
																				// it further otherwise do nothing
					try {
						String messageToBeSentToServer = "";
						String cast = "broadcast"; // this will be an identifier to identify type of message
						int flag = 0; // flag used to check whether used has selected any client or not for multicast
						if (oneToNRadioBtn.isSelected()) { // if 1-to-N is selected then do this
							cast = "multicast";
							// List<String> clientList = clientActiveUsersList.getSelectedValuesList(); //
							// get all the users selected on UI
							// if (clientList.size() == 0) // if no user is selected then set the flag for
							// further use
							// flag = 1;
							// for (String selectedUsr : clientList) { // append all the usernames selected
							// in a variable
							// if (clientIds.isEmpty())
							// clientIds += selectedUsr;
							// else
							// clientIds += "," + selectedUsr;
							// }
							// messageToBeSentToServer = cast + ":" + clientIds + ":" + textAreaMessage; //
							// prepare message to be sent to server
							messageToBeSentToServer = cast + ":" + textAreaMessage;
						} else {
							messageToBeSentToServer = cast + ":" + textAreaMessage; // in case of broadcast we don't
																					// need to know userIds
						}
						if (cast.equalsIgnoreCase("multicast")) {
							// if (flag == 1) { // for multicast check if no user was selected then prompt a
							// message dialog
							// JOptionPane.showMessageDialog(frame, "No user selected");
							// }
							// else { // otherwise just send the message to the user
							outStream.writeUTF(messageToBeSentToServer);
							clientTypingBoard.setText("");
							clientMessageBoard.append("< You sent msg to server>" + textAreaMessage + "\n"); // show the
																												// sent
																												// message
																												// to
																												// the
																												// sender's
																												// message
																												// board
							// }
						} else { // in case of broadcast
							outStream.writeUTF(messageToBeSentToServer);
							clientTypingBoard.setText("");
							clientMessageBoard.append("< You sent msg to All >" + textAreaMessage + "\n");
						}
						clientIds = ""; // clear the all the client ids
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(frame, "User does not exist anymore."); // if user doesn't exist
																								// then show message
					}
				}
			}
		});
		clientSendMsgBtn.setBounds(1154, 533, 137, 84);
		frame.getContentPane().add(clientSendMsgBtn);

		clientActiveUsersList = new JList();
		clientActiveUsersList.setToolTipText("Active Users");
		clientActiveUsersList.setBounds(1154, 63, 327, 457);
		frame.getContentPane().add(clientActiveUsersList);

		// THIS IS THE KILL BUTTON
		clientKillProcessBtn = new JButton("Kill Process");
		clientKillProcessBtn.addActionListener(new ActionListener() { // kill process event
			public void actionPerformed(ActionEvent e) {
				try {
					outStream.writeUTF("exit"); // closes the thread and show the message on server and client's message
												// board
					clientMessageBoard.append("You are disconnected now.\n");
					frame.dispose(); // close the frame
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		clientKillProcessBtn.setBounds(1303, 533, 193, 84);
		frame.getContentPane().add(clientKillProcessBtn);
		// END OF KILL BUTTON

		JLabel lblNewLabel = new JLabel("Active Users");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(1159, 43, 95, 16);
		frame.getContentPane().add(lblNewLabel);

		oneToNRadioBtn = new JRadioButton("Declare Word");
		oneToNRadioBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientActiveUsersList.setEnabled(true);
			}
		});
		// oneToNRadioBtn.setSelected(true); // width 72
		oneToNRadioBtn.setBounds(1370, 24, 120, 25);
		frame.getContentPane().add(oneToNRadioBtn);

		broadcastBtn = new JRadioButton("Chat");
		broadcastBtn.setSelected(true);
		broadcastBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientActiveUsersList.setEnabled(false);
			}
		});
		broadcastBtn.setBounds(1250, 24, 107, 25);
		frame.getContentPane().add(broadcastBtn);

		ButtonGroup btngrp = new ButtonGroup();
		btngrp.add(oneToNRadioBtn);
		btngrp.add(broadcastBtn);

		whiteboard = new drawing_board();
		
		// whiteboard_panel = new drawing_board().get_panel();
		whiteboard_panel = whiteboard.get_panel();
		whiteboard_panel.setBounds(12, 25, 550, 600);
		frame.getContentPane().add(whiteboard_panel);

		frame.setVisible(true);
	}

}
