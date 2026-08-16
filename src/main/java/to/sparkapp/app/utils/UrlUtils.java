package to.sparkapp.app.utils;

import lombok.extern.slf4j.Slf4j;

import java.awt.Desktop;
import java.net.URI;
import java.util.Collection;

@Slf4j
public class UrlUtils {

    public static void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            log.error("Error opening link", e);
        }
    }

    /**
     * Host of the URL without a leading {@code www.}, or {@code null} if it cannot be parsed.
     */
    public static String host(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            var host = URI.create(url).getHost();
            if (host == null) {
                return null;
            }
            host = host.toLowerCase();
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * True when both URLs point at the same host, treating {@code chat.example.com}
     * as belonging to {@code example.com}. Unparseable URLs are treated as a match
     * so that navigation is never blocked by a URL we failed to understand.
     */
    public static boolean isSameHost(String url, String baseUrl) {
        var host = host(url);
        var baseHost = host(baseUrl);
        if (host == null || baseHost == null) {
            return true;
        }
        return host.equals(baseHost) || host.endsWith("." + baseHost);
    }

    /**
     * The first URL of {@code baseUrls} that {@code url} belongs to, or {@code null}.
     */
    public static String matchingBaseUrl(String url, Collection<String> baseUrls) {
        if (host(url) == null || baseUrls == null) {
            return null;
        }
        for (var baseUrl : baseUrls) {
            if (host(baseUrl) != null && isSameHost(url, baseUrl)) {
                return baseUrl;
            }
        }
        return null;
    }
}