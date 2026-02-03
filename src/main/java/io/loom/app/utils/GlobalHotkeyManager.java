package io.loom.app.utils;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import io.loom.app.config.AppPreferences;
import io.loom.app.windows.MainWindow;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GlobalHotkeyManager implements NativeKeyListener, NativeMouseInputListener {

    private final MainWindow mainWindow;
    private final AppPreferences appPreferences;

    private final Set<Integer> pressedKeys = new HashSet<>();

    private boolean isRecording = false;
    private Runnable onRecordComplete;

    public GlobalHotkeyManager(MainWindow mainWindow, AppPreferences appPreferences) {
        this.mainWindow = mainWindow;
        this.appPreferences = appPreferences;

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
    }

    public void start() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            GlobalScreen.addNativeMouseListener(this);
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.removeNativeMouseListener(this);
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public void startRecording(Runnable onRecordComplete) {
        this.onRecordComplete = onRecordComplete;
        this.pressedKeys.clear();

        // ВАЖНО: Делаем небольшую задержку перед началом записи.
        // Иначе JNativeHook ловит событие MouseReleased от клика по кнопке "Record"
        // и мгновенно завершает запись (или записывает клик).
        new Timer(200, e -> {
            ((Timer) e.getSource()).stop();
            pressedKeys.clear(); // Чистим на случай, если что-то проскочило за 200мс
            isRecording = true;
        }).start();
    }

    public void clearHotkey() {
        appPreferences.setHotkeyToStartApplication(null);
    }

    private void checkHotkey() {
        if (isRecording) return;

        var saved = appPreferences.getHotkeyToStartApplication();
        if (saved == null || saved.isEmpty()) return;

        if (pressedKeys.size() == saved.size() && pressedKeys.containsAll(saved)) {
            pressedKeys.clear();
            SwingUtilities.invokeLater(this::toggleWindow);
        }
    }

    private void toggleWindow() {
        var frame = mainWindow.getFrame();
        if (frame.isVisible() && frame.isActive()) {
            frame.setVisible(false);
        } else {
            frame.setVisible(true);
            frame.setExtendedState(JFrame.NORMAL);
            frame.toFront();
            frame.requestFocus();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int code = e.getKeyCode();

        if (isRecording) {
            if (code == NativeKeyEvent.VC_ESCAPE) {
                finishRecording(false);
                return;
            }
            if (code == NativeKeyEvent.VC_DELETE || code == NativeKeyEvent.VC_BACKSPACE) {
                finishRecording(true, true); // true = save, true = clear (save empty)
                return;
            }

            pressedKeys.add(code);
            // Если нажато много клавиш - хватит
            if (pressedKeys.size() >= 4) {
                finishRecording(true);
            }
        } else {
            pressedKeys.add(code);
            checkHotkey();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (isRecording) {
            // Завершаем запись при отпускании любой клавиши, ЕСЛИ что-то уже было нажато
            if (!pressedKeys.isEmpty()) {
                finishRecording(true);
            }
        } else {
            pressedKeys.remove(e.getKeyCode());
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        var mouseCode = 10000 + e.getButton();
        pressedKeys.add(mouseCode);

        if (!isRecording) {
            checkHotkey();
        }
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        int mouseCode = 10000 + e.getButton();

        if (isRecording) {
            // Если отпустили кнопку мыши во время записи:

            // 1. Проверяем, не является ли это одиночным кликом ЛКМ/ПКМ без модификаторов
            boolean isSimpleClick = pressedKeys.size() == 1 && (pressedKeys.contains(10001) || pressedKeys.contains(10002));

            if (!pressedKeys.isEmpty() && !isSimpleClick) {
                // Если это Ctrl + Click, то здесь pressedKeys содержит [Ctrl, Mouse]. Сохраняем.
                finishRecording(true);
            } else {
                // Если это просто клик (или мышь отпустили раньше, чем нажали модификатор),
                // просто удаляем кнопку из буфера, давая шанс нажать другую комбинацию.
                pressedKeys.remove(mouseCode);
            }
        } else {
            pressedKeys.remove(mouseCode);
        }
    }

    // Overloads для удобства
    private void finishRecording(boolean save) {
        finishRecording(save, false);
    }

    private void finishRecording(boolean save, boolean forceClear) {
        isRecording = false;

        if (save) {
            if (forceClear || pressedKeys.isEmpty()) {
                appPreferences.setHotkeyToStartApplication(null);
            } else {
                appPreferences.setHotkeyToStartApplication(new ArrayList<>(pressedKeys));
            }
        } else {
            pressedKeys.clear();
        }

        // Сразу чистим pressedKeys после сохранения, чтобы не триггернуло действие сразу после записи
        pressedKeys.clear();

        if (onRecordComplete != null) {
            SwingUtilities.invokeLater(onRecordComplete);
        }
    }

    // Остальные методы пустые
    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
    }

    public static String getHotkeyText(List<Integer> codes) {
        if (codes == null || codes.isEmpty()) return "None";
        return codes.stream()
                .map(code -> {
                    if (code > 10000) return "Mouse " + (code - 10000);
                    return NativeKeyEvent.getKeyText(code);
                }).collect(Collectors.joining(" + "));
    }
}