package org.delcom.starter.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    /**
     * 1. Method untuk memberikan informasi berdasarkan NIM.
     *    (Kode ini dari permintaan Anda sebelumnya).
     */
    @GetMapping("/informasi-nim")
    public String informasiNim(@RequestParam String nim) {
        // Mapping prefix ke program studi
        HashMap<String, String> prodi = new HashMap<>();
        prodi.put("11S", "Sarjana Informatika");
        prodi.put("12S", "Sarjana Sistem Informasi");
        prodi.put("14S", "Sarjana Teknik Elektro");
        prodi.put("21S", "Sarjana Manajemen Rekayasa");
        prodi.put("22S", "Sarjana Teknik Metalurgi");
        prodi.put("31S", "Sarjana Teknik Bioproses");
        prodi.put("114", "Diploma 4 Teknologi Rekasaya Perangkat Lunak");
        prodi.put("113", "Diploma 3 Teknologi Informasi");
        prodi.put("133", "Diploma 3 Teknologi Komputer");

        // Ambil prefix dan angkatan
        String prefix = nim.substring(0, 3);
        String angkatan = "20" + nim.substring(3, 5);

        // Urutan: buang leading zero
        int urutInt = Integer.parseInt(nim.substring(nim.length() - 3));
        String urutan = String.valueOf(urutInt);

        // Buat output sebagai String
        StringBuilder sb = new StringBuilder();
        sb.append("Informasi NIM ").append(nim).append(":\n");
        sb.append(">> Program Studi: ").append(prodi.getOrDefault(prefix, "Unknown")).append("\n");
        sb.append(">> Angkatan: ").append(angkatan).append("\n");
        sb.append(">> Urutan: ").append(urutan);

        return sb.toString();
    }

    /**
     * 2. Method untuk menghitung perolehan nilai akhir.
     *    Input strBase64 di-decode, lalu setiap baris format "TIPE|NILAI|BOBOT"
     *    dihitung untuk mendapatkan nilai akhir.
     */
    @GetMapping("/perolehan-nilai")
    public String perolehanNilai(@RequestParam String strBase64) {
        // Decode input Base64 menjadi string biasa
        byte[] decodedBytes = Base64.getDecoder().decode(strBase64);
        String data = new String(decodedBytes);

        String[] lines = data.split("\n");
        double totalNilai = 0.0;
        int totalBobot = 0;

        for (String line : lines) {
            // Proses baris yang mengandung '|'
            if (line.contains("|")) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    try {
                        double nilai = Double.parseDouble(parts[1]);
                        int bobot = Integer.parseInt(parts[2]);
                        totalNilai += nilai * (bobot / 100.0);
                        totalBobot += bobot;
                    } catch (NumberFormatException e) {
                        // Abaikan baris yang formatnya salah
                    }
                }
            } else if (line.trim().equals("---")) {
                // Berhenti jika menemukan separator
                break;
            }
        }
        
        // Menentukan grade berdasarkan nilai akhir
        String grade;
        if (totalNilai >= 85) grade = "A";
        else if (totalNilai >= 75) grade = "B";
        else if (totalNilai >= 65) grade = "C";
        else if (totalNilai >= 55) grade = "D";
        else grade = "E";

        return String.format("Nilai Akhir: %.2f (Total Bobot: %d%%)\nGrade: %s", totalNilai, totalBobot, grade);
    }

    /**
     * 3. Method untuk menghitung perbedaan jarak antara path 'L' dan kebalikannya.
     *    'L' (kiri) lawannya 'R' (kanan), dan 'U' (atas) lawannya 'D' (bawah).
     *    Jarak dihitung menggunakan Manhattan Distance.
     */
    @GetMapping("/perbedaan-l")
    public String perbedaanL(@RequestParam String strBase64) {
        // Decode input Base64
        byte[] decodedBytes = Base64.getDecoder().decode(strBase64);
        String path = new String(decodedBytes).trim();

        // ---- Logika calculateCoordinates(path) di-inline di sini ----
        int xOriginal = 0, yOriginal = 0;
        for (char move : path.toCharArray()) {
            switch (move) {
                case 'U': yOriginal++; break;
                case 'D': yOriginal--; break;
                case 'L': xOriginal--; break;
                case 'R': xOriginal++; break;
            }
        }
        int[] originalCoord = new int[]{xOriginal, yOriginal};
        // -----------------------------------------------------------


        // Buat path kebalikannya
        StringBuilder oppositePath = new StringBuilder();
        for (char move : path.toCharArray()) {
            switch (move) {
                case 'U': oppositePath.append('D'); break;
                case 'D': oppositePath.append('U'); break;
                case 'L': oppositePath.append('R'); break;
                case 'R': oppositePath.append('L'); break;
            }
        }

        // ---- Logika calculateCoordinates(oppositePath.toString()) di-inline di sini ----
        int xOpposite = 0, yOpposite = 0;
        for (char move : oppositePath.toString().toCharArray()) {
            switch (move) {
                case 'U': yOpposite++; break;
                case 'D': yOpposite--; break;
                case 'L': xOpposite--; break;
                case 'R': xOpposite++; break;
            }
        }
        int[] oppositeCoord = new int[]{xOpposite, yOpposite};
        // -------------------------------------------------------------------------------


        // Hitung perbedaan jarak (Manhattan Distance)
        int distance = Math.abs(originalCoord[0] - oppositeCoord[0]) + Math.abs(originalCoord[1] - oppositeCoord[1]);

        return String.format("Path Original: %s -> (%d, %d)\nPath Kebalikan: %s -> (%d, %d)\nPerbedaan Jarak: %d",
                path, originalCoord[0], originalCoord[1],
                oppositePath.toString(), oppositeCoord[0], oppositeCoord[1],
                distance);
    }

    // Metode calculateCoordinates(String) sudah DIHAPUS dari sini.

    /**
     * 4. Method untuk menemukan kata berawalan "ter" yang paling sering muncul.
     *    Teks input di-decode, lalu dihitung frekuensi setiap kata "ter-".
     */
    @GetMapping("/paling-ter")
    public String palingTer(@RequestParam String strBase64) {
        // Decode input Base64
        byte[] decodedBytes = Base64.getDecoder().decode(strBase64);
        String text = new String(decodedBytes);

        // Buat map untuk menyimpan frekuensi kata
        Map<String, Integer> wordCount = new HashMap<>();

        // Pisahkan teks menjadi kata, ubah ke huruf kecil, dan hapus tanda baca
        String[] words = text.toLowerCase().split("\\W+");

        for (String word : words) {
            if (word.startsWith("ter")) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        if (wordCount.isEmpty()) {
            return "Tidak ditemukan kata yang berawalan 'ter'.";
        }

        // Cari kata dengan frekuensi tertinggi
        String mostFrequentWord = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentWord = entry.getKey();
            }
        }

        return String.format("Kata 'ter' yang paling sering muncul adalah '%s' (muncul %d kali).", mostFrequentWord, maxCount);
    }
}