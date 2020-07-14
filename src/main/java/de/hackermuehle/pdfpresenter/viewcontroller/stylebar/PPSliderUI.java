package de.hackermuehle.pdfpresenter.viewcontroller.stylebar;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.imageio.ImageIO;
import javax.swing.JSlider;


public class PPSliderUI extends javax.swing.plaf.basic.BasicSliderUI
{
	static final int THUMB_DIAMETER = 40;
	static final int TRACK_WIDTH = (int) ((double) THUMB_DIAMETER * 0.4);
	
	private static Image _thumbImage;
	private static Image _trackHead;
	private static Image _trackBody;
	private static Image _trackTail;
	
	private int _widthDelta = Integer.MIN_VALUE;
	private double _trackZoomRatio;
	
	private int _heightTrackHead;
	private int _heightTrackBody;
	private int _heightTrackTail;

	static {
		try {
	        _thumbImage = ImageIO.read(PPSliderUI.class.getResource("/thumb.png"));
	        _trackHead = ImageIO.read(PPSliderUI.class.getResource("/trackhead.png"));
	        _trackBody = ImageIO.read(PPSliderUI.class.getResource("/trackbody.png"));
	        _trackTail = ImageIO.read(PPSliderUI.class.getResource("/tracktail.png"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public PPSliderUI(JSlider b) {
		super(b);
		
		_trackZoomRatio = (double) TRACK_WIDTH / (double) _trackBody.getWidth(null);
		
		_heightTrackHead = (int) (_trackHead.getHeight(null) * _trackZoomRatio);
		_heightTrackBody = (int) (_trackBody.getHeight(null) * _trackZoomRatio);
		_heightTrackTail = (int) (_trackTail.getHeight(null) * _trackZoomRatio);
	}

	@Override
    protected Dimension getThumbSize() {
        Dimension size = new Dimension();

        size.width = THUMB_DIAMETER;
        size.height = THUMB_DIAMETER;

        return size;
    }


	public void paintThumb(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		
		Rectangle thumb = thumbRect;

		if (_widthDelta == Integer.MIN_VALUE) {
			_widthDelta = THUMB_DIAMETER - thumb.width;
		}
		
		thumb.width = THUMB_DIAMETER;
		thumb.height = THUMB_DIAMETER;

		g2d.drawImage(_thumbImage, thumb.x - _widthDelta / 2, thumb.y - _widthDelta / 2, 
				thumb.width, thumb.height , null);

	}

	public void paintTrack(Graphics g) {
		
		Graphics2D g2d = (Graphics2D) g;
				
		// 6 & 3 calculable?
		int remainingHeight = slider.getHeight() - 6;
		int height = 3;
		
				
		int x = (slider.getWidth() - TRACK_WIDTH) / 2;
		
		if (remainingHeight > _heightTrackHead + _heightTrackBody + _heightTrackTail) {
			// Head of slider
			g2d.drawImage(_trackHead, x, height, 
					TRACK_WIDTH, _heightTrackHead, null);
			height += _heightTrackHead;
			remainingHeight -= _heightTrackHead;
			
			// Body of slider
			int remainingBodyHeight = remainingHeight - _heightTrackTail;
			while (remainingBodyHeight > 0) {
				if (remainingBodyHeight >= _heightTrackBody) {
					// Draw full height
					g2d.drawImage(_trackBody, x, height, 
							TRACK_WIDTH, _heightTrackBody, null);
					height += _heightTrackBody;
					remainingHeight -= _heightTrackBody;
					remainingBodyHeight -= _heightTrackBody;
				} else {
					// Draw cropped
					g2d.drawImage(_trackBody, x, height, 
							TRACK_WIDTH, remainingBodyHeight, null);
					
					height += remainingBodyHeight;
					remainingHeight -= remainingBodyHeight;
					remainingBodyHeight = 0;
				}
			}
			
			// Tail of slider
			g2d.drawImage(_trackTail, x, height, 
					TRACK_WIDTH, _heightTrackTail, null);
			
		} else {
			// Only draw body
		}

	}
}

