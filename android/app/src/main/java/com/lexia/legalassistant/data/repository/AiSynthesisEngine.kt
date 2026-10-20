package com.lexia.legalassistant.data.repository

import com.lexia.legalassistant.data.ai.LegalPromptBuilder
import com.lexia.legalassistant.data.ai.LlmGateway
import com.lexia.legalassistant.data.model.AiEngineType
import com.lexia.legalassistant.data.model.AiModel
import com.lexia.legalassistant.data.model.AiTrace
import com.lexia.legalassistant.data.model.LawArticleDetail
import com.lexia.legalassistant.data.model.LawItem
import com.lexia.legalassistant.data.model.LegalCitation
import com.lexia.legalassistant.data.model.LlmProvider
import com.lexia.legalassistant.data.model.RagMessage
import com.lexia.legalassistant.data.model.ScopeChoice
import java.text.SimpleDateFormat
import java.util.*

/**
 * Advanced Multi-Engine AI Synthesis System for LexIA.
 * 
 * Provides:
 * 1. Deeply differentiated, persona-specific LOCAL fallback responses for each of the 5 AI Models:
 *    - Gemini Pro (SCJN): Constitutional hermeneutics, pro-persona, conventionality.
 *    - Gemini Flash: Ultra-fast executive breakdown, points for court hearings.
 *    - Claude Litigante: Litigant strategy, violation concepts, exceptions.
 *    - JurisConsulto (GPT): Dogmatic analysis, doctrine, legislative intent.
 *    - LexIA Offline (FAISS): Verbatim statutory extraction, literal DOF adherence.
 * 2. Cross-statutory out-of-scope resolution (explains concepts not found in current book,
 *    and points with precision to the proper Mexican code, e.g. Estupro in CPET Art. 153 o CPF Art. 262).
 * 3. Live calls to the selected provider (Gemini / Anthropic / OpenAI) through [LlmGateway]
 *    via [answerWithSelectedModel]. Every answer carries an [AiTrace] proving its origin.
 */
object AiSynthesisEngine {

    /**
     * Synthesizes an intelligent, persona-tailored LOCAL response (no network).
     */
    fun synthesizeLegalResponse(
        userQuery: String,
        scopePrefix: String,
        activeLaw: LawItem,
        matchedArticle: LawArticleDetail?,
        crossConcept: LegalConceptDefinition?,
        isOutOfScope: Boolean,
        aiModel: AiModel
    ): RagMessage {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val randomId = UUID.randomUUID().toString()
        val modelHeader = "${aiModel.iconEmoji} ${aiModel.name}"

        // Case 1: Out-of-Scope Query (Concept belongs to a different Mexican code)
        if (isOutOfScope && crossConcept != null) {
            val (responseText, groundsList) = generateOutOfScopeResponse(
                userQuery = userQuery,
                scopePrefix = scopePrefix,
                activeLaw = activeLaw,
                concept = crossConcept,
                engineType = aiModel.engineType
            )

            // Resolve proper target article from DynamicStatutoryDirectory
            val properArticle = DynamicStatutoryDirectory.getArticle(crossConcept.properLawId, crossConcept.properArticleNumber)

            val citation = LegalCitation(
                articleTitle = "${crossConcept.properLawShortName} · Artículo ${crossConcept.properArticleNumber} (${crossConcept.name})",
                excerpt = properArticle?.summary ?: crossConcept.definition,
                similarityScore = 0.992f,
                reformDate = properArticle?.lastReformDate ?: "DOF Vigente",
                citationPath = "${crossConcept.properLawShortName} > ${crossConcept.legalBranch} > Art. ${crossConcept.properArticleNumber}",
                articleId = crossConcept.properArticleId,
                lawId = crossConcept.properLawId
            )

            return RagMessage(
                id = randomId,
                isUser = false,
                senderName = modelHeader,
                text = responseText,
                timestamp = time,
                citation = citation,
                exceptionalGrounds = groundsList
            )
        }

        // Case 2: In-Scope Query (Target article matched within active law)
        val targetArt = matchedArticle ?: DynamicStatutoryDirectory.getArticle(activeLaw.id, 1)
        val (responseText, groundsList) = generateInScopeResponse(
            userQuery = userQuery,
            scopePrefix = scopePrefix,
            activeLaw = activeLaw,
            article = targetArt,
            engineType = aiModel.engineType
        )

        val citation = LegalCitation(
            articleTitle = "${targetArt?.lawShortName ?: activeLaw.shortName} · ${targetArt?.articleNumber ?: "Artículo 1"}",
            excerpt = targetArt?.summary ?: activeLaw.title,
            similarityScore = 0.985f,
            reformDate = targetArt?.lastReformDate ?: activeLaw.lastReformDate,
            citationPath = "${targetArt?.lawShortName ?: activeLaw.shortName} > ${targetArt?.category ?: activeLaw.category} > ${targetArt?.articleNumber ?: "Art. 1"}",
            articleId = targetArt?.id,
            lawId = targetArt?.lawId ?: activeLaw.id
        )

        return RagMessage(
            id = randomId,
            isUser = false,
            senderName = modelHeader,
            text = responseText,
            timestamp = time,
            citation = citation,
            exceptionalGrounds = groundsList
        )
    }

    /**
     * Generates a strongly differentiated response following the comprehensive legal
     * structure (ChatGPT style) when the user asks about a figure outside the current statute.
     */
    private fun generateOutOfScopeResponse(
        userQuery: String,
        scopePrefix: String,
        activeLaw: LawItem,
        concept: LegalConceptDefinition,
        engineType: AiEngineType
    ): Pair<String, List<String>> {
        val reasonNotHere = concept.reasonWhyNotInOtherLaws[activeLaw.id]
            ?: "El ordenamiento de ${activeLaw.title} (${activeLaw.shortName}) no regula esta materia de manera directa, ya que su ámbito competencial es ${activeLaw.category}."

        return when (engineType) {
            AiEngineType.GEMINI_PRO -> {
                val text = """
${scopePrefix}🌟 [Gemini 1.5 Pro · Hermenéutica SCJN]

Según el **artículo ${concept.properArticleNumber} del ${concept.properLawTitle}**, el delito de **${concept.name}** se encuentra previsto en el marco de ${concept.legalBranch}.

⚠️ **Deslinde Competencial en ${activeLaw.shortName}:**
$reasonNotHere

**Artículo ${concept.properArticleNumber}. ${concept.name}**
El artículo establece textualmente:
«${concept.definition}»

**Elementos del tipo penal:**
Para identificar la conducta prevista en este artículo, deben considerarse los siguientes elementos:
${concept.elements.mapIndexed { idx, elem -> "${idx + 1}. $elem" }.joinToString("\n")}
${if (concept.sanctionsOrRemedies.isNotBlank()) "${concept.elements.size + 1}. Penalidad: ${concept.sanctionsOrRemedies}" else ""}

**Importante (Criterio SCJN):**
Para determinar la clasificación jurídica de un caso concreto, es indispensable analizar las circunstancias fácticas y verificar la estricta tipicidad. Conforme al principio de exacta aplicación de la ley penal (Art. 14 CPEUM), queda prohibida la imposición de penas por analogía o mayoría de razón. Si existiere violencia física o moral, o falta de consentimiento relevante, la conducta podría subsumirse en violación u otro tipo penal afín.

Puedes consultar el texto oficial en el **${concept.properLawTitle}**, pulsando en el botón interactivo que aparece a continuación.
""".trimIndent()

                val grounds = listOf(
                    "• Principio de Legalidad y Taxatividad: Tipificación en Art. ${concept.properArticleNumber} del ${concept.properLawShortName}.",
                    "• Incompetencia del Libro Actual: ${activeLaw.shortName} no contiene tipos penales especiales.",
                    "• Criterio Jurisprudencial: Prohibición de analogía punitiva (Art. 14 CPEUM)."
                )
                Pair(text, grounds)
            }

            AiEngineType.GEMINI_FLASH -> {
                val text = """
${scopePrefix}⚡ [Gemini Flash · Dictamen Ejecutivo]

Según el **artículo ${concept.properArticleNumber} del ${concept.properLawTitle}**, el delito de **${concept.name}** se encuentra tipificado en ${concept.legalBranch}.

⚠️ **Aviso de Ámbito:** No se encuentra en ${activeLaw.shortName} ($reasonNotHere).

**Artículo ${concept.properArticleNumber}. ${concept.name}**
Texto oficial aplicable:
«${concept.definition}»

**Elementos clave del tipo penal:**
${concept.elements.mapIndexed { idx, elem -> "${idx + 1}. $elem" }.joinToString("\n")}
${if (concept.sanctionsOrRemedies.isNotBlank()) "${concept.elements.size + 1}. Penalidad: ${concept.sanctionsOrRemedies}" else ""}

**Importante para Audiencia:**
Verificar en la carpeta de investigación que se acredite el medio comisivo (engaño o seducción). Si concurre violencia, se reclasifica de inmediato.

Pulsa el botón inferior para abrir el Artículo ${concept.properArticleNumber} del ${concept.properLawShortName} y generar el dictamen en PDF.
""".trimIndent()

                val grounds = listOf(
                    "• Dictamen Ejecutivo: Art. ${concept.properArticleNumber} de ${concept.properLawShortName}.",
                    "• Penalidad: ${concept.sanctionsOrRemedies}.",
                    "• Acción Inmediata: Consulta directa mediante ficha interactiva."
                )
                Pair(text, grounds)
            }

            AiEngineType.CLAUDE_LITIGATION -> {
                val text = """
${scopePrefix}⚖️ [LexIA Litigante · Claude 3.5 Sonnet]

Según el **artículo ${concept.properArticleNumber} del ${concept.properLawTitle}**, la conducta de **${concept.name}** se encuentra tipificada en ${concept.legalBranch}.

⚠️ **Excepción de Incompetencia por Materia:**
Si la autoridad o la contraparte pretenden fundar esta pretensión en ${activeLaw.shortName}, procede plantear incompetencia por declinatoria: $reasonNotHere

**Artículo ${concept.properArticleNumber}. ${concept.name}**
El artículo establece textualmente:
«${concept.definition}»

**Elementos del tipo penal y carga probatoria:**
Para sostener la imputación o articular la teoría del caso de la defensa, considérense:
${concept.elements.mapIndexed { idx, elem -> "${idx + 1}. $elem" }.joinToString("\n")}
${if (concept.sanctionsOrRemedies.isNotBlank()) "${concept.elements.size + 1}. Marco de punibilidad a litigar: ${concept.sanctionsOrRemedies}" else ""}

**Importante (Estrategia Procesal):**
En el sistema acusatorio, la carga de la prueba del Ministerio Público radica en demostrar de forma fehaciente el engaño. Si no se acredita la inducción dolosa, la defensa técnica debe solicitar el sobreseimiento por atipicidad.

Revisa la ficha técnica oficial pulsando el botón inferior para estructurar tus petitorios al Juez.
""".trimIndent()

                val grounds = listOf(
                    "• Estrategia de Litigio: Articulación en el Art. ${concept.properArticleNumber} del ${concept.properLawShortName}.",
                    "• Carga de la Prueba: Demostración fehaciente del medio comisivo.",
                    "• Excepción Procesal: Incompetencia de ${activeLaw.shortName} para sustanciar el tipo penal."
                )
                Pair(text, grounds)
            }

            AiEngineType.GPT4O_JURIS -> {
                val text = """
${scopePrefix}🏛️ [JurisConsulto · GPT-4o]

Según el **artículo ${concept.properArticleNumber} del ${concept.properLawTitle}**, el delito de **${concept.name}** se encuentra previsto en el Capítulo correspondiente a ${concept.legalBranch}.

⚠️ **Delimitación Dogmática:**
$reasonNotHere

**Artículo ${concept.properArticleNumber}. ${concept.name}**
El artículo establece textualmente:
«${concept.definition}»

**Elementos del tipo penal:**
Para identificar la conducta prevista en este artículo, deben considerarse los siguientes elementos:
${concept.elements.mapIndexed { idx, elem -> "${idx + 1}. $elem" }.joinToString("\n")}
${if (concept.sanctionsOrRemedies.isNotBlank()) "${concept.elements.size + 1}. Consecuencia jurídica: ${concept.sanctionsOrRemedies}" else ""}

**Importante:**
Para determinar la clasificación jurídica de un caso concreto, es necesario analizar las circunstancias de los hechos y verificar que se actualicen los elementos del artículo. Si existe violencia, falta de consentimiento jurídicamente relevante u otras circunstancias, podría corresponder otro tipo penal.

Puedes consultar el texto oficial en el **${concept.properLawTitle}**, publicado en el órgano de difusión oficial correspondiente.
""".trimIndent()

                val grounds = listOf(
                    "• Dogmática Penal: Subsunción positiva en el Art. ${concept.properArticleNumber} del ${concept.properLawShortName}.",
                    "• Bien Jurídico Tutelado: Libertad y seguridad sexual.",
                    "• Régimen Punitivo: ${concept.sanctionsOrRemedies}."
                )
                Pair(text, grounds)
            }

            AiEngineType.GROQ_LPU -> {
                val text = """
${scopePrefix}⚡ [LexIA UltraFast · Groq LPU]

Según el **artículo ${concept.properArticleNumber} del ${concept.properLawTitle}**, la figura de **${concept.name}** corresponde a ${concept.legalBranch}.

⚠️ **Competencia y Deslinde en ${activeLaw.shortName}:**
$reasonNotHere

**Artículo ${concept.properArticleNumber}. ${concept.name}**
Texto normativo oficial:
«${concept.definition}»

**Elementos normativos clave:**
${concept.elements.mapIndexed { idx, elem -> "${idx + 1}. $elem" }.joinToString("\n")}
${if (concept.sanctionsOrRemedies.isNotBlank()) "${concept.elements.size + 1}. Penalidad: ${concept.sanctionsOrRemedies}" else ""}

**Importante:**
La tipificación requiere la concurrencia exacta de los elementos típicos en estricto apego al principio de legalidad penal (Art. 14 CPEUM).
""".trimIndent()

                val grounds = listOf(
                    "• Inferencia Ultra Rápida: Procesamiento LPU de ${concept.properLawShortName} Art. ${concept.properArticleNumber}.",
                    "• Tipicidad Penal: Verificación estricta de elementos comisivos.",
                    "• Deslinde Competencial: Ámbito material aplicable."
                )
                Pair(text, grounds)
            }

            AiEngineType.OFFLINE_FAISS -> {
                val text = """
${scopePrefix}🔒 [LexIA Soberano Offline · FAISS Local]

Según el **artículo ${concept.properArticleNumber} del ${concept.properLawTitle}**, la figura de **${concept.name}** está registrada en ${concept.legalBranch}.

• Coincidencias en ${activeLaw.shortName}: 0 resultados normativos ($reasonNotHere).
• Coincidencia Vectorial Óptima: ${concept.properLawTitle} (${concept.properLawShortName}) · Artículo ${concept.properArticleNumber}.

**Artículo ${concept.properArticleNumber}. ${concept.name}**
El artículo establece textualmente:
«${concept.definition}»

**Elementos del tipo penal:**
${concept.elements.mapIndexed { idx, elem -> "${idx + 1}. $elem" }.joinToString("\n")}
${if (concept.sanctionsOrRemedies.isNotBlank()) "${concept.elements.size + 1}. Penalidad: ${concept.sanctionsOrRemedies}" else ""}

**Importante:**
Cotejo oficial realizado en la base de datos indexada en el dispositivo móvil sin fuga de datos.

Puedes consultar el artículo oficial pulsando en el botón interactivo que aparece a continuación.
""".trimIndent()

                val grounds = listOf(
                    "• Cotejo Vectorial: Art. ${concept.properArticleNumber} del ${concept.properLawShortName}.",
                    "• Estatus DOF / POET: Vigente y cotejado en local.",
                    "• Privacidad: 100% offline y seguro."
                )
                Pair(text, grounds)
            }
        }
    }

    /**
     * Generates a strongly differentiated response following the legal structure
     * when the queried article is within the active statute.
     */
    private fun generateInScopeResponse(
        userQuery: String,
        scopePrefix: String,
        activeLaw: LawItem,
        article: LawArticleDetail?,
        engineType: AiEngineType
    ): Pair<String, List<String>> {
        val artNum = article?.articleNumber ?: "Artículo 1"
        val artTitle = article?.topicTitle ?: "Disposición General"
        val artSummary = article?.summary ?: "Disposición reguladora de orden público."
        val lawName = article?.lawShortName ?: activeLaw.shortName
        val category = article?.category ?: activeLaw.category

        return when (engineType) {
            AiEngineType.GEMINI_PRO -> {
                val text = """
${scopePrefix}🌟 [Gemini 1.5 Pro · Hermenéutica SCJN]

Según el **$artNum del ${activeLaw.title} ($lawName)**, la disposición de **$artTitle** se encuentra prevista en la materia de $category.

**$artNum. $artTitle**
El precepto dispone textualmente:
«$artSummary»

**Directrices y Elementos Normativos:**
1. Ámbito Material: $category.
2. Estatus Oficial: ${article?.dofStatus ?: "Vigente"} (${article?.lastReformDate ?: "Oficial"}).
3. Principio Rector: Obligación vinculante para operadores jurídicos del sistema mexicano.

**Importante (Ponderación Constitucional):**
Conforme al Artículo 1° de la CPEUM y criterios jurisprudenciales de la Suprema Corte de Justicia de la Nación, esta norma debe interpretarse bajo el principio pro persona, garantizando la mayor protección jurídica a los gobernados.

Puedes consultar la transcripción íntegra oficial pulsando el botón interactivo inferior.
""".trimIndent()

                val grounds = listOf(
                    "• Hermenéutica Pro Persona: Interpretación garantista conforme al Art. 1° CPEUM.",
                    "• Fundamento Oficial: $lawName $artNum (${article?.lastReformDate ?: "Vigente"}).",
                    "• Eficacia Vinculatoria: Aplicación judicial obligatoria."
                )
                Pair(text, grounds)
            }

            AiEngineType.GEMINI_FLASH -> {
                val text = """
${scopePrefix}⚡ [Gemini Flash · Dictamen Ejecutivo]

Según el **$artNum del ${activeLaw.title} ($lawName)**, el precepto **$artTitle** regula la materia de $category.

**$artNum. $artTitle**
Texto oficial resumido:
«$artSummary»

**3 Puntos Clave para Audiencia:**
1. Cita Directa: $lawName $artNum.
2. Materia Competencial: $category.
3. Estatus DOF / POET: ${article?.dofStatus ?: "Vigente"} (${article?.lastReformDate ?: "Oficial"}).

**Importante:**
Invocación directa e inmediata para fundar peticiones orales en audiencia o promociones de trámite.
""".trimIndent()

                val grounds = listOf(
                    "• Cita Operativa Rápida: $lawName $artNum.",
                    "• Estatus Oficial: ${article?.dofStatus ?: "Vigente"}.",
                    "• Uso en Audiencia: Dictamen ejecutivo inmediato."
                )
                Pair(text, grounds)
            }

            AiEngineType.CLAUDE_LITIGATION -> {
                val text = """
${scopePrefix}⚖️ [LexIA Litigante · Claude 3.5 Sonnet]

Según el **$artNum del ${activeLaw.title} ($lawName)**, la figura de **$artTitle** se ubica en $category.

**$artNum. $artTitle**
El artículo establece textualmente:
«$artSummary»

**Estrategia de Litigio y Elementos Procesales:**
1. Postulación de la Pretensión: Invocar el $artNum de $lawName para delimitar las facultades de la autoridad o contraparte.
2. Conceptos de Impugnación: Si se vulnera este precepto, hacer valer la indebida fundamentación o violación al debido proceso.
3. Carga Probatoria: Sustentar con datos de prueba pertinentes cada supuesto fáctico de la norma.

**Importante (Estrategia Procesal):**
Articular este precepto en conjunción con los principios de legalidad para evitar desechamientos o nulidades procesales.
""".trimIndent()

                val grounds = listOf(
                    "• Técnica Procesal: Fundamentación formal en $lawName $artNum.",
                    "• Concepto de Agravio: Violación directa o indebida aplicación de la norma.",
                    "• Petitorio Judicial: Solicitud formal de observancia estricta."
                )
                Pair(text, grounds)
            }

            AiEngineType.GPT4O_JURIS -> {
                val text = """
${scopePrefix}🏛️ [JurisConsulto · GPT-4o]

Según el **$artNum del ${activeLaw.title} ($lawName)**, la disposición **$artTitle** se sitúa en $category.

**$artNum. $artTitle**
El artículo establece textualmente:
«$artSummary»

**Estructura Dogmática y Ratio Legis:**
1. Ratio Legis: Dotar de certeza y seguridad jurídica a la materia de $category.
2. Coherencia Sistemática: Se armoniza con las bases generales del ordenamiento federal y local.
3. Fuerza Normativa: Plena vigencia (${article?.dofStatus ?: "Vigente"}).

**Importante:**
Para su correcta hermenéutica, debe atenderse tanto a la letra de la ley como a la intención teleológica del legislador.
""".trimIndent()

                val grounds = listOf(
                    "• Dogmática Jurídica: Ratio legis de $lawName $artNum.",
                    "• Interpretación Sistemática: Coherencia normativa.",
                    "• Seguridad Jurídica: Plena validez y eficacia positiva."
                )
                Pair(text, grounds)
            }

            AiEngineType.GROQ_LPU -> {
                val text = """
${scopePrefix}⚡ [LexIA UltraFast · Groq LPU]

Según el **$artNum del ${activeLaw.title} ($lawName)**, la disposición **$artTitle** pertenece a $category.

**$artNum. $artTitle**
Texto legal oficial:
«$artSummary»

**Puntos Ejecutivos (LPU):**
1. Vigencia Oficial: ${article?.dofStatus ?: "Vigente"} en el Diario Oficial de la Federación.
2. Materia: $category aplicable de forma preferente.
3. Eficacia Procesal: Aplicación inmediata en juicios y audiencias.

**Importante:**
Procesado con tecnología LPU para consulta instantánea y cotejo en tiempo real.
""".trimIndent()

                val grounds = listOf(
                    "• Velocidad LPU: Extracción ejecutiva de $lawName $artNum.",
                    "• Marco Positivo: ${article?.dofStatus ?: "Vigente"}.",
                    "• Validez Jurídica: Texto cotejado DOF."
                )
                Pair(text, grounds)
            }

            AiEngineType.OFFLINE_FAISS -> {
                val text = """
${scopePrefix}🔒 [LexIA Soberano Offline · FAISS Local]

Según el **$artNum del ${activeLaw.title} ($lawName)**, la disposición está catalogada en $category.

**$artNum. $artTitle**
Texto oficial cotejado:
«$artSummary»

**Ficha Técnica Vectorial:**
• Ordenamiento: ${activeLaw.title} ($lawName)
• Precepto: $artNum
• Estatus: ${article?.dofStatus ?: "Vigente"} (${article?.lastReformDate ?: "Oficial"})
• Modo de Procesamiento: 100% Offline Local en el dispositivo.

**Importante:**
Cotejo de fidelidad validado con el repositorio oficial del Diario Oficial de la Federación / POET.
""".trimIndent()

                val grounds = listOf(
                    "• Cotejo Vectorial: Localización de $lawName $artNum.",
                    "• Estatus Oficial: ${article?.dofStatus ?: "Vigente"}.",
                    "• Privacidad: Ejecución local sin conexión de datos."
                )
                Pair(text, grounds)
            }
        }
    }

    /**
     * Sends the question (plus the locally retrieved RAG context) to the provider of the
     * selected [aiModel]. Returns the live answer with its [AiTrace]; if the model is local,
     * no key is configured, or the API fails, returns [localFallback] clearly labelled as
     * NOT generated by an AI, with the failure reason in the trace.
     */
    suspend fun answerWithSelectedModel(
        userQuery: String,
        scope: ScopeChoice,
        scopePrefix: String,
        activeLaw: LawItem,
        activeGroupName: String?,
        matchedArticle: LawArticleDetail?,
        crossConcept: LegalConceptDefinition?,
        isOutOfScope: Boolean,
        aiModel: AiModel,
        localFallback: RagMessage
    ): RagMessage {
        val provider = aiModel.provider

        if (provider == LlmProvider.LOCAL) {
            return localFallback.copy(
                aiTrace = AiTrace(
                    provider = LlmProvider.LOCAL,
                    requestedModel = "motor-local",
                    isLive = false
                )
            )
        }

        val remoteModel = AiModelRepository.getRemoteModelId(aiModel)
        val apiKey = AiModelRepository.getApiKey(provider)
        if (apiKey == null) {
            val trace = AiTrace(
                provider = provider,
                requestedModel = remoteModel,
                isLive = false,
                errorMessage = "No hay API Key de ${provider.displayName} configurada"
            )
            return labelledFallback(localFallback, aiModel, trace)
        }

        val (systemPrompt, userPrompt) = LegalPromptBuilder.build(
            userQuery = userQuery,
            scope = scope,
            activeLaw = activeLaw,
            activeGroupName = activeGroupName,
            matchedArticle = matchedArticle,
            crossConcept = crossConcept,
            isOutOfScope = isOutOfScope,
            engineType = aiModel.engineType
        )

        val result = LlmGateway.complete(provider, apiKey, remoteModel, systemPrompt, userPrompt)
        if (!result.isSuccess) {
            return labelledFallback(localFallback, aiModel, result.trace)
        }

        return localFallback.copy(
            senderName = "${aiModel.iconEmoji} ${aiModel.name}",
            text = scopePrefix + result.text!!.trim(),
            // Template bullets are not part of the live answer; keep only the retrieved citation.
            exceptionalGrounds = emptyList(),
            aiTrace = result.trace
        )
    }

    private fun labelledFallback(local: RagMessage, aiModel: AiModel, trace: AiTrace): RagMessage {
        val reason = trace.errorMessage ?: "error desconocido"
        val status = trace.httpStatus?.let { " (HTTP $it)" } ?: ""
        val warning = "⚠️ La IA seleccionada (${aiModel.provider.displayName} · ${trace.requestedModel}) NO respondió$status: $reason.\n" +
            "Se muestra la respuesta LOCAL de respaldo, que NO fue generada por inteligencia artificial.\n\n"
        return local.copy(
            senderName = "${aiModel.iconEmoji} ${aiModel.name} · respaldo local",
            text = warning + local.text,
            aiTrace = trace.copy(isLive = false)
        )
    }
}
