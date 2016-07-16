package com.watchtogether.media.game.controller
{
	import com.watchtogether.code.Configurator;
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.game.GameModel;
	import com.watchtogether.media.game.GameModuleViewer;
	import com.watchtogether.media.game.PlayerModel;
	import com.watchtogether.media.game.TreeModel;
	import com.watchtogether.media.game.api.IGameUserControlController;
	import com.watchtogether.media.game.api.IGameViewerController;
	import com.watchtogether.media.game.constants.GameModuleConstants;
	import com.watchtogether.media.game.internals.Bullet;
	import com.watchtogether.media.game.internals.EnemyPlayerEntity;
	import com.watchtogether.media.game.internals.GameEngine;
	import com.watchtogether.media.game.internals.GameEntity;
	import com.watchtogether.media.game.internals.PlayerDeathEvent;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.system.ApplicationDomain;
	import flash.system.LoaderContext;
	import flash.system.SecurityDomain;
	
	import mx.collections.ArrayCollection;
	import mx.core.FlexGlobals;
	import mx.modules.Module;
	
	import net.flashpunk.Engine;
	import net.flashpunk.Entity;
	import net.flashpunk.FP;
	
	public class GameModuleViewerController extends ViewerController implements IGameViewerController
	{
		[Bindable]
		public var view:GameModuleViewer;
		
		private var userControlController:IGameUserControlController;
		private var displayInfoController:DisplayInfoController;
		
		private var gameEngine:GameEngine;
		
		private static var MAX_NUMBER_PLAYERS:int = 4; 
		
		private static var TREE_STUMPS:int = 10;
		
		private var gameModel:GameModel;
		
		private var treeCount:int = 0;
		
		public function GameModuleViewerController()
		{
		}
		
		
		public function init():void
		{
			var userControl:Object = (contentViewer.getUserControl().child as Object);
			var displayInfo:Object = (contentViewer.getDisplayInfo().child as Object);
			
			userControlController = (userControl.controller as IGameUserControlController);
			displayInfoController = (displayInfo.controller as DisplayInfoController);
			
			var app:main = (FlexGlobals.topLevelApplication as main);
			
			var stageWidth:int = app.stage.width;
			var xOffset:int = (stageWidth - view.width)/2;
			
			gameEngine = new GameEngine(userControlController, xOffset)
			view.rawChildren.addChild(gameEngine);
			
			MainApplication.instance.dispatcher.addEventListener(PlayerDeathEvent.DEATH_EVENT, deathEvent);
			
			this.initComplete(userControlController as UserControlController, null);
			setUnloadEvent();
		}
		
		private function deathEvent(evt:PlayerDeathEvent):void {
			var label:String = "";
			
			for each(var plModel:PlayerModel in gameModel.playerData) {
				if (evt.id == plModel.id)
					plModel.addDeath();
				
				label = label + " <FONT COLOR='"+plModel.color+"'>"+plModel.userName+":"+plModel.deaths+"</FONT>";
			}
		
			displayInfoController.setDescription(label);	
		}
		
		override public function initExistingModule(event:MediaViewerEvent):void
		{
			var type:String = event.viewerType;
			type = type.substring(type.lastIndexOf("/")+1, type.length-4);
			
			if (type == "GameModuleViewer" && this.contentViewer.desktopId == event.viewerId) {
				initComplete(userControlController as UserControlController, null);
				setUnloadEvent();
			}
		}
		
		override public function remove(event:MediaViewerEvent):void {
			if (this.contentViewer.desktopId == event.viewerId) {
				setLoadEvent();
			}
		}
		
		private function handleIOError(evtObj:IOErrorEvent):void
		{
			trace(evtObj.text);
		}
		
		public function onLoaderInit(event:Event):void {
			trace(event.type);
		}
		
		
//		public function remove():void {
//			
//		}
		
		//
		// Methods implemented from ViewerController
		//
		override public function getSynchState():Array {
			var playerData:ArrayCollection = gameModel.playerData;
			var treeData:ArrayCollection = gameModel.treeData;
			
			var localPlayer:Entity = gameEngine.world.typeFirst("player");
			
			for each(var plModel:PlayerModel in playerData) {
				if (plModel.id == gameModel.localPlayerID) {
					plModel.x = localPlayer.x;
					plModel.y = localPlayer.y;
				}
			}
			
			var bullets:Array = new Array();
			
			gameEngine.world.getType("bullet", bullets);
			
			var bulletsToSend:Array = new Array(bullets.length);
			
			for (var i:int=0;i<bullets.length;i++) {
				bulletsToSend[i] = new Object();
				bulletsToSend[i].x = (bullets[i] as Bullet).x;
				bulletsToSend[i].y = (bullets[i] as Bullet).y;
				bulletsToSend[i].angle = (bullets[i] as Bullet).angle;
				bulletsToSend[i].origId = ((bullets[i] as Bullet).origEntity as GameEntity).playerId;
			}
			
			return [playerData.source, treeData.source, bulletsToSend];
		}
		
		override public function synch(data:Array):void {
			gameModel = new GameModel();
			
			var playerData:Array = data[0];
			var treeData:Array = data[1];
			var bullets:Array = data[2];
			
			for (var i:int=0;i<playerData.length;i++) {
				var plModel:PlayerModel = new PlayerModel();
				
				plModel.deaths = playerData[i].deaths;
				plModel.id = playerData[i].id;
				plModel.userName = playerData[i].userName;
				plModel.x = playerData[i].x;
				plModel.y = playerData[i].y;
				
				gameModel.playerData.addItem(plModel);
				
				gameEngine.world.addNewEnemy(plModel.x, plModel.y, plModel.id);
			}
			
			for (i=0;i<treeData.length;i++) {
				var treeModel:TreeModel = new TreeModel();
				
				treeModel.x = treeData[i].x;
				treeModel.y = treeData[i].y;
				
				gameEngine.world.addTree(treeModel.x, treeModel.y);
				
				gameModel.treeData.addItem(treeModel);
			}
			
			for (i=0;i<bullets.length;i++) {
				gameEngine.world.addBullet(bullets[i].x, bullets[i].y, 
					bullets[i].angle, bullets[i].origId);
			}
			
			var startX:int = Math.random() * (gameEngine.width - 2*GameModuleConstants.LEFT_RIGHT_BORDER) + GameModuleConstants.LEFT_RIGHT_BORDER; 
			var startY:int = Math.random() * (gameEngine.height - 2*GameModuleConstants.TOP_BOTTOM_BORDER) +GameModuleConstants.TOP_BOTTOM_BORDER;
			var uid:Number = MainApplication.instance.login.loggedInUser.uid;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				GameModuleConstants.INIT_POS, [startX, startY, uid], true);
		}
		
		override public function command(command:String, data:Array):void {
			var uid:Number = MainApplication.instance.login.loggedInUser.uid;
			var sessSize:int = MainApplication.instance.sessionListDataProvider.length;
			var sess:ArrayCollection = MainApplication.instance.sessionListDataProvider;
			
			if (command ==  GameModuleConstants.LOAD) {
				if (gameEngine.started) {
					gameEngine.restart();
				}
				
				var startX:int = Math.random() * (gameEngine.width - 2*GameModuleConstants.LEFT_RIGHT_BORDER) + GameModuleConstants.LEFT_RIGHT_BORDER; 
				var startY:int = Math.random() * (gameEngine.height - 2*GameModuleConstants.TOP_BOTTOM_BORDER) +GameModuleConstants.TOP_BOTTOM_BORDER;
				
				MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
					contentViewer.getUserControlURL(),
					contentViewer.getDisplayInfoURL(),
					GameModuleConstants.INIT_POS, [startX, startY, uid], true);
				
				gameModel = new GameModel();
				
				if (MainApplication.instance.login.loggedInUser.isBoss || sessSize == 0) {
					var player:PlayerModel = new PlayerModel();
					player.x = startX;
					player.y = startY;
					player.id = gameModel.playerData.length;
					player.userName  = MainApplication.instance.login.loggedInUser.first_name+" "+MainApplication.instance.login.loggedInUser.last_name;
					
					gameModel.playerData.addItem(player);
					
					for (var i:int = 0; i<TREE_STUMPS; i++) {
						var treeX:int = Math.random() * (gameEngine.width - 2*GameModuleConstants.LEFT_RIGHT_BORDER) + GameModuleConstants.LEFT_RIGHT_BORDER;
						var treeY:int = Math.random() * (gameEngine.height - 2*GameModuleConstants.TOP_BOTTOM_BORDER) + GameModuleConstants.TOP_BOTTOM_BORDER;
						var tree:TreeModel = new TreeModel();
						tree.x = treeX;
						tree.y = treeY;
						gameModel.treeData.addItem(tree);
						
						MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
							contentViewer.getUserControlURL(),
							contentViewer.getDisplayInfoURL(),
							GameModuleConstants.ADD_TREE, [tree.x, tree.y], true);
					}
						
					MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
						contentViewer.getUserControlURL(),
						contentViewer.getDisplayInfoURL(),
						GameModuleConstants.INIT_POS_SYNCH, [player, uid], true);
					
					if (sessSize == 0) {
						gameEngine.start();
					}
				}
			} else if (command == GameModuleConstants.PLAYER_MOVED) {
				var x:int = data[0];
				var y:int = data[1];
				var id:int = data[2];
				
				for each(var plModel:PlayerModel in gameModel.playerData) {
					if (plModel.id == id) {
						plModel.x = x; 
						plModel.y = y;
					}
				}
				
				var enemyList:Array = [];
				
				gameEngine.world.getType("enemyPlayer"+id, enemyList);
				
				(enemyList[0] as EnemyPlayerEntity).setRemoteCoord(x,y);
			} else if (command ==  GameModuleConstants.NEW_BULLET) {
				x = data[0];
				y = data[1];
				var angle:Number = data[2];
				id = data[3];
				
				gameEngine.world.addBullet(x, y, angle, id);
			} else if (command ==  GameModuleConstants.RESPAWN) {
				x = data[0];
				y = data[1];
				id = data[2];
				
				gameEngine.world.addNewEnemy(x, y, id);
			} else if (command ==  GameModuleConstants.INIT_POS) {
				var remoteStartX:int = data[0];
				var remoteStartY:int = data[1];
				var remoteID:Number =  data[2];
				
				if (MainApplication.instance.login.loggedInUser.isBoss && remoteID != uid) {
					if (gameModel.playerData.length < MAX_NUMBER_PLAYERS) {
						for each(plModel in gameModel.playerData) {
							while (plModel.x == remoteStartX && plModel.y == remoteStartY) {
								remoteStartX = Math.random() * (gameEngine.width - 2*GameModuleConstants.LEFT_RIGHT_BORDER) + GameModuleConstants.LEFT_RIGHT_BORDER; 
								remoteStartY = Math.random() * (gameEngine.height - 2*GameModuleConstants.TOP_BOTTOM_BORDER) +GameModuleConstants.TOP_BOTTOM_BORDER;
							}
						}
							
						player = new PlayerModel();
						player.x = remoteStartX;
						player.y = remoteStartY;
						player.id = gameModel.playerData.length;
						gameModel.playerData.addItem(player);
						
						for (i=0; i<sessSize; i++) {
							var user:AbstractUser = sess.getItemAt(i) as AbstractUser;
							
							if (remoteID == user.uid) {
								player.userName = user.first_name+" "+user.last_name;
							}
						}
						
						MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
							contentViewer.getUserControlURL(),
							contentViewer.getDisplayInfoURL(),
							GameModuleConstants.INIT_POS_SYNCH, [player, remoteID], true);
						
						//gameModel.playerData.length - 1 because the localuser is not seen part of the 
						//sessionListDataProvider locally
						
						if (gameModel.playerData.length == MAX_NUMBER_PLAYERS || (gameModel.playerData.length - 1) == sessSize) {
							MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
								contentViewer.getUserControlURL(),
								contentViewer.getDisplayInfoURL(),
								GameModuleConstants.START_GAME, null, true);
						}
					} else {
						player = new PlayerModel();
						player.id = -1;
						
						MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
							contentViewer.getUserControlURL(),
							contentViewer.getDisplayInfoURL(),
							GameModuleConstants.INIT_POS_SYNCH, [player, remoteID], true);
					}
				}
			} else if (command ==  GameModuleConstants.INIT_POS_SYNCH) {
				player = new PlayerModel();
				player.id = data[0].id;
				player.x = data[0].x;
				player.y = data[0].y;
				remoteID = data[1];
				
				var found:Boolean = false;
				
				for each(plModel in gameModel.playerData) {
					if (player.id == plModel.id)
						found = true;
				}
				
				if (!found)
					gameModel.playerData.addItem(player);
				
				if (remoteID == uid) {
					if (player.id == -1) {
						//the local player is an observer
					} else {
						player.userName = MainApplication.instance.login.loggedInUser.first_name+" "+
							MainApplication.instance.login.loggedInUser.last_name;
						
						gameModel.localPlayerID = player.id;
						
						gameEngine.world.addLocalPlayer(player.x, player.y, player.id);
					}
				} else {
					if (player.id != -1) {
						gameEngine.world.addNewEnemy(player.x, player.y, player.id);
					}
					
					for (i=0; i<sessSize; i++) {
						user = sess.getItemAt(i) as AbstractUser;
						
						if (remoteID == user.uid) {
							player.userName = user.first_name+" "+user.last_name;
						}
					}
				}
			} else if (command ==  GameModuleConstants.START_GAME) {
				gameEngine.start();
				
				var label:String = "";
				
				for each(plModel in gameModel.playerData) {
					label = label + " <FONT COLOR='"+plModel.color+"'>"+plModel.userName+":"+plModel.deaths+"</FONT>";
				}
				
				displayInfoController.setDescription(label);
			} else if (command ==  GameModuleConstants.ADD_TREE) {
				treeX = data[0];
				treeY = data[1];
				
				trace("Adding tree "+(treeCount++)+" at "+treeX+","+treeY);
				
				gameEngine.world.addTree(treeX, treeY);
			}
		}
		
		override public function setSize(width:Number, height:Number, minimized:Boolean):void {
			if (!minimized) {
				view.width = width;
				view.height = height;
			}
			else {
				view.width = 608;
				view.height = 428;
			}
		}
	}
}

