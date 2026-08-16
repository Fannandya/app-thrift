# Daftar Barang

Menampilkan semua pakaian thrift dalam satu layar utama.

## Spesifikasi

### Tujuan
Menampilkan semua pakaian thrift dalam satu layar utama agar pengguna dapat melihat seluruh isi inventaris sekaligus.
### Selesai bila
- Layar utama menampilkan semua item pakaian yang tersimpan dalam bentuk daftar kartu yang bisa digulir.
- Setiap kartu menampilkan informasi utama: nama, ukuran, harga, dan status.
- Daftar langsung terbarui setelah ada penambahan, pengubahan, atau penghapusan item.

## Sub-fitur: Lihat item

Menampilkan nama, ukuran, harga, dan status pakaian.

### Tujuan
Menampilkan nama, ukuran, harga, dan status pada setiap kartu item agar pengguna dapat mengenali dan membedakan setiap pakaian.
### Selesai bila
- Setiap kartu menampilkan nama pakaian dengan jelas.
- Setiap kartu menampilkan ukuran pakaian (misal: M, L, XL) di posisi yang mudah terlihat.
- Setiap kartu menampilkan harga jual dalam format Rupiah (misal: Rp50.000).
- Setiap kartu menampilkan status pakaian.

## Sub-fitur: Status terjual

Menandai setiap pakaian sebagai tersedia atau terjual.

### Tujuan
Menandai setiap pakaian sebagai "Tersedia" atau "Terjual" agar pengguna langsung tahu barang mana yang masih bisa dijual dan mana yang sudah laku.
### Selesai bila
- Setiap item menampilkan label status yang jelas: "Tersedia" untuk yang belum laku dan "Terjual" untuk yang sudah laku.
- Status tampil dengan perbedaan visual yang mudah dikenali, misalnya warna atau ikon yang berbeda.
- Label status selalu sesuai dengan data tersimpan; item baru berstatus "Tersedia" sampai statusnya diubah menjadi "Terjual".

## Task

### 1. Buat layar utama daftar item tiruan

### 2. Buat kartu item tampilkan harga status

### 3. Buat dialog tambah item dari FAB

### 4. Buat dialog edit, toggle status, hapus item

### 5. Buat entity ThriftItem dan database Room

### 6. Buat DAO Room untuk operasi CRUD

### 7. Buat ViewModel dan integrasi ke UI
