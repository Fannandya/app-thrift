# Ubah Barang

Mengubah detail item atau menandai status terjual.

## Spesifikasi

### Tujuan
Memungkinkan pengguna mengubah detail pakaian thrift atau menandai status terjual sehingga data stok selalu akurat.

### Selesai bila
- Pengguna dapat mengetuk kartu item dan membuka dialog edit.
- Dialog menampilkan data item saat ini dan dapat mengubah nama, ukuran, harga beli, harga jual.
- Ada kontrol untuk mengubah status Tersedia/Terjual.
- Setelah menekan Simpan, perubahan langsung tersimpan dan daftar diperbarui.
- Perubahan tetap ada setelah aplikasi ditutup.

## Sub-fitur: Edit detail

Memperbarui nama, ukuran, atau harga item.

### Tujuan
Memperbarui nama, ukuran, atau harga item pakaian.

### Selesai bila
- Dialog edit menampilkan kolom nama, ukuran, harga beli, dan harga jual berisi nilai lama.
- Pengguna dapat mengubah teks pada kolom tersebut.
- Setelah disimpan, teks di kartu item berubah sesuai nilai baru.

## Sub-fitur: Tandai terjual

Mengubah status menjadi Terjual.

### Tujuan
Mengubah status item menjadi Terjual.

### Selesai bila
- Terdapat kontrol (misal toggle/switch) untuk mengubah status Tersedia/Terjual.
- Status Terjual ditampilkan jelas pada kartu (misal label "Terjual").
- Setelah disimpan, label status di daftar berubah menjadi Terjual atau kembali Tersedia.

## Sub-fitur: Simpan perubahan

Menyimpan hasil edit ke dalam daftar.

### Tujuan
Menyimpan hasil edit ke database agar daftar menampilkan data terbaru.

### Selesai bila
- Tombol "Simpan" di dialog menyimpan semua perubahan ke database.
- Dialog tertutup setelah disimpan.
- Daftar langsung menampilkan item dengan data terbaru.

## Task

### 1. Buat layar daftar item untuk ubah barang

### 2. Buat dialog edit dengan data lama

### 3. Tambahkan kontrol status dan label terjual

### 4. Simpan perubahan ke daftar dummy

### 5. Buat entity ThriftItem dan DAO update

### 6. Buat database Room dan repository

### 7. Hubungkan tombol Simpan ke ViewModel
