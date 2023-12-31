import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
import javax.swing.SwingUtilities;

public class client extends JFrame {

	/*
	 * UI References: https://www.youtube.com/watch?v=rd272SCl-XE
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
	private	JButton buzzerBtn;

	DataInputStream inputStream;
	DataOutputStream outStream;
	DefaultListModel<String> dm;
	String id, clientIds = "";


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

					if (m.equals("buzz")) {
						SwingUtilities.invokeLater(() -> {
							System.out.println("Disabling Buzzer");
							buzzerBtn.setText("Disabled");
							buzzerEnabled = false;
							clientTypingBoard.setVisible(buzzerEnabled);
						});
					} else if (m.equals("unbuzz")) {
						SwingUtilities.invokeLater(() -> {
							System.out.println("Enabling Buzzer");
							buzzerBtn.setText("Buzzer");
							buzzerEnabled = true;
						});
					}
					
					else if (m.contains(":;.,/=")) { 
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
							// TESTING FOR CLEARING
							System.out.println("Clearing!");
							whiteboard.drawArea.clear();
						} else if (mtemp != null && mtemp.equals("control,setdrawing,true")) {
							// TESTING FOR CONTROL MESSAGE
							System.out.println("Control message successful!");
							whiteboard.drawArea.setIsDrawing(true);
							whiteboard.toolbar.setIsDrawing(true);
						}
						else {
							clientMessageBoard.append("" + m + "\n"); // otherwise print on the clients message board
						}

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
		frame.setBounds(50, 50, 1200, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client View");

		clientMessageBoard = new JTextArea();
		clientMessageBoard.setEditable(false);
		clientMessageBoard.setBounds(600, 25, 300, 400);
		frame.getContentPane().add(clientMessageBoard);

		// chat box field
		clientTypingBoard = new JTextField();
		clientTypingBoard.setHorizontalAlignment(SwingConstants.LEFT);
		clientTypingBoard.setBounds(600, 450, 300, 80);
		frame.getContentPane().add(clientTypingBoard);
		clientTypingBoard.setColumns(10);

		// should only show the text box if a person is buzzing in
		clientTypingBoard.setVisible(buzzing);

		// buzzer button
		buzzerBtn = new JButton("Buzzer");
		buzzerBtn.setBounds(600, 450, 300, 80);
		buzzerBtn.setBackground(Color.RED);
		frame.getContentPane().add(buzzerBtn);
		buzzerBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (buzzerEnabled) {
					buzzing = !buzzing; // toggles the buzzing flag so that the text box will show
					clientTypingBoard.setVisible(buzzing); // makes the text box visible
					buzzerBtn.setVisible(!buzzing);
					try {
						outStream.writeUTF(buzzing ? "buzz" : "unbuzz");; // sends a message to server that someone is buzzing
					} catch (Exception ex) {
						ex.printStackTrace();
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
					ex.printStackTrace();
				}
				// hides the text field again and shows the buzzer if the user sends a guess
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

							// prepare message to be sent to server
							messageToBeSentToServer = cast + ":" + textAreaMessage;
						} else {
							messageToBeSentToServer = cast + ":" + textAreaMessage; // in case of broadcast we don't
							// need to know userIds
						}
						if (cast.equalsIgnoreCase("multicast")) {
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
		clientSendMsgBtn.setBounds(950, 450, 90, 84);
		frame.getContentPane().add(clientSendMsgBtn);

		clientActiveUsersList = new JList();
		clientActiveUsersList.setToolTipText("Active Users");
		clientActiveUsersList.setBounds(950, 50, 200, 300);
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
		clientKillProcessBtn.setBounds(1050, 450, 90, 84);
		frame.getContentPane().add(clientKillProcessBtn);
		// END OF KILL BUTTON

		JLabel lblNewLabel = new JLabel("Active Users");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(950, 25, 95, 16);
		frame.getContentPane().add(lblNewLabel);

		oneToNRadioBtn = new JRadioButton("Declare Word");
		oneToNRadioBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientActiveUsersList.setEnabled(true);
			}
		});

		oneToNRadioBtn.setBounds(950, 375, 120, 25);
		frame.getContentPane().add(oneToNRadioBtn);

		broadcastBtn = new JRadioButton("Chat");
		broadcastBtn.setSelected(true);
		broadcastBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientActiveUsersList.setEnabled(false);
			}
		});
		broadcastBtn.setBounds(950, 400, 107, 25);
		frame.getContentPane().add(broadcastBtn);

		ButtonGroup btngrp = new ButtonGroup();
		btngrp.add(oneToNRadioBtn);
		btngrp.add(broadcastBtn);

		whiteboard = new drawing_board();

		whiteboard_panel = whiteboard.get_panel();
		whiteboard_panel.setBounds(12, 25, 550, 500);
		frame.getContentPane().add(whiteboard_panel);

		frame.setVisible(true);
	}

}