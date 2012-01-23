package twit2;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Keeps a connection with a server alive. Needed to avoid the connection timing
 * out if there lacks activity.
 * 
 * @author os75
 */
public class KeepAlive extends Thread {
	private static final int SLEEP_TIME = 3000;
	public static final String KEEP_ALIVE = "KEEPALIVE";
	private static byte[] KEEP_ALIVE_BYTES = KEEP_ALIVE.getBytes();

	private OutputStream outputStream;
	private GUI client;
	private boolean alive;

	public KeepAlive(GUI gui, OutputStream outputStream) {
		this.client = gui;
		this.outputStream = outputStream;
		alive = true;
	}

	/**
	 * Send a message every 10 seconds to keep the connection alive.
	 */
	public void run() {
		try {
			// TODO
			Thread.sleep(SLEEP_TIME);
			while (alive) {
				outputStream.write(KEEP_ALIVE_BYTES);
				Thread.sleep(3000);
			}
		} catch (IOException e) {
			if (alive)
				client.disconnected();
			return;
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public void kill() {
		alive = false;
	}
}
