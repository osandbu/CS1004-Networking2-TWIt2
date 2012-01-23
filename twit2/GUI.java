package twit2;

import java.awt.Dimension;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import twit2.awt.HorizontalPanel;
import twit2.awt.Popup;
import twit2.awt.VerticalPanel;
import twit2.chat.ChatWindow;
import twit2.io.ByteWriter;

/**
 * The Messenger graphical user interface. Acts as both a server and a client.
 * Connects to a name-server in order to get the addresses and port numbers of
 * other peers. The connection with the name server is maintained in order to
 * keep track of who is online at any given time.
 * 
 * @author os75
 */
public class GUI extends JFrame implements ActionListener {
	/**
	 * Unused
	 */
	private static final long serialVersionUID = 1L;

	private static final int HEIGHT = 275;
	private static final int WIDTH = 300;
	private static final String SETTINGS_FILENAME = "settings.txt";
	private static final String TITLE = "Messenger Client";
	private static final int SO_TIMEOUT = 4000;

	// GUI Components.
	private JComboBox serverComboBox;
	private JList userList;
	private JButton onlineButton;
	private JButton connectButton;
	private MenuItem quitMenuItem;
	private MenuItem editServerMenuItem;
	private MenuItem addServerMenuItem;
	private MenuItem deleteServerMenuItem;
	private MenuItem editSettingsMenuItem;

	// Vector of Peers.
	private Vector<NameServerProfile> servers;
	private String nickName;
	private int portNumber;

	private Socket serverConnection;
	private Server server;
	private InputStream serverInput;
	private OutputStream serverOutput;
	private KeepAlive keepAlive;

	private JTextField statusField;
	public static final int DEFAULT_PORT = 60514;
	public static final String DEFAULT_HOSTNAME = "localhost";
	public static final NameServerProfile DEFAULT_SERVER = new NameServerProfile(
			"localhost", DEFAULT_HOSTNAME, DEFAULT_PORT);
	private static final int MIN_PORT_NUMBER = 32768;
	private static final int MAX_PORT_NUMBER = 61000;

	public GUI() {
		super(TITLE);
		loadSettings();
		makeGUI();

		setVisible(true);
	}

	private void setupServer() {
		if (portNumber < MIN_PORT_NUMBER || portNumber > MAX_PORT_NUMBER) {
			openSettingsDialog();
		}
		try {
			server = new Server(this, nickName, portNumber);
			server.start();
			onlineButton.setText("Go offline");
		} catch (SocketException e) {
			reportError("Port " + portNumber
					+ " is already in use by another service.", "Port taken");
			return;
		} catch (IOException e) {
			reportError("Error setting up server socket\n" + e.getMessage(),
					"IOError");
			return;
		}
	}

	private void shutDownServer() {
		server.shutDown();
		server = null;
		onlineButton.setText("Go online");
	}

	/**
	 * Initialise all GUI components of the main GUIClient window.
	 */
	private void makeGUI() {
		setWindowListener();
		this.setSize(WIDTH, HEIGHT);
		// put frame to the center of the screen
		setLocationRelativeTo(null);
		initMenus();
		final JPanel mainPanel = new VerticalPanel();
		JPanel serverPanel = initSelectServerPanel();
		mainPanel.add(serverPanel);
		mainPanel.add(initUserList());
		JPanel buttonPanel = initButtonPanel();
		mainPanel.add(buttonPanel);
		statusField = new JTextField();
		statusField.setEditable(false);
		statusField.setMaximumSize(new Dimension(10000, 20));
		mainPanel.add(statusField);
		setContentPane(mainPanel);
	}

	private JScrollPane initUserList() {
		DefaultListModel listModel = new DefaultListModel();
		userList = new JList(listModel);
		userList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = userList.locationToIndex(e.getPoint());
					userList.ensureIndexIsVisible(index);
					ListModel dlm = userList.getModel();
					Object item = dlm.getElementAt(index);
					if (!(item instanceof Peer))
						return;
					Peer peer = (Peer) item;
					if (!peer.getNickname().equals(nickName))
						openMessengerConnection(peer);
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(userList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return scrollPane;
	}

	/**
	 * Open a instant messaging window with a peer.
	 * 
	 * @param peer
	 *            A peer.
	 */
	private void openMessengerConnection(Peer peer) {
		try {
			new ChatWindow(nickName, peer);
		} catch (IOException e) {
			reportError("An error occured while trying to connect.",
					"Connection error");
		}
	}

	/**
	 * Setup a window listener which makes the program save the profile servers
	 * and close the problem when the window is closed.
	 */
	private void setWindowListener() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				/*
				 * Make the program save the profile servers and shut down the
				 * process when the window is closing.
				 */
				saveAndExit();
			}
		});
	}

	/**
	 * Initialise the menus. Including adding ActionListeners and shortcuts.
	 */
	private void initMenus() {
		Menu fileMenu = new Menu("File");
		quitMenuItem = new MenuItem("Quit");
		quitMenuItem.setShortcut(new MenuShortcut(KeyEvent.VK_Q));
		fileMenu.add(quitMenuItem);

		Menu editMenu = new Menu("Edit");
		addServerMenuItem = new MenuItem("Add new server");
		addServerMenuItem.setShortcut(new MenuShortcut(KeyEvent.VK_N));
		editMenu.add(addServerMenuItem);
		editServerMenuItem = new MenuItem("Edit selected server");
		editServerMenuItem.setShortcut(new MenuShortcut(KeyEvent.VK_E));
		editMenu.add(editServerMenuItem);
		deleteServerMenuItem = new MenuItem("Delete selected server");
		editMenu.add(deleteServerMenuItem);
		editSettingsMenuItem = new MenuItem("Edit settings");
		editMenu.add(editSettingsMenuItem);

		addMenuActionListeners();

		MenuBar menuBar = new MenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		this.setMenuBar(menuBar);
	}

	/**
	 * Add ActionListener to each of the MenuItems.
	 */
	private void addMenuActionListeners() {
		MenuItem[] menuItems = { quitMenuItem, editServerMenuItem,
				addServerMenuItem, deleteServerMenuItem, editSettingsMenuItem };

		for (MenuItem item : menuItems) {
			item.addActionListener(this);
		}
	}

	/**
	 * Initialise select server-profile panel, containing a label and a ComboBox
	 * from which server-profiles can be chosen.
	 * 
	 * @return The select server profile panel.
	 */
	private JPanel initSelectServerPanel() {
		JPanel serverPanel = new HorizontalPanel();
		JLabel serverLabel = new JLabel("Select server: ");
		serverComboBox = new JComboBox(servers);
		initButtonPanel();
		serverPanel.add(serverLabel);
		serverPanel.add(serverComboBox);
		return serverPanel;
	}

	private JPanel initButtonPanel() {
		JPanel buttonPanel = new HorizontalPanel();
		onlineButton = new JButton("Go online");
		onlineButton.addActionListener(this);
		connectButton = new JButton("Connect");
		connectButton.addActionListener(this);
		buttonPanel.add(onlineButton);
		buttonPanel.add(connectButton);
		return buttonPanel;
	}

	private void loadSettings() {
		servers = new Vector<NameServerProfile>();
		File settingsFile = new File(SETTINGS_FILENAME);
		Scanner in;
		try {
			in = new Scanner(settingsFile);
		} catch (FileNotFoundException fnfe) {
			openSettingsDialog();
			createSettingsFile(settingsFile);
			addDefaultServer();
			return;
		}
		if (in.hasNext()) {
			this.nickName = in.next();
		} else {
			this.nickName = "John";
		}
		if (in.hasNextInt()) {
			this.portNumber = in.nextInt();
		} else {
			this.portNumber = 60514;
		}
		while (in.hasNextLine()) {
			NameServerProfile server = NameServerProfile
					.fromLine(in.nextLine());
			if (server != null)
				servers.add(server);
		}
		in.close();
		if (servers.isEmpty()) {
			addDefaultServer();
		}
	}

	private void openSettingsDialog() {
		JLabel nickNameLabel = new JLabel("Enter nickname: ");
		JTextField nickNameField = new JTextField(nickName);
		JLabel portLabel = new JLabel("Port number: ");
		JTextField portField = new JTextField(Integer.toString(portNumber));
		Object[] message = { nickNameLabel, nickNameField, portLabel, portField };
		int option = JOptionPane.showConfirmDialog(this, message, "Settings",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			nickName = nickNameField.getText();
			try {
				String portStr = portField.getText();
				int port = Integer.parseInt(portStr);
				if (!Validator.isValidPortNumber(port)) {
					throw new NumberFormatException();
				} else {
					portNumber = Integer.parseInt(portStr);
				}
			} catch (NumberFormatException e) {
				reportError(
						"Please set the port number in the range 32768-61000.",
						"Try again.");
				openSettingsDialog();
			}
		}
		saveSettings();
	}

	/**
	 * Add default server to the list of profiles.
	 */
	private void addDefaultServer() {
		servers.add(GUI.DEFAULT_SERVER);
	}

	private void createSettingsFile(File settingsFile) {
		try {
			settingsFile.createNewFile();
		} catch (IOException e) {
			reportError(
					"The profile server file could not be found or created.\nIt may not be possible to save profile servers.",
					"Writing error");
		}
	}

	/**
	 * Save server profiles to a pre-specified file.
	 */
	private void saveSettings() {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(SETTINGS_FILENAME)));
			out.println(nickName);
			out.println(portNumber);
			for (NameServerProfile server : servers) {
				out.println(server.toLine());
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			reportError("The server profile file could not be saved.",
					"Writing error");
		}
	}

	/**
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source instanceof JButton) {
			JButton button = (JButton) source;
			if (button == onlineButton) {
				if (server == null)
					setupServer();
				else
					shutDownServer();
			} else if (button == connectButton) {
				if (isConnected())
					disconnect();
				else
					connect();
			}
		} else if (source instanceof MenuItem) {
			MenuItem item = (MenuItem) source;
			if (item == quitMenuItem) {
				saveAndExit();
			} else if (item == editServerMenuItem) {
				editServer();
			} else if (item == addServerMenuItem) {
				addServer();
			} else if (item == deleteServerMenuItem) {
				deleteServer();
			} else if (item == editSettingsMenuItem) {
				if (isConnected())
					reportError("Cannot edit settings while connected.",
							"Connected");
				else
					openSettingsDialog();
			}
		}
	}

	/**
	 * Disconnect from the name server.
	 */
	private void disconnect() {
		try {
			ByteWriter.write(serverOutput, "/quit");
			// give some time to receive the message.
			Thread.sleep(400);
			disconnectCleanup();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
		}
	}

	/**
	 * This method is called if the name server connection has been lost.
	 */
	public void disconnected() {
		reportError("Connection with server lost", "Connection lost");
		disconnectCleanup();
	}

	/**
	 * Cleanup after disconnecting.
	 */
	public void disconnectCleanup() {
		connectButton.setText("Connect");
		serverComboBox.setEnabled(true);
		DefaultListModel model = (DefaultListModel) userList.getModel();
		model.clear();
		keepAlive.kill();
		if (serverConnection != null) {
			try {
				serverConnection.close();
			} catch (IOException e) {
			}
			serverConnection = null;
		}
	}

	/**
	 * Tells whether there is a connection with a name server open.
	 * 
	 * @return true if there is a connection to a name server, false otherwise.
	 */
	public boolean isConnected() {
		return serverConnection != null && serverConnection.isConnected();
	}

	/**
	 * Save server profiles and close the application.
	 */
	private void saveAndExit() {
		if (isConnected())
			disconnect();
		saveSettings();
		System.exit(0);
	}

	/**
	 * Open a window in which the user can edit the server currently selected in
	 * the ComboBox.
	 */
	private void editServer() {
		int index = serverComboBox.getSelectedIndex();
		if (index == -1) {
			reportError("There is no server to edit.", "No server");
		}
		new ServerProfileDialog(this, servers, index);
		refreshServerComboBox();
		serverComboBox.setSelectedIndex(index);
	}

	/**
	 * Open a window from which the user can add a new server profile.
	 */
	private void addServer() {
		new ServerProfileDialog(this, servers);
		refreshServerComboBox();
		serverComboBox.setSelectedIndex(servers.size() - 1);
	}

	/**
	 * Refresh the profile server combo box with a newly edited/added profile.
	 */
	private void refreshServerComboBox() {
		serverComboBox.setModel(new DefaultComboBoxModel(servers));
	}

	/**
	 * Delete the currently selected server profile.
	 */
	private void deleteServer() {
		Object selected = serverComboBox.getSelectedItem();
		if (selected == null) {
			reportError("There is no server to delete.", "No server");
		}
		NameServerProfile server = (NameServerProfile) selected;
		String profileName = server.getProfileName();
		int option = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to delete the " + profileName
						+ " profile?", "Delete?", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (option == JOptionPane.YES_OPTION) {
			servers.remove(serverComboBox.getSelectedIndex());
			refreshServerComboBox();
		}
	}

	/**
	 * Connects with the currently selected name server.
	 */
	private void connect() {
		if (nickName == null || nickName.equals("") || nickName.equals("null")) {
			openSettingsDialog();
			return;
		}

		Object selected = serverComboBox.getSelectedItem();
		if (selected == null) {
			reportError(
					"There is no server to send the message to.\nPlease create a server profile and try again.",
					"No server");
		}
		NameServerProfile server = (NameServerProfile) selected;
		try {
			connect(server);
			connectButton.setText("Disconnect");
			serverComboBox.setEnabled(false);
		} catch (UnknownHostException e) {
			reportError("Could not connect to server.\n"
					+ "Make sure the hostname and port is correct.",
					"Invalid server");
		} catch (IOException e) {
			reportError("Could not connect to server. Please try again later.",
					"Server not responding");
		}
		setStatus("Connected");
	}

	/**
	 * Connects with a name server of a given profile.
	 * 
	 * @param server
	 *            A NameServerProfile.
	 * @throws UnknownHostException
	 *             If the hostname is not found after DNS-lookup.
	 * @throws SocketException
	 *             If the connection is reset.
	 * @throws IOException
	 *             If any other IOError occurs.
	 */
	public void connect(NameServerProfile server) throws UnknownHostException,
			IOException, SocketException {
		serverConnection = new Socket(server.getHostname(), server.getPort());
		serverConnection.setSoTimeout(SO_TIMEOUT);
		serverOutput = serverConnection.getOutputStream();
		// Write nickname and port to server.
		ByteWriter.write(serverOutput, nickName + " " + portNumber);
		serverInput = serverConnection.getInputStream();
		keepAlive = new KeepAlive(this, serverOutput);
		keepAlive.start();
		Scanner scanner = new Scanner(serverInput);
		if (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("ERROR")) {
				setStatus(line.substring(6));
				disconnectCleanup();
				return;
			}
			String[] userListStrings = line.split("&");
			DefaultListModel model = (DefaultListModel) userList.getModel();
			for (Peer peer : Peer.fromStrings(userListStrings)) {
				model.addElement(peer);
			}
		}
		new UserListUpdater(serverInput, userList, this).start();
	}

	/**
	 * If the server is not created due to the port already being taken.
	 */
	public void serverNotCreated() {
		reportError("Port already taken by another service.", "Port taken.");
		openSettingsDialog();
		onlineButton.setText("Go online");
	}

	/**
	 * Set the message in the status field.
	 * @param status A message.
	 */
	public void setStatus(String status) {
		statusField.setText(status);
	}

	/**
	 * Report some information to the user in a pop-up window.
	 * 
	 * @param message
	 *            The message to be displayed.
	 * @param title
	 *            The title of the window.
	 */
	public void report(String message, String title) {
		Popup.report(this, message, title);
	}

	/**
	 * Report an error to the user in a pop-up window.
	 * 
	 * @param message
	 *            The error-message to be displayed.
	 * @param title
	 *            The title of the window.
	 */
	public void reportError(String message, String title) {
		Popup.reportError(this, message, title);
	}

	/**
	 * Runs the program.
	 * 
	 * @param args
	 *            Array of strings. Unused.
	 */
	public static void main(String[] args) {
		new GUI();
	}
}
