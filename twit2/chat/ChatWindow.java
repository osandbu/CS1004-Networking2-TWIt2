package twit2.chat;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import twit2.Peer;
import twit2.awt.HorizontalPanel;
import twit2.awt.Popup;
import twit2.awt.VerticalPanel;
import twit2.io.ByteReader;
import twit2.io.ByteWriter;

/**
 * The chat window used to send and receive messages between two users.
 * 
 * @author os75
 */
public class ChatWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final int HEIGHT = 275;
	private static final int WIDTH = 300;
	// GUI Components
	private JTextArea conversationTextArea;
	private JTextField enterMessageField;
	private JButton sendButton;

	private Socket connection;
	private InputStream input;
	private OutputStream output;
	private String myNickname;
	private String otherNickname;

	/**
	 * Create a new Chat window.
	 * 
	 * @param myNickname
	 *            The nickname of the user using the application.
	 * @param peer
	 *            The peer to which the user wishes to connect.
	 * @throws UnknownHostException
	 *             If the hostname of the peer is not found.
	 * @throws IOException
	 *             If other IO errors occur.
	 */
	public ChatWindow(String myNickname, Peer peer)
			throws UnknownHostException, IOException {
		super();
		this.myNickname = myNickname;
		otherNickname = peer.getNickname();
		connection = new Socket(peer.getHostname(), peer.getPort());
		input = connection.getInputStream();
		output = connection.getOutputStream();
		ByteWriter.write(output, myNickname);
		init();
	}

	/**
	 * Create a new chat window with a given connection.
	 * 
	 * @param myNickname
	 *            The nickname of the user using the application.
	 * @param connection
	 *            A connection to another peer.
	 * @throws IOException
	 *             If an IO error occurs.
	 */
	public ChatWindow(String myNickname, Socket connection) throws IOException {
		super();
		this.myNickname = myNickname;
		this.connection = connection;
		input = connection.getInputStream();
		output = connection.getOutputStream();
		otherNickname = ByteReader.read(input, 16);
		init();
	}

	/**
	 * Initialise graphical user interface and set up other variables.
	 */
	public void init() {
		setTitle("Conversation with " + otherNickname);
		conversationTextArea = new JTextArea();
		new MessageReceiver(input, conversationTextArea, otherNickname).start();
		makeGUI();
		setVisible(true);
		// Request that the field in which the user inputs messages gets
		// keyboard focus.
		enterMessageField.requestFocus();
	}

	/**
	 * Set up the graphical user interface.
	 */
	private void makeGUI() {
		setSize(WIDTH, HEIGHT);
		// put frame to the center of the screen
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		JPanel mainPanel = new VerticalPanel();
		JScrollPane messagePane = initTextAreaScrollPane();
		mainPanel.add(messagePane);
		JPanel messagePanel = initEnterMessageFieldPane();
		mainPanel.add(messagePanel);
		setContentPane(mainPanel);
	}

	/**
	 * Initialise text area scroll pane, in which the messages are displayed.
	 * 
	 * @return A scrollpane.
	 */
	private JScrollPane initTextAreaScrollPane() {
		conversationTextArea.setEditable(false);
		// turn on word-wrap
		conversationTextArea.setLineWrap(true);
		conversationTextArea.setWrapStyleWord(true);
		JScrollPane textAreaScrollPane = new JScrollPane(conversationTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		DefaultCaret caret = (DefaultCaret) conversationTextArea.getCaret();
		/*
		 * Make the scrollbar always scroll all the way down when text is
		 * appended upon.
		 */
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		textAreaScrollPane.setPreferredSize(new Dimension(5000, 5000));
		return textAreaScrollPane;
	}

	/**
	 * Initialise the panel in which the field in which messages are entered and
	 * the send button is contained.
	 * 
	 * @return A panel.
	 */
	private JPanel initEnterMessageFieldPane() {
		JPanel panel = new HorizontalPanel();
		enterMessageField = new JTextField();
		enterMessageField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				int keyCode = keyEvent.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}
		});
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				sendMessage();
			}
		});
		panel.add(enterMessageField);
		panel.add(sendButton);
		return panel;
	}

	/**
	 * Send the message contained in the textfield.
	 */
	private void sendMessage() {
		if (connection.isClosed()) {
			return;
		}
		String message = enterMessageField.getText().trim();
		if (message.length() == 0) {
			return;
		} else if (message.length() > 140) {
			Popup
					.reportError(
							this,
							"The maximum message length is 140 characters.\nPlease shorten it.",
							"Message too long");
			return;
		}
		try {
			ByteWriter.write(output, message);
			enterMessageField.setText("");
			conversationTextArea.append(myNickname + ": " + message + "\n");
		} catch (SocketTimeoutException ste) {
			Popup.reportError(this,
					"Connection timed out. Please try again later.",
					"Server not responding");
		} catch (UnknownHostException uhe) {
			Popup.reportError(this, "Could not connect to server.\n"
					+ "Make sure the hostname and port is correct.",
					"Invalid server");
		} catch (IOException ioe) {
			Popup.reportError(this,
					"Could not connect to server. Please try again later.",
					"Server not responding");
		}
	}

	/**
	 * Close the connection with a client.
	 */
	public void close() {
		if (connection != null) {
			try {
				ByteWriter.write(output, "/quit");
				connection.close();
			} catch (IOException e) {
			}
		}
		dispose();
	}
}
