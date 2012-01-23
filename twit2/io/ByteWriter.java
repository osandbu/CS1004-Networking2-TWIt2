package twit2.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The ByteWriter class writes a String to an OutputStream.
 * 
 * @author Ole
 */
public class ByteWriter {

	/**
	 * Writes a String to an OutputStream.
	 * 
	 * @param out
	 *            An OutputStream.
	 * @param str
	 *            A String.
	 * @throws IOException
	 *             If there is a problem writing to the OutputStream. For
	 *             example if it has already been closed.
	 */
	public static void write(OutputStream out, String str) throws IOException {
		write(out, str.getBytes());
	}

	/**
	 * Writes an array of bytes to an OutputStream.
	 * 
	 * @param out
	 *            An OutputStream.
	 * @param bytes
	 *            The bytes to be written.
	 * @throws IOException
	 *             If there is a problem writing to the OutputStream. For
	 *             example if it has already been closed.
	 */
	private static void write(OutputStream out, byte[] bytes)
			throws IOException {
		write(out, bytes, bytes.length);
	}

	/**
	 * Writes length bytes from an array of bytes to an OutputStream.
	 * 
	 * @param out
	 *            An OutputStream.
	 * @param bytes
	 *            The bytes to be written.
	 * @param length
	 * @throws IOException
	 *             If there is a problem writing to the OutputStream. For
	 *             example if it has already been closed.
	 */
	public static void write(OutputStream out, byte[] bytes, int length)
			throws IOException {
		out.write(bytes, 0, length);
		out.flush();
	}

}
