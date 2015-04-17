package downloadmanager.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import downloadmanager.constants.Constants;

public abstract class PopupGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	protected String name;
	protected long sourceFileSize;
	protected long targetFileSize;

	protected JPanel panel;
	protected JLabel textLabel;
	protected JButton yesButton;
	protected JButton noButton;
	protected JButton yesToAllButton;
	protected JButton noToAllButton;
	protected boolean pressed;
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
			whatButtonWasPressed = 1;
		} else if (e.getSource() == noButton) {
			whatButtonWasPressed = 2;
		} else if(e.getSource() == yesToAllButton) {
			whatButtonWasPressed = 3;
		} else if(e.getSource() == noToAllButton) {
			whatButtonWasPressed = 4;
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
