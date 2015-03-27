package DownloadManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

public class DownloadGUI extends JFrame implements ActionListener {
	final static Logger logger = Logger.getLogger(DownloadGUI.class);
	private static final long serialVersionUID = 1L;

	private FTPLogin downloader;

	private JScrollPane scroll;
	private JTextArea display;
	private JButton selectPathButton;
	private JLabel pathLabel;
	private String path;
	private JLabel errorLabel;
	private JTextField noOfThreadsTextField;
	private JButton runButton;
	private int noOfThreads;

	boolean pathWasChosen;

	public DownloadGUI(FTPLogin downloader) {
		super("DownloadGUI");
		this.setResizable(false);
		this.downloader = downloader;
		this.setSize(300, 550);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		this.add(panel);
		placeComponents(panel);

		this.setVisible(true);

	}

	private void placeComponents(JPanel panel) {
		panel.setLayout(null);

		JLabel hostnameLabel = new JLabel("You logged in succesfull");
		hostnameLabel.setBounds(10, 10, 160, 25);
		panel.add(hostnameLabel);

		selectPathButton = new JButton("Path");
		selectPathButton.addActionListener(this);
		selectPathButton.setBounds(10, 50, 100, 25);
		panel.add(selectPathButton);

		pathLabel = new JLabel("Path: no path selected");
		pathLabel.setBounds(10, 90, 250, 25);
		panel.add(pathLabel);

		JLabel threads = new JLabel("no. of threads: ");
		threads.setBounds(10, 130, 100, 25);
		panel.add(threads);

		noOfThreadsTextField = new JTextField();
		noOfThreadsTextField.setBounds(150, 130, 30, 26);
		panel.add(noOfThreadsTextField);

		errorLabel = new JLabel("");
		errorLabel.setBounds(10, 160, 160, 25);
		panel.add(errorLabel);

		runButton = new JButton("Donwload");
		runButton.addActionListener(this);
		runButton.setBounds(70, 220, 100, 25);
		runButton.setEnabled(false);
		panel.add(runButton);

		display = new JTextArea();
		display.setEditable(false);
		scroll = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		scroll.setBounds(10, 260, 270, 230);
		panel.add(scroll);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == selectPathButton) {
			path = choosePath();
			if (path == null) {
				pathLabel.setText("<html><font color='red'>Path: Invalid path</font></html>");
			} else {
				pathLabel.setText("Path: " + path);
				runButton.setEnabled(true);
			}
		} else if (e.getSource() == runButton) {
			
			if (noOfThreadsTextField.getText().trim().isEmpty()) {
				noOfThreads = 5;
				setErrorLabel();
			} else {
				try {
					noOfThreads = Integer.parseInt(noOfThreadsTextField.getText());
					errorLabel.setText("");
				} catch (Exception ex) {
					logger.error(ex.getMessage());
					setErrorLabel();
				}
			}

			if (noOfThreads <= 0) {
				setErrorLabel();
			} else {
				try {
					String[] files = downloader.getFtpClient().listNames();
					
				} catch (Exception ex) {
					logger.info(ex.getMessage());
				}
				System.out.println("Run was clicked");
			}
		}

	}

	private void setErrorLabel() {
		noOfThreads = 5;
		errorLabel.setText("<html><font color='red'>Invalid number of threads. " + "Selcted default 5.</font></html>");

	}

	public String choosePath() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("choosertitle");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			/*
			 * System.out.println("getCurrentDirectory(): " +
			 * chooser.getCurrentDirectory());
			 * System.out.println("getSelectedFile() : " +
			 * chooser.getSelectedFile());
			 */
			return chooser.getSelectedFile().toString();
		} else {
			return null;
		}
	}
}
