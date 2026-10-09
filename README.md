# CyberSafetyApp

A simple Java Swing college mini project with two offline tools in one window:

- **Phishing URL Detector:** checks a pasted link for warning signs and shows a Safe, Suspicious or High Risk verdict, points out of 100, the actual host and reasons.
- **Password Strength Checker:** checks a practice password for length, character variety and obvious patterns. Shows Weak, Medium or Strong, a score and improvement suggestions.

The interface uses Java's built-in Nimbus theme, a teal header, separate tabs, score bars and readable verdict colors. No external libraries are needed.

## Requirements and run steps

Install JDK 8 or later. A desktop graphical display is required. Keep all four Java source files together and open a terminal in that folder:

```sh
javac UrlRule.java UrlDetector.java PasswordChecker.java CyberSafetyApp.java
java CyberSafetyApp
```

Or open these files in IntelliJ IDEA, Eclipse or NetBeans and run `CyberSafetyApp.main()`.

## How to use

### URL detector tab

Paste a complete `http://` or `https://` link and click **Check URL**, or press Enter. Read the verdict, actual host and reasons. Clear resets the tab. The prefilled fictional sample scores **High Risk, 90/100** when checked:

```text
http://paypa1-secure-login.verify-account.xyz/signin
```

The app never visits the link, makes network calls, resolves DNS or follows redirects.

### Password strength tab

Type a **practice example**, not your real account password, and click **Check strength**, or press Enter. Input is masked; there is no reveal button, and the result never repeats the typed password. Click Clear to remove the input and result.

No password is sent online, written to a file or logged by the app. The checker receives a temporary `char[]` which the GUI overwrites after checking. The masked input remains in the Swing field until Clear or closing the app. Java/Swing memory clearing is not a guarantee of secure erasure.

## URL rules (unchanged from the original detector)

| Check | Points |
| --- | ---: |
| HTTP rather than HTTPS | 20 |
| IPv4 or IPv6 address as host | 30 |
| More than four domain labels, excluding IP addresses | 15 |
| Punycode `xn--` in host | 20 |
| TLD on the demo caution list: `.xyz`, `.top`, `.click`, `.zip`, `.mov` | 15 |
| Single-label host such as localhost | 10 |
| User information before `@` in the authority | 30 |
| `@` only in path/query | 5 |
| Substrings login, verify, secure, account, update, bank, free, bonus | 5 per distinct word, maximum 25 |
| Host contains paypa1, g00gle or faceb00k | 35, once |
| URL longer than 200 characters | 10 |
| Listed shortener or its subdomain: bit.ly, tinyurl.com, t.co, is.gd, goo.gl | 25 |

Add the points and cap at 100. **Safe: 0-24; Suspicious: 25-59; High Risk: 60-100.** Empty or malformed URLs are not scored.

## Password rules

These weights are transparent project choices, not calibrated estimates of cracking time.

| Check | Points or effect |
| --- | --- |
| Length below 8 | 0 length points; final score capped at 39 |
| Length 8-11 | 25 |
| Length 12-15 | 45 |
| Length 16 or more | 60 |
| Lowercase present | +10 |
| Uppercase present | +10 |
| Digit present | +10 |
| Non-letter/digit symbol present, excluding whitespace | +10 |
| Three or more consecutive identical characters | -20, once |
| Three ASCII letters or digits in consecutive forward/backward order, such as abc or 321 | -20, once |
| Contains password, 123456, qwerty, letmein or admin, ignoring case | Final score capped at 20 |

Clamp the score to 0-100. **Weak: 0-39; Medium: 40-74; Strong: 75-100.** Empty/null or whitespace-only input is not scored. Spaces count toward length when mixed with other characters but do not earn symbol points. Checks count Java `char` units; this is not a full Unicode password analyzer.

Suggestions explain missing character categories, insufficient length, weak words and repeated/sequential patterns. Common-word matches do not display the matching word or the input.

## OOP concepts for a viva

- **Classes/objects:** the app creates checker objects and result objects.
- **Inheritance:** seven URL rule classes extend abstract `UrlRule`. Three password rule classes extend abstract `PasswordRule`. `CyberSafetyApp` extends `JFrame`.
- **Encapsulation:** fields and collections are private. Result data is accessed through getters, and reasons/suggestions are returned as list copies.
- **Polymorphism:** each checker loops through parent rule references and calls `check()`. Java runs the child class's overridden implementation.
- **Collections:** `ArrayList` stores rules, reasons and suggestions; a list holds common weak words.

## Files

- `UrlRule.java`: abstract URL rule and child classes.
- `UrlDetector.java`: local URL parsing, original scoring and result data.
- `PasswordChecker.java`: abstract/nested password rules, score and suggestions.
- `CyberSafetyApp.java`: tabbed Swing GUI and main method.
- `CyberSafety-preview.png`: actual running app showing a weak practice password, masked input and suggestions.

## Limits and testing

**Educational demo only.** Safe does not prove a URL is safe, and Strong does not guarantee password security. Scores are rule points, not probabilities. The app does not inspect websites, certificates, domain ownership, redirects, reputation, breaches or account details. HTTPS does not prove trust; legitimate sites can trigger warning rules, and phishing links can score zero. Fixed lists and substring checks are intentionally basic. Encoded keywords and many lookalikes/patterns are missed. A long predictable password can fool a simple strength checker.

Compiled on Java 11 with no compiler lint warnings. Tested the retained URL rules on normal HTTPS samples, suspicious URLs, empty/malformed input, IPv4/IPv6, @ positions, Unicode/punycode, trailing dots, long URLs and score caps. Tested password length/variety, common weak words, suffix tricks, repeated characters, forward/backward sequences, score thresholds/caps, null/empty/whitespace input and short-password limits. Both tabs' check/clear actions, invalid-result reset and no password echo were exercised in the running GUI.
