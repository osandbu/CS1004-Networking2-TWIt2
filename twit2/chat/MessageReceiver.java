package twit2.chat;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JTextArea;

import twit2.KeepAlive;
import twit2.io.ByteReader;

/**
 * Thread for receiving messages. Received messages are appended to a JTextArea.
 * 
 * @author os75
 */
public class MessageReceiver extends Thread {

	private static final String NEW_LINE = "\n";
	private InputStream input;
	private JTextArea textArea;
	private String otherNickname;

	/**
	 * Create a new MessageReceiver with a given inputStream and textarea.
	 * 
	 * @param input
	 *            An inputstream.
	 * @param textArea
	 *            A JTextArea.
	 * @param nickname
	 *            The nickname of a person a peer is chatting with.
	 */
	public MessageReceiver(InputStream input, JTextArea textArea,
			String nickname) {
		this.input = input;
		this.textArea = textArea;
		this.otherNickname = nickname;
	}

	/**
	 * Receive messages from the inputStream and append them to the text area.
	 */
	public void run() {
		while (true) {
			try {
				int available = input.available();
				if (available > 0) {
					String message = ByteReader.read(input, available);
					if (message.equals("/quit")) {
						textArea.append(otherNickname + " left the chat.");
						input.close();
						break;
					} else if (!message.equals(KeepAlive.KEEP_ALIVE)) {
						textArea.append(otherNickname + ": ");
						textArea.append(message);
						textArea.append(NEW_LINE);
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
