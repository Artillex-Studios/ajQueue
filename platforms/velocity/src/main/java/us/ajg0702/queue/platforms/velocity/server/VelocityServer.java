package us.ajg0702.queue.platforms.velocity.server;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerInfo;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.queue.platforms.velocity.players.VelocityPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class VelocityServer implements AdaptedServer {

    private final RegisteredServer handle;

    private AdaptedServerPing lastPing = null;
    private AdaptedServerPing lastSuccessfullPing = null;
    private long lastOffline = 0;

    private int offlineTime = 0;

    public VelocityServer(RegisteredServer handle) {
        this.handle = handle;
    }

    @Override
    public AdaptedServerInfo getServerInfo() {
        return new VelocityServerInfo(handle.getServerInfo());
    }

    @Override
    public String getName() {
        return handle.getServerInfo().getName();
    }

    @Override
    public CompletableFuture<AdaptedServerPing> ping(boolean debug, QueueLogger logger) {
        CompletableFuture<AdaptedServerPing> future = new CompletableFuture<>();

        long sent = System.currentTimeMillis();

        CompletableFuture<ServerPing> serverPing = handle.ping();

        if(debug) logger.info("[pinger] [" + getName() + "] sending ping");

        serverPing.thenRunAsync(() -> {
            VelocityServerPing ping;
            try {
                ping = new VelocityServerPing(serverPing.get(), sent);
            } catch (Throwable e) {
                markOffline(debug, logger, future, sent, e);
                return;
            }

            offlineTime = 0;
            lastSuccessfullPing = ping;

            if(debug) logger.info(
                    "[pinger] [" + getName() + "] online. motd: "+ping.getPlainDescription()+" " +
                            " players: "+ping.getPlayerCount()+"/"+ping.getMaxPlayers()
            );

            future.complete(ping);
            lastPing = ping;
        }).exceptionally(e -> {
            markOffline(debug, logger, future, sent, e);
            return null;
        });
        return future;
    }

    private void markOffline(boolean debug, QueueLogger logger, CompletableFuture<AdaptedServerPing> future, long sent, Throwable e) {
        long lastOnline = lastSuccessfullPing == null ? 0 : lastSuccessfullPing.getFetchedTime();
        offlineTime = (int) Math.min(sent - lastOnline, Integer.MAX_VALUE);

        lastOffline = sent;

        future.completeExceptionally(e);
        lastPing = null;
        if(debug) logger.info("[pinger] [" + getName() + "] offline:", e);
    }

    @Override
    public Optional<AdaptedServerPing> getLastPing() {
        return Optional.ofNullable(lastPing);
    }

    @Override
    public boolean canAccess(AdaptedPlayer player) {
        return true;
    }

    @Override
    public List<AdaptedPlayer> getPlayers() {
        List<AdaptedPlayer> players = new ArrayList<>();
        for(Player player : handle.getPlayersConnected()) {
            players.add(new VelocityPlayer(player));
        }
        return players;
    }

    @Override
    public int getOfflineTime() {
        return offlineTime;
    }

    @Override
    public boolean canJoinFull(AdaptedPlayer player) {
        if(player == null) return true;
        Debug.info("on "+getName());
        return
                player.hasPermission("ajqueue.joinfull") ||
                        player.hasPermission("ajqueue.joinfullserver."+getName()) ||
                        player.hasPermission("ajqueue.joinfullandbypassserver."+getName()) ||
                        player.hasPermission("ajqueue.joinfullandbypass") ||
                        (AjQueueAPI.getInstance().isPremium() && AjQueueAPI.getInstance().getLogic().getPermissionGetter().hasUniqueFullBypass(player, getName()))
                ;
    }

    @Override
    public boolean justWentOnline() {
        return System.currentTimeMillis()-lastOffline <= (AjQueueAPI.getInstance().getConfig().getDouble("wait-time") * 2 * 1000) && isOnline();
    }

    @Override
    public RegisteredServer getHandle() {
        return handle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VelocityServer)) return false;
        VelocityServer that = (VelocityServer) o;
        return getHandle().equals(that.getHandle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHandle());
    }
}
