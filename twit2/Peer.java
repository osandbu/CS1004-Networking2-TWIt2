package twit2;

/**
 * The Peer class is a wrapper class used to store information about a peer's
 * nickname, hostname and port number.
 * 
 * @author os75
 */
public class Peer {
	private String nickname;
	private String hostname;
	private int port;

	public Peer(String nickname, String hostname, int port) {
		this.nickname = nickname;
		this.hostname = hostname;
		this.port = port;
	}

	public String getNickname() {
		return nickname;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public String toString() {
		return nickname;
	}

	public static Peer fromString(String str) {
		String[] split = str.split(";");
		if (split.length < 3) {
			System.out.println("fromStringError: " + str);
			return null;
		}
		return new Peer(split[0], split[1], Integer.parseInt(split[2]));
	}

	public static Peer[] fromStrings(String[] str) {
		Peer[] peers = new Peer[str.length];
		for (int i = 0; i < str.length; i++) {
			peers[i] = fromString(str[i]);
		}
		return peers;
	}
}
