package com.watchtogether.ui.session.skin
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.mediaserver.WebcamReceiveStream;
	
	import flash.media.SoundTransform;
	import flash.media.Video;
	
	import mx.core.UIComponent;
	
	public class VideoPanel extends UIComponent
	{
		private var _stream:WebcamReceiveStream;
		private var muted:Boolean = false;
		private var oldVolume:Number = 1;
		
		public function VideoPanel()
		{
			super();
		}
		
		public function get stream():WebcamReceiveStream
		{
			return _stream;
		}

		public function attachVideoStream(videoStreamName:String, width:int, height:int):void {
			var app:MainApplication = MainApplication.instance;
			var video:Video = new Video();
			_stream = new WebcamReceiveStream(app.mediaServerConnection);
			_stream.bufferTime = 0;
			_stream.play(videoStreamName);
			video.attachNetStream(_stream);
			app.sessionStreams.addItem(_stream);
			video.width = width;
			video.height = height;
			
			if (MainApplication.instance.session.mutedAll) {
				MainApplication.instance.session.oldSoundTransforms[_stream] = _stream.soundTransform;
				var st:SoundTransform = new SoundTransform();
				st.volume = 0;
				_stream.soundTransform = st;
			}
			
			this.addChild(video);
		}
		
		public function detachVideoStream():void {
			var app:MainApplication = MainApplication.instance;
			if (_stream != null) {
				_stream.close();
				app.sessionStreams.removeItemAt(app.sessionStreams.getItemIndex(_stream));
				_stream = null;
			}
		}
		
		public function muteUnmute():void {
			if (!muted) {
				oldVolume = _stream.soundTransform.volume;
				var soundTranf:SoundTransform = new SoundTransform();
				soundTranf.volume = 0;
				_stream.soundTransform = soundTranf;
				muted = true;
			} else {
				soundTranf = new SoundTransform();
				soundTranf.volume = oldVolume;
				_stream.soundTransform = soundTranf;
				muted = false;
			}
		}
		
		public function setVolume(vol:Number):void {
			oldVolume = vol / 100;
			var soundTranf:SoundTransform = new SoundTransform();
			soundTranf.volume = oldVolume;
			_stream.soundTransform = soundTranf;
		}
	}
}