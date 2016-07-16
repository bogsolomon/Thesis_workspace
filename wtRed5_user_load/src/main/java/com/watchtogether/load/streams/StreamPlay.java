package com.watchtogether.load.streams;

import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IMetaData;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;
import com.xuggle.xuggler.demos.VideoImage;

public class StreamPlay extends Thread {

	private int videoStreamId = -1;
	private IStreamCoder videoCoder = null;
	private int audioStreamId = -1;
	private IStreamCoder audioCoder = null;
	
	private IContainer inContainer;
	private IContainer outContainer;
	
	/**
	 * The audio line we'll output sound to; it'll be the default audio device on your system if available
	 */
	//private static SourceDataLine mLine;

	/**
	 * The window we'll draw the video on. 
	 */
	//private static VideoImage mScreen = null;
	
	private boolean run = true;
	
	public StreamPlay(String url) {
		inContainer = createInput(url+" live=1");
	}
	
	private IContainer createInput(String url) {
		IContainer container = IContainer.make();
		
		IContainerFormat format = IContainerFormat.make();
		format.setInputFormat("flv");
		
		container.open(url, IContainer.Type.READ, null);
		container.queryStreamMetaData();		
		
		return container;
	}

	@Override
	public void run() {
		IPacket packet = IPacket.make();
	
		Map<Integer, IStreamCoder> knownStreams = new HashMap<Integer, IStreamCoder>();
		
		IVideoResampler resampler = null;
	    
		while(inContainer.readNextPacket(packet)>=0 && run)
		{
		   if (packet.isComplete()) {
		     if (knownStreams.get(packet.getStreamIndex()) == null) {
		    	inContainer.queryStreamMetaData();
		    	
		        // stream should now be set up correct
		        IStream stream = inContainer.getStream(packet.getStreamIndex());
		        IMetaData metaData  = stream.getMetaData();
				for (String key:metaData.getKeys()) {
					System.out.println(key+": "+metaData.getValue(key));
				}
				
		        if (stream.getStreamCoder().getCodecType() != ICodec.Type.CODEC_TYPE_UNKNOWN) {
			        knownStreams.put(packet.getStreamIndex(), stream.getStreamCoder());
			        
			        IStreamCoder coder = knownStreams.get(packet.getStreamIndex());
				     
				     if (videoStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
				     {
				    	 videoStreamId = packet.getStreamIndex();
				         videoCoder = coder;
				         
				         if (videoCoder != null)
					     {
					       if(videoCoder.open() < 0)
					         throw new RuntimeException("could not open video decoder for container:");
					     
					       if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
					       {
					         // if this stream is not in BGR24, we're going to need to
					         // convert it.  The VideoResampler does that for us.
					         resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24,
					             videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
					         if (resampler == null)
					           throw new RuntimeException("could not create color space resampler for:");
					       }
					       /*
					        * And once we have that, we draw a window on screen
					        */
					      // openJavaVideo();
					     }
				     }
				     else if (audioStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
				     {
				    	 audioStreamId = packet.getStreamIndex();
				         audioCoder = coder;
				         
				         if (audioCoder != null)
					     {
					       if (audioCoder.open() < 0)
					         throw new RuntimeException("could not open audio decoder for container:");
					       
					       /*
					        * And once we have that, we ask the Java Sound System to get itself ready.
					        */
//					       try
//					       {
//					         openJavaSound(audioCoder);
//					       }
//					       catch (LineUnavailableException ex)
//					       {
//					         throw new RuntimeException("unable to open sound device on your system when playing back container:");
//					       }
					     }
				     }
		        }
		     }
		     
		     if (packet.getStreamIndex() == videoStreamId)
		      {
		        /*
		         * We allocate a new picture to get the data out of Xuggler
		         */
		        IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
		            videoCoder.getWidth(), videoCoder.getHeight());
		        
		        /*
		         * Now, we decode the video, checking for any errors.
		         * 
		         */
		        int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
		        if (bytesDecoded < 0)
		          throw new RuntimeException("got error decoding video in: ");

		        /*
		         * Some decoders will consume data in a packet, but will not be able to construct
		         * a full video picture yet.  Therefore you should always check if you
		         * got a complete picture from the decoder
		         */
		        if (picture.isComplete())
		        {
		          IVideoPicture newPic = picture;
		          /*
		           * If the resampler is not null, that means we didn't get the video in BGR24 format and
		           * need to convert it into BGR24 format.
		           */
		          if (resampler != null)
		          {
		            // we must resample
		            newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
		            if (resampler.resample(newPic, picture) < 0)
		              throw new RuntimeException("could not resample video from:");
		          }
		          if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
		            throw new RuntimeException("could not decode video as BGR 24 bit data in:");

		          // And finally, convert the picture to an image and display it

		          //mScreen.setImage(Utils.videoPictureToImage(newPic));
		        }
		      }
		     else if (packet.getStreamIndex() == audioStreamId)
		      {
		        /*
		         * We allocate a set of samples with the same number of channels as the
		         * coder tells us is in this buffer.
		         * 
		         * We also pass in a buffer size (1024 in our example), although Xuggler
		         * will probably allocate more space than just the 1024 (it's not important why).
		         */
		        IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());
		        
		        /*
		         * A packet can actually contain multiple sets of samples (or frames of samples
		         * in audio-decoding speak).  So, we may need to call decode audio multiple
		         * times at different offsets in the packet's data.  We capture that here.
		         */
		        int offset = 0;
		        
		        /*
		         * Keep going until we've processed all data
		         */
		        while(offset < packet.getSize())
		        {
		          int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
		          if (bytesDecoded < 0)
		            throw new RuntimeException("got error decoding audio in: ");
		          offset += bytesDecoded;
		          /*
		           * Some decoder will consume data in a packet, but will not be able to construct
		           * a full set of samples yet.  Therefore you should always check if you
		           * got a complete set of samples from the decoder
		           */
		          if (samples.isComplete())
		          {
		            // note: this call will block if Java's sound buffers fill up, and we're
		            // okay with that.  That's why we have the video "sleeping" occur
		            // on another thread.
		            //playJavaSound(samples);
		          }
		        }
		      }
		      else
		      {
		        /*
		         * This packet isn't part of our video stream, so we just silently drop it.
		         */
		        do {} while(false);
		      }
		   }
		}
		
//		closeJavaSound();
//		closeJavaVideo();
	}
	
//	private static void openJavaVideo()
//	  {
//	    mScreen = new VideoImage();
//	  }
//
//	  /**
//	   * Forces the swing thread to terminate; I'm sure there is a right
//	   * way to do this in swing, but this works too.
//	   */
//	  private static void closeJavaVideo()
//	  {
//		  mScreen.dispose();
//	  }
//
//	  private static void openJavaSound(IStreamCoder aAudioCoder) throws LineUnavailableException
//	  {
//	    AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
//	        (int)IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
//	        aAudioCoder.getChannels(),
//	        true, /* xuggler defaults to signed 16 bit samples */
//	        false);
//	    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
//	    mLine = (SourceDataLine) AudioSystem.getLine(info);
//	    /**
//	     * if that succeeded, try opening the line.
//	     */
//	    mLine.open(audioFormat);
//	    /**
//	     * And if that succeed, start the line.
//	     */
//	    mLine.start();
//	    
//	    
//	  }
//
//	  private static void playJavaSound(IAudioSamples aSamples)
//	  {
//	    /**
//	     * We're just going to dump all the samples into the line.
//	     */
//	    byte[] rawBytes = aSamples.getData().getByteArray(0, aSamples.getSize());
//	    mLine.write(rawBytes, 0, aSamples.getSize());
//	  }
//
//	  private static void closeJavaSound()
//	  {
//	    if (mLine != null)
//	    {
//	      /*
//	       * Wait for the line to finish playing
//	       */
//	      mLine.drain();
//	      /*
//	       * Close the line.
//	       */
//	      mLine.close();
//	      mLine=null;
//	    }
//	  }
	  
	  public void stopPlay() {
		  run = false;
	  }
}
