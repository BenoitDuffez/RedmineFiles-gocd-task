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

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.Map;

public class ValidateRequest {
    public GoPluginApiResponse execute(GoPluginApiRequest request) {
        HashMap<String, Object> validationResult = new HashMap<>();
        int responseCode = DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;
        Map configMap = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);
        HashMap<String, String> errorMap = new HashMap<>();
        if (!hasValidEntry(configMap, TaskPlugin.PROJECT_ID_PROPERTY)) {
            errorMap.put(TaskPlugin.PROJECT_ID_PROPERTY, "Project ID cannot be empty");
        }
        if (!hasValidEntry(configMap, TaskPlugin.VERSION_ID_PROPERTY)) {
            errorMap.put(TaskPlugin.VERSION_ID_PROPERTY, "Version ID cannot be empty");
        }
        if (!hasValidEntry(configMap, TaskPlugin.FILE_PATH_PROPERTY)) {
            errorMap.put(TaskPlugin.FILE_PATH_PROPERTY, "File Path cannot be empty");
        }
        if (!hasValidEntry(configMap, TaskPlugin.FILE_NAME_PROPERTY)) {
            errorMap.put(TaskPlugin.FILE_NAME_PROPERTY, "File Name cannot be empty");
        }
        validationResult.put("errors", errorMap);
        return new DefaultGoPluginApiResponse(responseCode, TaskPlugin.GSON.toJson(validationResult));
    }

    private boolean hasValidEntry(Map map, String key) {
        return map.containsKey(key)
                && ((Map) map.get(key)).get("value") != null
                && !((String) ((Map) map.get(key)).get("value")).trim().isEmpty();
    }
}
