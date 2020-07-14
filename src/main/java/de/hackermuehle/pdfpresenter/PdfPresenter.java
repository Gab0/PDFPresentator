package de.hackermuehle.pdfpresenter;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.apple.eawt.Application;
import com.jidesoft.plaf.LookAndFeelFactory;

import de.hackermuehle.pdfpresenter.viewcontroller.ApplicationFrame;
import de.hackermuehle.pdfpresenter.viewcontroller.ViewUtilities;

/**
 * Main class. Spawns the controller and interface.
 */
public class PdfPresenter {
	private static boolean _isOnMac = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
	private static boolean _isOnLinux = System.getProperty("os.name").toLowerCase().contains("nix") ||
										System.getProperty("os.name").toLowerCase().contains("nux");
	private static ResourceBundle _localizedStrings = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault());

	public static void main(String[] args) {

		// Configure Log4j logger:
		BasicConfigurator.configure();

		if (isOnMac()) {
	    	// Mac OS X specific settings

        // Show app name in system menu bar instead of class name
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "PDF Presenter");
        // Set dock icon
	    	Application.getApplication().setDockIconImage(ViewUtilities.loadImage("/applicationicon.png"));
		}

		// Set the system look and feel:
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Use default look-and-feel.
			e.printStackTrace();
		}

		// Linux only: default Swing JFileChooser looks ugly. With this it
		// resembles the native file chooser much better.
		if (isGtkLaF()){
			// Does not work with SUN VM (tested with 1.6.0_20 / Ubuntu).
			// However it works with current versions of OpenJDK:
			if (System.getProperty("java.vm.name").startsWith("OpenJDK"))
				UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
		}
		
		// Fix for check mark bug in jide (concerns sytem menu entries without
		// icon):
		LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE_WITHOUT_MENU);

		// Since view and controller are linked, this sets the controller to
		// work:
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				new ApplicationFrame();	
			}
		});
	}
	
	/**
	 * Java reads bundles in ISO-8859-1 encoding which has bad support
	 * for non-English languages. This converts it to the universal UTF-8.
	 */
	public static String getLocalizedString(String key) {
		if (key == null) return null;
		
		String original;
		try {
			original = _localizedStrings.getString(key);
		} catch (MissingResourceException e) {
			original = key;
			Logger.getLogger(PdfPresenter.class).warn(
					"Localized string not found for the key " +
					key +
					". This key is used as the localized string.");
		}
		return convertToUtf8Encoding(original);
	}

	private static String convertToUtf8Encoding(String in) {
		String out = in;
		try {
			out = new String(in.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Shouldn't fail. In worst case a message with erroneous encoding
			// will be returned.
			e.printStackTrace();
		}
		return out;
	}
	
	/**
	 * @return True if the program is running on a Mac OS X system
	 */
	public static boolean isOnMac() {
		return _isOnMac;
	}
	
	/**
	 * @return True if the program is running on a Linux or Unix system
	 */
	public static boolean isOnLinux() {
		return _isOnLinux;
	}
	
	
	/**
	 * @return True if the current look and feel is GTK. This is 
	 * likely to be true on Linux.
	 * 
	 */
	public static boolean isGtkLaF() {
		return UIManager.getLookAndFeel().getName().equals("GTK look and feel");
	}
	
}
