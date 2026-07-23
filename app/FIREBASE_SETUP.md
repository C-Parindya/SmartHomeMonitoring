# Firebase Setup

Before building, you must place your `google-services.json` file in the `app/` directory.

To obtain it:
1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create or select your project
3. Add an Android app with package name `com.example.smarthome`
4. Download `google-services.json` and place it in `app/`

The project is configured to use:
- Firebase Authentication (`firebase-auth-ktx`)
- Firebase Realtime Database (`firebase-database-ktx`)
- Firebase Cloud Messaging (`firebase-messaging-ktx`) — for push notifications from safety cutoffs
