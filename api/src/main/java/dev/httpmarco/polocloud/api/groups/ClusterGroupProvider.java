package dev.httpmarco.polocloud.api.groups;

import dev.httpmarco.osgan.networking.packet.PacketBuffer;
import dev.httpmarco.polocloud.api.Reloadable;
import dev.httpmarco.polocloud.api.Sendable;
import dev.httpmarco.polocloud.api.platforms.PlatformGroupDisplay;
import dev.httpmarco.polocloud.api.properties.PropertiesBuffer;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class ClusterGroupProvider implements Sendable<ClusterGroup> {

    public abstract CompletableFuture<Set<ClusterGroup>> groupsAsync();

    @SneakyThrows
    public Set<ClusterGroup> groups() {
        return groupsAsync().get(5, TimeUnit.SECONDS);
    }

    public abstract CompletableFuture<Boolean> existsAsync(String group);

    @SneakyThrows
    public boolean exists(String group) {
        return this.existsAsync(group).get(5, TimeUnit.SECONDS);
    }

    public abstract CompletableFuture<Optional<String>> deleteAsync(String group);

    @SneakyThrows
    public Optional<String> delete(String group) {
        return deleteAsync(group).get(5, TimeUnit.SECONDS);
    }

    public abstract CompletableFuture<Optional<String>> createAsync(String name, String[] nodes, PlatformGroupDisplay platform, int maxMemory, boolean staticService, int minOnline, int maxOnline);

    @SneakyThrows
    public Optional<String> create(String name, String[] nodes, PlatformGroupDisplay platform, int maxMemory, boolean staticService, int minOnline, int maxOnline) {
        return this.createAsync(name, nodes, platform, maxMemory, staticService, minOnline, maxOnline).get(5, TimeUnit.SECONDS);
    }

    public abstract CompletableFuture<ClusterGroup> findAsync(@NotNull String group);

    @SneakyThrows
    public @Nullable ClusterGroup find(@NotNull String group) {
        return this.findAsync(group).get(5, TimeUnit.SECONDS);
    }

    @Override
    public void write(@NotNull ClusterGroup group, @NotNull PacketBuffer buffer) {
        buffer.writeString(group.name());
        buffer.writeInt(group.maxMemory());
        buffer.writeInt(group.maxPlayers());
        buffer.writeInt(group.minOnlineServerInstances());
        buffer.writeInt(group.maxOnlineServerInstances());
        buffer.writeBoolean(group.staticService());

        buffer.writeString(group.platform().platform());
        buffer.writeString(group.platform().version());
        buffer.writeEnum(group.platform().type());

        buffer.writeInt(group.nodes().length);
        for (var node : group.nodes()) {
            buffer.writeString(node);
        }

        buffer.writeInt(group.templates().length);
        for (var template : group.templates()) {
            buffer.writeString(template);
        }

        PropertiesBuffer.write(group.properties(), buffer);
    }
}
