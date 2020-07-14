package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * Behaves similar to the {@link BoxLayout}, especially useful in combination
 * with a {@link JToolBar}.
 * If the combined height/width of the components of the container this layout
 * is applied to exceeds the container's height/width, buttons are shown that
 * allow to scroll the components vertically/horizontally.
 */
public class ExpandLayout implements LayoutManager, SwingConstants, PropertyChangeListener {
	private static final int BUTTON_SIZE = 35;
	private Container _container;
	private int _translateX = 0;
	private int _translateY = 0;
	private int _deltaX = 100;
	private int _deltaY = 100;
	private int _orientation;
	private BoxLayout _boxLayout;
	private JButton _leftButton = new LeftButton();
	private JButton _rightButton = new RightButton();
	private JButton _upButton = new UpButton();
	private JButton _downButton = new DownButton();
	
	private static Image _arrowLeft;
	private static Image _arrowRight;
	private static Image _arrowUp;
	private static Image _arrowDown;
	
	static {
		try {
			_arrowRight = ImageIO.read(ColorGroupedButton.class.getResource("/triangleright.png"));
			_arrowLeft = ImageIO.read(ColorGroupedButton.class.getResource("/triangleleft.png"));
			_arrowUp = ImageIO.read(ColorGroupedButton.class.getResource("/triangleup.png"));
			_arrowDown = ImageIO.read(ColorGroupedButton.class.getResource("/triangledown.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates and initializes a {@link HorizontalExpandLayout}.
	 *
	 * @param container The container this {@link LayoutManager} is applied to
	 * @param orientation Either {@link SwingConstants#HORIZONTAL} or {@link SwingConstants#VERTICAL}
	 */
	public ExpandLayout(Container container, int orientation) {
		_orientation = orientation;
		_container = container;
		_container.addPropertyChangeListener(this);
		
		if (_orientation == VERTICAL) {
			_boxLayout = new BoxLayout(_container, BoxLayout.PAGE_AXIS);
		} else {
			_boxLayout = new BoxLayout(_container, BoxLayout.LINE_AXIS);
		}
	}
 
	public void addLayoutComponent(String name, Component component) {
		if ((component != _leftButton) && (component != _rightButton) &&
			(component != _upButton) && (component != _downButton))
			_boxLayout.addLayoutComponent(name, component);
	}
 
	public void layoutContainer(Container parent) {
		_container = parent;
		_container.remove(_leftButton);
		_container.remove(_rightButton);
		_container.remove(_upButton);
		_container.remove(_downButton);
		
		// Calculate the combined size of all components:
		Insets insets = _container.getInsets();
		int minWidth = insets.right + insets.left;
		int minHeight = insets.top + insets.bottom;
		for (Component component : _container.getComponents()) {
			minWidth += component.getPreferredSize().width;
			minHeight += component.getPreferredSize().height;
		}
		
		// Set the scroll-step to the size of the biggest component:
		_deltaX = 0;
		_deltaY = 0;
		for (Component component : _container.getComponents()) {
			if (component.getSize().width > _deltaX) 
				_deltaX = component.getPreferredSize().width;
			if (component.getSize().height > _deltaY) 
				_deltaY = component.getPreferredSize().height;
		}
	
		Dimension originalSize = _container.getSize();
		if ((originalSize.width < minWidth) && (_orientation == HORIZONTAL)) {
			
			// The container is smaller than the minimum width, apply the
			// original layout and show scroll buttons:
			_container.setSize(_boxLayout.preferredLayoutSize(_container));
			_boxLayout.layoutContainer(_container);
			_container.setSize(originalSize);
			
			for (Component component : _container.getComponents()) {
				component.setLocation(component.getX() + _translateX, component.getY());
			}
			
			// Show right button if necessary:
			if (originalSize.width < _translateX + minWidth) {
				_container.add(_rightButton, 0);
				_rightButton.setSize(BUTTON_SIZE, _container.getHeight());
				_rightButton.setLocation(_container.getWidth() - _rightButton.getWidth(), 0);
			}
			
			// Show left button if necessary:
			if (_translateX < 0) {
				_container.remove(_leftButton);
				_container.add(_leftButton, 0);
				_leftButton.setSize(BUTTON_SIZE, _container.getHeight());
			}
		}
		else if ((originalSize.height < minHeight) && (_orientation == VERTICAL)) {

			// The container is smaller than the minimum height, apply the
			// original layout and show scroll buttons:
			_container.setSize(_boxLayout.preferredLayoutSize(_container));
			_boxLayout.layoutContainer(_container);
			_container.setSize(originalSize);
			
			for (Component component : _container.getComponents()) {
				component.setLocation(component.getX(), component.getY() + _translateY);
			}
			
			// Show down button if necessary:
			if (originalSize.height < _translateY + minHeight) {
				_container.add(_downButton, 0);
				_downButton.setSize(_container.getWidth(), BUTTON_SIZE);
				_downButton.setLocation(0, _container.getHeight() - _downButton.getHeight());
			}
			
			// Show up button if necessary:
			if (_translateY < 0) {
				_container.remove(_upButton);
				_container.add(_upButton, 0);
				_upButton.setSize(_container.getWidth(), BUTTON_SIZE);
				_upButton.setLocation(0, 0);
			}
		}
		else {
			
			// The container is bigger/equal than the minimum width, apply the
			// original layout and reset the translation:
			_boxLayout.layoutContainer(_container);
			_translateX = 0;
			_translateY = 0;
		}
	}
 
	public Dimension minimumLayoutSize(Container parent) {
		return _boxLayout.minimumLayoutSize(parent);
	}
 
	public Dimension preferredLayoutSize(Container parent) {
		return _boxLayout.preferredLayoutSize(parent);
	}
 
	public void removeLayoutComponent(Component component) {
		if ((component != _leftButton) && (component != _rightButton) &&
			(component != _upButton) && (component != _downButton))
			_boxLayout.removeLayoutComponent(component);
	}
 
	/**
	 * When applied to a {@See JToolBar}, the tool bar informs about
	 * orientation changes via this listener.
	 * 
	 * @param e	The property change event of the component to handle
	 */
	@Override
    public void propertyChange(PropertyChangeEvent e) {
    	String name = e.getPropertyName();
    	if (name.equals("orientation")) {
			_orientation = ((Integer) e.getNewValue()).intValue();

			if (_orientation == VERTICAL)
				_boxLayout = new BoxLayout(_container, BoxLayout.PAGE_AXIS);
			else {
				_boxLayout = new BoxLayout(_container, BoxLayout.LINE_AXIS);
			}
		}
	}
	
	/**
	 * Transparent gradient button with left arrow.
	 */
	class LeftButton extends JButton {
		private static final long serialVersionUID = -4821080938588393086L;

		public LeftButton() {
			setOpaque(false);
			setFocusable(false);
			setBorderPainted(false);
			setAction(new AbstractAction("", new ImageIcon(_arrowLeft)) {
				private static final long serialVersionUID = -6265621674817184136L;

				@Override
				public void actionPerformed(ActionEvent e) {
					// Increases the x translation:
					_translateX += _deltaX;
					layoutContainer(_container);
				}
			});
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			GradientPaint gradient = new GradientPaint(0, 0, _container.getBackground(), getWidth(), 0, new Color(0,0,0,0));
			g2d.setPaint(gradient);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			super.paintComponent(g);
		}
	}
	
	/**
	 * Transparent gradient button with right arrow.
	 */
	class RightButton extends JButton {
		private static final long serialVersionUID = -2579611036998047248L;

		public RightButton() {
			setOpaque(false);
			setFocusable(false);
			setBorderPainted(false);
			setAction(new AbstractAction("", new ImageIcon(_arrowRight)) {
				private static final long serialVersionUID = 6067842386116327273L;

				@Override
				public void actionPerformed(ActionEvent e) {
					_translateX -= _deltaX;
					layoutContainer(_container);
				}
			});
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			GradientPaint gradient = new GradientPaint(0, 0, new Color(0,0,0,0), getWidth(), 0, _container.getBackground());
			g2d.setPaint(gradient);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			super.paintComponent(g);
		}
	}
	
	/**
	 * Transparent gradient button with up arrow.
	 */
	class UpButton extends JButton {
		private static final long serialVersionUID = -7518840895559635530L;

		public UpButton() {
			setOpaque(false);
			setFocusable(false);
			setBorderPainted(false);
			setAction(new AbstractAction("", new ImageIcon(_arrowUp)) {
				private static final long serialVersionUID = -6265621674817184136L;

				@Override
				public void actionPerformed(ActionEvent e) {
					_translateY += _deltaY;
					layoutContainer(_container);
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			GradientPaint gradient = new GradientPaint(0, 0, _container.getBackground(), 0, getHeight(), new Color(0,0,0,0));
			g2d.setPaint(gradient);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			super.paintComponent(g);
		}
	};
	
	/**
	 * Transparent gradient button with down arrow.
	 */
	class DownButton extends JButton {
		private static final long serialVersionUID = 531860393293160405L;
		
		public DownButton() {
			setOpaque(false);
			setFocusable(false);
			setBorderPainted(false);
			setAction(new AbstractAction("", new ImageIcon(_arrowDown)) {
				private static final long serialVersionUID = 6067842386116327273L;

				@Override
				public void actionPerformed(ActionEvent e) {
					_translateY -= _deltaY;
					layoutContainer(_container);
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			GradientPaint gradient = new GradientPaint(0, 0, new Color(0,0,0,0), 0, getHeight(), _container.getBackground());
			g2d.setPaint(gradient);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			super.paintComponent(g);
		}
	};
}