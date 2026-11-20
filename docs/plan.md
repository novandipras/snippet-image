# Implementation plan

1. Daftarkan action yang hanya aktif ketika ada kode terpilih.
2. Ambil teks dan warna token dari highlighter editor, tanpa mengubah file sumber.
3. Render Java2D dengan batas ukuran, tab, font editor, dan nomor baris opsional.
4. Tambahkan dialog preview dengan simpan PNG dan clipboard, serta penanganan kegagalan.
5. Paketkan memakai SDK/JDK lokal; periksa kompilasi, PNG, metadata, dan isi ZIP.

Tidak menambahkan unit/widget test sesuai instruksi pengguna. Validasi menggunakan smoke check sementara.
