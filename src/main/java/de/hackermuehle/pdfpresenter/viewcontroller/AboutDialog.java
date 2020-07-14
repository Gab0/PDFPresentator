package de.hackermuehle.pdfpresenter.viewcontroller;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import de.hackermuehle.pdfpresenter.PdfPresenter;
import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.Popover;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = -5562138286920403201L;

	public AboutDialog(Frame frame) {
		super(frame);
		setTitle(PdfPresenter.getLocalizedString("abTitle"));
		
		setFocusable(true);
		setResizable(false);
		setModal(true);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false); // Mac
				dispose();
			}
		});
		
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				// Close the dialog with ctrl/cmd-w or escape or space:
				int controlOrCommandKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
				if ((e.getKeyCode() == KeyEvent.VK_W && e.getModifiers() == controlOrCommandKeyMask) ||
					(e.getKeyCode() == KeyEvent.VK_ESCAPE) ||
					(e.getKeyCode() == KeyEvent.VK_SPACE)) {
					setVisible(false); // Mac
					dispose();
				}
			}
		});
		
		initializeUi();
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void initializeUi() {
		JPanel panel = new JPanel(
			new MigLayout(
				"wrap 1, insets 10 10 10 8", 
				"[center]", 
				""));
		
		JLabel logoLabel = new JLabel(ViewUtilities.createIcon("/applicationicon.png", 0));
		panel.add(logoLabel, "gaptop 10, gapbottom 10");
		
		JLabel nameLabel = new JLabel("<html><center><font size=+2><b>PDF Presenter</b></font><br>" +
				PdfPresenter.getLocalizedString("abVersion") + " 1.0</html>");
		//nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
		panel.add(nameLabel, "gapleft 30, gapright 30");
		
		JSeparator line = new JSeparator(JSeparator.HORIZONTAL);
		panel.add(line, "growx, gaptop 8, gapbottom 8");
		
		JLabel developedByLabel = new JLabel(PdfPresenter.getLocalizedString("abDevelopedBy"));
		panel.add(developedByLabel, "gapbottom 5");
		
		String developer[] = new String[] {"Jürgen Benjamin Ronshausen", "Niklas Büscher", "Shuo Yang 杨硕", "Martin Tschirsich"};
		int i = Math.abs((int) System.currentTimeMillis() - 4);
		JLabel monkeysAndFrenchLabel = 
			new JLabel("<html><center>" +
				developer[i++ % developer.length] + "<br>" +
				developer[i++ % developer.length] + "<br>" +
				developer[i++ % developer.length] + "<br>" +
				developer[i++ % developer.length] + "</html>");	
		panel.add(monkeysAndFrenchLabel, "wrap, gapleft 30, gapright 30");
		
		JLabel contactLabel = new JLabel(PdfPresenter.getLocalizedString("abContact"));
		panel.add(contactLabel, "gaptop 5, gapbottom 1");
		
		String email = "bp@hackermuehle.de";
		JTextField contactTextField = new JTextField(email, email.length());
		contactTextField.setHorizontalAlignment(JTextField.CENTER);
		contactTextField.setEditable(false);
		contactTextField.setBorder(null);
		contactTextField.setBackground(getBackground());
		panel.add(contactTextField);
		
		//JButton closeButton = new JButton("Close");
		//panel.add(closeButton, "center, gaptop 8");
		
		// Done button
		ImageIcon icon = new ImageIcon(Popover.class.getResource("/saveAndClose.png"));
		ImageIcon pressedIcon = new ImageIcon(Popover.class.getResource("/saveAndClosePressed.png"));
		
		JButton doneButton = new JButton(icon);
		doneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false); // Mac
				dispose();
			}
		});
		doneButton.setPressedIcon(pressedIcon);
		doneButton.setBorderPainted(false); 		// Mac
		doneButton.setContentAreaFilled(false); 	// Windows
		panel.add(doneButton, "gaptop 5");
		
		add(panel);
	}
}
