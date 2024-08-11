package dev.httpmarco.polocloud.node;

import dev.httpmarco.polocloud.api.CloudAPI;
import dev.httpmarco.polocloud.api.groups.ClusterGroupProvider;
import dev.httpmarco.polocloud.node.cluster.ClusterProvider;
import dev.httpmarco.polocloud.node.cluster.ClusterProviderImpl;
import dev.httpmarco.polocloud.node.commands.CommandService;
import dev.httpmarco.polocloud.node.commands.CommandServiceImpl;
import dev.httpmarco.polocloud.node.groups.ClusterGroupProviderImpl;
import dev.httpmarco.polocloud.node.module.ModuleProvider;
import dev.httpmarco.polocloud.node.platforms.PlatformService;
import dev.httpmarco.polocloud.node.services.ClusterServiceProviderImpl;
import dev.httpmarco.polocloud.node.templates.TemplatesProvider;
import dev.httpmarco.polocloud.node.terminal.JLineTerminal;
import dev.httpmarco.polocloud.node.terminal.commands.ClusterCommand;
import dev.httpmarco.polocloud.node.terminal.commands.GroupCommand;
import dev.httpmarco.polocloud.node.terminal.commands.ServiceCommand;
import dev.httpmarco.polocloud.node.util.Configurations;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;

@Log4j2
@Getter
@Accessors(fluent = true)
public final class Node extends CloudAPI {

    @Getter
    private static Node instance;

    private final ClusterProvider clusterProvider;
    private final TemplatesProvider templatesProvider;
    private final PlatformService platformService;
    private final ClusterGroupProvider groupProvider;
    private final ClusterServiceProviderImpl serviceProvider;
    private final ModuleProvider moduleProvider;

    private final JLineTerminal terminal;
    private final CommandService commandService;

    public Node() {
        instance = this;

        var nodeConfig = Configurations.readContent(Path.of("config.json"), new NodeConfig());

        this.templatesProvider = new TemplatesProvider();
        this.clusterProvider = new ClusterProviderImpl(nodeConfig);
        this.moduleProvider = new ModuleProvider();

        this.commandService = new CommandServiceImpl();
        this.terminal = new JLineTerminal(nodeConfig);

        this.platformService = new PlatformService();
        this.groupProvider = new ClusterGroupProviderImpl(clusterProvider);
        this.serviceProvider = new ClusterServiceProviderImpl();

        // register provider commands
        this.commandService.registerCommands(new GroupCommand(), new ServiceCommand(), new ClusterCommand(this.clusterProvider));

        // start cluster and check other node
        this.clusterProvider.initialize();

        // load all Modules
        this.moduleProvider.loadAllUnloadedModules();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> NodeShutdown.nodeShutdown(false)));

        log.info("Cluster node boot successfully &8(&7Took {}ms&8)", System.currentTimeMillis() - Long.parseLong(System.getProperty("startup")));

        this.terminal.allowInput();
        this.serviceProvider.clusterServiceQueue().start();
    }
}
