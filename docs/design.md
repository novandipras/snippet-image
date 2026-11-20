# Snippet Image

Fokus: fungsi plugin screenshot kode untuk Android Studio, tanpa meniru tampilannya secara persis.

Pilihan implementasi: renderer Java2D native, memakai token warna highlighter editor yang sedang aktif.
Alternatif WebView membutuhkan mesin browser untuk fungsi yang dapat ditangani API bawaan.
Java dipilih agar dapat dikompilasi langsung memakai JDK dan SDK Android Studio yang terpasang tanpa dependency baru.

Alur: blok kode, pilih Snippet Image dari menu editor/Tools, preview, simpan PNG atau copy gambar.
Opsi minimum: nomor baris aktual, padding transparan, skala 1x/2x. Tema dan font mengikuti editor.
Tidak ada startup service, upload, telemetry, atau pengubahan dokumen sumber.
Ukuran input dan piksel dibatasi sebelum alokasi untuk menjaga respons IDE.
Token lexer dipertahankan; semantic highlighting dan dekorasi diagnostik tidak termasuk.

Target awal: Android Studio lokal build 262. Distribusi ZIP untuk Install Plugin from Disk.
Validasi: kompilasi terhadap SDK lokal, pemeriksaan struktur ZIP dan descriptor, serta smoke check renderer PNG.
Benchmark perbandingan dengan plugin screenshot kode dan pengujian IDE versi lain di luar cakupan versi awal.
