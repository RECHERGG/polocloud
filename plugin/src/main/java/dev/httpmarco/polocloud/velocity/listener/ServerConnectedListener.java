/*
 * Copyright 2024 Mirco Lindenau | HttpMarco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.httpmarco.polocloud.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import dev.httpmarco.polocloud.api.CloudAPI;
import dev.httpmarco.polocloud.api.events.player.CloudPlayerSwitchServerEvent;
import dev.httpmarco.polocloud.api.packets.player.CloudPlayerServiceChangePacket;
import dev.httpmarco.polocloud.runner.CloudInstance;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ServerConnectedListener {

    @Subscribe
    public void onPlayerChooseInitialServer(ServerConnectedEvent event) {
        var player = event.getPlayer();
        var cloudPlayer = CloudAPI.instance().playerProvider().find(player.getUniqueId());
        var cloudService = CloudAPI.instance().serviceProvider().find(event.getServer().getServerInfo().getName());
        CloudInstance.instance().client().transmitter().sendPacket(new CloudPlayerServiceChangePacket(player.getUniqueId(), cloudService.id()));

        if (event.getPreviousServer().isEmpty()) return;
        var previousService = CloudAPI.instance().serviceProvider().find(event.getPreviousServer().get().getServerInfo().getName());
        CloudAPI.instance().globalEventNode().call(new CloudPlayerSwitchServerEvent(cloudPlayer, cloudService, previousService));
    }
}
