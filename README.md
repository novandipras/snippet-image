# Snippet Image

Plugin Android Studio untuk membuat gambar dari kode terpilih. Renderer native Java2D,
tanpa WebView, dependency tambahan, service startup, atau akses jaringan.

## Pasang

1. Buka **Settings → Plugins → ⚙ → Install Plugin from Disk…**.
2. Pilih `dist/snippet-image-0.1.1.zip`, lalu restart IDE jika diminta.
3. Blok kode, klik kanan → **Snippet Image**; tersedia juga melalui Tools dan Find Action.
4. Atur nomor baris, padding transparan, dan skala 1x/2x. Pilih **Save PNG…** atau **Copy Image**.

Warna token dan font mengikuti editor aktif. Preview diperkecil bila perlu; ekspor tetap resolusi penuh.
File yang sudah ada tidak ditimpa: pilih nama baru untuk menyimpan lagi.
Clipboard gambar dapat ditempel pada aplikasi yang menerima gambar.
ZIP yang sama digunakan untuk Windows dan macOS, dengan Android Studio platform **262**.

## Build lokal

Memerlukan Python 3 serta Android Studio dengan JDK 21 atau lebih baru.

```sh
python3 build.py
```

Lokasi default SDK macOS: `/Applications/Android Studio.app/Contents`.
Gunakan environment variable `ANDROID_STUDIO_HOME` untuk lokasi lain.
Tidak ada dependency yang diunduh. Target kompatibilitas versi awal: platform **262**.

### Windows (PowerShell)

Untuk memasang plugin, cukup gunakan ZIP melalui **Install Plugin from Disk**; Python hanya diperlukan untuk build source.
Script build mendeteksi instalasi default di `%ProgramFiles%\Android\Android Studio` dan menggunakan JDK bawaannya.

```powershell
cd C:\lokasi\snippet-image
py -3 build.py
```

Jika Android Studio dipasang di lokasi lain, tentukan folder yang berisi `lib` dan `jbr`:

```powershell
$env:ANDROID_STUDIO_HOME = 'D:\Aplikasi\Android Studio'
py -3 build.py
```

Hasil build ada di `dist\snippet-image-0.1.1.zip`.
Setelah plugin terpasang: pilih kode → klik kanan → **Snippet Image** → **Save PNG…** atau **Copy Image**.
Untuk Confluence, gunakan ekspor PNG dan lampirkan gambar tersebut.

Script build diperiksa dengan simulasi layout instalasi Windows, termasuk path berisi spasi.
Pengujian UI langsung baru dilakukan di macOS; clipboard, font, dan scaling DPI Windows masih perlu diverifikasi di perangkat Windows.

## Batasan

- Satu seleksi kontinu, maksimal 300 baris / 20.000 karakter.
- Gambar maksimal 8.192 piksel per sisi dan 16 juta piksel total untuk membatasi penggunaan memori.
- Syntax highlighting memakai token lexer; semantic highlighting, diagnostic underline, dan inlay hints tidak diekspor.
- Padding transparan mengelilingi panel kode; warna panel tetap mengikuti tema agar kode terbaca.
- Belum ada benchmark pembanding plugin screenshot kode; ukuran paket kecil tidak membuktikan penggunaan RAM lebih rendah.

## Versi 0.1.1

Build source mendukung layout Android Studio Windows, `javac.exe`, dan classpath wildcard agar perintah tetap pendek.

## Versi 0.1.0

Seleksi kode, preview, nomor baris aktual, PNG 1x/2x, padding transparan, dan salin gambar ke clipboard.
