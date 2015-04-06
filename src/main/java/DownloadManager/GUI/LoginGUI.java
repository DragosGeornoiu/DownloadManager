package DownloadManager.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import DownloadManager.FTPLogin;
import DownloadManager.Constants.Constants;

/**
 * 
 * Initialises the login swing form where the user has to insert the hostname,
 * the username and the password.
 * 
 */
public class LoginGUI extends JFrame implements ActionListener {
	final static Logger logger = Logger.getLogger(LoginGUI.class);
	private static final long serialVersionUID = 1L;

	private JTextField hostnameText;
	private JTextField userText;
	private JPasswordField passwordText;
	private JLabel errorLabel;
	private FTPLogin ftpLogin;

	public LoginGUI() {
		super("Login");
		this.setResizable(false);
		this.setSize(300, 350);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		this.add(panel);
		placeComponents(panel);

		this.setVisible(true);
	}

	/**
	 * Places all the components of the LoginGUI in the panel given as
	 * parameter.
	 * 
	 * @param panel
	 *            where the components are placed on / added to.
	 */
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

		JLabel passwordLabel = new JLabel(Constants.PASSWORD_LABEL);
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

	/**
	 * The action when the user presses the loginButton. If the credentials are
	 * correct, it launches the DownloadGUI interface, otherwise it tells the
	 * user, using the errorLabel, that the login was not succesfull.
	 */
	public void actionPerformed(ActionEvent e) {
		logger.info("Login button was pressed"); 
		ftpLogin = new FTPLogin(hostnameText.getText(), 21);
		if (ftpLogin.login(userText.getText(), new String(passwordText.getPassword()))) {
			this.setEnabled(false);
			new DownloadGUI(ftpLogin);
		} else {
			logger.error("Login not succesfull");
			errorLabel.setText(Constants.LOGIN_NOT_SUCCESSFULL);
		}

	}

}
