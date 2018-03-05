//package com.wowza.wms.plugin.transcoderoverlays;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import com.wowza.util.SystemUtils;

/**
 * Helper class for drawing an overlay image
 *
 */
public class OverlayImage {
	
	
	private int currentOpacity=100;

	//done so memory allocations are minimized
	private Map<Double, BufferedImage> finalBufferedImageMap = new HashMap<Double, BufferedImage>();
	private Map<Double, byte[]> tempBufferMap = new  HashMap<Double, byte[]>();
	private Map<String, String> envMap = new HashMap<String, String>();

	private int xPos=0;
	private int yPos=0;
	private int width = 0;
	private int height = 0;

	private AnimationEvents faddingEvents = new AnimationEvents();
	private AnimationEvents imageRotateEvents = new AnimationEvents();
	private AnimationEvents xPosMovementEvents = new AnimationEvents();
	private AnimationEvents yPosMovementEvents = new AnimationEvents();

	private List<OverlayImage> childrenList = new ArrayList<OverlayImage>();
	private ImageAttributes myImage = null;
	private TextAttributes myText = null;

/**
 * Create an OverlayImage with specified height, width and opacity relative to its parent.
 * @param x - x position relative to its parent.  If no parent it is relative to screen.
 * @param y - y position relative to its parent.  If no parent it is relative to screen.
 * @param width - width of the image to be created.
 * @param height - height of the image to be created.
 * @param opacity - opacity of the image.  (0=invisible, 100=visible)
 */
	public OverlayImage(int x, int y, int width, int height, int opacity)
	{
		envMap.put("com.wowza.wms.plugin.transcoderoverlays.overlayimage.step", "1");	 
		this.currentOpacity = opacity;
		this.xPos = x;
		this.yPos=y;
		this.width = width;
		this.height = height;
		tempBufferMap.put(1.0,new byte[width*height*4]);
		finalBufferedImageMap.put(1.0, new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR));
	}

/**
 * Create an OverlayImage with text
 * @param txt - text to be displayed.
 * @param size - point size of the Font.
 * @param fName - font name. This can be a font face name or a font family name, and may represent either a logical font or a physical font found in this GraphicsEnvironment. 
 * @param fStyle - style constant for the Font. The style argument is an integer bitmask that may be PLAIN, or a bitwise union of BOLD and/or ITALIC (for example, ITALIC or BOLD|ITALIC).
 * @param fColor - color for the font.
 * @param width - width of of container for the text to be displayed in.
 * @param height - height of the contain for the text to be displayed in.
 * @param opacity - opacity of text.  (0=invisible, 100=visible)
 */
	public OverlayImage(String txt, int size, String fName, int fStyle, Color fColor, int width, int height, int opacity )
	{
		envMap.put("com.wowza.wms.plugin.transcoderoverlays.overlayimage.step", "1");		

		myText = new TextAttributes(txt,size,fName,fStyle,fColor,0,0);
		
		this.currentOpacity = opacity;
		this.width = width;
		this.height = height;
		tempBufferMap.put(1.0,new byte[width*height*4]);
		finalBufferedImageMap.put(1.0, new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR));
		
	}
	
	/**
	 * Create an OverlayImage with specified image filename.
	 * @param filename - complete path of the image to be used. (supports GIF, PNG, BMP, and JPEG)
	 * @param opacity - opacity of the image.  (0=invisible, 100=visible)
	 */
	public OverlayImage(String filename, int opacity)
	{
		envMap.put("com.wowza.wms.plugin.transcoderoverlays.overlayimage.step", "1");		

		this.myImage = new ImageAttributes(filename,opacity,0,0);
		this.currentOpacity = opacity;
		this.width = myImage.width;
		this.height = myImage.height;
		tempBufferMap.put(1.0,new byte[width*height*4]);
		finalBufferedImageMap.put(1.0, new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR));
	}	
	
	/**
	 * Adds another OverlayImage as a child.
	 * @param oImage - OverlayImage to be added.
	 * @param x - x position for the oImage relative to this.
	 * @param y - y position for the oImage relative to this.
	 */
	public void addOverlayImage(OverlayImage oImage, int x, int y)
	{
		oImage.xPos = x;
		oImage.yPos = y;
		childrenList.add(oImage);
	}
	/**
	 * Adds a fading step as a non-operation.
	 * @param noop_steps - number of steps to do nothing.
	 */
 	public void addFadingStep(double noop_steps)
 	{
		faddingEvents.addAnimationStep(noop_steps); 		
 	}
 	/**
 	 * Adds a fading step.  Updates the opacity of the image.  <i>(0=invisible, 100=visible)</i>
 	 * @param start - starting value of the opacity.
 	 * @param end - ending value of the opacity.
 	 * @param steps - number of steps it will take to get from <b>start</b> to <b>end</b>.
 	 */
 	public void addFadingStep(int start, int end, double steps)
	{
		faddingEvents.addAnimationStep(start, end, steps);
	}
	/**
	 * Adds an image step as a non-operation.
	 * @param noop_steps - number of steps to do nothing.
	 */
 	public void addImageStep(double noop_steps)
	{
		imageRotateEvents.addAnimationStep(noop_steps);
	}
 	/**
 	 * Adds an image step.  Updates the environment variable: <i>${com.wowza.wms.plugin.transcoderoverlays.overlayimage.step}</i>
 	 * @param start - starting value for the steps to take.
 	 * @param end - ending value for the steps to take.
 	 * @param steps - number of steps it will take to get from <b>start</b> to <b>end</b>.
 	 */
	public void addImageStep(int start, int end, double steps)
	{
		imageRotateEvents.addAnimationStep(start, end, steps);
	}
	/**
	 * Adds a movement step as a non-operation.
	 * @param noop_steps - number of steps to do nothing.
	 */
	public void addMovementStep(double noop_steps)
	{
		xPosMovementEvents.addAnimationStep(noop_steps);
		yPosMovementEvents.addAnimationStep(noop_steps);
	}
	/**
	 * Adds a movement step.  Moves the image around the screen.
	 * @param xStart - starting value for the x position of the image.
	 * @param yStart - starting value for the y position of the image.
	 * @param xEnd - ending value for the x position of the image.
	 * @param yEnd - ending value for the y position of the image.
	 * @param steps - number of steps it will take to get from <b>xStart,yStart</b> to <b>xEnd,yEnd</b>
	 */
	public void addMovementStep(int xStart, int yStart, int xEnd, int yEnd, double steps)
	{
		xPosMovementEvents.addAnimationStep(xStart, xEnd, steps);
		yPosMovementEvents.addAnimationStep(yStart, yEnd, steps);
	}
	/**
	 * Increments all Fading, Movement and Image Steps by one and calls step on all child OverlayImages.
	 */
	public void step()
	{

		if(faddingEvents.step())
		{
			this.currentOpacity = faddingEvents.getStepValue();
		}
		if(xPosMovementEvents.step())
		{
			this.xPos = xPosMovementEvents.getStepValue();
		}
		if(yPosMovementEvents.step())
		{
			this.yPos = yPosMovementEvents.getStepValue();
		}
		if(myImage !=null)
		{
			//Update image for overlay
			if(imageRotateEvents.step())
			{
				//imageRotateEvents.setCurrentAnimationOperation();				
				int imgID = imageRotateEvents.getStepValue();
				envMap.put("com.wowza.wms.plugin.transcoderoverlays.overlayimage.step", Integer.toString(imgID));
				myImage.LoadBufferedImage(true);
			}
		}

		for(OverlayImage img :childrenList)
		{
			img.step();
		}

	}
	
	/**
	 * Returns a byte[] buffer of the image and all its children drawn ontop.
	 * @param scaled - the amount to scaled the image by from the oringal.
	 * @return a byte[] array of the image.
	 */
	public byte[] GetBuffer(double scaled)
	{
		byte[] retVal = GetTempBuffer(scaled);
		BufferedImage bImage=GetBufferedImage(scaled);
		
		Graphics2D g = bImage.createGraphics();
		//clear the old image out
		g.setComposite(AlphaComposite.Clear); 
		g.fillRect(0, 0, GetWidth(scaled), GetHeight(scaled)); 
		g.setComposite(AlphaComposite.SrcOver);

		if(currentOpacity > 0.0) //don't bother if invisible
		{
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	
			if(myImage !=null)
			{
				g.drawImage(myImage.imageBuf, (int)(myImage.xOffset*scaled), (int)(myImage.yOffset*scaled), GetWidth(scaled), GetHeight(scaled), null);
			}
			for(OverlayImage img :childrenList)
			{
				img.GetBuffer(scaled);
				float opac = (float) (img.currentOpacity/100.0);
				AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opac);
				g.setComposite(composite); 
				g.drawImage(img.GetBufferedImage(scaled), img.GetxPos(scaled), img.GetyPos(scaled), img.GetWidth(scaled), img.GetHeight(scaled), null);
			}
			if(myText !=null)
			{
				g.setColor(myText.fontColor); 
				Font newFont = myText.font.deriveFont((float) (myText.font.getSize()*scaled));
				g.setFont(newFont); 
				g.drawString(myText.text,(int)(myText.xPos*scaled), (int)(myText.yPos*scaled));				
			}
		}
		transferImage(bImage,retVal,currentOpacity);
		
		return retVal;
	}
	/**
	 * Returns the height of the image
	 * @param scaled - amount to scale the height by.
	 * @return height of the image multiplied by the scaling factor.
	 */
	public int GetHeight(double scaled)
	{
		return (int)(height*scaled);
	}
	/**
	 * Returns the width of the image
	 * @param scaled - amount to scale the width by.
	 * @return width of the image multiplied by the scaling factor.
	 */
	public int GetWidth(double scaled)
	{
		return (int)(width*scaled);
	}
	/**
	 * Returns the x coordinate of the image
	 * @param scaled - amount to scale the x coordinate by.
	 * @return x coordinate of the image multiplied by the scaling factor.
	 */	
	public int GetxPos(double scaled)
	{
		return (int)(xPos*scaled);
	}
	/**
	 * Returns the y coordinate of the image
	 * @param scaled - amount to scale the y coordinate by.
	 * @return y coordinate of the image multiplied by the scaling factor.
	 */	
	public int GetyPos(double scaled)
	{
		return (int)(yPos*scaled);
	}
	/**
	 * Set the text of an text Image
	 * @param txt - the text for the image.
	 * @return True if text was set, false if not.
	 */
	public boolean SetText(String txt)
	{
		boolean retVal=false;
		if(myText!=null)
		{
			myText.text = txt;
			retVal=true;
		}
		return retVal;
	}
	/**
	 * Sets the x and y coordinates for the image relative the parent.
	 * @param x - x position relative to its parent.  If no parent it is relative to screen.
	 * @param y - y position relative to its parent.  If no parent it is relative to screen.	 
	 */
	public void SetPos(int x, int y)
	{
		xPos = x;
		yPos = y;
	}
	private BufferedImage GetBufferedImage(double scaled)
	{
		if(!finalBufferedImageMap.containsKey(scaled))
		{
			finalBufferedImageMap.put(scaled, new BufferedImage(GetWidth(scaled), GetHeight(scaled), BufferedImage.TYPE_4BYTE_ABGR));
		}
		return finalBufferedImageMap.get(scaled);
	}
	private byte[] GetTempBuffer(double scaled)
	{
		if(!tempBufferMap.containsKey(scaled))
		{
			tempBufferMap.put(scaled, new byte[GetWidth(scaled)*GetHeight(scaled)*4]);
		}
		return tempBufferMap.get(scaled);
	}

	private static void transferImage(BufferedImage image, byte[] imgBuffer, int opacity)
	{
	    int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();
					
		//TODO - cfg - I am sure there is a faster way to do this
		int imgLoc = 0;
		for(int y=0;y<imgHeight;y++)
		{
			for(int x=0;x<imgWidth;x++)
			{
				int argb = image.getRGB(x, y);
				
				imgBuffer[(imgLoc*4)+0] = (byte)((argb>>0)&0x0FF);
				imgBuffer[(imgLoc*4)+1] = (byte)((argb>>8)&0x0FF);
				imgBuffer[(imgLoc*4)+2] = (byte)((argb>>16)&0x0FF);

				int alpha = ((argb>>24)&0x0FF);
				if (opacity < 100)
					alpha = (alpha*opacity)/100;
				
				imgBuffer[(imgLoc*4)+3] = (byte)alpha;
				
				imgLoc++;
			}
		}
	}

	public class TextAttributes extends Object
	{
		String text;
		Font font;
		Color fontColor;
		int xPos;
		int yPos;
		/**
		 * Creates a TextAttribute object
		 * @param txt - text to be displayed.
		 * @param size - point size of the Font.
		 * @param fName - font name. This can be a font face name or a font family name, and may represent either a logical font or a physical font found in this GraphicsEnvironment. 
		 * @param fStyle - style constant for the Font. The style argument is an integer bitmask that may be PLAIN, or a bitwise union of BOLD and/or ITALIC (for example, ITALIC or BOLD|ITALIC).
		 * @param fColor - color for the font.
		 * @param x - x position relative to its parent.  If no parent it is relative to screen.
		 * @param y - y position relative to its parent.  If no parent it is relative to screen.
		 */
		public TextAttributes(String txt, int size, String fName, int fStyle, Color fColor, int x, int y)
		{
			text = txt;
			font = new Font( fName, fStyle, size);
			fontColor = fColor;
			xPos = x;
			yPos = y+size; //+fontSize because drawstring draws to bottom left corner 				
		}
	}
	public class ImageAttributes extends Object
	{
		String filename;
		int opacity = 100;
		BufferedImage imageBuf = null;
		int xOffset=0;
		int yOffset=0;
		int width;
		int height;
		String lastFilename;
		
		public ImageAttributes(String filename, int opacity, int x, int y)
		{
			this.filename = filename;
			this.opacity = opacity;
			this.xOffset = x;
			this.yOffset = y;
			LoadBufferedImage();
		}
		public ImageAttributes(String filename, int x, int y)
		{
			this.xOffset = x;
			this.yOffset = y;
			this.filename = filename;
			LoadBufferedImage();
		}
		private void LoadBufferedImage(boolean checkFileName)
		{		
			if(checkFileName)
			{
				String fName = SystemUtils.expandEnvironmentVariables(filename, envMap);
				if(fName!=lastFilename)
				{
					LoadBufferedImage();
				}
				
			}
			else
			{
				LoadBufferedImage();
			}
		}
		private void LoadBufferedImage()
		{		
			//might be helpful to add a cache to minimize reloading images from disk
			try 
			{
				String fName = SystemUtils.expandEnvironmentVariables(filename, envMap);
				File imgFile = new File(fName);		
				imageBuf = ImageIO.read(imgFile);
				this.width = imageBuf.getWidth();
				this.height = imageBuf.getHeight();
				lastFilename = fName; 
			} 
			catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

}
