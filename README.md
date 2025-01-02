Here's a detailed `README.md` template you can use for your **react-native-ping-module**:

---

# React Native Ping Module

A simple React Native module that allows you to ping an IP address and retrieve the results directly within your mobile app. Built with Kotlin for Android.

## Table of Contents

- [React Native Ping Module](#react-native-ping-module)
  - [Table of Contents](#table-of-contents)
  - [Installation](#installation)
  - [Usage](#usage)
    - [Example](#example)
    - [Method Documentation](#method-documentation)
      - [`PingModule.ping(host: string)`](#pingmodulepinghost-string)
  - [Android Permissions](#android-permissions)
  - [Troubleshooting](#troubleshooting)
  - [Contributing](#contributing)
  - [License](#license)

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
