Apologies for the confusion! I see that I missed including the necessary Kotlin code for both `MainApplication.kt` and `PingModule.kt` in the `README.md`. Here is the updated version of the file, with the full code included for both files:

```markdown
# React Native Ping Module

A simple React Native module that allows you to ping an IP address and retrieve the results directly within your mobile app. Built with Kotlin for Android.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Example](#example)
- [Android Permissions](#android-permissions)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)
- [Code Files](#code-files)

## Installation

1. **Install the package**:

   You can install the package via npm or yarn.

   ```bash
   npm install react-native-ping-module --save
   ```

   Or with Yarn:

   ```bash
   yarn add react-native-ping-module
   ```

2. **Link the package**:

   For React Native 0.59 and earlier, you need to link the native module:

   ```bash
   react-native link react-native-ping-module
   ```

   For React Native 0.60 and above, the linking process is automatic via **autolinking**, so this step is not required.

## Usage

To use the ping module, simply import it into your JavaScript file and call the `ping` function with the IP address you wish to ping.

### Example

Here’s how you can use the `ping` function in your React Native app:

```javascript
import React, { useState } from 'react';
import { NativeModules, Text, Button, ScrollView, TextInput, StyleSheet, ActivityIndicator, View } from 'react-native';

const { PingModule } = NativeModules;

const PingInfo = () => {
  const [pingResult, setPingResult] = useState<string[]>([]); // Store the output lines
  const [loading, setLoading] = useState(false); // Loading state
  const [data, setData] = useState<string | null>(null);

  const handlePing = async () => {
    setLoading(true); // Start loading
    setPingResult([]); // Clear previous results

    try {
      // Call the ping method from the native module and await the result
      const result: string = await PingModule.ping(data);

      // Split the result into lines and show each line one by one
      const lines = result.split('\n');
      lines.forEach((line, index) => {
        setTimeout(() => {
          setPingResult(prevResult => [...prevResult, line]);
        }, index * 500); // Add a delay to simulate line-by-line execution
      });
    } catch (error) {
      console.error('Ping error:', error);
      if (error instanceof Error) {
        setPingResult([error.message || 'Ping failed.']); // Set the error message if ping fails
      } else {
        setPingResult(['Ping failed.']); // Set a generic error message if error is not an instance of Error
      }
    }

    setLoading(false); // Stop loading once the ping operation is done
  };

  return (
    <View style={styles.pingContainer}>
      <Text style={styles.ping}>Ping Component IP</Text>
      <TextInput
        style={styles.input}
        onChangeText={text => setData(text)}
        value={data ? data : ''}
        placeholder="Enter IP address"
      />
      <Button title="Ping" onPress={handlePing} />
      <>
        {loading ? (
          <ActivityIndicator size="large" color="#0000ff" style={styles.loader} />
        ) : (
          <ScrollView style={styles.scroll}>
            {pingResult.map((line, index) => (
              <Text key={index}>{line}</Text>
            ))}
          </ScrollView>
        )}
      </>
    </View>
  );
};

const styles = StyleSheet.create({
  pingContainer: {
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 10,
    padding: 10,
    backgroundColor: '#f9f9f9',
  },
  ping: {
    fontSize: 20,
    fontWeight: 'bold',
    textAlign: 'center',
    marginVertical: 10,
    color: 'skyblue',
  },
  input: {
    width: '100%',
    height: 40,
    borderColor: 'gray',
    borderWidth: 0.5,
    marginVertical: 10,
    paddingLeft: 10,
    borderRadius: 5,
  },
  scroll: {
    width: '100%',
    marginVertical: 10,
    marginTop: 20,
  },
  loader: {
    marginTop: 20,
  },
});

export default PingInfo;
```

### Method Documentation

#### `PingModule.ping(host: string)`

- **Arguments**:
  - `host`: The IP address or domain to ping (e.g., `8.8.8.8` or `google.com`).
  
- **Returns**:
  - Returns a promise with the formatted output from the ping operation.
  - The result includes information such as response time and packet loss.

---

## Android Permissions

To allow the app to perform network operations like pinging, ensure the following permissions are added to your `AndroidManifest.xml` file:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
  <application
    android:name=".MainApplication"
    android:label="@string/app_name"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:allowBackup="false"
    android:theme="@style/AppTheme"
    android:supportsRtl="true">
    <activity
      android:name=".MainActivity"
      android:label="@string/app_name"
      android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|screenSize|smallestScreenSize|uiMode"
      android:launchMode="singleTask"
      android:windowSoftInputMode="adjustResize"
      android:exported="true">
      <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>
    </activity>
  </application>
</manifest>
```

---

## Code Files

### `PingModule.kt`

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
           

 "Reply from $ip: time=$time ms"
        } else {
            line
        }
    }

    private fun formatStatisticsLine(line: String): String {
        // Example raw statistics line: "4 packets transmitted, 4 received, 0% packet loss, time 3003ms"
        val regex = Regex("([0-9]+) packets transmitted, ([0-9]+) received, ([0-9]+)% packet loss")
        val match = regex.find(line)
        return if (match != null) {
            "Packets transmitted: ${match.groupValues[1]}, Packets received: ${match.groupValues[2]}, Loss: ${match.groupValues[3]}%"
        } else {
            line
        }
    }

    private fun formatRTTLine(line: String): String {
        // Example raw RTT line: "rtt min/avg/max/mdev = 47.874/49.035/50.288/0.963 ms"
        val regex = Regex("rtt min/avg/max/mdev = ([0-9.]+)/([0-9.]+)/([0-9.]+)/([0-9.]+) ms")
        val match = regex.find(line)
        return if (match != null) {
            "Min RTT: ${match.groupValues[1]} ms, Avg RTT: ${match.groupValues[2]} ms, Max RTT: ${match.groupValues[3]} ms"
        } else {
            line
        }
    }
}
```

### `MainApplication.kt`

```kotlin
package com.projectname // Replace with your project name

import android.app.Application
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.shell.MainReactPackage
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactContext
import com.facebook.react.ReactInstanceManager
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import com.facebook.react.modules.network.NetworkingModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.ReactActivity

class MainApplication : Application(), ReactApplication {

    private val mReactNativeHost: ReactNativeHost = object : ReactNativeHost(this) {
        override fun getUseDeveloperSupport(): Boolean {
            return BuildConfig.DEBUG
        }

        override fun getPackages(): List<ReactPackage> {
            return listOf(MainReactPackage(), PingModulePackage())
        }

        override fun getJSMainModuleName(): String {
            return "index"
        }
    }

    override fun getReactNativeHost(): ReactNativeHost {
        return mReactNativeHost
    }
}
```

---

## Troubleshooting

- **Issue**: If the `ping` method returns an error, make sure you have added the required permissions in `AndroidManifest.xml` and ensure your app has proper network access.
- **Issue**: If you see a build error related to Kotlin, make sure your project is properly configured to use Kotlin as mentioned earlier.

---

## Contributing

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/feature-name`).
3. Make your changes.
4. Commit your changes (`git commit -am 'Add new feature'`).
5. Push to the branch (`git push origin feature/feature-name`).
6. Create a new Pull Request.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---