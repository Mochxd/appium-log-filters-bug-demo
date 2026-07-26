# Appium log-filters bug

Author: Mohamed Mostafa ([@Mochxd](https://github.com/Mochxd))

## What it does

1. Starts real Appium with `--log-filters` for password `MyP@ss1!`
2. Opens a Fake driver session that sends that password in capabilities
3. Checks `appium.log`

The password is still in the log → fail.

## Run

```bash
npm install -g appium
appium driver install fake
mvn -q compile exec:java
```
