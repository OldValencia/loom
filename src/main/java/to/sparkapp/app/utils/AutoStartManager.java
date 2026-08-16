package to.sparkapp.app.utils;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import to.sparkapp.app.config.AppPaths;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;

@Slf4j
public class AutoStartManager {
    private static final String APP_NAME = "Spark";
    private static final String RUN_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";

    public static void setAutoStart(boolean enable) {
        try {
            var executablePath = ProcessHandle.current().info().command().orElse(null);

            if (executablePath == null) {
                log.warn("Can't get executable file path, skipping");
                return;
            }

            if (SystemUtils.isWindows()) {
                handleWindows(enable, executablePath);
            } else if (SystemUtils.isMac()) {
                handleMac(enable, executablePath);
            }
        } catch (Exception e) {
            log.error("Can't set auto start", e);
        }
    }

    private static void handleWindows(boolean enable, String path) {
        if (!enable) {
            if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, RUN_KEY, APP_NAME)) {
                Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, RUN_KEY, APP_NAME);
                log.info("Auto start disabled");
            }
            return;
        }

        var executable = path.toLowerCase();
        if (executable.endsWith("java.exe") || executable.endsWith("javaw.exe")) {
            // Happens when the app runs from a JDK launcher (IDE / Gradle) instead of the
            // installed launcher: registering it would start a bare JVM at logon.
            log.warn("Auto start skipped, not running from the installed launcher: {}", path);
            return;
        }

        // Quoted so that Windows does not truncate paths containing spaces.
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, RUN_KEY, APP_NAME, "\"" + path + "\"");
        log.info("Auto start enabled for {}", path);
    }

    private static void handleMac(boolean enable, String path) throws Exception {
        var launchAgentsDir = new File(AppPaths.DATA_DIR, "Library/LaunchAgents");
        if (!launchAgentsDir.exists()) {
            launchAgentsDir.mkdirs();
        }

        var plistFile = new File(launchAgentsDir, "to.sparkapp.app.plist");

        if (enable) {
            var plistContent = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
                    <plist version="1.0">
                    <dict>
                        <key>Label</key>
                        <string>to.sparkapp.app</string>
                        <key>ProgramArguments</key>
                        <array>
                            <string>%s</string>
                        </array>
                        <key>RunAtLoad</key>
                        <true/>
                    </dict>
                    </plist>
                    """.formatted(path);

            try (var writer = new FileWriter(plistFile)) {
                writer.write(plistContent);
            }
        } else {
            if (plistFile.exists()) {
                plistFile.delete();
            }
        }
    }
}
