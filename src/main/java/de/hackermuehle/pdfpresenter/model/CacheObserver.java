package de.hackermuehle.pdfpresenter.model;

public interface CacheObserver {
	public void notify(CacheEntry cacheEntry, String msg);
}