package twit2.awt;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * The Popup class is used to create pop-up windows which display information
 * and error messages.
 * 
 * @author Ole
 */
public class Popup {
	/**
	 * Display a JOptionPane message, with a given title, message and title.
	 * 
	 * @see JOptionPane
	 * @see JOptionPane#showConfirmDialog(Component, Object, String, int)
	 * @param owner
	 *            determines the Frame in which the dialog is displayed; if
	 *            null, or if the parentComponent has no Frame, a default Frame
	 *            is used.
	 * @param message
	 *            The message to be displayed.
	 * @param title
	 *            The title of the dialog.
	 */
	public static void report(Component owner, String message, String title) {
		JOptionPane.showConfirmDialog(owner, message, title,
				JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * Display a JOptionPane error message, with a given title, message and
	 * title.
	 * 
	 * @see JOptionPane
	 * @see JOptionPane#showConfirmDialog(Component, Object, String, int, int)
	 * @param owner
	 *            determines the Frame in which the dialog is displayed; if
	 *            null, or if the parentComponent has no Frame, a default Frame
	 *            is used.
	 * @param message
	 *            The message to be displayed.
	 * @param title
	 *            The title of the dialog.
	 */
	public static void reportError(Component owner, String message, String title) {
		JOptionPane.showConfirmDialog(owner, message, title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
	}
}
