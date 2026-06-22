package to.sparkapp.app.utils;

import lombok.extern.slf4j.Slf4j;
import to.sparkapp.app.config.AppPaths;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SingleInstanceLock {

    private static final File PORT_FILE = new File(AppPaths.DATA_DIR, "lock.port");
    private static ServerSocket serverSocket;
    private static final AtomicReference<Runnable> onActivate = new AtomicReference<>();

    public static void setOnActivate(Runnable callback) {
        onActivate.set(callback);
    }

    public static boolean tryAcquire() {
        int existingPort = readPort();
        if (existingPort > 0) {
            if (signalExistingInstance(existingPort)) {
                return false; // Another instance handled the signal
            }
        }

        try {
            serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
            int port = serverSocket.getLocalPort();
            writePort(port);
            startListenerThread();
            return true;
        } catch (IOException e) {
            log.error("Could not bind socket for single instance lock", e);
            return true; // Proceed anyway if we fail to bind for some reason
        }
    }

    public static void release() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (PORT_FILE.exists()) {
                PORT_FILE.delete();
            }
        } catch (IOException ignored) {
        }
    }

    private static void startListenerThread() {
        var thread = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    var client = serverSocket.accept();
                    client.close();
                    var callback = onActivate.get();
                    if (callback != null) {
                        javafx.application.Platform.runLater(callback);
                    }
                } catch (IOException ignored) {
                }
            }
        }, "single-instance-listener");
        thread.setDaemon(true);
        thread.start();
    }

    private static boolean signalExistingInstance(int port) {
        try (var ignored = new Socket(InetAddress.getLoopbackAddress(), port)) {
            log.info("Another instance is running — signalled it to come to front");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static int readPort() {
        try {
            if (PORT_FILE.exists()) {
                String content = Files.readString(PORT_FILE.toPath()).trim();
                return Integer.parseInt(content);
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    private static void writePort(int port) {
        try {
            Files.writeString(PORT_FILE.toPath(), String.valueOf(port));
            PORT_FILE.deleteOnExit();
        } catch (Exception e) {
            log.warn("Could not write port file", e);
        }
    }
}
