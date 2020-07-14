package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

public class DropDownButton extends JButton {

	private static final long serialVersionUID = 1L;

	private static final JPopupMenu _menu = new JPopupMenu();

	{
		// Opens / Closes the DropDown-Menu:
		addActionListener(new DropDownActionListener());
	}
	
	public DropDownButton() {
		super();
	}
	
	public DropDownButton(Icon icon) {
		super();
		setIcon(icon);
	}
	
	public DropDownButton(String label) {
		super(label);
	}
	
	public DropDownButton(Icon icon, String label) {
		super(label);
		setIcon(icon);
	}
	
	// Opens / Closes the DropDown-Menu:
	class DropDownActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent event) {
			if (_menu.isVisible()) {
				_menu.setVisible(false);
			} else {
				_menu.show(DropDownButton.this, 0, DropDownButton.this.getHeight() + 2);
				_menu.setVisible(true);
			}
			
			// TODO V2
//			// On the Mac when we try to click on the button to close the menu
//			// when it is already opened, we cannot do so. Instead we get it 
//			// displayed again. This is because the menu already changes state
//			// on the Mac for mousePressed event: it is already invisible after
//			// mousePressed so when mouseRelease activates actionPerformed it
//			// will be shown again.
//			// Now using our own boolean to solve this problem.
//			if (_isVisible) {
//				menu.setVisible(false);
//				_isVisible = false;
//			} else {
//				menu.show(DropDownButton.this, 0, DropDownButton.this.getHeight()); 
//				menu.setVisible(true);
//				_isVisible = true;
//			}
		}
	}
	
	public JPopupMenu getMenu() {
		return _menu;
	}

	public void setIcon(Icon icon) {
		super.setIcon(createIcon(icon));
	}
    
	// Adds a small black indicator arrow to the given icon:
	private Icon createIcon(Icon defaultIcon) {
		Image image = new BufferedImage(defaultIcon.getIconWidth() + 6 + 4, defaultIcon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = image.getGraphics();
		defaultIcon.paintIcon(this, g, 0, 0);
		g.setColor(Color.BLACK);
		int[] x = {defaultIcon.getIconWidth() + 2, defaultIcon.getIconWidth() + 8, defaultIcon.getIconWidth() + 5};
		int[] y = {defaultIcon.getIconHeight() / 2, defaultIcon.getIconHeight() / 2, defaultIcon.getIconHeight() / 2 + 3};
		g.fillPolygon(x, y, 3);
		
		return new ImageIcon(image);
	}
}
