package to.sparkapp.app.utils;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NativeWindowUtils {

    public static volatile double cachedWebviewHeight = 0;

    // Win32 style / SetWindowPos constants
    private static final int GWL_STYLE = -16;
    private static final int GWL_EXSTYLE = -20;

    private static final int WS_POPUP = 0x80000000;
    private static final int WS_CHILD = 0x40000000;
    private static final int WS_CLIPSIBLINGS = 0x04000000;
    private static final int WS_CLIPCHILDREN = 0x02000000;
    private static final int WS_CAPTION = 0x00C00000;
    private static final int WS_BORDER = 0x00800000;
    private static final int WS_DLGFRAME = 0x00400000;
    private static final int WS_SYSMENU = 0x00080000;
    private static final int WS_THICKFRAME = 0x00040000;
    private static final int WS_MINIMIZEBOX = 0x00020000;
    private static final int WS_MAXIMIZEBOX = 0x00010000;

    private static final int WS_EX_DLGMODALFRAME = 0x00000001;
    private static final int WS_EX_WINDOWEDGE = 0x00000100;
    private static final int WS_EX_CLIENTEDGE = 0x00000200;
    private static final int WS_EX_STATICEDGE = 0x00020000;
    private static final int WS_EX_APPWINDOW = 0x00040000;

    private static final int SWP_NOSIZE = 0x0001;
    private static final int SWP_NOMOVE = 0x0002;
    private static final int SWP_NOZORDER = 0x0004;
    private static final int SWP_NOACTIVATE = 0x0010;
    private static final int SWP_FRAMECHANGED = 0x0020;

    public static long getJavaFXWindowHandle(String windowTitle) {
        if (!SystemUtils.isWindows() || windowTitle == null || windowTitle.isEmpty()) {
            return 0L;
        }
        try {
            var hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
            if (hwnd != null) {
                return Pointer.nativeValue(hwnd.getPointer());
            }
            return 0L;
        } catch (Exception e) {
            log.warn("Error getting HWND for JavaFX window", e);
            return 0L;
        }
    }

    private static int getWindowLong(WinDef.HWND hwnd, int index) {
        if (Platform.is64Bit()) {
            return User32.INSTANCE.GetWindowLongPtr(hwnd, index).intValue();
        } else {
            return User32.INSTANCE.GetWindowLong(hwnd, index);
        }
    }

    private static void setWindowLong(WinDef.HWND hwnd, int index, int value) {
        if (Platform.is64Bit()) {
            User32.INSTANCE.SetWindowLongPtr(hwnd, index, new Pointer(value));
        } else {
            User32.INSTANCE.SetWindowLong(hwnd, index, value);
        }
    }

    public static void setParent(long childHandle, long parentHandle) {
        if (!SystemUtils.isWindows() || childHandle == 0 || parentHandle == 0) {
            return;
        }
        try {
            var child = new WinDef.HWND(new Pointer(childHandle));
            var parent = new WinDef.HWND(new Pointer(parentHandle));

            int style = getWindowLong(child, GWL_STYLE);
            style = (style & ~(WS_POPUP | WS_CAPTION | WS_THICKFRAME | WS_BORDER |
                    WS_SYSMENU | WS_MINIMIZEBOX | WS_MAXIMIZEBOX | WS_DLGFRAME))
                    | WS_CHILD | WS_CLIPSIBLINGS | WS_CLIPCHILDREN;
            setWindowLong(child, GWL_STYLE, style);

            int exStyle = getWindowLong(child, GWL_EXSTYLE);
            exStyle = exStyle & ~(WS_EX_DLGMODALFRAME | WS_EX_WINDOWEDGE | WS_EX_CLIENTEDGE |
                    WS_EX_STATICEDGE | WS_EX_APPWINDOW);
            setWindowLong(child, GWL_EXSTYLE, exStyle);

            User32.INSTANCE.SetParent(child, parent);

            // Flush the style change into the window's non-client area immediately.
            User32.INSTANCE.SetWindowPos(child, null, 0, 0, 0, 0,
                    SWP_NOSIZE | SWP_NOMOVE | SWP_NOZORDER | SWP_NOACTIVATE | SWP_FRAMECHANGED);

            log.debug("Webview parented successfully");
        } catch (Throwable e) {
            log.warn("SetParent failed", e);
        }
    }

    /**
     * Removes the webview window from its parent (used only in edge cases;
     * normal hide-to-tray no longer calls this).
     */
    public static void unparent(long childHandle) {
        if (!SystemUtils.isWindows() || childHandle == 0) return;
        try {
            var child = new WinDef.HWND(new Pointer(childHandle));
            int style = getWindowLong(child, GWL_STYLE);

            style = (style & ~WS_CHILD) | WS_POPUP;
            setWindowLong(child, GWL_STYLE, style);

            User32.INSTANCE.SetParent(child, null);

            User32.INSTANCE.SetWindowPos(child, null, 0, 0, 0, 0,
                    SWP_NOSIZE | SWP_NOMOVE | SWP_NOZORDER | SWP_NOACTIVATE | SWP_FRAMECHANGED);

            log.debug("Webview unparented");
        } catch (Throwable e) {
            log.warn("Unparent failed", e);
        }
    }

    public static void setBounds(long windowHandle, int x, int y, int width, int height) {
        if (windowHandle == 0) {
            return;
        }
        setBoundsWindows(windowHandle, x, y, width, height);
    }

    public static void setVisible(long windowHandle, boolean visible) {
        if (windowHandle == 0) {
            return;
        }
        setVisibleWindows(windowHandle, visible);
    }

    private static void setBoundsWindows(long handle, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        try {
            var hwnd = new WinDef.HWND(new Pointer(handle));
            User32.INSTANCE.SetWindowPos(hwnd, null, x, y, width, height, SWP_NOZORDER | SWP_NOACTIVATE);
        } catch (Exception e) {
            log.warn("SetWindowPos failed", e);
        }
    }

    private static void setVisibleWindows(long handle, boolean visible) {
        try {
            var hwnd = new WinDef.HWND(new Pointer(handle));
            User32.INSTANCE.ShowWindow(hwnd, visible ? WinUser.SW_SHOWNOACTIVATE : WinUser.SW_HIDE);
        } catch (Exception e) {
            log.warn("ShowWindow failed", e);
        }
    }
}
