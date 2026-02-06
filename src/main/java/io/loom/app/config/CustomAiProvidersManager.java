package io.loom.app.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class CustomAiProvidersManager {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private final File configFile;
    private final File iconsDir;

    public CustomAiProvidersManager() {
        File configDir = resolveConfigDirectory();
        this.configFile = new File(configDir, "custom-providers.json");
        this.iconsDir = new File(configDir, "icons");

        if (!configDir.exists() && !configDir.mkdirs()) {
            log.error("Failed to create config directory: {}", configDir);
        }
        if (!iconsDir.exists() && !iconsDir.mkdirs()) {
            log.error("Failed to create icons directory: {}", iconsDir);
        }
    }

    private File resolveConfigDirectory() {
        String userHome = System.getProperty("user.home");
//        String os = System.getProperty("os.name", "").toLowerCase();
//        if (os.contains("mac")) {
//            return new File(userHome, "Library/Application Support/Loom");
//        }
        return new File(userHome, ".loom");
    }

    public List<AiConfiguration.AiConfig> loadProviders() {
        if (!configFile.exists() || configFile.length() == 0) {
            log.info("Configuration file not found. Loading defaults from resources...");
            restoreDefaults();
        }

        // 2. Читаем JSON
        try {
            return jsonMapper.readValue(configFile, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Failed to load custom providers", e);
            return new ArrayList<>();
        }
    }

    public void restoreDefaults() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/default-providers.yml");
            if (inputStream == null) {
                inputStream = getClass().getResourceAsStream("/ai-configurations.yml");
            }

            if (inputStream == null) {
                log.error("Default providers file not found!");
                return;
            }

            JsonNode rootNode = yamlMapper.readTree(inputStream);
            List<AiConfiguration.AiConfig> defaultConfigs;
            List<AiConfiguration.AiConfig> processedConfigs = new ArrayList<>();

            if (rootNode.has("providers")) {
                defaultConfigs = yamlMapper.convertValue(rootNode.get("providers"), new TypeReference<>() {});
            } else if (rootNode.has("configurations")) {
                defaultConfigs = yamlMapper.convertValue(rootNode.get("configurations"), new TypeReference<>() {});
            } else if (rootNode.isArray()) {
                // Если это просто список
                defaultConfigs = yamlMapper.convertValue(rootNode, new TypeReference<>() {});
            } else {
                log.error("Invalid YAML structure: root is not array and has no 'providers' key");
                return;
            }

            for (AiConfiguration.AiConfig config : defaultConfigs) {
                String localIconName = extractIconFromResources(config.icon());

                processedConfigs.add(new AiConfiguration.AiConfig(
                        config.id(),
                        config.name(),
                        config.url(),
                        config.color(),
                        localIconName
                ));
            }

            // Сохраняем в JSON
            saveProviders(processedConfigs);
            log.info("Restored default providers and icons.");

        } catch (Exception e) {
            log.error("Failed to restore defaults", e);
        }
    }

    private String extractIconFromResources(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) return null;

        try {
            // resourcePath в YAML выглядит как "/icons/chatgpt.svg"
            InputStream in = getClass().getResourceAsStream(resourcePath);
            if (in == null) {
                log.warn("Icon resource not found: {}", resourcePath);
                return null;
            }

            String filename = new File(resourcePath).getName(); // chatgpt.svg
            File targetFile = new File(iconsDir, filename);

            // Копируем файл в .loom/icons, если его там нет или перезаписываем при сбросе
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return filename; // Возвращаем имя файла для сохранения в JSON
        } catch (IOException e) {
            log.error("Failed to extract icon: " + resourcePath, e);
            return null;
        }
    }

    public void saveProviders(List<AiConfiguration.AiConfig> providers) {
        try {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, providers);
            log.info("Saved {} providers to {}", providers.size(), configFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save providers", e);
        }
    }

    // Метод для добавления нового кастомного провайдера (с фавиконкой)
    public void addCustomProvider(String name, String url, String color) {
        List<AiConfiguration.AiConfig> current = loadProviders();

        String id = "custom_" + UUID.randomUUID().toString().substring(0, 8);
        String iconFilename = downloadFavicon(url, id);

        current.add(new AiConfiguration.AiConfig(id, name, url, color, iconFilename));
        saveProviders(current);
    }

    public void updateProvider(String id, String name, String url, String color) {
        List<AiConfiguration.AiConfig> current = loadProviders();
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).id().equals(id)) {
                String oldIcon = current.get(i).icon();
                // Если URL поменялся, можно попробовать перекачать иконку, но пока оставим старую
                current.set(i, new AiConfiguration.AiConfig(id, name, url, color, oldIcon));
                break;
            }
        }
        saveProviders(current);
    }

    public void deleteProvider(String id) {
        List<AiConfiguration.AiConfig> current = loadProviders();
        current.removeIf(p -> p.id().equals(id));
        saveProviders(current);
        // Можно добавить удаление файла иконки, если нужно
    }

    private String downloadFavicon(String urlString, String id) {
        try {
            URL url = new URL("https://www.google.com/s2/favicons?domain=" + new URL(urlString).getHost() + "&sz=64");
            File iconFile = new File(iconsDir, id + ".png");

            // Скачиваем
            try (InputStream in = url.openStream()) {
                Files.copy(in, iconFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Проверяем, не скачалась ли пустышка (некоторые сервисы возвращают 1x1 пиксель)
            if (iconFile.length() < 100) {
                return null; // Иконка битая
            }

            return iconFile.getName();
        } catch (Exception e) {
            log.error("Failed to download favicon for " + urlString, e);
            return null;
        }
    }

    public File getIconsDir() {
        return iconsDir;
    }
}
