# Freelance Books 🇿🇦

A QuickBooks-style invoicing, quoting, and client management app designed specifically for South African freelancers.

---

## 📲 How to Download the APK from GitHub

Once this repository is on GitHub, you can download and install the APK on your phone in two easy ways:

### Option 1: Download from GitHub Releases (Recommended)
1. Go to your repository on GitHub.
2. In the right-hand sidebar, click **"Releases"** (or click the **Tags** section).
3. Under **Assets**, click **`FreelanceBooks-v1.0.apk`** to download it directly to your phone or computer.
4. Open the downloaded file on your Android device and tap **Install**.

### Option 2: Download from GitHub Actions
1. Click the **"Actions"** tab at the top of your GitHub repository.
2. Click the latest workflow run named **"Build and Package Android APK"**.
3. Scroll down to the **Artifacts** section at the bottom of the page.
4. Click **`FreelanceBooks-APK`** to download the ZIP file containing your ready-to-install `.apk`.
5. You can also manually trigger a new build anytime by clicking **Actions** > **Build and Package Android APK** > **Run workflow**.

---

## ✨ Key Freelancer Features

* **South African Rand (`R`) Native**: All amounts, line items, and totals are formatted in `R` (e.g. `R 4,500.00`).
* **Daily Rates**: Line items track **Days** and **Daily Rates** (no hourly tracking).
* **Zero Tax Complexity**: Tailored for freelancers (no company VAT/tax overhead added).
* **Client Directory & Auto-Fill**: Save clients with name, company, email, phone, and billing address. Selecting a client automatically fills all details on invoices and quotes.
* **Job Invites**: Enter your job invite / RFP reference number and choose between creating a preliminary Price Quote or an immediate Invoice.
* **1-Tap Quote to Invoice with PO Number**: Store quotes in the Quotes section. When ready, tap "Generate Invoice", enter the client's PO number, and it auto-increments the invoice number while copying all client info and daily rate items.
* **Vector A4 PDF Generation & Direct Email**: Export beautiful vector invoices and quotes, complete with South African EFT banking details (Bank, Account #, Branch Code, Ref), and email directly to clients with attached PDFs.
* **Auto-Incrementing Invoices**: Tracks sequences (e.g. `INV-1001`, `INV-1002`) and status (`Draft`, `Sent`, `Paid`, `Overdue`).

---

## 🛠 Building Locally with Gradle

To build the APK locally on your computer:

```bash
# Clone the repository
git clone <your-repo-url>
cd <repo-folder>

# Build the Debug APK
./gradlew assembleDebug

# The APK will be generated at:
# app/build/outputs/apk/debug/app-debug.apk
```
