package twit2.nameserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import twit2.io.ByteReader;
import twit2.io.ByteWriter;
import static twit2.io.ByteWriter.write;

/**
 * The ClientHandler class deals with information coming from a client of the
 * name server.
 * 
 * @author os75
 */
public class ClientHandler extends Thread {
	private static final int MAX_NAME_LENGTH = 40;
	private static final int MAX_PORT_LENGTH = 5;
	private static final int MAX_PORT_NAME_LENGTH = MAX_NAME_LENGTH + 1
			+ MAX_PORT_LENGTH;
	private static final int MAX_MESSAGE_LENGTH = 140;
	private Socket client;
	private NameServer server;
	private boolean alive;
	private InputStream in;
	private OutputStream out;
	private String nickName;

	public ClientHandler(Socket client, NameServer server) {
		this.client = client;
		this.server = server;
		alive = true;
	}

	/**
	 * Receive messages from a Peer.
	 */
	public void run() {
		try {
			in = client.getInputStream();
			out = client.getOutputStream();
			receiveInitialization();
			while (alive) {
				String message = ByteReader.read(in, MAX_MESSAGE_LENGTH + 1);
				if (message.length() <= MAX_MESSAGE_LENGTH) {
					if (message.equalsIgnoreCase("/quit")) {
						System.out.println("Received quit command from " + nickName);
						closeConnection();
					}
				} else {
					sendMessage("Message received too long. Max is 140 characters.");
				}
			}
		} catch (SocketTimeoutException e) {
			alive = false;
		} catch (SocketException e) {
			System.out.println("Connection with " + nickName + " reset.");
			closeConnection();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			closeConnection();
			return;
		} catch (NumberFormatException e) {
		} catch (NameServerException e) {
			try {
				write(out, "ERROR " + e.getMessage());
			} catch (IOException ioe) {
			}
			System.out.println(e.getMessage());
			closeConnection();
			return;
		} finally {
			closeConnection();
		}
	}

	/**
	 * Receive the nickname and port number of a peer wishes to use.
	 * 
	 * @throws IOException
	 *             If a reading error occurs.
	 * @throws NameServerException
	 */
	private void receiveInitialization() throws IOException,
			NameServerException {
		String input = ByteReader.read(in, MAX_PORT_NAME_LENGTH + 1);
		if (input.length() > MAX_NAME_LENGTH) {
			// Message too long, may not be receiving the full message
			throw new NameServerException(
					"Nickname or portnumber too long, please try again.");
		} else {
			String address = client.getInetAddress().getHostAddress();
			String[] split = input.split(" ");
			if (split.length >= 2) {
				String nickName = split[0];
				int port = Integer.parseInt(split[1]);
				boolean entryAdded = server.addEntry(nickName, address, port);
				if (entryAdded) {
					this.nickName = nickName;
					sendUserList();
				} else {
					throw new NameServerException(
							"Nickname already taken, please try a different one.");
				}
			} else {
				throw new NameServerException(
						"Expected nickname and port number.");
			}
		}
	}

	/**
	 * Close the connection with a peer.
	 * 
	 * @throws IOException
	 *             If there is a problem with closing the connection.
	 */
	private void closeConnection() {
		try {
			client.close();
		} catch (IOException e) {
		}
		alive = false;
		server.removeEntry(nickName);
	}

	/**
	 * Send a list of all the connected peers to the peer.
	 * 
	 * @throws IOException
	 *             If there is an error writing information to the
	 *             output-stream.
	 */
	private void sendUserList() throws IOException {
		String users = server.getUserList();
		write(out, users);
	}

	/**
	 * Tells if the connection has been closed.
	 * 
	 * @return true if the connection has been closed, false otherwise.
	 */
	public boolean isClosed() {
		return !alive;
	}

	/**
	 * Send a message to the peer.
	 * 
	 * @param message
	 *            A message.
	 * @throws IOException
	 *             If an error occurs while writing to the peer.
	 */
	public void sendMessage(String message) throws IOException {
		ByteWriter.write(out, message);
	}

	public String getNickname() {
		if(nickName == null)
			return "null";
		return nickName;
	}
}
