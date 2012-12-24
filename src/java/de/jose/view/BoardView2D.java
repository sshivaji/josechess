/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.view;

import de.jose.*;
import de.jose.chess.EngUtil;
import de.jose.chess.Move;
import de.jose.chess.Rook;
import de.jose.image.*;
import de.jose.profile.FontEncoding;
import de.jose.profile.UserProfile;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class BoardView2D
		extends BoardView
		implements MouseListener, MouseMotionListener
{

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	background double buffer (we DO NOT use the Java double buffering)	 */
	protected BufferedImage buffer = null;
	/**	left and top inset	 */
	protected Point inset;
	/**	size of one square	 */
	protected int squareSize;
	protected boolean isResizing;
	/**	texture offsets	 */
	protected static int[] textureOffsets;


	/**	randomize texture offsets ?
	 * 	true	no two squares will look the same
	 * 	false	textures are aligned
	 */
	protected static boolean randomTxtrOffset = true;

	static {
		textureOffsets = new int[2*OUTER_BOARD_SIZE];
		Random rnd = new Random(0xfafafb);
		for (int i=0; i<textureOffsets.length; i++)
			textureOffsets[i] = Math.abs(rnd.nextInt());
	}

	protected static final Color SHADOW_64 = new Color(0,0,0,64);

	/**	animation: number of frames per second (roughly)
	 * 	note that processing power is not the limiting factor here;
	 * 	the most determining factor is the scheduling of the Timer thread
	 * */
	protected static final int	FPS	= 100;

	/**	sprite image for sliding and mouse dragging	 */
	protected Board2DSprite sprite1 = null;
	/**	one more for castlings	*/
	protected Board2DSprite sprite2 = null;

	/**	current font for rendering pieces	*/
	protected String currentFont;
	/**	square surfaces	*/
	protected Surface currentLight,currentDark;
	/**	piece surfaces	*/
	protected Surface currentWhite,currentBlack;
	/**	background surface	*/
	protected Surface currentBackground;

	protected boolean forceRedraw = false;
	/**	lock cached images (i.e. prevent them from Garbage collection)	*/
	protected boolean lockImgCache;

	public BoardView2D(IBoardAdapter board, boolean lockImgCache)
	{
		super(board);

		setDoubleBuffered(Version.useDoubleBuffer());	//	we'll use our own buffer

		addMouseListener(this);
		addMouseMotionListener(this);

		inset = new Point();
		this.lockImgCache = lockImgCache;
		recalcSize();
	}

	public void init()
	{
		UserProfile prf = AbstractApplication.theUserProfile;
		Map map = (Map)prf.get("board.images");
		if (map != null)
		{
			Iterator i = map.entrySet().iterator();
            int count = 0;
			while (i.hasNext())
				try {
					Map.Entry mety = (Map.Entry)i.next();
					String key = (String)mety.getKey();

					FontCapture.MapEntry fety = (FontCapture.MapEntry)mety.getValue();
					FontCapture.add(key, fety, lockImgCache);
					count++;
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
//System.out.println(count+" images read");
		}

		updateProfile(prf);
		showCoords(prf.getBoolean("board.coords"));
		flip(prf.getBoolean("board.flip"));
	}

	public void refresh(boolean stopAnimation)	{
		if (stopAnimation) {
			hideAllHints();
			if (sprite1!=null) sprite1.drop();
			if (sprite2!=null) sprite2.drop();
		}
		super.repaint();
	}


	public void activate(boolean active)
	{
		/*	nothing to do - position will be synched in paint()	*/
	}

	public void storeProfile(UserProfile prf)
	{
		//	store image data
		//	TO DO
		Map map = FontCapture.getAllImages(currentFont, squareSize, null, currentWhite, currentBlack);
		Iterator i = map.entrySet().iterator();
		FontCapture.MapEntry fety = null;

		while (i.hasNext()) {
			Map.Entry mety = (Map.Entry)i.next();
			String key = (String)mety.getKey();
			fety = (FontCapture.MapEntry)mety.getValue();
			map.put(key,fety);
		}
		if (fety != null)
			prf.set("board.images",map);
	}

	/**
	 *	paint the board
	 *
	 * this method <i>might</i> paint into a double buffer
	 * (although we have turned double buffering off)
	 *
	 * 	we have to take special care that the moving sprites are painted
	 * 	into the same buffer (otherwise, we would see a flicker)
	 *
	 *  TODO think about using this double buffer directly (instead of creating our own buffer)
	 *  OR create the buffer in graphics memory so that copying is faster (VolatileImage)
	 * */
	protected void paintComponent(Graphics g)
	{
		if (isResizing)
			paintUgly(g);
/*
		else if (sizeChanged()) {
			paintUgly(g);
			recalcSize();
			buffer = null;
			repaint(2000);
		}
*/
		else {
			/*	paint off screen	*/
            prepareImage((Graphics2D)g);
            /*	then copy	*/
			g.drawImage(buffer,0,0,null);

			if (sprite1.isMoving()) sprite1.paint(g);
			if (sprite2.isMoving()) sprite2.paint(g);
		}
	}

	/**
	 * called during resizing; live resizing would be too expensive (FontCapture!)
	 * that's why we simply paint a scaled image. after resizing, the correct image is painted again
	 */
	protected void paintUgly(Graphics g)
	{
		Rectangle src = new Rectangle(0,0,buffer.getWidth(),buffer.getHeight());
		Rectangle screen = new Rectangle(0,0,getWidth(),getHeight());
		Rectangle dst = new Rectangle(0,0,getWidth(),getHeight());

		double scale = (double)calcSquareSize(screen.width,screen.height) / (double)squareSize;

		dst.width = (int)Math.round(src.width*scale);
		dst.height = (int)Math.round(src.height*scale);

		//  center on screen
		dst.x = (screen.width-dst.width)/2;
		dst.y = (screen.height-dst.height)/2;

		//  fill empty area with background pattern
		Area clip = new Area(screen);
		clip.subtract(new Area(dst));

		if (!clip.isEmpty())
		{
			Shape oldClip = g.getClip();
			g.setClip(clip);
			if (currentBackground.useTexture()) {
				Image txtr = TextureCache.getTexture(currentBackground.texture, TextureCache.LEVEL_MAX);
				TextureCache.paintTexture(g, screen.x,screen.y,screen.width,screen.height, txtr);
			}
			else {
				Graphics2D g2 = (Graphics2D)g;
				g2.setPaint(currentBackground.getPaint(getWidth(),getHeight()));
				g2.fillRect(screen.x,screen.y,screen.width,screen.height);
			}
			g.setClip(oldClip);
		}

		ImgUtil.copy(buffer, src.x,src.y,src.width,src.height,
		        g, dst.x,dst.y,dst.width,dst.height);
		/** ImgUtil.copy adjusts negative coords and avoids painting outside the graphics port
		 *  (because Java2D don't like it)
		 */
	}

	/**	calculate square size and inset after resize	 */
	public void recalcSize()
	{
		float div = showCoords ? 8.4f : 8.0f;
		squareSize = (int)((float)Math.min(getWidth()-4, getHeight()-4) / div);
		inset.x = (int)(getWidth()-8*squareSize) / 2;
		inset.y = (int)(getHeight()-div*squareSize) / 2;
	}

	private int calcSquareSize(int imgWidth, int imgHeight)
	{
		float div = showCoords ? 8.4f : 8.0f;
		return (int)((float)Math.min(getWidth()-4, getHeight()-4) / div);
	}

	public Graphics2D getBufferGraphics()
	{
		return (Graphics2D)buffer.getGraphics();
	}

	/**	repaint one square	 */
	public void updateOne(Graphics2D g, int file, int row)
	{
		prepareImage(g);

		paintImmediate(g,EngUtil.square(file,row));
	}

	protected void paintImmediate(Graphics2D g, int square)
	{
		Point p = origin(square);
		g.drawImage(buffer, p.x,p.y, p.x+squareSize, p.y+squareSize,
					p.x,p.y, p.x+squareSize, p.y+squareSize, null);
	}

	public Point getScreenLocation (int square)
	{
		Point pt = origin(square);
		SwingUtilities.convertPointToScreen(pt,this);
		return pt;
	}

	/**
	 *	@param p a point on the screen
	 *	@return the square index (or 0, if off the board)
	 * */
	public final int findSquare(Point p)
	{
		return findSquare(p.x,p.y);
	}

	/**
	 *	@param x
     *  @param y a point on the screen
	 *	@return the square index (or 0, if off the board)
	 * */

	public int findSquare(int x, int y)
	{
		if (squareSize==0 || x<inset.x || y<inset.y)
			return 0;

		int file = (x-inset.x)/squareSize;
		int row = (y-inset.y)/squareSize;

		if (flipped) {
			file = FILE_H-file;
			row = ROW_1+row;
		}
		else {
			file = FILE_A+file;
			row = ROW_8-row;
		}

		if (EngUtil.innerSquare(file,row))
			return EngUtil.square(file,row);
		else
			return 0;
	}

	public int[] findPreferredSquares(Point mousePoint, Rectangle spriteBounds)
	{
		int[] result = new int[6];
		/**	1. choice: mouse location	*/
		result[0] = findSquare(mousePoint);
		/**	2. choice: intersection with sprite (sorted by area)	*/
		if (spriteBounds!=null) {
			result[1] = findSquare(ViewUtil.center(spriteBounds));
			/**	next choice: any intersection with sprite	*/
			result[2] = findSquare(ViewUtil.topLeft(spriteBounds));
			result[3] = findSquare(ViewUtil.topRight(spriteBounds));
			result[4] = findSquare(ViewUtil.bottomLeft(spriteBounds));
			result[5] = findSquare(ViewUtil.bottomRight(spriteBounds));
			/* remove duplicates */
			for (int i=0; i < 5; i++)
				for (int j=i+1; j < 6; j++)
					if (result[j]==result[i]) result[j]=0;
		}
		return result;
	}


	/**
	 *	@return the screen location of the upper left corner of a given square
	 * */
	protected Point origin(int file, int row)
	{
		if (flipped)
			return new Point((FILE_H-file) * squareSize + inset.x,
						 (row-ROW_1) * squareSize + inset.y);
		else
			return new Point((file-FILE_A) * squareSize + inset.x,
						 (ROW_8-row) * squareSize + inset.y);
	}

	/**
	 *	@return the screen location of the upper left corner of a given square
	 * */
	protected final Point origin(int square)
	{
		return origin(EngUtil.fileOf(square),
					  EngUtil.rowOf(square));
	}

    protected final Point lowerRight(int square)
    {
        Point p = origin(square);
        p.x += squareSize;
        p.y += squareSize;
        return p;
    }

    protected final Point center(int square)
    {
        Point p = origin(square);
        p.x += squareSize/2;
        p.y += squareSize/2;
        return p;
    }


	/**
	 *	move (slide) a piece from one square to another
	 *
	 * @param duration duration of move in millisecs
	 * @param frameRate if > 0: desired frames per second;
	 *					if <= 0: draw synchronously
	 * */
	public void move(int startSquare,
					 int piece, int endSquare,
					 float duration, int frameRate)
	{
		sprite1.animate(startSquare,endSquare,
						piece, EMPTY,
						calcMillis(duration),frameRate);
	}


	public void move(Move mv, float time)
	{
		long millis = calcMillis(time);
		int piece = mv.moving.piece();

		if (showAnimationHints) showHint(mv, millis, ANIM_HINT_COLOR);

		/*	set up sprite */
		sprite1.pickUp(mv.from, piece);

		finishMove(mv, millis);
	}

	protected void doShowHint(Hint hnt)
	{
		forceRedraw = true;
		repaint();
	}

	protected void doHideHint(Hint hnt)
	{
		forceRedraw = true;
		repaint();
	}

	protected void doHideAllHints(int count)
	{
		forceRedraw = true;
		repaint();
	}

	public synchronized void synch(boolean redraw)
	{
		/*	synch with actual position	*/
		Graphics2D g = getBufferGraphics();

		for (int file = FILE_A; file <= FILE_H; file++)
			for (int row = ROW_1; row <= ROW_8; row++)
			{
				int square = EngUtil.square(file,row);
				if (square==sprite1.src || square==sprite2.src ||
					square==sprite1.dst || square==sprite2.dst)
					continue;
				/**
				 * squares that take part in animation are not synched
				 * they will be synched by Board2DSprite AFTER the animation has finished
				 * TODO could this conflict with hint arrows ?
				 */

				int newPiece = board.pieceAt(square);
				if (redraw || newPiece != pieceAt(square))
					set(g,newPiece,square);
			}

		if (redraw) {
			sprite1.updateBuffer();
			sprite2.updateBuffer();
		}
	}


	public void startContinuousResize()
	{
		/** while continous resizing:
		 *  don't compute piece images, it's just too expensive
		 *  use the old images scaled down
		 */
		isResizing = true;
	}

	public void finishContinuousResize()
	{
		/**
		 * return to normal painting
		 */
		isResizing = false;
		repaint();
	}

	private void paintHints()
	{
		if (hints.isEmpty()) return;

		for (int i=0; i < hints.size(); i++)
		{
			Hint hnt =(Hint)hints.get(i);
			Graphics2D g = getBufferGraphics();
			paintArrow(g, center(hnt.from), center(hnt.to),
			        squareSize/6, hnt.color);
		}

		sprite1.updateBuffer();
		sprite2.updateBuffer();
	}

	public static final int[] createArrowXCoordinates(int length, int width, int tip)
	{
		int[] x = {  0,          0, length-tip, length-tip, length, length-tip, length-tip, };
		return x;
	}

	public static final int[] craeteArrowYCoordinates(int length, int width, int tip)
	{
		int[] y = { -width, +width,     +width,        tip,      0,       -tip,     -width, };
		return y;
	}

	public static final float[] createArrowFloatCoordinates(float length, float width, float tip)
	{
		float[] xyz = {
			0,                  -width,         0,
			length-tip,         -width,         0,
			length-tip,         -tip,           0,
			length,             0,              0,
			length-tip,         tip,            0,
			length-tip,         +width,         0,
			0,                  +width,         0,
		};
		return xyz;
	}

	private void paintArrow(Graphics2D g, Point p1, Point p2,
	                        int width, Color color)
	{
		/** set up a a polygon of normal width */
		int length = (int)Math.round(p1.distance(p2));
		int tip = 2*width;

		int[] x = createArrowXCoordinates(length,width,tip);
		int[] y = craeteArrowYCoordinates(length,width,tip);

		/** rotate into place   */
		AffineTransform oldTransform = g.getTransform();
		AffineTransform tf = (AffineTransform)oldTransform.clone();
		tf.translate(p1.x,p1.y);
//		tf.scale(box.width/100.0, box.height/100.0);
		double angle = Math.atan2(p2.y-p1.y, p2.x-p1.x);
		tf.rotate(angle);    //  TODO
		g.setTransform(tf);

		g.setColor(color);
		g.fillPolygon(x,y, x.length);

		g.setColor(Color.black);
		g.drawPolygon(x,y, x.length);

		g.setTransform(oldTransform);
	}

	protected void finishMove(Move mv, long millis)
	{
		int piece = sprite1.pc;
		if (mv.isPromotion())
			piece = mv.getPromotionPiece() + EngUtil.colorOf(piece);

		switch (mv.castlingMask()) {    //  FRC
		case WHITE_KINGS_CASTLING:
			sprite2.pickUp(mv.to,WHITE_ROOK); break;
		case WHITE_QUEENS_CASTLING:
			sprite2.pickUp(mv.to, WHITE_ROOK); break;
		case BLACK_KINGS_CASTLING:
			sprite2.pickUp(mv.to, BLACK_ROOK); break;
		case BLACK_QUEENS_CASTLING:
			sprite2.pickUp(mv.to, BLACK_ROOK); break;
		}

		sprite1.dropTo(mv, piece, millis,FPS, !mv.isCastling());

		switch (mv.castlingMask()) {    //  FRC
		case WHITE_KINGS_CASTLING:
			sprite2.dropTo(F1,WHITE_ROOK, calcMillis(0.4f),FPS,false); break;
		case WHITE_QUEENS_CASTLING:
			sprite2.dropTo(D1, WHITE_ROOK, calcMillis(0.4f),FPS,false); break;
		case BLACK_KINGS_CASTLING:
			sprite2.dropTo(F8, BLACK_ROOK, calcMillis(0.4f),FPS,false); break;
		case BLACK_QUEENS_CASTLING:
			sprite2.dropTo(D8, BLACK_ROOK, calcMillis(0.4f),FPS,false); break;
		}

		if (mv.isEnPassant())
			set(EMPTY, mv.getEnPassantSquare());
	}

	public void doFlip(boolean on)
	{
		recalcSize();
		forceRedraw = true;
	}

	public void doShowCoords(boolean on)
	{
		recalcSize();
		forceRedraw = true;
	}

	public void updateProfile(UserProfile prf)
	{
		String user_font = prf.getString("font.diagram");

		if (currentFont==null || !currentFont.equals(user_font))
		{
			/*	font has changed	*/
			currentFont = user_font;
			forceRedraw = true;
		}

		if (!prf.get("board.surface.light").equals(currentLight))
		{
			currentLight = (Surface)prf.get("board.surface.light");
			forceRedraw = true;
		}

		if (!prf.get("board.surface.dark").equals(currentDark))
		{
			currentDark = (Surface)prf.get("board.surface.dark");
			forceRedraw = true;
		}


		if (!prf.get("board.surface.white").equals(currentWhite))
		{
			currentWhite = (Surface)prf.get("board.surface.white");
			forceRedraw = true;
		}

		if (!prf.get("board.surface.black").equals(currentBlack))
		{
			currentBlack = (Surface)prf.get("board.surface.black");
			forceRedraw = true;
		}

		if (!prf.get("board.surface.background").equals(currentBackground))
		{
			currentBackground = (Surface)prf.get("board.surface.background");
			forceRedraw = true;
		}

		showAnimationHints = prf.getBoolean("board.animation.hints");
	}

	private boolean sizeChanged() {
		return buffer!=null &&
			(getWidth() != buffer.getWidth() ||
			 getHeight() != buffer.getHeight());
	}

	private BufferedImage createBuffer(Graphics2D screeng, int width, int height)
	{
		return screeng.getDeviceConfiguration().createCompatibleImage(width,height);
		/**
		 * TODO think about using VolatileImage (reside in graphic card memory)
		 * but have to handle the case when contents is lost
		 */
//		return screeng.getDeviceConfiguration().createCompatibleVolatileImage(width,height);
	}

	/**
	 *	prepare the off-screen image
	 * */
	protected void prepareImage(Graphics2D screeng)
	{
		boolean redraw = forceRedraw;
		forceRedraw = false;

		if (buffer==null || sizeChanged())
		{
			/*	size has changed	*/
			recalcSize();
			buffer = createBuffer(screeng, getWidth(),getHeight());

			if (sprite1==null)
				sprite1 = new Board2DSprite(buffer);
			else
				sprite1.resetBackground(buffer);

			if (sprite2==null)
				sprite2 = new Board2DSprite(buffer);
			else
				sprite2.resetBackground(buffer);

			redraw = true;
		}

		Graphics2D g = (Graphics2D)buffer.getGraphics();
		ImgUtil.setRenderingHints(g);

		int boardSize = 8*squareSize;
		int x2 = inset.x+boardSize;
		int y2 = inset.y+boardSize;

		if (redraw) {
			if (lockImgCache) FontCapture.unlock();	//	make outdated images available for gc

			if (currentBackground.useTexture()) {
				Image txtr = TextureCache.getTexture(currentBackground.texture, TextureCache.LEVEL_MAX);
				TextureCache.paintTexture(g, 0,0, getWidth(), inset.y, txtr);
				TextureCache.paintTexture(g, 0,inset.y, inset.x, boardSize, txtr);
				TextureCache.paintTexture(g, x2,inset.y, getWidth()-x2, boardSize, txtr);
				TextureCache.paintTexture(g, 0,y2, getWidth(), getHeight()-y2, txtr);
			}
			else {
				g.setPaint(currentBackground.getPaint(getWidth(),getHeight()));
				g.fillRect(0,0, getWidth(), inset.y);
				g.fillRect(0,inset.y, inset.x, boardSize);
				g.fillRect(x2,inset.y, getWidth()-x2, boardSize);
				g.fillRect(0,y2, getWidth(), getHeight()-y2);
			}


			g.setColor(Color.black);
	//		g.drawRect(inset.x-2, inset.y-2, boardSize+4,boardSize+4);
			Border b = new SoftBevelBorder(BevelBorder.RAISED);
			b.paintBorder(this,g, inset.x-2, inset.y-2, boardSize+4,boardSize+4);

			if (showCoords) {
                /** draw coordinates    */
				char[] c = new char[2];
				int fontSize = (int)(squareSize*0.3f);
				Font f = new Font("SansSerif",Font.PLAIN, fontSize);
				g.setFont(f);
				FontMetrics fmx = g.getFontMetrics();

				int drop = Math.max(squareSize/48,1);

				for (int i=0; i<8; i++) {
					c[0] = (char)(flipped ? ('1'+i):('8'-i));
					int x0 = inset.x - fmx.charWidth(c[0])*9/8 - 4;
					int y0 = inset.y + squareSize*i + (squareSize+fmx.getAscent()) / 2;

					c[1] = (char)(flipped ? ('h'-i):('a'+i));
					int x1 = inset.x + squareSize*i + (squareSize-fmx.charWidth(c[1])) / 2;
					int y1 = inset.y + 8*squareSize + fmx.getAscent();

					g.setColor(SHADOW_64);
					g.drawChars(c,0,1, x0+drop,y0+drop);
					g.drawChars(c,1,1, x1+drop,y1+drop);

					if (currentBackground.isDark())
						g.setColor(Color.white);
					else
						g.setColor(Color.black);
					g.drawChars(c,0,1, x0,y0);
					g.drawChars(c,1,1, x1,y1);
				}
            }
		}

		synch(redraw);

		paintHints();

		paintHook(redraw);
	}


	protected void paintHook(boolean redraw)
	{
		//	for derived classes
	}



	public final BufferedImage getPieceImage(int piece, Rectangle bounds)
	{
		return getPieceImage(currentFont,squareSize, piece,
							currentWhite, currentBlack, 
							bounds,lockImgCache);
	}

	public  static BufferedImage getPieceImage(String font, int size, int piece,
									   Surface white, Surface black,
									   Rectangle bounds, boolean lock)
	{
		String c = FontEncoding.get(font,piece);
		if (c==null) c = "?";

		try {
			return FontCapture.getImage(font, size, c, white, black,
					bounds, FontCapture.SYNCH, lock);

		} catch (FileNotFoundException e) {
			Application.error(e);
			return null;
		}
	}

	public ImageIcon getPopupIcon(int piece)
	{
		return new ImageIcon(getPieceImage(piece,null));
	}

	/**
	 * set a piece at a given square and draw it immediately
	 */
    public void set(Graphics2D g, int piece, int square)
    {
		paint(g,piece,square);
		pieces[square] = piece;
	}

	protected void paint(Graphics2D g, int piece, int square)
	{
		drawBackground(g, square);

		if (piece != EMPTY) {
			Point p = origin(square);
			Rectangle imgBounds = new Rectangle();
			Image img = getPieceImage(piece,imgBounds);
			g.drawImage(img, p.x+imgBounds.x, p.y+imgBounds.y, null);
		}
	}

	protected Surface getBackground(int square)
	{
		boolean light = EngUtil.isLightSquare(square);
		return light ? currentLight:currentDark;
	}

	protected void drawBackground(Graphics2D g, int square)
	{
		Point p = origin(square);
		Surface surf = getBackground(square);
		if (surf.useTexture())
			try {
				if (randomTxtrOffset)
					TextureCache.paintTexture(g, p.x,p.y, squareSize,squareSize,
								 surf.texture, TextureCache.LEVEL_MAX,
								 textureOffsets[2*square],
								 textureOffsets[2*square+1]);
				else
					TextureCache.paintTexture(g, p.x,p.y, squareSize,squareSize,
								surf.texture, TextureCache.LEVEL_MAX,
								p.x, p.y);
			} catch (IOException fnex) {
				//	what can we do ?
			}
		else {
			/*	color	*/
			g.setPaint(surf.getPaint(p.x,p.y, squareSize,squareSize));
			g.fillRect(p.x,p.y, squareSize,squareSize);
		}
	}


	public void captureImage(MessageListener callbackTarget, boolean transparent)
	{
		//  create a copy
		BufferedImage img;
		forceRedraw = true;

		Surface oldBackground = currentBackground;
		BufferedImage oldBuffer = buffer;
		boolean wasRedraw = forceRedraw;

		try {
			if (transparent) {
				buffer = img = new BufferedImage(buffer.getWidth(), buffer.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
				currentBackground = Surface.newColor(new Color(0,0,0,0));
			}
			else
				buffer = img = getGraphicsConfiguration().createCompatibleImage(buffer.getWidth(), buffer.getHeight());

			Graphics2D g = (Graphics2D)img.getGraphics();
			prepareImage(g);

			if (sprite1.isMoving()) sprite1.paint(g);
			if (sprite2.isMoving()) sprite2.paint(g);

		} finally {
			currentBackground = oldBackground;
			buffer = oldBuffer;
			forceRedraw = wasRedraw;
		}

		callbackTarget.handleMessage(this,MESSAGE_CAPTURE_IMAGE,img);
	}

	//-------------------------------------------------------------------------------
	//	Interface MouseListener
	//-------------------------------------------------------------------------------


	public void mouseClicked(MouseEvent e)
	{
/*      no need to intercept mouseClicked. mousePressed/Released is better  */
	}

	public void mousePressed(MouseEvent e)
	{
		if (! ContextMenu.isTrigger(e) && ! promoPopupShowing)
			mouseStart(e.getPoint());
	}

	public void mouseEntered(MouseEvent e)
	{
		mouseMovement(e.getPoint());
	}

	public void mouseExited(MouseEvent e)
	{
		setCursor(Cursor.getDefaultCursor());
	}

	public void mouseReleased(MouseEvent e)
	{
		if (! ContextMenu.isTrigger(e) && ! promoPopupShowing)
			mouseEnd(e.getPoint());
	}




	/**
	 * called when the mouse is pressed
	 */
	public void mouseStart(Point startPoint)
	{
		int square = findSquare(mouseStartPoint = startPoint);

		if (board.canMove(square)) {
			mouseStartSquare = square;
			int piece = board.pieceAt(mouseStartSquare);
			sprite1.pickUp(mouseStartSquare,piece);
		}
		else	/*	piece can't move: ignore	*/
			mouseStartSquare = 0;
	}

	public void mouseEnd(Point p)
	{
		mouseEnd(p,0);
	}

	public void promotionPopup(int promoPiece)
	{
		if (promoPiece <= 0)
			mouseEnd(origin(mouseMove.from), 0);				//	cancel move
		else
			mouseEnd(origin(mouseMove.to), promoPiece);			//	make move
	}

	/**
	 * called when the mouse is released
	 */
	private void mouseEnd(Point endPoint, int promoPiece)
	{
		if (mouseStartSquare==0) return;	//	irrelevant mouse click

		int[] destSquare = findPreferredSquares(endPoint,
							sprite1.isMoving() ? sprite1.getCurrentBounds():null);

		int i=0;
		for (i=0; i<destSquare.length; i++)
		{
			if (destSquare[i]==0)
				continue;

			if (destSquare[i] == mouseStartSquare)
			{	//	piece dropped (touch-move rule not enforced!)
				sprite1.dropTo(mouseStartSquare, board.pieceAt(mouseStartSquare), 100,FPS,true);
				break;
			}

			//	end dragging
			mouseMove = new Move(mouseStartSquare,destSquare[i]);
			mouseMove.setPromotionPiece(promoPiece);

			if (board.isLegal(mouseMove)) {
				/*	legal move	*/
				finishMove(mouseMove, calcMillis(0.1f));
				board.userMove(mouseMove);
				break;
			}

			if (couldBePromotion(mouseMove)) {
				/*	show popup	*/
				showPromotionPopup(board.movesNext(), origin(mouseMove.to));
				/*	when the user selects an item, mouseEnd will be called again	*/
				return;	//	important: keep mouseMove
			}
		}

		/*	illegal move	*/
		if (i >= destSquare.length) {
			Sound.play("sound.error");
			sprite1.dropTo(mouseStartSquare, board.pieceAt(mouseStartSquare), 500,FPS,true);
		}

		mouseStartSquare = 0;
		mouseMove = null;
	}

	protected class Board2DSprite extends Sprite
	{
		/**	source, start square	*/
		int src;
		/**	moving piece	*/
		int pc;
		/**	destination square (may be 0)	*/
		int dst;
		/**	destination piece (may be different from moving piece)	*/
		int dstpc;
		/** hint move   */
		Move hint;
		/** HACK clear original square after drop ? */
		boolean clearOnDrop = true;

		Board2DSprite(BufferedImage background)
		{
			Rectangle imgBounds = new Rectangle();
			BufferedImage img = getPieceImage(WHITE_QUEEN,imgBounds);

			init(background,getGraphics2D(), BoardView2D.this.getBounds(),
				img, new Point(0,0), imgBounds.x, imgBounds.y);
		}

		public boolean isMoving()		{ return src!=0; }

		public void drop()
		{
			if (isAnimating()) {
				finishAnimation();  //  calls onAnimationFinis(), drop(), again !!
				return;
			}
			if (src!=0) {
				if (src!=dst) {
					if (clearOnDrop) BoardView2D.this.set(EMPTY,src);
//					BoardView2D.this.set(board.pieceAt(src),src);
					//  TODO This line was removed for a reason, but why ?
					BoardView2D.this.paintImmediate(getGraphics2D(),src);
				}
				src = 0;
				pc = 0;
			}
			if (dst!=0) {
				BoardView2D.this.set(dstpc,dst);
				BoardView2D.this.paintImmediate(getGraphics2D(),dst);
				dst = 0;
				dstpc = 0;
			}
		}

		public void animate(int startSquare, int endSquare,
							int movingPiece, int destPiece,
							long duration, int frameRate)
		{
			pickUp(startSquare,movingPiece);
			dropTo(endSquare,destPiece, duration,frameRate,true);
		}

		public void pickUp(int startSquare, int movingPiece)
		{
			drop();

			src = startSquare;
			pc = movingPiece;

			dst = 0;
			dstpc = 0;

			set(EMPTY,startSquare);

			Rectangle imgBounds = new Rectangle();
			BufferedImage img = getPieceImage(pc,imgBounds);

			init(buffer,getGraphics2D(), BoardView2D.this.getBounds(),
					img, origin(src), imgBounds.x, imgBounds.y);
		}

		public void dropTo(Move mv, int destPiece, long duration, int frameRate, boolean clear)
		{
			hint = mv;
			switch (mv.castlingMask())
			{
			case WHITE_KINGS_CASTLING:
				dropTo(G1, destPiece,duration,frameRate,clear);  //  FRC
				break;
			case WHITE_QUEENS_CASTLING:
				dropTo(C1, destPiece,duration,frameRate,clear);  //  FRC
				break;
			case BLACK_KINGS_CASTLING:
				dropTo(G8, destPiece,duration,frameRate,clear);  //  FRC
				break;
			case BLACK_QUEENS_CASTLING:
				dropTo(C8, destPiece,duration,frameRate,clear);  //  FRC
				break;
			default:
				dropTo(mv.to, destPiece,duration,frameRate,clear);
				break;
			}
		}

		public void dropTo(int endSquare, int destPiece, long duration, int frameRate, boolean clear)
		{
			dst = endSquare;
			dstpc = destPiece;
			clearOnDrop = clear;

			moveTo(origin(dst), duration,frameRate);
		}


		/**
		 * call back when animation is finished
		 */
		public void onAnimationFinish()
		{
			drop();
		}
	}

	//-------------------------------------------------------------------------------
	//	Interface MouseMotionListener
	//-------------------------------------------------------------------------------

	public void mouseDragged(MouseEvent e)
	{
		/*	drag along	*/
		if ((mouseStartSquare!=0) && sprite1.isMoving()) {
			Point orig = origin(mouseStartSquare);
			Point pt = e.getPoint();
			int dx = pt.x - mouseStartPoint.x;
			int dy = pt.y - mouseStartPoint.y;

			sprite1.moveTo(orig.x+dx, orig.y+dy);
		}
	}

	public void mouseMoved(MouseEvent e)
	{
		mouseMovement(e.getPoint());
	}

	public void mouseMovement(Point p)
	{
		int square = findSquare(p);

		if (board.canMove(square))
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		else
			setCursor(Cursor.getDefaultCursor());
	}


}
