package twit2;

/**
 * Wrapper used to store information about server profiles. This information
 * includes profile name, hostname and port.
 * 
 * @author Ole
 */
public class NameServerProfile {
	private String profileName;
	private String hostname;
	private int port;

	/**
	 * Create a new FavoriteServer with a profile name, hostname and port
	 * number.
	 * 
	 * @param profileName
	 *            A profile name.
	 * @param hostname
	 *            A hostname.
	 * @param port
	 *            A port number.
	 */
	public NameServerProfile(String profileName, String hostname, int port) {
		this.profileName = profileName;
		this.hostname = hostname;
		this.port = port;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns the profile name of this FavoriteServer. This is used when
	 * initializing the ComboBoxModel in the client.
	 */
	public String toString() {
		return profileName;
	}

	/**
	 * Represent this server in one string.
	 * 
	 * @return A one line representation.
	 */
	public String toLine() {
		StringBuilder sb = new StringBuilder();
		sb.append(profileName);
		sb.append(';');
		sb.append(hostname);
		sb.append(';');
		sb.append(port);
		return sb.toString();
	}

	/**
	 * Create an object from a line.
	 * 
	 * @param line
	 *            A one line representation of the server. Should contain the
	 *            following (without quotation marks):
	 *            "profileName(String);hostname(String);portNumber(int)".
	 *            Example: "os75;host-os75.cs.st-andrews.ac.uk;60514"
	 * @return A FavoriteServer object or null if the input string is
	 *         incorrectly formatted.
	 */
	public static NameServerProfile fromLine(String line) {
		String[] split = line.split(";");
		if (split.length != 3)
			return null;
		String profileName = split[0];
		String hostname = split[1];
		int port;
		try {
			port = Integer.parseInt(split[2]);
		} catch (NumberFormatException e) {
			return null;
		}
		return new NameServerProfile(profileName, hostname, port);
	}
}
