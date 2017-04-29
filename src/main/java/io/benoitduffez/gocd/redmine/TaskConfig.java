/*
 * Copyright 2017 ThoughtWorks, Inc.
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

package io.benoitduffez.gocd.redmine;

import java.io.IOException;
import java.util.Map;

public class TaskConfig {
    private final String projectId;
    private final String versionId;
    private final String filePath;

    public TaskConfig(Map config) throws IOException {
        projectId = getValue(config, TaskPlugin.PROJECT_ID_PROPERTY);
        versionId = getValue(config, TaskPlugin.VERSION_ID_PROPERTY);
        filePath = getValue(config, TaskPlugin.FILE_PATH_PROPERTY);
    }

    private String getValue(Map config, String property) {
        return (String) ((Map) config.get(property)).get("value");
    }

    public String getProjectId() {
        return projectId;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getFilePath() {
        return filePath;
    }
}
