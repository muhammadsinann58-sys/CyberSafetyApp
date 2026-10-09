import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

// Offline analysis only: URI and IDN parse strings; neither visits a website.
public class UrlDetector {
    private final ArrayList<UrlRule> rules = new ArrayList<UrlRule>();

    public UrlDetector() {
        rules.add(new UrlRule.ProtocolRule());
        rules.add(new UrlRule.HostRule());
        rules.add(new UrlRule.AddressRule());
        rules.add(new UrlRule.KeywordRule());
        rules.add(new UrlRule.LookalikeRule());
        rules.add(new UrlRule.LengthRule());
        rules.add(new UrlRule.ShortenerRule());
    }

    public Result analyze(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Paste a URL beginning with http:// or https://.");
        }
        String text = input.trim();
        URI url;
        String host;
        try {
            url = new URI(text);
            String scheme = url.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Use a complete http:// or https:// URL.");
            }
            if (url.getRawAuthority() == null || url.getRawAuthority().isEmpty()) {
                throw new IllegalArgumentException("The URL must contain a valid host.");
            }
            host = url.getHost();
            if (host == null) {
                // URI may not recognize Unicode domains. Convert just the host
                // to ASCII punycode locally, then validate its host and port.
                String authority = url.getRawAuthority();
                int at = authority.lastIndexOf('@');
                if (at >= 0) { authority = authority.substring(at + 1); }
                int colon = authority.lastIndexOf(':');
                String domain = colon >= 0 ? authority.substring(0, colon) : authority;
                String port = colon >= 0 ? authority.substring(colon) : "";
                String ascii = IDN.toASCII(domain, IDN.USE_STD3_ASCII_RULES);
                URI normalized = new URI(scheme + "://" + ascii + port + "/");
                host = normalized.getHost();
                // Reparse the complete normalized URL so @ is still detected.
                String oldAuthority = url.getRawAuthority();
                String userInfo = at >= 0 ? oldAuthority.substring(0, at + 1) : "";
                url = new URI(scheme + "://" + userInfo + ascii + port
                        + text.substring(text.indexOf("://") + 3 + oldAuthority.length()));
            }
            if (host == null || host.isEmpty()) {
                throw new IllegalArgumentException("The URL must contain a valid host.");
            }
            if (url.getPort() > 65535) {
                throw new IllegalArgumentException("Port must be between 0 and 65535.");
            }
            host = host.toLowerCase(Locale.ROOT);
            // A final DNS dot is valid; remove it before comparing domains.
            if (host.endsWith(".")) { host = host.substring(0, host.length() - 1); }
            if (host.isEmpty()) { throw new IllegalArgumentException("Host cannot be empty."); }
            // Reject numeric dotted hosts that look like invalid IPv4 addresses.
            if (host.matches("[0-9]+(\\.[0-9]+){3}")) {
                for (String part : host.split("\\.")) {
                    if (part.length() > 3 || Integer.parseInt(part) > 255) {
                        throw new IllegalArgumentException("Invalid IPv4 address.");
                    }
                }
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Malformed URL. Check spaces, host and port.");
        }

        int score = 0;
        ArrayList<String> reasons = new ArrayList<String>();
        // Polymorphism: each UrlRule reference calls its child's check method.
        for (UrlRule rule : rules) {
            UrlRule.Finding finding = rule.check(url, host, text);
            if (finding != null) {
                score += finding.getPoints();
                reasons.add(finding.getReason());
            }
        }
        score = Math.min(100, score);
        String verdict = score >= 60 ? "High Risk" : score >= 25 ? "Suspicious" : "Safe";
        if (reasons.isEmpty()) {
            reasons.add("No warning signs matched this project's rules.");
        }
        return new Result(host, score, verdict, reasons);
    }

    public static class Result {
        private final String host;
        private final int score;
        private final String verdict;
        private final ArrayList<String> reasons;
        public Result(String host, int score, String verdict, ArrayList<String> reasons) {
            this.host = host;
            this.score = score;
            this.verdict = verdict;
            this.reasons = new ArrayList<String>(reasons);
        }
        public String getHost() { return host; }
        public int getScore() { return score; }
        public String getVerdict() { return verdict; }
        public ArrayList<String> getReasons() { return new ArrayList<String>(reasons); }
    }
}