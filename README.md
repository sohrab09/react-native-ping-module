Here is the complete `README.md` file that provides detailed instructions for installing, setting up, and using your package:

```markdown
# Ping Module for React Native

This is a custom React Native module that allows you to ping an IP address or host from your mobile app. It leverages the native Android functionality to perform the ping operation and return the results to the JavaScript code.

## Installation

### 1. Install the package

You can install the package using `npm` or `yarn`:

```bash
npm install ping-module --save
# OR
yarn add ping-module
```

### 2. Link Native Modules

For React Native versions 0.60 and above, linking of native modules is done automatically using auto-linking. However, if you're using an older version, you may need to link manually:

```bash
react-native link ping-module
```

## Android Setup

### 3. Modify `AndroidManifest.xml`

Add the required permissions in your `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

### 4. Modify `MainApplication.java` or `MainApplication.kt`

Make sure your `MainApplication.java` (or `MainApplication.kt`) is correctly set up to include the native module. Below is the `MainApplication.kt` file.

In `android/src/main/java/com/projectname/MainApplication.kt`, use the following code:

```kotlin
package com.projectname // Replace with your project name

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.soloader.OpenSourceMergedSoMapping
import com.facebook.soloader.SoLoader
import com.projectname.PingModule // Replace with your module name
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class MainApplication : Application(), ReactApplication {

  override val reactNativeHost: ReactNativeHost =
      object : DefaultReactNativeHost(this) {
        override fun getPackages(): List<ReactPackage> =
            PackageList(this).packages.apply {
              // Add PingModule manually
              add(
                  object : ReactPackage {
                      override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
                          return listOf(PingModule(reactContext))
                      }

                      override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
                          return emptyList()
                      }
                  }
              )
            }

        override fun getJSMainModuleName(): String = "index"

        override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

        override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
        override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
      }

  override val reactHost: ReactHost
    get() = getDefaultReactHost(applicationContext, reactNativeHost)

  override fun onCreate() {
    super.onCreate()
    SoLoader.init(this, OpenSourceMergedSoMapping)
    if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
      // If you opted-in for the New Architecture, we load the native entry point for this app.
      load()
    }
  }
}
```

### 5. Create the `PingModule.kt` File

Create a new Kotlin file `PingModule.kt` in the directory `android/src/main/java/com/projectname/` and add the following code:

```kotlin
package com.projectname // Replace with your project name

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import java.io.BufferedReader
import java.io.InputStreamReader

class PingModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "PingModule"
    }

    @ReactMethod
    fun ping(host: String, promise: Promise) {
        try {
            // Execute the ping command
            val process = Runtime.getRuntime().exec("ping -c 4 $host")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val outputLines = mutableListOf<String>()
            var line: String?

            // Read the output of the ping command
            while (reader.readLine().also { line = it } != null) {
                outputLines.add(line!!)
            }
            reader.close()
            process.waitFor()

            // Format the output
            val formattedOutput = formatPingOutput(outputLines, host)
            promise.resolve(formattedOutput)
        } catch (e: Exception) {
            promise.reject("Ping Error", e)
        }
    }

    private fun formatPingOutput(outputLines: List<String>, host: String): String {
        val result = StringBuilder()
        result.append("ping $host\n\n")

        val replyLines = outputLines.filter { it.contains("bytes from") || it.contains("icmp_seq") }
        if (replyLines.isNotEmpty()) {
            result.append("Pinging $host with 32 bytes of data:\n")
            replyLines.forEach { line ->
                result.append(formatReplyLine(line)).append("\n")
            }
        }

        val statisticsLine = outputLines.find { it.contains("packets transmitted") }
        statisticsLine?.let {
            result.append("\nPing statistics for $host:\n")
            result.append(formatStatisticsLine(it)).append("\n")
        }

        val rttLine = outputLines.find { it.contains("rtt") }
        rttLine?.let {
            result.append("Approximate round trip times in milli-seconds:\n")
            result.append(formatRTTLine(it))
        }

        return result.toString()
    }

    private fun formatReplyLine(line: String): String {
        // Example raw reply: "64 bytes from 8.8.8.8: icmp_seq=1 ttl=109 time=49 ms"
        val regex = Regex("bytes from ([^:]+):.*time=([0-9.]+) ms")
        val match = regex.find(line)
        return if (match != null) {
            val ip = match.groupValues[1]
            val time = match.groupValues[2]
            "Reply from $ip: bytes=32 time=${time}ms TTL=109"
        } else {
            line
        }
    }

    private fun formatStatisticsLine(line: String): String {
        // Example raw line: "4 packets transmitted, 4 received, 0% packet loss, time 3002ms"
        val regex = Regex("(\\d+) packets transmitted, (\\d+) received, (\\d+)% packet loss")
        val match = regex.find(line)
        return if (match != null) {
            val sent = match.groupValues[1]
            val received = match.groupValues[2]
            val loss = match.groupValues[3]
            "    Packets: Sent = $sent, Received = $received, Lost = ${sent.toInt() - received.toInt()} ($loss% loss)"
        } else {
            line
        }
    }

    private fun formatRTTLine(line: String): String {
        // Example raw line: "rtt min/avg/max/mdev = 49.163/49.567/50.031/0.370 ms"
        val regex = Regex("rtt min/avg/max/mdev = ([0-9.]+)/([0-9.]+)/([0-9.]+)/([0-9.]+) ms")
        val match = regex.find(line)
        return if (match != null) {
            val min = match.groupValues[1]
            val max = match.groupValues[3]
            val avg = match.groupValues[2]
            "    Minimum = ${min}ms, Maximum = ${max}ms, Average = ${avg}ms"
        } else {
            line
        }
    }
}
```

## Usage

### 6. Using the Module in JavaScript

Once you have installed and linked the module, you can now use the `PingModule` to ping an IP address or domain.

```javascript
import { NativeModules } from 'react-native';

const { PingModule } = NativeModules;

const pingHost = async (host) => {
  try {
    const result = await PingModule.ping(host);
    console.log(result);
  } catch (error) {
    console.error('Ping failed:', error);
  }
};
```

Call the `pingHost` function with the host or IP address you wish to ping:

```javascript
pingHost('8.8.8.8'); // Ping Google's public DNS server
```

## Troubleshooting

- **Error: `Ping Error`**: Ensure that the app has the necessary permissions and the device is connected to the internet.
- **No results shown**: Check if the device is connected to a Wi-Fi network and if the `ping` command is being executed correctly.

## License

This project is licensed under the MIT License.
```

This README provides instructions on:

1. **Installation**: How to install the package and link it.
2. **Android Setup**: How to configure permissions and set up the native module in `MainApplication.kt`.
3. **Usage**: Example of how to call the `ping` method from JavaScript.
4. **Troubleshooting**: Common issues and how to resolve them.

Feel free to modify the instructions according to your package's specific needs. Let me know if you need more adjustments!