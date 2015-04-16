package downloadmanager.gui;

import downloadmanager.constants.Constants;

public class ReconnectGUI extends PopupGUI{
	private static final long serialVersionUID = 1L;

	public ReconnectGUI(String name, long sourceFileSize, long targetFileSize) {
		super(name, sourceFileSize, targetFileSize, Constants.CONNECTION_LOST);
		
		textLabel.setText("<html>Do you want to try to reconnect ? <br> Downloaded " + sourceFileSize + " bytes of "
				+ targetFileSize + " for file " + name + "</html>");
	}
	
}

