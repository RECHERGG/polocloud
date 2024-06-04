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

package dev.httpmarco.polocloud.base.templates;

import dev.httpmarco.osgan.files.Files;
import dev.httpmarco.osgan.files.json.JsonDocument;
import dev.httpmarco.polocloud.api.groups.GroupProperties;
import dev.httpmarco.polocloud.base.CloudBase;
import dev.httpmarco.polocloud.base.common.PropertiesPoolSerializer;
import dev.httpmarco.polocloud.base.services.LocalCloudService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.List;

@Getter
@Accessors(fluent = true)
public final class TemplatesService {

    public static final Path TEMPLATES = Path.of("templates");
    public static final Path STATIC = Path.of("static");

    @Getter(AccessLevel.PACKAGE)
    private final JsonDocument<TemplatesConfig> document;

    public TemplatesService() {
        Files.createDirectoryIfNotExists(TEMPLATES);

        this.document = new JsonDocument<>(new TemplatesConfig(), TEMPLATES.resolve("templates.json"), new PropertiesPoolSerializer());

        this.createTemplates("every");
        this.createTemplates("every_server");
        this.createTemplates("every_proxy");
    }

    public boolean createTemplates(String name, String... mergedTemplates) {
        if (isTemplate(name)) {
            return false;
        }

        var directory = TEMPLATES.resolve(name);
        var template = new Template(name, mergedTemplates);
        Files.createDirectoryIfNotExists(directory);
        templates().add(template);
        this.document.updateDocument();

        return true;
    }

    public boolean isTemplate(String id) {
        return templates().stream().anyMatch(it -> it.id().equalsIgnoreCase(id));
    }

    public void cloneTemplate(LocalCloudService service) {
        if (service.group().properties().has(GroupProperties.TEMPLATES)) {
            var temp = CloudBase.instance().templatesService().template(service.group().properties().property(GroupProperties.TEMPLATES));
            if (temp != null) {
                temp.copy(service);
            }
        }

        if (service.group().properties().has(GroupProperties.STATIC)) {
            Files.createDirectoryIfNotExists(STATIC);
        }
    }

    public boolean deleteTemplate(String name) {
        if (!isTemplate(name)) {
            return false;
        }

        for (var file : TEMPLATES.toFile().listFiles()) {
            if (!file.getName().equalsIgnoreCase(name)) {
                return false;
            }
            file.delete();
            this.templates().remove(name);
            this.document.value().templates().remove(name);
            this.document.updateDocument();
        }
        return true;
    }

    public List<Template> templates() {
        return this.document.value().templates();
    }

    public Template template(String name) {
        return templates().stream().filter(it -> it.id().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
