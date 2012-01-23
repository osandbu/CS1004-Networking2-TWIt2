package twit2.awt;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A JPanel which arranges components horizontally.
 * 
 * @author Ole
 */
@SuppressWarnings("serial")
public class HorizontalPanel extends JPanel {
	/**
	 * Create an empty HorizontalPanel.
	 */
	public HorizontalPanel() {
		super();
		BoxLayout bl = new BoxLayout(this, BoxLayout.X_AXIS);
		setLayout(bl);
	}
}
