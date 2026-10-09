import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

// Pure Swing interface. The two checker classes handle the rule logic.
public class CyberSafetyApp extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color BACKGROUND = new Color(241, 245, 249);
    private static final Color INK = new Color(29, 48, 66);
    private static final Color TEAL = new Color(15, 111, 119);
    private final UrlDetector urlDetector = new UrlDetector();
    private final PasswordChecker passwordChecker = new PasswordChecker();
    private final JTextField urlField = new JTextField("http://paypa1-secure-login.verify-account.xyz/signin");
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel urlVerdict = new JLabel("Ready to check a URL");
    private final JLabel passwordVerdict = new JLabel("Ready to check a password");
    private final JTextArea urlReasons = outputArea();
    private final JTextArea passwordSuggestions = outputArea();
    private final JProgressBar urlScore = scoreBar();
    private final JProgressBar passwordScore = scoreBar();
    private final JLabel hostLabel = new JLabel("Actual host: -");

    public CyberSafetyApp() {
        super("Cyber Safety - URL and Password Checker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 800);
        setMinimumSize(new Dimension(940, 740));
        setLocationRelativeTo(null);
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));
        setContentPane(content);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBackground(TEAL);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JLabel title = new JLabel("Cyber Safety");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Two simple tools. Clear reasons. Fully offline.");
        subtitle.setForeground(new Color(223, 243, 244));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        content.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        tabs.addTab("URL detector", buildUrlPanel());
        tabs.addTab("Password strength", buildPasswordPanel());
        content.add(tabs, BorderLayout.CENTER);

        // Right-click Cut/Copy/Paste menus on both input fields.
        addRightClickMenu(urlField);
        addRightClickMenu(passwordField);
        JLabel footer = new JLabel("Educational checks only. Nothing is sent online or saved by this app.");
        footer.setForeground(new Color(75, 94, 109));
        content.add(footer, BorderLayout.SOUTH);
    }

    // Adds a right-click menu with Cut, Copy and Paste to a text field.
    private static void addRightClickMenu(JTextField field) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new DefaultEditorKit.CutAction()).setText("Cut");
        menu.add(new DefaultEditorKit.CopyAction()).setText("Copy");
        menu.add(new DefaultEditorKit.PasteAction()).setText("Paste");
        field.setComponentPopupMenu(menu);
    }

    private JPanel toolPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        return p;
    }

    private JPanel intro(String title, String hint) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
        heading.setForeground(INK);
        p.add(heading, BorderLayout.NORTH);
        p.add(new JLabel(hint), BorderLayout.SOUTH);
        return p;
    }

    private JButton actionButton(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setPreferredSize(new Dimension(120, 42));
        if (primary) {
            b.setBackground(TEAL);
            b.setForeground(Color.WHITE);
        }
        return b;
    }

    private JPanel inputRow(JTextField input, JButton check, JButton clear) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        input.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        input.setPreferredSize(new Dimension(400, 44));
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(check);
        buttons.add(clear);
        row.add(input, BorderLayout.CENTER);
        row.add(buttons, BorderLayout.EAST);
        return row;
    }

    private JPanel resultPanel(JLabel verdict, JProgressBar score, JTextArea reasons, JLabel detail) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        verdict.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        verdict.setForeground(INK);
        top.add(verdict, BorderLayout.NORTH);
        top.add(score, BorderLayout.CENTER);
        top.add(detail, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(reasons);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 229, 235)));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildUrlPanel() {
        JPanel p = toolPanel();
        JButton check = actionButton("Check URL", true);
        JButton clear = actionButton("Clear", false);
        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(intro("Inspect a link before you trust it", "Paste a complete http:// or https:// URL. The app never visits it."), BorderLayout.NORTH);
        top.add(inputRow(urlField, check, clear), BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);
        p.add(resultPanel(urlVerdict, urlScore, urlReasons, hostLabel), BorderLayout.CENTER);
        p.add(note("Safe means a low rule score, not proven safety. HTTPS does not prove trust.",
                "No page contents, redirects or reputation are checked."), BorderLayout.SOUTH);
        check.addActionListener(e -> checkUrl());
        urlField.addActionListener(e -> checkUrl());
        clear.addActionListener(e -> {
            urlField.setText("");
            reset(urlVerdict, urlScore, urlReasons, "Ready to check a URL");
            hostLabel.setText("Actual host: -");
            urlField.requestFocusInWindow();
        });
        return p;
    }

    private JPanel buildPasswordPanel() {
        JPanel p = toolPanel();
        JButton check = actionButton("Check strength", true);
        JButton clear = actionButton("Clear", false);
        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(intro("Build a better password", "Use a practice password. Input stays masked and is never shown in the result."), BorderLayout.NORTH);
        top.add(inputRow(passwordField, check, clear), BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);
        p.add(resultPanel(passwordVerdict, passwordScore, passwordSuggestions,
                new JLabel("Improvement suggestions")), BorderLayout.CENTER);
        p.add(note("Strong means a high score under simple rules, not guaranteed security.",
                "No breach database or account information is checked."), BorderLayout.SOUTH);
        check.addActionListener(e -> checkPassword());
        passwordField.addActionListener(e -> checkPassword());
        clear.addActionListener(e -> {
            passwordField.setText("");
            reset(passwordVerdict, passwordScore, passwordSuggestions, "Ready to check a password");
            passwordField.requestFocusInWindow();
        });
        return p;
    }

    private JTextArea note(String first, String second) {
        JTextArea a = new JTextArea(first + "\n" + second);
        a.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        a.setForeground(new Color(75, 94, 109));
        a.setOpaque(false);
        a.setEditable(false);
        a.setFocusable(false);
        return a;
    }

    private static JTextArea outputArea() {
        JTextArea a = new JTextArea();
        a.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        a.setForeground(INK);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setMargin(new java.awt.Insets(16, 18, 16, 18));
        return a;
    }

    private static JProgressBar scoreBar() {
        JProgressBar b = new JProgressBar(0, 100);
        b.setStringPainted(true);
        b.setString("Not checked");
        b.setPreferredSize(new Dimension(400, 24));
        return b;
    }

    private void display(JLabel label, JProgressBar bar, JTextArea area,
                         String verdict, int score, Color color, List<String> lines, String thresholds) {
        label.setText(verdict + "  |  " + score + " / 100");
        label.setForeground(color);
        bar.setValue(score);
        bar.setForeground(color);
        bar.setString(score + " / 100");
        StringBuilder text = new StringBuilder();
        int i = 1;
        for (String line : lines) {
            text.append(i++).append(". ").append(line).append("\n\n");
        }
        text.append(thresholds).append("\nRule points only, not a probability or a security guarantee.");
        area.setText(text.toString());
        area.setCaretPosition(0);
    }

    private void checkUrl() {
        try {
            UrlDetector.Result r = urlDetector.analyze(urlField.getText());
            Color color = r.getScore() >= 60 ? new Color(165, 37, 47)
                    : r.getScore() >= 25 ? new Color(151, 98, 0) : new Color(25, 113, 76);
            display(urlVerdict, urlScore, urlReasons, r.getVerdict(), r.getScore(), color,
                    r.getReasons(), "0-24 Safe | 25-59 Suspicious | 60-100 High Risk");
            hostLabel.setText("Actual host: " + r.getHost());
        } catch (IllegalArgumentException ex) {
            invalid(urlVerdict, urlScore, urlReasons, ex.getMessage());
            hostLabel.setText("Actual host: -");
        }
    }

    private void checkPassword() {
        char[] password = passwordField.getPassword();
        try {
            PasswordChecker.Result r = passwordChecker.analyze(password);
            Color color = r.getScore() >= 75 ? new Color(25, 113, 76)
                    : r.getScore() >= 40 ? new Color(151, 98, 0) : new Color(165, 37, 47);
            display(passwordVerdict, passwordScore, passwordSuggestions, r.getVerdict(), r.getScore(),
                    color, r.getSuggestions(), "0-39 Weak | 40-74 Medium | 75-100 Strong");
        } catch (IllegalArgumentException ex) {
            invalid(passwordVerdict, passwordScore, passwordSuggestions, ex.getMessage());
        } finally {
            // Clear the temporary array after checking. The masked input remains
            // only until the user clears it or closes the window.
            Arrays.fill(password, '\0');
        }
    }

    private void reset(JLabel label, JProgressBar bar, JTextArea area, String message) {
        label.setText(message);
        label.setForeground(INK);
        bar.setValue(0);
        bar.setString("Not checked");
        area.setText("");
    }

    private void invalid(JLabel label, JProgressBar bar, JTextArea area, String message) {
        reset(label, bar, area, "Invalid input - not scored");
        label.setForeground(new Color(165, 37, 47));
        area.setText(message);
    }

    public static void main(String[] args) {
        // Nimbus is included in Java. No theme library is required.
        try {
            for (UIManager.LookAndFeelInfo theme : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(theme.getName())) {
                    UIManager.setLookAndFeel(theme.getClassName());
                    break;
                }
            }
        } catch (Exception ex) { /* The default Swing theme still works. */ }
        UIManager.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        SwingUtilities.invokeLater(() -> new CyberSafetyApp().setVisible(true));
    }
}