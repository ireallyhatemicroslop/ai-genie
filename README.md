# Galaxy A03s Root Guide

A small offline preparation guide for owners of the Samsung Galaxy A03s. It explains the checks and trade-offs involved in the bootloader-unlock and Magisk workflow, with a review checklist in the Android app and browser page.

## Important limitations

- This is a guide, not a one-tap rooting utility. It does not access devices, unlock bootloaders, patch images, flash firmware, or grant root.
- Galaxy A03s variants, regions, carrier policies, and bootloader states differ. Entering a model number is informational only; this project does not claim that any particular variant is unlockable.
- Bootloader unlocking erases user data. Rooting trips Samsung Knox permanently and may affect warranty, Secure Folder, payment/banking apps, and software updates.
- Use only firmware and instructions verified for the exact device model and region. Stop if OEM unlocking is unavailable or the instructions do not match your device.

## Android app

Open the `android` project in Android Studio and run the `app` configuration. The app uses the included Gradle configuration and requires Android SDK 35.

## Browser guide

Open [index.html](index.html) in a browser. The checklist stays in the current page and does not send the entered model number anywhere.

## References

- [Official Magisk installation guide](https://topjohnwu.github.io/Magisk/install.html)
