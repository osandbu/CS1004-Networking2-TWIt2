package twit2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator used to validate the fields in the ServerProfileDialog.
 * 
 * @author os75
 */
public class Validator {

	private static final int DOMAIN_NAME_MAX_LENGTH = 253;
	private static final int MIN_PORT_NUMBER = 32768;
	private static final int MAX_PORT_NUMBER = 61000;
	private static final String HOSTNAME_REGEX = "\\A[A-Za-z0-9]{1}[A-Za-z0-9-.]*[A-Za-z0-9]{1}\\z|\\A[A-Za-z0-9]{1}\\z";
	private static final Pattern HOSTNAME_PATTERN = Pattern
			.compile(HOSTNAME_REGEX);
	private static final Matcher hostnameMatcher = HOSTNAME_PATTERN.matcher("");

	// Nicknames need to contain 1-16 word characters: [a-zA-Z_0-9].
	private static final String NICKNAME_REGEX = "\\w{1,16}";
	private static final Pattern NICKNAME_PATTERN = Pattern
			.compile(NICKNAME_REGEX);
	private static final Matcher nicknameMatcher = NICKNAME_PATTERN.matcher("");

	/**
	 * Tells whether or not a given integer is in the range of valid port
	 * numbers.
	 * 
	 * @param port
	 *            A port number.
	 * @return Returns true if the given integer falls in the range 32768-61000
	 *         inclusive.
	 */
	public static boolean isValidPortNumber(int port) {
		return port >= MIN_PORT_NUMBER && port <= MAX_PORT_NUMBER;
	}

	/**
	 * Returns true if the given host-name is valid, false otherwise. A valid
	 * host-name consists of up to 127 levels, each containing up to 63
	 * characters. The allowed characters are a-z, 0-9, period (.) and dash (-).
	 * However, dash and period cannot be at the beginning or end of any of the
	 * parts of the domain name. The full domain name may not exceed 253
	 * characters.
	 * 
	 * Source: http://en.wikipedia.org/wiki/Domain_name#Parts_of_a_domain_name
	 * 
	 * @param hostname
	 *            A hostname String.
	 * @return Whether or not the hostname is valid.
	 */
	public static boolean isValidHostname(String hostname) {
		if (hostname.length() == 0
				|| hostname.length() > DOMAIN_NAME_MAX_LENGTH)
			return false;
		hostnameMatcher.reset(hostname);
		return hostnameMatcher.matches();
	}

	/**
	 * Returns true if the given profile name is valid, false otherwise. It is
	 * in valid if the string is empty or contains a semi-colon.
	 * 
	 * @param profileName
	 *            A profile name String.
	 * @return Whether or not the profile name is valid.
	 */
	public static boolean isValidProfileName(String profileName) {
		return profileName.length() > 0 && !profileName.contains(";");
	}

	public static boolean isValidNickname(String nickname) {
		return nicknameMatcher.reset(nickname).matches();
	}
}
