# AGENTS.md

Panduan untuk semua agent (AI maupun manusia) yang mengerjakan proyek ini.

## Gambaran Proyek

Aplikasi Android **Inventaris Pakaian Thrift** berbasis Jetpack Compose (Material3). Fitur: kelola stok barang thrift (tambah, ubah, hapus, tandai terjual), dashboard ringkasan penjualan, dan bottom navigation (Dashboard + Inventaris).

- Bahasa: Kotlin 2.0.20, AGP 9.3.1, Compose BOM 2026.02.01
- Semua teks UI dalam Bahasa Indonesia

## Struktur Kode

```
app/src/main/java/com/mamay/cobain/
├── MainActivity.kt              # Entry point, wiring ViewModel
├── data/
│   ├── entity/ThriftItem.kt     # Model data (kotlinx.serialization)
│   ├── dao/ThriftItemStorage.kt # Persistence: file JSON di filesDir
│   └── repository/ThriftItemRepository.kt
├── presentation/
│   ├── ui/                      # Composables (MainScreen, DashboardScreen, dll)
│   └── viewmodel/ThriftViewModel.kt
└── ui/theme/                    # CobainTheme, Color, Type
```

## Aturan Wajib (DO)

- Ikuti pola layer yang ada: UI hanya bicara ke `ThriftViewModel` → `ThriftItemRepository` → `ThriftItemStorage`. Jangan panggil storage langsung dari UI.
- Gunakan `ThriftViewModelFactory` untuk membuat ViewModel (tanpa framework DI). Tambahkan factory bila ViewModel baru butuh dependency.
- Persistence = `ThriftItemStorage` (file JSON `thrift_items.json` via kotlinx.serialization + `StateFlow`). JANGAN tambah Room/DataStore baru tanpa persetujuan.
- Model `ThriftItem` (id, name, size, buyPrice, sellPrice, isSold) wajib `@Serializable`; ubah field bersama-sama di entity, storage, dan UI.
- UI Compose + Material3. Layar utama = `MainScreen` dengan bottom bar (`NavigationBar`); tab baru tambahkan di enum `MainTab`.
- State UI reactive: `collectAsState` dari `StateFlow` viewModel; dialog pakai `remember { mutableStateOf(...) }`; state tab pakai `rememberSaveable`.
- Format uang: `"Rp$harga"` (tanpa separator ribuan), konsisten dengan kode existing.
- Ikon: pakai `material-icons-extended` (`Icons.Default.*`).
- Verifikasi setiap perubahan dengan: `./gradlew :app:compileDebugKotlin` — wajib sukses sebelum selesai.
- Ikuti style kode existing (indentasi 4 spasi, penamaan camelCase, parametrik modifier agar composable bisa dipad).

## Larangan (DON'T)

- JANGAN commit: `.gradle/`, `build/`, `app/build/`, `.kotlin/`, `.claude/`, `local.properties`, `.idea/workspace.xml`, `*.iml`, `.DS_Store`, `inventaris-thrift.zip` (sudah di `.gitignore`).
- JANGAN hapus/hindari `.gitignore` — selalu cek `git status` sebelum commit.
- JANGAN tambah komentar kode ("DO NOT ADD ANY COMMENTS") kecuali diminta.
- JANGAN ganti bahasa UI ke selain Bahasa Indonesia.
- JANGAN commit langsung ke `main` tanpa persetujuan; jangan push tanpa diminta.
- JANGAN tambah dependency baru tanpa cek dulu apakah sudah ada di `gradle/libs.versions.toml`.

## Graphify (Knowledge Graph)

Proyek ini punya knowledge graph di `graphify-out/` (graph.html, graph.json, GRAPH_REPORT.md). Pakai graph ini SEBELUM baca file proyek agar hemat token — jangan scan ulang seluruh proyek tiap task.

**Flow wajib sebelum eksekusi perintah user:**
1. Cek `graphify-out/graph.json` ada apa tidak.
2. Kalau ada, jawab pertanyaan tentang alur/struktur/hubungan fungsi dengan `graphify query "…"`, `graphify explain "<node>"`, atau `graphify path "<a>" "<b>"` — bukan dengan baca ulang file satu per satu.
3. Hanya baca file secara langsung (Read/Grep) untuk detail vertex yang sudah dipetakan graph, misal cek isi fungsi tertentu.
4. Kalau `graphify-out/` belum ada atau AST graph sudah basi (banyak file baru di luar commit), jalankan `graphify update` atau rebuild manual.

**Menjaga graph tetap segar:**
- Hook post-commit sudah terpasang — graph otomatis di-rebuild setiap `git commit`.
- Setelah menambah/mengubah banyak file tanpa commit, jalankan: `graphify update`.
- Lihat ringkasan topologi: `graphify-out/GRAPH_REPORT.md` (god nodes, surprising connections, suggested questions).

## Perintah Berguna

- Build/verifikasi: `./gradlew :app:compileDebugKotlin`
- Full build: `./gradlew assembleDebug`
- Cek status git: `git status` dan `git ls-files --others --exclude-standard`
- Query knowledge graph: `graphify query "bagaimana alur tambah barang?"`
