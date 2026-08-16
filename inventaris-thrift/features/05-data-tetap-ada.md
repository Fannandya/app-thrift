# Data Tetap Ada

Semua catatan tersimpan di perangkat dan tidak hilang.

## Spesifikasi

### Tujuan
Semua catatan pakaian thrift tersimpan permanen di perangkat, sehingga tidak hilang saat aplikasi dibuka dan ditutup.

### Selesai bila
- Setiap penambahan, perubahan, atau penghapusan item langsung tersimpan.
- Setelah aplikasi ditutup dan dibuka kembali, semua item yang tersimpan masih muncul di daftar.
- Data tetap ada meskipun perangkat di-restart.

## Sub-fitur: Tersimpan otomatis

Setiap perubahan item langsung tersimpan.

### Tujuan
Setiap perubahan pada item langsung disimpan tanpa perlu tombol simpan terpisah.

### Selesai bila
- Item baru langsung muncul di daftar setelah disimpan.
- Perubahan detail atau status terjual langsung tersimpan.
- Jika aplikasi ditutup segera setelah perubahan, perubahan tetap ada.

## Sub-fitur: Aman saat ditutup

Data tetap ada walau aplikasi ditutup.

### Tujuan
Data inventaris tidak hilang saat aplikasi ditutup atau perangkat dimatikan.

### Selesai bila
- Setelah aplikasi ditutup dan dibuka lagi, daftar item masih sama.
- Setelah perangkat di-restart, semua item tetap tampil.
- Data tersimpan sepenuhnya lokal tanpa internet.

## Task

### 1. Buat layar utama daftar item thrift

### 2. Buat dialog tambah dan edit item

### 3. Buat aksi hapus dan toggle status

### 4. Buat entity DAO database Room

### 5. Buat repository Room untuk CRUD

### 6. Integrasi repository Room ke ViewModel untuk auto-save
