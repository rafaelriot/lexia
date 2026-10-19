package com.lexia.legalassistant.data.repository

/**
 * Registro auténtico de artículos clave del Código Penal para el Estado de Tabasco (CPET).
 * Publicado en el Periódico Oficial del Estado de Tabasco (POET).
 * Vigente con reformas en materia de Feminicidio, Violencia Familiar y Maltrato Animal.
 */
object CpetStatutoryData {

    data class ArticleMeta(
        val title: String,
        val category: String,
        val summary: String
    )

    val articles: Map<Int, ArticleMeta> = mapOf(
        1 to ArticleMeta("Ámbito Espacial y Aplicación Territorial", "Parte General", "Este Código se aplicará en el Estado de Tabasco por los delitos cometidos en su territorio que sean de la competencia de sus tribunales."),
        110 to ArticleMeta("Tipo Penal de Homicidio Simple", "Vida e Integridad", "Al que prive de la vida a otro se le impondrá prisión de ocho a veinte años en el fuero común del Estado de Tabasco."),
        111 to ArticleMeta("Homicidio Calificado por Premeditación o Alevosía", "Vida e Integridad", "Se impondrá de veinte a cincuenta años de prisión al que prive de la vida con premeditación, ventaja, alevosía o traición."),
        115 to ArticleMeta("Tipo Penal de Feminicidio por Razones de Género", "Vida e Integridad", "Comete feminicidio quien prive de la vida a una mujer por razones de género. Se impondrá prisión de cuarenta a cincuenta años."),
        116 to ArticleMeta("Tipo Penal de Lesiones", "Vida e Integridad", "Comete el delito de lesiones quien causa a otro un daño que altere su salud física o mental."),
        120 to ArticleMeta("Lesiones que ponen en Peligro la Vida", "Vida e Integridad", "A quien infiera lesiones que por su naturaleza pongan en peligro la vida se le impondrán de tres a ocho años de prisión."),
        126 to ArticleMeta("Homicidio en Razón del Parentesco o Parricidio", "Vida e Integridad", "A quien prive de la vida a su ascendiente o descendiente consanguíneo en línea recta conociendo el parentesco se le impondrá de treinta a cincuenta años de prisión."),
        131 to ArticleMeta("Inducción o Ayuda al Suicidio", "Vida e Integridad", "Al que preste auxilio o induzca a otro para que se suicide se le impondrá prisión de dos a ocho años."),
        141 to ArticleMeta("Privación Ilegal de la Libertad Personal", "Libertad Personal", "Al particular que prive a otro de su libertad sin el propósito de obtener rescate o causar daño grave se le impondrá prisión de dos a cinco años."),
        142 to ArticleMeta("Modalidades Agravadas de Privación de Libertad", "Libertad Personal", "Se aumentará la pena si la privación se realiza con violencia, si dura más de 24 horas o se comete contra menores o adultos mayores."),
        147 to ArticleMeta("Tipo Penal de Violación", "Seguridad Sexual", "Comete el delito de violación quien por medio de la violencia física o moral tenga cópula con una persona sin su consentimiento. Prisión de ocho a veinte años."),
        148 to ArticleMeta("Violación Equiparada", "Seguridad Sexual", "Se sanciona con igual pena a quien tenga cópula con persona privada de razón o de sentido, o que por enfermedad no pueda resistir la agresión sexual."),
        149 to ArticleMeta("Tipo Penal de Abuso Sexual", "Seguridad Sexual", "A quien sin consentimiento y sin llegar a la cópula ejecute en una persona actos de contenido sexual o la obligue a realizarlos, de dos a seis años de prisión."),
        150 to ArticleMeta("Hostigamiento y Acoso Sexual", "Seguridad Sexual", "Se sanciona el asedio o asedio con fines lascivos valiéndose de una posición jerárquica en el ámbito laboral, docente o doméstico en Tabasco."),
        151 to ArticleMeta("Agravantes en Delitos contra la Seguridad Sexual", "Seguridad Sexual", "Las penas se incrementarán si el delito es cometido por ministros de culto, tutores, docentes o servidores públicos encargados de la custodia."),
        152 to ArticleMeta("Medidas de Protección y Reparación Psicológica", "Seguridad Sexual", "El sentenciado por delitos sexuales estará obligado a cubrir el tratamiento integral psicológico de la víctima."),
        153 to ArticleMeta("Tipo Penal de Estupro", "Seguridad Sexual", "Al que por medio del engaño tenga cópula con mujer mayor de catorce y menor de dieciocho años que no haya alcanzado su normal desarrollo psicosexual, se le aplicará prisión de cuatro a seis años."),
        154 to ArticleMeta("Atenuantes y Extinción por Matrimonio Derogado", "Seguridad Sexual", "El perdón o posterior unión matrimonial no extingue la acción penal tratándose de delitos sexuales en agravio de personas menores de edad."),
        155 to ArticleMeta("Corrupción de Menores e Incapaces", "Normal Desarrollo", "A quien induzca, procure o facilite a un menor el consumo de alcohol, estupefacientes o la mendicidad se le impondrá prisión de cinco a diez años."),
        175 to ArticleMeta("Tipo Penal de Robo Simple", "Patrimonio", "Al que, con ánimo de dominio y sin consentimiento de quien legalmente pueda otorgarlo, se apodere de una cosa mueble ajena. Sancionado conforme a escalas UMA."),
        183 to ArticleMeta("Tipo Penal de Abuso de Confianza", "Patrimonio", "Se sanciona a quien con perjuicio de alguien disponga para sí o para otro de una cosa ajena mueble de la cual se le haya transmitido la tenencia y no el dominio."),
        185 to ArticleMeta("Tipo Penal de Fraude General", "Patrimonio", "Comete el delito de fraude quien engañando a uno o aprovechándose del error en que éste se halla, se hace ilícitamente de alguna cosa o alcanza un lucro indebido."),
        192 to ArticleMeta("Tipo Penal de Despojo de Inmuebles o Aguas", "Patrimonio", "A quien por propia autoridad y haciendo violencia o furtivamente ocupe un inmueble ajeno o haga uso de él en el Estado de Tabasco."),
        196 to ArticleMeta("Delito de Extorsión y Cobro de Piso", "Patrimonio", "Al que para obtener un provecho indebido obligue a otro a dar, hacer o tolerar algo bajo amenaza de causarle un daño en su persona, bienes o familia."),
        205 to ArticleMeta("Tipo Penal de Abigeato (Ganado Bovino y Equino)", "Patrimonio", "Comete abigeato quien se apodere de una o más cabezas de ganado bovino o equino sin derecho ni consentimiento. Prisión de seis a diez años."),
        206 to ArticleMeta("Incumplimiento de Obligaciones de Asistencia Familiar", "Familia", "A quien sin causa justificada incumpla su obligación de dar alimentos a sus hijos o cónyuge se le impondrá de uno a cuatro años de prisión y pago de pensiones caídas."),
        208 to ArticleMeta("Tipo Penal de Violencia Familiar", "Familia", "Comete violencia familiar quien ejerza acto abusivo de poder dirigido a dominar, someter, controlar o agredir física, psicológica, patrimonial o sexualmente a un familiar dentro o fuera del domicilio."),
        216 to ArticleMeta("Ejercicio Indebido del Servicio Público", "Función Pública", "Al servidor público que ejerza funciones sin haber tomado posesión legal o continúe ejerciéndolas después de haber cesado en su encargo."),
        220 to ArticleMeta("Abuso de Autoridad en Tabasco", "Función Pública", "Al servidor público que en razón de sus funciones ejerza violencia contra las personas sin causa legítima, las humille o retarde indebidamente trámites."),
        224 to ArticleMeta("Tipo Penal de Cohecho en Tabasco", "Función Pública", "Al servidor público que por sí o por interpósita persona solicite o reciba dinero o dádivas para hacer o dejar de hacer algo justo o injusto relativo a sus funciones."),
        228 to ArticleMeta("Tipo Penal de Peculado en Tabasco", "Función Pública", "Al servidor público que para usos propios o ajenos distraiga de su objeto dinero, valores o fincas pertenecientes al Estado o municipios de Tabasco."),
        261 to ArticleMeta("Tipo Penal de Asociación Delictuosa", "Seguridad Colectiva", "A quienes formen parte de una asociación o banda de tres o más personas con el propósito de delinquir en el Estado de Tabasco."),
        304 to ArticleMeta("Tipo Penal de Maltrato o Crueldad Animal", "Protección Animal", "A quien intencionalmente cometa actos de maltrato o crueldad en contra de animales causándoles sufrimiento o lesiones se le impondrán de seis meses a tres años de prisión.")
    )
}
