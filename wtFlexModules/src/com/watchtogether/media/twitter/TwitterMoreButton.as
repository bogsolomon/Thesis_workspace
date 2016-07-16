package com.watchtogether.media.twitter
{
	import com.watchtogether.media.twitter.constants.TwitterConstants;

	public class TwitterMoreButton
	{
		[Embed(source="com/watchtogether/media/twitter/images/twitter.png")]
		public var thumbnailUrl:Class;
		public var title:String = "more";
		public var subtitle:String = "";
		public var mode:String=null;
		public var count:Number = TwitterConstants.DEFAULT_RESULT_COUNT;
		public function TwitterMoreButton(){}
	}
}