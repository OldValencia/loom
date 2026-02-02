package io.aipanel.app.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BrowserCookieImporter {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    public record BrowserProfile(String browser, String profileName, Path cookiePath) {

        @Override
        public String toString() {
            return browser + " - " + profileName;
        }
    }

    public static List<BrowserProfile> detectBrowsers() {
        var profiles = new ArrayList<BrowserProfile>();

        profiles.addAll(detectChrome());
        profiles.addAll(detectFirefox());
        profiles.addAll(detectEdge());

        return profiles;
    }

    private static List<BrowserProfile> detectChrome() {
        var profiles = new ArrayList<BrowserProfile>();
        Path cookieFile;

        if (OS_NAME.contains("win")) {
            cookieFile = Paths.get(USER_HOME, "AppData", "Local", "Google", "Chrome", "User Data", "Default", "Cookies");
        } else if (OS_NAME.contains("mac")) {
            cookieFile = Paths.get(USER_HOME, "Library", "Application Support", "Google", "Chrome", "Default", "Cookies");
        } else {
            cookieFile = Paths.get(USER_HOME, ".config", "google-chrome", "Default", "Cookies");
        }

        if (Files.exists(cookieFile)) {
            profiles.add(new BrowserProfile("Chrome", "Default", cookieFile));
        }

        return profiles;
    }

    private static List<BrowserProfile> detectFirefox() {
        var profiles = new ArrayList<BrowserProfile>();
        Path firefoxDir;

        if (OS_NAME.contains("win")) {
            firefoxDir = Paths.get(USER_HOME, "AppData", "Roaming", "Mozilla", "Firefox", "Profiles");
        } else if (OS_NAME.contains("mac")) {
            firefoxDir = Paths.get(USER_HOME, "Library", "Application Support", "Firefox", "Profiles");
        } else {
            firefoxDir = Paths.get(USER_HOME, ".mozilla", "firefox");
        }

        if (Files.exists(firefoxDir)) {
            try (var firefoxList = Files.list(firefoxDir)) {
                firefoxList
                        .filter(Files::isDirectory)
                        .forEach(profileDir -> {
                            var cookieFile = profileDir.resolve("cookies.sqlite");
                            if (Files.exists(cookieFile)) {
                                profiles.add(new BrowserProfile("Firefox", profileDir.getFileName().toString(), cookieFile));
                            }
                        });
            } catch (IOException e) {
                log.error("Failed to list Firefox profiles", e);
            }
        }

        return profiles;
    }

    private static List<BrowserProfile> detectEdge() {
        var profiles = new ArrayList<BrowserProfile>();
        Path cookieFile;

        if (OS_NAME.contains("win")) {
            cookieFile = Paths.get(USER_HOME, "AppData", "Local", "Microsoft", "Edge", "User Data", "Default", "Cookies");
        } else if (OS_NAME.contains("mac")) {
            cookieFile = Paths.get(USER_HOME, "Library", "Application Support", "Microsoft Edge", "Default", "Cookies");
        } else {
            cookieFile = Paths.get(USER_HOME, ".config", "microsoft-edge", "Default", "Cookies");
        }

        if (Files.exists(cookieFile)) {
            profiles.add(new BrowserProfile("Edge", "Default", cookieFile));
        }

        return profiles;
    }

    public static boolean importCookies(BrowserProfile profile, Path targetDir) {
        try {
            Files.createDirectories(targetDir);
            var targetFile = targetDir.resolve("imported_cookies_" + profile.browser.toLowerCase());
            Files.copy(profile.cookiePath, targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Imported cookies from {} to {}", profile, targetFile);
            return true;
        } catch (IOException e) {
            log.error("Failed to import cookies from {}", profile, e);
            return false;
        }
    }
}