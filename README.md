# Appium log-filters bug (Java demo)

Author: Mohamed Mostafa ([@Mochxd](https://github.com/Mochxd))

## What it does

1. Takes password `MyP@ss1!`
2. Builds the same `\b...\b` filter Appium uses for `--log-filters`
3. Checks a fake Appium HTTP log line

The password is still there → job fails.

## Run

```bash
mvn -q compile exec:java
# or
APP_PASSWORD='MyP@ss1!' mvn -q compile exec:java
```
