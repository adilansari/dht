# The goal of this app is simple: enabling two Android devices to send messages to each other.

There is only one text box on screen where a user of the device can write a text message to the other device.

The other device should be able to display on screen what was received. And vice versa. Used Java Socket API. All communication over TCP. Assumed that the size of a message will never exceed 128 bytes (characters).

In the app, we can open only one server socket that listens on port 10000 regardless of which AVD your app runs on.

The app on avd0 can connect to the listening server socket of the app on avd1 by connecting to <ip>:<port> == 10.0.2.2:11112.

The app on avd1 can connect to the listening server socket of the app on avd0 by connecting to <ip>:<port> == 10.0.2.2:11108.


## References

[1] More project details [here](https://docs.google.com/document/d/1JqBqZChFWzbnTXgbYB8Z6l9IRlzt8c0loIwa6ymkPps/pub)

[2] Single best resource on Android, [Android dev](http://developer.android.com)


## Screenshots

Product demo:


![Emulator-1](https://raw.github.com/adilansari/Android-Messenger/master/screen/Emulator-1.png)

![Emulator-2](https://raw.github.com/adilansari/Android-Messenger/master/screen/Emulator-2.png)
