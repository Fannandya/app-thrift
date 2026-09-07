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
- **Persistensi**: Room, `cobain.db`, sekarang **version 4** dengan migrasi
  `MIGRATION_1_2 .. MIGRATION_3_4` di `data/AppDatabase.kt`.
- **Navigasi**: 5 tab di `presentation/ui/MainScreen.kt` →
  `MainTab { Dashboard, Kasir, Riwayat, Inventaris, Pengaturan }`.
- **Logika murni** (ada unit test): `domain/CheckoutCalculator.kt`,
  `domain/ReceiptBuilder.kt`, `domain/SalesAnalytics.kt`.
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
- Master data di Pengaturan: Profil Toko, Kategori (`CategoryManagementScreen`),
  Ukuran (`SizeManagementScreen`).
- Kasir: keranjang multi-item, checkout dengan diskon + kembalian, kurangi stok.
- Struk: dibuat otomatis setelah checkout, bisa dibagikan sebagai teks, bisa
  dicetak ulang dari Riwayat.
- Riwayat Transaksi: daftar + detail per transaksi.
- Dashboard: total aset, hitungan stok, grafik penjualan harian (Vico).
- Cadangkan Data: ekspor `cobain.db` ke folder aplikasi.
- Migrasi otomatis data dari format lama saat pertama kali buka.
- Unit test: `CheckoutCalculator`, `ReceiptBuilder`, `SalesAnalytics`,
  `ThriftViewModel`. Instrumented test: `ThriftItemDaoTest`.

### Belum dikerjakan / ide terbuka
- **Restore** database dari berkas cadangan (saat ini hanya ekspor satu arah).
- **Share berkas** cadangan / struk sebagai file (perlu `FileProvider` di manifest;
  pola sekarang teks-only).
- Struk masih teks biasa — belum ada cetak ke printer thermal / PDF.
- Belum ada test UI (Compose) untuk layar-layar utama.
- Belum ada penanganan error terlihat kalau migrasi legacy gagal (hanya `Log.e`).
- Dashboard: rentang grafik masih tetap (harian) — belum bisa pilih
  mingguan/bulanan atau rentang tanggal.
- Belum ada laporan/ekspor penjualan (CSV/Excel) untuk pembukuan.
- Belum ada multi-user / autentikasi (kemungkinan memang di luar cakupan).
