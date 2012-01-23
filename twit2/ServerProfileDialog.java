package twit2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import twit2.awt.HorizontalPanel;
import twit2.awt.Popup;

/**
 * Dialog used to edit existing/create new name server profiles for the peer to
 * peer client.
 * 
 * @author Ole
 */
public class ServerProfileDialog extends JDialog implements ActionListener,
		KeyListener {
	/**
	 * Unused
	 */
	private static final long serialVersionUID = 1L;

	private static final int HEIGHT = 168;
	private static final int WIDTH = 300;
	private static final String ADD_TITLE = "Add new server";
	private static final String EDIT_TITLE = "Edit server";

	private static final Dimension LINE_FILLER_DIM = new Dimension(10000, 0);
	private static final int TEXT_FIELD_LENGTH = 18;

	private JButton okButton;
	private JButton cancelButton;

	private List<NameServerProfile> servers;
	private int serverIndex;
	private JTextField profileField;
	private JTextField hostField;
	private JTextField portField;

	/**
	 * Create a new ServerDialog from which a new server profile can be created.
	 * 
	 * @param owner
	 *            The owner frame.
	 * @param servers
	 *            The server profiles.
	 */
	public ServerProfileDialog(Frame owner, Vector<NameServerProfile> servers) {
		this(owner, servers, -1);
	}

	/**
	 * Create a new ServerDialog from which a server profile can be edited or
	 * created.
	 * 
	 * @param owner
	 *            The owner frame.
	 * @param servers
	 *            A Vector of server profiles.
	 */
	public ServerProfileDialog(Frame owner, Vector<NameServerProfile> servers,
			int serverIndex) {
		/*
		 * If no server index was selected (i.e. -1), add new server, otherwise
		 * edit.
		 */
		super(owner, (serverIndex == -1) ? ADD_TITLE : EDIT_TITLE, true);

		this.servers = servers;
		this.serverIndex = serverIndex;

		makeGUI();
		// make the dialog appear in the middle of owner window.
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	private void makeGUI() {
		JPanel profilePane = initProfilePane();
		JPanel hostPane = initHostPane();
		JPanel portPane = initPortPane();
		JPanel buttonPane = initButtons();

		JPanel panel = new JPanel(new BorderLayout());
		panel.setLayout(new FlowLayout(FlowLayout.LEADING));
		panel.add(profilePane);
		panel.add(createLineFiller());
		panel.add(hostPane);
		panel.add(createLineFiller());
		panel.add(portPane);
		panel.add(createLineFiller());
		panel.add(buttonPane, BorderLayout.CENTER);
		setContentPane(panel);

		JComponent[] components = { profileField, hostField, portField };
		for (JComponent comp : components)
			comp.addKeyListener(this);

		setSize(WIDTH, HEIGHT);
		setResizable(false);
	}

	/**
	 * Create a filler component of a given width and height.
	 * 
	 * @param width
	 *            An integer.
	 * @param height
	 *            An integer.
	 * @return A Box.Filler object with the given dimensions.
	 */
	public Box.Filler createFiller(int width, int height) {
		Dimension dim = new Dimension(width, height);
		return new Box.Filler(dim, dim, dim);
	}

	/**
	 * Creates a filler object which forces a line-break.
	 * 
	 * @return A filler object.
	 */
	public Box.Filler createLineFiller() {
		return new Box.Filler(LINE_FILLER_DIM, LINE_FILLER_DIM, LINE_FILLER_DIM);
	}

	private JPanel initProfilePane() {
		JPanel profilePane = new HorizontalPanel();
		JLabel profileLabel = new JLabel("Profile name:");
		profileField = new JTextField(TEXT_FIELD_LENGTH);
		if (serverIndex >= 0) {
			String profileName = servers.get(serverIndex).getProfileName();
			profileField.setText(profileName);
		}
		profilePane.add(profileLabel);
		profilePane.add(profileField);
		return profilePane;
	}

	private JPanel initHostPane() {
		JPanel hostPane = new HorizontalPanel();
		JLabel hostLabel = new JLabel("Hostname:");
		hostField = new JTextField(TEXT_FIELD_LENGTH);
		if (serverIndex >= 0) {
			String hostName = servers.get(serverIndex).getHostname();
			hostField.setText(hostName);
		}
		hostPane.add(hostLabel);
		hostPane.add(createFiller(14, 0));
		hostPane.add(hostField);
		return hostPane;
	}

	private JPanel initPortPane() {
		JPanel portPane = new HorizontalPanel();
		portField = new JTextField(TEXT_FIELD_LENGTH);
		if (serverIndex >= 0) {
			String port = Integer.toString(servers.get(serverIndex).getPort());
			portField.setText(port);
		}
		JLabel portLabel = new JLabel("Port:");
		portPane.add(portLabel);
		portPane.add(createFiller(48, 0));
		portPane.add(portField);
		return portPane;
	}

	private JPanel initButtons() {
		JPanel buttonPane = new JPanel();
		okButton = new JButton("Ok");
		cancelButton = new JButton("Cancel");
		JButton[] buttons = { okButton, cancelButton };
		buttonPane.add(createFiller(75, 0));
		for (JButton b : buttons) {
			b.addActionListener(this);
			buttonPane.add(b);
		}
		return buttonPane;
	}

	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source instanceof JButton) {
			JButton button = (JButton) source;
			if (button == okButton) {
				addServerAndClose();
			} else if (button == cancelButton) {
				dispose();
			}
		}
	}

	/**
	 * Edit/add server to the server profiles and close the window.
	 */
	private void addServerAndClose() {
		String profileName = profileField.getText();
		if (!Validator.isValidProfileName(profileName)) {
			reportInvalidProfileName();
			return;
		}
		String hostname = hostField.getText();
		if (!Validator.isValidHostname(hostname)) {
			reportInvalidHostname();
			return;
		}
		String portString = portField.getText();
		int port;
		try {
			port = Integer.parseInt(portString);
			if (!Validator.isValidPortNumber(port))
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			reportInvalidPortNumber();
			return;
		}
		if (serverIndex == -1) {
			NameServerProfile server = new NameServerProfile(profileName,
					hostname, port);
			servers.add(server);
		} else {
			NameServerProfile server = servers.get(serverIndex);
			server.setProfileName(profileName);
			server.setHostname(hostname);
			server.setPort(port);
		}
		dispose();
	}

	private void reportInvalidHostname() {
		Popup.reportError(this,
				"The hostname can only contain characters, numbers and dashes (-)\n"
						+ "but cannot start with a dash.", "Invalid hostname");
	}

	private void reportInvalidProfileName() {
		Popup.reportError(this, "The profile name cannot contain semi-colons.",
				"Invalid profile name");
	}

	private void reportInvalidPortNumber() {
		Popup.reportError(this,
				"Please enter a valid port number (32768-61000 inclusive).",
				"Invalid port");
	}

	/**
	 * Determines what will happen if specific keys are pressed on a keyboard or
	 * similar device. If enter is pressed: click the okButton. If Escape is
	 * pressed, click the cancelButton.
	 */
	public void keyPressed(KeyEvent evt) {
		int keyCode = evt.getKeyCode();
		if (keyCode == KeyEvent.VK_ENTER) {
			okButton.doClick();
		} else if (keyCode == KeyEvent.VK_ESCAPE) {
			cancelButton.doClick();
		}
	}

	/**
	 * Unused.
	 */
	public void keyReleased(KeyEvent evt) {

	}

	/**
	 * Unused.
	 */
	public void keyTyped(KeyEvent evt) {

	}
}
