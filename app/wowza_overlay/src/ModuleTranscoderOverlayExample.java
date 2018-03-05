//package com.wowza.wms.plugin.transcoderoverlays;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wowza.util.SystemUtils;
import com.wowza.wms.application.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoder;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoderNotify;
import com.wowza.wms.transcoder.model.LiveStreamTranscoder;
import com.wowza.wms.transcoder.model.LiveStreamTranscoderActionNotifyBase;
import com.wowza.wms.transcoder.model.TranscoderSession;
import com.wowza.wms.transcoder.model.TranscoderSessionVideo;
import com.wowza.wms.transcoder.model.TranscoderSessionVideoEncode;
import com.wowza.wms.transcoder.model.TranscoderStream;
import com.wowza.wms.transcoder.model.TranscoderStreamDestination;
import com.wowza.wms.transcoder.model.TranscoderStreamDestinationVideo;
import com.wowza.wms.transcoder.model.TranscoderStreamSourceVideo;
import com.wowza.wms.transcoder.model.TranscoderVideoDecoderNotifyBase;
import com.wowza.wms.transcoder.model.TranscoderVideoOverlayFrame;

public class ModuleTranscoderOverlayExample extends ModuleBase {

	String graphicName = "logo_${com.wowza.wms.plugin.transcoderoverlays.overlayimage.step}.png";
	int overlayIndex = 1;
	private IApplicationInstance appInstance = null;
	/**
	 * full path to the content directory where the graphics are kept
	 */
	private String basePath = null;
	private Object lock = new Object();

	public void onAppStart(IApplicationInstance appInstance)
	{
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("onAppStart: " + fullname);
		this.appInstance = appInstance;
		String artworkPath = "${com.wowza.wms.context.VHostConfigHome}/content/" + appInstance.getApplication().getName();
		Map<String, String> envMap = new HashMap<String, String>();
		if (appInstance.getVHost() != null)
		{
			envMap.put("com.wowza.wms.context.VHost", appInstance.getVHost().getName());
			envMap.put("com.wowza.wms.context.VHostConfigHome", appInstance.getVHost().getHomePath());
		}
		envMap.put("com.wowza.wms.context.Application", appInstance.getApplication().getName());
		if (this != null)
			envMap.put("com.wowza.wms.context.ApplicationInstance", appInstance.getName());
		this.basePath =  SystemUtils.expandEnvironmentVariables(artworkPath, envMap);
		this.basePath = this.basePath.replace("\\", "/");
		if (!this.basePath.endsWith("/"))
			this.basePath = this.basePath+"/";
		this.appInstance.addLiveStreamTranscoderListener(new TranscoderCreateNotifierExample());
	}

	class EncoderInfo
	{
		public String encodeName;
		public TranscoderSessionVideoEncode sessionVideoEncode = null;
		public TranscoderStreamDestinationVideo destinationVideo = null;
		/**
		 * array on ints that defines number of pixels to pad the video by.  Left,Top,Right,Bottom
		 */
		public int[] videoPadding = new int[4];
		public EncoderInfo(String name, TranscoderSessionVideoEncode sessionVideoEncode, TranscoderStreamDestinationVideo destinationVideo)
		{
			this.encodeName = name;
			this.sessionVideoEncode = sessionVideoEncode;
			this.destinationVideo = destinationVideo;
		}
	}

	public void onAppStop(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("onAppStop: " + fullname);
	}

	public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("onConnect: " + client.getClientId());
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("onDisconnect: " + client.getClientId());
	}

	class TranscoderCreateNotifierExample implements ILiveStreamTranscoderNotify
	{
		public void onLiveStreamTranscoderCreate(ILiveStreamTranscoder liveStreamTranscoder, IMediaStream stream)
		{
			getLogger().info("ModuleTranscoderOverlayExample#TranscoderCreateNotifierExample.onLiveStreamTranscoderCreate["+appInstance.getContextStr()+"]: "+stream.getName());
			((LiveStreamTranscoder)liveStreamTranscoder).addActionListener(new TranscoderActionNotifierExample());
		}

		public void onLiveStreamTranscoderDestroy(ILiveStreamTranscoder liveStreamTranscoder, IMediaStream stream)
		{
		}

		public void onLiveStreamTranscoderInit(ILiveStreamTranscoder liveStreamTranscoder, IMediaStream stream)
		{
		}	
	}

	class TranscoderActionNotifierExample extends LiveStreamTranscoderActionNotifyBase
	{
		TranscoderVideoDecoderNotifyExample transcoder=null;

		public void onSessionVideoEncodeSetup(LiveStreamTranscoder liveStreamTranscoder, TranscoderSessionVideoEncode sessionVideoEncode)
		{
			getLogger().info("ModuleTranscoderOverlayExample#TranscoderActionNotifierExample.onSessionVideoEncodeSetup["+appInstance.getContextStr()+"]");
			TranscoderStream transcoderStream = liveStreamTranscoder.getTranscodingStream();
			if (transcoderStream != null && transcoder==null)
			{
				TranscoderSession transcoderSession = liveStreamTranscoder.getTranscodingSession();
				TranscoderSessionVideo transcoderVideoSession = transcoderSession.getSessionVideo();
				List<TranscoderStreamDestination> alltrans = transcoderStream.getDestinations();

				int w = transcoderVideoSession.getDecoderWidth();
				int h = transcoderVideoSession.getDecoderHeight();
				transcoder = new TranscoderVideoDecoderNotifyExample(w,h);
				transcoderVideoSession.addFrameListener(transcoder);

				//apply an overlay to all outputs
				for(TranscoderStreamDestination destination:alltrans)
				{
					//TranscoderSessionVideoEncode sessionVideoEncode = transcoderVideoSession.getEncode(destination.getName());
					TranscoderStreamDestinationVideo videoDestination = destination.getVideo();
					System.out.println("sessionVideoEncode:"+sessionVideoEncode);
					System.out.println("videoDestination:"+videoDestination);
					if (sessionVideoEncode != null && videoDestination !=null)
					{
						transcoder.addEncoder(destination.getName(),sessionVideoEncode,videoDestination);
					} 
				}
			}
			return;
		}
	}
	class TranscoderVideoDecoderNotifyExample extends TranscoderVideoDecoderNotifyBase
	{
		private OverlayImage mainImage=null;private OverlayImage wowzaImage=null;
		private OverlayImage wowzaText = null;
		private OverlayImage wowzaTextShadow = null;
		List<EncoderInfo> encoderInfoList = new ArrayList<EncoderInfo>();
		AnimationEvents videoBottomPadding = new AnimationEvents();

		public TranscoderVideoDecoderNotifyExample (int srcWidth, int srcHeight)
		{
			int lowerThirdHeight = 70;

			//create a transparent container for the bottom third of the screen.
			mainImage = new OverlayImage(0,srcHeight-lowerThirdHeight,srcWidth,lowerThirdHeight,100);
			//Create the Wowza logo image
			wowzaImage = new OverlayImage(basePath+graphicName,100);
			mainImage.addOverlayImage(wowzaImage,srcWidth-wowzaImage.GetWidth(1.0),0);

			//Add Text with a drop shadow
			wowzaText = new OverlayImage("Wowza", 12, "SansSerif", Font.BOLD, Color.white, 66,15,100);
			wowzaTextShadow = new OverlayImage("Wowza", 12, "SansSerif", Font.BOLD, Color.darkGray, 66,15,100);
			mainImage.addOverlayImage(wowzaText, wowzaImage.GetxPos(1.0)+12, 54);
			wowzaText.addOverlayImage(wowzaTextShadow, 1, 1);

			//Fade the Logo and text independently
			//wowzaImage.addFadingStep(100,0,25);
			//wowzaImage.addFadingStep(0,100,25);
			//wowzaText.addFadingStep(0,100,25);
			//wowzaText.addFadingStep(100,0,25);

			//do nothing for a bit
			mainImage.addFadingStep(50);
			wowzaImage.addImageStep(50);
			wowzaText.addMovementStep(50);

			//Fade the logo and text
			mainImage.addFadingStep(0,100,100);


			//Rotate the image while fading
			wowzaImage.addImageStep(1,37,25);
			wowzaImage.addImageStep(1,37,25);
			wowzaImage.addImageStep(1,37,25);
			wowzaImage.addImageStep(1,37,25);
			//Animate the text off screen to original location
			wowzaText.addMovementStep(-75, 0, wowzaText.GetxPos(1.0), 54, 100);

			//hold everything for a bit
			mainImage.addFadingStep(50);
			wowzaImage.addImageStep(50);
			wowzaText.addMovementStep(50);

			//Fade out
			mainImage.addFadingStep(100,0,50);
			wowzaImage.addImageStep(50);
			wowzaText.addMovementStep(50);

			//Pinch back video
			videoBottomPadding.addAnimationStep(0, 60, 50);
			videoBottomPadding.addAnimationStep(100);

			//unpinch the video
			videoBottomPadding.addAnimationStep(60, 0, 50);
			mainImage.addFadingStep(50);
			wowzaImage.addImageStep(50);
			wowzaText.addMovementStep(50);
			videoBottomPadding.addAnimationStep(100);

		}	

		public void addEncoder(String name, TranscoderSessionVideoEncode sessionVideoEncode, TranscoderStreamDestinationVideo destinationVideo)
		{
			encoderInfoList.add(new EncoderInfo(name,sessionVideoEncode,destinationVideo));
		}

		public void onBeforeScaleFrame(TranscoderSessionVideo sessionVideo, TranscoderStreamSourceVideo sourceVideo, long frameCount)
		{
			boolean encodeSource=false;
			boolean showTime=false;
			double scalingFactor=1.0;
			synchronized(lock)
			{
				if (mainImage != null)
				{
					//does not need to be done for a static graphic, but left here to build on (transparency/animation)
					videoBottomPadding.step();
					mainImage.step();
					int sourceHeight = sessionVideo.getDecoderHeight();
					int sourceWidth = sessionVideo.getDecoderWidth();
					if(showTime)
					{
						Date dNow = new Date( );
						SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
						wowzaText.SetText(ft.format(dNow));
						wowzaTextShadow.SetText(ft.format(dNow));
					}
					if(encodeSource)
					{
						//put the image onto the source
						scalingFactor = 1.0;
						TranscoderVideoOverlayFrame overlay = new TranscoderVideoOverlayFrame(mainImage.GetWidth(scalingFactor),
								mainImage.GetHeight(scalingFactor), mainImage.GetBuffer(scalingFactor));
						overlay.setDstX(mainImage.GetxPos(scalingFactor));
						overlay.setDstY(mainImage.GetyPos(scalingFactor));
						sourceVideo.addOverlay(overlayIndex, overlay);
					} 
					else	
					{
						///put the image onto each destination but scaled to fit
						for(EncoderInfo encoderInfo: encoderInfoList)
						{
							if (!encoderInfo.destinationVideo.isPassThrough())
							{
								int destinationHeight = encoderInfo.destinationVideo.getFrameSizeHeight();
								scalingFactor = (double)destinationHeight/(double)sourceHeight;
								TranscoderVideoOverlayFrame overlay = new TranscoderVideoOverlayFrame(mainImage.GetWidth(scalingFactor),
										mainImage.GetHeight(scalingFactor), mainImage.GetBuffer(scalingFactor));
								overlay.setDstX(mainImage.GetxPos(scalingFactor));
								overlay.setDstY(mainImage.GetyPos(scalingFactor));
								encoderInfo.destinationVideo.addOverlay(overlayIndex,	overlay);
								//Add padding to the destination video i.e. pinch
								encoderInfo.videoPadding[0] = 0; // left
								encoderInfo.videoPadding[1] = 0; // top
								encoderInfo.videoPadding[2] = 0; // right
								encoderInfo.videoPadding[3] = (int)(((double)videoBottomPadding.getStepValue())*scalingFactor); // bottom
								encoderInfo.destinationVideo.setPadding(encoderInfo.videoPadding);
							}
						}
					}
				} 
			}
			return; 
		}
	}
}