package dev.httpmarco.polocloud.api.groups;

import dev.httpmarco.polocloud.api.CloudAPI;
import dev.httpmarco.polocloud.api.platforms.PlatformGroupDisplay;
import dev.httpmarco.polocloud.api.properties.PropertiesPool;
import dev.httpmarco.polocloud.api.services.ClusterService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public abstract class AbstractClusterGroup implements ClusterGroup {

    private final String name;
    private final PlatformGroupDisplay platform;

    private String[] templates;
    private String[] nodes;
    private int maxMemory;
    private int maxPlayers;
    private boolean staticService;
    private int minOnlineServerInstances;
    private int maxOnlineServerInstances;

    private final PropertiesPool properties;

    @Override
    public String details() {
        var defaultDetail = "platform&8=&7" + platform.details() + "&8, &7nodes&8=&7" + Arrays.toString(nodes) + ", &7maxMemory&8=&7" + maxMemory + "&8, &7static&8=&7" + staticService;

        if(this instanceof FallbackClusterGroup) {
            defaultDetail += " &8--fallback";
        }

        return defaultDetail;
    }

    @Override
    public long serviceCount() {
        return CloudAPI.instance().serviceProvider().services().stream().filter(it -> it.group().equals(this)).count();
    }

    @Override
    public List<ClusterService> services() {
        return CloudAPI.instance().serviceProvider().services().stream().filter(it -> it.group().equals(this)).toList();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClusterGroup clusterGroup && clusterGroup.name().equals(name);
    }
}
