# Tambah Barang

Menambahkan pakaian thrift baru ke dalam inventaris.

## Spesifikasi

### Tujuan
Menambahkan pakaian thrift baru ke dalam inventaris dengan cepat dan mudah.

### Selesai bila
- Pengguna dapat membuka form tambah barang dari tombol + di layar utama.
- Form menampilkan kolom nama, ukuran, harga beli, dan harga jual.
- Setelah mengisi data dan menekan Simpan, item baru muncul di daftar barang.
- Item baru tersimpan di perangkat dan tetap ada saat aplikasi ditutup.

## Sub-fitur: Isi data

Memasukkan nama, ukuran, harga beli, dan harga jual.

### Tujuan
Memasukkan detail pakaian baru: nama, ukuran, harga beli, dan harga jual.

### Selesai bila
- Layar menampilkan form dengan 4 kolom: Nama, Ukuran, Harga Beli, Harga Jual.
- Setiap kolom dapat diisi oleh pengguna (teks untuk nama/ukuran, angka untuk harga).
- Data yang diketik tampak jelas dan bisa diedit sebelum disimpan.

## Sub-fitur: Simpan item

Menyimpan item baru sehingga muncul di daftar.

### Tujuan
Menyimpan item baru ke dalam inventaris sehingga muncul di daftar.

### Selesai bila
- Ada tombol "Simpan" di form yang bisa ditekan.
- Setelah ditekan, item baru muncul di daftar barang.
- Form tertutup dan kembali ke layar utama.
- Item baru berstatus "Tersedia".

## Task

### 1. Buat layar utama daftar barang dengan FAB

### 2. Buat form tambah barang dengan validasi

### 3. Simpan item & set status Tersedia

### 4. Buat entity Room untuk ThriftItem

### 5. Buat DAO ThriftItem untuk CRUD

### 6. Buat AppDatabase dan migrasi

### 7. Sambungkan repository Room ke ViewModel
