package twit2.awt;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A JPanel which arranges components vertically.
 * 
 * @see JPanel
 * @author os75
 */
@SuppressWarnings("serial")
public class VerticalPanel extends JPanel {
	/**
	 * Create an empty VerticalPanel.
	 */
	public VerticalPanel() {
		super();
		BoxLayout bl = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(bl);
	}
}
