# Dallas College Bookstore — Installation Guide

## What This Is
A desktop application for the Dallas College Bookstore. It runs a small web server on your computer and you interact with it through your web browser.

## Requirements
- Windows 10 or 11
- Port 8080 must be free (no other app using it)
- ~200 MB of disk space
- No Java installation needed — Java is bundled inside the installer

## Installation

1. Download `DallasCollegeBookstore-1.2.exe` from the Assets section below.
2. Double-click the file to start the installer.
3. If Windows shows a "Windows protected your PC" warning (SmartScreen), click "More info" → "Run anyway".
4. Click Next on the welcome screen.
5. Choose an install location (default is fine) → Next.
6. Click Install and wait about 10 seconds.
7. Click Finish.

## Running the App

1. Open the Start Menu.
2. Search for and click "Dallas College Bookstore".
3. A black console window will open — this is the app's server. It will show:
   - Loaded 25 items from CSV.
   - Server running at http://127.0.0.1:8080
4. Your web browser will open automatically to the bookstore homepage.

If the browser doesn't open automatically, manually open it and go to: `http://127.0.0.1:8080/`

## Using the App
- Browse products — the homepage shows all 25 items in the catalog.
- Filter by category — click BOOK, DEVICE, TOOL, etc. to filter.
- Search — type in the search box at the top.
- Add to cart — click "Add to Cart" on any product card.
- View cart — click "Cart" in the navigation bar.
- Manager dashboard — click "Manager" and log in with username `team government` and password `manager123`.

## Stopping the App
Close the black console window to stop the server. To use the app again, just launch it from the Start Menu.

## Uninstalling
Settings → Apps → Installed apps → Dallas College Bookstore → Uninstall.

## Troubleshooting

### "This site can't be reached"
- Make sure the black console window is still open — closing it stops the server.
- Try `http://127.0.0.1:8080/` instead of `http://localhost:8080/`.

### The app closes immediately
Another program is using port 8080. Open Command Prompt and run `netstat -ano | findstr :8080`. If it shows a PID, force-close with `taskkill /F /PID <that-number>`.

### Browser shows `[]` (empty array)
The `items.csv` file may be missing from the install folder. Reinstall the app.

### Windows Firewall asks about the app
Allow it for Private networks. The app needs to accept local connections from your browser.

### "Windows protected your PC" (SmartScreen warning)
Click "More info" → "Run anyway". This warning appears because the installer isn't code-signed. Expected for a class project.

## Advanced: Command-Line Access

Files live at `C:\Program Files\DallasCollegeBookstore\`: