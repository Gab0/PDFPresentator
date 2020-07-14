package de.hackermuehle.pdfpresenter.viewcontroller.viewpanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.hackermuehle.pdfpresenter.model.CacheEntry;
import de.hackermuehle.pdfpresenter.model.CacheObserver;
import de.hackermuehle.pdfpresenter.model.Clipping;
import de.hackermuehle.pdfpresenter.model.ImmutableClipping;
import de.hackermuehle.pdfpresenter.model.document.Document;
import de.hackermuehle.pdfpresenter.model.slide.Grid;
import de.hackermuehle.pdfpresenter.model.slide.Slide;
import de.hackermuehle.pdfpresenter.model.slide.SlideCacheEntry;
import de.hackermuehle.pdfpresenter.model.slide.SlideListener;

/**
 * The SlideViewPanel displays the current state of a {@link Slide}, i.e.
 * annotations, the slide background, grid and zoom.
 * Slide updates are visualized immediately.
 */
public class SlideViewPanel extends ViewPanel {
	private static final long serialVersionUID = 1L;
	
	private boolean _reuseBuffer = false;
	private Rectangle2D _dirty = null;
	private int _priority;
	private int _precachePriority;
	protected Slide _slide;
	protected Slide _precacheSlide;
	private Grid _grid;
	private Clipping _clipping;
	private Rectangle2D _source;
	private Rectangle2D _bufferSource = null;
	private BufferedImage _buffer = null;
	private SlideCacheEntry _slideCacheEntry = null;
	private SlideCacheEntry _precacheCacheEntry = null;
	private ViewPanelGraphicsListener _graphicsListener = new ViewPanelGraphicsListener();
	private SlideCacheObserver _slideCacheObserver = new SlideCacheObserver();
	
	public SlideViewPanel(Slide slide, Rectangle2D source, Grid grid) {
		initialize(slide, source, grid, 0, null, 0);
	}
	
	public SlideViewPanel(Slide slide, Rectangle2D source, Grid grid, int priority) {
		initialize(slide, source, grid, priority, null, 0);
	}
	
	public SlideViewPanel(Slide slide, Rectangle2D source, Grid grid, int priority, Slide precacheSlide, int precachePriority) {
		initialize(slide, source, grid, priority, precacheSlide, precachePriority);
	}
	
	private void initialize(Slide slide, Rectangle2D source, Grid grid, int priority, Slide precacheSlide, int precachePriority) {
		_source = (Rectangle2D) source.clone();
		
		_grid = grid;
		_priority = priority;
		_precacheSlide = precacheSlide;
		_precachePriority = precachePriority;
		
		// Listen to slide changes immediately:
		_slide = slide;
		_slide.addListener(_graphicsListener);
		
		// Set transparent for background / otherwise TextInputPanel has redraw problems (XXX: Why?):
		setOpaque(false);
	}
    
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
	
	class SlideCacheObserver implements CacheObserver {
		@Override
		public void notify(CacheEntry cacheEntry, String msg) {
			
			makeDirty(_slide.getSize(), true);
			repaint();
		}
	}
	
	class ViewPanelGraphicsListener implements SlideListener {
		/**
		 * Called on slide changes. Redraw slide immediately if cached
		 * or no caching available, otherwise wait for cache completion.
		 */
		@Override
		public void update(Rectangle2D r) {
			
			makeDirty(r, false);
			if (_clipping != null) 
				repaint(_clipping.getTransform().createTransformedShape(r).getBounds());
		}
	}
	
	/**
	 * Overwritten since it is necessary to react to size changes before
	 * external listeners receive the size change messages.
	 * This method is used internally by Swing to handle all kind of size
	 * changes. This behavior may change in future, then overwrite setBounds()
	 * instead.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void reshape(int x, int y, int width, int height) {

		// Create a new clipping and check whether it is valid:
		if (width != getWidth() || height != getHeight()) {
		
			// Places size change messages in the message queue but does not 
			// handle them before this method is terminated:
			super.reshape(x, y, width, height);
			
			Clipping clipping = null;
			try {
				clipping = createClipping(_source, width, height);
			} catch (NoninvertibleTransformException exception) {
				// Clipping is not valid, slide becomes invisible:
				_buffer = null;
				_clipping = null;
				if (_slideCacheEntry != null) _slideCacheEntry.dispose();
				if (_precacheCacheEntry != null) _precacheCacheEntry.dispose();
				return;
			}
			
			// Check whether the clipping changed:
			if (!clipping.equals(_clipping)) {
				_clipping = clipping;
				
				// Clipping changed, cache entries have to be updated:
				if (_slideCacheEntry != null) {
					_slideCacheEntry.update(_clipping, _priority, _slideCacheObserver);
				} else {
					_slideCacheEntry = _slide.cache(_clipping, _priority, _slideCacheObserver);
				}
					
				if (_precacheCacheEntry != null) {
					_precacheCacheEntry.update(_clipping, _priority, null);
				} else if (_precacheSlide != null) {
					_precacheCacheEntry = _precacheSlide.cache(_clipping, _precachePriority, null);
				}
			}
			
			makeDirty(_slide.getSize(), false);
			//repaint();
		}
		else {

			// Places size change messages in the message queue but does not 
			// handle them before this method is terminated:
			super.reshape(x, y, width, height);
		}
	}
	
	public void setPriority(int priority) {
		_priority = priority;
	}
	
	public Slide getSlide() {
		return _slide;
	}
	
	public void setSlide(Slide slide, Slide precacheSlide, Rectangle2D source, int precachePriority) {
		setSlide(slide, source);
		
		if (precacheSlide != _precacheSlide) {
			if (_precacheCacheEntry != null) _precacheCacheEntry.dispose();
			if (precacheSlide != null) _precacheCacheEntry = precacheSlide.cache(_clipping, precachePriority, null);
		} else if (_precacheCacheEntry != null) {
			_precacheCacheEntry.update(_clipping, precachePriority, null);
		}
		_precacheSlide = precacheSlide;
		_precachePriority = precachePriority;
	}
	
	public void setSlide(Slide slide, Rectangle2D source) {
		_source = (Rectangle2D) source;
		_slide.removeListener(_graphicsListener);
		
		// Create a new clipping and check whether it is valid:
		try {
			_clipping = createClipping(_source);
		} catch (NoninvertibleTransformException exception) {
			// Clipping is not valid, slide becomes invisible:
			_buffer = null;
			_clipping = null;
			if (_slideCacheEntry != null) _slideCacheEntry.dispose();
			if (_precacheCacheEntry != null) _precacheCacheEntry.dispose();
			return;
		}
		
		if (slide == _slide) {
			// Slide has not changed, just update an existing cache entry:
			if (_slideCacheEntry != null) {
				_slideCacheEntry.update(_clipping, _priority, _slideCacheObserver);
			} else {
				_slideCacheEntry = _slide.cache(_clipping, _priority, _slideCacheObserver);
			}
		} else {
			// Slide has changed, so a new cache entry has to be created:
			_slide = slide;
			if (_slideCacheEntry != null) _slideCacheEntry.dispose();
			_slideCacheEntry = _slide.cache(_clipping, _priority, _slideCacheObserver);
		}

		_slide.addListener(_graphicsListener);
		
		if (_slideCacheEntry == null) {
			makeDirty(_slide.getSize(), false);
			repaint();
		}
	}
	
	public void setSource(Rectangle2D source) {
		// XXX: For performance only; should be checked by the caller:
		if (source.equals(_source)) return;
		
		_source = source;
		
		// Create a new clipping and check whether it is valid:
		Clipping clipping = null;
		try {
			clipping = createClipping(_source);
		} catch (NoninvertibleTransformException exception) {
			// Clipping is not valid, slide becomes invisible:
			_buffer = null;
			_clipping = null;
			if (_slideCacheEntry != null) _slideCacheEntry.dispose();
			if (_precacheCacheEntry != null) _precacheCacheEntry.dispose();
			return;
		}
		
		// Check whether the clipping changed:
		if (!clipping.equals(_clipping)) {
			_clipping = clipping;
			
			// Clipping changed, cache entries have to be updated:
			if (_slideCacheEntry != null) {
				_slideCacheEntry.update(_clipping, _priority, _slideCacheObserver);
			} else {
				_slideCacheEntry = _slide.cache(_clipping, _priority, _slideCacheObserver);
			}
				
			if (_precacheCacheEntry != null) {
				_precacheCacheEntry.update(_clipping, _priority, null);
			} else if (_precacheSlide != null) {
				_precacheCacheEntry = _precacheSlide.cache(_clipping, _precachePriority, null);
			}
			
			// Repaint immediately if there is no caching:
			if (_slideCacheEntry == null) {
				makeDirty(_slide.getSize(), true);
				repaint();
			}
			//else makeDirty(_slide.getSize(), true);
		}
	}
	
	public ImmutableClipping getClipping() {
		return _clipping;
	}
	
	public void setGrid(Grid grid) {
		// XXX: For performance only; should be checked by the caller:
		if ((_grid == null && grid == null) || (_grid != null && _grid.equals(grid))) return;
		_grid = grid;

		makeDirty(_slide.getSize(), false);
		repaint();
	}

	public void dispose() {
		// Remove any reference to cached content (so it can be released):
		if (_slideCacheEntry != null) {
			_slideCacheEntry.dispose();
		}

		if (_precacheCacheEntry != null) {
			_precacheCacheEntry.dispose();
		}
		
		// The slide might remain in usage, this graphicsListener not:
		_slide.removeListener(_graphicsListener);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (_clipping == null) return;
		Graphics2D g2d = (Graphics2D) g;
		
		// Draw slide parts that lie outside the source rectangle in gray:
		if (!_clipping.getSource().equals(_slide.getSize())) {
			g2d.transform(_clipping.getTransform());
			g2d.setPaint(new Color(110, 110, 110));
			
			Rectangle2D source = _clipping.getSource();
			Rectangle2D size = _slide.getSize();
			Rectangle2D top = new Rectangle2D.Double(0, 0, size.getWidth(), source.getY());
			Rectangle2D left = new Rectangle2D.Double(0, source.getY(), source.getX(), source.getHeight());
			Rectangle2D right = new Rectangle2D.Double(source.getX() + source.getWidth(), source.getY(), size.getWidth() - source.getWidth() - source.getX(), source.getHeight());
			Rectangle2D bottom = new Rectangle2D.Double(0, source.getY() + source.getHeight(), size.getWidth(), size.getHeight() - source.getHeight() - source.getY());
			g2d.fill(left);
			g2d.fill(right);
			g2d.fill(top);
			g2d.fill(bottom);
			g2d.transform(_clipping.getInverseTransform());
		}
		
		// Paint slide: Use a double buffer if appropriate:
		// Buffer only if priority is high:
		if (_priority > Document.PRIO_THUMBNAIL) {
			
			// Clipping is valid, check whether buffer dimensions changed:
			if ((_buffer == null) ||
				(_clipping.getDestination().getWidth() != _buffer.getWidth()) ||
				(_clipping.getDestination().getHeight() != _buffer.getHeight())) {
				
				// Buffer dimensions changed, create new buffer:
				_buffer = GraphicsEnvironment.getLocalGraphicsEnvironment().
						  getDefaultScreenDevice().getDefaultConfiguration().
						  createCompatibleImage(
							(int)_clipping.getDestination().getWidth(),
							(int)_clipping.getDestination().getHeight());
				
				Graphics2D g2dbuffer = (Graphics2D) _buffer.createGraphics();
				
				g2dbuffer.setColor(Color.GRAY);
				g2dbuffer.fillRect(0, 0, _buffer.getWidth(), _buffer.getHeight());
				g2dbuffer.dispose();
					
				// Whole buffer changed, draw the slide into the new buffer:
				makeDirty(_slide.getSize(), false);
			}
		
			// Buffer exists, but its content has to be redrawn:
			if (_dirty != null) {

				
				// Caching is not available or finished, draw immediately:
				if (_slideCacheEntry == null || _slideCacheEntry.isCached() && _slideCacheEntry.getClipping().equals(_clipping)) {
					Graphics2D g2dbuffer = (Graphics2D) _buffer.createGraphics();
					
					//if (!_reuseBuffer) {

							g2dbuffer.translate(-_clipping.getDestination().getX(), -_clipping.getDestination().getY());
						
							// Redraw full dirty area:
							g2dbuffer.setClip(_clipping.getTransform().createTransformedShape(_dirty));
							_slide.paint(g2dbuffer, _clipping, _grid, 0);
							_bufferSource = _clipping.getSource();
					
					//}
					// This is for performance optimization only!
					// Normal scrolling without zooming allows us to reuse a big part of
					// the buffer, if existent:
					// Commented out: Still contains glitches, especially threading issues:
					/*else if ((_bufferSource != null) &&
						(_clipping.getSource().getWidth() == _bufferSource.getWidth()) &&
						(_clipping.getSource().getHeight() == _bufferSource.getHeight())) {
				
						
						Rectangle dirtyClip = _clipping.getTransform().createTransformedShape(_slide.getSize()).getBounds();
						
						// Translation between old and new source in destination units (pixel):
						int dx = (int) (_clipping.getTransform().getScaleX() * (_bufferSource.getX() - _clipping.getSource().getX()));
						int dy = (int) (_clipping.getTransform().getScaleY() * (_bufferSource.getY() - _clipping.getSource().getY()));
						
						// Copy reusable area:
						//g2dbuffer.setClip(dirtyClip);
						g2dbuffer.copyArea(0, 0, _buffer.getWidth(), _buffer.getHeight(), dx, dy);
						
						g2dbuffer.translate(-_clipping.getDestination().getX(), -_clipping.getDestination().getY());
						int origX = -(int) g2dbuffer.getTransform().getTranslateX();
						int origY = -(int) g2dbuffer.getTransform().getTranslateY();
						
						// Redraw the rest:
						// Top:
						if (dy > 0) {
							Rectangle top = new Rectangle(origX + dx, origY, _buffer.getWidth(), dy);
							g2dbuffer.setClip(dirtyClip.intersection(top));
							_slide.paint(g2dbuffer, _clipping, _grid, 0);
						}
						
						// Left:
						if (dx > 0) {
							Rectangle left = new Rectangle(origX, origY, dx, _buffer.getHeight());
							g2dbuffer.setClip(dirtyClip.intersection(left));
							_slide.paint(g2dbuffer, _clipping, _grid, 0);
						}
						
						// Right:
						if (dx < 0) {
							Rectangle right = new Rectangle(origX + _buffer.getWidth() + dx, 0, origY - dx, _buffer.getHeight());
							g2dbuffer.setClip(dirtyClip.intersection(right));
							_slide.paint(g2dbuffer, _clipping, _grid, 0);
						}
						
						// Bottom:
						if (dy < 0) {
							Rectangle bottom = new Rectangle(origX, origY + _buffer.getHeight() + dy, _buffer.getWidth(), -dy);
							g2dbuffer.setClip(dirtyClip.intersection(bottom));
							_slide.paint(g2dbuffer, _clipping, _grid, 0);
						}

						g2dbuffer.dispose();
						_bufferSource = (Rectangle2D) _clipping.getSource().clone();
					}*/
					g2dbuffer.dispose();
					
					_dirty = null;
				}
			}
			
			// Draw the buffer content:
			if (_buffer != null)
				g.drawImage(_buffer, (int)_clipping.getDestination().getX(), (int)_clipping.getDestination().getY(), null);
		}
		else {
			// Don't use a buffer to save memory:
			// Caching is not available or finished, draw immediately:
			if (_slideCacheEntry == null || _slideCacheEntry.isCached() && _slideCacheEntry.getClipping().equals(_clipping)) {
				_slide.paint((Graphics2D) g, _clipping, _grid, 0);
			}
			else paintPreviewSlide((Graphics2D) g);
		}
				
		// Commented out: Only for debugging purposes:
		//Graphics2D g2d = (Graphics2D) g;
		//g2d.setPaint(new Color((int)(Math.random()*255),0,0, 30));
		//g2d.fill(g.getClip().getBounds());
	}
	
	private void makeDirty(Rectangle2D dirt, boolean reuse) {
		if (reuse && _dirty == null) {
			_reuseBuffer = true;
		}
		else 
			_reuseBuffer = false;
		
		if (_dirty != null) _dirty.add(dirt);
		else _dirty = (Rectangle2D) dirt.clone();
	}
	
	private void paintPreviewSlide(Graphics2D g2d) {
		if (_clipping == null) return;
		
		// Paint slide outline:
		g2d.setPaint(Color.GRAY);
		g2d.fill(_clipping.getDestination());
	}
}