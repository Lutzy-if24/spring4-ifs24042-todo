package org.delcom.starter.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

public class HomeControllerTest {

    private HomeController controller;

    @BeforeEach
    void setUp() {
        controller = new HomeController();
    }

    @Test
    void testInformasiNim() {
        String nimValid = "11S23001";
        String resultValid = controller.informasiNim(nimValid);
        assertTrue(resultValid.contains("Sarjana Informatika"), "Harus mengandung program studi yang benar");
        assertTrue(resultValid.contains("Angkatan: 2023"), "Harus mengandung angkatan yang benar");
        assertTrue(resultValid.contains("Urutan: 1"), "Harus mengandung nomor urut yang benar");

        String nimUnknown = "99X23123";
        String resultUnknown = controller.informasiNim(nimUnknown);
        assertTrue(resultUnknown.contains("Program Studi: Unknown"), "Harus menampilkan 'Unknown' untuk prefix tidak dikenal");
    }

    @Test
    void testPerolehanNilai() {
        String dataNilai = "UAS|85|40\nUTS|75|30\nPA|90|20\nK|100|10\n---\n";
        String strBase64 = Base64.getEncoder().encodeToString(dataNilai.getBytes());
        String result = controller.perolehanNilai(strBase64);

        boolean isNilaiBenar = result.contains("Nilai Akhir: 84,50") || result.contains("Nilai Akhir: 84.50");
        assertTrue(isNilaiBenar, "Nilai akhir harus 84.50 atau 84,50");
        
        assertTrue(result.contains("Total Bobot: 100%"), "Total bobot harus 100%");
        assertTrue(result.contains("Grade: B"), "Grade harus B");
    }

    @Test
    void testPerbedaanL() {
        String path = "UULL";
        String strBase64 = Base64.getEncoder().encodeToString(path.getBytes());
        String result = controller.perbedaanL(strBase64);

        assertTrue(result.contains("Path Original: UULL -> (-2, 2)"), "Koordinat original harus benar");
        assertTrue(result.contains("Path Kebalikan: DDRR -> (2, -2)"), "Koordinat kebalikan harus benar");
        assertTrue(result.contains("Perbedaan Jarak: 8"), "Perbedaan jarak harus 8");
    }

    @Test
    void testPalingTer() {
        String teks = "Sistem terbaik adalah sistem yang tercipta untuk menjadi yang terbaik dan terhebat.";
        String strBase64 = Base64.getEncoder().encodeToString(teks.getBytes());
        String result = controller.palingTer(strBase64);

        assertTrue(result.contains("'terbaik'"), "Harus menemukan 'terbaik' sebagai kata yang paling sering muncul");
        assertTrue(result.contains("muncul 2 kali"), "Harus menyatakan bahwa kata tersebut muncul 2 kali");
        
        String teksTanpaTer = "Ini adalah sebuah kalimat biasa.";
        String strBase64TanpaTer = Base64.getEncoder().encodeToString(teksTanpaTer.getBytes());
        String resultTanpaTer = controller.palingTer(strBase64TanpaTer);
        assertEquals("Tidak ditemukan kata yang berawalan 'ter'.", resultTanpaTer);
    }
}