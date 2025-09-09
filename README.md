# Cursor Overlay Android (minimal)

This is a minimal Android app that shows a Windows cursor (.cur/.ico) as an overlay at the last touch position.

What it does
- Let you pick a cursor file from storage.
- Starts an overlay service that draws the cursor bitmap at the last touch.

Limitations
- The included parser only handles PNG-backed cursor/icon images. Classic XOR/MASK cursors are not supported.
- Does not implement animated .ani parsing.
- Requires "Display over other apps" permission.

Build
- Open this folder in Android Studio and build/run on a device (minSdk 26).
# blank-repo
for codespaces. 
