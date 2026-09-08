# Log Perubahan Agent

Catatan kronologis pekerjaan yang dilakukan oleh AI agent di repo ini. Tujuannya:
agent lain bisa cepat paham konteks tanpa membaca ulang seluruh git history.

- **Update file ini setiap selesai satu unit pekerjaan** (idealnya bersamaan dengan commit).
- Format entri: tanggal, ringkasan, file tersentuh, status build/test, sisa pekerjaan.
- Tanggal ditulis absolut (mis. `2026-09-08`), bukan "kemarin".

---

## Gambaran Aplikasi

Aplikasi Android (Kotlin + Jetpack Compose) untuk mengelola toko baju thrift:
inventaris, kasir/checkout, riwayat transaksi, dan dashboard penjualan.

- **Arsitektur**: `AppContainer` (DI manual, tanpa Hilt) → `RoomThriftItemRepository`
  → `ThriftViewModel` → layar Compose. Lihat komentar di `AppContainer.kt`.
- **Persistensi**: Room, `cobain.db`, sekarang **version 5** dengan migrasi
  `MIGRATION_1_2 .. MIGRATION_4_5` di `data/AppDatabase.kt`.
- **Navigasi**: 5 tab di `presentation/ui/MainScreen.kt` →
  `MainTab { Dashboard, Kasir, Riwayat, Inventaris, Pengaturan }`.
- **Logika murni** (ada unit test): `domain/CheckoutCalculator.kt`,
  `domain/ReceiptBuilder.kt`, `domain/SalesAnalytics.kt`, `domain/DiscountEngine.kt`,
  `domain/ExcelReportBuilder.kt`.
- **Migrasi data lama**: `data/legacy/LegacyDataMigrator` dijalankan sekali saat
  startup dari `CobainApplication.onCreate()`.

---

## Konvensi

- Bahasa commit & UI: **Indonesia**. Komentar kode: Indonesia, menjelaskan *kenapa*.
- Commit message diakhiri trailer `Co-Authored-By:` + `Claude-Session:`.
- File `.idea/*` yang ikut termodifikasi **tidak di-commit** (noise IDE).
- Setelah ubah kode, jalankan `graphify update .` (lihat `AGENTS.md`) untuk
  memperbarui knowledge graph di `graphify-out/`.
- Verifikasi sebelum klaim selesai:
  `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`.

---

## Riwayat Pekerjaan

### 2026-09-08 — Palet profesional (tanpa gradasi) + fix jank + tombol Reset Toko
- **Plan**: `~/.claude/plans/aku-menemukan-beberapa-bug-cheerful-bear.md`. Didahului
  audit performa (2 subagent) — akar jank: komputasi berat di body composable tanpa
  `remember` atas 3100 item tiap frame, Dashboard `verticalScroll` bukan `LazyColumn`,
  `collectAsState` bukan `...WithLifecycle`, grafik Vico diberi data tak stabil.
- **Warna** (`ui/theme/Color.kt`): palet ditata ulang jadi **netral-profesional** —
  indigo lebih lembut (`#3730A3`), surface abu netral (buang nuansa biru), pasangan
  container/on ke konvensi M3 (fill terang + teks gelap), teal & amber saturasi
  rendah hanya utk status. Dark diturunkan senada.
- **Gradasi dihapus**: `RevenueHeroCard` → `AppCard` warna `primary` solid.
- **Kartu datar** (`presentation/ui/components/AppCard.kt` baru): clip + background +
  border `outlineVariant` 1px, tanpa `Modifier.shadow`. Dipakai di Dashboard;
  `CashierScreen.CartSummaryBar` & `SettingsScreen.SettingOptionCard` ikut jadi
  border-flat. `TopAppBar` 7 layar: `primary` → `surface`/`onSurface` (tak lagi
  indigo pekat). Badge ikon dekoratif Dashboard → netral; teks nilai teal/amber →
  `onSurface`.
- **Performa (targeted)**:
  - `DashboardScreen`: semua derivasi → `computeDashboardData(...)` di dalam satu
    `remember(items, sales, transactions, range, threshold)`; body jadi `LazyColumn`
    (`item {}` per section) — kartu & grafik off-screen tak compose.
  - `ThriftInventoryScreen` & `CashierScreen`: `filteredItems`, opsi filter
    per-atribut, `availableItems` di-`remember`; per baris pakai `valuesByItem` +
    `activeByItem` (map dibangun sekali) alih-alih scan list penuh.
  - `domain/DiscountEngine.kt`: `indexActiveDiscounts(discounts, links, now)` —
    peta `itemId -> diskon aktif` satu lintasan (+ test). `ItemDisplay.kt`: varian
    `attributesTextFrom` / `displayDiscountFrom` berbasis map.
  - `collectAsState()` → `collectAsStateWithLifecycle()` di ~13 layar
    (+ dep `androidx.lifecycle:lifecycle-runtime-compose`).
  - `remember` kecil di `DiscountManagementScreen` / `ExportDialog`; loading min
    `MainScreen` 800→400ms.
- **Reset Toko** (`AppContainer.resetStore()` → `database.clearAllTables()`):
  kartu merah "Reset Toko" paling bawah di Pengaturan → dialog **wajib ketik
  "RESET"** → hapus semua tabel (termasuk `store_profile`) → Room memancarkan ulang
  kosong → `ThriftViewModel.notifyStoreReset()` popup + `onResetDone()` pindah ke
  tab Dashboard. `MainScreen` teruskan `onResetDone`.
- **File**: `ui/theme/Color.kt`, `presentation/ui/components/AppCard.kt` (baru),
  `presentation/ui/DashboardScreen.kt` (tulis ulang), `ThriftInventoryScreen.kt`,
  `CashierScreen.kt`, `ItemDisplay.kt`, `MainScreen.kt`, `SettingsScreen.kt`,
  `DiscountManagementScreen.kt`, `ExportDialog.kt`, 7 layar `TopAppBar`,
  `domain/DiscountEngine.kt` (+ test), `AppContainer.kt`, `ThriftViewModel.kt`
  (+ test), `gradle/libs.versions.toml`, `app/build.gradle.kts`.
- **Build/Test**: `compileDebugKotlin`, `assembleDebug`, `assembleRelease` (R8)
  hijau. `testDebugUnitTest` **67 hijau** (+`indexActiveDiscounts` ×2,
  `notifyStoreReset`). Terpasang di HP Xiaomi (debug) — palet profesional & kartu
  datar tanpa gradasi terverifikasi via screenshot.
- **Belum diverifikasi**: kemulusan scroll/pindah-tab & alur Reset Toko tak bisa
  diotomasi (`adb shell input` diblok HyperOS; `installRelease` juga
  `INSTALL_FAILED_USER_RESTRICTED`). **Ukur di build RELEASE** — sebagian besar
  "berat" dari runtime debug. `Card` biasa di layar Atribut/Diskon/Riwayat masih
  elevasi tonal 1dp bawaan M3 (bukan drop shadow) — belum diubah ke border-flat.

### 2026-09-08 — Redesign Dashboard (mockup Stitch) + palet warna baru app-wide
- **Plan**: `~/.claude/plans/aku-menemukan-beberapa-bug-cheerful-bear.md`. Sumber:
  export HTML/Tailwind dari Google Stitch project "Compose POS Dashboard Screen".
- **Palet warna app-wide** (`ui/theme/Color.kt`): sumber warna ganti dari teal ke
  **indigo** (primary `#2A14B4`, dari Stitch). Light = persis Stitch; **Dark =
  turunan** (primary lavender `#C3C0FF`, surface biru-gelap, teal jadi secondary).
  `Theme.kt` tak berubah (sudah membaca Light/DarkColorScheme).
- **Tipografi** (`ui/theme/Type.kt`): isi peran `Typography` yang kosong dengan
  skala/berat ala mockup, tetap `FontFamily.Default` (tanpa resource font baru).
- **`DashboardScreen.kt` ditulis ulang** mengikuti urutan mockup: header card
  (nama toko + badge verified + pill "Live" + strip "Diperbarui otomatis"),
  chip rentang (terpilih terisi primary + ikon check), "Status Inventaris" (3
  mini-card badge ikon), "Kinerja Keuangan" (kartu hero gradien + 2 kartu
  aset/potensi), "Metrik Operasional Kasir" (kartu 2×2: Laba Kotor + margin %,
  Jumlah Transaksi, Rata-rata Keranjang, Diskon Diberikan), kartu grafik Vico +
  baris callout "Puncak", "Barang Terlaris" (ikon placeholder, harga satuan, rank),
  "Peringatan Stok Menipis" (titik status + kategori + pill sisa), "Riwayat
  Transaksi Kasir" (ID mono + waktu + pill Selesai + tombol "Lihat Semua"),
  banner "Kasir Siap Transaksi" + tombol "Buka POS".
  - Metrik turunan baru: `%` vs periode sebelumnya (hanya 7/30 hari), margin %,
    harga satuan Top Selling, puncak grafik.
  - **Di-drop** (tak ada data): foto produk (pakai ikon), metode bayar, tombol
    "Order Baru", "Pilih Tanggal".
  - `DashboardScreen` sekarang menerima `onOpenCashier` / `onOpenHistory`.
- **`domain/SalesAnalytics.kt`**: `revenueDelta(current, previous)` (+ test).
- **`MainScreen.kt`**: teruskan callback pindah-tab ke Dashboard; warna
  `NavigationBarItem` terpilih pakai peran primary (pill lavender) agar seragam
  dengan palet baru.
- **File**: `ui/theme/Color.kt`, `ui/theme/Type.kt`,
  `presentation/ui/DashboardScreen.kt` (tulis ulang), `presentation/ui/MainScreen.kt`,
  `domain/SalesAnalytics.kt`, `domain/SalesAnalyticsTest.kt`.
- **Build/Test**: `compileDebugKotlin`, `assembleDebug`, `assembleRelease` (R8)
  hijau. `testDebugUnitTest` **64 hijau** (+`revenueDelta`). Terpasang di HP Xiaomi;
  bagian atas Dashboard + palet indigo di dark mode terverifikasi via screenshot.
- **Belum diverifikasi**: section bawah Dashboard (grafik, Top Selling, Stok
  Menipis, Riwayat, banner) & tombol "Buka POS"/"Lihat Semua" — `adb shell input`
  diblok HyperOS jadi tak bisa scroll/tap dari sini. Perlu cek manual dgn tangan.
- **Belum**: struktur bottom-nav (mockup punya tab "Laporan") tidak diubah; tak
  ada TopAppBar/lonceng/avatar; tak ada field foto item / metode bayar.

### 2026-09-08 — Popup konfirmasi sukses + loading screen
- **Plan**: `~/.claude/plans/aku-menemukan-beberapa-bug-cheerful-bear.md` (disetujui).
- **Popup "berhasil"** (modal AlertDialog, tombol OK) untuk operasi **hapus + transaksi**:
  - `ThriftViewModel`: kanal `_successMessage`/`successMessage`/`consumeSuccessMessage()`
    meniru pola `_errorMessage`. Diisi di `.onSuccess {}` tiap `deleteItem` /
    `deleteCategory` / `deleteAttribute` / `deleteAttributeOption` / `deleteDiscount`,
    dan setelah `checkout` sukses (`"Transaksi Rp… berhasil disimpan"`). Metode baru
    `deleteDiscounts(list)` untuk "Bersihkan diskon kadaluarsa" → satu popup, bukan N.
  - `components/MessageDialog.kt` (baru) — sibling `ConfirmDialog` tanpa tombol batal.
  - `MainScreen`: titik tunggal — `successMessage` non-null → `MessageDialog` "Berhasil".
  - `CashierScreen`: `ReceiptDialog` ditahan `if (successMessage == null)` supaya kotak
    "Transaksi berhasil" muncul lebih dulu, struk setelah OK.
  - `DiscountManagementScreen`: cleanup pakai `viewModel.deleteDiscounts(...)`.
- **Loading screen** (overlay Compose, setiap cold start):
  - `ThriftViewModel.isInitializing: StateFlow<Boolean>` = `combine(items, categories,
    transactions, storeProfile) { false }` dgn nilai awal `true`; jadi `false` begitu
    keempat Flow Room memancarkan sekali. Di-*retain* dgn ViewModel → warm start tidak
    memunculkan loading.
  - `LoadingScreen.kt` (baru): layar penuh, ikon Storefront + `app_name` + spinner +
    "Memuat…", menelan sentuhan.
  - `MainScreen`: `Box` membungkus `Scaffold`; overlay tampil saat `isInitializing ||
    !minTimePassed` (min 800 ms via `delay`).
- **File**: `presentation/viewmodel/ThriftViewModel.kt`, `presentation/ui/MainScreen.kt`,
  `CashierScreen.kt`, `DiscountManagementScreen.kt`, `components/MessageDialog.kt` (baru),
  `LoadingScreen.kt` (baru), `ThriftViewModelTest.kt` (+5 test).
- **Build/Test**: `compileDebugKotlin`, `assembleDebug`, `assembleRelease` (R8) hijau.
  `testDebugUnitTest` **63 test hijau**. Terpasang & dijalankan di **HP Xiaomi asli
  (2312FPCA6G, Android 14) via USB** — loading screen tampil benar saat cold start lalu
  Dashboard (data hasil migrasi v5 utuh: 3100 item, transaksi ada).
- **Catatan**: `adb shell input` diblok HyperOS di HP ini (butuh toggle "USB debugging
  (Security settings)"), jadi alur tap popup hapus/checkout tidak bisa diotomasi dari
  sini — sudah dicakup unit test + review; silakan cek manual dgn tangan.

### 2026-09-08 — Generalisasi aplikasi: atribut kustom, istilah toko, ekspor Excel, diskon per barang, Dashboard, fix crash
- **Plan**: `~/.claude/plans/aku-menemukan-beberapa-bug-cheerful-bear.md` (disetujui).
- **Room `cobain.db` v4 → v5**, satu migrasi `MIGRATION_4_5` di `data/AppDatabase.kt`,
  didaftarkan di `AppContainer`. Skema terekspor `app/schemas/.../5.json` di-commit.
  Migrasi mencakup semua fitur di bawah; `MigrationTest.kt` baru memvalidasi.
- **Fitur A — Atribut barang kustom** (ganti konsep "Ukuran" yang hardcoded):
  entity baru `ItemAttribute` / `ItemAttributeOption` / `ItemAttributeValue` /
  `AttributeMode`. "Kategori" tetap kolom kelas satu di `ThriftItem`. Tabel `sizes`
  + `items.sizeId` dihapus di migrasi; data ukuran lama otomatis pindah jadi
  atribut bawaan "Ukuran" (mode LIST). Layar `SizeManagementScreen` diganti
  `AttributeManagementScreen`. Form Tambah/Edit barang merender input dinamis per
  atribut (`components/AttributeInputs.kt`, `components/StringDropdown.kt`). Atribut
  tampil di kartu/detail barang, filter Inventaris, struk (`ReceiptBuilder`),
  detail transaksi. Snapshot teks disimpan di `ThriftSale.attributesSummary`
  (kolom `size` dipertahankan sebagai legacy).
- **Fitur B — Istilah barang**: `StoreProfile.itemTerm` (default "Barang") +
  `lowStockThreshold`. Diisi di `StoreProfileScreen`. Semua label "Pakaian"/
  "pakaian" di UI kini memakai istilah ini.
- **Fitur C — Ekspor Excel (.xlsx)**: library `org.dhatim:fastexcel` +
  core library desugaring + rule ProGuard. `domain/ExcelReportBuilder.kt` (pure,
  ada test). `presentation/ui/ExportDialog.kt`: admin pilih sheet (Inventaris /
  Penjualan per item / Rekap transaksi / Ringkasan) + rentang tanggal. Ditulis
  lewat SAF `ACTION_CREATE_DOCUMENT` dari `SettingsScreen` (tanpa FileProvider,
  tanpa izin). `ThriftViewModel.writeExcelReport()`.
- **Fitur D — Dashboard**: metrik baru di `domain/SalesAnalytics.kt`
  (`grossProfit`, `transactionStats`, `topSellingItems`, `lowStock`,
  `itemDiscountGiven`, `costOfGoodsSold`). `DashboardScreen` menambah kartu Laba
  Kotor, Jumlah + Rata-rata Transaksi, Total Diskon Diberikan, daftar Barang
  Terlaris & Stok Menipis. `ThriftSale.buyPrice` (snapshot modal, di-backfill).
- **Fitur E — Fix force close "7 Hari"**: `components/SalesChart.kt` sekarang
  `key(data.size)` + `remember(data.size)` untuk `CartesianChartModelProducer` &
  scroll state; `initialScroll` jadi `Start` saat bucket ≤ 7.
  Test instrumentasi baru `SalesChartRangeToggleTest.kt`.
- **Fitur F — Diskon per barang**: entity `Discount` / `DiscountItem`,
  `ThriftItem.defaultDiscountId`. `domain/DiscountEngine.kt` (pure, ada test):
  persen 1–100, `applyPercent` bulat ke rupiah terdekat, jendela `start ≤ now <
  end`. `DiscountManagementScreen` + `DiscountEditorDialog`: buat/edit diskon
  berjangka waktu, tautkan ke barang (checklist), status Aktif/Terjadwal/
  Kadaluarsa, bersih-bersih diskon kadaluarsa. Kasir menampilkan harga coret +
  chip `-N%`; kalau >1 diskon aktif tanpa default, kasir wajib memilih
  (`CartLine.overrideDiscountId`). `checkout()` memotong per baris dulu lalu
  diskon transaksi dari subtotal terdiskon; menyimpan `originalSellPrice` +
  `discountPercent` di `ThriftSale`. Sheet Excel & Dashboard ikut melaporkan.
- **File tersentuh (utama)**: `data/AppDatabase.kt`, `data/dao/ThriftItemDao.kt`,
  `data/entity/*` (6 baru, `ThriftItem`/`ThriftSale`/`StoreProfile` diubah,
  `ItemSize` dihapus), `data/repository/*` + `FakeThriftItemRepository`,
  `data/legacy/LegacyDataMigrator.kt`, `presentation/viewmodel/ThriftViewModel.kt`,
  `domain/{SalesAnalytics,ReceiptBuilder,DiscountEngine,ExcelReportBuilder}.kt`,
  `presentation/ui/*` (~14 layar + 4 komponen baru), `gradle/libs.versions.toml`,
  `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/schemas/.../5.json`.
- **Build/Test**: `compileDebugKotlin`, `assembleDebug`, `assembleRelease` (R8 +
  desugar) hijau. `testDebugUnitTest` hijau (58 test, termasuk `DiscountEngineTest`
  & `ExcelReportBuilderTest` baru). Instrumented di emulator Pixel_6a:
  `MigrationTest` (2/2) & `ThriftItemDaoTest` (8/8, termasuk cascade atribut &
  diskon) **hijau** — migrasi v4→v5 tervalidasi, `foreign_key_check` bersih.
- **Belum diverifikasi**: `SalesChartRangeToggleTest` tidak bisa jalan di emulator
  API 36 (bug `espresso-core 3.5.1` × `InputManager.getInstance`, bukan kode kita —
  lihat KDoc test; jalankan di emulator API ≤ 35). Uji manual di HP belum
  dilakukan. Versi `fastexcel` di-pin `0.18.4` (resolusi Maven Central berhasil
  saat build). Resolusi konflik diskon di
  `DiscountEditorDialog` masih sederhana (belum ada sub-dialog "pilih default"
  saat menautkan barang yang sudah punya diskon beririsan — `discountConflicts()`
  sudah tersedia di ViewModel untuk itu).

### 2026-09-08 — Tombol "Cadangkan Data" di Pengaturan
- **Commit**: `a3f11d8 feat: tombol Cadangkan Data di Pengaturan`
- **Perubahan**:
  - `presentation/ui/SettingsScreen.kt`: menu baru "Cadangkan Data". Memanggil
    `AppContainer.exportDatabase()` di coroutine, hasil (path berkas / pesan gagal)
    ditampilkan lewat `SnackbarHostState`. `SettingsMenuScreen` kini mengambil
    `container` dari `LocalContext` (`CobainApplication`), bukan lewat ViewModel —
    ekspor dianggap urusan infrastruktur berkas.
  - `presentation/ui/MainScreen.kt`: meneruskan `snackbarHostState` ke `SettingsScreen`.
  - Import di `SettingsScreen.kt` dirapikan urutannya.
- **Catatan**: `AppContainer.exportDatabase()` sudah ada di HEAD sebelumnya
  (checkpoint WAL `TRUNCATE` lalu salin `cobain.db` ke
  `getExternalFilesDir/database_exports/cobain-backup-<timestamp>.db`).
- **Build/Test**: `compileDebugKotlin` + `testDebugUnitTest` hijau.
- **Belum**: tidak ada tombol *share* berkas cadangan (butuh `FileProvider`, yang
  selama ini sengaja dihindari — lihat `util/ShareText.kt`). Tidak ada fitur
  *restore* dari berkas cadangan. Cadangan hanya bisa diambil manual lewat
  file manager di `Android/data/com.mamay.cobain/files/database_exports/`.

### Sebelumnya (dari git history, ringkas)
- `bfbb4ff` — Grafik penjualan harian di Dashboard pakai library Vico
  (`presentation/ui/components/SalesChart.kt`, `domain/SalesAnalytics.kt`).
- `247c0ce` — Pencarian barang di Kasir & Inventaris
  (`presentation/ui/components/SearchField.kt`).
- `3a09412` — Tab Riwayat Transaksi + detail + cetak ulang struk
  (`TransactionHistoryScreen.kt`, `TransactionDetailScreen.kt`).
- `6f6ef62` — Struk otomatis setelah checkout + tombol bagikan (`ShareText.kt`,
  `components/ReceiptDialog.kt`, `domain/ReceiptBuilder.kt`).
- `e674f6b` — Checkout dengan diskon, uang dibayar, kembalian
  (`domain/CheckoutCalculator.kt`).
- `4bb1fef` — Header transaksi penjualan + kalkulator checkout, Room v4
  (`data/entity/SaleTransaction.kt`).
- `4b06b6a` / `f42e071` — Layar Profil Toko + judul dinamis, Room v3
  (`StoreProfileScreen.kt`, `data/entity/StoreProfile.kt`).
- `3d51ad6` — Migrasi penyimpanan ke Room (dari storage lama).

---

## Status Fitur

### Sudah ada / jalan
- Inventaris: daftar barang, tambah/edit (`AddItemDialog`, `EditItemDialog`),
  detail (`ItemDetailScreen`), filter kategori/status, pencarian.
- Master data di Pengaturan: Profil Toko (+ istilah barang, ambang stok),
  Kategori (`CategoryManagementScreen`), Atribut kustom
  (`AttributeManagementScreen`), Diskon per barang (`DiscountManagementScreen`).
- Kasir: keranjang multi-item, diskon per barang otomatis (harga coret) +
  diskon transaksi + kembalian, kurangi stok.
- Ekspor Excel (.xlsx) inventaris & laporan penjualan lewat dialog "Simpan ke..."
  (`ExportDialog`, `domain/ExcelReportBuilder.kt`).
- Struk: dibuat otomatis setelah checkout, bisa dibagikan sebagai teks, bisa
  dicetak ulang dari Riwayat.
- Riwayat Transaksi: daftar + detail per transaksi.
- Dashboard: total aset, hitungan stok, laba kotor, jumlah + rata-rata transaksi,
  total diskon, barang terlaris, stok menipis, grafik penjualan harian (Vico),
  filter rentang 7/30 hari (tidak lagi force close).
- Cadangkan Data: ekspor `cobain.db` ke folder aplikasi.
- Migrasi otomatis data dari format lama saat pertama kali buka.
- Loading screen ("Memuat…") saat cold start sampai data Room siap (`LoadingScreen`,
  `ThriftViewModel.isInitializing`).
- Popup modal "Berhasil" untuk operasi hapus & checkout (`components/MessageDialog`,
  `ThriftViewModel.successMessage` → ditampilkan `MainScreen`).
- Unit test: `CheckoutCalculator`, `ReceiptBuilder`, `SalesAnalytics`,
  `DiscountEngine`, `ExcelReportBuilder`, `ThriftViewModel`. Instrumented test:
  `ThriftItemDaoTest`, `MigrationTest`, `SalesChartRangeToggleTest`.

### Belum dikerjakan / ide terbuka
- **Restore** database dari berkas cadangan (saat ini hanya ekspor satu arah).
- **Share berkas** cadangan / struk sebagai file (perlu `FileProvider` di manifest;
  pola sekarang teks-only).
- Struk masih teks biasa — belum ada cetak ke printer thermal / PDF.
- Belum ada penanganan error terlihat kalau migrasi legacy gagal (hanya `Log.e`).
- Belum ada multi-user / autentikasi (kemungkinan memang di luar cakupan).
- Instrumented test (`MigrationTest`, dll) belum dijalankan di emulator/perangkat.
- `DiscountEditorDialog` belum punya sub-dialog "pilih diskon default" saat
  menautkan barang yang sudah punya diskon beririsan (helper
  `ThriftViewModel.discountConflicts()` sudah ada, tinggal dipakai di UI).
