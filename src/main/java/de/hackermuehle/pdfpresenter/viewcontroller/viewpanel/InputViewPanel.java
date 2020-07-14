package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import de.hackermuehle.pdfpresenter.model.Presentation;
import de.hackermuehle.pdfpresenter.model.slide.Slide;

/**
 * An {@link InputViewPanel} receives user input and 
 */
public abstract class InputViewPanel extends ViewPanel {
	private static final long serialVersionUID = 1100748881286660825L;
	
	public abstract void setSlide(Slide slide);
	public abstract void setPresentation(Presentation presentation);
}
