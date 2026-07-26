package com.mochxd;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Shows that Appium-style text log filters miss passwords like MyP@ss1!
 * because they wrap the value in \\b word boundaries.
 */
public class ShowBug {

  public static void main(String[] args) throws Exception {
    String password = System.getenv().getOrDefault("APP_PASSWORD", "MyP@ss1!");

    // Same filter file Appium loads with --log-filters
    Path filters = Path.of("log-filters.json");
    Files.writeString(
        filters,
        "[{\"text\":\"" + jsonEscape(password) + "\"}]\n",
        StandardCharsets.UTF_8);

    System.out.println("Password: " + password);
    System.out.println("Wrote " + filters.toAbsolutePath());
    System.out.println();

    // Same pattern Appium builds for a plain "text" filter today
    String appiumPattern = "\\b" + escapeRegExp(password) + "\\b";
    Pattern pattern = Pattern.compile(appiumPattern);

    String logLine = "info HTTP POST /session {\"password\":\"" + password + "\"}";
    boolean masked = pattern.matcher(logLine).find();

    System.out.println("Appium filter pattern: " + appiumPattern);
    System.out.println("Log line: " + logLine);
    System.out.println();

    if (!masked) {
      System.out.println("FAIL: password is still visible: " + password);
      System.exit(1);
    }

    System.out.println("OK: password was masked.");
  }

  // Same idea as escapeRegExp() in @appium/logger
  private static String escapeRegExp(String value) {
    return value.replaceAll("[.*+?^${}()|\\[\\]\\\\]", "\\\\$0");
  }

  private static String jsonEscape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
