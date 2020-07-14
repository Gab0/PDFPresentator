package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.hackermuehle.pdfpresenter.viewcontroller.stylebar.StyleBar.StyleBarAlignment;

public class ColorPopover extends Popover {
	
	private static final long serialVersionUID = 5883983352101598207L;
	private static final int GRID_ROW = 6;
	private static final int GRID_COLUMN = 4;
	private static final int COLOR_PALETTE_WIDTH = 120;
	private static final int COLOR_PALETTE_HEIGHT = (int) ((double) COLOR_PALETTE_WIDTH / GRID_COLUMN * GRID_ROW);
	
	private Color[][] _colors = {
			{ new Color(128, 0, 0),   new Color(128, 64, 0), new Color(255, 0, 0),    new Color(255, 255, 255) },
			{ new Color(128, 128, 0), new Color(64, 128, 0), new Color(255, 255, 0),  new Color(204, 204, 204) },
			{ new Color(0, 128, 0),   new Color(0, 128, 64), new Color(0, 255, 0),    new Color(153, 153, 153) }, 
			{ new Color(0, 128, 128), new Color(0, 64, 128), new Color(0, 255, 255),  new Color(102, 102, 102) }, 
			{ new Color(0, 0, 128),   new Color(64, 0, 128), new Color(0, 0, 255),    new Color(51, 51, 51) }, 
			{ new Color(128, 0, 128), new Color(128, 0, 64), new Color(255, 0, 255),  new Color(0, 0, 0) } 
	}; 
	private ColorGroupedButton _button;
	private GradientColorComponent _currentColorBlock;
		
	/**
	 * 
	 * 
	 * @param frame
	 * @param button
	 * @param alignment 
	 */
	public ColorPopover(JFrame frame, ColorGroupedButton button, StyleBarAlignment alignment) {
		super(frame, button, alignment);
		_button = button;
		getContentPane().add(createPanel());
		pack();
		setVisible(true);
	}
	
	public ColorPopover(JFrame frame, ColorGroupedButton button, 
			StyleBarAlignment alignment, Color[][] colors) {
		super(frame, button, alignment);
		_button = button;
		_colors = colors;
		getContentPane().add(createPanel());
		pack();
		setVisible(true);
	}

	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.BLACK);
			
		JPanel innerPanel = new JPanel(new GridLayout(GRID_ROW, GRID_COLUMN, 0, 0));
		innerPanel.setPreferredSize(new Dimension(COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT));
		
		ColorPopoverMouseListener colorPopoverMouseListener = new ColorPopoverMouseListener();
		for (int i = 0; i < GRID_ROW; i++) {
			for (int j = 0; j < GRID_COLUMN; j++) {
				Color color = _colors[i][j];
				GradientColorComponent colorBlock = new GradientColorComponent(color);
				colorBlock.addMouseListener(colorPopoverMouseListener);
				if (_button.getColor().equals(color)) {
					_currentColorBlock = colorBlock;
					colorBlock.setChosen(true);
				}
				innerPanel.add(colorBlock);
			}
		}

		// TODO throw exception if initial color illegal?
		
		panel.add(innerPanel);
		panel.add(createDoneButton());	
		
		return panel;
	}
	
	private class ColorPopoverMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			GradientColorComponent colorBlock = (GradientColorComponent) e.getSource();
			
			if (_currentColorBlock != null) {
				if (colorBlock.getColor().equals(_currentColorBlock.getColor())) return;
				_currentColorBlock.setChosen(false);
			}
			
			colorBlock.setChosen(true);
			_currentColorBlock = colorBlock;
			
			_button.setColor(colorBlock.getColor());
		}

		@Override
		public void mouseReleased(MouseEvent e) {}
		
	}
	
}
