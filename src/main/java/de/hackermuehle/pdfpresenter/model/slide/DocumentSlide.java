package de.hackermuehle.pdfpresenter.model.slide;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.hackermuehle.pdfpresenter.model.CacheObserver;
import de.hackermuehle.pdfpresenter.model.Clipping;
import de.hackermuehle.pdfpresenter.model.ImmutableClipping;
import de.hackermuehle.pdfpresenter.model.document.Document;
import de.hackermuehle.pdfpresenter.model.document.DocumentCacheEntry;

/**
 * A document slide is a standard slide with a background. The background is a
 * page of the given document.
 */
public class DocumentSlide extends Slide {
	private Document _document;
	private int _pageNr;
	
	/**
	 * Constructs and initializes a DocumentSlide.
	 * 
	 * @param document	Document that contains the page used as the background
	 * @param pageNr	Page number of the document that identifies the page
	 */
	public DocumentSlide(Document document, int pageNr) {
		_document = document;
		_pageNr = pageNr;
		setSize(_document.getBounds(pageNr));
	}
	
	/**
	 * Generates a cache entry for this slide and the given clipping.
	 * Effectively generates a cache entry for the document page used as the 
	 * background of this slide. Subsequent calls to {@link Slide#paint(
	 * Graphics2D, Clipping, Grid, int) Slide.paint(...)} with the 
	 * same clipping will probably be much faster, depending on the document.
	 * 
	 * @param clipping {@link de.hackermuehle.pdfpresenter.model.document.Document#cache(int, Clipping, int, CacheObserver) Document.cache(...)}
	 * @param priority {@link de.hackermuehle.pdfpresenter.model.document.Document#cache(int, Clipping, int, CacheObserver) Document.cache(...)}
	 * @param cacheObserver {@link de.hackermuehle.pdfpresenter.model.document.Document#cache(int, Clipping, int, CacheObserver) Document.cache(...)}
	 * @return A SlideCacheEntry or null if the document has no caching abilities
	 */
	public SlideCacheEntry cache(final Clipping clipping, final Integer priority, final CacheObserver observer) {
		final DocumentCacheEntry documentCacheEntry = _document.cache(_pageNr, clipping, priority, observer);
		
		if (documentCacheEntry != null) {
			return new SlideCacheEntry() {
				private DocumentCacheEntry _entry = documentCacheEntry;
				
				@Override
				public void dispose() {
					_entry.dispose();
				}
				
				@Override
				public void update(Clipping clipping, int priority, CacheObserver observer) {
					_entry.update(_pageNr, clipping, priority, observer);
				}
				
				@Override
				public boolean isCached() {
					return _entry.isCached();
				}
				
				@Override
				public ImmutableClipping getClipping() {
					return _entry.getClipping();
				}
			};
		}
		return null;
	}

	/**
	 * @return The size of the document page used as background
	 */
	@Override
	public Rectangle2D getDefaultSize() {
		return _document.getBounds(_pageNr);
	}
	
	@Override
	protected void paintBackground(Graphics2D g2d, Clipping clipping, int revision) {;
		_document.paint(g2d, _pageNr, clipping);
	}
}
