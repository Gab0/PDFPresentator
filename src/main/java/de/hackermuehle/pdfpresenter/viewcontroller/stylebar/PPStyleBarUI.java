package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Color;
import java.awt.Point;

import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicToolBarUI;

public class PPStyleBarUI extends BasicToolBarUI {
	
	// TODO V2 enable rollover effect of buttons (e.g. reset button)
	
	public PPStyleBarUI() {
		// Green border if it's dockable, that is acceptable.
		// Red border if it's not.
		UIManager.put("ToolBar.dockingForeground", Color.GREEN);
		UIManager.put("ToolBar.floatingForeground", Color.RED);
	}
	

	@Override
	public void setFloating(boolean b, Point p) {
		super.setFloating(false, p);
	}

}
