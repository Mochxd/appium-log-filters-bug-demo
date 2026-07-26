package com.mochxd;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShowBug {
  public static void main(String[] args) throws Exception {
    String password = System.getenv().getOrDefault("APP_PASSWORD", "MyP@ss1!");
    Path filters = Path.of("log-filters.json");
    Path logFile = Path.of("appium.log");

    Files.writeString(filters, "[{\"text\":\"" + password + "\"}]");
    Files.deleteIfExists(logFile);

    Process appium = new ProcessBuilder(
        "appium", "server", "--port", "4723", "--use-drivers", "fake",
        "--log-filters", filters.toAbsolutePath().toString(),
        "--log", logFile.toAbsolutePath().toString(),
        "--log-level", "info")
        .inheritIO().start();

    try {
      Thread.sleep(8000); // wait for Appium

      String body = """
          {"capabilities":{"alwaysMatch":{
            "platformName":"Fake",
            "appium:automationName":"Fake",
            "appium:deviceName":"Linux",
            "appium:app":"fixtures/app.xml",
            "appium:password":"%s"
          }}}
          """.formatted(password);

      HttpClient http = HttpClient.newHttpClient();
      HttpResponse<String> created = http.send(
          HttpRequest.newBuilder(URI.create("http://127.0.0.1:4723/session"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
          HttpResponse.BodyHandlers.ofString());

      System.out.println("Create session: " + created.statusCode());
      System.out.println(created.body());

      String log = Files.readString(logFile);
      if (log.contains(password)) {
        System.out.println("FAIL: password still in Appium log: " + password);
        System.exit(1);
      }
      System.out.println("OK: password was masked");
    } finally {
      appium.destroyForcibly();
    }
  }
}
