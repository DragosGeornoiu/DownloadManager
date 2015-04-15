import org.apache.log4j.Logger;

import downloadmanager.gui.DownloadGUI;


public class DownloadManager {
	final static Logger logger = Logger.getLogger(DownloadManager.class);
	
	public static void main(String[] args) {
		logger.info("Starting Download Manager application");
		new DownloadGUI();
	}
}
