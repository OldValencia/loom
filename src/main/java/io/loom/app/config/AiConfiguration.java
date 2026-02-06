package io.loom.app.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AiConfiguration {

    @Getter
    private final CustomAiProvidersManager customProvidersManager;

    @Getter
    private List<AiConfig> configurations = new ArrayList<>();

    public AiConfiguration() {
        this.customProvidersManager = new CustomAiProvidersManager();
        reload();
    }

    public void reload() {
        this.configurations = customProvidersManager.loadProviders();
        log.info("AiConfiguration loaded {} providers", configurations.size());
    }

    public void resetToDefaults() {
        customProvidersManager.restoreDefaults();
        reload();
    }

    public record AiConfig(
            String id,
            String name,
            String url,
            String color,
            String icon
    ) {}
}