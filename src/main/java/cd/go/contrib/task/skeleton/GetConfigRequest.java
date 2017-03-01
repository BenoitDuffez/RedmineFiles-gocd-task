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

package cd.go.contrib.task.skeleton;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;

// TODO: change this to allow configuration options in your configuration
public class GetConfigRequest {

    public GoPluginApiResponse execute() {
        HashMap<String, Object> config = new HashMap<>();

        HashMap<String, Object> projectId = new HashMap<>();
        projectId.put("display-order", "0");
        projectId.put("display-name", "Project ID");
        projectId.put("required", true);
        config.put(TaskPlugin.PROJECT_ID_PROPERTY, projectId);

        HashMap<String, Object> versionId = new HashMap<>();
        versionId.put("display-order", "1");
        versionId.put("display-name", "Version ID");
        versionId.put("required", true);
        config.put(TaskPlugin.VERSION_ID_PROPERTY, versionId);

        HashMap<String, Object> filePath = new HashMap<>();
        filePath.put("display-order", "2");
        filePath.put("display-name", "File Path");
        filePath.put("required", true);
        config.put(TaskPlugin.FILE_PATH_PROPERTY, filePath);

        return DefaultGoPluginApiResponse.success(TaskPlugin.GSON.toJson(config));
    }
}
