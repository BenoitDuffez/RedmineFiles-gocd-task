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

import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RedmineUploadFileTaskExecutor {

    private String getText(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

    public Result execute(TaskConfig taskConfig, Context context, JobConsoleLogger console) {
        console.printLine("start executor");
        try {
            return runCommand(context, taskConfig, console);
        } catch (Exception e) {
            console.printLine("Coudln't execute command: " + e);
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                console.printLine("   at: " + stackTraceElement.toString());
            }
            return new Result(false, "Failed to upload artifact to Redmine", e);
        }
    }

    /**
     * Try to run what we were asked to do
     *
     * @param taskContext    Context
     * @param taskTaskConfig Config
     * @param console        Logging console
     * @return Task result
     * @throws IOException In case of network error
     */
    private Result runCommand(Context taskContext, TaskConfig taskTaskConfig, JobConsoleLogger console) throws IOException {
        String apiKey = (String) taskContext.getEnvironmentVariables().get("REDMINE_API_KEY");
        String redmineUrl = (String) taskContext.getEnvironmentVariables().get("REDMINE_URL");

        AttachmentUpload upload = uploadFile(redmineUrl, apiKey, taskContext.getWorkingDir(), taskTaskConfig, console);
        if (upload == null || upload.upload == null || upload.upload.token == null || upload.upload.token.trim().isEmpty()) {
            return new Result(false, "Redmine didn't accept the file upload. Check API key, URL, artifact path...");
        }

        linkUploadToVersion(redmineUrl, apiKey, upload, taskContext, taskTaskConfig, console);

        String filesUrl = redmineUrl + "/projects/" + taskTaskConfig.getProjectId() + "/files";
        String resultHtml = "<p>File uploaded to: <a href=\"" + filesUrl + "\">" + filesUrl + "</a></p>";
        FileOutputStream fos = new FileOutputStream(new File(taskContext.getWorkingDir(), "redmine_result.html"));
        fos.write(resultHtml.getBytes());
        fos.close();

        return new Result(true, "End of run command");
    }

    /**
     * Upload file to redmine
     *
     * @param redmineUrl Redmine URL
     * @param apiKey     Redmine API key
     * @param workingDir Go working dir
     * @param config     Config
     * @param console    Logging console
     * @return Redmine attachment token container
     * @throws IOException In case of network error
     */
    private AttachmentUpload uploadFile(String redmineUrl, String apiKey, String workingDir, TaskConfig config, JobConsoleLogger console) throws IOException {
        String url = redmineUrl + "/uploads.json";
        URLConnection connection = new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setRequestProperty("X-Redmine-API-Key", apiKey);

        console.printLine("Uploading " + config.getFilePath() + " to " + url);

        File file = new File(workingDir, config.getFilePath());
        try (OutputStream output = connection.getOutputStream()) {
            Files.copy(file.toPath(), output);
        }

        InputStream response = connection.getInputStream();
        String uploadTokenJson = getText(response);

        return TaskPlugin.GSON.fromJson(uploadTokenJson, AttachmentUpload.class);
    }

    /**
     * Link attachment upload and project versions (will make the file appear in the files tab of Redmine)
     *
     * @param redmineUrl  Redmine URL
     * @param apiKey      Redmine API key
     * @param upload      Result of file upload (for the token)
     * @param taskContext Task context (for environment variables)
     * @param config      Config
     * @param console     Logging console   @throws IOException In case of network error
     */
    private void linkUploadToVersion(String redmineUrl, String apiKey, AttachmentUpload upload, Context taskContext, TaskConfig config, JobConsoleLogger console) throws IOException {
        String fileName = config.getFileName();
        Map<String, String> replacements = new HashMap<String, String>() {{
            String sha1 = (String) taskContext.getEnvironmentVariables().get("GO_REVISION");
            put("%R", sha1);
            put("%r", sha1 != null && sha1.length() > 8 ? sha1.substring(0, 8) : sha1);
            put("%P", (String) taskContext.getEnvironmentVariables().get("GO_PIPELINE_NAME"));
            put("%C", (String) taskContext.getEnvironmentVariables().get("GO_PIPELINE_COUNTER"));
            put("%L", (String) taskContext.getEnvironmentVariables().get("GO_PIPELINE_LABEL"));
            put("%A", getAndroidVersionName(taskContext, config));
        }};
        for (String key : replacements.keySet()) {
            console.printLine("Replacing " + key + " with " + replacements.get(key));
            fileName = fileName.replaceAll(key, replacements.get(key));
        }

        String description = String.format(Locale.getDefault(),
                "File generated on %s (go pipeline #%s)",
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()),
                taskContext.getEnvironmentVariables().get("GO_PIPELINE_COUNTER"));

        // Prepare JSON
        FileUpload linkToVersion = new FileUpload();
        linkToVersion.file.description = description;
        linkToVersion.file.token = upload.upload.token;
        linkToVersion.file.versionId = config.getVersionId();
        linkToVersion.file.filename = fileName;
        String json = TaskPlugin.GSON.toJson(linkToVersion, FileUpload.class);

        // Link attachment to version
        String url = redmineUrl + "/projects/" + config.getProjectId() + "/files.json";
        URLConnection connection = new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Redmine-API-Key", apiKey);

        console.printLine("Uploading " + json + " to " + url);

        try (OutputStream output = connection.getOutputStream()) {
            output.write(json.getBytes());
        }

        console.printLine("Result: " + getText(connection.getInputStream()));
    }

    private String getAndroidVersionName(Context taskContext, TaskConfig config) {
        String appFolder = config.getAppFolder();
        if (appFolder == null || appFolder.trim().length() == 0) {
            appFolder = "app";
        }

        String build = "pipelines/"
                + taskContext.getEnvironmentVariables().get("GO_PIPELINE_NAME")
                + "/" + appFolder
                + "/build.gradle";
        return execute(taskContext, new ProcessBuilder("grep", "-oP", "(?<=versionName\\s\")[^\"]+", build));
    }

    private String execute(Context taskContext, ProcessBuilder androidVersion) {
        //noinspection unchecked
        androidVersion.environment().putAll(taskContext.getEnvironmentVariables());

        try {
            Process process = androidVersion.start();
            return IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Redmine response for the file upload:
     * {"upload":{"token":"7167.ed1ccdb093229ca1bd0b043618d88743"}}
     */
    public static class AttachmentUpload {
        Upload upload;

        static class Upload {
            String token;
        }
    }

    /**
     * POST body when linking attachment with version
     */
    public static class FileUpload {
        RedmineFile file;

        FileUpload() {
            file = new RedmineFile();
        }

        static class RedmineFile {
            String token;
            String versionId;
            String filename;
            String description;
        }
    }
}
