package twit2;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import twit2.io.ByteReader;

/**
 * The UserListUpdater class keeps the list of users in the peer to peer client
 * up to date by maintaining a connection with the name server.
 * 
 * @author os75
 */
public class UserListUpdater extends Thread {
	private InputStream input;
	private DefaultListModel listModel;
	private GUI gui;

	public UserListUpdater(InputStream serverInput, JList userList, GUI gui) {
		this.input = serverInput;
		listModel = (DefaultListModel) userList.getModel();
		this.gui = gui;
	}

	public void run() {
		while (gui.isConnected()) {
			try {
				int available = input.available();
				if (available > 0) {
					String str = ByteReader.read(input, available);
					String message = str.substring(4);
					if (str.startsWith("ONL")) {
						Peer peer = Peer.fromString(message);
						listModel.addElement(peer);
					} else if (str.startsWith("OFL")) {
						for (int i = 0; i < listModel.size(); i++) {
							Peer peer = (Peer) listModel.get(i);
							if (peer.getNickname().equals(message)) {
								listModel.remove(i);
								break;
							}
						}
					}
				} else {
					// if nothing available, sleep
					Thread.sleep(100);
				}
			} catch (IOException e) {
				return;
			} catch (InterruptedException e) {
			}
		}
	}
}
