# Hapus Barang

Menghapus item yang tidak diperlukan dari daftar.

## Spesifikasi

### Tujuan
Memungkinkan pengguna menghapus item yang tidak diperlukan dari daftar inventaris agar data tetap bersih dan rapi.

### Selesai bila
- Setiap kartu item memiliki tombol hapus yang mudah dikenali.
- Menekan tombol hapus memunculkan dialog konfirmasi sebelum item benar-benar dihapus.
- Setelah konfirmasi, item langsung hilang dari daftar dan datanya terhapus dari database.
- Data yang sudah dihapus tidak muncul lagi saat aplikasi dibuka kembali.

## Sub-fitur: Tombol hapus

Menyediakan tombol hapus pada setiap item.

### Tujuan
Memberikan tombol hapus pada setiap kartu item agar pengguna dapat memulai proses penghapusan.

### Selesai bila
- Setiap item di daftar utama menampilkan tombol/ikon hapus (misalnya ikon tempat sampah).
- Tombol hapus dapat ditekan tanpa mengganggu fungsi klik kartu untuk mengedit item.
- Saat tombol hapus ditekan, muncul dialog konfirmasi (belum ada data yang dihapus).

## Sub-fitur: Konfirmasi hapus

Menanyakan keyakinan sebelum item dihapus.

### Tujuan
Menanyakan keyakinan pengguna sebelum item dihapus agar tidak terjadi penghapusan tidak sengaja.

### Selesai bila
- Muncul dialog konfirmasi berisi pesan jelas seperti: Hapus item ini? dan menampilkan nama item yang akan dihapus.
- Dialog menyediakan dua pilihan: Hapus dan Batal.
- Menekan Hapus menghapus item dari database dan daftar langsung diperbarui; menekan Batal menutup dialog tanpa mengubah data.

## Task

### 1. Tambahkan tombol hapus pada setiap kartu item

### 2. Munculkan dialog konfirmasi hapus saat tombol ditekan

### 3. Proses pilihan Hapus dan Batal pada dialog

### 4. Tambahkan delete di DAO dan Repository

### 5. Gunakan repository Room untuk hapus dari ViewModel
