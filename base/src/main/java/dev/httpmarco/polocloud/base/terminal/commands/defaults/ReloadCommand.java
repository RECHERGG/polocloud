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

package dev.httpmarco.polocloud.base.terminal.commands.defaults;

import dev.httpmarco.polocloud.api.CloudAPI;
import dev.httpmarco.polocloud.api.groups.CloudGroup;
import dev.httpmarco.polocloud.base.CloudBase;
import dev.httpmarco.polocloud.base.groups.CloudGroupProvider;
import dev.httpmarco.polocloud.base.terminal.commands.Command;
import dev.httpmarco.polocloud.base.terminal.commands.DefaultCommand;

@Command(command = "reload", aliases = {"rl"}, description = "Reload all configurations and services")
public final class ReloadCommand {

    @DefaultCommand
    public void handle() {

        var time = System.currentTimeMillis();
        CloudBase.instance().logger().info("Reloading network cluster...");

        CloudBase.instance().logger().info("Reloading groups...");
        ((CloudGroupProvider) CloudAPI.instance().groupProvider()).reload();

        CloudBase.instance().logger().info("Share new cloud group information...");
        for (var group : CloudAPI.instance().groupProvider().groups()) {
            group.update();
        }

        CloudAPI.instance().logger().success("Reload successfully in " + (System.currentTimeMillis() - time) + "ms");
    }

}
