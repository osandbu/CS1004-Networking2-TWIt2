package twit2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import twit2.chat.ChatWindow;

/**
 * The Server class represents the server-side of the peer, it makes it possible
 * for the peers to create direct connection to other peers, without having the
 * messages go through a centralised server.
 * 
 * @author os75
 */
public final class Server extends Thread {

	private static final int SO_TIMEOUT = 15000;
	private ServerSocket serverSocket;
	private boolean running;
	private String nickname;
	private GUI gui;

	/**
	 * Set up server at PORT_NUMBER.
	 * 
	 * @param gui
	 * @throws SocketException
	 *             , IOException
	 * @throws IOException
	 *             if there is a problem establishing a server socket at the
	 *             specified port number.
	 */
	public Server(GUI gui, String nickname, int portNumber)
			throws SocketException, IOException {
		this.gui = gui;
		this.nickname = nickname;
		serverSocket = new ServerSocket(portNumber);
		serverSocket.setSoTimeout(SO_TIMEOUT);
		gui.setStatus("Server socket established at port " + portNumber + ".");
	}

	/**
	 * Accept connections from serverSocket.
	 */
	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				Socket client = serverSocket.accept();
				String address = client.getInetAddress().getHostName();
				int port = client.getPort();
				// Can be printed to the console as it is not required for the
				// user to know. a window will pop up anyway.
				System.out.print("Connection established with ");
				System.out.println(address + ":" + port + ".");
				new ChatWindow(nickname, client);
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				gui.reportError(e.getMessage(), "IOError");
			}
		}
	}

	public void shutDown() {
		gui.setStatus("Shutting down local chat server.");
		running = false;
	}
}
