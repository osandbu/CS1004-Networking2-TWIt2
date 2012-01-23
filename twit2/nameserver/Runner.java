package twit2.nameserver;

import java.io.IOException;
import java.util.Scanner;

/**
 * The Runner class runs the name-server.
 * 
 * @author os75
 */
public class Runner {
	/**
	 * Runs the name server.
	 * 
	 * @param args
	 *            If provided, uses the first argument as the port number at
	 *            which the name server is run.
	 */
	public static void main(String[] args) {
		int port;
		if (args.length == 0)
			port = 60514;
		else
			port = Integer.parseInt(args[0]);
		try {
			NameServer server = new NameServer(port);
			server.start();
			System.out.println("Enter QUIT to shut down the server.");
			Scanner in = new Scanner(System.in);
			while (in.hasNext()) {
				if (in.next().equalsIgnoreCase("quit")) {
					System.out.println("Server shutting down.");
					System.exit(0);
				}
			}

		} catch (IOException e) {
			System.out.println("An IO error occured.");
		}
	}
}
