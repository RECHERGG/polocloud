package dev.httpmarco.polocloud.node.players;

import dev.httpmarco.osgan.networking.packet.Packet;
import dev.httpmarco.polocloud.api.packet.resources.player.PlayerActionBarPacket;
import dev.httpmarco.polocloud.api.packet.resources.player.PlayerConnectPacket;
import dev.httpmarco.polocloud.api.packet.resources.player.PlayerMessagePacket;
import dev.httpmarco.polocloud.api.packet.resources.player.PlayerTitlePacket;
import dev.httpmarco.polocloud.api.players.AbstractClusterPlayer;
import dev.httpmarco.polocloud.api.services.ClusterService;
import dev.httpmarco.polocloud.node.Node;
import dev.httpmarco.polocloud.node.services.ClusterLocalServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@Accessors(fluent = true)
public final class ClusterPlayerImpl extends AbstractClusterPlayer {

    private final ClusterService currentProxy;

    @Setter
    @Nullable
    private ClusterService currentServer;

    public ClusterPlayerImpl(String name, UUID uniqueId, @NotNull ClusterService currentProxy) {
        super(name, uniqueId, "unknown", currentProxy.name());
        this.currentProxy = currentProxy;
    }

    public ClusterPlayerImpl(String name, UUID uniqueId, ClusterService currentProxy, @Nullable ClusterService currentServer) {
        super(name, uniqueId, currentServer.name(), currentProxy.name());
        this.currentProxy = currentProxy;
        this.currentServer = currentServer;
    }

    @Override
    public String currentProxyName() {
        return this.currentProxy.name();
    }

    @Override
    public String currentServerName() {
        return this.currentServer.name();
    }

    @Override
    public CompletableFuture<ClusterService> currentProxyAsync() {
        return CompletableFuture.completedFuture(currentProxy);
    }

    @Override
    public CompletableFuture<ClusterService> currentServerAsync() {
        return CompletableFuture.completedFuture(currentServer);
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        sendPlayerAction(new PlayerTitlePacket(uniqueId(), title, subtitle, fadeIn, stay, fadeOut));
    }

    @Override
    public void sendTablist(String header, String footer) {

    }

    @Override
    public void sendMessage(String message) {
        sendPlayerAction(new PlayerMessagePacket(uniqueId(), message));
    }

    @Override
    public void sendActionBar(String message) {
        sendPlayerAction(new PlayerActionBarPacket(uniqueId(), message));
    }

    @Override
    public void connect(String serviceId) {
        sendPlayerAction(new PlayerConnectPacket(uniqueId(), serviceId));
    }

    private void sendPlayerAction(Packet packet) {
        if (currentProxy instanceof ClusterLocalServiceImpl service) {
            service.transmit().sendPacket(packet);
            return;
        }
        Node.instance().clusterProvider().find(currentProxy.runningNode()).transmit().sendPacket(packet);
    }
}
