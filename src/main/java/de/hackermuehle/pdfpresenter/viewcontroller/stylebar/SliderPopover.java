package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar.StyleBarAlignment;


/**
 * CustomizePopover with a slider. It can be used for font size,
 * brush thickness etc.
 * 
 * @author shuo
 *
 */
public class SliderPopover extends Popover {

	private static final long serialVersionUID = 1410848627787048432L;
	private static final int SLIDER_POPOVER_WIDTH = 80;
	private static final int SLIDER_POPOVER_HEIGHT = 200;

	private JLabel _fontSizeLabel;
	private ThicknessGroupedButton _button;

	public SliderPopover(JFrame frame, ThicknessGroupedButton button, StyleBarAlignment alignment) {
		super(frame, button, alignment);
		
		_button = button;
		
		getContentPane().add(createPanel());
		
		pack();
		setVisible(true);
	}

	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.BLACK);
		
		panel.setPreferredSize(new Dimension(SLIDER_POPOVER_WIDTH, SLIDER_POPOVER_HEIGHT));
		
		_fontSizeLabel = new JLabel(String.valueOf(_button.getThickness()));
		_fontSizeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		_fontSizeLabel.setForeground(Color.WHITE);
        _fontSizeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		panel.add(_fontSizeLabel);
		
		JSlider fontSizeSlider = new JSlider(JSlider.VERTICAL,(int) (100 * Math.log(1)),(int)(100 * Math.log(10)), (int) (100 * Math.log(_button.getThickness() / 11 + 1)));
		fontSizeSlider.setInverted(true);
		fontSizeSlider.addChangeListener(new SliderValueChangeListener());
		// Commented out: On-Click-Selection
		//fontSizeSlider.addMouseListener(new SlideMouseListener());
		fontSizeSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		fontSizeSlider.setOpaque(false);
		fontSizeSlider.setUI(new PPSliderUI(fontSizeSlider));
		
		panel.add(fontSizeSlider);
		panel.add(createDoneButton());	
		
		return panel;
	}
	
	private class SlideMouseListener implements  MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {
			setVisible(false);
		}
	}
	
	private class SliderValueChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			// Slider value changed
			
			int updatedValue = (int) (1 + 11 * (Math.exp(((JSlider) e.getSource()).getValue() / 100.0)-1));
			
			// TODO update controller
			_button.setThickness(updatedValue);
			_fontSizeLabel.setText(String.valueOf(updatedValue));
		}
	}


}