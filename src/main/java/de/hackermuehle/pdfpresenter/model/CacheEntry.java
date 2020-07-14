package de.hackermuehle.pdfpresenter.model;

public interface CacheEntry {
	public void dispose();
	public boolean isCached();
	public ImmutableClipping getClipping();
}
