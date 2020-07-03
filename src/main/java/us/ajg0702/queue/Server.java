package us.ajg0702.queue;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Server {
	String name;
	ServerInfo info;
	public Server(String name, ServerInfo info) {
		this.name = name;
		this.info = info;
		update();
	}
	
	public String getName() {
		return name;
	}
	public ServerInfo getInfo() {
		return info;
	}
	
	int offlineTime = 0;
	boolean online = false;
	int playercount = 0;
	int maxplayers = 0;
	long lastUpdate = -1;
	public void update() {
		info.ping(new Callback<ServerPing>() {
			@Override
			public void done(ServerPing result, Throwable error) {
				online = error == null;
				
				/*if(error != null) {
					ProxyServer.getInstance().getLogger().info("[ajQueue] [pinger] Status: "+online+".  Error: ");
					error.printStackTrace();
				} else {
					ProxyServer.getInstance().getLogger().info("[ajQueue] [pinger] Status: "+online+".  motd: "+result.getDescriptionComponent());
				}*/
				
				
				if(lastUpdate == -1) {
					lastUpdate = System.currentTimeMillis();
					offlineTime = 0;
				} else {
					int timesincelast = Math.round((System.currentTimeMillis() - lastUpdate)/1000);
					lastUpdate = System.currentTimeMillis();
					if(!online) {
						offlineTime += timesincelast;
					} else {
						offlineTime = 0;
					}
				}
				if(!online) {
					playercount = 0;
					maxplayers = 0;
					return;
				}
				
				playercount = result.getPlayers().getOnline();
				maxplayers = result.getPlayers().getMax();
			}
		});
	}
	
	public int getOfflineTime() {
		return offlineTime;
	}
	
	public boolean isOnline() {
		return online;
	}
	public boolean isFull() {
		return playercount >= maxplayers;
	}
	
	
	List<ProxiedPlayer> queue = new ArrayList<>();
	public List<ProxiedPlayer> getQueue() {
		return queue;
	}
	
	
	public boolean canAccess(ProxiedPlayer ply) {
		return info.canAccess(ply);
	}
	
	
	boolean whitelisted = false;
	List<String> whitelistedplayers = new ArrayList<>();
	public void setWhitelisted(boolean b) {
		whitelisted = b;
	}
	public void setWhitelistedPlayers(List<String> plys) {
		whitelistedplayers = plys;
	}
	public boolean getWhitelisted() {
		return whitelisted;
	}
	public boolean isWhitelisted() {
		return whitelisted;
	}
	public List<String> getWhitelistedPlayers() {
		return whitelistedplayers;
	}
}
