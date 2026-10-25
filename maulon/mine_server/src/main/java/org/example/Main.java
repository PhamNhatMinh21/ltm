package org.example;


import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.ping.Status;

import java.io.IOException;
import java.nio.file.Path;


public class Main {
    static void main() throws IOException {
        MinecraftServer server = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();


        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        Path polarPath = Path.of("world.polar");
        instanceContainer.setChunkLoader(new PolarLoader(polarPath));

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 100, 0));
        });
        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> {
            event.setStatus(Status.builder()
                    .playerInfo(MinecraftServer.getConnectionManager().getOnlinePlayerCount(), 5)
                    .build());
        });
        server.start("0.0.0.0", 25565);
    }
}
