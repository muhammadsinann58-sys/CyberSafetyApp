import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// Abstract parent: every rule has the same interface.
public abstract class UrlRule {
    public abstract Finding check(URI url, String host, String text);

    // Encapsulation: a rule returns data through getters.
    public static class Finding {
        private final int points;
        private final String reason;
        public Finding(int points, String reason) {
            this.points = points;
            this.reason = reason;
        }
        public int getPoints() { return points; }
        public String getReason() { return reason; }
    }

    // These child classes demonstrate inheritance and method overriding.
    public static class ProtocolRule extends UrlRule {
        @Override
        public Finding check(URI url, String host, String text) {
            if (url.getScheme().equalsIgnoreCase("http")) {
                return new Finding(20, "HTTP is not encrypted. HTTPS is preferred (+20).");
            }
            return null;
        }
    }

    public static class HostRule extends UrlRule {
        @Override
        public Finding check(URI url, String host, String text) {
            List<String> reasons = new ArrayList<String>();
            int score = 0;
            if (isIp(host)) {
                score += 30;
                reasons.add("IP address used instead of a domain name (+30)");
            } else {
                // A simple dot count, not a public-suffix database.
                if (host.split("\\.").length > 4) {
                    score += 15;
                    reasons.add("More than four domain labels (+15)");
                }
                if (host.contains("xn--")) {
                    score += 20;
                    reasons.add("Internationalized/punycode domain; inspect its spelling (+20)");
                }
                String tld = host.substring(host.lastIndexOf('.') + 1);
                if (Arrays.asList("xyz", "top", "click", "zip", "mov").contains(tld)) {
                    score += 15;
                    reasons.add("TLD is on this project's caution list: ." + tld + " (+15)");
                }
                if (host.indexOf('.') < 0) {
                    score += 10;
                    reasons.add("Single-label host, not a typical public domain (+10)");
                }
            }
            return score == 0 ? null : new Finding(score, String.join("; ", reasons) + ".");
        }
        private boolean isIp(String host) {
            if (host.startsWith("[") && host.endsWith("]")) { return true; }
            String[] parts = host.split("\\.");
            if (parts.length != 4) { return false; }
            for (String part : parts) {
                if (!part.matches("[0-9]{1,3}")) { return false; }
                if (Integer.parseInt(part) > 255) { return false; }
            }
            return true;
        }
    }

    public static class AddressRule extends UrlRule {
        @Override
        public Finding check(URI url, String host, String text) {
            if (url.getRawUserInfo() != null) {
                return new Finding(30, "Text before @ is user information, not the real host (+30).");
            }
            if (text.contains("@")) {
                return new Finding(5, "@ appears in the path or query; it does not change the host (+5).");
            }
            return null;
        }
    }

    public static class KeywordRule extends UrlRule {
        @Override
        public Finding check(URI url, String host, String text) {
            String lower = text.toLowerCase(Locale.ROOT);
            List<String> found = new ArrayList<String>();
            for (String word : new String[] {"login", "verify", "secure", "account",
                    "update", "bank", "free", "bonus"}) {
                // Simple substring matching; each keyword counts once.
                if (lower.contains(word)) { found.add(word); }
            }
            if (found.isEmpty()) { return null; }
            int points = Math.min(25, found.size() * 5);
            return new Finding(points, "Sensitive/reward words: " + String.join(", ", found)
                    + " (+" + points + "). These words can also appear in legitimate URLs.");
        }
    }

    public static class LookalikeRule extends UrlRule {
        @Override
        public Finding check(URI url, String host, String text) {
            for (String spelling : new String[] {"paypa1", "g00gle", "faceb00k"}) {
                if (host.contains(spelling)) {
                    return new Finding(35, "Host contains a brand-like spelling: " + spelling + " (+35).");
                }
            }
            return null;
        }
    }

    public static class LengthRule extends UrlRule {
        @Override
        public Finding check(URI url, String host, String text) {
            if (text.length() > 200) {
                return new Finding(10, "URL is longer than 200 characters (+10).");
            }
            return null;
        }
    }

    public static class ShortenerRule extends UrlRule {
        @Override
        public Finding check(URI url, String host, String text) {
            for (String domain : new String[] {"bit.ly", "tinyurl.com", "t.co", "is.gd", "goo.gl"}) {
                if (host.equals(domain) || host.endsWith("." + domain)) {
                    return new Finding(25, "Known URL shortener: destination is hidden (+25).");
                }
            }
            return null;
        }
    }
}