# Graph Report - coba-kotlin  (2026-08-16)

## Corpus Check
- 31 files · ~9,025 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 163 nodes · 284 edges · 21 communities (16 shown, 5 thin omitted)
- Extraction: 85% EXTRACTED · 15% INFERRED · 0% AMBIGUOUS · INFERRED: 42 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c6eaee4d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- PRD Inventaris Pakaian Thrift
- ThriftItem
- DashboardScreen.kt
- MainActivity.kt
- Android Launcher Icon (mdpi)
- Tabel thrift_items
- AndroidManifest.xml
- gradlew
- ExampleInstrumentedTest
- ExampleUnitTest
- Android Application (Manifest)
- App Launcher Icon (mipmap-xhdpi)
- App Launcher Icon (xxxhdpi)
- ThriftViewModel
- AGENTS.md
- ItemCategory

## God Nodes (most connected - your core abstractions)
1. `ThriftItem` - 27 edges
2. `ThriftViewModel` - 21 edges
3. `ItemCategory` - 20 edges
4. `ThriftItemStorage` - 17 edges
5. `PRD Inventaris Pakaian Thrift` - 17 edges
6. `ThriftItemRepository` - 14 edges
7. `DAO (Data Access Object)` - 12 edges
8. `Tabel thrift_items` - 9 edges
9. `ThriftInventoryScreen()` - 8 edges
10. `Spesifikasi Tambah Barang` - 8 edges

## Surprising Connections (you probably didn't know these)
- `Round Launcher Icon (legacy webp, xxxhdpi)` --semantically_similar_to--> `Adaptive Round Launcher Icon (v26+)`  [INFERRED] [semantically similar]
  app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp → app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
- `AndroidManifest.xml` --references--> `Adaptive Round Launcher Icon (v26+)`  [EXTRACTED]
  app/src/main/res/mipmap-mdpi/ic_launcher.webp → app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
- `ThriftInventoryScreen()` --calls--> `AddItemDialog()`  [INFERRED]
  app/src/main/java/com/mamay/cobain/presentation/ui/ThriftInventoryScreen.kt → app/src/main/java/com/mamay/cobain/presentation/ui/AddItemDialog.kt
- `MainScreen()` --calls--> `DashboardScreen()`  [INFERRED]
  app/src/main/java/com/mamay/cobain/presentation/ui/MainScreen.kt → app/src/main/java/com/mamay/cobain/presentation/ui/DashboardScreen.kt
- `ThriftInventoryScreen()` --calls--> `DeleteConfirmDialog()`  [INFERRED]
  app/src/main/java/com/mamay/cobain/presentation/ui/ThriftInventoryScreen.kt → app/src/main/java/com/mamay/cobain/presentation/ui/DeleteConfirmDialog.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fitur Inti Aplikasi (Fase 1-3)** — inventaris_thrift_prd_feature_daftar_barang, inventaris_thrift_prd_feature_tambah_barang, inventaris_thrift_prd_feature_ubah_barang, inventaris_thrift_prd_feature_hapus_barang, inventaris_thrift_prd_feature_data_tetap_ada [EXTRACTED 1.00]
- **Alur CRUD (Baca, Tambah, Ubah, Hapus)** — inventaris_thrift_prd_feature_daftar_barang, inventaris_thrift_prd_feature_tambah_barang, inventaris_thrift_prd_feature_ubah_barang, inventaris_thrift_prd_feature_hapus_barang, inventaris_thrift_prd_crud [EXTRACTED 1.00]
- **Round App Icon Resources** — app_src_main_androidmanifest, app_src_main_res_mipmap_xxxhdpi_ic_launcher_round, app_src_main_res_mipmap_anydpi_v26_ic_launcher_round [INFERRED 0.85]
- **Lapisan Persistensi Room/SQLite** — inventaris_thrift_prd_room_database, inventaris_thrift_prd_dao, inventaris_thrift_prd_sqlite, inventaris_thrift_prd_thrift_items_table, inventaris_thrift_prd_thriftitem [INFERRED 0.85]
- **ic_launcher Density Bucket Family** — app_src_main_res_mipmap_mdpi_ic_launcher, app_src_main_res_mipmap_hdpi_ic_launcher, app_src_main_res_mipmap_xhdpi_ic_launcher, app_src_main_res_mipmap_xxhdpi_ic_launcher, app_src_main_res_mipmap_xxxhdpi_ic_launcher [INFERRED 0.95]

## Communities (21 total, 5 thin omitted)

### Community 0 - "PRD Inventaris Pakaian Thrift"
Cohesion: 0.13
Nodes (32): Spesifikasi Daftar Barang, Sub-fitur Status Terjual, Spesifikasi Tambah Barang, Repository (layer data di atas DAO), Sub-fitur Simpan Item, Spesifikasi Ubah Barang, Sub-fitur Simpan Perubahan, Sub-fitur Tandai Terjual (+24 more)

### Community 1 - "ThriftItem"
Cohesion: 0.15
Nodes (7): Flow, ThriftItemStorage, ThriftItem, Flow, ThriftItemRepository, DeleteConfirmDialog(), ThriftItemCard()

### Community 2 - "DashboardScreen.kt"
Cohesion: 0.80
Nodes (4): DashboardScreen(), Modifier, StatCard(), ImageVector

### Community 3 - "MainActivity.kt"
Cohesion: 0.27
Nodes (7): MainActivity, ThriftViewModelFactory, CobainTheme(), Bundle, ComponentActivity, T, ViewModelProvider

### Community 4 - "Android Launcher Icon (mdpi)"
Cohesion: 0.22
Nodes (11): AndroidManifest.xml, Adaptive Icon ic_launcher (API 26+), Adaptive Round Launcher Icon (v26+), Android Launcher Icon (hdpi), Android Launcher Icon (mdpi), Android Launcher Icon Round (mdpi), Android Launcher Icon (xhdpi), Round Launcher Icon (ic_launcher_round) (+3 more)

### Community 5 - "Tabel thrift_items"
Cohesion: 0.36
Nodes (9): Sub-fitur Lihat Item, Sub-fitur Isi Data, Sub-fitur Edit Detail, Kolom buyPrice, Kolom id, Kolom name, Kolom sellPrice, Kolom size (+1 more)

### Community 6 - "AndroidManifest.xml"
Cohesion: 0.60
Nodes (4): App Branding Icon (visual identity for Inventaris Pakaian Thrift), Android Launcher Icon (ic_launcher.webp), Android Round Launcher Icon (ic_launcher_round.webp), App Launcher Icon (xxhdpi)

### Community 7 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 18 - "ThriftViewModel"
Cohesion: 0.13
Nodes (14): MainScreen(), MainTab, Dashboard, Inventaris, Pengaturan, AddCategoryRow(), CategoryRow(), Modifier (+6 more)

### Community 19 - "AGENTS.md"
Cohesion: 0.25
Nodes (6): Aturan Wajib (DO), Gambaran Proyek, Graphify (Knowledge Graph), Larangan (DON'T), Perintah Berguna, Struktur Kode

### Community 20 - "ItemCategory"
Cohesion: 0.23
Nodes (7): ItemCategory, AddItemDialog(), CategoryDropdown(), Modifier, CategoryDropdown(), EditItemDialog(), Modifier

## Knowledge Gaps
- **23 isolated node(s):** `Dashboard`, `Inventaris`, `Pengaturan`, `Gambaran Proyek`, `Struktur Kode` (+18 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ThriftViewModel` connect `ThriftViewModel` to `ThriftItem`, `DashboardScreen.kt`, `MainActivity.kt`, `ItemCategory`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **Why does `ThriftItem` connect `ThriftItem` to `ThriftViewModel`, `ItemCategory`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **Why does `ItemCategory` connect `ItemCategory` to `ThriftItem`, `ThriftViewModel`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **What connects `Dashboard`, `Inventaris`, `Pengaturan` to the rest of the system?**
  _23 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `PRD Inventaris Pakaian Thrift` be split into smaller, more focused modules?**
  _Cohesion score 0.1310483870967742 - nodes in this community are weakly interconnected._
- **Should `ThriftViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.13043478260869565 - nodes in this community are weakly interconnected._