package dev.httpmarco.polocloud.node.commands;

import dev.httpmarco.polocloud.api.groups.ClusterGroup;
import dev.httpmarco.polocloud.api.services.ClusterService;
import dev.httpmarco.polocloud.node.cluster.ClusterProvider;
import dev.httpmarco.polocloud.node.cluster.NodeEndpoint;
import dev.httpmarco.polocloud.node.commands.specific.*;
import dev.httpmarco.polocloud.node.commands.type.*;
import dev.httpmarco.polocloud.api.groups.ClusterGroupProvider;
import dev.httpmarco.polocloud.node.module.LoadedModule;
import dev.httpmarco.polocloud.node.module.ModuleProvider;
import dev.httpmarco.polocloud.node.platforms.Platform;
import dev.httpmarco.polocloud.node.platforms.PlatformService;
import dev.httpmarco.polocloud.node.platforms.PlatformVersion;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class CommandArgumentType {

    public @NotNull CommandArgument<LoadedModule> ModuleArgument(ModuleProvider moduleProvider, String key) {
        return new ModuleArgument(key, moduleProvider);
    }

    public @NotNull CommandArgument<ClusterGroup> ClusterGroup(ClusterGroupProvider groupService, String key) {
        return new GroupArgument(key, groupService);
    }

    public @NotNull CommandArgument<ClusterService> ClusterService(String key) {
        return new ServiceArgument(key);
    }

    public @NotNull CommandArgument<PlatformVersion> PlatformVersion(String key) {
        return new PlatformBindVersionArgument(key);
    }

    public @NotNull CommandArgument<Platform> Platform(PlatformService platformService, String key) {
        return new PlatformArgument(key, platformService);
    }

    public @NotNull <E extends Enum<E>> CommandArgument<E> Enum(Class<E> enumClass, String key) {
        return new EnumArgument<>(enumClass, key);
    }

    public @NotNull CommandArgument<NodeEndpoint> NodeEndpoint(ClusterProvider clusterProvider, String key) {
        return new NodeEndpointArgument(key, clusterProvider);
    }

    @Contract("_ -> new")
    public @NotNull CommandArgument<Integer> Integer(String key) {
        return new IntArgument(key);
    }

    @Contract("_ -> new")
    public @NotNull CommandArgument<String> StringArray(String key) {
        return new StringArrayArgument(key);
    }

    @Contract("_ -> new")
    public @NotNull CommandArgument<Boolean> Boolean(String key) {
        return new BooleanArgument(key);
    }


    @Contract("_ -> new")
    public @NotNull CommandArgument<String> Text(String key) {
        return new TextArgument(key);
    }

    @Contract("_ -> new")
    public @NotNull CommandArgument<String> Keyword(String key) {
        return new KeywordArgument(key);
    }
}
