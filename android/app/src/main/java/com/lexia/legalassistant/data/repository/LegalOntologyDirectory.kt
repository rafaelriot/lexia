package com.lexia.legalassistant.data.repository

import com.lexia.legalassistant.data.model.LawArticleDetail

/**
 * Data class representing a specialized legal concept or figure across
 * the Mexican Statutory System (Penal, Procesal, Constitucional, Amparo, Civil, Laboral).
 */
data class LegalConceptDefinition(
    val id: String,
    val name: String,
    val keywords: List<String>,
    val legalBranch: String,
    val properLawId: String,
    val properLawShortName: String,
    val properLawTitle: String,
    val properArticleNumber: Int,
    val properArticleId: String,
    val definition: String,
    val elements: List<String>,
    val sanctionsOrRemedies: String,
    val reasonWhyNotInOtherLaws: Map<String, String> = emptyMap()
)

/**
 * High-precision ontological knowledge directory that resolves cross-statutory figures,
 * detects Out-of-Scope queries (e.g. asking for "estupro" in the Constitution),
 * and routes questions to the appropriate Mexican statute.
 */
object LegalOntologyDirectory {

    val concepts: List<LegalConceptDefinition> = listOf(
        // ==================== MATERIA PENAL SUSTANTIVA (CPF / CPET) ====================
        LegalConceptDefinition(
            id = "estupro_tabasco",
            name = "Delito de Estupro en el Estado de Tabasco",
            keywords = listOf("estupro en tabasco", "estupro tabasco", "estupro cpet", "tabasco estupro", "tipo penal de estupro en tabasco"),
            legalBranch = "Derecho Penal Estatal (Tabasco - Delitos contra la Seguridad Sexual)",
            properLawId = "cpet",
            properLawShortName = "CP Tabasco",
            properLawTitle = "Código Penal para el Estado de Tabasco",
            properArticleNumber = 153,
            properArticleId = "cpet_153",
            definition = "Al que por medio del engaño tenga cópula con mujer mayor de catorce y menor de dieciocho años que no haya alcanzado su normal desarrollo psicosexual, se le aplicará prisión de cuatro a seis años.",
            elements = listOf(
                "Sujeto activo: la persona que realiza la conducta.",
                "Sujeto pasivo: una mujer mayor de 14 años y menor de 18 años.",
                "Conducta: tener cópula con la persona referida.",
                "Medio comisivo: el engaño.",
                "Elemento adicional: que la víctima no haya alcanzado su normal desarrollo psicosexual.",
                "Penalidad: de cuatro a seis años de prisión."
            ),
            sanctionsOrRemedies = "Prisión de cuatro a seis años (Artículo 153 del Código Penal para el Estado de Tabasco).",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La Constitución Política (CPEUM) no tipifica delitos en particular; la tipificación corresponde al Código Penal para el Estado de Tabasco en el fuero común.",
                "amparo" to "La Ley de Amparo es un medio de control constitucional contra actos de autoridad, no un catálogo de delitos sustantivos.",
                "cnpp" to "El CNPP regula las reglas procesales del procedimiento acusatorio, pero remite al Código Penal del Estado para las conductas sustantivas."
            )
        ),
        LegalConceptDefinition(
            id = "estupro",
            name = "Delito de Estupro (Código Penal Federal)",
            keywords = listOf("estupro", "delito de estupro", "seduccion", "seducción", "engaño a menor", "copula con menor"),
            legalBranch = "Derecho Penal Sustantivo Federal (Delitos contra la Libertad y Normal Desarrollo Psicosexual)",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 262,
            properArticleId = "cpf_262",
            definition = "Comete el delito de estupro quien tenga cópula con una persona mayor de quince y menor de dieciocho años de edad, obteniendo su consentimiento mediante seducción o engaño.",
            elements = listOf(
                "Sujeto Activo: Cualquier persona imputable.",
                "Sujeto Pasivo Calificado: Persona mayor de 15 años y menor de 18 años.",
                "Conducta Típica: Acceso carnal (cópula).",
                "Elemento Subjetivo / Vicio del Consentimiento: Consentimiento viciado obtenido mediante seducción o engaño."
            ),
            sanctionsOrRemedies = "Prisión de uno a cuatro años (Art. 262 CPF) y medidas de reparación integral del daño psicológico a la víctima.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La Constitución Política (CPEUM) no tipifica delitos particulares ni contiene tipos penales especiales; su función primordial es consagrar derechos humanos, garantías procesales y la organización política del Estado.",
                "amparo" to "La Ley de Amparo es un ordenamiento procesal constitucional para combatir actos u omisiones de autoridad, no un catálogo de tipos penales sustantivos.",
                "cnpp" to "El CNPP regula las reglas procesales del juicio oral (audiencias, medidas cautelares, pruebas), pero remite al Código Penal para los delitos sustantivos."
            )
        ),
        LegalConceptDefinition(
            id = "abuso_sexual",
            name = "Delito de Abuso Sexual",
            keywords = listOf("abuso sexual", "tocamientos", "tocamiento", "actos lascivos"),
            legalBranch = "Derecho Penal Sustantivo",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 260,
            properArticleId = "cpf_260",
            definition = "Comete el delito de abuso sexual quien ejecute en una persona sin su consentimiento, o la obligue a ejecutar para sí o en otra persona, actos sexuales sin llegar a la cópula.",
            elements = listOf(
                "Conducta: Ejecución de actos de contenido sexual sin llegar a la cópula.",
                "Ausencia de consentimiento de la víctima o empleo de violencia física o moral.",
                "Pena agravada si la víctima es menor de quince años o incapaz (Art. 261)."
            ),
            sanctionsOrRemedies = "Prisión de uno a seis años y multa (Art. 260 CPF); si es menor de 15 años, de seis a trece años de prisión.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La CPEUM no define tipos penales en su texto; estos se reservan a los Códigos Penales Federal y locales.",
                "amparo" to "La Ley de Amparo no tipifica conductas delictivas entre particulares."
            )
        ),
        LegalConceptDefinition(
            id = "violacion",
            name = "Delito de Violación",
            keywords = listOf("violacion", "violación", "violacion equiparada", "violación equiparada", "agresion sexual violenta"),
            legalBranch = "Derecho Penal Sustantivo",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 265,
            properArticleId = "cpf_265",
            definition = "Comete el delito de violación quien por medio de la violencia física o moral realice cópula con persona de cualquier sexo. Se equipara cuando se realice con persona que no tenga capacidad de comprender o resistir el hecho.",
            elements = listOf(
                "Conducta: Acceso carnal (cópula vaginal, anal o bucal).",
                "Medio Comisivo: Violencia física o coacción moral grave.",
                "Violación Equiparada (Art. 266): Víctima privada de razón, sentido o con imposibilidad de resistir."
            ),
            sanctionsOrRemedies = "Prisión de ocho a veinte años (Art. 265 CPF), incrementable hasta en una mitad por agravantes de parentesco o cargo público (Art. 267).",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La CPEUM únicamente menciona delitos en relación con la prisión preventiva oficiosa (Art. 19), pero su tipificación y penalidad competen al Código Penal.",
                "amparo" to "La Ley de Amparo tutela las garantías constitucionales del imputado o de la víctima, pero no tipifica el delito."
            )
        ),
        LegalConceptDefinition(
            id = "homicidio",
            name = "Delito de Homicidio",
            keywords = listOf("homicidio", "asesinato", "privar de la vida", "homicidio calificado", "homicidio culposo"),
            legalBranch = "Derecho Penal Sustantivo (Vida e Integridad)",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 302,
            properArticleId = "cpf_302",
            definition = "Comete el delito de homicidio el que priva de la vida a otro. Puede ser simple, doloso, culposo o calificado por premeditación, alevosía, ventaja o traición.",
            elements = listOf(
                "Conducta: Acción u omisión que provoca la muerte de un ser humano.",
                "Resultado Material: Muerte comprobada de la víctima.",
                "Nexo Causal: Relación directa entre la conducta del agente y el fallecimiento.",
                "Calificativas (Arts. 315-320): Premeditación, ventaja, alevosía y traición."
            ),
            sanctionsOrRemedies = "Homicidio simple: de 12 a 24 años de prisión (Art. 307 CPF); Homicidio calificado: de 30 a 60 años de prisión (Art. 320 CPF).",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El homicidio doloso es causa de prisión preventiva oficiosa según el Art. 19 CPEUM, pero la definición y penalidad radican en el Código Penal.",
                "amparo" to "La materia penal sustantiva está codificada en el CPF y los códigos de los Estados."
            )
        ),
        LegalConceptDefinition(
            id = "feminicidio",
            name = "Delito de Feminicidio",
            keywords = listOf("feminicidio", "razones de genero", "violencia contra la mujer"),
            legalBranch = "Derecho Penal Sustantivo",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 325,
            properArticleId = "cpf_325",
            definition = "Comete el delito de feminicidio quien prive de la vida a una mujer por razones de género. Se configuran razones de género cuando concurran signos de violencia sexual, lesiones infamantes, antecedentes de violencia o exposición del cuerpo.",
            elements = listOf(
                "Sujeto Pasivo: Mujer.",
                "Privación de la vida motivada por razones de género objetivamente acreditables.",
                "Presencia de violencia previa, amenazas, relación afectiva o incomunicación."
            ),
            sanctionsOrRemedies = "Prisión de cuarenta a sesenta años y de quinientos a mil días multa (Art. 325 CPF).",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La Constitución consagra la igualdad de género y no discriminación (Art. 1° y 4°), pero no contiene tipos penales autónomos."
            )
        ),
        LegalConceptDefinition(
            id = "robo",
            name = "Delito de Robo",
            keywords = listOf("robo", "delito de robo", "apoderamiento ilicito", "robo con violencia"),
            legalBranch = "Derecho Penal Sustantivo (Patrimonio)",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 367,
            properArticleId = "cpf_367",
            definition = "Comete el delito de robo el que se apodera de una cosa mueble ajena, sin derecho y sin consentimiento de la persona que puede disponer de ella con arreglo a la ley.",
            elements = listOf(
                "Apoderamiento material de un bien mueble.",
                "Ajenidad de la cosa mueble sustraída.",
                "Ausencia de derecho y falta de consentimiento legítimo.",
                "Ánimo de apropiación y lucro."
            ),
            sanctionsOrRemedies = "Penas graduales de acuerdo al valor de lo robado y agravantes por violencia o lugar cerrado (Arts. 368-381 CPF).",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El robo es un delito patrimonial ordinario regulado exclusivamente en los códigos penales sustantivos."
            )
        ),
        LegalConceptDefinition(
            id = "fraude",
            name = "Delito de Fraude",
            keywords = listOf("fraude", "delito de fraude", "engano patrimonial", "engaño patrimonial", "lucro indebido"),
            legalBranch = "Derecho Penal Sustantivo (Patrimonio)",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 386,
            properArticleId = "cpf_386",
            definition = "Comete el delito de fraude el que engañando a uno o aprovechándose del error en que éste se halla, se hace ilícitamente de alguna cosa o alcanza un lucro indebido.",
            elements = listOf(
                "Engaño o aprovechamiento del error ajeno.",
                "Disposición patrimonial perjudicial por parte del sujeto pasivo.",
                "Obtención de un lucro indebido o apoderamiento ilícito por el sujeto activo.",
                "Nexo causal directo entre el ardid y la merma patrimonial."
            ),
            sanctionsOrRemedies = "Prisión proporcional al monto de lo defraudado conforme al Artículo 386 del CPF.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La CPEUM prohíbe la prisión por deudas de carácter puramente civil (Art. 17), pero remite la estafa y el fraude al Código Penal."
            )
        ),
        LegalConceptDefinition(
            id = "despojo",
            name = "Delito de Despojo de Inmuebles o Aguas",
            keywords = listOf("despojo", "invasion de terreno", "despojo de inmueble", "despojo de tierras"),
            legalBranch = "Derecho Penal Sustantivo",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 395,
            properArticleId = "cpf_395",
            definition = "Comete el delito de despojo quien por propia autoridad y haciendo violencia o furtivamente, o empleando amenaza o engaño, ocupe un inmueble ajeno o haga uso de él o de un derecho real que no le pertenezca.",
            elements = listOf(
                "Bien Jurídico: La posesión pacífica de bienes inmuebles o el uso de aguas.",
                "Medios Comisivos: Violencia, furtividad, amenazas o engaño.",
                "Ocupación ilegítima o privación de la posesión legítima."
            ),
            sanctionsOrRemedies = "Prisión de tres meses a cinco años y multa de cincuenta a quinientos pesos (Art. 395 CPF).",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La propiedad originaria y comunal se sustenta en el Art. 27 CPEUM, pero la sanción penal por usurpación material se rige por el Código Penal."
            )
        ),
        LegalConceptDefinition(
            id = "secuestro",
            name = "Delito de Secuestro / Privación Ilegal",
            keywords = listOf("secuestro", "privacion ilegal de la libertad", "rescate", "plagio"),
            legalBranch = "Derecho Penal Sustantivo Especial",
            properLawId = "cpf",
            properLawShortName = "CPF",
            properLawTitle = "Código Penal Federal",
            properArticleNumber = 366,
            properArticleId = "cpf_366",
            definition = "Privación de la libertad de una persona con el propósito de obtener rescate, causar daño o perjuicio, o lograr que la autoridad realice o deje de realizar un acto determinado.",
            elements = listOf(
                "Privación de la libertad ambulatoria.",
                "Propósito extorsivo: Exigencia de rescate o beneficio indebido.",
                "Sancionado con penas de alta penalidad conforme a la Ley General para Prevenir y Sancionar los Delitos en Materia de Secuestro."
            ),
            sanctionsOrRemedies = "Prisión de cuarenta a ochenta años conforme a la ley especial correspondiente.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El secuestro está catalogado en el Art. 19 CPEUM para prisión preventiva oficiosa, pero su tipificación sustantiva es penal."
            )
        ),

        // ==================== PROCEDIMIENTO PENAL ACUSATORIO (CNPP) ====================
        LegalConceptDefinition(
            id = "audiencia_inicial",
            name = "Audiencia Inicial en el Proceso Penal",
            keywords = listOf("audiencia inicial", "audiencia de imputacion", "primera audiencia penal"),
            legalBranch = "Derecho Procesal Penal (Sistema Acusatorio Oral)",
            properLawId = "cnpp",
            properLawShortName = "CNPP",
            properLawTitle = "Código Nacional de Procedimientos Penales",
            properArticleNumber = 307,
            properArticleId = "cnpp_307",
            definition = "Es la primera etapa judicial del proceso penal acusatorio en la que se informa al imputado de sus derechos, se califica la legalidad de su detención (si fue en flagrancia), se formula la imputación, se debate la vinculación a proceso y se fijan medidas cautelares.",
            elements = listOf(
                "Control de la detención en flagrancia o caso urgente (Art. 308 CNPP).",
                "Formulación de la imputación por el Ministerio Público (Art. 311 CNPP).",
                "Oportunidad de declarar del imputado (Art. 312 CNPP).",
                "Debate sobre vinculación a proceso (plazo de 72 o 144 horas, Art. 313 CNPP).",
                "Imposición de medidas cautelares (Art. 153-155 CNPP)."
            ),
            sanctionsOrRemedies = "Auto de vinculación o no vinculación a proceso y fijación de plazo de investigación complementaria.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El Art. 16 y 19 CPEUM fijan los plazos máximos (48 y 72 horas), pero el desarrollo procesal paso a paso de la audiencia pertenece al CNPP.",
                "cpf" to "El Código Penal Federal define los delitos, no las audiencias del tribunal de control."
            )
        ),
        LegalConceptDefinition(
            id = "vinculacion_proceso",
            name = "Auto de Vinculación a Proceso",
            keywords = listOf("vinculacion a proceso", "auto de vinculacion", "vincular a proceso", "vinculación a proceso"),
            legalBranch = "Derecho Procesal Penal",
            properLawId = "cnpp",
            properLawShortName = "CNPP",
            properLawTitle = "Código Nacional de Procedimientos Penales",
            properArticleNumber = 316,
            properArticleId = "cnpp_316",
            definition = "Resolución judicial dictada por el Juez de Control mediante la cual se formaliza la investigación penal en contra del imputado, fijando los hechos que serán materia de investigación complementaria.",
            elements = listOf(
                "Haber formulado imputación previa.",
                "Oportunidad de contestar y rendir declaración.",
                "Existencia de datos de prueba que establezcan que se ha cometido un hecho delictivo.",
                "Probabilidad de que el imputado lo cometió o participó en su comisión.",
                "Que no se actualice una causa de extinción de la acción penal o excluyente de delito."
            ),
            sanctionsOrRemedies = "Sujeta al imputado formalmente a las etapas de investigación complementaria y medidas cautelares.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El Art. 19 CPEUM consagra el plazo constitucional de 72 horas para emitir este auto, pero los requisitos de procedencia procesal se rigen por el Art. 316 del CNPP.",
                "cpf" to "El CPF no regula resoluciones jurisdiccionales de control de investigación."
            )
        ),
        LegalConceptDefinition(
            id = "medidas_cautelares",
            name = "Régimen de Medidas Cautelares Penales",
            keywords = listOf("medidas cautelares", "prision preventiva", "prisión preventiva justificada", "brazalete electronico", "firma periodica"),
            legalBranch = "Derecho Procesal Penal",
            properLawId = "cnpp",
            properLawShortName = "CNPP",
            properLawTitle = "Código Nacional de Procedimientos Penales",
            properArticleNumber = 155,
            properArticleId = "cnpp_155",
            definition = "Mecanismos procesales ordenados por el Juez de Control a solicitud del Ministerio Público para asegurar la presencia del imputado en el juicio, garantizar la seguridad de la víctima/testigos o evitar la obstaculización del procedimiento.",
            elements = listOf(
                "Principio de Proporcionalidad y Mínima Intervención (Art. 156 CNPP).",
                "Catálogo de 14 medidas cautelares no privativas (firma periódica, fianza, embargo, prohibición de acercarse a la víctima, etc.).",
                "Prisión preventiva justificada como medida excepcional de última ratio (Art. 168-170 CNPP)."
            ),
            sanctionsOrRemedies = "Vigencia durante el proceso penal; revisable periódicamente por cambio de circunstancias objetivas.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El Art. 19 CPEUM enumera los delitos de prisión oficiosa, pero el catálogo general y la justificación técnica radican en el CNPP."
            )
        ),

        // ==================== JUICIO DE AMPARO (LEY DE AMPARO / CPEUM 103 Y 107) ====================
        LegalConceptDefinition(
            id = "suspension_acto",
            name = "Suspensión del Acto Reclamado",
            keywords = listOf("suspension del acto", "suspensión provisional", "suspensión definitiva", "suspension provisional", "suspension definitiva"),
            legalBranch = "Derecho Procesal Constitucional (Amparo)",
            properLawId = "amparo",
            properLawShortName = "Ley de Amparo",
            properLawTitle = "Ley de Amparo Reglamentaria de los Arts. 103 y 107 Constitucionales",
            properArticleNumber = 125,
            properArticleId = "amparo_125",
            definition = "Medida cautelar dictada dentro del juicio de amparo que tiene por objeto mantener viva la materia del juicio constitucional y evitar que la ejecución del acto de autoridad cause daños de imposible reparación al quejoso.",
            elements = listOf(
                "Suspensión de Oficio y de Plano (Art. 126): Actos que importen peligro de privación de la vida, ataques a la libertad fuera de procedimiento, deportación o tormento.",
                "Suspensión a Petición de Parte (Art. 128): Que la solicite el quejoso y que no se siga perjuicio al interés social ni se contravengan disposiciones de orden público.",
                "Suspensión Provisional (Art. 138) y Suspensión Definitiva (Art. 144 tras audiencia incidental)."
            ),
            sanctionsOrRemedies = "Paralización inmediata de los efectos del acto reclamado; responsabilidad penal para la autoridad que desacate (Art. 262 Ley de Amparo).",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El Art. 107 Fracc. X CPEUM estipula la base de la suspensión, pero su trámite procesal, incidentes y contragarantías se detallan exclusivamente en la Ley de Amparo.",
                "cpf" to "El Código Penal no tramita medidas suspensivas de amparo."
            )
        ),
        LegalConceptDefinition(
            id = "recurso_revision",
            name = "Recurso de Revisión en Amparo",
            keywords = listOf("recurso de revision", "recurso de revisión", "revision adhesiva", "revisión en amparo"),
            legalBranch = "Derecho Procesal Constitucional",
            properLawId = "amparo",
            properLawShortName = "Ley de Amparo",
            properLawTitle = "Ley de Amparo",
            properArticleNumber = 81,
            properArticleId = "amparo_81",
            definition = "Medio de impugnación ordinario en el juicio de amparo procedente contra resoluciones de Jueces de Distrito (que concedan/nieguen la suspensión definitiva o sentencias de amparo indirecto) y sentencias de Tribunales Colegiados que resuelvan sobre constitucionalidad de normas generales.",
            elements = listOf(
                "Plazo de interposición: 10 días (Art. 86 Ley de Amparo).",
                "Competencia: Tribunales Colegiados de Circuito o Suprema Corte de Justicia de la Nación (SCJN).",
                "Revisión Adhesiva: Oportunidad de la parte vencedora para fortalecer la resolución recurrida."
            ),
            sanctionsOrRemedies = "Confirmación, modificación o revocación de la resolución impugnada por el tribunal de alzada.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La Constitución faculta a la SCJN y Colegiados para conocer de revisiones, pero el catálogo de autos recurribles está en el Art. 81 de la Ley de Amparo."
            )
        ),

        // ==================== MATERIA LABORAL (LFT / CPEUM 123) ====================
        LegalConceptDefinition(
            id = "despido_injustificado",
            name = "Despido Injustificado y Acciones Laborales",
            keywords = listOf("despido injustificado", "despido", "indemnizacion constitucional", "reinstalacion laboral", "finiquito", "liquidacion"),
            legalBranch = "Derecho del Trabajo y Seguridad Social",
            properLawId = "lft",
            properLawShortName = "LFT",
            properLawTitle = "Ley Federal del Trabajo",
            properArticleNumber = 48,
            properArticleId = "lft_48",
            definition = "Terminación unilateral de la relación de trabajo provocada por el patrón sin mediar una de las causas justificadas de rescisión previstas en el Artículo 47 de la Ley Federal del Trabajo.",
            elements = listOf(
                "Acción de Reinstalación en el puesto o Indemnización Constitucional de tres meses de salario (Art. 48 LFT y 123 CPEUM).",
                "Pago de Salarios Vencidos computados hasta por un período máximo de doce meses.",
                "Interés del dos por ciento mensual capitalizable si el juicio excede del año.",
                "Etapa prejudicial obligatoria ante los Centros de Conciliación Laboral."
            ),
            sanctionsOrRemedies = "Pago de tres meses de salario, salarios caídos, prima de antigüedad (Art. 162 LFT) y partes proporcionales de aguinaldo, vacaciones y prima vacacional.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El Artículo 123 Apartado A Fracc. XXII CPEUM consagra el derecho fundamental a la indemnización, pero el cálculo aritmético, procedimiento rescisorio y aviso de despido se norman en la LFT.",
                "cpf" to "El conflicto laboral obrero-patronal no constituye materia penal salvo delitos específicos como retención indebida de salarios."
            )
        ),
        LegalConceptDefinition(
            id = "vacaciones_dignas",
            name = "Régimen de Vacaciones Dignas",
            keywords = listOf("vacaciones", "vacaciones dignas", "periodo vacacional", "prima vacacional"),
            legalBranch = "Derecho Laboral y Seguridad Social",
            properLawId = "lft",
            properLawShortName = "LFT",
            properLawTitle = "Ley Federal del Trabajo",
            properArticleNumber = 76,
            properArticleId = "lft_76",
            definition = "Las personas trabajadoras que tengan más de un año de servicios disfrutarán de un periodo anual de vacaciones pagadas no menor a doce días laborables, aumentando dos días laborables por cada año subsecuente hasta llegar a veinte.",
            elements = listOf(
                "Mínimo inicial: 12 días laborables continuos tras el primer año de servicios.",
                "Incremento bianual: Dos días por cada año subsecuente hasta el quinto año (20 días).",
                "A partir del sexto año: Aumento de dos días por cada cinco años de servicio.",
                "Prima vacacional: Pago obligatorio no menor al 25% sobre los salarios del periodo (Art. 80 LFT)."
            ),
            sanctionsOrRemedies = "Disfrute remunerado forzoso de los días de descanso y pago de prima vacacional mínima del 25%; son irrenunciables y no compensables en dinero.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La CPEUM consagra las jornadas máximas y descanso semanal (Art. 123 Fracc. IV), pero los días específicos de vacaciones y la prima vacacional se encuentran en la LFT."
            )
        ),
        LegalConceptDefinition(
            id = "aguinaldo",
            name = "Derecho al Aguinaldo Anual Obligatorio",
            keywords = listOf("aguinaldo", "pago de aguinaldo", "dias de aguinaldo", "aguinaldo proporcional"),
            legalBranch = "Derecho Laboral y Seguridad Social",
            properLawId = "lft",
            properLawShortName = "LFT",
            properLawTitle = "Ley Federal del Trabajo",
            properArticleNumber = 87,
            properArticleId = "lft_87",
            definition = "Los trabajadores tendrán derecho a un aguinaldo anual que deberá pagarse antes del día veinte de diciembre, equivalente a quince días de salario, por lo menos.",
            elements = listOf(
                "Plazo perentorio de pago: A más tardar el 19 de diciembre de cada año.",
                "Cuantía legal mínima: 15 días de salario integrado ordinario.",
                "Parte proporcional: Trabajadores que no hayan cumplido el año de servicios tienen derecho a cobrar la parte proporcional al tiempo laborado."
            ),
            sanctionsOrRemedies = "Exigibilidad inmediata ante los Tribunales Laborales y sanciones pecuniarias patronales de 50 a 5000 UMA por incumplimiento.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "La Constitución establece el principio general del salario digno, pero el aguinaldo es una prestación legal regulada de forma precisa en el Artículo 87 de la LFT."
            )
        ),
        LegalConceptDefinition(
            id = "reparto_utilidades",
            name = "Participación de los Trabajadores en las Utilidades (PTU)",
            keywords = listOf("utilidades", "ptu", "reparto de utilidades", "pago de utilidades"),
            legalBranch = "Derecho Laboral y Seguridad Social",
            properLawId = "lft",
            properLawShortName = "LFT",
            properLawTitle = "Ley Federal del Trabajo",
            properArticleNumber = 117,
            properArticleId = "lft_117",
            definition = "Derecho constitucional y legal de los trabajadores a participar del diez por ciento de las utilidades netas generadas por la empresa durante el ejercicio fiscal anterior.",
            elements = listOf(
                "Porcentaje aplicable: 10% fijado por la Comisión Nacional (CONAPTU).",
                "División en dos partes iguales: Una por días trabajados en el año y otra en proporción a los salarios devengados.",
                "Tope máximo: Tres meses de salario o promedio de utilidades de los últimos 3 años (Art. 127 Fracc. VIII LFT).",
                "Fecha límite: Mayo para personas morales y junio para personas físicas."
            ),
            sanctionsOrRemedies = "Crédito preferente de cobro frente a otras obligaciones y multas patronales de hasta 5000 UMA por evasión.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El Artículo 123 Apartado A Fracc. IX CPEUM establece el derecho al reparto de utilidades, mientras que la LFT reglamenta el procedimiento, fechas de entrega y excepciones."
            )
        ),

        // ==================== MATERIA CIVIL (CCF / CCET) ====================
        LegalConceptDefinition(
            id = "divorcio",
            name = "Divorcio Incausado y Disolución Matrimonial",
            keywords = listOf("divorcio", "divorcio incausado", "disolucion conyugal", "separacion conyugal"),
            legalBranch = "Derecho Familiar y Civil",
            properLawId = "ccf",
            properLawShortName = "CCF",
            properLawTitle = "Código Civil Federal",
            properArticleNumber = 266,
            properArticleId = "ccf_266",
            definition = "El divorcio disuelve el vínculo del matrimonio y deja a los cónyuges en aptitud de contraer otro. Podrá solicitarse por uno o ambos cónyuges sin necesidad de invocar causal alguna (libre desarrollo de la personalidad).",
            elements = listOf(
                "Principio del Libre Desarrollo de la Personalidad: No requiere acreditar culpabilidad o causa grave.",
                "Propuesta de Convenio Obligatoria: Guarda y custodia de hijos, régimen de visitas, pensión alimenticia y liquidación de bienes (Art. 267 CCF).",
                "Efectos Inmediatos: Cese de la obligación de cohabitación y disolución de la sociedad conyugal."
            ),
            sanctionsOrRemedies = "Sentencia judicial firme de disolución conyugal y fijación de medidas definitivas alimentarias y de custodia.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El Artículo 4° CPEUM protege la organización y el desarrollo de la familia, pero la regulación sustantiva del matrimonio y su disolución corresponde a los Códigos Civiles.",
                "cpf" to "El divorcio es una institución de estricto derecho civil privado, no punible penalmente."
            )
        ),
        LegalConceptDefinition(
            id = "pension_alimenticia",
            name = "Obligación Alimentaria y Pensión",
            keywords = listOf("pension alimenticia", "alimentos civiles", "dar alimentos", "deber de alimentos"),
            legalBranch = "Derecho Familiar y Civil",
            properLawId = "ccf",
            properLawShortName = "CCF",
            properLawTitle = "Código Civil Federal",
            properArticleNumber = 301,
            properArticleId = "ccf_301",
            definition = "La obligación de dar alimentos es recíproca. El que los da tiene a su vez el derecho de pedirlos cuando carezca de medios propios. Comprende comida, vestido, habitación, salud y educación.",
            elements = listOf(
                "Principio de Reciprocidad y Proporcionalidad: En proporción a la posibilidad de quien los da y necesidad de quien los recibe (Art. 311).",
                "Contenido Integral: Comida, vestido, vivienda, medicinas y educación obligatoria hasta tener profesión lícita (Art. 308).",
                "Carácter Preferente e Inembargable: Crédito de orden público con prioridad absoluta sobre cualquier otra deuda patrimonial."
            ),
            sanctionsOrRemedies = "Embargo judicial precautorio de hasta el 50% del salario o ingresos del deudor; en caso de abandono o dolo, se configura delito penal (Art. 206 CPET / Art. 336 CPF).",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El Artículo 4° CPEUM salvaguarda el interés superior de la niñez y el derecho a la alimentación, mientras que la cuantificación y reglas de prelación se norman en el Código Civil."
            )
        ),
        LegalConceptDefinition(
            id = "usucapion",
            name = "Usucapión o Prescripción Adquisitiva de Bienes",
            keywords = listOf("usucapion", "usucapión", "prescripcion positiva", "prescribir terreno", "prescripcion adquisitiva"),
            legalBranch = "Derecho Civil (Derechos Reales y Bienes)",
            properLawId = "ccf",
            properLawShortName = "CCF",
            properLawTitle = "Código Civil Federal",
            properArticleNumber = 1135,
            properArticleId = "ccf_1135",
            definition = "Medio de adquirir el dominio de bienes inmuebles mediante la posesión pacífica, continua, pública y en concepto de propietario durante el tiempo que fije la ley.",
            elements = listOf(
                "Cualidades de la Posesión: En concepto de dueño, pacífica (sin violencia), continua (sin interrupción) y pública (Art. 1151).",
                "Plazo de Buena Fe: Cinco años para bienes inmuebles con justo título (Art. 1152 Fracc. I).",
                "Plazo de Mala Fe: Diez años para bienes inmuebles sin justo título si se cumple con las cualidades de la posesión (Art. 1152 Fracc. II)."
            ),
            sanctionsOrRemedies = "Juicio ordinario de usucapión para que la sentencia firme sirva como título de propiedad inscribible en el Registro Público de la Propiedad.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "El Artículo 27 CPEUM sienta las bases de la propiedad de tierras y aguas, pero la adquisición privada por usucapión es regulada por el Código Civil."
            )
        ),
        LegalConceptDefinition(
            id = "dano_moral",
            name = "Indemnización por Daño Moral",
            keywords = listOf("daño moral", "dano moral", "afectacion moral", "indemnizacion por dano moral"),
            legalBranch = "Derecho Civil (Responsabilidad Extracontractual)",
            properLawId = "ccf",
            properLawShortName = "CCF",
            properLawTitle = "Código Civil Federal",
            properArticleNumber = 1916,
            properArticleId = "ccf_1916",
            definition = "Por daño moral se entiende la afectación que una persona sufre en sus sentimientos, afectos, creencias, decoro, honor, reputación, vida privada, configuración y aspecto físicos, o en la consideración que de sí misma tienen los demás.",
            elements = listOf(
                "Hecho Ilícito o Acto Abusivo generador de la afectación personal o a derechos de la personalidad.",
                "Lesión a bienes inmateriales: Honor, vida privada, imagen, sentimientos, integridad física o decoro.",
                "Nexo de Causalidad: Vínculo directo entre la conducta antijurídica dolosa o culposa y el menoscabo moral resentido.",
                "Cuantificación Judicial: El juez determinará la indemnización en dinero ponderando los derechos lesionados, grado de responsabilidad y situación económica de las partes."
            ),
            sanctionsOrRemedies = "Condena judicial pecuniaria a la reparación económica del daño moral y publicación de la sentencia condenatoria en los mismos medios de difusión.",
            reasonWhyNotInOtherLaws = mapOf(
                "cpeum" to "Los derechos de personalidad emanan de los Artículos 1°, 6° y 16 CPEUM, pero la acción indemnizatoria resarcitoria se litiga conforme al Artículo 1916 del Código Civil Federal."
            )
        )
    )

    /**
     * Resolves if the user's query refers to a known legal figure that belongs to
     * a specific Mexican statute.
     */
    fun findConceptForQuery(query: String, activeLawId: String = ""): LegalConceptDefinition? {
        val q = query.lowercase().trim()
        val normalizedWords = q.replace(Regex("""[¿?¡!,.;:]"""), " ")
        
        // Priority 1: If query explicitly asks about Tabasco, or active law is CPET
        if (activeLawId.equals("cpet", ignoreCase = true) || q.contains("tabasco") || q.contains("cpet")) {
            if (q.contains("estupro")) {
                val tabascoEstupro = concepts.find { it.id == "estupro_tabasco" }
                if (tabascoEstupro != null) return tabascoEstupro
            }
        }

        // Priority 2: Standard keyword matching
        return concepts.find { concept ->
            concept.keywords.any { keyword ->
                normalizedWords.contains(keyword.lowercase())
            }
        }
    }
}
