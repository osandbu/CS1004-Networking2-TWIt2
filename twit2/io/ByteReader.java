package twit2.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * The ByteReader class reads bytes from an InputStream.
 * 
 * @author Ole
 */
public class ByteReader {

	/**
	 * Reads maxBytes bytes from an InputStream and returns a string.
	 * 
	 * @param inputStream
	 *            The InputStream to be read.
	 * @param maxBytes
	 *            The maximum number of bytes to read from the InputStream.
	 * @return A string representation of the information read from the
	 *         InputStream.
	 * @throws IOException
	 *             If there is a problem reading the information.
	 */
	public static String read(InputStream inputStream, int maxBytes)
			throws IOException {
		byte[] receivedBytes = new byte[maxBytes];
		// Read up to maxBytes bytes from inputStream.
		int length = inputStream.read(receivedBytes);
		// length is the number of bytes read.
		// Return a String containing the received message.
		return new String(receivedBytes, 0, length);
	}
}
