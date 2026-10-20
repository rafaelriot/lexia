package com.lexia.legalassistant.data.ai

import com.lexia.legalassistant.data.model.AiEngineType
import com.lexia.legalassistant.data.model.LawArticleDetail
import com.lexia.legalassistant.data.model.LawItem
import com.lexia.legalassistant.data.model.ScopeChoice
import com.lexia.legalassistant.data.repository.LegalConceptDefinition

/**
 * Builds the (system, user) prompt pair sent to the remote LLM.
 * The locally retrieved statutory context (article text, FAISS chunks, cross-statute concept)
 * is injected so the model answers grounded on LexIA's corpus (RAG).
 */
object LegalPromptBuilder {

    private const val MAX_CONTEXT_CHARS = 9_000

    fun build(
        userQuery: String,
        scope: ScopeChoice,
        activeLaw: LawItem,
        activeGroupName: String?,
        matchedArticle: LawArticleDetail?,
        crossConcept: LegalConceptDefinition?,
        isOutOfScope: Boolean,
        engineType: AiEngineType
    ): Pair<String, String> {
        val system = """
Eres LexIA, asistente jurídico experto en derecho mexicano (federal y del Estado de Tabasco).
${personaFor(engineType)}

Reglas obligatorias:
1. Responde siempre en español jurídico claro y profesional.
2. Fundamenta tu respuesta prioritariamente en el CONTEXTO NORMATIVO proporcionado; cita ley y artículo.
3. Si el contexto no basta, dilo expresamente y señala qué ordenamiento mexicano debe consultarse.
4. No inventes artículos, tesis ni jurisprudencia. Si mencionas criterios de la SCJN, indica que deben verificarse en el Semanario Judicial.
5. Estructura: respuesta directa, fundamento legal, elementos o requisitos, y una nota "Importante" práctica.
6. Usa texto plano con **negritas** para títulos; evita tablas.
""".trim()

        val scopeLabel = when (scope) {
            ScopeChoice.GENERAL_CONSTITUTION -> "Búsqueda general en ${activeLaw.title} (${activeLaw.shortName})"
            ScopeChoice.GROUP_DDHH -> "Capítulo: ${activeGroupName ?: activeLaw.shortName}"
            ScopeChoice.SPECIFIC_ARTICLE -> "Artículo específico: ${matchedArticle?.articleNumber ?: "no determinado"}"
        }

        val context = buildString {
            if (crossConcept != null) {
                appendLine("[Figura jurídica identificada]")
                appendLine("Nombre: ${crossConcept.name} (${crossConcept.legalBranch})")
                appendLine("Ordenamiento competente: ${crossConcept.properLawTitle} (${crossConcept.properLawShortName}), artículo ${crossConcept.properArticleNumber}")
                appendLine("Texto/definición: ${crossConcept.definition}")
                if (crossConcept.elements.isNotEmpty()) {
                    appendLine("Elementos: " + crossConcept.elements.joinToString("; "))
                }
                if (crossConcept.sanctionsOrRemedies.isNotBlank()) appendLine("Sanción/remedio: ${crossConcept.sanctionsOrRemedies}")
                if (isOutOfScope) {
                    val reason = crossConcept.reasonWhyNotInOtherLaws[activeLaw.id]
                        ?: "La figura no se regula en ${activeLaw.shortName}."
                    appendLine("Deslinde: el usuario consulta desde ${activeLaw.shortName}, pero la figura pertenece a otro ordenamiento. $reason")
                }
                appendLine()
            }
            if (matchedArticle != null) {
                appendLine("[Artículo recuperado]")
                appendLine("${matchedArticle.lawShortName} · ${matchedArticle.articleNumber} — ${matchedArticle.topicTitle}")
                appendLine("Materia: ${matchedArticle.category} · Estatus: ${matchedArticle.dofStatus} · Última reforma: ${matchedArticle.lastReformDate}")
                appendLine("Resumen: ${matchedArticle.summary}")
                matchedArticle.chunks
                    .sortedByDescending { it.similarity }
                    .take(4)
                    .forEach { appendLine("Fragmento (${it.path}): ${it.content}") }
            }
            if (crossConcept == null && matchedArticle == null) {
                appendLine("No se recuperó ningún artículo del corpus local para esta consulta.")
            }
        }.take(MAX_CONTEXT_CHARS)

        val user = """
ORDENAMIENTO ACTIVO: ${activeLaw.title} (${activeLaw.shortName}) · ${activeLaw.jurisdiction} · ${activeLaw.category}
ALCANCE: $scopeLabel

CONTEXTO NORMATIVO RECUPERADO (RAG LexIA):
$context

PREGUNTA DEL ABOGADO:
$userQuery
""".trim()

        return system to user
    }

    private fun personaFor(engineType: AiEngineType): String = when (engineType) {
        AiEngineType.GEMINI_PRO ->
            "Enfoque: hermenéutica constitucional, principio pro persona, control de convencionalidad y criterios de la SCJN."
        AiEngineType.GEMINI_FLASH ->
            "Enfoque: dictamen ejecutivo breve para audiencia; máximo 3 puntos clave y cita directa."
        AiEngineType.CLAUDE_LITIGATION ->
            "Enfoque: estrategia de litigio, agravios, conceptos de violación, excepciones procesales y carga probatoria."
        AiEngineType.GPT4O_JURIS ->
            "Enfoque: análisis dogmático y doctrinal, ratio legis e interpretación sistemática."
        AiEngineType.GROQ_LPU ->
            "Enfoque: análisis jurídico ultra rápido, ejecutivo y fundamentado directamente en la ley aplicable con síntesis procesal de alta velocidad."
        AiEngineType.OFFLINE_FAISS ->
            "Enfoque: extracción literal del texto normativo."
    }
}
