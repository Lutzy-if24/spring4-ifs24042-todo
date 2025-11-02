package org.delcom.starter.controllers;

import java.lang.reflect.Method;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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

    // 1. informasiNim
    @Test
    void testInformasiNim_Valid() {
        String result = controller.informasiNim("11S23001");
        assertTrue(result.contains("Sarjana Informatika"));
        assertTrue(result.contains("Angkatan: 2023"));
        assertTrue(result.contains("Urutan: 1"));
    }

    @Test
    void testInformasiNim_Invalid() {
        assertTrue(controller.informasiNim("123").contains("minimal 8 karakter"));
        assertTrue(controller.informasiNim(null).contains("minimal 8 karakter"));
    }

    @Test
    void testInformasiNim_UnknownProdi() {
        assertTrue(controller.informasiNim("99X23123").contains("Unknown"));
    }

    // 2. perolehanNilai
    @Test
    void testPerolehanNilai_Valid() {
        String data = "UAS|85|40\nUTS|75|30\nPA|90|20\nK|100|10\n---\n";
        String b64 = Base64.getEncoder().encodeToString(data.getBytes());
        String result = controller.perolehanNilai(b64);
        assertTrue(result.contains("84.50"));
        assertTrue(result.contains("Total Bobot: 100%"));
        assertTrue(result.contains("Grade: B"));
    }

    @Test
    void testPerolehanNilai_CoversBobotPositive() {
        // INI YANG MENUTUP 1% TERAKHIR!
        // 80 * 100% = 80.00 → PASTI masuk if (bobot > 0)
        String data = "Tugas|80|100\n---\n";
        String b64 = Base64.getEncoder().encodeToString(data.getBytes());
        String result = controller.perolehanNilai(b64);
        assertEquals("Nilai Akhir: 80.00 (Total Bobot: 100%)\nGrade: B", result);
    }

    @Test
    void testPerolehanNilai_InvalidBase64() {
        assertThrows(IllegalArgumentException.class, () -> controller.perolehanNilai("!@#"));
    }

    @Test
    void testPerolehanNilai_EmptyData() {
        String data = "---\n";
        String b64 = Base64.getEncoder().encodeToString(data.getBytes());
        String result = controller.perolehanNilai(b64);
        assertEquals("Nilai Akhir: 0.00 (Total Bobot: 0%)\nGrade: E", result);
    }

    @Test
    void testPerolehanNilai_InvalidLine() {
        String data = "Invalid|abc|def\n---\n";
        String b64 = Base64.getEncoder().encodeToString(data.getBytes());
        String result = controller.perolehanNilai(b64);
        assertEquals("Nilai Akhir: 0.00 (Total Bobot: 0%)\nGrade: E", result);
    }

    // 3. perbedaanL
    @Test
    void testPerbedaanL_Valid() {
        String b64 = Base64.getEncoder().encodeToString("UULL".getBytes());
        String result = controller.perbedaanL(b64);
        assertTrue(result.contains("UULL -> (-2, 2)"));
        assertTrue(result.contains("DDRR -> (2, -2)"));
        assertTrue(result.contains("Perbedaan Jarak: 8"));
    }

    @Test
    void testPerbedaanL_EmptyPath() {
        String b64 = Base64.getEncoder().encodeToString("".getBytes());
        String result = controller.perbedaanL(b64);
        assertTrue(result.contains(" -> (0, 0)"));
        assertTrue(result.contains(" -> (0, 0)"));
        assertTrue(result.contains("Perbedaan Jarak: 0"));
    }

    @Test
    void testPerbedaanL_CoversAllDirections() throws Exception {
        Method reverse = HomeController.class.getDeclaredMethod("reversePath", String.class);
        Method endpoint = HomeController.class.getDeclaredMethod("calculateEndPoint", String.class);
        reverse.setAccessible(true);
        endpoint.setAccessible(true);

        assertEquals("D", reverse.invoke(controller, "U"));
        assertEquals("U", reverse.invoke(controller, "D"));
        assertEquals("R", reverse.invoke(controller, "L"));
        assertEquals("L", reverse.invoke(controller, "R"));

        assertArrayEquals(new int[]{0, 1}, (int[]) endpoint.invoke(controller, "U"));
        assertArrayEquals(new int[]{0, -1}, (int[]) endpoint.invoke(controller, "D"));
        assertArrayEquals(new int[]{-1, 0}, (int[]) endpoint.invoke(controller, "L"));
        assertArrayEquals(new int[]{1, 0}, (int[]) endpoint.invoke(controller, "R"));
    }

    // 4. palingTer
    @Test
    void testPalingTer_Valid() {
        String text = "terbaik Terbaik terbaik";
        String b64 = Base64.getEncoder().encodeToString(text.getBytes());
        String result = controller.palingTer(b64);
        assertTrue(result.contains("'terbaik'"));
        assertTrue(result.contains("muncul 3 kali"));
    }

    @Test
    void testPalingTer_NoTer() {
        String b64 = Base64.getEncoder().encodeToString("hello world".getBytes());
        assertEquals("Tidak ditemukan kata yang berawalan 'ter'.", controller.palingTer(b64));
    }

    @Test
    void testPalingTer_MultipleTer() {
        String text = "terbaik termahal terpendek";
        String b64 = Base64.getEncoder().encodeToString(text.getBytes());
        String result = controller.palingTer(b64);
        assertTrue(result.contains("muncul 1 kali"));
    }

    // Tutup calculateGrade
    @Test
    void testCalculateGrade_Coverage() throws Exception {
        Method method = HomeController.class.getDeclaredMethod("calculateGrade", double.class);
        method.setAccessible(true);
        assertEquals("A", method.invoke(controller, 90.0));
        assertEquals("B", method.invoke(controller, 80.0));
        assertEquals("C", method.invoke(controller, 70.0));
        assertEquals("D", method.invoke(controller, 60.0));
        assertEquals("E", method.invoke(controller, 50.0));
    }
}