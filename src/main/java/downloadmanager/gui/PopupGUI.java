package downloadmanager.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import downloadmanager.constants.Constants;

/**
 * Prompts a new frame where the user is asked something and is offered a "yes"
 * or "no" option.
 *
 */
public abstract class PopupGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	/** Name of the file to be downloaded. */
	protected String name;
	/** Size of the file on disk. */
	protected long sourceFileSize;
	/** Size of the file on server. */
	protected long targetFileSize;
	/** The panel where all the components are added. */
	protected JPanel panel;
	/** The label where the question is set by the subclasses. */
	protected JLabel textLabel;
	/** Button pressed by user if he answers "yes". */
	protected JButton yesButton;
	/** Button pressed by user if he answers "no". */
	protected JButton noButton;
	/** Button pressed by user if he answers "yes to all". */
	protected JButton yesToAllButton;
	/** Button pressed by user if he answers "no to all". */
	protected JButton noToAllButton;
	/** Used for checking if the user has pressed any buttons */
	protected boolean pressed;
	/** An int representing the button pressed by the user.	 */
	protected int whatButtonWasPressed;

	public PopupGUI(String name, long sourceFileSize, long targetFileSize, String title) {
		super(title);
		this.name = name;
		this.sourceFileSize = sourceFileSize;
		this.targetFileSize = targetFileSize;

		pressed = false;
		this.setResizable(false);
		this.setSize(420, 250);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		panel = new JPanel();
		this.add(panel);
		placeComponents();
		this.setVisible(true);

	}

	protected void placeComponents() {
		panel.setLayout(null);

		textLabel = new JLabel();
		textLabel.setBounds(10, 10, 360, 125);
		panel.add(textLabel);

		yesButton = new JButton(Constants.YES);
		yesButton.setBounds(20, 130, 100, 25);
		panel.add(yesButton);
		yesButton.addActionListener(this);

		yesToAllButton = new JButton(Constants.YES_TO_ALL);
		yesToAllButton.setBounds(20, 170, 100, 25);
		panel.add(yesToAllButton);
		yesToAllButton.addActionListener(this);

		noButton = new JButton(Constants.NO);
		noButton.setBounds(220, 130, 100, 25);
		panel.add(noButton);
		noButton.addActionListener(this);

		noToAllButton = new JButton(Constants.NO_TO_ALL);
		noToAllButton.setBounds(220, 170, 100, 25);
		panel.add(noToAllButton);
		noToAllButton.addActionListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		pressed = true;
		if (e.getSource() == yesButton) {
			whatButtonWasPressed = Constants.PRESSED_YES_BUTTON;
		} else if (e.getSource() == noButton) {
			whatButtonWasPressed = Constants.PRESSED_NO_BUTTON;
		} else if (e.getSource() == yesToAllButton) {
			whatButtonWasPressed = Constants.PRESSED_YES_TO_ALL_BUTTON;
		} else if (e.getSource() == noToAllButton) {
			whatButtonWasPressed = Constants.PRESSED_NO_TO_ALL_BUTTON;
		}

		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	public boolean isPressed() {
		return pressed;
	}

	public int getWhatButtonWasPressed() {
		return whatButtonWasPressed;
	}
}
