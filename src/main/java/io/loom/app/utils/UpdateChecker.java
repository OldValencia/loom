package io.loom.app.utils;

import com.google.gson.JsonParser;
import io.loom.app.ui.dialogs.UpdateDialog;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class UpdateChecker {

    private static final String REPO_URL = "https://api.github.com/repos/oldvalencia/loom/releases/latest";

    public static void check(Component parentComponent) {
        new Thread(() -> {
            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder()
                        .uri(URI.create(REPO_URL))
                        .build();

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    var json = JsonParser.parseString(response.body()).getAsJsonObject();
                    var latestTag = json.get("tag_name").getAsString();
                    if (!latestTag.equals(SystemUtils.VERSION)) {
                        SwingUtilities.invokeLater(() -> showUpdateDialog(parentComponent, latestTag, json.get("html_url").getAsString()));
                    }
                }
            } catch (Exception e) {
                log.error("Error while checking new version", e);
            }
        }).start();
    }

    private static void showUpdateDialog(Component parent, String version, String url) {
        var updateDialogResult = new UpdateDialog(version, parent);
        var selectedValue = updateDialogResult.getValue();

        if (selectedValue instanceof Integer && (int) selectedValue == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                log.error("Error while opening browser", ex);
            }
        }
    }
}