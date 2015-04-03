package DownloadManager.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import DownloadManager.FTPLogin;
import DownloadManager.Constants.Constants;

public class LoginGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private JTextField hostnameText;
	private JTextField userText;
	private JPasswordField passwordText;
	private JLabel errorLabel;
	private FTPLogin downloader; 
	
	public LoginGUI() {
		super("LoginGUI");
		this.setResizable(false);
		this.setSize(300, 350);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		this.add(panel);
		placeComponents(panel);

		this.setVisible(true);
	}

	private void placeComponents(JPanel panel) {

		panel.setLayout(null);

		JLabel hostnameLabel = new JLabel(Constants.HOSTNAME_LABEL);
		hostnameLabel.setBounds(10, 10, 80, 25);
		panel.add(hostnameLabel);

		hostnameText = new JTextField(20);
		hostnameText.setBounds(100, 10, 160, 25);
		panel.add(hostnameText);

		JLabel userLabel = new JLabel(Constants.USER);
		userLabel.setBounds(10, 40, 80, 25);
		panel.add(userLabel);

		userText = new JTextField(20);
		userText.setBounds(100, 40, 160, 25);
		panel.add(userText);

		JLabel passwordLabel = new JLabel();
		passwordLabel.setBounds(10, 70, 80, 25);
		panel.add(passwordLabel);

		passwordText = new JPasswordField(20);
		passwordText.setBounds(100, 70, 160, 25);
		panel.add(passwordText);

		JButton loginButton = new JButton(Constants.LOGIN_BUTTON);
		loginButton.setBounds(180, 100, 80, 25);
		panel.add(loginButton);

		loginButton.addActionListener(this);

		errorLabel = new JLabel("");
		errorLabel.setBounds(10, 130, 160, 25);
		panel.add(errorLabel);
		
		hostnameText.setText("localhost");
		userText.setText("user");
		passwordText.setText("password");
	}

	public void actionPerformed(ActionEvent e) {
		downloader = new FTPLogin(hostnameText.getText(), 21);
		if(downloader.login(userText.getText(), new String(passwordText.getPassword()))) {
			this.setEnabled(false);
			new DownloadGUI(downloader);
		} else {
			errorLabel.setText(Constants.LOGIN_NOT_SUCCESSFULL);
		}
		

	}

}
