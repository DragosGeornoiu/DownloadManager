import org.apache.log4j.Logger;

import DownloadManager.GUI.LoginGUI;


public class DownloadManager {
	final static Logger logger = Logger.getLogger(DownloadManager.class);
	
	public static void main(String[] args) {
		logger.info("Starting Download Manager application");
		new LoginGUI();
	}
}
