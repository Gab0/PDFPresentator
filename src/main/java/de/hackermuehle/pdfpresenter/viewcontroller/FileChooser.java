package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.model.Preferences;
import de.hackermuehle.pdfpresenter.model.State;
import de.hackermuehle.pdfpresenter.model.document.ImageDocument;
import de.hackermuehle.pdfpresenter.model.document.PdfDocument;
import de.hackermuehle.pdfpresenter.model.document.TextDocument;

/**
 * Helper class providing often used functionality concerning
 * file opening / choosing / exception handling.
 */
public class FileChooser {
	
	static class AcceptedFileNameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if (ImageDocument.isAcceptedFileName(name))
				return true;
			if (PdfDocument.isAcceptedFileName(name))
				return true;
			if (TextDocument.isAcceptedFileName(name))
				return true;

			return false;
		}
	}
	
	/**
	 * Creates a File Open Dialog and returns the chosen file if the user does,
	 * or null if the user chooses "cancel" or the file doesn't exist
	 * 
	 * @return absolute path of the chosen file
	 */
	public static String choosePresentation(JFrame frame, State state, Preferences preferences) {
		

			// On the Mac platform the AWT implementation is preferred
			FileDialog dialog = new FileDialog(frame, PdfPresenter.getLocalizedString("fcTitle"), FileDialog.LOAD);
			dialog.setFilenameFilter(new AcceptedFileNameFilter());
			dialog.setDirectory(state.getPresentationDirectory());
			dialog.setVisible(true);
			return dialog.getFile() == null ? null : dialog.getDirectory() + dialog.getFile();

	}

	/**
	 * Chooses a filename and opens a presentation. Handles known exceptions, 
	 * i.e. shows error messages.
	 */
	public static void openPresentation(JFrame frame, State state) {
		String filename = choosePresentation(frame, state, state.getPreferences());
		if (filename == null) return; // User aborted the operation.
		
		openPresentation(filename, state);
	}
	
	/**
	 * Opens a presentation. Handles known exceptions, 
	 * i.e. shows error messages.
	 */
	public static void openPresentation(String filename, State state) {
		state.setPresentationDirectory(filename);
		
		try {
			state.openPresentation(filename);
		
		} catch (FileNotFoundException e) {
			String messageTitle = PdfPresenter.getLocalizedString("fileNotExistsErrorTitle");
			String messageBody = 
				PdfPresenter.getLocalizedString("fileNotExistsErrorBodyFirstPart") +
				filename + 
				PdfPresenter.getLocalizedString("fileNotExistsErrorBodySecondPart");

			JOptionPane.showMessageDialog(null, messageBody, messageTitle, JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception exception) {
			Logger.getLogger(FileChooser.class).error("Cannot open file '" + filename + "'. " + exception.getLocalizedMessage());

			String messageTitle = PdfPresenter.getLocalizedString("fileOpenErrorTitle");
			String messageBody = 
				PdfPresenter.getLocalizedString("fileOpenErrorBodyFirstPart") +
				filename +
				PdfPresenter.getLocalizedString("fileOpenErrorBodySecondPart");

			JOptionPane.showMessageDialog(null, messageBody, messageTitle, JOptionPane.WARNING_MESSAGE);
		}
	}
}
