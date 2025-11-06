package org.delcom.starter.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String hello() {
        return "Hay, selamat datang di aplikasi dengan Spring Boot!";
    }

    @GetMapping("/hello/{name}")
    public String sayHello(@PathVariable String name) {
        return "Hello, " + name + "!";
    }

    //======================================================================
    // METHOD 1: Dari NIMController
    //======================================================================
    @GetMapping("/info-nim/{nim}")
    public String informasiNim(@PathVariable String nim) {
        if (nim.length() != 8) {
            return "NIM harus 8 karakter";
        }
        String namaProdi;
        String angkatan;
        int nomorUrut;
        
        String kodeProdi = nim.substring(0, 3);
        String kodeAngkatan = nim.substring(3, 5);
        String strNomorUrut = nim.substring(5);
        switch (kodeProdi) {
            case "11S": namaProdi = "Sarjana Informatika"; break;
            case "12S": namaProdi = "Sarjana Sistem Informasi"; break;
            case "14S": namaProdi = "Sarjana Teknik Elektro"; break;
            case "21S": namaProdi = "Sarjana Manajemen Rekayasa"; break;
            case "22S": namaProdi = "Sarjana Teknik Metalurgi"; break;
            case "31S": namaProdi = "Sarjana Teknik Bioproses"; break;
            case "114": namaProdi = "Diploma 4 Teknologi Rekasaya Perangkat Lunak"; break;
            case "113": namaProdi = "Diploma 3 Teknologi Informasi"; break;
            case "133": namaProdi = "Diploma 3 Teknologi Komputer"; break;
            default: namaProdi = "Program Studi tidak Tersedia"; break;
        }
        if ("Program Studi tidak Tersedia".equals(namaProdi)) {
            return namaProdi;
        }
        angkatan = "20" + kodeAngkatan;
        nomorUrut = Integer.parseInt(strNomorUrut);
        
        return String.format(
            "Inforamsi NIM %s: >> Program Studi: %s>> Angkatan: %s>> Urutan: %d",
            nim, namaProdi, angkatan, nomorUrut
        );
    }

    //======================================================================
    // METHOD 2: perolehanNilai(strBase64)
    //======================================================================
    @GetMapping("/perolehan-nilai")
    public String perolehanNilai(@RequestParam("data") String strBase64) {
        byte[] decodedBytes = Base64.getDecoder().decode(strBase64);
        String decodedString = new String(decodedBytes);
        List<String> lines = Arrays.stream(decodedString.split("\\R")).collect(Collectors.toList());
        int bobotPA = Integer.parseInt(lines.get(0).trim());
        int bobotT = Integer.parseInt(lines.get(1).trim());
        int bobotK = Integer.parseInt(lines.get(2).trim());
        int bobotP = Integer.parseInt(lines.get(3).trim());
        int bobotUTS = Integer.parseInt(lines.get(4).trim());
        int bobotUAS = Integer.parseInt(lines.get(5).trim());
        if ((bobotPA + bobotT + bobotK + bobotP + bobotUTS + bobotUAS) != 100) return "Total bobot harus 100<br/>";
        List<String> daftarNilai = lines.subList(6, lines.size());
        List<String> errorLog = new ArrayList<>();
        Locale.setDefault(Locale.US);
        int totalPA = 0, maxPA = 0, totalT = 0, maxT = 0, totalK = 0, maxK = 0, totalP = 0, maxP = 0, totalUTS = 0, maxUTS = 0, totalUAS = 0, maxUAS = 0;
        for (String line : daftarNilai) {
            String[] parts = line.split("\\|");
            if (parts.length != 3) {
                if (!line.trim().equals("---")) errorLog.add("Data tidak valid. Silahkan menggunakan format: Simbol|Bobot|Perolehan-Nilai");
                continue;
            }
            String simbol = parts[0].trim();
            int maks = Integer.parseInt(parts[1].trim());
            int nilai = Integer.parseInt(parts[2].trim());
            switch (simbol) {
                case "PA": maxPA += maks; totalPA += nilai; break;
                case "T":  maxT += maks;  totalT += nilai;  break;
                case "K":  maxK += maks;  totalK += nilai;  break;
                case "P":  maxP += maks;  totalP += nilai;  break;
                case "UTS":maxUTS += maks;totalUTS += nilai;break;
                case "UAS":maxUAS += maks;totalUAS += nilai;break;
                default: errorLog.add("Simbol tidak dikenal"); break;
            }
        }
        int bulatPA = (int) ((maxPA == 0) ? 0 : (totalPA * 100.0 / maxPA));
        int bulatT = (int) ((maxT == 0) ? 0 : (totalT * 100.0 / maxT));
        int bulatK = (int) ((maxK == 0) ? 0 : (totalK * 100.0 / maxK));
        int bulatP = (int) ((maxP == 0) ? 0 : (totalP * 100.0 / maxP));
        int bulatUTS = (int) ((maxUTS == 0) ? 0 : (totalUTS * 100.0 / maxUTS));
        int bulatUAS = (int) ((maxUAS == 0) ? 0 : (totalUAS * 100.0 / maxUAS));
        double nilaiPA = (bulatPA / 100.0) * bobotPA, nilaiT = (bulatT / 100.0) * bobotT, nilaiK = (bulatK / 100.0) * bobotK, nilaiP = (bulatP / 100.0) * bobotP, nilaiUTS = (bulatUTS / 100.0) * bobotUTS, nilaiUAS = (bulatUAS / 100.0) * bobotUAS;
        double totalNilai = nilaiPA + nilaiT + nilaiK + nilaiP + nilaiUTS + nilaiUAS;
        String grade;
        if (totalNilai >= 79.5) grade = "A"; else if (totalNilai >= 72) grade = "AB"; else if (totalNilai >= 64.5) grade = "B"; else if (totalNilai >= 57) grade = "BC"; else if (totalNilai >= 49.5) grade = "C"; else if (totalNilai >= 34) grade = "D"; else grade = "E";
        StringBuilder response = new StringBuilder();
        if (!errorLog.isEmpty()) response.append(String.join("<br/>", errorLog)).append("<br/>");
        response.append("Perolehan Nilai:<br/>");
        response.append(String.format(">> Partisipatif: %d/100 (%.2f/%d)<br/>", bulatPA, nilaiPA, bobotPA));
        response.append(String.format(">> Tugas: %d/100 (%.2f/%d)<br/>", bulatT, nilaiT, bobotT));
        response.append(String.format(">> Kuis: %d/100 (%.2f/%d)<br/>", bulatK, nilaiK, bobotK));
        response.append(String.format(">> Proyek: %d/100 (%.2f/%d)<br/>", bulatP, nilaiP, bobotP));
        response.append(String.format(">> UTS: %d/100 (%.2f/%d)<br/>", bulatUTS, nilaiUTS, bobotUTS));
        response.append(String.format(">> UAS: %d/100 (%.2f/%d)<br/>", bulatUAS, nilaiUAS, bobotUAS));
        response.append("<br/>");
        response.append(String.format(">> Nilai Akhir: %.2f<br/>", totalNilai));
        response.append(String.format(">> Grade: %s<br/>", grade));
        return response.toString();
    }

    //======================================================================
    // METHOD 3: perbedaanL(strBase64)
    //======================================================================
    @GetMapping("/perbedaan-l")
    public String perbedaanL(@RequestParam("data") String strBase64) {
        byte[] decodedBytes = Base64.getDecoder().decode(strBase64);
        String decodedString = new String(decodedBytes);
        List<String> lines = Arrays.stream(decodedString.split("\\R")).collect(Collectors.toList());
        
        int n = Integer.parseInt(lines.get(0).trim());
        List<String> elemenList = lines.stream().skip(1).flatMap(line -> Stream.of(line.trim().split("\\s+"))).collect(Collectors.toList());

        int[][] matriks = new int[n][n]; int index = 0;
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) matriks[i][j] = Integer.parseInt(elemenList.get(index++));
        String nilaiLStr = "Tidak Ada", nilaiKebalikanStr = "Tidak Ada", nilaiTengahStr = "Tidak Ada", perbedaanStr = "Tidak Ada"; int dominan = 0;
        if (n == 1) { dominan = matriks[0][0]; nilaiTengahStr = String.valueOf(dominan); } 
        else {
            if (n % 2 == 1) nilaiTengahStr = String.valueOf(matriks[n / 2][n / 2]);
            else { int mid = n / 2; nilaiTengahStr = String.valueOf(matriks[mid - 1][mid - 1] + matriks[mid - 1][mid] + matriks[mid][mid - 1] + matriks[mid][mid]); }
        }
        if (n == 2) dominan = Integer.parseInt(nilaiTengahStr);
        if (n >= 3) {
            int nilaiL = 0; for (int i = 0; i < n; i++) nilaiL += matriks[i][0]; for (int j = 1; j <= n - 2; j++) nilaiL += matriks[n - 1][j]; nilaiLStr = String.valueOf(nilaiL);
            int nilaiKebalikan = 0; for (int i = 0; i < n; i++) nilaiKebalikan += matriks[i][n - 1]; for (int j = 1; j <= n - 2; j++) nilaiKebalikan += matriks[0][j]; nilaiKebalikanStr = String.valueOf(nilaiKebalikan);
            perbedaanStr = String.valueOf(Math.abs(nilaiL - nilaiKebalikan));
             if (nilaiL == nilaiKebalikan) dominan = Integer.parseInt(nilaiTengahStr); else dominan = Math.max(nilaiL, nilaiKebalikan);
        }
        StringBuilder response = new StringBuilder();
        response.append("Nilai L: ").append(nilaiLStr).append("<br/>").append("Nilai Kebalikan L: ").append(nilaiKebalikanStr).append("<br/>").append("Nilai Tengah: ").append(nilaiTengahStr).append("<br/>").append("Perbedaan: ").append(perbedaanStr).append("<br/>").append("Dominan: ").append(dominan).append("<br/>");
        return response.toString();
    }
    
    //======================================================================
    // METHOD 4: palingTer(strBase64)
    //======================================================================
    @GetMapping("/paling-ter")
    public String palingTer(@RequestParam("data") String strBase64) {
        List<Integer> daftarNilai = new ArrayList<>();
        byte[] decodedBytes = Base64.getDecoder().decode(strBase64);
        String decodedString = new String(decodedBytes);
        String[] lines = decodedString.split("\\r?\\n|\\r");
        for (String line : lines) {
            try {
                daftarNilai.add(Integer.parseInt(line.trim()));
            } catch (NumberFormatException e) {}
        }
        
        if (daftarNilai.isEmpty()) {
            return "Informasi tidak tersedia";
        }

        HashMap<Integer, Integer> hashMapNilai = new HashMap<>();
        HashMap<Integer, Integer> hashMapCounterTerbanyak = new HashMap<>();
        HashMap<Integer, Integer> hashMapTotalTerendah = new HashMap<>();
       
        int[] arrayNilai = daftarNilai.stream().mapToInt(Integer::intValue).toArray();

        int nilaiTertinggi = arrayNilai[0];
        int nilaiTerendah = arrayNilai[0];

        for (int i = 1; i < arrayNilai.length; i++) {
            if (nilaiTertinggi < arrayNilai[i]) {
                nilaiTertinggi = arrayNilai[i];
            }
            if (nilaiTerendah > arrayNilai[i]) {
                nilaiTerendah = arrayNilai[i];
            }
        }
        for (int nilai : arrayNilai) {
            hashMapNilai.put(nilai, hashMapNilai.getOrDefault(nilai, 0) + 1);
        }

        int nilaiJumlahTerendah = arrayNilai[0];
        int jumlahTerendah = nilaiJumlahTerendah;

        for(int i = 0; i < arrayNilai.length; i++) {
            if (hashMapTotalTerendah.containsKey(arrayNilai[i])) {
                int newTotal = hashMapTotalTerendah.get(arrayNilai[i]) + arrayNilai[i];
                hashMapTotalTerendah.put(arrayNilai[i], newTotal);
            } else {
                hashMapTotalTerendah.put(arrayNilai[i], arrayNilai[i]);
            }
            if (arrayNilai[i] == nilaiJumlahTerendah) {
                jumlahTerendah = hashMapTotalTerendah.get(nilaiJumlahTerendah);
            }
            else if (jumlahTerendah > hashMapTotalTerendah.get(arrayNilai[i])) {
                nilaiJumlahTerendah = arrayNilai[i];
                jumlahTerendah = hashMapTotalTerendah.get(arrayNilai[i]);
            }
        }

        int nilaiTerbanyak = 0;
        int frekuensiTerbanyak = 0;

        for(int i = 0; i < arrayNilai.length; i++) {
            int frekuensiSaatIni = hashMapNilai.get(arrayNilai[i]);
            if(frekuensiSaatIni > frekuensiTerbanyak) {
                frekuensiTerbanyak = frekuensiSaatIni;
            }
        }

        int i = 0;
        do {
            int nilai = arrayNilai[i];
            int count = hashMapCounterTerbanyak.getOrDefault(nilai, 0) + 1;
            hashMapCounterTerbanyak.put(nilai, count);
            
            if (count == frekuensiTerbanyak) {
                nilaiTerbanyak = nilai;
                break;
            }
            i++;
        } while (true);

        int nilaiTerdikit = arrayNilai[0];
        HashMap<Integer, Integer> seenNumbers = new HashMap<>();
        boolean isSearchingForNew = false;
        
        for (int currentValue : arrayNilai) {
            boolean isFirstTimeSeen = !seenNumbers.containsKey(currentValue);
            seenNumbers.put(currentValue, 1);

            if (isSearchingForNew && isFirstTimeSeen) {
                nilaiTerdikit = currentValue;
                isSearchingForNew = false;
            } else if (currentValue == nilaiTerdikit && !isFirstTimeSeen) {
                isSearchingForNew = true;
            }
        }
        int frekuensiTerdikit = hashMapNilai.get(nilaiTerdikit);

        int jumlahTertinggi = arrayNilai[0];
        int nilaiJumlahTertinggi = 0;
        int frekuensiNilaiJumlahTertinggi = 0;

        for (Map.Entry<Integer, Integer> entry : hashMapNilai.entrySet()) {
            int frekuensiSaatIni = entry.getValue();
            int angkaSaatIni = entry.getKey();
            int jumlah = frekuensiSaatIni * angkaSaatIni;
            
            if(jumlah > jumlahTertinggi) {
                jumlahTertinggi = jumlah;
                nilaiJumlahTertinggi = angkaSaatIni;
                frekuensiNilaiJumlahTertinggi = frekuensiSaatIni;
            } else if (jumlah == jumlahTertinggi) {
                    nilaiJumlahTertinggi = angkaSaatIni;
                    frekuensiNilaiJumlahTertinggi = frekuensiSaatIni;
            }
        }
        
        StringBuilder response = new StringBuilder();
        response.append("Tertinggi: ").append(nilaiTertinggi).append("<br/>");
        response.append("Terendah: ").append(nilaiTerendah).append("<br/>");
        response.append("Terbanyak: ").append(nilaiTerbanyak).append(" (").append(frekuensiTerbanyak).append("x)").append("<br/>");
        response.append("Tersedikit: ").append(nilaiTerdikit).append(" (").append(frekuensiTerdikit).append("x)").append("<br/>");
        response.append("Jumlah Tertinggi: ").append(nilaiJumlahTertinggi).append(" * ").append(frekuensiNilaiJumlahTertinggi).append(" = ").append(jumlahTertinggi).append("<br/>");
        response.append("Jumlah Terendah: ").append(nilaiJumlahTerendah).append(" * ").append(hashMapNilai.get(nilaiJumlahTerendah)).append(" = ").append(jumlahTerendah).append("<br/>");
        return response.toString();
    }
}