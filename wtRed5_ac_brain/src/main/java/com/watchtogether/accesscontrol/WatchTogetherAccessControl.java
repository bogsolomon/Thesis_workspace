package com.watchtogether.accesscontrol;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.ServiceUtils;
import org.slf4j.Logger;

import com.watchtogether.accesscontrol.groups.GroupManager;
import com.watchtogether.accesscontrol.util.UserLocation;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;

public class WatchTogetherAccessControl extends MultiThreadedApplicationAdapter {

	private Logger logger = null;

	private IClient clientListDisplay;
	private IScope clientListScope;

	@Override
	public boolean appStart(IScope scope) {
		logger = Red5LoggerFactory.getLogger(WatchTogetherAccessControl.class,
				scope.getName());
		logger.info(scope.getContextPath() + " appStart");

		GroupManager.getInstance().setCoreServer(this);

		return super.appStart(scope);
	}

	@Override
	public boolean connect(IConnection conn, IScope scope, Object[] params) {
		logger.info("connect");

		if (params != null && params.length > 0
				&& params[0].equals("clientListDisplay")) {
			clientListDisplay = conn.getClient();
			clientListScope = conn.getScope();
			return true;
		} else {

			UserLocation loc = getUserLocation(conn.getRemoteAddress());

			ServerApplicationMessage closestServ = GroupManager.getInstance()
					.getClosestServer(loc);

			if (closestServ != null) {
				ServiceUtils.invokeOnConnection(
						conn,
						"serverRedirect",
						new Object[] { closestServ.getHost(),
								closestServ.getPort(), closestServ.getApp() });
				return true;
			} else {
				logger.error("No servers found");
				return false;
			}
		}
	}

	public UserLocation getUserLocation(String addr) {
		UserLocation loc = new UserLocation();

		/*
		 * try { URL httpurl = new
		 * URL("http://ipinfodb.com/ip_query.php?timezone=false&ip="+addr);
		 * 
		 * HttpURLConnection con = (HttpURLConnection) httpurl.openConnection();
		 * con.setUseCaches (false); con.setDoInput(true);
		 * 
		 * con.setRequestMethod("GET");
		 * 
		 * InputStream is = con.getInputStream(); BufferedReader rd = new
		 * BufferedReader(new InputStreamReader(is)); String line;
		 * 
		 * while((line = rd.readLine()) != null) { if
		 * (line.contains("<Latitude>")) { loc.setLat(new
		 * Float(line.substring(line.indexOf("<Latitude>")+10,
		 * line.indexOf("</Latitude>")))); } else if
		 * (line.contains("<Longitude>")) { loc.setLongit(new
		 * Float(line.substring(line.indexOf("<Longitude>")+11,
		 * line.indexOf("</Longitude>")))); } }
		 * 
		 * if(con != null) { con.disconnect(); } } catch (MalformedURLException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		return loc;
	}

	public void addedClient(String clientID, ServerApplicationMessage serv) {
		ServiceUtils.invokeOnConnection(
				clientListDisplay.getConnections(clientListScope).iterator()
						.next(), "clientJoined", new Object[] {
						clientID,
						"rtmp://" + serv.getHost() + ":" + serv.getPort() + "/"
								+ serv.getApp() });
	}

	public void removedClient(String clientID, ServerApplicationMessage serv) {
		ServiceUtils.invokeOnConnection(
				clientListDisplay.getConnections(clientListScope).iterator()
						.next(), "clientLeft", new Object[] {
						clientID,
						"rtmp://" + serv.getHost() + ":" + serv.getPort() + "/"
								+ serv.getApp() });
	}
}
