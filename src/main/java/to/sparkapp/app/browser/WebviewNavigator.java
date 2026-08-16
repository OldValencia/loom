package to.sparkapp.app.browser;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import to.sparkapp.app.config.AiConfiguration;
import to.sparkapp.app.utils.UrlUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
public class WebviewNavigator {

    private final WebviewManager bridge;
    private final WebviewZoomManager zoomManager;

    private static final AtomicLong NAV_ID_SEQUENCE = new AtomicLong();

    private volatile long currentNavId = 0L;
    private volatile String currentUrl;
    private volatile String configBaseUrl;

    private static final List<String> AUTH_DOMAINS = List.of(
            // Google
            "accounts.google.",
            "consent.google.",
            // OpenAI
            "auth.openai.",
            // Apple
            "appleid.apple.com",
            "idmsa.apple.com",
            // Microsoft
            "login.microsoftonline.com",
            "login.live.com",
            "login.windows.net",
            "account.microsoft.com",
            // Facebook
            "login.facebook.com",
            "www.facebook.com/login",
            // GitHub
            "github.com/login",
            "github.com/session",
            "github.com/oauth",
            // Cloudflare
            "challenges.cloudflare.com",
            "cloudflare.com/cdn-cgi/challenge",
            // Anthropic / Claude
            "auth.anthropic.com",
            "login.anthropic.com",
            // xAI / Grok
            "auth.x.ai",
            "login.x.ai",
            "twitter.com/i/flow/login",
            "x.com/i/flow/login",
            // Auth0 (commonly used)
            "auth0.com",
            // Perplexity
            "auth.perplexity.ai",
            // Midjourney / Discord
            "discord.com/login",
            "discord.com/oauth2",
            // HuggingFace
            "huggingface.co/login",
            "huggingface.co/oauth"
    );

    private static final List<String> AUTH_PATH_PATTERNS = List.of(
            "/oauth",
            "/oauth2",
            "/auth/",
            "/authorize",
            "/sso/",
            "/saml/",
            "/signin",
            "/login",
            "/callback?code=",
            "/oidc/"
    );

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                var t = new Thread(r, "spark-nav-scheduler");
                t.setDaemon(true);
                return t;
            });

    @Setter
    private Consumer<String> onUrlChanged;

    WebviewNavigator(WebviewManager bridge, WebviewZoomManager zoomManager) {
        this.bridge = bridge;
        this.zoomManager = zoomManager;
    }

    void handleUrlChange(String url) {
        if (url.isBlank() || url.equals("about:blank") || url.startsWith("chrome-error://")) return;

        if (onUrlChanged != null) onUrlChanged.accept(url);

        if (configBaseUrl != null && !isAuthUrl(url) && !UrlUtils.isSameHost(url, configBaseUrl)) {
            log.info("WebviewNavigator: External URL detected [{}], opening in browser", url);
            UrlUtils.openLink(url);
            final long navId = currentNavId;
            schedule(80, () -> {
                if (currentNavId == navId) {
                    bridge.dispatch(() -> bridge.loadURL(configBaseUrl));
                }
            });
        }
    }

    /**
     * Switches to a provider, optionally opening a deeper page of it (for example the
     * conversation the user had open when the app was last closed).
     */
    void setCurrentConfig(AiConfiguration.AiConfig config, String targetUrl) {
        var url = targetUrl != null && UrlUtils.isSameHost(targetUrl, config.url())
                ? targetUrl
                : config.url();

        log.info("WebviewNavigator: Changing config to: {}", url);
        this.configBaseUrl = config.url();
        navigate(url);
    }

    void navigate(String url) {
        this.currentUrl = url;
        final long navId = NAV_ID_SEQUENCE.incrementAndGet();
        this.currentNavId = navId;

        bridge.dispatch(() -> {
            if (currentNavId == navId) {
                log.info("WebviewNavigator: Loading {} [Nav-{}]", url, navId);
                bridge.loadURL(url);
            }
        });

        schedule(500, () -> {
            if (navId == currentNavId) {
                bridge.dispatch(zoomManager::applyZoomCss);
            }
        });
    }

    void clearCookies() {
        log.info("WebviewNavigator: Clearing cookies...");
        final String returnUrl = currentUrl != null ? currentUrl : "about:blank";
        final long navId = NAV_ID_SEQUENCE.incrementAndGet();
        this.currentNavId = navId;

        bridge.dispatch(() -> {
            if (currentNavId != navId) return;
            bridge.eval("""
                    (function() {
                        document.cookie.split(';').forEach(function(c) {
                            document.cookie = c.trim().split('=')[0] +
                                '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/';
                        });
                    })();
                    """);
            bridge.loadURL("about:blank");
            schedule(400, () -> {
                if (currentNavId == navId) {
                    bridge.dispatch(() -> bridge.loadURL(returnUrl));
                }
            });
        });
    }

    String getCurrentUrl() {
        return currentUrl != null ? currentUrl : "about:blank";
    }

    void handleLinkClicked(String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        if (isAuthUrl(url) || (configBaseUrl != null && UrlUtils.isSameHost(url, configBaseUrl))) {
            log.info("WebviewNavigator: Internal/Auth click detected, loading inside: {}", url);
            navigate(url);
        } else {
            log.info("WebviewNavigator: External click detected, opening in OS browser: {}", url);
            UrlUtils.openLink(url);
        }
    }

    public static boolean isAuthUrl(String url) {
        if (url == null) {
            return false;
        }
        var lower = url.toLowerCase();

        for (var domain : AUTH_DOMAINS) {
            if (lower.contains(domain)) {
                return true;
            }
        }
        for (var pattern : AUTH_PATH_PATTERNS) {
            if (lower.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private void schedule(long delayMs, Runnable task) {
        scheduler.schedule(task, delayMs, TimeUnit.MILLISECONDS);
    }
}
