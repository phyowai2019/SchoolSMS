# ကျောင်း စီမံခန့်ခွဲမှု — Android App

School Management System WebView App

## အမြန်တည်ဆောက်နည်း (Android Studio)

### လိုအပ်သောဆော့ဖ်ဝဲ
- Android Studio (Arctic Fox or newer)
- JDK 8 or higher
- Android SDK API 21+

### အဆင့်များ
1. Android Studio ဖွင့်ပါ
2. **File → Open** → ဤ folder ကိုရွေးပါ
3. Gradle sync ပြီးဆုံးရန် စောင့်ပါ
4. **Build → Generate Signed APK** (သို့) **Run** ကိုနှိပ်ပါ

## APK Build (Command Line)

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk

./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release-unsigned.apk
```

## App Features
- ✅ Full WebView — SMS URL တစ်ခုတည်း load
- ✅ Back button navigation
- ✅ Offline detection + retry
- ✅ File chooser (ဓာတ်ပုံ upload)
- ✅ Cookie support (login persistent)
- ✅ Mobile-friendly CSS injection
- ✅ Hamburger menu button (sidebar toggle)
- ✅ Progress bar
- ✅ Hardware accelerated

## App URL
```
https://script.google.com/macros/s/AKfycbzfZENGzuFlqfq3_RDpwRESZMpWn_TshFlGu6I3AX6S3CbRlXbjmgqgJsiz5lrOTDxePA/exec
```

## Min Requirements
- Android 5.0 (API 21) နှင့်အထက်
- Internet connection လိုသည်
