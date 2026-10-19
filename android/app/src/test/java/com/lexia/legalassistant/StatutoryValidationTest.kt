package com.lexia.legalassistant

import com.lexia.legalassistant.data.model.ScopeChoice
import com.lexia.legalassistant.data.repository.DynamicStatutoryDirectory
import com.lexia.legalassistant.data.repository.LegalArticlesRepository
import com.lexia.legalassistant.data.repository.LegalOntologyDirectory
import org.junit.Assert.*
import org.junit.Test

/**
 * Suite de validación automatizada de exactitud estatutaria para todos los libros de LexIA.
 * Verifica que las consultas a cada ley resuelvan al artículo exacto según el DOF / POET,
 * eliminando desviaciones por rangos (como el desvío previo de Art. 141 vs 153 en CPET).
 */
class StatutoryValidationTest {

    // ==================== 1. VALIDACIÓN CPET (CÓDIGO PENAL TABASCO) ====================

    @Test
    fun testCpetEstuproResolvesToArticle153() {
        // En el CPET de Tabasco, Estupro es el Artículo 153 (NO el 141)
        val article = LegalArticlesRepository.findBestMatchingArticle("estupro", "cpet")
        assertNotNull("No se encontró artículo para 'estupro' en CPET", article)
        assertEquals("cpet_153", article!!.id)
        assertTrue(article.topicTitle.contains("Estupro", ignoreCase = true))

        val directDetail = DynamicStatutoryDirectory.getArticle("cpet", 153)
        assertNotNull(directDetail)
        assertTrue(directDetail!!.summary.contains("desarrollo psicosexual", ignoreCase = true))
    }

    @Test
    fun testCpetPrivacionLibertadResolvesToArticle141() {
        val article = LegalArticlesRepository.findBestMatchingArticle("privacion de la libertad", "cpet")
        assertNotNull(article)
        assertEquals("cpet_141", article!!.id)
        assertTrue(article.topicTitle.contains("Privación Ilegal", ignoreCase = true))
    }

    @Test
    fun testCpetFeminicidioResolvesToArticle115() {
        val article = LegalArticlesRepository.findBestMatchingArticle("feminicidio", "cpet")
        assertNotNull(article)
        assertEquals("cpet_115", article!!.id)
        assertTrue(article.topicTitle.contains("Feminicidio", ignoreCase = true))
    }

    @Test
    fun testCpetAbigeatoResolvesToArticle205() {
        val article = LegalArticlesRepository.findBestMatchingArticle("abigeato", "cpet")
        assertNotNull(article)
        assertEquals("cpet_205", article!!.id)
        assertTrue(article.topicTitle.contains("Abigeato", ignoreCase = true))
    }

    // ==================== 2. VALIDACIÓN CPF (CÓDIGO PENAL FEDERAL) ====================

    @Test
    fun testCpfEstuproResolvesToArticle262() {
        val article = LegalArticlesRepository.findBestMatchingArticle("estupro", "cpf")
        assertNotNull(article)
        assertEquals("cpf_262", article!!.id)
        assertTrue(article.topicTitle.contains("Estupro", ignoreCase = true))
    }

    @Test
    fun testCpfHomicidioResolvesToArticle302() {
        val article = LegalArticlesRepository.findBestMatchingArticle("homicidio", "cpf")
        assertNotNull(article)
        assertEquals("cpf_302", article!!.id)
    }

    @Test
    fun testCpfRoboResolvesToArticle367() {
        val article = LegalArticlesRepository.findBestMatchingArticle("robo", "cpf")
        assertNotNull(article)
        assertEquals("cpf_367", article!!.id)
    }

    @Test
    fun testCpfFraudeResolvesToArticle386() {
        val article = LegalArticlesRepository.findBestMatchingArticle("fraude", "cpf")
        assertNotNull(article)
        assertEquals("cpf_386", article!!.id)
    }

    // ==================== 3. VALIDACIÓN LFT (LEY FEDERAL DEL TRABAJO) ====================

    @Test
    fun testLftDespidoInjustificadoResolvesToArticle48() {
        val article = LegalArticlesRepository.findBestMatchingArticle("despido injustificado", "lft")
        assertNotNull(article)
        assertEquals("lft_48", article!!.id)
        assertTrue(article.topicTitle.contains("Despido Injustificado", ignoreCase = true))
        assertTrue(article.summary.contains("tres meses de salario", ignoreCase = true))
    }

    @Test
    fun testLftVacacionesResolvesToArticle76() {
        val article = LegalArticlesRepository.findBestMatchingArticle("vacaciones", "lft")
        assertNotNull(article)
        assertEquals("lft_76", article!!.id)
        assertTrue(article.topicTitle.contains("Vacaciones", ignoreCase = true))
        assertTrue(article.summary.contains("doce días", ignoreCase = true))
    }

    @Test
    fun testLftAguinaldoResolvesToArticle87() {
        val article = LegalArticlesRepository.findBestMatchingArticle("aguinaldo", "lft")
        assertNotNull(article)
        assertEquals("lft_87", article!!.id)
        assertTrue(article.topicTitle.contains("Aguinaldo", ignoreCase = true))
        assertTrue(article.summary.contains("quince días de salario", ignoreCase = true))
    }

    @Test
    fun testLftUtilidadesResolvesToArticle117() {
        val article = LegalArticlesRepository.findBestMatchingArticle("utilidades", "lft")
        assertNotNull(article)
        assertEquals("lft_117", article!!.id)
        assertTrue(article.summary.contains("10%", ignoreCase = true))
    }

    // ==================== 4. VALIDACIÓN CCF (CÓDIGO CIVIL FEDERAL) ====================

    @Test
    fun testCcfDivorcioResolvesToArticle266() {
        val article = LegalArticlesRepository.findBestMatchingArticle("divorcio", "ccf")
        assertNotNull(article)
        assertEquals("ccf_266", article!!.id)
        assertTrue(article.topicTitle.contains("Divorcio", ignoreCase = true))
    }

    @Test
    fun testCcfAlimentosResolvesToArticle301() {
        val article = LegalArticlesRepository.findBestMatchingArticle("alimentos", "ccf")
        assertNotNull(article)
        assertEquals("ccf_301", article!!.id)
        assertTrue(article.topicTitle.contains("Alimentaria", ignoreCase = true) || article.topicTitle.contains("Alimentos", ignoreCase = true))
    }

    @Test
    fun testCcfUsucapionResolvesToArticle1135() {
        val article = LegalArticlesRepository.findBestMatchingArticle("usucapion", "ccf")
        assertNotNull(article)
        assertEquals("ccf_1135", article!!.id)
        assertTrue(article.topicTitle.contains("Usucapión", ignoreCase = true) || article.summary.contains("usucapión", ignoreCase = true))
    }

    @Test
    fun testCcfDanoMoralResolvesToArticle1916() {
        val article = LegalArticlesRepository.findBestMatchingArticle("daño moral", "ccf")
        assertNotNull(article)
        assertEquals("ccf_1916", article!!.id)
        assertTrue(article.topicTitle.contains("Daño Moral", ignoreCase = true))
    }

    // ==================== 5. VALIDACIÓN LEY DE AMPARO ====================

    @Test
    fun testAmparoIndirectoResolvesToArticle107() {
        val article = LegalArticlesRepository.findBestMatchingArticle("amparo indirecto", "amparo")
        assertNotNull(article)
        assertEquals("amparo_107", article!!.id)
    }

    @Test
    fun testAmparoDirectoResolvesToArticle170() {
        val article = LegalArticlesRepository.findBestMatchingArticle("amparo directo", "amparo")
        assertNotNull(article)
        assertEquals("amparo_170", article!!.id)
    }

    @Test
    fun testAmparoSuspensionResolvesToArticle125() {
        val article = LegalArticlesRepository.findBestMatchingArticle("suspension del acto", "amparo")
        assertNotNull(article)
        assertEquals("amparo_125", article!!.id)
    }

    // ==================== 6. VALIDACIÓN CNPP ====================

    @Test
    fun testCnppFlagranciaResolvesToArticle146() {
        val article = LegalArticlesRepository.findBestMatchingArticle("flagrancia", "cnpp")
        assertNotNull(article)
        assertEquals("cnpp_146", article!!.id)
    }

    @Test
    fun testCnppImputacionResolvesToArticle311() {
        val article = LegalArticlesRepository.findBestMatchingArticle("formulacion de la imputacion", "cnpp")
        assertNotNull(article)
        assertEquals("cnpp_311", article!!.id)
    }

    // ==================== 7. VALIDACIÓN CPEUM ====================

    @Test
    fun testCpeumNacionalidadResolvesToArticle30() {
        val article = LegalArticlesRepository.findBestMatchingArticle("nacionalidad", "cpeum")
        assertNotNull(article)
        assertEquals("cpeum_30", article!!.id)
    }

    @Test
    fun testCpeumMandamientoEscritoResolvesToArticle16() {
        val article = LegalArticlesRepository.findBestMatchingArticle("mandamiento escrito", "cpeum")
        assertNotNull(article)
        assertEquals("cpeum_16", article!!.id)
    }

    // ==================== 8. VALIDACIÓN DE DESLINDE OUT-OF-SCOPE EN CHAT ====================

    @Test
    fun testQueryEstuproInCpeumDoesNotReturnArticles1To5() {
        // En la CPEUM, preguntar por "qué es estupro" debe detectar deslinde de competencia y no mandar Arts 1 a 5
        val ragResponse = LegalArticlesRepository.queryConstitutionalRag(
            userQuery = "que es estupro en el cpeum",
            scope = ScopeChoice.GENERAL_CONSTITUTION,
            lawId = "cpeum"
        )
        assertNotNull(ragResponse)
        // No debe ser Artículo 1 a 5
        assertFalse(ragResponse.text.contains("Artículo 1° del"))
        assertFalse(ragResponse.text.contains("Artículo 2° del"))
        assertFalse(ragResponse.text.contains("Artículo 3° del"))
        assertFalse(ragResponse.text.contains("Artículo 4° del"))
        assertFalse(ragResponse.text.contains("Artículo 5° del"))

        // Debe aclarar que no está en la CPEUM y remitir al CPF o CPET
        assertTrue(ragResponse.text.contains("no tipifica") || ragResponse.text.contains("no se encuentra regulado") || ragResponse.text.contains("no contempla tipos penales"))
        assertTrue(ragResponse.text.contains("262") || ragResponse.text.contains("153"))
    }

    @Test
    fun testQueryDespidoInCpeumDirectsToLft48() {
        val ragResponse = LegalArticlesRepository.queryConstitutionalRag(
            userQuery = "que procede en un despido injustificado",
            scope = ScopeChoice.GENERAL_CONSTITUTION,
            lawId = "cpeum"
        )
        assertNotNull(ragResponse)
        assertTrue(ragResponse.text.contains("48") || ragResponse.text.contains("LFT"))
    }
}
