# Graph Report - coba-kotlin  (2026-08-16)

## Corpus Check
- 33 files · ~9,910 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 176 nodes · 316 edges · 20 communities (15 shown, 5 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 43 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0c23b821`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- PRD Inventaris Pakaian Thrift
- ThriftItem
- MainTab
- MainActivity.kt
- Android Launcher Icon (mdpi)
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
1. `ThriftItem` - 31 edges
2. `ThriftViewModel` - 25 edges
3. `ThriftItemStorage` - 21 edges
4. `ItemCategory` - 20 edges
5. `PRD Inventaris Pakaian Thrift` - 17 edges
6. `ThriftItemRepository` - 16 edges
7. `DAO (Data Access Object)` - 12 edges
8. `ThriftSale` - 11 edges
9. `Tabel thrift_items` - 9 edges
10. `MainScreen()` - 8 edges

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
- **Fitur Inti Aplikasi (Fase 1-3)** — inventaris_thrift_prd_feature_daftar_barang, inventaris_thrift_prd_feature_tambah_barang, inventaris_thrift_prd_feature_ubah_barang, inventaris_thrift_prd_feature_hapus_barang, inventaris_thrift_prd_feature_data_tetap_ada [EXTRACTED 1.00]
- **Alur CRUD (Baca, Tambah, Ubah, Hapus)** — inventaris_thrift_prd_feature_daftar_barang, inventaris_thrift_prd_feature_tambah_barang, inventaris_thrift_prd_feature_ubah_barang, inventaris_thrift_prd_feature_hapus_barang, inventaris_thrift_prd_crud [EXTRACTED 1.00]
- **Round App Icon Resources** — app_src_main_androidmanifest, app_src_main_res_mipmap_xxxhdpi_ic_launcher_round, app_src_main_res_mipmap_anydpi_v26_ic_launcher_round [INFERRED 0.85]
- **Lapisan Persistensi Room/SQLite** — inventaris_thrift_prd_room_database, inventaris_thrift_prd_dao, inventaris_thrift_prd_sqlite, inventaris_thrift_prd_thrift_items_table, inventaris_thrift_prd_thriftitem [INFERRED 0.85]
- **ic_launcher Density Bucket Family** — app_src_main_res_mipmap_mdpi_ic_launcher, app_src_main_res_mipmap_hdpi_ic_launcher, app_src_main_res_mipmap_xhdpi_ic_launcher, app_src_main_res_mipmap_xxhdpi_ic_launcher, app_src_main_res_mipmap_xxxhdpi_ic_launcher [INFERRED 0.95]

## Communities (20 total, 5 thin omitted)

### Community 0 - "PRD Inventaris Pakaian Thrift"
Cohesion: 0.10
Nodes (41): Spesifikasi Daftar Barang, Sub-fitur Lihat Item, Sub-fitur Status Terjual, Spesifikasi Tambah Barang, Sub-fitur Isi Data, Repository (layer data di atas DAO), Sub-fitur Simpan Item, Spesifikasi Ubah Barang (+33 more)

### Community 1 - "ThriftItem"
Cohesion: 0.12
Nodes (8): Flow, ThriftItemStorage, ThriftItem, ThriftSale, Flow, ThriftItemRepository, DeleteConfirmDialog(), ThriftItemCard()

### Community 2 - "MainTab"
Cohesion: 0.40
Nodes (5): MainTab, Dashboard, Inventaris, Kasir, Pengaturan

### Community 3 - "MainActivity.kt"
Cohesion: 0.27
Nodes (7): MainActivity, ThriftViewModelFactory, CobainTheme(), Bundle, ComponentActivity, T, ViewModelProvider

### Community 4 - "Android Launcher Icon (mdpi)"
Cohesion: 0.22
Nodes (11): AndroidManifest.xml, Adaptive Icon ic_launcher (API 26+), Adaptive Round Launcher Icon (v26+), Android Launcher Icon (hdpi), Android Launcher Icon (mdpi), Android Launcher Icon Round (mdpi), Android Launcher Icon (xhdpi), Round Launcher Icon (ic_launcher_round) (+3 more)

### Community 6 - "AndroidManifest.xml"
Cohesion: 0.60
Nodes (4): App Branding Icon (visual identity for Inventaris Pakaian Thrift), Android Launcher Icon (ic_launcher.webp), Android Round Launcher Icon (ic_launcher_round.webp), App Launcher Icon (xxhdpi)

### Community 7 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 18 - "ThriftViewModel"
Cohesion: 0.14
Nodes (14): CashierItemCard(), CashierScreen(), Modifier, SaleDialog(), DashboardScreen(), Modifier, StatCard(), MainScreen() (+6 more)

### Community 19 - "AGENTS.md"
Cohesion: 0.25
Nodes (6): Aturan Wajib (DO), Gambaran Proyek, Graphify (Knowledge Graph), Larangan (DON'T), Perintah Berguna, Struktur Kode

### Community 20 - "ItemCategory"
Cohesion: 0.19
Nodes (11): ItemCategory, AddItemDialog(), CategoryDropdown(), Modifier, CategoryDropdown(), EditItemDialog(), Modifier, AddCategoryRow() (+3 more)

## Knowledge Gaps
- **24 isolated node(s):** `Dashboard`, `Kasir`, `Inventaris`, `Pengaturan`, `Gambaran Proyek` (+19 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ThriftViewModel` connect `ThriftViewModel` to `ThriftItem`, `MainActivity.kt`, `ItemCategory`?**
  _High betweenness centrality (0.107) - this node is a cross-community bridge._
- **Why does `ThriftItem` connect `ThriftItem` to `ThriftViewModel`, `ItemCategory`?**
  _High betweenness centrality (0.078) - this node is a cross-community bridge._
- **Why does `ItemCategory` connect `ItemCategory` to `ThriftItem`, `ThriftViewModel`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **What connects `Dashboard`, `Kasir`, `Inventaris` to the rest of the system?**
  _24 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `PRD Inventaris Pakaian Thrift` be split into smaller, more focused modules?**
  _Cohesion score 0.10365853658536585 - nodes in this community are weakly interconnected._
- **Should `ThriftItem` be split into smaller, more focused modules?**
  _Cohesion score 0.12258064516129032 - nodes in this community are weakly interconnected._
- **Should `ThriftViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.13666666666666666 - nodes in this community are weakly interconnected._