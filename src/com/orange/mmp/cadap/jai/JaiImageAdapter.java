/*
 * Copyright (C) 2010 France Telecom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orange.mmp.cadap.jai;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp; 

import net.sf.image4j.codec.ico.ICODecoder;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import sun.awt.image.BufferedImageGraphicsConfig;

import com.orange.mmp.cadap.Constants;
import com.orange.mmp.cadap.MediaAdapter;
import com.orange.mmp.cadap.MediaAdapterException;
import com.orange.mmp.cadap.MediaContainer;
import com.orange.mmp.core.data.Element;
import com.orange.mmp.net.Connection;
import com.orange.mmp.net.ConnectionManagerFactory;
import com.orange.mmp.net.MMPNetException;
import com.sun.media.jai.codec.SeekableStream;

/**
 * Image adapter based on JAI API (local)
 * @author nmtv3386
  */
public class JaiImageAdapter implements MediaAdapter {

	/**
     * Contains the rendering hints for JAI Adapter
     */
    private static Map<RenderingHints.Key, Object> renderdingHints = null;

    /**
     * List of supported formats in input
     */
    private static List<String> inSupportedFormats = null;

    /**
     * List of supported formats in output
     */
    private static List<String> outSupportedFormats = null;
    
    /**
     * Constant used to check "format" parameter 
     */
    private static final int PARAM_FORMAT_HASHCODE = -1268779017;
    
    /**
     * Constant used to check "width" parameter 
     */
    private static final int PARAM_WIDTH_HASHCODE = 113126854;
    
    /**
     * Constant used to check "height" parameter 
     */
    private static final int PARAM_HEIGHT_HASHCODE = -1221029593;
    
    /**
     * Constant used to check "zoom" parameter 
     */
    private static final int PARAM_ZOOM_HASHCODE = 3744723;
    
    /**
     * Constant used to check "ratio" parameter 
     */
    private static final int PARAM_RATIO_HASHCODE = 108285963;

    /**
     * Maximum size in bytes of source image
     */
    private int imageMaxSize;

    static{
		JaiImageAdapter.renderdingHints = new HashMap<RenderingHints.Key, Object>();
		JaiImageAdapter.renderdingHints.put(RenderingHints.KEY_RENDERING , RenderingHints.VALUE_RENDER_SPEED);
		JaiImageAdapter.renderdingHints.put(RenderingHints.KEY_ANTIALIASING  , RenderingHints.VALUE_ANTIALIAS_ON);
		JaiImageAdapter.renderdingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING  , RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		JaiImageAdapter.renderdingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION , RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
		JaiImageAdapter.renderdingHints.put(RenderingHints.KEY_COLOR_RENDERING  , RenderingHints.VALUE_COLOR_RENDER_SPEED  );
		JaiImageAdapter.renderdingHints.put(RenderingHints.KEY_DITHERING  , RenderingHints.VALUE_DITHER_DISABLE   );
		JaiImageAdapter.renderdingHints.put(RenderingHints.KEY_STROKE_CONTROL   , RenderingHints.VALUE_STROKE_PURE  );
		JaiImageAdapter.renderdingHints.put(RenderingHints.KEY_FRACTIONALMETRICS    , RenderingHints.VALUE_FRACTIONALMETRICS_OFF );
		JaiImageAdapter.renderdingHints.put(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		JaiImageAdapter.inSupportedFormats  = Arrays.asList(ImageIO.getReaderFormatNames());
		JaiImageAdapter.outSupportedFormats = Arrays.asList(new String[]{Constants.IMG_EXT_PNG,Constants.IMG_EXT_JPG,Constants.IMG_EXT_JPEG});
    }

    /* (non-Javadoc)
     * @see com.orange.mmp.cadap.MediaAdapter#adapt(java.lang.String, java.lang.String)
     */
    public MediaContainer adapt(String mediaLocation, Element ... parameters) throws MediaAdapterException{
    	Connection httpConnection = null;
    	MediaContainer imageContainer = new MediaContainer();
    	
    	//Default Parameters
    	String format = Constants.IMG_EXT_PNG;
    	int width = 0;
    	int height = 0;
    	int ratio = -1;
    	boolean zoom = false;
    	
    	//Check parameters
    	if(parameters != null){
    		for(Element parameter : parameters){
    			switch(parameter.getKey().hashCode()){
	    			case PARAM_FORMAT_HASHCODE :
						format = (String)parameter.getValue();
					break;	
					case PARAM_WIDTH_HASHCODE :
	    				width = (Integer)parameter.getValue();
	    			break;
					case PARAM_HEIGHT_HASHCODE :
						height = (Integer)parameter.getValue();
	    			break;
					case PARAM_ZOOM_HASHCODE :
						zoom = (Boolean)parameter.getValue();
	    			break;
					case PARAM_RATIO_HASHCODE :
						ratio = (Integer)parameter.getValue();
	    			break;
    			}
    		}
    	}

    	if(mediaLocation == null ){
    		throw new MediaAdapterException("Bad parameters for resizing");
    	}

    	try{
    		//Get image
    		httpConnection = ConnectionManagerFactory.getInstance().getConnectionManager("http").getConnection();
    		httpConnection.init(mediaLocation.toString(), 60);

    		//Load image
    		BufferedImage srcImage = null;
    		BufferedImage dstImage = null;
    		InputStream imgIn = httpConnection.getData();

    		//Check Image size
    		String imageSizeStr = (String)httpConnection.getProperty(com.orange.mmp.mvc.Constants.HTTP_HEADER_CONTENTLENGTH);
    		if(imageSizeStr != null && Integer.parseInt(imageSizeStr) > this.imageMaxSize){
    			throw new MediaAdapterException("Source image too large : "+imageSizeStr+"KB");
    		}

    		//get the image format
    		String[] srcImageType = null;
    		if((String)httpConnection.getProperty(com.orange.mmp.mvc.Constants.HTTP_HEADER_CONTENTTYPE) != null){
    			if(((String)httpConnection.getProperty(com.orange.mmp.mvc.Constants.HTTP_HEADER_CONTENTTYPE)).contains(Constants.MIME_TYPE_HTMLCONTENT)){
    				String newImageLocation = this.getImageLocationFromHTML(mediaLocation,imgIn);
    				return this.adapt(newImageLocation, parameters);
    			}
    			else srcImageType = ((String)httpConnection.getProperty(com.orange.mmp.mvc.Constants.HTTP_HEADER_CONTENTTYPE)).split("/");
    		}
    		else srcImageType = Constants.MIME_TYPE_JPEGCONTENT.split("/");

    		if(imgIn != null){
    			//JAI
    			if(JaiImageAdapter.inSupportedFormats.contains(srcImageType[1])){
    				RenderedOp renderedOp = JAI.create("stream", SeekableStream.wrapInputStream(imgIn,true));
    				srcImage = renderedOp.getAsBufferedImage();
    			}
    			//Image IO
    			else{
    				List<BufferedImage> images = ICODecoder.read(imgIn);
    				if(images != null && images.size() > 0){
    					srcImage = images.get(0);
    				}
    				else throw new MediaAdapterException("Failed to get source image data (unsupported format)");
    				format = Constants.IMG_EXT_PNG;
    			}
    		}
    		else throw new MediaAdapterException("Failed to get source image data (not found)");

    		//Get image dimensions
    		int srcWidth = srcImage.getWidth();
    		int srcHeight = srcImage.getHeight();
    		int newImgWidth = srcWidth;
    		int newImgHeight = srcHeight;
    		
    		//Dimension using ratio
    		if(ratio > 0){
    			newImgWidth = Math.round(srcWidth * ratio / 100);
    			newImgHeight = Math.round(srcHeight * ratio / 100);
    			zoom = true;
    		}
    		//Dimension using width and height
    		else{
    			if(width == 0) width = srcWidth;
    			if(height == 0) height = srcHeight;
    			
    			//Determine the new image dimensions
        		float scrRatio = (float) width / height;
        		float imgRatio = (float) srcWidth / srcHeight;
        		
        		if(imgRatio > scrRatio) {
        			newImgWidth = width;
        			newImgHeight = Math.round((width * srcHeight) / srcWidth);
        		} else {
        			newImgHeight = height;
        			newImgWidth = Math.round((height * srcWidth) / srcHeight);
        		}
    		}

    		// Set image container format according to supported and queried formats
    		if(format == null) format = srcImageType[1];
    		if(JaiImageAdapter.outSupportedFormats.contains(format)){
    			imageContainer.setFormat(format);
    		}
    		else imageContainer.setFormat(Constants.IMG_EXT_PNG);
    		
    		//Create Image container
    		imageContainer.setSourceLocation(mediaLocation);

    		if(newImgWidth >= srcWidth && !zoom) {
    			dstImage = srcImage;
    			imageContainer.setWidth(srcWidth);
    			imageContainer.setHeight(srcHeight);
    		} else {
    			dstImage = this.createCompatibleImage(srcImage);
    			if(newImgWidth < srcWidth>>1 || newImgHeight < srcHeight>>1){
    				dstImage = this.blurImage(dstImage);
    			}
    			dstImage = resize(dstImage, newImgWidth, newImgHeight, imageContainer.getFormat().equals(Constants.IMG_EXT_PNG));
    			imageContainer.setWidth(newImgWidth);
    			imageContainer.setHeight(newImgHeight);
    		}


    		// Copy image data to outputstream
    		ByteArrayOutputStream buffOut = new ByteArrayOutputStream();
    		ImageIO.write(dstImage, imageContainer.getFormat(), buffOut);
    		imageContainer.setData(buffOut.toByteArray());

    		return imageContainer;

    	}catch(IOException ioe){
    		throw new MediaAdapterException(ioe);
    	}catch(MMPNetException mne){
    		throw new MediaAdapterException(mne);
    	}
    	finally{
    		if(httpConnection != null){
    			try{
    				ConnectionManagerFactory.getInstance().getConnectionManager("http").releaseConnection(httpConnection);
    			}catch(MMPNetException mne){
    	    		//NOP - Just log
    	    	}
    		}
    	}
    }

    /**
     * If stream is HTML, this method allows to try to get the image from HTML content
     * @param sourceLocation The initial image location used to find image in HTML
     * @param htmlIn The HTML input stream
     * @return A new Image location under String
     * @throws IOException
     */
    @SuppressWarnings("serial")
	private String getImageLocationFromHTML(String sourceLocation, InputStream htmlIn) throws IOException{
    	String imageToken = null;
    	//Search for image token in source location
    	URL sourceUrl = new URL(sourceLocation);
    	String urlPathParts[] = sourceUrl.getPath().split("/");
    	if(urlPathParts.length > 0){
    		imageToken = urlPathParts[urlPathParts.length-1];
    	}
    	else throw new IOException("Failed to get image token ["+sourceLocation+"]");

    	//Get HTML content
    	String line = null;
    	StringWriter writer = null;
    	BufferedReader reader = null;
    	try{
    		writer = new StringWriter();
    		reader = new BufferedReader(new InputStreamReader(htmlIn));
    		while((line = reader.readLine()) != null){
    			writer.write(line);
    		}

    		//Parse HTML content to find image
    		try{
    			Parser htmlParser = Parser.createParser(writer.toString(), com.orange.mmp.core.Constants.DEFAULT_ENCODING);
    			NodeList nodeList = htmlParser.extractAllNodesThatMatch(new NodeFilter(){
    				public boolean accept(Node node){
    					return (node instanceof ImageTag);
    				}
    			});
    			if(nodeList != null && nodeList.size() > 0){
    				SimpleNodeIterator nodeIterator = nodeList.elements();
    				while(nodeIterator.hasMoreNodes()){
    					ImageTag imgNode = (ImageTag)nodeIterator.nextNode();
    					if(imgNode.getImageURL().contains(imageToken)){
    						return imgNode.getImageURL();
    					}
    				}
    			}
    			else throw new IOException("Failed to find images in HTML content");
    		}catch(ParserException pe){
    			throw new IOException("Failed to parse HTML content");
    		}
    	}finally{
    		if(writer != null) writer.close();
    		if(reader != null) reader.close();
    	}

    	return null;

    }

    /**
     * Resize a BufferedImage to the given dimensions
     * @param buffIm	The image to resize
     * @param width	The new width
     * @param height	The new height
     * @param algo	Integer indicating the interpolation algorithm to use (1=bilinear, 2=nearest neighbour)
     * @return		The BufferedImage resized
     */
    private BufferedImage resize(BufferedImage buffIm, int width, int height, boolean transparencySupport) {
		BufferedImage bi = new BufferedImage(width, height, (transparencySupport) ? BufferedImage.TYPE_INT_ARGB:BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		double xScale = (double)width / buffIm.getWidth();
		double yScale = (double)height / buffIm.getHeight();
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		g2d.addRenderingHints(JaiImageAdapter.renderdingHints);
		g2d.drawRenderedImage(buffIm, at);
		g2d.dispose();
		return bi;
    }

    /**
     * Blur a source image (should be done before resizing)
     * @param image The input image
     * @return The output image
     */
    private BufferedImage blurImage(BufferedImage image) {
		float blurKernel[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f,
		        0.0625f, 0.125f, 0.0625f };
		RenderingHints hints = new RenderingHints(JaiImageAdapter.renderdingHints);
		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, hints);
		return op.filter(image, null);
    }

    /**
     * Create a compatible image for operations like blur
     * @param image The input image
     * @return The output image
     */
    private BufferedImage createCompatibleImage(BufferedImage image) {
		GraphicsConfiguration gc = BufferedImageGraphicsConfig.getConfig(image);
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage result = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		Graphics2D g2 = result.createGraphics();
		g2.drawRenderedImage(image, null);
		g2.dispose();
		return result;
    }

    /**
     * @return the imageMaxSize
     */
    public int getImageMaxSize() {
        return imageMaxSize;
    }

    /**
     * @param imageMaxSize the imageMaxSize to set
     */
    public void setImageMaxSize(int imageMaxSize) {
        this.imageMaxSize = imageMaxSize;
    }

}
