package com.watchtogether.load.streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;

public class StreamPublisher extends Thread{

	private int videoStreamId = -1;
	private IStreamCoder videoDecoder = null;
	private int audioStreamId = -1;
	private IStreamCoder audioDecoder = null;
	private IStreamCoder outVideoCoder = null;
	private IStreamCoder outAudioCoder = null;
	
	private IContainer inContainer;
	private IContainer outContainer;
	
	private static long mSystemVideoClockStartTime;
	private static long mFirstVideoTimestampInStream;
	
	private boolean run = true;
	private boolean streamEnded = false;
	
	private String url = "";
	
	protected static Logger log = LoggerFactory.getLogger(StreamPublisher.class);
	
	public StreamPublisher(String serverURL, String fileName) {
		this.url = serverURL;
		
		inContainer = openFile(fileName);
		
		outContainer = createOutput(serverURL);
	}
	
	private IContainer openFile(String fileName) {
		IContainer container = IContainer.make();
	    
	    // Open up the container
	    if (container.open(fileName, IContainer.Type.READ, null) < 0)
	      throw new IllegalArgumentException("could not open file: " + fileName);
	    
	    // query how many streams the call to open found
	    int numStreams = container.getNumStreams();
	    
	    for(int i = 0; i < numStreams; i++)
	    {
	      // Find the stream object
	      IStream stream = container.getStream(i);
	      // Get the pre-configured decoder that can decode this stream;
	      IStreamCoder coder = stream.getStreamCoder();
	      
	      if (videoStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
	      {
	        videoStreamId = i;
	        videoDecoder = coder;
	      }
	      else if (audioStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
	      {
	        audioStreamId = i;
	        audioDecoder = coder;
	      }
	    }
	    if (videoStreamId == -1 && audioStreamId == -1)
	      throw new RuntimeException("could not find audio or video stream in container: "+fileName);
	    
	    return container;
	}
	
	public IContainer createOutput(String urlOut) {
		IContainer outContainer = IContainer.make();
		IContainerFormat outContainerFormat = IContainerFormat.make();
	    outContainerFormat.setOutputFormat("flv", urlOut, null);
	    int retVal = outContainer.open(urlOut, IContainer.Type.WRITE, outContainerFormat);
	    if (retVal < 0) {
	        System.out.println("Could not open output container");
	        return null;
	    }

	    IStream outVideoStream = outContainer.addNewStream(0);
	    outVideoCoder = outVideoStream.getStreamCoder();
	    outVideoCoder.setCodec(ICodec.ID.CODEC_ID_FLV1);
	    //outVideoCoder.setWidth(125);
	    //outVideoCoder.setHeight(125);
	    outVideoCoder.setWidth(64);
	    outVideoCoder.setHeight(64);
	    outVideoCoder.setPixelType(IPixelFormat.Type.YUV420P);
	    outVideoCoder.setNumPicturesInGroupOfPictures(12);
	    outVideoCoder.setProperty("nr", 0);
	    outVideoCoder.setProperty("mbd",0);
	    //outVideoCoder.setProperty("b_adapt", false);
	    //outVideoCoder.setProperty("bf", 0); 
	    //outVideoCoder.setProperty("threads", 0); 
	    outVideoCoder.setTimeBase(IRational.make(1, 25));
	    outVideoCoder.setFrameRate(IRational.make(25, 1));
	    outVideoCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
	    retVal = outVideoCoder.open();
	    if (retVal < 0) {
	    	System.out.println("Could not open video coder");
	        return null;
	    }

	    IStream outAudioStream = outContainer.addNewStream(1);
	    outAudioCoder = outAudioStream.getStreamCoder();
	    outAudioCoder.setCodec(ICodec.ID.CODEC_ID_MP3);
	    outAudioCoder.setSampleRate(44100);
	    outAudioCoder.setChannels(2);
	    
	    retVal = outAudioCoder.open();
	    if (retVal < 0) {
	    	System.out.println("Could not open audio coder");
	        return null;
	    }

	    retVal = outContainer.writeHeader();
	    if (retVal < 0) {
	    	System.out.println("Could not write output FLV header: ");
	        return null;
	    }
	    return outContainer;
	}
	
	@Override
	public void run() {
		IPacket packetIn = IPacket.make();
		
		mFirstVideoTimestampInStream = Global.NO_PTS;
	    mSystemVideoClockStartTime = 0;
		
		if(videoDecoder.open() < 0)
			 throw new RuntimeException("could not open video decoder for container:");
		if(audioDecoder.open() < 0)
			 throw new RuntimeException("could not open audio decoder for container:");
		
		IVideoResampler videoResampler = IVideoResampler.make(outVideoCoder.getWidth(), outVideoCoder.getHeight(), outVideoCoder.getPixelType(),
				videoDecoder.getWidth(), videoDecoder.getHeight(), videoDecoder.getPixelType());
		
		IAudioResampler audioResampler = IAudioResampler.make(outAudioCoder.getChannels(), // output channels
				audioDecoder.getChannels(), // input channels
				outAudioCoder.getSampleRate(), // new sample rate
				audioDecoder.getSampleRate() // old sample rate
				);
		
		while(inContainer.readNextPacket(packetIn) >= 0 && run) {
			if (packetIn.getStreamIndex() == videoStreamId) {  //video frame
				IVideoPicture picture = IVideoPicture.make(videoDecoder.getPixelType(),
						videoDecoder.getWidth(), videoDecoder.getHeight());
			        
		        /*
		         * Now, we decode the video, checking for any errors.
		         */
		        int bytesDecoded = videoDecoder.decodeVideo(picture, packetIn, 0);
		        if (bytesDecoded < 0)
		        	throw new RuntimeException("got error decoding video in");
		        
		        if (picture.isComplete())
		        {
			        //resample  
		        	IVideoPicture videoPicture_resampled = IVideoPicture.make(videoResampler.getOutputPixelFormat(), 
		        			videoResampler.getOutputWidth(), videoResampler.getOutputHeight());
			        videoResampler.resample(videoPicture_resampled, picture);
	
			        IPacket packet_out = IPacket.make();
			        
			        int retVal = outVideoCoder.encodeVideo(packet_out, videoPicture_resampled, 0);
			        if (retVal < 0) {
			            System.out.println("Could not encode video");
			            videoPicture_resampled.delete();
			            continue;
			        }
			   
			        long delay = millisecondsUntilTimeToDisplay(videoPicture_resampled);
			        
			        videoPicture_resampled.delete();
			        packet_out.setDuration(packetIn.getDuration());
			        //packet_out.setDts(packetIn.getDts());
			        //packet_out.setPts(packetIn.getPts());
			        packet_out.setPosition(packetIn.getPosition());
			        //packet_out.setKeyPacket(true);
	
			        if (packet_out.isComplete()) {             
			            retVal = outContainer.writePacket(packet_out, true);
			        }
			        
			        			        
			        // if there is no audio stream; go ahead and hold up the main thread.  We'll end
			        // up caching fewer video pictures in memory that way.
			        try
			        {
			        	if (delay > 0)
			        		Thread.sleep(delay);
			        }
			        catch (InterruptedException e)
			        {
			        	return;
			        }
		        }
			} else if (packetIn.getStreamIndex() == audioStreamId) {
				 IAudioSamples samples = IAudioSamples.make(1024, audioDecoder.getChannels());
				 
				 int offset = 0;
			        
				 /*
				  * Keep going until we've processed all data
				  */
				 while(offset < packetIn.getSize())
				 {
					 int bytesDecoded = audioDecoder.decodeAudio(samples, packetIn, offset);
			          if (bytesDecoded < 0)
			            throw new RuntimeException("got error decoding audio in:");
			          offset += bytesDecoded;
			          /*
			           * Some decoder will consume data in a packet, but will not be able to construct
			           * a full set of samples yet.  Therefore you should always check if you
			           * got a complete set of samples from the decoder
			           */
			          if (samples.isComplete())
			          {
			        	  IAudioSamples audioSamples_resampled = IAudioSamples.make(1024, outAudioCoder.getChannels());
			        	  
			        	  IPacket packet_out = IPacket.make();
			        	  
			        	  audioResampler.resample(audioSamples_resampled, samples, samples.getNumSamples());
			        	  //playJavaSound(audioSamples_resampled);
			        	  //audioSamples_resampled.setPts(Global.NO_PTS);
			        	  int samplesConsumed = 0;
			        	  while (samplesConsumed < audioSamples_resampled.getNumSamples()) {
			        		  int retVal = outAudioCoder.encodeAudio(packet_out, audioSamples_resampled, samplesConsumed);
			        		  if (retVal <= 0)
			        			  throw new RuntimeException("Could not encode audio");
			        		  samplesConsumed += retVal;
			        		  if (packet_out.isComplete()) {
			        			  packet_out.setPosition(packetIn.getPosition());
			        			  packet_out.setStreamIndex(1);
			        			  outContainer.writePacket(packet_out, true);
			        		  }
			        	  }
			          }
				 }
			}
		}
		
		log.warn("Stream {} should stop", new Object[]{url});
		
		inContainer.close();
		//outContainer.flushPackets();
		outContainer.close();
		
		log.warn("Stream {} has stopped", new Object[]{url});
		
		streamEnded = true;
	}
	
	public void stopPublish() {
		log.warn("Stream {} has received stop signal", new Object[]{url});
		
		run = false;
	}
	
	private static long millisecondsUntilTimeToDisplay(IVideoPicture picture)
	  {
	    /**
	     * We could just display the images as quickly as we decode them, but it turns
	     * out we can decode a lot faster than you think.
	     * 
	     * So instead, the following code does a poor-man's version of trying to
	     * match up the frame-rate requested for each IVideoPicture with the system
	     * clock time on your computer.
	     * 
	     * Remember that all Xuggler IAudioSamples and IVideoPicture objects always
	     * give timestamps in Microseconds, relative to the first decoded item.  If
	     * instead you used the packet timestamps, they can be in different units depending
	     * on your IContainer, and IStream and things can get hairy quickly.
	     */
	    long millisecondsToSleep = 0;
	    if (mFirstVideoTimestampInStream == Global.NO_PTS)
	    {
	      // This is our first time through
	      mFirstVideoTimestampInStream = picture.getTimeStamp();
	      // get the starting clock time so we can hold up frames
	      // until the right time.
	      mSystemVideoClockStartTime = System.currentTimeMillis();
	      millisecondsToSleep = 0;
	    } else {
	      long systemClockCurrentTime = System.currentTimeMillis();
	      long millisecondsClockTimeSinceStartofVideo = systemClockCurrentTime - mSystemVideoClockStartTime;
	      // compute how long for this frame since the first frame in the stream.
	      // remember that IVideoPicture and IAudioSamples timestamps are always in MICROSECONDS,
	      // so we divide by 1000 to get milliseconds.
	      long millisecondsStreamTimeSinceStartOfVideo = (picture.getTimeStamp() - mFirstVideoTimestampInStream)/1000;
	      final long millisecondsTolerance = 50; // and we give ourselfs 50 ms of tolerance
	      millisecondsToSleep = (millisecondsStreamTimeSinceStartOfVideo -
	          (millisecondsClockTimeSinceStartofVideo+millisecondsTolerance));
	    }
	    return millisecondsToSleep;
	  }
	
	public boolean isStreamEnded() {
		return streamEnded;
	}
}
