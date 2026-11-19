# ClipNest

ClipNest is an Android app that runs yt-dlp locally. Its navigation bar sits at the bottom and has four pages: **Download**, **Updates**, **Browser**, and **Web config**.

## Build

Open this project folder in Android Studio and run the `app` configuration, or from the project root run:

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Pages and behavior

- **Download:** Paste a URL, select video or MP3 audio, choose quality, and save to `Downloads/ClipNest` or a user-selected folder. Destination and quality choices are remembered.
- **Gallery-dl:** The Download page also accepts a gallery/site URL and runs the bundled Gallery-dl Python integration, copying all downloaded files to the same ClipNest destination.
- **Updates:** Displays the installed yt-dlp version and provides a manual nightly update button. It is independent of the download URL. ClipNest also checks nightly at startup and attempts one update/retry after an explicit over-90-days failure.
- **Browser:** In-app WebView with the maximum available page area. Navigate directly to YouTube or Google and sign in inside the WebView after enabling the cookie settings.
- **Cookie handoff:** The **Web config** page contains **Export cookies** and **Import cookies.txt**. Export writes the current WebView session as a Netscape `cookies.txt` file; import copies a user-owned cookie file into app-private storage. Downloads pass the selected file to yt-dlp with `--cookies`.
- **Web config:** Persisted controls for JavaScript, first-party cookies, third-party cookies, DOM storage, and zoom, plus a button to clear cookies, site data, cache, and history. Cookie files can access the account; keep them private and revoke them if shared.

The `--no-update` option only suppresses yt-dlp's old-version warning; ClipNest updates yt-dlp rather than hiding that warning. Downloads run while the app is open and may be interrupted if Android reclaims the app.

## Responsible use

Only download media you own or are authorized to save, and follow source-site terms and applicable law. ClipNest does not collect Google passwords. The cookie handoff is user-initiated and is intended for accounts the user owns or is authorized to use; it does not bypass DRM or access controls. Cookie files are bearer credentials: do not share them, commit them, or upload them. Third-party license texts and notices are included with the source project.
