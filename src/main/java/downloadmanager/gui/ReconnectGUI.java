package downloadmanager.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ReconnectGUI extends JFrame implements ActionListener {

	private String name;
	long downloaded;
	private long size;

	private JPanel panel;
	private JLabel textLabel;
	private JButton yesButton;
	private JButton noButton;
	private boolean pressed;
	private int whatButtonWasPressed;

	public ReconnectGUI(String name, long downloaded, long size) {
		super("Connection was lost...");
		this.name = name;
		this.size = size;
		this.downloaded = downloaded;

		pressed = false;
		this.setResizable(false);
		this.setSize(420, 250);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		panel = new JPanel();
		this.add(panel);
		placeComponents();
		this.setVisible(true);

	}

	private void placeComponents() {
		panel.setLayout(null);

		textLabel = new JLabel("<html>Do you want to try to reconnect ? <br> Downloaded " + downloaded + " bytes of " + size
				+ " for file " + name + "</html>");
		textLabel.setBounds(10, 10, 360, 125);
		panel.add(textLabel);
		
		yesButton = new JButton("Yes");
		yesButton.setBounds(20, 130, 100, 25);
		panel.add(yesButton);
		yesButton.addActionListener(this);

		
		noButton = new JButton("No");
		noButton.setBounds(220, 130, 100, 25);
		panel.add(noButton);
		noButton.addActionListener(this);
		
		

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		pressed = true;
		if(e.getSource() == yesButton) { 
			whatButtonWasPressed = 1;
		} else if(e.getSource() == noButton) {
			whatButtonWasPressed = 2;
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
