package com.lexia.legalassistant

import com.lexia.legalassistant.data.ai.LegalPromptBuilder
import com.lexia.legalassistant.data.ai.LlmGateway
import com.lexia.legalassistant.data.model.AiEngineType
import com.lexia.legalassistant.data.model.AiModel
import com.lexia.legalassistant.data.model.AiTrace
import com.lexia.legalassistant.data.model.LawArticleDetail
import com.lexia.legalassistant.data.model.LawItem
import com.lexia.legalassistant.data.model.LlmProvider
import com.lexia.legalassistant.data.model.ScopeChoice
import com.lexia.legalassistant.data.model.VectorChunk
import com.lexia.legalassistant.data.repository.AiModelRepository
import com.lexia.legalassistant.data.repository.AiSynthesisEngine
import com.lexia.legalassistant.data.repository.LegalConceptDefinition
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiGatewayIntegrationTest {

    @Test
    fun testProviderMappingForEveryAvailableModel() {
        val models = AiModelRepository.availableModels
        assertEquals(6, models.size)

        val geminiPro = models.find { it.id == "gemini-pro-scjn" }!!
        assertEquals(LlmProvider.GEMINI, geminiPro.provider)
        assertEquals("gemini-3.8-flash", geminiPro.defaultRemoteModelId)

        val geminiFlash = models.find { it.id == "gemini-flash-speed" }!!
        assertEquals(LlmProvider.GEMINI, geminiFlash.provider)
        assertEquals("gemini-3.8-flash", geminiFlash.defaultRemoteModelId)

        val groq = models.find { it.id == "groq-lpu-ultra" }!!
        assertEquals(LlmProvider.GROQ, groq.provider)
        assertEquals("openai/gpt-oss-120b", groq.defaultRemoteModelId)

        val claude = models.find { it.id == "claude-litigation" }!!
        assertEquals(LlmProvider.ANTHROPIC, claude.provider)
        assertEquals("claude-sonnet-4-5", claude.defaultRemoteModelId)

        val gpt = models.find { it.id == "gpt4o-jurisconsult" }!!
        assertEquals(LlmProvider.OPENAI, gpt.provider)
        assertEquals("gpt-4o", gpt.defaultRemoteModelId)

        val offline = models.find { it.id == "lexia-offline-faiss" }!!
        assertEquals(LlmProvider.LOCAL, offline.provider)
        assertTrue(offline.isOfflineCapable)

        assertEquals(AiModelRepository.defaultGroqApiKey, AiModelRepository.getApiKey(LlmProvider.GROQ))
    }

    @Test
    fun testGatewayClientsResolution() {
        assertNotNull(LlmGateway.clientFor(LlmProvider.GEMINI))
        assertNotNull(LlmGateway.clientFor(LlmProvider.GROQ))
        assertNotNull(LlmGateway.clientFor(LlmProvider.ANTHROPIC))
        assertNotNull(LlmGateway.clientFor(LlmProvider.OPENAI))
        assertNull(LlmGateway.clientFor(LlmProvider.LOCAL))
    }

    @Test
    fun testLocalProviderNeverPerformsNetworkCall() = runBlocking {
        val result = LlmGateway.complete(
            provider = LlmProvider.LOCAL,
            apiKey = "",
            model = "motor-local",
            systemPrompt = "sys",
            userPrompt = "user"
        )
        assertFalse(result.isSuccess)
        assertFalse(result.trace.isLive)
        assertEquals(LlmProvider.LOCAL, result.trace.provider)
        assertTrue(result.trace.errorMessage?.contains("Proveedor local") == true)
    }

    @Test
    fun testLegalPromptBuilderInjectsRagContext() {
        val dummyLaw = LawItem(
            id = "cpeum",
            title = "Constitución Política de los Estados Unidos Mexicanos",
            shortName = "CPEUM"
        )
        val dummyArticle = LawArticleDetail(
            id = "cpeum_16",
            lawId = "cpeum",
            lawShortName = "CPEUM",
            articleNumber = "Artículo 16",
            topicTitle = "Garantía de Legalidad y Mandamiento Escrito",
            category = "Constitucional",
            summary = "Nadie puede ser molestado en su persona sino en virtud de mandamiento escrito.",
            chunks = listOf(
                VectorChunk(
                    chunkId = "c1",
                    chunkIndex = 0,
                    similarity = 0.98f,
                    path = "CPEUM > Art 16",
                    content = "Texto íntegro del primer párrafo del artículo 16 constitucional."
                )
            )
        )

        val (sysPrompt, userPrompt) = LegalPromptBuilder.build(
            userQuery = "¿Cuáles son los requisitos de una orden de aprehensión?",
            scope = ScopeChoice.SPECIFIC_ARTICLE,
            activeLaw = dummyLaw,
            activeGroupName = null,
            matchedArticle = dummyArticle,
            crossConcept = null,
            isOutOfScope = false,
            engineType = AiEngineType.CLAUDE_LITIGATION
        )

        assertTrue(sysPrompt.contains("LexIA"))
        assertTrue(sysPrompt.contains("estrategia de litigio")) // Persona de Claude Litigante
        assertTrue(userPrompt.contains("Artículo 16"))
        assertTrue(userPrompt.contains("Garantía de Legalidad"))
        assertTrue(userPrompt.contains("Texto íntegro del primer párrafo"))
        assertTrue(userPrompt.contains("¿Cuáles son los requisitos de una orden de aprehensión?"))
    }

    @Test
    fun testLegalPromptBuilderOutOfScopeConceptRouting() {
        val dummyLaw = LawItem(
            id = "cpeum",
            title = "Constitución Política",
            shortName = "CPEUM"
        )
        val crossConcept = LegalConceptDefinition(
            id = "estupro_test",
            name = "Estupro",
            keywords = listOf("estupro"),
            legalBranch = "Derecho Penal Sustantivo",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 262,
            properArticleId = "cpf_262",
            definition = "Al que tenga cópula con persona menor de dieciocho años...",
            elements = listOf("Cópula consentida", "Víctima menor de 18 años", "Engaño o seducción"),
            sanctionsOrRemedies = "Prisión de 6 meses a 4 años",
            reasonWhyNotInOtherLaws = mapOf("cpeum" to "La CPEUM consagra garantías fundamentales y no tipos penales específicos.")
        )

        val (_, userPrompt) = LegalPromptBuilder.build(
            userQuery = "¿Qué es el estupro según la ley?",
            scope = ScopeChoice.GENERAL_CONSTITUTION,
            activeLaw = dummyLaw,
            activeGroupName = null,
            matchedArticle = null,
            crossConcept = crossConcept,
            isOutOfScope = true,
            engineType = AiEngineType.GEMINI_PRO
        )

        assertTrue(userPrompt.contains("Código Penal Federal"))
        assertTrue(userPrompt.contains("artículo 262"))
        assertTrue(userPrompt.contains("La CPEUM consagra garantías fundamentales"))
    }

    @Test
    fun testAiTraceLogLineFormatting() {
        val traceLive = AiTrace(
            provider = LlmProvider.ANTHROPIC,
            requestedModel = "claude-sonnet-4-5",
            isLive = true,
            responseModel = "claude-sonnet-4-5-20250929",
            httpStatus = 200,
            latencyMs = 1420,
            requestId = "req_01XYZ",
            inputTokens = 650,
            outputTokens = 420
        )
        val line = traceLive.toLogLine()
        assertTrue(line.contains("anthropic"))
        assertTrue(line.contains("HTTP 200"))
        assertTrue(line.contains("1420 ms"))
        assertTrue(line.contains("tokens 650→420"))
        assertTrue(line.contains("req req_01XYZ"))
    }
}
