package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import javax.swing.Icon;

import org.apache.log4j.Logger;

/**
 * Maybe in V2
 * 
 * @author shuo
 *
 */
public class MainBarPopoverableButton extends PopoverableToggleButton {
	private static final long serialVersionUID = 6289769091729268284L;

	public MainBarPopoverableButton(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void slideInCustomizePopover() {
		// TODO Auto-generated method stub
		Logger.getLogger(getClass()).info("Slide");
	}

}
