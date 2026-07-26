package com.mochxd;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ShowBug {

  private static final String PASSWORD = System.getenv().getOrDefault("APP_PASSWORD", "MyP@ss1!");
  private static final Path FILTERS = Path.of("log-filters.json");
  private static final Path LOG = Path.of("appium.log");
  private static final String BASE = "http://127.0.0.1:4723";

  public static void main(String[] args) throws Exception {
    Files.writeString(FILTERS, "[{\"text\":\"" + PASSWORD + "\"}]\n", StandardCharsets.UTF_8);
    Files.deleteIfExists(LOG);

    System.out.println("Password: " + PASSWORD);
    System.out.println("Starting Appium with --log-filters...");

    Process appium = new ProcessBuilder(
            "appium", "server",
            "--port", "4723",
            "--use-drivers", "fake",
            "--log-filters", FILTERS.toAbsolutePath().toString(),
            "--log", LOG.toAbsolutePath().toString(),
            "--log-level", "info")
        .inheritIO()
        .start();

    try {
      waitForServer();
      String sessionId = createSession();
      System.out.println("Session opened: " + sessionId);
      deleteSession(sessionId);

      String logText = Files.readString(LOG, StandardCharsets.UTF_8);
      System.out.println();
      if (logText.contains(PASSWORD)) {
        System.out.println("FAIL: Appium log still contains the password:");
        System.out.println(PASSWORD);
        System.exit(1);
      }
      System.out.println("OK: password was masked in the Appium log.");
    } finally {
      appium.destroy();
      appium.waitFor(10, TimeUnit.SECONDS);
    }
  }

  private static void waitForServer() throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest status = HttpRequest.newBuilder(URI.create(BASE + "/status"))
        .timeout(Duration.ofSeconds(2))
        .GET()
        .build();

    for (int i = 0; i < 60; i++) {
      try {
        HttpResponse<String> res = client.send(status, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 200) {
          System.out.println("Appium is up");
          return;
        }
      } catch (Exception ignored) {
        // still starting
      }
      Thread.sleep(1000);
    }
    throw new IllegalStateException("Appium did not start");
  }

  private static String createSession() throws Exception {
    String body = """
        {
          "capabilities": {
            "alwaysMatch": {
              "platformName": "Fake",
              "appium:automationName": "Fake",
              "appium:deviceName": "Linux",
              "appium:app": "/foo/bar.app",
              "appium:password": "%s"
            }
          }
        }
        """.formatted(PASSWORD);

    HttpResponse<String> res = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create(BASE + "/session"))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build(),
        HttpResponse.BodyHandlers.ofString());

    System.out.println("Create session status: " + res.statusCode());
    if (res.statusCode() < 200 || res.statusCode() >= 300) {
      throw new IllegalStateException("Could not open session: " + res.body());
    }

    String json = res.body();
    int start = json.indexOf("\"sessionId\":\"") + 13;
    int end = json.indexOf('"', start);
    return json.substring(start, end);
  }

  private static void deleteSession(String sessionId) throws Exception {
    HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create(BASE + "/session/" + sessionId))
            .timeout(Duration.ofSeconds(15))
            .DELETE()
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
