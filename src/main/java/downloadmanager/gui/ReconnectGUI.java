package downloadmanager.gui;

import downloadmanager.constants.Constants;

/**
 * ReconnectGUI extends the PopupGUI and has almost the same functionality,
 * except that it provides some information to the user about what he is going
 * to overwrite.
 * 
 * At the moment, no actual need for ReconnectGUI to be a class, the label can
 * be set in PopupGUI, but added for possible future needs.
 *
 */
public class ReconnectGUI extends PopupGUI{
	private static final long serialVersionUID = 1L;

	public ReconnectGUI(String name, long sourceFileSize, long targetFileSize) {
		super(name, sourceFileSize, targetFileSize, Constants.CONNECTION_LOST);
		
		textLabel.setText("<html>Do you want to try to reconnect ? <br> Downloaded " + sourceFileSize + " bytes of "
				+ targetFileSize + " for file " + name + "</html>");
	}
	
}

