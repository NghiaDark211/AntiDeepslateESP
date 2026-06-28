package me.nghiadark.antideepslateesp.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.nghiadark.antideepslateesp.AntiDeepslateESP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class UpdateChecker {

    private final AntiDeepslateESP plugin;

    public UpdateChecker(AntiDeepslateESP plugin) {
        this.plugin = plugin;
    }

    public void check() {
        String repo = plugin.getConfigManager().getGithubRepo();
        if (repo == null || repo.isEmpty()) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URI uri = new URI("https://api.github.com/repos/" + repo + "/releases/latest");
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Accept", "application/vnd.github+json");

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    plugin.debug("Update check failed: HTTP " + responseCode);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                String latestTag = json.get("tag_name").getAsString();
                String currentVersion = plugin.getPluginMeta().getVersion();

                if (!currentVersion.equals(latestTag.replace("v", ""))) {
                    plugin.getLogger().info("New version available: " + latestTag
                            + " (current: v" + currentVersion + ")");
                    plugin.getLogger().info("Download at: https://github.com/" + repo + "/releases/tag/" + latestTag);
                } else {
                    plugin.debug("Plugin is up-to-date (v" + currentVersion + ")");
                }
            } catch (Exception e) {
                plugin.debug("Update check failed: " + e.getMessage());
            }
        });
    }
}
