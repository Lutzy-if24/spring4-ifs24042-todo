package org.delcom.starter.controllers;

import java.lang.reflect.Method;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HomeControllerTest {

    private HomeController controller;

    @BeforeEach
    void setUp() {
        controller = new HomeController();
    }

    // ===================================================================
    // 1. informasiNim
    // ===================================================================
    @Test
    void testInformasiNim_Valid() {
        String result = controller.informasiNim("11S23001");
        assertTrue(result.contains("Sarjana Informatika"));
        assertTrue(result.contains("Angkatan: 2023"));
        assertTrue(result.contains("Urutan: 1"));
    }

    @Test
    void testInformasiNim_InvalidLength() {
        assertTrue(controller.informasiNim("123").contains("minimal 8 karakter"));
    }

    @Test
    void testInformasiNim_Null() {
        assertTrue(controller.informasiNim(null).contains("minimal 8 karakter"));
    }

    @Test
    void testInformasiNim_UnknownProdi() {
        assertTrue(controller.informasiNim("99X23123").contains("Unknown"));
    }

    // ===================================================================
    // 2. perolehanNilai
    // ===================================================================
    @Test
    void testPerolehanNilai_Valid() {
        String data = "UAS|85|40\nUTS|75|30\nPA|90|20\nK|100|10";
        String b64 = Base64.getEncoder().encodeToString(data.getBytes());
        String result = controller.perolehanNilai(b64);
        assertTrue(result.contains("84.50"));
        assertTrue(result.contains("Grade: B"));
        assertTrue(result.contains("Total Bobot: 100%"));
    }

    @Test
    void testPerolehanNilai_FullBranchCoverage() {
        String data =
                "Valid|90|50\n" +
                "\n" +
                "Zero|80|0\n" +
                "NoPipe\n" +
                "Two|Parts\n" +
                "Bad|abc|def\n" +
                "---\n" +
                "Last|70|30";

        String b64 = Base64.getEncoder().encodeToString(data.getBytes());
        String result = controller.perolehanNilai(b64);
        assertEquals("Nilai Akhir: 66.00 (Total Bobot: 80%)\nGrade: C", result);
    }

    @Test
    void testPerolehanNilai_InvalidBase64() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.perolehanNilai("!@#invalid"));
    }

    // ===================================================================
    // 3. perbedaanL
    // ===================================================================
    @Test
    void testPerolehanL_Valid() {
        String path = "UULL";
        String b64 = Base64.getEncoder().encodeToString(path.getBytes());
        String result = controller.perbedaanL(b64);
        assertTrue(result.contains("Path Original: UULL -> (-2, 2)"));
        assertTrue(result.contains("Path Kebalikan: DDRR -> (2, -2)"));
        assertTrue(result.contains("Perbedaan Jarak: 8"));
    }

    @Test
    void testPerbedaanL_InvalidCharacters() {
        String path = "U R D L X Y Z";
        String b64 = Base64.getEncoder().encodeToString(path.getBytes());
        String result = controller.perbedaanL(b64);
        assertTrue(result.contains("Perbedaan Jarak: 0"));
    }

    @Test
    void testPerbedaanL_InvalidBase64() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.perbedaanL("not-base64!"));
    }

    // ===================================================================
    // 4. palingTer – 100% BRANCH COVERAGE (FIXED)
    // ===================================================================

    @Test
    void testPalingTer_NoTerWords() {
        String text = "hello world java spring";
        String b64 = Base64.getEncoder().encodeToString(text.getBytes());
        assertEquals("Tidak ditemukan kata yang berawalan 'ter'.", controller.palingTer(b64));
    }

    @Test
    void testPalingTer_OneTerWord() {
        String text = "terbaik";
        String b64 = Base64.getEncoder().encodeToString(text.getBytes());
        String result = controller.palingTer(b64);
        assertTrue(result.contains("'terbaik' (muncul 1 kali)"));
    }

    @Test
    void testPalingTer_MultipleTerWords_DifferentFrequency() {
        String text = "terbaik terendah terbaik terburuk terendah terbaik";
        String b64 = Base64.getEncoder().encodeToString(text.getBytes());
        String result = controller.palingTer(b64);
        assertTrue(result.contains("'terbaik' (muncul 3 kali)"));
    }

    // KRITIS: Pastikan cabang FALSE dari if (value > maxCount)
    @Test
    void testPalingTer_TwoWords_SameFrequency_EnsuresFalseBranch() {
        String text = "terbaik terburuk"; // masing-masing 1 kali
        String b64 = Base64.getEncoder().encodeToString(text.getBytes());

        String result = controller.palingTer(b64);
        assertTrue(
            result.contains("'terbaik'") || result.contains("'terburuk'"),
            "Harus memilih salah satu dari dua kata"
        );
        assertTrue(result.contains("muncul 1 kali"));
    }

    // KRITIS: Exception dari decodeBase64 di palingTer
    @Test
    void testPalingTer_InvalidBase64() {
        assertThrows(IllegalArgumentException.class, () -> {
            controller.palingTer("ini-bukan-base64!");
        });
    }

    // KRITIS: GANTI DARI DOUBLE SPACE → PAKAI TITIK → PASTI HASILKAN ""
    @Test
    void testPalingTer_EmptyWord_ForceShortCircuit_WithPunctuation() {
        String text = ".terbaik"; // titik → split("\\W+") → ["", "terbaik"]
        String b64 = Base64.getEncoder().encodeToString(text.getBytes());

        String result = controller.palingTer(b64);
        assertTrue(result.contains("'terbaik' (muncul 1 kali)"));

        // word = "" → !word.isEmpty() → false → SHORT-CIRCUIT → tercover!
    }

    // ===================================================================
    // Helper: calculateGrade (via reflection)
    // ===================================================================
    @Test
    void testCalculateGrade_AllGrades() throws Exception {
        Method method = HomeController.class.getDeclaredMethod("calculateGrade", double.class);
        method.setAccessible(true);

        assertEquals("A", method.invoke(controller, 85.0));
        assertEquals("B", method.invoke(controller, 75.0));
        assertEquals("C", method.invoke(controller, 65.0));
        assertEquals("D", method.invoke(controller, 55.0));
        assertEquals("E", method.invoke(controller, 54.9));
    }
}