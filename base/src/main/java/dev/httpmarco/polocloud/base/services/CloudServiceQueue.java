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
import dev.httpmarco.polocloud.api.services.CloudServiceProvider;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class CloudServiceQueue extends Thread {

    private CloudServiceProvider serviceProvider;

    @Override
    public void run() {
        while (!isInterrupted()) {
            for (var group : CloudAPI.instance().groupProvider().groups()) {
                var onlineDiff = group.onlineAmount() - group.minOnlineServices();

                if (onlineDiff < 0) {
                    for (int i = 0; i < (-onlineDiff); i++) {
                        serviceProvider.factory().start(group);
                    }
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignore) {
            }
        }
    }
}
