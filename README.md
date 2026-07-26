# Appium log-filters bug

Author: Mohamed Mostafa ([@Mochxd](https://github.com/Mochxd))

Starts Appium, opens one Fake session with password `MyP@ss1!`, checks `appium.log`.

```bash
npm install -g appium
appium driver install --source=npm @appium/fake-driver
mvn -q compile exec:java
```
