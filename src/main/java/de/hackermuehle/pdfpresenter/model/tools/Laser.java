package de.hackermuehle.pdfpresenter.model.tools;

import java.awt.Color;
import de.hackermuehle.pdfpresenter.model.Preferences;

public class Laser extends WriterTool {

    public String PREFERENCE_COLOR = "laser.color";
    public String PREFERENCE_SIZE = "laser.size";
    public Laser(Color color, double size) {
        super(color, size);
    }

    public Laser() {
        super();

    }


    public Laser(Preferences prefs) {
        super(prefs);
    }
}
