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

package dev.httpmarco.polocloud.base.services;

import dev.httpmarco.polocloud.api.CloudAPI;
import dev.httpmarco.polocloud.api.CloudProperty;
import dev.httpmarco.polocloud.api.groups.GroupProperties;
import dev.httpmarco.polocloud.api.services.CloudServiceProvider;
import dev.httpmarco.polocloud.api.services.ServiceState;
import dev.httpmarco.polocloud.base.CloudBase;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class CloudServiceQueue extends Thread {

    private CloudServiceProvider serviceProvider;

    @Override
    public void run() {
        while (!isInterrupted()) {
            if (!CloudBase.instance().running()) {
                return;
            }
            for (var group : CloudAPI.instance().groupProvider().groups()) {

                var onlineDiff = group.onlineAmount() - group.minOnlineService();

                if (onlineDiff < 0) {

                    var maxValue = group.properties().has(GroupProperties.MAX_SERVICES) ? group.properties().property(GroupProperties.MAX_SERVICES) : -1;

                    for (int i = 0; i < (-onlineDiff); i++) {

                        if (maxValue != -1 && (group.onlineAmount() + 1) > maxValue) {
                            continue;
                        }

                        var currentStartedServices = CloudAPI.instance().serviceProvider().services().stream().filter(it -> it.state() == ServiceState.STARTING).count();

                        if (CloudBase.instance().globalProperties().has(CloudProperty.MAX_QUEUE_SIZE)) {
                            if (CloudBase.instance().globalProperties().property(CloudProperty.MAX_QUEUE_SIZE) > currentStartedServices) {
                                serviceProvider.factory().start(group);
                            }

                        } else {
                            serviceProvider.factory().start(group);
                        }
                    }
                }
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignore) {
        }
    }
}
