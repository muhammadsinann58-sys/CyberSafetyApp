import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Uses character arrays, not a stored or displayed copy of the password.
public class PasswordChecker {
    private final ArrayList<PasswordRule> rules = new ArrayList<PasswordRule>();

    public PasswordChecker() {
        rules.add(new LengthRule());
        rules.add(new CharacterRule());
        rules.add(new PatternRule());
    }

    public Result analyze(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Type a password to check. Use a practice example, not your real password.");
        }
        boolean onlyWhitespace = true;
        for (char c : password) {
            if (!Character.isWhitespace(c)) { onlyWhitespace = false; break; }
        }
        if (onlyWhitespace) {
            throw new IllegalArgumentException("A password cannot contain only spaces or whitespace.");
        }
        int score = 0;
        boolean common = false;
        ArrayList<String> suggestions = new ArrayList<String>();
        // Polymorphism: each child overrides the same check method.
        for (PasswordRule rule : rules) {
            Finding finding = rule.check(password);
            score += finding.points;
            common = common || finding.common;
            suggestions.addAll(finding.suggestions);
        }
        score = Math.max(0, Math.min(100, score));
        // A known weak word must not become strong by adding a suffix.
        if (common) { score = Math.min(score, 20); }
        if (password.length < 8) { score = Math.min(score, 39); }
        String verdict = score >= 75 ? "Strong" : score >= 40 ? "Medium" : "Weak";
        if (suggestions.isEmpty()) {
            suggestions.add("Good length and character variety under these simple rules.");
        }
        suggestions.add("Use a unique password for each account; a password manager can generate one.");
        return new Result(score, verdict, suggestions);
    }

    // Abstract parent demonstrates inheritance and a common interface.
    private abstract static class PasswordRule {
        public abstract Finding check(char[] password);
    }

    private static class Finding {
        private int points;
        private boolean common;
        private final ArrayList<String> suggestions = new ArrayList<String>();
    }

    private static class LengthRule extends PasswordRule {
        @Override
        public Finding check(char[] p) {
            Finding f = new Finding();
            f.points = p.length >= 16 ? 60 : p.length >= 12 ? 45 : p.length >= 8 ? 25 : 0;
            if (p.length < 12) {
                f.suggestions.add("Make it at least 12 characters long; 16 or more is better for this demo.");
            } else if (p.length < 16) {
                f.suggestions.add("Add more length; this demo gives full length points at 16 characters.");
            }
            return f;
        }
    }

    private static class CharacterRule extends PasswordRule {
        @Override
        public Finding check(char[] p) {
            boolean lower = false, upper = false, digit = false, symbol = false;
            for (char c : p) {
                lower = lower || Character.isLowerCase(c);
                upper = upper || Character.isUpperCase(c);
                digit = digit || Character.isDigit(c);
                symbol = symbol || (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c));
            }
            Finding f = new Finding();
            if (lower) { f.points += 10; } else { f.suggestions.add("Include lowercase letters for more variety."); }
            if (upper) { f.points += 10; } else { f.suggestions.add("Include uppercase letters for more variety."); }
            if (digit) { f.points += 10; } else { f.suggestions.add("Include a digit for more variety."); }
            if (symbol) { f.points += 10; } else { f.suggestions.add("Include a symbol for more variety."); }
            return f;
        }
    }

    private static class PatternRule extends PasswordRule {
        private final List<String> weakWords = Arrays.asList("password", "123456", "qwerty", "letmein", "admin");
        @Override
        public Finding check(char[] p) {
            Finding f = new Finding();
            for (String word : weakWords) {
                if (containsIgnoreCase(p, word)) {
                    f.common = true;
                    f.suggestions.add("Avoid common weak words or number patterns, even with added symbols.");
                    break;
                }
            }
            boolean repeated = false;
            for (int i = 2; i < p.length; i++) {
                if (p[i] == p[i - 1] && p[i] == p[i - 2]) { repeated = true; break; }
            }
            if (repeated) {
                f.points -= 20;
                f.suggestions.add("Avoid runs of three or more identical characters (-20 points).");
            }
            boolean sequential = false;
            for (int i = 2; i < p.length; i++) {
                char a = Character.toLowerCase(p[i - 2]);
                char b = Character.toLowerCase(p[i - 1]);
                char c = Character.toLowerCase(p[i]);
                boolean letters = a >= 'a' && a <= 'z' && b >= 'a' && b <= 'z' && c >= 'a' && c <= 'z';
                boolean numbers = a >= '0' && a <= '9' && b >= '0' && b <= '9' && c >= '0' && c <= '9';
                if ((letters || numbers) && ((b == a + 1 && c == b + 1) || (b == a - 1 && c == b - 1))) {
                    sequential = true; break;
                }
            }
            if (sequential) {
                f.points -= 20;
                f.suggestions.add("Avoid three-character alphabet or number sequences, forward or backward (-20 points).");
            }
            return f;
        }
        private boolean containsIgnoreCase(char[] p, String word) {
            for (int start = 0; start <= p.length - word.length(); start++) {
                boolean match = true;
                for (int j = 0; j < word.length(); j++) {
                    if (Character.toLowerCase(p[start + j]) != word.charAt(j)) { match = false; break; }
                }
                if (match) { return true; }
            }
            return false;
        }
    }

    // Result contains only the score/verdict/suggestions, never the password.
    public static class Result {
        private final int score;
        private final String verdict;
        private final ArrayList<String> suggestions;
        public Result(int score, String verdict, ArrayList<String> suggestions) {
            this.score = score;
            this.verdict = verdict;
            this.suggestions = new ArrayList<String>(suggestions);
        }
        public int getScore() { return score; }
        public String getVerdict() { return verdict; }
        public ArrayList<String> getSuggestions() { return new ArrayList<String>(suggestions); }
    }
}
