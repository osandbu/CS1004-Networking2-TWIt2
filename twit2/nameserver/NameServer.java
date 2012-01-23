package twit2.nameserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * The name server provides peer-to-peer clients with the nickname, hostname and
 * port number of all connected peers. It also maintains this list as peers
 * connect and disconnect.
 * 
 * @author os75
 */
public class NameServer extends Thread {
	private static final int SO_TIMEOUT = 15000;
	private ServerSocket socket;
	private boolean running;
	private HashMap<String, InetSocketAddress> table;
	private Vector<ClientHandler> clients;

	/**
	 * Create a new name server with operating on a given port number.
	 * 
	 * @param port
	 *            A port number.
	 * @throws IOException
	 *             If an error occurs while setting up the server.
	 */
	public NameServer(int port) throws IOException {
		System.out.println("Setting up server...");
		table = new HashMap<String, InetSocketAddress>();
		clients = new Vector<ClientHandler>();
		socket = new ServerSocket(port);
		socket.setSoTimeout(SO_TIMEOUT);
		System.out.println("Name server set up at port " + port);
	}

	/**
	 * Accepts new peers (clients).
	 */
	public void run() {
		running = true;
		while (running) {
			try {
				Socket client = socket.accept();
				System.out.println("Accepted connection from "
						+ client.getInetAddress().getHostName());
				ClientHandler handler = new ClientHandler(client, this);
				handler.start();
				clients.add(handler);
			} catch (SocketTimeoutException e) {
				// ignore timeouts
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Shut down the name server.
	 */
	public void kill() {
		running = false;
	}

	/**
	 * Add entry to list of peers connected.
	 * 
	 * @param nickname
	 *            A nickname.
	 * @param hostname
	 *            A hostname.
	 * @param port
	 *            A port number.
	 * @return true if the nickname does not already exist, false otherwise.
	 */
	public boolean addEntry(String nickname, String hostname, int port) {
		InetSocketAddress address = new InetSocketAddress(hostname, port);
		if (table.containsKey(nickname)) {
			return false;
		} else {
			table.put(nickname, address);
			sendToAllExceptMostRecent("ONL " + nickname + ";" + hostname + ";"
					+ port);
			return true;
		}
	}

	/**
	 * Remove the peer with a given nickname from the list of connected peers.
	 * 
	 * @param name
	 *            A nickname of a peer.
	 */
	public void removeEntry(String name) {
		if (name != null && table.containsKey(name)) {
			table.remove(name);
			sendToAll("OFL " + name);
		}
	}

	/**
	 * Send a message to all peers connected to the name server.
	 * 
	 * @param message
	 *            A message.
	 */
	private void sendToAll(String message) {
		System.out.println("Sending to all: " + message);
		for (int i = 0; i < clients.size(); i++) {
			ClientHandler client = clients.get(i);
			if (client.isClosed()) {
				clients.remove(i);
				i--;
			} else {
				sendMessage(message, client);
			}
		}
	}

	/**
	 * Send a message to all peers connected to the name server, with one
	 * exception.
	 * 
	 * @param message
	 *            A message.
	 * @param exception
	 *            The name of the user who this message should not be sent to.
	 */
	private void sendToAllExceptMostRecent(String message) {
		System.out.println("Sending to all: " + message);
		for (int i = 0; i < clients.size() - 1; i++) {
			ClientHandler client = clients.get(i);
			if (client.isClosed()) {
				clients.remove(i);
				i--;
			} else {
				sendMessage(message, client);
			}
		}
	}

	private void sendMessage(String message, ClientHandler client) {
		try {
			client.sendMessage(message);
		} catch (IOException e) {
			System.out.println("IOException: the message could not be sent to "
					+ client.getNickname());
		}
	}

	/**
	 * Get the list of users currently connected to the name server.
	 * 
	 * @return A string representation of the peers connected to the name
	 *         server.
	 */
	public String getUserList() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, InetSocketAddress> entry : table.entrySet()) {
			sb.append(entry.getKey());
			sb.append(';');
			InetSocketAddress address = entry.getValue();
			sb.append(address.getHostName());
			sb.append(';');
			sb.append(address.getPort());
			sb.append('&');
		}
		return sb.toString();
	}

}
