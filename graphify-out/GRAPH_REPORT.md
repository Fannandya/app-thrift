# Graph Report - coba-kotlin  (2026-08-16)

## Corpus Check
- Corpus is ~7,399 words - fits in a single context window. You may not need a graph.

## Summary
- 135 nodes · 228 edges · 18 communities (13 shown, 5 thin omitted)
- Extraction: 82% EXTRACTED · 18% INFERRED · 0% AMBIGUOUS · INFERRED: 41 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Fitur & Spesifikasi Produk
- Data Layer & Persistence
- UI Screens & Navigasi
- Entry Point & ViewModel Wiring
- Launcher Icons & Manifest Resources
- Kolom Data Barang
- App Branding Icons
- Gradle Wrapper Scripts
- Instrumented Tests
- Unit Tests
- Manifest Icon Wiring
- Launcher Icon (xhdpi)
- Launcher Icon (xxxhdpi)

## God Nodes (most connected - your core abstractions)
1. `ThriftItem` - 27 edges
2. `PRD Inventaris Pakaian Thrift` - 17 edges
3. `ThriftViewModel` - 16 edges
4. `ThriftItemStorage` - 12 edges
5. `DAO (Data Access Object)` - 12 edges
6. `ThriftItemRepository` - 11 edges
7. `Tabel thrift_items` - 9 edges
8. `ThriftInventoryScreen()` - 8 edges
9. `Spesifikasi Tambah Barang` - 8 edges
10. `Room Database` - 7 edges

## Surprising Connections (you probably didn't know these)
- `Round Launcher Icon (legacy webp, xxxhdpi)` --semantically_similar_to--> `Adaptive Round Launcher Icon (v26+)`  [INFERRED] [semantically similar]
  app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp → app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
- `AndroidManifest.xml` --references--> `Adaptive Round Launcher Icon (v26+)`  [EXTRACTED]
  app/src/main/res/mipmap-mdpi/ic_launcher.webp → app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
- `ThriftInventoryScreen()` --calls--> `AddItemDialog()`  [INFERRED]
  app/src/main/java/com/mamay/cobain/presentation/ui/ThriftInventoryScreen.kt → app/src/main/java/com/mamay/cobain/presentation/ui/AddItemDialog.kt
- `ThriftInventoryScreen()` --calls--> `DeleteConfirmDialog()`  [INFERRED]
  app/src/main/java/com/mamay/cobain/presentation/ui/ThriftInventoryScreen.kt → app/src/main/java/com/mamay/cobain/presentation/ui/DeleteConfirmDialog.kt
- `ThriftInventoryScreen()` --calls--> `EditItemDialog()`  [INFERRED]
  app/src/main/java/com/mamay/cobain/presentation/ui/ThriftInventoryScreen.kt → app/src/main/java/com/mamay/cobain/presentation/ui/EditItemDialog.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Alur CRUD (Baca, Tambah, Ubah, Hapus)** — inventaris_thrift_prd_feature_daftar_barang, inventaris_thrift_prd_feature_tambah_barang, inventaris_thrift_prd_feature_ubah_barang, inventaris_thrift_prd_feature_hapus_barang, inventaris_thrift_prd_crud [EXTRACTED 1.00]
- **Lapisan Persistensi Room/SQLite** — inventaris_thrift_prd_room_database, inventaris_thrift_prd_dao, inventaris_thrift_prd_sqlite, inventaris_thrift_prd_thrift_items_table, inventaris_thrift_prd_thriftitem [INFERRED 0.85]
- **Fitur Inti Aplikasi (Fase 1-3)** — inventaris_thrift_prd_feature_daftar_barang, inventaris_thrift_prd_feature_tambah_barang, inventaris_thrift_prd_feature_ubah_barang, inventaris_thrift_prd_feature_hapus_barang, inventaris_thrift_prd_feature_data_tetap_ada [EXTRACTED 1.00]
- **ic_launcher Density Bucket Family** — app_src_main_res_mipmap_mdpi_ic_launcher, app_src_main_res_mipmap_hdpi_ic_launcher, app_src_main_res_mipmap_xhdpi_ic_launcher, app_src_main_res_mipmap_xxhdpi_ic_launcher, app_src_main_res_mipmap_xxxhdpi_ic_launcher [INFERRED 0.95]
- **Round App Icon Resources** — app_src_main_androidmanifest, app_src_main_res_mipmap_xxxhdpi_ic_launcher_round, app_src_main_res_mipmap_anydpi_v26_ic_launcher_round [INFERRED 0.85]

## Communities (18 total, 5 thin omitted)

### Community 0 - "Fitur & Spesifikasi Produk"
Cohesion: 0.13
Nodes (32): Spesifikasi Daftar Barang, Sub-fitur Status Terjual, Spesifikasi Tambah Barang, Repository (layer data di atas DAO), Sub-fitur Simpan Item, Spesifikasi Ubah Barang, Sub-fitur Simpan Perubahan, Sub-fitur Tandai Terjual (+24 more)

### Community 1 - "Data Layer & Persistence"
Cohesion: 0.16
Nodes (8): Flow, ThriftItemStorage, ThriftItem, Flow, ThriftItemRepository, DeleteConfirmDialog(), EditItemDialog(), ThriftItemCard()

### Community 2 - "UI Screens & Navigasi"
Cohesion: 0.13
Nodes (14): AddItemDialog(), DashboardScreen(), Modifier, StatCard(), MainScreen(), MainTab, Dashboard, Inventaris (+6 more)

### Community 3 - "Entry Point & ViewModel Wiring"
Cohesion: 0.27
Nodes (7): MainActivity, ThriftViewModelFactory, CobainTheme(), Bundle, ComponentActivity, T, ViewModelProvider

### Community 4 - "Launcher Icons & Manifest Resources"
Cohesion: 0.22
Nodes (11): AndroidManifest.xml, Adaptive Icon ic_launcher (API 26+), Adaptive Round Launcher Icon (v26+), Android Launcher Icon (hdpi), Android Launcher Icon (mdpi), Android Launcher Icon Round (mdpi), Android Launcher Icon (xhdpi), Round Launcher Icon (ic_launcher_round) (+3 more)

### Community 5 - "Kolom Data Barang"
Cohesion: 0.36
Nodes (9): Sub-fitur Lihat Item, Sub-fitur Isi Data, Sub-fitur Edit Detail, Kolom buyPrice, Kolom id, Kolom name, Kolom sellPrice, Kolom size (+1 more)

### Community 6 - "App Branding Icons"
Cohesion: 0.60
Nodes (4): App Branding Icon (visual identity for Inventaris Pakaian Thrift), Android Launcher Icon (ic_launcher.webp), Android Round Launcher Icon (ic_launcher_round.webp), App Launcher Icon (xxhdpi)

### Community 7 - "Gradle Wrapper Scripts"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **16 isolated node(s):** `Dashboard`, `Inventaris`, `Aplikasi Inventaris Pakaian Thrift`, `Kolom id`, `Sub-fitur Tombol Hapus` (+11 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ThriftItem` connect `Data Layer & Persistence` to `UI Screens & Navigasi`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Why does `ThriftViewModel` connect `UI Screens & Navigasi` to `Data Layer & Persistence`, `Entry Point & ViewModel Wiring`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Why does `PRD Inventaris Pakaian Thrift` connect `Fitur & Spesifikasi Produk` to `Kolom Data Barang`?**
  _High betweenness centrality (0.031) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `DAO (Data Access Object)` (e.g. with `Repository (layer data di atas DAO)` and `Sub-fitur Simpan Perubahan`) actually correct?**
  _`DAO (Data Access Object)` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Dashboard`, `Inventaris`, `Aplikasi Inventaris Pakaian Thrift` to the rest of the system?**
  _16 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Fitur & Spesifikasi Produk` be split into smaller, more focused modules?**
  _Cohesion score 0.1310483870967742 - nodes in this community are weakly interconnected._
- **Should `UI Screens & Navigasi` be split into smaller, more focused modules?**
  _Cohesion score 0.13438735177865613 - nodes in this community are weakly interconnected._