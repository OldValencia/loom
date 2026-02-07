package io.loom.app.ui.settings.components;

import io.loom.app.config.AiConfiguration;
import io.loom.app.config.CustomAiProvidersManager;
import io.loom.app.ui.dialogs.ProviderEditDialog;
import io.loom.app.ui.settings.utils.FrameUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ProvidersManagementPanel extends JPanel {

    private final CustomAiProvidersManager providersManager;
    private final Consumer<Void> onProvidersChanged;
    private final JPanel listContainer;

    public ProvidersManagementPanel(CustomAiProvidersManager providersManager, Consumer<Void> onProvidersChanged) {
        this.providersManager = providersManager;
        this.onProvidersChanged = onProvidersChanged;

        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        this.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.add(new ProvidersListHeader(e -> openAddDialog()), BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setOpaque(false);
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        this.add(listContainer, BorderLayout.CENTER);

        refreshProvidersList();
    }

    private void refreshProvidersList() {
        listContainer.removeAll();
        var allProviders = providersManager.loadProviders();

        if (allProviders.isEmpty()) {
            listContainer.add(new ProvidersEmptyListLabel());
        } else {
            fillProviderList(allProviders);
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private void fillProviderList(List<AiConfiguration.AiConfig> allProviders) {
        for (var provider : allProviders) {
            var itemPanel = new ProviderListItem(
                    provider,
                    () -> openEditDialog(provider),
                    () -> confirmAndDelete(provider)
            );
            listContainer.add(itemPanel);
            listContainer.add(Box.createVerticalStrut(4));
        }
    }

    private void openAddDialog() {
        var dialog = new ProviderEditDialog(FrameUtils.getOwnerFrame(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            var name = dialog.getProviderName();
            var url = dialog.getProviderUrl();
            var color = String.format("#%06x", (int) (Math.random() * 0xFFFFFF));

            executeAsyncOp(() -> providersManager.addCustomProvider(name, url, color));
        }
    }

    private void openEditDialog(AiConfiguration.AiConfig provider) {
        var dialog = new ProviderEditDialog(FrameUtils.getOwnerFrame(this), provider);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            var name = dialog.getProviderName();
            var url = dialog.getProviderUrl();

            executeAsyncOp(() -> providersManager.updateProvider(provider.id(), name, url, provider.color()));
        }
    }

    private void confirmAndDelete(AiConfiguration.AiConfig provider) {
        int result = JOptionPane.showConfirmDialog(
                FrameUtils.getOwnerFrame(this),
                "Are you sure you want to delete \"" + provider.name() + "\"?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            providersManager.deleteProvider(provider.id());
            refreshProvidersList();
            onProvidersChanged.accept(null);
        }
    }

    private void executeAsyncOp(Runnable backgroundAction) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                backgroundAction.run();
                return null;
            }

            @Override
            protected void done() {
                refreshProvidersList();
                onProvidersChanged.accept(null);
            }
        }.execute();
    }
}
