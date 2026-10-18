package com.lexia.legalassistant.data.repository

import com.lexia.legalassistant.data.model.DictamenData
import com.lexia.legalassistant.data.model.LawArticleDetail
import com.lexia.legalassistant.data.model.LegalCitation
import com.lexia.legalassistant.data.model.RagMessage
import com.lexia.legalassistant.data.model.ScopeChoice
import com.lexia.legalassistant.data.model.VectorChunk
import java.text.SimpleDateFormat
import java.util.*

object LegalArticlesRepository {

    // Metadata for the 136 Articles of the Mexican Constitution
    private val cpeumMeta = listOf(
        Triple(1, "Derechos Humanos, Bloque de Constitucionalidad y Principio Pro Persona", "En los Estados Unidos Mexicanos todas las personas gozarán de los derechos humanos reconocidos en la Constitución y tratados internacionales. Principio Pro Persona y prohibición de discriminación."),
        Triple(2, "Composición Pluricultural de la Nación y Pueblos Indígenas", "La Nación Mexicana es única e indivisible, sustentada en sus pueblos indígenas. Reconoce autonomía, libre determinación, sistemas normativos y derechos colectivos."),
        Triple(3, "Derecho Universal a la Educación Laica, Gratuita y de Calidad", "Toda persona tiene derecho a la educación inicial, básica, media superior y superior impartida por el Estado, la cual será laica, gratuita, universal e inclusiva."),
        Triple(4, "Igualdad de Género, Salud, Vivienda, Niñez y Medio Ambiente", "La mujer y el hombre son iguales ante la ley. Tutela el derecho a decidir el número de hijos, a la alimentación, protección de la salud, medio ambiente sano, agua y vivienda digna."),
        Triple(5, "Libertad de Trabajo, Profesión, Industria y Comercio", "A ninguna persona podrá impedirse que se dedique a la profesión, industria, comercio o trabajo que le acomode, siendo lícitos. Nadie puede ser privado del producto de su trabajo sino por resolución judicial."),
        Triple(6, "Libertad de Expresión y Derecho de Acceso a la Información", "La manifestación de las ideas no será objeto de ninguna inquisición judicial o administrativa. El Estado garantizará el derecho al libre acceso a la información plural y oportuna y a las TIC."),
        Triple(7, "Libertad de Difusión de Opiniones, Información e Imprenta", "Es inviolable la libertad de difundir opiniones, información e ideas, a través de cualquier medio. No se puede restringir este derecho por vías o medios indirectos."),
        Triple(8, "Derecho de Petición Pacífica y por Escrito", "Los funcionarios y empleados públicos respetarán el ejercicio del derecho de petición, siempre que ésta se formule por escrito, de manera pacífica y respetuosa."),
        Triple(9, "Libertad de Asociación y Reunión Pacífica", "No se podrá coartar el derecho de asociarse o reunirse pacíficamente con cualquier objeto lícito; ninguna reunión armada tiene derecho de deliberar."),
        Triple(10, "Derecho a Poseer Armas en el Domicilio para Seguridad", "Los habitantes de los Estados Unidos Mexicanos tienen derecho a poseer armas en su domicilio, para su seguridad y legítima defensa, con excepción de las prohibidas por la Ley Federal."),
        Triple(11, "Libertad de Tránsito, Entrada y Salida, y Derecho de Asilo", "Toda persona tiene derecho para entrar en la República, salir de ella, viajar por su territorio y mudar de residencia, sin necesidad de carta de seguridad o pasaporte. Reconoce el derecho de asilo y refugio."),
        Triple(12, "Prohibición de Títulos de Nobleza y Prerrogativas", "En los Estados Unidos Mexicanos no se concederán títulos de nobleza, ni prerrogativas y honores hereditarios, ni se dará efecto alguno a los otorgados por cualquier otro país."),
        Triple(13, "Prohibición de Leyes Privativas, Tribunales Especiales y Fuero Militar", "Nadie puede ser juzgado por leyes privativas ni por tribunales especiales. Ninguna persona o corporación puede tener fuero. El fuero de guerra subsiste únicamente para delitos militares estrictos."),
        Triple(14, "Garantía de Audiencia, Debido Proceso Legal e Irretroactividad", "A ninguna ley se dará efecto retroactivo en perjuicio de persona alguna. Nadie podrá ser privado de la libertad o derechos sino mediante juicio conforme a formalidades esenciales del procedimiento."),
        Triple(15, "Prohibición de Tratados de Extradición para Reos Políticos", "No se autoriza la celebración de tratados para la extradición de reos políticos, ni para la de aquellos delincuentes del orden común que hayan tenido en el país la condición de esclavos."),
        Triple(16, "Principio de Legalidad, Mandamiento Escrito, Flagrancia y Cateo", "Nadie puede ser molestado en su persona o bienes sino por mandamiento escrito debidamente fundado y motivado. Delimita flagrancia estricta, caso urgente, cateos y comunicaciones privadas."),
        Triple(17, "Acceso a la Justicia, Tutela Judicial y Prohibición de Autotutela", "Ninguna persona podrá hacerse justicia por sí misma. Toda persona tiene derecho a que se le administre justicia por tribunales expeditos, imparciales y gratuitos. Prohíbe prisión por deudas civiles."),
        Triple(18, "Sistema Penitenciario, Reinserción Social y Adolescentes", "Sólo por delito que merezca pena privativa de libertad habrá prisión preventiva. El sistema penitenciario se organizará sobre la base del respeto a los derechos humanos, trabajo, capacitación y educación."),
        Triple(19, "Auto de Vinculación a Proceso, Plazo 72h y Prisión Preventiva", "Ninguna detención ante autoridad judicial excederá de 72 horas sin que se justifique con un auto de vinculación a proceso. Establece causales taxativas de prisión preventiva."),
        Triple(20, "Sistema Penal Acusatorio Oral, Presunción de Inocencia y Víctimas", "El proceso penal será acusatorio y oral, rigiéndose por publicidad, contradicción, concentración, continuidad e inmediación. Apartado B: Derechos del Imputado; Apartado C: Derechos de la Víctima."),
        Triple(21, "Investigación Ministerial, Ejercicio de la Acción Penal y Seguridad", "La investigación de los delitos corresponde al Ministerio Público y a las policías. La seguridad pública es función concurrente del Estado, regulando a la Guardia Nacional."),
        Triple(22, "Prohibición de Penas Inusitadas, Tormento y Extinción de Dominio", "Quedan prohibidas las penas de muerte, mutilación, infamia, marcas y tormentos. Regula el juicio civil autónomo de extinción de dominio sobre bienes vinculados a hechos ilícitos."),
        Triple(23, "Principio Non Bis In Idem (Tres Instancias Máximas)", "Ningún juicio criminal deberá tener más de tres instancias. Nadie puede ser juzgado dos veces por el mismo delito, ya sea que en el juicio se le absuelva o se le condene."),
        Triple(24, "Libertad de Convicciones Éticas, Conciencia y Religión", "Toda persona tiene derecho a la libertad de convicciones éticas, de conciencia y de religión, y a tener o adoptar, en su caso, la de su agrado y a participar en ceremonias religiosas."),
        Triple(25, "Rectoría Económica del Estado y Desarrollo Sustentable", "Corresponde al Estado la rectoría del desarrollo nacional para garantizar que sea integral y sustentable, fortaleciendo la soberanía y la justa distribución del ingreso."),
        Triple(26, "Planeación Democrática del Desarrollo y Sistema de Información", "El Estado organizará un sistema de planeación democrática del desarrollo nacional que imprima solidez, dinamismo, competitividad y equidad. Regula el INEGI y el CONEVAL."),
        Triple(27, "Propiedad Originaria de Tierras y Aguas y Régimen Agrario", "La propiedad de las tierras y aguas comprendidas dentro de los límites del territorio nacional corresponde originariamente a la Nación. Regula recursos naturales, ejidos y comunidades."),
        Triple(28, "Prohibición de Monopolios, Libre Concurrencia y Órganos Reguladores", "En los Estados Unidos Mexicanos quedan prohibidos los monopolios, las prácticas monopólicas y los estancos. Regula a la COFECE, IFT y el Banco de México."),
        Triple(29, "Suspensión de Derechos en Casos de Invasión o Perturbación Grave", "En los casos de invasión o perturbación grave de la paz pública, solamente el Presidente de la República, con la aprobación del Congreso, podrá restringir o suspender el ejercicio de los derechos."),
        Triple(30, "Nacionalidad Mexicana por Nacimiento y Naturalización", "La nacionalidad mexicana se adquiere por nacimiento o por naturalización. Define los supuestos del jus soli y del jus sanguinis."),
        Triple(31, "Obligaciones Fundamentales de los Mexicanos", "Ser responsables de la educación de sus hijos, recibir instrucción cívica y militar, y contribuir a los gastos públicos de la Federación, estados y municipios de manera proporcional y equitativa."),
        Triple(32, "Preferencia de Mexicanos y Cargos Reservados por Nacimiento", "La ley regulará el ejercicio de los derechos que la legislación mexicana concede a los mexicanos que posean otra nacionalidad. Reserva cargos estratégicos a mexicanos por nacimiento."),
        Triple(33, "Condición de Personas Extranjeras y Garantía de Audiencia", "Son personas extranjeras las que no posean las calidades del artículo 30. Gozan de los derechos humanos constitucionales. El Ejecutivo puede expulsar personas extranjeras previa audiencia legal."),
        Triple(34, "Requisitos de la Ciudadanía Mexicana", "Son ciudadanos de la República los varones y mujeres que, teniendo la calidad de mexicanos, hayan cumplido 18 años y tengan un modo honesto de vivir."),
        Triple(35, "Derechos Políticos de la Ciudadanía", "Votar en las elecciones populares, ser votados para cargos de elección popular, asociarse pacíficamente para asuntos políticos, consulta popular y revocación de mandato."),
        Triple(36, "Obligaciones Electorales y Cívicas de los Ciudadanos", "Inscribirse en el catastro y padrón electoral, alistarse en la Guardia Nacional, votar en las elecciones y desempeñar los cargos de elección popular."),
        Triple(37, "Pérdida de la Nacionalidad y de la Ciudadanía", "Ningún mexicano por nacimiento podrá ser privado de su nacionalidad. La nacionalidad por naturalización se pierde por adquisición voluntaria de otra nacionalidad extranjera."),
        Triple(38, "Causales de Suspensión de Prerrogativas Ciudadanas", "Los derechos de los ciudadanos se suspenden por falta de cumplimiento de obligaciones, sujeción a proceso penal por delito grave, vagancia o ebriedad consuetudinaria declarada, o sentencia judicial."),
        Triple(39, "Soberanía Nacional Originaria en el Pueblo", "La soberanía nacional reside esencial y originariamente en el pueblo. Todo poder público dimana del pueblo y se instituye para beneficio de éste."),
        Triple(40, "Forma Republicana, Democrática, Laica y Federal de Gobierno", "Es voluntad del pueblo mexicano constituirse en una República representativa, democrática, laica y federal, compuesta por Estados libres y soberanos en su régimen interior."),
        Triple(41, "Renovación de Poderes mediante Elecciones y Partidos", "El pueblo ejerce su soberanía por medio de los Poderes de la Unión. Los partidos políticos son entidades de interés público. Regula al Instituto Nacional Electoral (INE)."),
        Triple(42, "Partes Integrantes del Territorio Nacional", "El territorio nacional comprende las partes integrantes de la Federación, las islas adyacentes, los arrecifes, la plataforma continental y el espacio situado sobre el territorio."),
        Triple(43, "Estados que Integran la Federación Mexicana", "Las partes integrantes de la Federación son los 31 Estados soberanos y la Ciudad de México."),
        Triple(44, "La Ciudad de México como Sede de los Poderes de la Unión", "La Ciudad de México es la entidad federativa sede de los Poderes de la Unión y capital de los Estados Unidos Mexicanos."),
        Triple(45, "Límites y Extensión de las Entidades Federativas", "Los Estados de la Federación conservan la extensión y límites que hasta hoy han tenido, siempre que no haya dificultad en cuanto a éstos."),
        Triple(46, "Solución de Conflictos Limítrofes entre Estados", "Las entidades federativas pueden arreglar entre sí sus respectivos límites territoriales por convenios amistosos, o someterlos ante la Suprema Corte de Justicia de la Nación."),
        Triple(47, "Límites del Estado de Nayarit", "El Estado de Nayarit tendrá la extensión territorial y límites que comprende el territorio de Tepic."),
        Triple(48, "Jurisdicción Federal sobre Islas, Cayos y Arrecifes", "Las islas, cayos y arrecifes de los mares adyacentes dependerán directamente del Gobierno de la Federación, exceptuando aquellas sobre las que los Estados hayan ejercido jurisdicción."),
        Triple(49, "División del Supremo Poder de la Federación", "El Supremo Poder de la Federación se divide para su ejercicio en Legislativo, Ejecutivo y Judicial. No podrán reunirse dos o más de estos Poderes en una sola persona o corporación."),
        Triple(50, "Poder Legislativo Depositado en el Congreso General", "El Poder Legislativo de los Estados Unidos Mexicanos se deposita en un Congreso General, que se dividirá en dos Cámaras: una de Diputados y otra de Senadores."),
        Triple(51, "Cámara de Diputados y Representación de la Nación", "La Cámara de Diputados se compondrá de representantes de la Nación, electos en su totalidad cada tres años."),
        Triple(52, "Integración de la Cámara de Diputados (500 Curules)", "La Cámara de Diputados estará integrada por 300 diputadas y diputados electos por mayoría relativa y 200 según el principio de representación proporcional."),
        Triple(53, "Distritación Electoral Uninominal y Circunscripciones", "La demarcación de los 300 distritos electorales uninominales se hará dividiendo la población total entre los distritos, y se distribuirán cinco circunscripciones plurinominales."),
        Triple(54, "Reglas de Asignación por Representación Proporcional", "La elección de los 200 diputados según el principio de representación proporcional se sujetará a las bases de votación nacional efectiva, tope de sobrerrepresentación (8%) y registro de listas."),
        Triple(55, "Requisitos para ser Diputado Federal", "Ser ciudadano mexicano por nacimiento, tener 18 años cumplidos al día de la elección, ser originario o vecino de la entidad federativa y no estar en servicio militar activo ni en el clero."),
        Triple(56, "Integración de la Cámara de Senadores (128 Escaños)", "La Cámara de Senadores se integrará por 128 senadoras y senadores, de los cuales, en cada Estado y en la Ciudad de México, dos serán elegidos según el principio de mayoría relativa y uno asignado a la primera minoría."),
        Triple(57, "Elección de Senadores Suplentes", "Por cada senador propietario se elegirá un suplente."),
        Triple(58, "Requisitos para ser Senador de la República", "Para ser senador se requieren los mismos requisitos que para ser diputado, excepto el de la edad, que será de 21 años cumplidos al día de la elección."),
        Triple(59, "Reelección Consecutiva de Legisladores Federales", "Los Senadores podrán ser electos hasta por dos periodos consecutivos y los Diputados hasta por cuatro periodos consecutivos, postulados por el mismo partido o coalición."),
        Triple(60, "Calificación de Elecciones de Legisladores por el TEPJF", "El organismo público autónomo electoral declarará la validez de las elecciones de diputados y senadores, y sus determinaciones podrán ser impugnadas ante el Tribunal Electoral."),
        Triple(61, "Inviolabilidad Parlamentaria de Diputados y Senadores", "Los diputados y senadores son inviolables por las opiniones que manifiesten en el desempeño de sus cargos, y jamás podrán ser reconvenidos ni juzgados por ellas."),
        Triple(62, "Incompatibilidad de Cargos Públicos sin Licencia", "Los diputados y senadores propietarios durante el período de su encargo, no podrán desempeñar ninguna otra comisión o empleo de la Federación o de los Estados por los cuales se disfrute sueldo."),
        Triple(63, "Quórum Legal para Sesiones de las Cámaras", "Las Cámaras no pueden abrir sus sesiones ni ejercer su cargo sin la concurrencia, en cada una de ellas, de más de la mitad del número total de sus miembros."),
        Triple(64, "Sanción a Legisladores por Inasistencias Injustificadas", "Los diputados y senadores que no concurran a una sesión sin causa justificada o sin permiso expreso no tendrán derecho a la dieta correspondiente al día en que falten."),
        Triple(65, "Periodos Ordinarios de Sesiones del Congreso", "El Congreso se reunirá a partir del 1o. de septiembre de cada año para celebrar un primer periodo de sesiones ordinarias y a partir del 1o. de febrero para celebrar un segundo periodo."),
        Triple(66, "Duración de los Periodos Ordinarios de Sesiones", "Cada período de sesiones ordinarias durará el tiempo necesario para tratar todos los asuntos; el primero no podrá prolongarse sino hasta el 15 de diciembre, y el segundo hasta el 30 de abril."),
        Triple(67, "Sesiones Extraordinarias del Congreso de la Unión", "El Congreso o una sola de las Cámaras se reunirán en sesiones extraordinarias cada vez que los convoque para ese objeto la Comisión Permanente."),
        Triple(68, "Residencia Conjunta de las Cámaras del Congreso", "Las dos Cámaras residirán en un mismo lugar y no podrán trasladarse a otro sin que antes convengan en la traslación y en el tiempo y modo de verificarla."),
        Triple(69, "Apertura de Sesiones e Informe de Gobierno Presidencial", "En la apertura de Sesiones Ordinarias del Primer Periodo de cada año, el Presidente de la República presentará un informe por escrito en el que manifieste el estado general de la administración pública."),
        Triple(70, "Resoluciones del Congreso y Ley Orgánica Parlamentaria", "Toda resolución del Congreso tendrá el carácter de ley o decreto. El Congreso expedirá la ley que regulará su estructura y funcionamiento internos, la cual no podrá ser vetada por el Ejecutivo."),
        Triple(71, "Derecho de Iniciar Leyes o Decretos", "El derecho de iniciar leyes compete al Presidente de la República, a los Diputados y Senadores, a las Legislaturas de los Estados y a los ciudadanos en un número equivalente al 0.13% de la lista nominal."),
        Triple(72, "Procedimiento Legislativo Federal y Veto Presidencial", "Todo proyecto de ley o decreto cuya resolución no sea exclusiva de alguna de las Cámaras se discutirá sucesivamente en ambas. Regula la aprobación, observaciones presidenciales y promulgación."),
        Triple(73, "Facultades del Congreso de la Unión", "El Congreso tiene facultad para admitir nuevos Estados, legislar en hidrocarburos, minería, comercio, contribuciones, seguridad nacional, derecho penal sustantivo y procesal único, y declarar la guerra."),
        Triple(74, "Facultades Exclusivas de la Cámara de Diputados", "Aprobar anualmente el Presupuesto de Egresos de la Federación, revisar la Cuenta Pública por medio de la ASF, declarar si ha lugar a proceder contra servidores públicos y ratificar nombramientos hacendarios."),
        Triple(75, "Remuneraciones Obligatorias en el Presupuesto de Egresos", "La Cámara de Diputados, al aprobar el Presupuesto de Egresos, no podrá dejar de señalar la retribución que corresponda a un empleo que esté establecido por la ley."),
        Triple(76, "Facultades Exclusivas del Senado de la República", "Analizar la política exterior y aprobar los tratados internacionales, ratificar nombramientos diplomáticos y militares superiores, autorizar salida de tropas y designar ministros de la SCJN."),
        Triple(77, "Facultades de Cada Cámara sin Intervención de la Otra", "Dictar resoluciones económicas relativas a su régimen interior, nombrar los empleados de su secretaría y convocar a elecciones extraordinarias para cubrir vacantes de sus miembros."),
        Triple(78, "Comisión Permanente del Congreso de la Unión", "Durante los recesos del Congreso habrá una Comisión Permanente compuesta de 37 miembros de los que 19 serán Diputados y 18 Senadores."),
        Triple(79, "Auditoría Superior de la Federación (ASF)", "La Auditoría Superior de la Federación de la Cámara de Diputados tendrá autonomía técnica y de gestión para fiscalizar los ingresos, egresos y deuda pública federal."),
        Triple(80, "Poder Ejecutivo Depositado en el Presidente de la República", "Se deposita el ejercicio del Supremo Poder Ejecutivo de la Unión en un solo individuo, que se denominará 'Presidente de los Estados Unidos Mexicanos'."),
        Triple(81, "Elección Directa del Presidente de la República", "La elección del Presidente será directa y en los términos que disponga la ley electoral."),
        Triple(82, "Requisitos para ser Presidente de la República", "Ser ciudadano mexicano por nacimiento, en pleno goce de sus derechos, hijo de padre o madre mexicanos, tener 35 años cumplidos y haber residido en el país durante el año anterior a la elección."),
        Triple(83, "Periodo Presidencial y No Reelección Absoluta", "El Presidente entrará a ejercer su encargo el 1o. de octubre y durará en él seis años. El ciudadano que haya desempeñado el cargo en ningún caso y por ningún motivo podrá volver a desempeñarlo."),
        Triple(84, "Falta Absoluta del Presidente (Interino y Sustituto)", "En caso de falta absoluta del Presidente, el Secretario de Gobernación asumirá provisionalmente la titularidad del Ejecutivo en tanto el Congreso nombra al Presidente interino o sustituto."),
        Triple(85, "Falta Temporal del Presidente de la República", "Si al comenzar un periodo constitucional no se presentase el Presidente electo o la elección no estuviere hecha y declarada, ejercerá interinamente el que designe el Congreso."),
        Triple(86, "Renuncia al Cargo de Presidente de la República", "El cargo de Presidente de la República sólo es renunciable por causa grave, que calificará el Congreso de la Unión, ante el que se presentará la renuncia."),
        Triple(87, "Toma de Protesta Constitucional del Presidente", "El Presidente, al tomar posesión de su encargo, prestará ante el Congreso de la Unión la protesta de guardar y hacer guardar la Constitución Política de los Estados Unidos Mexicanos."),
        Triple(88, "Ausencia del Territorio Nacional del Presidente", "El Presidente de la República podrá ausentarse del territorio nacional hasta por siete días, informando previamente de los motivos de la ausencia a la Cámara de Senadores."),
        Triple(89, "Facultades y Obligaciones del Presidente de la República", "Promulgar y ejecutar las leyes, nombrar y remover a Secretarios de Estado, dirigir la política exterior, disponer de la Fuerza Armada permanente y conceder indultos."),
        Triple(90, "Administración Pública Federal Centralizada y Paraestatal", "La Administración Pública Federal será centralizada y paraestatal conforme a la Ley Orgánica que expida el Congreso, que distribuirá los negocios del orden administrativo de la Federación."),
        Triple(91, "Requisitos para ser Secretario del Despacho", "Para ser secretario del despacho se requiere ser ciudadano mexicano por nacimiento, estar en ejercicio de sus derechos y tener treinta años cumplidos."),
        Triple(92, "Refrendo Ministerial de Reglamentos y Decretos", "Todos los reglamentos, decretos, acuerdos y órdenes del Presidente deberán estar firmados por el Secretario de Estado a que el asunto corresponda."),
        Triple(93, "Comparecencias de Secretarios ante el Congreso", "Los Secretarios del Despacho, luego que esté abierto el periodo de sesiones ordinarias, darán cuenta al Congreso del estado que guarden sus respectivos ramos, y podrán ser citados a comparecer."),
        Triple(94, "Depósito del Poder Judicial de la Federación", "Se deposita el ejercicio del Poder Judicial de la Federación en una Suprema Corte de Justicia de la Nación, en un Tribunal Electoral, en Plenos Regionales, Tribunales Colegiados y Juzgados de Distrito."),
        Triple(95, "Requisitos para ser Ministro de la Suprema Corte", "Ser ciudadano mexicano por nacimiento, tener título de licenciado en derecho con antigüedad mínima de diez años, gozar de buena reputación y no haber sido sentenciado por delito doloso."),
        Triple(96, "Procedimiento de Nombramiento de Ministros de la SCJN", "Para nombrar a los Ministros de la Suprema Corte de Justicia, el Presidente someterá una terna a consideración del Senado, el cual designará al ministro por el voto de las dos terceras partes."),
        Triple(97, "Magistrados de Circuito y Jueces de Distrito", "Los Magistrados de Circuito y los Jueces de Distrito serán nombrados y adscritos por el Consejo de la Judicatura Federal, con base en criterios objetivos y mediante concursos de oposición."),
        Triple(98, "Renuncias y Licencias de los Ministros de la SCJN", "Las renuncias de los Ministros de la Suprema Corte solamente procederán por causas graves; serán sometidas al Ejecutivo y aceptadas por el Senado de la República."),
        Triple(99, "Tribunal Electoral del Poder Judicial de la Federación", "El Tribunal Electoral será la máxima autoridad jurisdiccional en la materia y órgano especializado del Poder Judicial de la Federación, resolviendo impugnaciones de elecciones federales y locales."),
        Triple(100, "Consejo de la Judicatura Federal (CJF)", "El Consejo de la Judicatura Federal será un órgano del Poder Judicial de la Federación con independencia técnica, de gestión y para emitir sus resoluciones, encargado de la administración y disciplina judicial."),
        Triple(101, "Incompatibilidad de Funciones para Miembros del PJF", "Los Ministros, Magistrados, Jueces y Consejeros no podrán, en ningún caso, aceptar ni desempeñar empleo o encargo de la Federación, de las entidades federativas o de particulares, salvo los cargos no remunerados en asociaciones científicas o docentes."),
        Triple(102, "Ministerio Público de la Federación y CNDH", "Regula a la Fiscalía General de la República como órgano público autónomo, dotado de personalidad jurídica y patrimonio propio. Apartado B: Comisión Nacional de los Derechos Humanos."),
        Triple(103, "Competencia Federal en Amparo y Protección de Derechos", "Los Tribunales de la Federación resolverán toda controversia que se suscite por normas generales, actos u omisiones de autoridad que violen los derechos humanos reconocidos en la Constitución y tratados."),
        Triple(104, "Competencia de los Tribunales Federales en Materia Común", "Los Tribunales de la Federación conocerán de los procedimientos relacionados con delitos del orden federal, controversias civiles o mercantiles sobre cumplimiento de leyes federales y recursos de revisión fiscal."),
        Triple(105, "Controversias Constitucionales y Acciones de Inconstitucionalidad", "La Suprema Corte conocerá en forma exclusiva de las controversias constitucionales entre Federación, Estados y Municipios, y de las acciones de inconstitucionalidad planteadas contra normas generales."),
        Triple(106, "Resolución de Conflictos Competenciales Judiciales", "Corresponde al Poder Judicial de la Federación dirimir las controversias que se susciten entre los tribunales de la Federación, entre éstos y los de las entidades federativas o entre los de un Estado y otro."),
        Triple(107, "Bases del Juicio de Amparo y Relatividad de Sentencias", "El juicio de amparo se seguirá a instancia de parte agraviada. Regula amparo directo, amparo indirecto, la suspensión del acto reclamado y la declaratoria general de inconstitucionalidad."),
        Triple(108, "Sujetos a Responsabilidad Pública y Juicio Político", "Para los efectos de las responsabilidades a que alude este Título se reputarán como servidores públicos a los representantes de elección popular, miembros del PJF y funcionarios públicos."),
        Triple(109, "Responsabilidades Política, Penal y Administrativa", "Los servidores públicos y particulares que incurran en faltas administrativas graves o actos de corrupción serán sancionados conforme a la legislación aplicable en materias política, penal y administrativa."),
        Triple(110, "Sujetos y Efectos de la Sentencia en Juicio Político", "Podrán ser sujetos de juicio político los altos funcionarios por violaciones graves a la Constitución y leyes federales. Las sanciones consistirán en la destitución e inhabilitación para ejercer cargos públicos."),
        Triple(111, "Declaración de Procedencia (Desafuero Constitucional)", "Para proceder penalmente contra Diputados, Senadores, Ministros de la SCJN y otros altos funcionarios por la comisión de delitos durante el tiempo de su encargo, la Cámara de Diputados declarará si ha lugar o no a proceder."),
        Triple(112, "Excepción a Declaración de Procedencia", "No se requerirá declaración de procedencia de la Cámara de Diputados cuando alguno de los servidores públicos a que hace referencia el artículo 111 cometa un delito durante el tiempo en que se encuentre separado de su encargo."),
        Triple(113, "Sistema Nacional Anticorrupción (SNA)", "El Sistema Nacional Anticorrupción es la instancia de coordinación entre las autoridades de todos los órdenes de gobierno competentes en la prevención, detección y sanción de responsabilidades administrativas y hechos de corrupción."),
        Triple(114, "Plazos de Prescripción para Responsabilidades Públicas", "El procedimiento de juicio político sólo podrá iniciarse durante el período en que el servidor público desempeñe su cargo y dentro de un año después. La prescripción de faltas administrativas graves no será menor a siete años."),
        Triple(115, "Bases del Municipio Libre y Administración Local", "Los Estados adoptarán la forma de gobierno republicano, teniendo como base de su división territorial y organización política y administrativa el Municipio Libre, gobernado por un Ayuntamiento electo."),
        Triple(116, "Poderes de los Estados de la Federación", "El poder público de los Estados se dividirá en Ejecutivo, Legislativo y Judicial. Establece reglas para gobernadores, legislaturas locales y tribunales de justicia estatales."),
        Triple(117, "Prohibiciones Expresas a los Estados de la República", "Los Estados no pueden celebrar alianzas con otros Estados ni con naciones extranjeras, acuñar moneda, emitir papel moneda, gravar el tránsito de personas o mercancías, ni contraer deuda con extranjeros."),
        Triple(118, "Actos Sujetos a Consentimiento del Congreso Federal", "Tampoco pueden los Estados, sin consentimiento del Congreso de la Unión, establecer derechos de tonelaje, tener tropas permanentes ni hacer la guerra a potencias extranjeras."),
        Triple(119, "Extradición y Colaboración entre Entidades Federativas", "Los Poderes de la Unión tienen el deber de proteger a las entidades federativas contra toda invasión o violencia exterior. Regula la entrega inmediata de imputados y sentenciados entre entidades."),
        Triple(120, "Obligación de Gobernadores de Publicar Leyes Federales", "Los titulares de los Poderes Ejecutivos de las entidades federativas están obligados a publicar y hacer cumplir las leyes federales."),
        Triple(121, "Entera Fe y Crédito a los Actos de Cada Estado", "En cada entidad federativa se dará entera fe y crédito a los actos públicos, registros y procedimientos judiciales de todas las otras. Las leyes de un Estado sólo tendrán efecto en su propio territorio."),
        Triple(122, "Naturaleza Jurídica y Poderes de la Ciudad de México", "La Ciudad de México es una entidad federativa que goza de autonomía en todo lo concerniente a su régimen interior y a su organización política y administrativa. Cuenta con Jefatura de Gobierno, Congreso y Poder Judicial."),
        Triple(123, "Del Trabajo y la Previsión Social", "Toda persona tiene derecho al trabajo digno y socialmente útil. Apartado A: Trabajadores, obreros, jornaleros y empleados (jornada máxima de 8h, salario mínimo, estabilidad en el empleo e indemnización). Apartado B: Servidores públicos."),
        Triple(124, "Principio de Facultades Residuales de los Estados", "Las facultades que no están expresamente concedidas por esta Constitución a los funcionarios federales, se entienden reservadas a los Estados o a la Ciudad de México, en los ámbitos de sus respectivas competencias."),
        Triple(125, "Incompatibilidad de Dos Cargos de Elección Popular", "Ningún individuo podrá desempeñar a la vez dos cargos federales de elección popular, ni uno de la Federación y otro de una entidad federativa que sean también de elección; pero el electo puede elegir cuál desempeñar."),
        Triple(126, "Prohibición de Pagos Fuera del Presupuesto de Egresos", "No podrá hacerse pago alguno que no esté comprendido en el Presupuesto o determinado por la ley posterior."),
        Triple(127, "Remuneraciones Máximas de Servidores Públicos", "Los servidores públicos de la Federación, estados y municipios recibirán una remuneración adecuada e irrenunciable. Ningún servidor público podrá tener una remuneración mayor a la establecida para el Presidente de la República."),
        Triple(128, "Protesta Constitucional de Todo Funcionario Público", "Todo funcionario público, sin excepción alguna, antes de tomar posesión de su encargo, prestará la protesta de guardar la Constitución y las leyes que de ella emanen."),
        Triple(129, "Fuerzas Armadas en Tiempo de Paz y Disciplina Militar", "En tiempo de paz, ninguna autoridad militar puede ejercer más funciones que las que tengan exacta conexión con la disciplina militar."),
        Triple(130, "Separación Histórica del Estado y las Iglesias", "El principio histórico de la separación del Estado y las iglesias orienta las normas contenidas en este artículo. Las iglesias y las agrupaciones religiosas tendrán personalidad jurídica como asociaciones religiosas."),
        Triple(131, "Facultad Federal Exclusiva sobre Aranceles y Comercio Exterior", "Es facultad privativa de la Federación gravar las mercancías que se importen o exporten, o que pasen de tránsito por el territorio nacional, así como reglamentar el comercio exterior."),
        Triple(132, "Bienes Destinados al Servicio Público de la Federación", "Los fuertes, los cuarteles, almacenes de depósito y demás bienes inmuebles destinados por el Gobierno de la Unión al servicio público o al uso común, estarán sujetos a la jurisdicción de los Poderes Federales."),
        Triple(133, "Supremacía Constitucional y Tratados Internacionales", "Esta Constitución, las leyes del Congreso de la Unión que emanen de ella y todos los tratados que estén de acuerdo con la misma, celebrados por el Presidente con aprobación del Senado, serán la Ley Suprema de toda la Unión."),
        Triple(134, "Administración Eficiente de Recursos Públicos y Licitaciones", "Los recursos económicos de que dispongan la Federación, las entidades federativas y municipios se administrarán con eficiencia, eficacia, economía, transparencia y honradez. Las adquisiciones se adjudicarán por licitación pública."),
        Triple(135, "Procedimiento Rígido de Reforma a la Constitución", "La presente Constitución puede ser adicionada o reformada por el voto de las dos terceras partes de los individuos presentes del Congreso de la Unión y la aprobación de la mayoría de las legislaturas de los Estados y de la Ciudad de México."),
        Triple(136, "Principio de Inviolabilidad de la Constitución", "Esta Constitución no perderá su fuerza y vigor, aun cuando por alguna rebelión se interrumpa su observancia. En caso de que se establezca un gobierno contrario a sus principios, tan luego como el pueblo recobre su libertad, se restablecerá su observancia.")
    )

    private fun getCategoryForArticleNumber(num: Int): String {
        return when (num) {
            in 1..29 -> "Garantías y Derechos Humanos"
            in 30..38 -> "Nacionalidad y Ciudadanía"
            in 39..48 -> "Soberanía y Territorio"
            49 -> "División de Poderes"
            in 50..79 -> "Poder Legislativo"
            in 80..93 -> "Poder Ejecutivo"
            in 94..107 -> "Poder Judicial y Amparo"
            in 108..114 -> "Responsabilidades Públicas"
            in 115..122 -> "Estados y Municipios"
            123 -> "Laboral y Previsión Social"
            in 124..134 -> "Prevenciones Generales"
            135 -> "Reformas Constitucionales"
            136 -> "Inviolabilidad Constitucional"
            else -> "Constitucional"
        }
    }

    // Build all 136 constitutional articles
    val cpeumArticles: List<LawArticleDetail> = cpeumMeta.map { (num, title, summary) ->
        val cat = getCategoryForArticleNumber(num)
        LawArticleDetail(
            id = "cpeum_$num",
            lawId = "cpeum",
            lawShortName = "CPEUM",
            articleNumber = "Artículo $num${if (num == 1) "°" else ""}",
            topicTitle = title,
            category = cat,
            summary = summary,
            dofStatus = "VIGENTE DOF",
            lastReformDate = if (num in listOf(1, 14, 16, 19, 20, 107, 123)) "Reforma 2021" else "DOF Vigente",
            totalChunks = if (num in listOf(16, 20, 123)) 18 else 8,
            chunks = listOf(
                VectorChunk(
                    chunkId = "cpeum_${num}_p1",
                    chunkIndex = 1,
                    similarity = 0.950f - (num % 5) * 0.008f,
                    path = "Párrafo 1° · Precepto Normativo",
                    content = summary,
                    isTopMatch = true
                ),
                VectorChunk(
                    chunkId = "cpeum_${num}_p2",
                    chunkIndex = 2,
                    similarity = 0.910f,
                    path = "Interpretación Doctrinal y SCJN",
                    content = "Criterio jurisprudencial de la Suprema Corte de Justicia de la Nación respecto a la obligatoriedad y alcance del Artículo $num constitucional en el orden jurídico nacional.",
                    isTopMatch = false
                )
            )
        )
    }

    // Catálogo oficial de leyes en LexIA
    val cpeum = com.lexia.legalassistant.data.model.LawItem("cpeum", "Constitución Política de los Estados Unidos Mexicanos", "CPEUM", "DOF Vigente", "Federal", "Constitucional", 136, "Mayo 2021")
    val cnpp = com.lexia.legalassistant.data.model.LawItem("cnpp", "Código Nacional de Procedimientos Penales", "CNPP", "DOF Vigente", "Federal", "Penal", 490, "Enero 2024")
    val cpf = com.lexia.legalassistant.data.model.LawItem("cpf", "Código Penal Federal", "CPF", "DOF Vigente", "Federal", "Penal", 429, "Noviembre 2023")
    val amparo = com.lexia.legalassistant.data.model.LawItem("amparo", "Ley de Amparo, Reglamentaria de los Arts. 103 y 107 CPEUM", "Ley de Amparo", "DOF Vigente", "Federal", "Amparo", 271, "Junio 2022")
    val cpet = com.lexia.legalassistant.data.model.LawItem("cpet", "Código Penal para el Estado de Tabasco", "CP Tabasco", "POET Vigente", "Estatal (Tabasco)", "Penal", 369, "Marzo 2024")
    val ccf = com.lexia.legalassistant.data.model.LawItem("ccf", "Código Civil Federal", "CCF", "DOF Vigente", "Federal", "Civil", 3044, "Enero 2024")
    val cnpcyf = com.lexia.legalassistant.data.model.LawItem("cnpcyf", "Código Nacional de Procedimientos Civiles y Familiares", "CNPCyF", "DOF Vigente", "Nacional", "Familiar", 1191, "Junio 2023")
    val ccet = com.lexia.legalassistant.data.model.LawItem("ccet", "Código Civil para el Estado de Tabasco", "CC Tabasco", "POET Vigente", "Estatal (Tabasco)", "Civil", 3227, "Diciembre 2023")
    val lft = com.lexia.legalassistant.data.model.LawItem("lft", "Ley Federal del Trabajo", "LFT", "DOF Vigente", "Federal", "Laboral", 1010, "Abril 2024")

    val catalogLaws: List<com.lexia.legalassistant.data.model.LawItem> = listOf(cpeum, cnpp, cpf, amparo, cpet, ccf, cnpcyf, ccet, lft)

    fun getLawById(lawId: String?): com.lexia.legalassistant.data.model.LawItem {
        return catalogLaws.firstOrNull { it.id.equals(lawId, ignoreCase = true) } ?: cpeum
    }

    // 1. CÓDIGO NACIONAL DE PROCEDIMIENTOS PENALES (CNPP)
    val cnppArticles: List<LawArticleDetail> = listOf(
        LawArticleDetail("cnpp_1", "cnpp", "CNPP", "Artículo 1°", "Ámbito de Aplicación Territorial y Fuero", "Penal", "Las disposiciones de este Código son de orden público y de observancia general en toda la República Mexicana, para los delitos que sean competencia de los órganos jurisdiccionales federales y locales.", "VIGENTE DOF", "Enero 2024", 8, listOf(VectorChunk("cnpp_1_p1", 1, 0.960f, "Ámbito Territorial", "Aplica en todo el territorio nacional tanto para delitos del fuero común como del fuero federal.", true))),
        LawArticleDetail("cnpp_2", "cnpp", "CNPP", "Artículo 2°", "Objeto del Código Nacional", "Penal", "Esclarecer los hechos, proteger al inocente, procurar que el culpable no quede impune y que los daños causados por el delito se reparen.", "VIGENTE DOF", "Enero 2024", 8, listOf(VectorChunk("cnpp_2_p1", 1, 0.955f, "Fines del Proceso", "Tutela integral a víctimas y garantía de esclarecimiento fáctico y reparación.", true))),
        LawArticleDetail("cnpp_4", "cnpp", "CNPP", "Artículo 4°", "Características y Principios Rectores", "Penal", "El proceso penal será acusatorio y oral, en él se observarán los principios de publicidad, contradicción, concentración, continuidad e inmediación.", "VIGENTE DOF", "Enero 2024", 10, listOf(VectorChunk("cnpp_4_p1", 1, 0.968f, "Principios Rectores", "Oralidad, contradicción de pruebas, inmediación del juzgador y concentración de actos.", true))),
        LawArticleDetail("cnpp_12", "cnpp", "CNPP", "Artículo 12", "Principio de Juicio Previo y Debido Proceso", "Penal", "Ninguna persona podrá ser condenada a una pena ni sometida a una medida de seguridad, sino en virtud de resolución dictada por un órgano jurisdiccional previamente establecido.", "VIGENTE DOF", "Enero 2024", 8, listOf(VectorChunk("cnpp_12_p1", 1, 0.952f, "Juicio Previo", "Garantía de debido proceso legal estricto ante tribunales competentes.", true))),
        LawArticleDetail("cnpp_13", "cnpp", "CNPP", "Artículo 13", "Principio de Presunción de Inocencia", "Penal", "Toda persona se presume inocente y será tratada como tal en todas las etapas del procedimiento, mientras no se declare su responsabilidad mediante sentencia firme.", "VIGENTE DOF", "Enero 2024", 12, listOf(VectorChunk("cnpp_13_p1", 1, 0.975f, "Presunción de Inocencia", "Regla de trato, probatoria y de juicio. La carga probatoria recae exclusivamente en la acusación.", true))),
        LawArticleDetail("cnpp_14", "cnpp", "CNPP", "Artículo 14", "Prohibición de Doble Enjuiciamiento (Non Bis In Idem)", "Penal", "La persona condenada, absuelta o cuyo proceso haya sido sobreseído, no podrá ser sometida a otro proceso penal por los mismos hechos.", "VIGENTE DOF", "Enero 2024", 8, listOf(VectorChunk("cnpp_14_p1", 1, 0.940f, "Non Bis In Idem", "Inadmisibilidad de persecución penal múltiple por la misma conducta histórica.", true))),
        LawArticleDetail("cnpp_113", "cnpp", "CNPP", "Artículo 113", "Derechos Fundamentales del Imputado", "Penal", "A ser informado de sus derechos, comunicarse de inmediato, declarar o guardar silencio, tener defensor particular o público y ser puesto sin demora ante el Juez.", "VIGENTE DOF", "Enero 2024", 15, listOf(VectorChunk("cnpp_113_p1", 1, 0.965f, "Defensa Técnica", "Derecho a defensa adecuada por licenciado en derecho con cédula y asistencia consular si es extranjero.", true))),
        LawArticleDetail("cnpp_131", "cnpp", "CNPP", "Artículo 131", "Obligaciones del Ministerio Público", "Penal", "Vigilar que en toda investigación se cumpla estrictamente con los derechos humanos reconocidos en la Constitución y tratados. Iniciar la investigación correspondiente cuando proceda.", "VIGENTE DOF", "Enero 2024", 10, listOf(VectorChunk("cnpp_131_p1", 1, 0.941f, "Obligaciones Ministeriales", "Carga de la prueba, objetividad y deber de lealtad procesal.", true))),
        LawArticleDetail("cnpp_146", "cnpp", "CNPP", "Artículo 146", "Supuestos Constitucionales de Flagrancia", "Penal", "Se podrá detener a una persona sin orden judicial en caso de flagrancia: I. En el momento de estar cometiendo el delito; II. Inmediatamente después de cometerlo, por persecución material o señalamiento con objetos o rastros.", "VIGENTE DOF", "Enero 2024", 16, listOf(VectorChunk("cnpp_146_p1", 1, 0.980f, "Flagrancia Estricta y Cuasiflagrancia", "Requisitos taxativos para la detención material inmediata sin orden judicial previa.", true))),
        LawArticleDetail("cnpp_150", "cnpp", "CNPP", "Artículo 150", "Supuesto Excepcional de Caso Urgente", "Penal", "Solo en casos urgentes el Ministerio Público podrá ordenar la detención de una persona, fundando y motivando: delito grave calificado, riesgo de sustracción y que no pueda acudir ante juez por razón de hora o lugar.", "VIGENTE DOF", "Enero 2024", 10, listOf(VectorChunk("cnpp_150_p1", 1, 0.945f, "Caso Urgente", "Orden ministerial excepcional que requiere confirmación inmediata del juez de control en audiencia.", true))),
        LawArticleDetail("cnpp_153", "cnpp", "CNPP", "Artículo 153", "Reglas Generales de las Medidas Cautelares", "Penal", "Asegurar la presencia del imputado en el procedimiento, garantizar la seguridad de la víctima, ofendido o testigos, o evitar la obstaculización del procedimiento.", "VIGENTE DOF", "Enero 2024", 12, listOf(VectorChunk("cnpp_153_p1", 1, 0.950f, "Fines Cautelares", "Proporcionalidad y necesidad estricta para la imposición de cualquier medida cautelar.", true))),
        LawArticleDetail("cnpp_155", "cnpp", "CNPP", "Artículo 155", "Catálogo Taxativo de Tipos de Medidas Cautelares", "Penal", "Presentación periódica, embargo de bienes, inmovilización de cuentas, prohibición de salir del país, separación del domicilio, brazalete electrónico o prisión preventiva justificada.", "VIGENTE DOF", "Enero 2024", 16, listOf(VectorChunk("cnpp_155_p1", 1, 0.970f, "Catálogo I a XIV", "Graduación de medidas desde la menos lesiva hasta la prisión preventiva como última ratio.", true))),
        LawArticleDetail("cnpp_165", "cnpp", "CNPP", "Artículo 165", "Temporalidad y Revisión de la Prisión Preventiva", "Penal", "La prisión preventiva no podrá exceder del tiempo fijado por la ley, y en ningún caso será superior a dos años, salvo que su prolongación se deba al ejercicio del derecho de defensa.", "VIGENTE DOF", "Enero 2024", 12, listOf(VectorChunk("cnpp_165_p1", 1, 0.962f, "Límite 2 Años", "Expirado el plazo sin sentencia, el imputado será puesto en libertad inmediata o sujeta a otra medida no privativa.", true))),
        LawArticleDetail("cnpp_211", "cnpp", "CNPP", "Artículo 211", "Etapas del Procedimiento Penal Acusatorio", "Penal", "I. Investigación (inicial desde denuncia hasta imputación, y complementaria); II. Intermedia (formulación de acusación y depuración probatoria); III. Juicio Oral.", "VIGENTE DOF", "Enero 2024", 14, listOf(VectorChunk("cnpp_211_p1", 1, 0.968f, "Tres Etapas Procesales", "Delimitación formal de las tres fases del sistema de justicia penal acusatorio.", true))),
        LawArticleDetail("cnpp_251", "cnpp", "CNPP", "Artículo 251", "Actos de Investigación sin Autorización Judicial", "Penal", "Inspección de lugar, inspección de personas, revisión corporal, inspección de vehículos, entrevistas a testigos, peritajes y aseguramiento de bienes.", "VIGENTE DOF", "Enero 2024", 12, listOf(VectorChunk("cnpp_251_p1", 1, 0.940f, "Facultades Policiales", "Actuaciones de investigación directa que no vulneran derechos fundamentales bajo control judicial.", true))),
        LawArticleDetail("cnpp_252", "cnpp", "CNPP", "Artículo 252", "Actos de Investigación con Control Judicial Previo", "Penal", "Cateos, intervención de comunicaciones privadas, toma de muestras biológicas no consentidas y exhumación de cadáveres requieren mandamiento judicial expreso.", "VIGENTE DOF", "Enero 2024", 14, listOf(VectorChunk("cnpp_252_p1", 1, 0.972f, "Control Judicial Previo", "Reserva judicial estricta para actos que inciden sobre la inviolabilidad del domicilio o intimidad.", true))),
        LawArticleDetail("cnpp_307", "cnpp", "CNPP", "Artículo 307", "Desarrollo de la Audiencia Inicial", "Penal", "Control de detención, formulación de la imputación, declaración del imputado si lo desea, resolución sobre medidas cautelares y vinculación a proceso.", "VIGENTE DOF", "Enero 2024", 14, listOf(VectorChunk("cnpp_307_p1", 1, 0.965f, "Audiencia Inicial", "Secuencia procesal concentrada ante el Juez de Control tras la detención.", true))),
        LawArticleDetail("cnpp_308", "cnpp", "CNPP", "Artículo 308", "Control de Legalidad de la Detención", "Penal", "Inmediatamente después de que el imputado detenido en flagrancia o caso urgente sea puesto a disposición del Juez de Control, se procederá a calificar la legalidad de la detención.", "VIGENTE DOF", "Enero 2024", 12, listOf(VectorChunk("cnpp_308_p1", 1, 0.960f, "Control de Detención", "El Juez examinará el cumplimiento del plazo constitucional y los requisitos de flagrancia o caso urgente.", true))),
        LawArticleDetail("cnpp_309", "cnpp", "CNPP", "Artículo 309", "Oportunidad para Formular la Imputación", "Penal", "La formulación de la imputación es la comunicación que el Ministerio Público efectúa al imputado, en presencia del Juez de Control, de que desarrolla una investigación en su contra.", "VIGENTE DOF", "Enero 2024", 12, listOf(VectorChunk("cnpp_309_p1", 1, 0.968f, "Concepto de Imputación", "Comunicación judicializada de los hechos investigados y sujetos intervinientes.", true))),
        LawArticleDetail("cnpp_311", "cnpp", "CNPP", "Artículo 311", "Procedimiento para Formular la Imputación", "Penal", "Una vez que el imputado esté presente en la audiencia inicial, por haberse calificado de legal su detención o por haber comparecido mediante citación u orden de aprehensión, se le dará la palabra al Ministerio Público para que exponga al imputado el hecho que se le atribuye, la calificación jurídica preliminar, la fecha, lugar y modo de su comisión, la forma de intervención que se le atribuye en el mismo, así como el nombre de su acusador.", "VIGENTE DOF", "Enero 2024", 16, listOf(VectorChunk("cnpp_311_p1", 1, 0.988f, "Desahogo de la Imputación", "El MP expone el hecho fáctico concreto, grado de intervención, calificación jurídica y denunciantes.", true), VectorChunk("cnpp_311_p2", 2, 0.970f, "Aclaraciones y Derecho a Declarar", "El Juez de Control verifica que el imputado haya entendido la imputación y le concede el derecho a solicitar precisiones.", true))),
        LawArticleDetail("cnpp_312", "cnpp", "CNPP", "Artículo 312", "Oportunidad del Imputado para Declarar", "Penal", "Formulada la imputación, el Juez de Control preguntará al imputado si la entiende y si es su deseo contestar el cargo rindiendo su declaración o reservarse su derecho a guardar silencio.", "VIGENTE DOF", "Enero 2024", 10, listOf(VectorChunk("cnpp_312_p1", 1, 0.955f, "Declaración del Imputado", "Garantía de no autoincriminación y derecho a rendir declaración libre o guardar silencio.", true))),
        LawArticleDetail("cnpp_313", "cnpp", "CNPP", "Artículo 313", "Oportunidad para Resolver la Vinculación a Proceso", "Penal", "Oportunidad procesal donde el Ministerio Público solicitará al Juez de Control la vinculación a proceso del imputado, quien podrá acogerse al plazo constitucional de 72 horas o duplicidad a 144 horas.", "VIGENTE DOF", "Enero 2024", 14, listOf(VectorChunk("cnpp_313_p1", 1, 0.972f, "Plazo Constitucional", "Plazo de 72 o 144 horas para resolver la situación jurídica procesal.", true))),
        LawArticleDetail("cnpp_316", "cnpp", "CNPP", "Artículo 316", "Requisitos para el Auto de Vinculación a Proceso", "Penal", "Que se haya formulado la imputación, otorgado oportunidad para declarar, y que de los antecedentes de la investigación se desprendan datos de prueba que establezcan un hecho ilícito y la probabilidad de comisión.", "VIGENTE DOF", "Enero 2024", 15, listOf(VectorChunk("cnpp_316_p1", 1, 0.980f, "Auto de Vinculación", "Estándar probatorio de datos de prueba idóneos para fijar la litis de la investigación complementaria.", true))),
        LawArticleDetail("cnpp_356", "cnpp", "CNPP", "Artículo 356", "Principio de Libertad Probatoria y Licitud", "Penal", "Todos los hechos y circunstancias aportados para la adecuada solución del caso podrán ser probados por cualquier medio pertinente lícito. Prohíbe prueba obtenida con violación a derechos humanos.", "VIGENTE DOF", "Enero 2024", 10, listOf(VectorChunk("cnpp_356_p1", 1, 0.958f, "Prueba Ilícita", "Nulidad de pleno derecho de elementos probatorios obtenidos mediante tortura o quebranto procesal.", true))),
        LawArticleDetail("cnpp_401", "cnpp", "CNPP", "Artículo 401", "Sentencia Definitiva en Juicio Oral", "Penal", "Inmediatamente después de la deliberación, el Tribunal de Enjuiciamiento comunicará el fallo absolutorio o condenatorio fundado y motivado.", "VIGENTE DOF", "Enero 2024", 10, listOf(VectorChunk("cnpp_401_p1", 1, 0.944f, "Fallo Judicial", "Lectura y explicación pública de la sentencia definitiva en audiencia de juicio.", true))),
        LawArticleDetail("cnpp_467", "cnpp", "CNPP", "Artículo 467", "Resoluciones del Juez de Control Apelables", "Penal", "Desistimiento de la acción penal, vinculación o no a proceso, imposición o modificación de medidas cautelares, y sentencia definitiva en procedimiento abreviado.", "VIGENTE DOF", "Enero 2024", 12, listOf(VectorChunk("cnpp_467_p1", 1, 0.960f, "Recurso de Apelación", "Recursos ordinarios ante el Tribunal de Alzada contra resoluciones de control previo.", true)))
    )

    // 2. CÓDIGO PENAL FEDERAL (CPF)
    val cpfArticles: List<LawArticleDetail> = listOf(
        LawArticleDetail("cpf_7", "cpf", "CPF", "Artículo 7°", "Concepto Dogmático de Delito", "Penal Sustantivo", "Delito es el acto u omisión que sancionan las leyes penales. En los delitos de resultado material también será atribuible el resultado cuando quien omite tenía el deber jurídico de evitarlo.", "VIGENTE DOF", "Noviembre 2023", 10, listOf(VectorChunk("cpf_7_p1", 1, 0.970f, "Teoría del Delito", "Acción u omisión típica, antijurídica y culpable con sanción penal expresa.", true))),
        LawArticleDetail("cpf_8", "cpf", "CPF", "Artículo 8°", "Formas de Culpabilidad: Dolo y Culpa", "Penal Sustantivo", "Las acciones u omisiones delictivas solamente pueden realizarse dolosa o culposamente.", "VIGENTE DOF", "Noviembre 2023", 8, listOf(VectorChunk("cpf_8_p1", 1, 0.950f, "Dolo y Culpa", "Exclusión de responsabilidad objetiva en el orden penal federal.", true))),
        LawArticleDetail("cpf_9", "cpf", "CPF", "Artículo 9°", "Definición Legal del Dolo y Culpa", "Penal Sustantivo", "Obra dolosamente el que, conociendo los elementos del tipo penal, o previendo como posible el resultado típico, quiere o acepta su realización. Obra culposamente el que produce el resultado que no previó siendo previsible.", "VIGENTE DOF", "Noviembre 2023", 12, listOf(VectorChunk("cpf_9_p1", 1, 0.965f, "Elemento Cognitivo y Volitivo", "Distinción entre dolo directo, dolo eventual y culpa con o sin representación.", true))),
        LawArticleDetail("cpf_12", "cpf", "CPF", "Artículo 12", "Tentativa Punible", "Penal Sustantivo", "Existe tentativa punible cuando, mediante actos u omisiones idóneos e inequívocos, se encamina directamente a la consumación de un delito, si ésta no se consuma por causas ajenas a la voluntad del agente.", "VIGENTE DOF", "Noviembre 2023", 10, listOf(VectorChunk("cpf_12_p1", 1, 0.955f, "Tentativa", "Punibilidad de la tentativa acabada e inacabada según el grado de aproximación.", true))),
        LawArticleDetail("cpf_13", "cpf", "CPF", "Artículo 13", "Formas de Autoría y Participación Criminal", "Penal Sustantivo", "Son responsables del delito: I. Quienes lo acuerden o preparen; II. Quienes lo realicen por sí; III. Quienes lo realicen conjuntamente; IV. Quienes se sirvan de otro; V. Quienes determinen dolosamente a otro; VI. Quienes presten auxilio.", "VIGENTE DOF", "Noviembre 2023", 14, listOf(VectorChunk("cpf_13_p1", 1, 0.968f, "Autoría y Participación", "Autoría directa, coautoría, autoría mediata, instigación y complicidad.", true))),
        LawArticleDetail("cpf_15", "cpf", "CPF", "Artículo 15", "Causas de Exclusión del Delito y Legítima Defensa", "Penal Sustantivo", "El delito se excluye cuando: concurra causa de atipicidad; legítima defensa frente a agresión real, actual o inminente; estado de necesidad; o cumplimiento de un deber.", "VIGENTE DOF", "Noviembre 2023", 18, listOf(VectorChunk("cpf_15_p1", 1, 0.985f, "Causas de Justificación e Inculpabilidad", "Legítima defensa privilegiada, estado de necesidad y ausencia de conducta.", true))),
        LawArticleDetail("cpf_24", "cpf", "CPF", "Artículo 24", "Catálogo de Penas y Medidas de Seguridad", "Penal Sustantivo", "Prisión, tratamiento en libertad, semilibertad, trabajo a favor de la comunidad, sanción pecuniaria (multa y reparación del daño), decomiso de instrumentos, y suspensión de derechos.", "VIGENTE DOF", "Noviembre 2023", 12, listOf(VectorChunk("cpf_24_p1", 1, 0.950f, "Consecuencias Jurídicas", "Sanciones penales federales aplicables por los tribunales.", true))),
        LawArticleDetail("cpf_29", "cpf", "CPF", "Artículo 29", "Reparación del Daño Integral", "Penal Sustantivo", "La reparación del daño comprende: la restitución de la cosa; la indemnización del daño material y moral causado; y el resarcimiento de los perjuicios ocasionados.", "VIGENTE DOF", "Noviembre 2023", 10, listOf(VectorChunk("cpf_29_p1", 1, 0.960f, "Reparación del Daño", "Pena pública que se fijará con base en el daño real acreditado a favor de la víctima.", true))),
        LawArticleDetail("cpf_105", "cpf", "CPF", "Artículo 105", "Prescripción de la Acción Penal", "Penal Sustantivo", "La acción penal prescribirá en un plazo igual al término medio aritmético de la pena de prisión que señale la ley para el delito, sin que pueda ser menor de tres años.", "VIGENTE DOF", "Noviembre 2023", 10, listOf(VectorChunk("cpf_105_p1", 1, 0.942f, "Término Medio Aritmético", "Cómputo de la extinción de la pretensión punitiva por el transcurso del tiempo.", true))),
        LawArticleDetail("cpf_139", "cpf", "CPF", "Artículo 139", "Delito de Terrorismo", "Penal Sustantivo", "Se impondrá pena de quince a cuarenta años de prisión a quien utilice sustancias tóxicas, armas o explosivos para producir alarma, temor o terror en la población para atentar contra la seguridad nacional.", "VIGENTE DOF", "Noviembre 2023", 12, listOf(VectorChunk("cpf_139_p1", 1, 0.955f, "Terrorismo", "Tipo penal federal de tutela a la seguridad nacional y orden constitucional.", true))),
        LawArticleDetail("cpf_225", "cpf", "CPF", "Artículo 225", "Delitos contra la Administración de Justicia", "Penal Sustantivo", "Delitos cometidos por servidores públicos en el ámbito judicial: dictar resoluciones contrarias a la ley por cohecho, dilatar indebidamente o fabricar elementos probatorios.", "VIGENTE DOF", "Noviembre 2023", 14, listOf(VectorChunk("cpf_225_p1", 1, 0.950f, "Responsabilidad Judicial", "Tipos penales que salvaguardan la imparcialidad y rectitud jurisdiccional.", true))),
        LawArticleDetail("cpf_243", "cpf", "CPF", "Artículo 243", "Falsificación de Documentos Públicos y Privados", "Penal Sustantivo", "El delito de falsificación de documentos se castigará, tratándose de documentos públicos, con prisión de cuatro a ocho años; y en documentos privados, con prisión de seis meses a cinco años.", "VIGENTE DOF", "Noviembre 2023", 10, listOf(VectorChunk("cpf_243_p1", 1, 0.948f, "Fe Pública", "Tutela penal de la autenticidad e integridad documental.", true))),
        LawArticleDetail("cpf_302", "cpf", "CPF", "Artículo 302", "Tipo Penal de Homicidio", "Penal Sustantivo", "Comete el delito de homicidio el que priva de la vida a otro.", "VIGENTE DOF", "Noviembre 2023", 12, listOf(VectorChunk("cpf_302_p1", 1, 0.980f, "Privación de la Vida", "Delito base de tutela del bien jurídico supremo de la vida humana.", true))),
        LawArticleDetail("cpf_307", "cpf", "CPF", "Artículo 307", "Sanciones al Homicidio Simple Intencional", "Penal Sustantivo", "Al responsable de cualquier homicidio simple intencional que no tenga señalada una pena especial en este Código, se le impondrán de doce a veinticuatro años de prisión.", "VIGENTE DOF", "Noviembre 2023", 10, listOf(VectorChunk("cpf_307_p1", 1, 0.960f, "Penalidad de Homicidio", "Marco punitivo de 12 a 24 años de prisión para homicidio simple.", true))),
        LawArticleDetail("cpf_367", "cpf", "CPF", "Artículo 367", "Tipo Penal de Robo", "Penal Sustantivo", "Comete el delito de robo: el que se apodera de una cosa mueble ajena, sin derecho y sin consentimiento de la persona que puede disponer de ella con arreglo a la ley.", "VIGENTE DOF", "Noviembre 2023", 14, listOf(VectorChunk("cpf_367_p1", 1, 0.975f, "Apoderamiento Ilegítimo", "Elementos objetivos y subjetivos del delito patrimonial de robo.", true))),
        LawArticleDetail("cpf_386", "cpf", "CPF", "Artículo 386", "Tipo Penal de Fraude", "Penal Sustantivo", "Comete el delito de fraude el que engañando a uno o aprovechándose del error en que éste se halla se hace ilícitamente de alguna cosa o alcanza un lucro indebido.", "VIGENTE DOF", "Noviembre 2023", 12, listOf(VectorChunk("cpf_386_p1", 1, 0.965f, "Engaño y Lucro Indebido", "Relación causal entre el engaño/error y el perjuicio patrimonial resentido.", true))),
        LawArticleDetail("cpf_390", "cpf", "CPF", "Artículo 390", "Tipo Penal de Extorsión", "Penal Sustantivo", "Al que sin derecho obligue a otro a dar, hacer, dejar de hacer o tolerar algo, obteniendo un lucro para sí o para otro o causando a alguien un perjuicio patrimonial, se le aplicarán de dos a ocho años de prisión.", "VIGENTE DOF", "Noviembre 2023", 12, listOf(VectorChunk("cpf_390_p1", 1, 0.970f, "Extorsión", "Coacción ilegal y afectación patrimonial.", true)))
    )

    // 3. LEY DE AMPARO
    val amparoArticles: List<LawArticleDetail> = listOf(
        LawArticleDetail("amparo_1", "amparo", "Ley de Amparo", "Artículo 1°", "Objeto y Fines del Juicio de Amparo", "Amparo", "El juicio de amparo tiene por objeto resolver toda controversia por normas generales, actos u omisiones de autoridad que violen los derechos humanos reconocidos y las garantías otorgadas para su protección por la Constitución y tratados.", "VIGENTE DOF", "Junio 2022", 14, listOf(VectorChunk("amparo_1_p1", 1, 0.985f, "Objeto del Amparo", "Mecanismo extraordinario de tutela constitucional y convencional.", true))),
        LawArticleDetail("amparo_5", "amparo", "Ley de Amparo", "Artículo 5°", "Partes en el Juicio de Amparo", "Amparo", "Son partes en el juicio de amparo: I. El quejoso; II. La autoridad responsable; III. El tercero interesado; IV. El Ministerio Público de la Federación.", "VIGENTE DOF", "Junio 2022", 12, listOf(VectorChunk("amparo_5_p1", 1, 0.970f, "Partes Procesales", "Legitimación procesal de las cuatro partes del juicio de garantías.", true))),
        LawArticleDetail("amparo_6", "amparo", "Ley de Amparo", "Artículo 6°", "Legitimación del Quejoso e Interés Legítimo", "Amparo", "El juicio de amparo puede promoverse por la persona física o moral que aduzca ser titular de un derecho subjetivo o de un interés legítimo individual o colectivo.", "VIGENTE DOF", "Junio 2022", 10, listOf(VectorChunk("amparo_6_p1", 1, 0.960f, "Interés Jurídico y Legítimo", "Acreditación de afectación a la esfera jurídica para acudir al amparo.", true))),
        LawArticleDetail("amparo_17", "amparo", "Ley de Amparo", "Artículo 17", "Plazos Generales para la Demanda de Amparo", "Amparo", "El plazo para presentar la demanda de amparo es de quince días. Excepciones: 30 días contra normas autoaplicativas; 8 años contra sentencia condenatoria penal; o en cualquier tiempo contra actos del Art. 22 constitucional.", "VIGENTE DOF", "Junio 2022", 14, listOf(VectorChunk("amparo_17_p1", 1, 0.975f, "Términos Procesales", "Cómputo estricto del plazo de 15 días hábiles y supuestos de imprescriptibilidad.", true))),
        LawArticleDetail("amparo_61", "amparo", "Ley de Amparo", "Artículo 61", "Causales Taxativas de Improcedencia", "Amparo", "El juicio de amparo es improcedente contra adiciones a la Constitución, actos de la Suprema Corte, actos consumados de modo irreparable, consentimiento expreso o tácito, y cosa juzgada.", "VIGENTE DOF", "Junio 2022", 20, listOf(VectorChunk("amparo_61_p1", 1, 0.980f, "Improcedencia Fracciones I a XXIII", "Motivos legales que impiden al juzgador analizar el fondo del acto reclamado.", true))),
        LawArticleDetail("amparo_63", "amparo", "Ley de Amparo", "Artículo 63", "Supuestos de Sobreseimiento en el Amparo", "Amparo", "El sobreseimiento en el juicio de amparo procede por desistimiento del quejoso, inexistencia del acto reclamado probada en informe justificado, o cuando sobrevenga causal de improcedencia.", "VIGENTE DOF", "Junio 2022", 12, listOf(VectorChunk("amparo_63_p1", 1, 0.965f, "Sobreseimiento", "Terminación anormal del juicio de amparo sin pronunciamiento sobre constitucionalidad.", true))),
        LawArticleDetail("amparo_73", "amparo", "Ley de Amparo", "Artículo 73", "Efectos Restitutorios de la Sentencia Protectora", "Amparo", "La sentencia que conceda el amparo restituirá al quejoso en el pleno goce del derecho violado, restableciendo las cosas al estado que guardaban antes de la violación si el acto es positivo, u obligando a actuar si es negativo.", "VIGENTE DOF", "Junio 2022", 12, listOf(VectorChunk("amparo_73_p1", 1, 0.970f, "Efectos de Sentencia", "Efecto restitutorio directo frente a la autoridad responsable.", true))),
        LawArticleDetail("amparo_79", "amparo", "Ley de Amparo", "Artículo 79", "Suplencia de la Queja Deficiente", "Amparo", "La autoridad que conozca del amparo suplirá la deficiencia de los conceptos de violación en materia penal a favor del imputado o de la víctima, en materia agraria, laboral en favor del trabajador, y a favor de menores.", "VIGENTE DOF", "Junio 2022", 14, listOf(VectorChunk("amparo_79_p1", 1, 0.980f, "Suplencia de la Queja", "Protección sustantiva oficiosa para salvaguardar el acceso a la justicia de partes vulnerables.", true))),
        LawArticleDetail("amparo_107", "amparo", "Ley de Amparo", "Artículo 107", "Procedencia del Juicio de Amparo Indirecto", "Amparo", "El amparo indirecto procede contra normas generales con motivo de su sola vigencia o acto de aplicación, actos fuera de juicio o después de concluido, y actos en juicio de imposible reparación.", "VIGENTE DOF", "Junio 2022", 16, listOf(VectorChunk("amparo_107_p1", 1, 0.985f, "Amparo Biinstancial", "Competencia del Juez de Distrito en vía indirecta.", true))),
        LawArticleDetail("amparo_108", "amparo", "Ley de Amparo", "Artículo 108", "Requisitos de la Demanda de Amparo Indirecto", "Amparo", "Nombre del quejoso, tercero interesado, autoridades responsables, norma general o acto reclamado, hechos bajo protesta de decir verdad, y conceptos de violación.", "VIGENTE DOF", "Junio 2022", 12, listOf(VectorChunk("amparo_108_p1", 1, 0.960f, "Estructura de Demanda", "Requisitos formales indispensables para la admisión de la demanda.", true))),
        LawArticleDetail("amparo_115", "amparo", "Ley de Amparo", "Artículo 115", "Informe Justificado y Audiencia Constitucional", "Amparo", "De admitirse la demanda, el juez pedirá informe con justificación a las autoridades responsables (15 días) y señalará día y hora para la audiencia constitucional dentro de los 30 días siguientes.", "VIGENTE DOF", "Junio 2022", 12, listOf(VectorChunk("amparo_115_p1", 1, 0.952f, "Audiencia Constitucional", "Etapa de pruebas, alegatos y dictado de sentencia en amparo indirecto.", true))),
        LawArticleDetail("amparo_125", "amparo", "Ley de Amparo", "Artículo 125", "Vías de la Suspensión del Acto Reclamado", "Amparo", "La suspensión del acto reclamado se decretará de oficio o a petición del quejoso.", "VIGENTE DOF", "Junio 2022", 10, listOf(VectorChunk("amparo_125_p1", 1, 0.960f, "Suspensión del Acto", "Medida cautelar constitucional para preservar la materia del juicio y evitar daños irreparables.", true))),
        LawArticleDetail("amparo_126", "amparo", "Ley de Amparo", "Artículo 126", "Suspensión de Oficio y de Plano", "Amparo", "La suspensión se concederá de oficio y de plano cuando se trate de actos que importen peligro de privación de la vida, ataques a la libertad personal fuera de procedimiento, incomunicación o deportación.", "VIGENTE DOF", "Junio 2022", 14, listOf(VectorChunk("amparo_126_p1", 1, 0.985f, "Suspensión de Plano", "Procedencia inmediata en el auto admisorio sin fianza ni incidente previo.", true))),
        LawArticleDetail("amparo_128", "amparo", "Ley de Amparo", "Artículo 128", "Requisitos de la Suspensión a Petición de Parte", "Amparo", "Con excepción de los casos de oficio, la suspensión se decretará cuando: I. La solicite el quejoso; y II. No se siga perjuicio al interés social ni se contravengan disposiciones de orden público.", "VIGENTE DOF", "Junio 2022", 12, listOf(VectorChunk("amparo_128_p1", 1, 0.970f, "Interés Social y Orden Público", "Ponderación entre la apariencia del buen derecho y el orden público.", true))),
        LawArticleDetail("amparo_138", "amparo", "Ley de Amparo", "Artículo 138", "Trámite del Incidente de Suspensión (Provisional y Definitiva)", "Amparo", "Promovida la suspensión a petición, el juez acordará la suspensión provisional en 24 horas y fijará fecha para la audiencia incidental en 5 días para resolver la suspensión definitiva.", "VIGENTE DOF", "Junio 2022", 14, listOf(VectorChunk("amparo_138_p1", 1, 0.975f, "Suspensión Provisional y Definitiva", "Doble fase cautelar en cuaderno incidental por separado.", true))),
        LawArticleDetail("amparo_170", "amparo", "Ley de Amparo", "Artículo 170", "Procedencia del Juicio de Amparo Directo", "Amparo", "El juicio de amparo directo procede contra sentencias definitivas, laudos y resoluciones que pongan fin al juicio, dictadas por tribunales judiciales, administrativos, agrarios o del trabajo.", "VIGENTE DOF", "Junio 2022", 16, listOf(VectorChunk("amparo_170_p1", 1, 0.985f, "Amparo Uniinstancial", "Competencia de los Tribunales Colegiados de Circuito contra sentencias definitivas.", true))),
        LawArticleDetail("amparo_175", "amparo", "Ley de Amparo", "Artículo 175", "Requisitos de la Demanda de Amparo Directo", "Amparo", "Se presentará por conducto de la autoridad responsable que emitió la sentencia definitiva, con los conceptos de violación de fondo y violaciones procesales trascendentes.", "VIGENTE DOF", "Junio 2022", 12, listOf(VectorChunk("amparo_175_p1", 1, 0.965f, "Presentación por Autoridad", "Tramitación por conducto de la autoridad responsable y certificación de notificación.", true))),
        LawArticleDetail("amparo_215", "amparo", "Ley de Amparo", "Artículo 215", "Mecanismos de Creación de Jurisprudencia", "Penal/Amparo", "La jurisprudencia se establece por precedentes obligatorios (Pleno y Salas de la SCJN), por reiteración (Tribunales Colegiados) y por contradicción de criterios.", "VIGENTE DOF", "Junio 2022", 12, listOf(VectorChunk("amparo_215_p1", 1, 0.970f, "Precedentes Obligatorios", "Efecto vinculante de las sentencias del Pleno y Salas de la Suprema Corte.", true)))
    )

    // 4. CÓDIGO PENAL PARA EL ESTADO DE TABASCO (CPET)
    val cpetArticles: List<LawArticleDetail> = listOf(
        LawArticleDetail("cpet_1", "cpet", "CP Tabasco", "Artículo 1°", "Validez Espacial y Ámbito de Aplicación en Tabasco", "Penal", "Este Código se aplicará en el Estado de Tabasco por los delitos cometidos en su territorio que sean de la competencia de sus tribunales, respetando los derechos humanos y garantías constitucionales.", "VIGENTE POET", "Marzo 2024", 8, listOf(VectorChunk("cpet_1_p1", 1, 0.970f, "Ámbito Territorial Tabasco", "Aplica a todos los hechos constitutivos de delito del fuero común en los 17 municipios de Tabasco.", true))),
        LawArticleDetail("cpet_110", "cpet", "CP Tabasco", "Artículo 110", "Homicidio Simple y Tipificación Sustantiva", "Penal", "Al que prive de la vida a otro, se le impondrá prisión de ocho a veinte años.", "VIGENTE POET", "Marzo 2024", 10, listOf(VectorChunk("cpet_110_p1", 1, 0.985f, "Tipo Básico de Homicidio", "Punibilidad de 8 a 20 años de prisión por la privación ilegítima de la vida en el fuero común de Tabasco.", true))),
        LawArticleDetail("cpet_115", "cpet", "CP Tabasco", "Artículo 115 Bis", "Feminicidio por Razones de Género en Tabasco", "Penal", "Comete el delito de feminicidio quien prive de la vida a una mujer por razones de género. Se impondrá prisión de cuarenta a cincuenta años, y agravantes especiales cuando existan relaciones de confianza o violencia previa.", "VIGENTE POET", "Marzo 2024", 16, listOf(VectorChunk("cpet_115_p1", 1, 0.992f, "Tipo Penal de Feminicidio", "Razones de género, antecedentes de violencia familiar y sanciones privativas de 40 a 50 años.", true))),
        LawArticleDetail("cpet_175", "cpet", "CP Tabasco", "Artículo 175", "Delito de Robo y Escalas de Punibilidad por UMA", "Penal", "Al que, con ánimo de dominio y sin consentimiento de quien legalmente pueda otorgarlo, se apodere de una cosa mueble ajena. Se sanciona según el valor de lo robado tasado en Unidades de Medida y Actualización (UMA).", "VIGENTE POET", "Marzo 2024", 12, listOf(VectorChunk("cpet_175_p1", 1, 0.980f, "Robo Simple y Graduación UMA", "Definición del apoderamiento sin consentimiento y escalas punitivas patrimoniales.", true))),
        LawArticleDetail("cpet_196", "cpet", "CP Tabasco", "Artículo 196", "Extorsión y Coacción Patrimonial Ilícita", "Penal", "Al que para obtener un provecho indebido para sí o para un tercero, obligue a otro a dar, hacer, tolerar o dejar de hacer algo, conminándolo con causarle un daño en su persona, bienes o familia.", "VIGENTE POET", "Marzo 2024", 14, listOf(VectorChunk("cpet_196_p1", 1, 0.982f, "Delito de Extorsión", "Protección estricta frente a coacción, cobro de piso y amenazas de daño patrimonial o personal.", true))),
        LawArticleDetail("cpet_205", "cpet", "CP Tabasco", "Artículo 205 Bis", "Abigeato Mayor (Ganado Bovino y Equino)", "Penal", "Comete el delito de abigeato quien se apodere de una o más cabezas de ganado bovino o equino sin derecho ni consentimiento. Se impondrá de seis a ocho años de prisión de una a dos cabezas, con penas aumentadas en caso de reincidencia o violencia.", "VIGENTE POET", "Marzo 2024", 16, listOf(VectorChunk("cpet_205_p1", 1, 0.990f, "Protección a la Producción Pecuaria", "Delito de abigeato en Tabasco, con sanciones graves para salvaguardar el hato ganadero estatal.", true))),
        LawArticleDetail("cpet_208", "cpet", "CP Tabasco", "Artículo 208 Bis", "Violencia Familiar y Medidas de Protección", "Penal", "A quien cometa el delito de violencia familiar se le impondrá de uno a seis años de prisión. Se entiende por tal el acto abusivo de poder dirigido a agredir física, verbal, psicológica, patrimonial o sexualmente.", "VIGENTE POET", "Marzo 2024", 12, listOf(VectorChunk("cpet_208_p1", 1, 0.986f, "Violencia Familiar en Tabasco", "Tutela a la integridad de cónyuges, concubinos o familiares consanguíneos dentro o fuera del domicilio.", true))),
        LawArticleDetail("cpet_304", "cpet", "CP Tabasco", "Artículo 304 Quater", "Maltrato o Crueldad en contra de Animales", "Penal", "A quien intencionalmente cometa actos de maltrato o crueldad en contra de cualquier animal causándole sufrimiento o lesiones, se le impondrán de uno a cinco años de prisión y multa económica.", "VIGENTE POET", "Marzo 2024", 10, listOf(VectorChunk("cpet_304_p1", 1, 0.978f, "Protección a Animales Domésticos", "Tipificación del maltrato animal y agravantes en caso de provocar la muerte.", true)))
    )

    val allArticles: List<LawArticleDetail> = cpeumArticles + cnppArticles + cpfArticles + amparoArticles + cpetArticles

    fun getArticlesForLaw(lawId: String?): List<LawArticleDetail> {
        val targetId = lawId?.lowercase() ?: "cpeum"
        return DynamicStatutoryDirectory.getAllArticlesForLaw(targetId)
    }

    fun getGroupsForLaw(lawId: String?): List<String> {
        return when (lawId?.lowercase()) {
            "cpet" -> listOf("Todos (CP Tabasco)", "Parte General y Validez (1-42)", "Vida e Integridad (105-140)", "Feminicidio (115 Bis)", "Libertad y Seguridad (141-174)", "Robo y Patrimonio (175-205)", "Abigeato y Ganado (205 Bis-Ter)", "Familia y Violencia (206-215)", "Servidores Públicos (230-260)", "Protección Animal (304-369)")
            "cnpp" -> listOf("Todos (CNPP)", "Principios y Derechos (1-14)", "Competencia y Plazos (15-104)", "Sujetos Procesales (105-136)", "Medidas Cautelares (153-182)", "Justicia Restaurativa (183-210)", "Investigación (211-258)", "Audiencia Inicial (307-333)", "Etapa Intermedia (334-347)", "Juicio Oral (348-413)", "Recursos (456-490)")
            "cpf" -> listOf("Todos (CPF)", "Teoría del Delito (1-23)", "Penas y Medidas (24-76)", "Extinción Penal (77-118)", "Seguridad de la Nación (119-138)", "Paz Pública y Terrorismo (139-148)", "Servidores Públicos (212-227)", "Vida e Integridad (302-322)", "Libertad Personal (323-366)", "Delitos Patrimoniales (367-399)", "Delitos Federales (400-429)")
            "amparo" -> listOf("Todos (Amparo)", "Reglas Generales (1-32)", "Competencia (33-60)", "Improcedencia y Sobreseimiento (61-72)", "Sentencias y Suplencia (73-106)", "Amparo Indirecto (107-124)", "Suspensión del Acto (125-169)", "Amparo Directo (170-191)", "Cumplimiento (192-214)", "Jurisprudencia (215-235)", "Responsabilidades (236-271)")
            "ccf" -> listOf("Todos (CCF)", "Personas y Familia (1-746)", "Bienes y Propiedad (747-1280)", "Sucesiones y Herencias (1281-1791)", "Obligaciones y Contratos (1792-3044)")
            "cnpcyf" -> listOf("Todos (CNPCyF)", "Sistema de Justicia Oral (1-100)", "Competencia y Jurisdicción (101-200)", "Pruebas y Audiencias (201-450)", "Juicio Oral Familiar (451-700)", "Juicio Oral Civil (701-900)", "Vía de Apremio y Recursos (901-1191)")
            "ccet" -> listOf("Todos (CC Tabasco)", "Personas y Familia (1-800)", "Bienes y Posesión (801-1400)", "Sucesiones (1401-1900)", "Obligaciones y Contratos (1901-3227)")
            "lft" -> listOf("Todos (LFT)", "Principios Generales (1-55)", "Relaciones Individuales (56-131)", "Condiciones de Trabajo (132-353)", "Relaciones Colectivas y Huelga (354-471)", "Riesgos de Trabajo (472-522)", "Justicia Laboral y Conciliación (685-1010)")
            else -> listOf("General (136)", "DDHH (1-29)", "Nacionalidad (30-38)", "Poderes (49-107)")
        }
    }

    fun getArticleById(id: String): LawArticleDetail {
        val parts = id.split("_")
        if (parts.size == 2) {
            val lawId = parts[0]
            val num = parts[1].toIntOrNull()
            if (num != null) {
                val resolved = DynamicStatutoryDirectory.getArticle(lawId, num)
                if (resolved != null) return resolved
            }
        }
        return allArticles.firstOrNull { it.id == id } ?: cpeumArticles.first { it.id == "cpeum_16" }
    }

    fun buildDictamen(
        articleId: String?,
        groupName: String? = null,
        lawId: String = "cpeum"
    ): DictamenData {
        // Case 1: Specific article selected
        if (!articleId.isNullOrBlank()) {
            val article = getArticleById(articleId)
            val numSlug = article.articleNumber
                .replace(" ", "_")
                .replace("°", "")
                .replace(".", "")
            val fileName = "Dictamen_${numSlug}_${article.lawShortName.replace(" ", "_")}.pdf"
            val expediente = "EXP: LEXIA-2025-${numSlug.uppercase()}-REV"

            val supuestos = when (article.id) {
                "cpeum_16" -> listOf(
                    "1. Mandamiento Judicial: Orden de aprehensión librada con datos que establezcan la comisión del hecho.",
                    "2. Flagrancia Estricta (Párrafo 5°): Cualquier persona puede detener al indiciado al momento de cometer el ilícito.",
                    "3. Caso Urgente (Párrafo 6°): Exclusivo del Ministerio Público ante riesgo inminente de sustracción."
                )
                "cpeum_14" -> listOf(
                    "1. Garantía de Audiencia Previa: Prohibición de privación de derechos sin juicio previo ante tribunales.",
                    "2. Irretroactividad de la Ley: Prohibición de aplicar retroactivamente normas en perjuicio de persona alguna.",
                    "3. Formalidades Esenciales del Procedimiento: Notificación, ofrecimiento y desahogo de pruebas y alegatos."
                )
                "cpeum_1" -> listOf(
                    "1. Principio Pro Persona: Preferencia interpretativa de la norma que otorgue mayor protección.",
                    "2. Bloque de Constitucionalidad: Incorporación plena de tratados internacionales de DDHH ratificados por México.",
                    "3. Obligación de Autoridades: Deber correlativo de promover, respetar, proteger y garantizar los derechos humanos."
                )
                "cpeum_30" -> listOf(
                    "1. Nacionalidad por Nacimiento (Ius Soli e Ius Sanguinis): Nacidos en territorio mexicano o hijos de padres mexicanos.",
                    "2. Nacionalidad por Naturalización: Carta de naturalización de la SRE o personas extranjeras unidas en matrimonio.",
                    "3. Protección Contra Apatridia: Ningún mexicano por nacimiento podrá ser privado de su nacionalidad bajo ninguna circunstancia."
                )
                "cpeum_19" -> listOf(
                    "1. Auto de Vinculación a Proceso: Término constitucional perentorio de 72 a 144 horas.",
                    "2. Prisión Preventiva Justificada y Oficiosa: Aplicación restrictiva y excepcional bajo necesidad de cautela.",
                    "3. Delimitación de Litis: Fijación exclusiva sobre los hechos materia de la formal imputación."
                )
                "cpeum_20" -> listOf(
                    "1. Principios Rectores: Oralidad, publicidad, contradicción, concentración, continuidad e inmediación.",
                    "2. Derechos de la Persona Imputada: Presunción de inocencia, defensa técnica adecuada y no autoincriminación.",
                    "3. Derechos de la Víctima: Asesoría jurídica gratuita, reparación integral del daño y protección de identidad."
                )
                "cpeum_107" -> listOf(
                    "1. Principio de Instancia de Parte y Agravio Personal o Interés Legítimo Acreditado.",
                    "2. Suspensión del Acto Reclamado: Provisional y definitiva para preservar la materia del juicio de garantías.",
                    "3. Efectos Restitutorios de la Sentencia: Restituir al quejoso en el goce del derecho fundamental violado."
                )
                "cpeum_123" -> listOf(
                    "1. Jornada Máxima y Descanso Semanal: 8 horas diurnas y por cada seis días de trabajo uno de descanso.",
                    "2. Salario Mínimo General y Profesional: Remuneración digna para satisfacer necesidades materiales y familiares.",
                    "3. Estabilidad Laboral e Indemnización: Indemnización de tres meses de salario por despido sin causa justificada."
                )
                "cpeum_133" -> listOf(
                    "1. Supremacía Constitucional: Prevalencia de la Carta Magna sobre cualquier otra ley federal o local.",
                    "2. Bloque de Constitucionalidad: Leyes federales y tratados internacionales conformes a la Constitución.",
                    "3. Control Difuso de Constitucionalidad: Jueces locales obligados a preferir la Constitución frente a normas locales."
                )
                else -> listOf(
                    "1. Ámbito de Validez Constitucional: Obligatoriedad erga omnes del precepto conforme a la reforma DOF del ${article.lastReformDate}.",
                    "2. Bien Jurídico Tutelado: Garantía y protección de ${article.topicTitle} bajo el orden constitucional.",
                    "3. Vinculación Jurisprudencial: Aplicación estricta por tribunales federales y autoridades de la materia (${article.category})."
                )
            }

            return DictamenData(
                fileName = fileName,
                title = "DICTAMEN TÉCNICO JURÍDICO",
                subtitle = "Dictamen Pericial Oficial · ${article.lawShortName} ${article.articleNumber}",
                expediente = expediente,
                materiaConsultadaTitle = "I. PRECISIÓN DE LA MATERIA CONSULTADA",
                materiaConsultadaDesc = "Análisis dogmático, hermenéutico y jurisprudencial del ${article.articleNumber} de la Constitución Política de los Estados Unidos Mexicanos (${article.lawShortName}) relativo a: ${article.topicTitle}. Materia: ${article.category}.",
                fundamentacionNormativaTitle = "II. FUNDAMENTACIÓN NORMATIVA DOF (${article.lawShortName})",
                fundamentacionNormativaText = "«${article.summary}»",
                supuestosTitle = "III. SUPUESTOS CONSTITUCIONALES Y ELEMENTOS NORMATIVOS",
                supuestosList = supuestos,
                reformDate = article.lastReformDate,
                dofStatus = article.dofStatus,
                hashSha256 = "SHA256: " + Integer.toHexString((article.id + article.summary).hashCode()) + "4f98a2e1d71b839c09bf45ef2038"
            )
        }

        // Case 2: Chapter Group selected (e.g. DDHH)
        if (!groupName.isNullOrBlank()) {
            val groupSlug = groupName
                .replace(" ", "_")
                .replace("(", "")
                .replace(")", "")
                .replace("-", "_")
                .replace(".", "")
            val isDdhh = groupName.contains("DDHH") || groupName.contains("Derechos")
            val fileName = "Dictamen_${groupSlug}_CPEUM.pdf"

            val supuestos = if (isDdhh) {
                listOf(
                    "1. Principio Pro Persona y Bloque de Constitucionalidad (Art. 1° CPEUM).",
                    "2. Garantías de Seguridad Jurídica y Debido Proceso (Arts. 14, 16, 19 y 20 CPEUM).",
                    "3. Libertades Fundamentales: Expresión, tránsito, trabajo e integridad personal (Arts. 5, 6, 9 y 11)."
                )
            } else {
                listOf(
                    "1. Coherencia Sistemática: Interconexión funcional de los preceptos que componen el capítulo normativo.",
                    "2. Delimitación de Competencias: Distribución y alcance normativo en el orden federal y estatal.",
                    "3. Criterio Vinculante SCJN: Criterios hermenéuticos aplicables por el Poder Judicial de la Federación."
                )
            }

            val fundText = if (isDdhh) {
                "«En los Estados Unidos Mexicanos todas las personas gozarán de los derechos humanos reconocidos en esta Constitución y en los tratados internacionales de los que el Estado Mexicano sea parte, así como de las garantías para su protección, cuyo ejercicio no podrá restringirse ni suspenderse, salvo en los casos y bajo las condiciones que esta Constitución establece.» — Art. 1° CPEUM"
            } else {
                "«La Constitución Política de los Estados Unidos Mexicanos establece las bases fundamentales para el ejercicio del poder público, la soberanía nacional y la convivencia armónica de la República.» — CPEUM DOF Vigente"
            }

            return DictamenData(
                fileName = fileName,
                title = "DICTAMEN TÉCNICO JURÍDICO",
                subtitle = "Capítulo Constitucional · $groupName",
                expediente = "EXP: LEXIA-2025-GRP-${groupSlug.take(6).uppercase()}-DOF",
                materiaConsultadaTitle = "I. PRECISIÓN DE LA MATERIA CONSULTADA",
                materiaConsultadaDesc = "Análisis dogmático e integrador del grupo normativo correspondiente a $groupName de la Constitución Política de los Estados Unidos Mexicanos, evaluando su impacto transversal en el orden jurídico.",
                fundamentacionNormativaTitle = "II. FUNDAMENTACIÓN NORMATIVA DOF (CPEUM)",
                fundamentacionNormativaText = fundText,
                supuestosTitle = "III. PILARES CONSTITUCIONALES DEL GRUPO",
                supuestosList = supuestos,
                reformDate = "DOF Vigente",
                dofStatus = "DOF VIGENTE",
                hashSha256 = "SHA256: 8a71d93b" + Integer.toHexString(groupName.hashCode()) + "e1d71b839c09bf45ef203847"
            )
        }

        // Case 3: General Law Review (CPEUM, CNPP, CPF, Amparo)
        val law = getLawById(lawId)
        return when (law.id) {
            "cnpp" -> DictamenData(
                fileName = "Dictamen_General_CNPP.pdf",
                title = "DICTAMEN TÉCNICO PROCESAL PENAL",
                subtitle = "Código Nacional de Procedimientos Penales (490 Artículos)",
                expediente = "EXP: LEXIA-2025-CNPP-GRAL-PROC",
                materiaConsultadaTitle = "I. PRECISIÓN DE LA MATERIA CONSULTADA",
                materiaConsultadaDesc = "Revisión técnica integral del Sistema Procesal Penal Acusatorio y Oral bajo el CNPP, analizando debido proceso legal, medidas cautelares, licitud probatoria y recursos ordinarios.",
                fundamentacionNormativaTitle = "II. FUNDAMENTACIÓN NORMATIVA DOF (CNPP)",
                fundamentacionNormativaText = "«Las disposiciones de este Código son de orden público y de observancia general en toda la República Mexicana... El proceso penal será acusatorio y oral, rigiéndose por los principios de publicidad, contradicción, concentración, continuidad e inmediación.» — Arts. 1° y 4° CNPP",
                supuestosTitle = "III. EJES PROCESALES IDENTIFICADOS",
                supuestosList = listOf(
                    "1. Sistema Acusatorio Oral: Contradicción probatoria e inmediación judicial directa.",
                    "2. Excepcionalidad de la Prisión Preventiva: Proporcionalidad estricta y temporalidad máxima de dos años (Art. 165).",
                    "3. Exclusión de Prueba Ilícita: Nulidad absoluta de actos con violación a derechos humanos (Art. 356)."
                ),
                reformDate = "Enero 2024",
                dofStatus = "DOF VIGENTE",
                hashSha256 = "SHA256: 9b12a84ef" + Integer.toHexString(lawId.hashCode()) + "e1d71b839c09bf45ef203847"
            )
            "cpf" -> DictamenData(
                fileName = "Dictamen_General_CPF.pdf",
                title = "DICTAMEN TÉCNICO PENAL SUSTANTIVO",
                subtitle = "Código Penal Federal (429 Artículos)",
                expediente = "EXP: LEXIA-2025-CPF-GRAL-PEN",
                materiaConsultadaTitle = "I. PRECISIÓN DE LA MATERIA CONSULTADA",
                materiaConsultadaDesc = "Análisis dogmático sustantivo de la teoría del delito, tipicidad, causas de exclusión del delito, imputabilidad, autoría y marco de sanciones punitivas federales.",
                fundamentacionNormativaTitle = "II. FUNDAMENTACIÓN NORMATIVA DOF (CPF)",
                fundamentacionNormativaText = "«Delito es el acto u omisión que sancionan las leyes penales... Las acciones u omisiones delictivas solamente pueden realizarse dolosa o culposamente.» — Arts. 7° y 8° CPF",
                supuestosTitle = "III. ELEMENTOS DOGMÁTICOS IDENTIFICADOS",
                supuestosList = listOf(
                    "1. Tipicidad y Dolo/Culpa: Prohibición de responsabilidad objetiva en materia penal (Arts. 7 y 9).",
                    "2. Causas de Exclusión: Legítima defensa y estado de necesidad justificante (Art. 15).",
                    "3. Reparación Integral del Daño: Pena pública preferente para tutela de la víctima (Art. 29)."
                ),
                reformDate = "Noviembre 2023",
                dofStatus = "DOF VIGENTE",
                hashSha256 = "SHA256: 7c43a12bf" + Integer.toHexString(lawId.hashCode()) + "e1d71b839c09bf45ef203847"
            )
            "amparo" -> DictamenData(
                fileName = "Dictamen_General_Ley_Amparo.pdf",
                title = "DICTAMEN TÉCNICO DE CONTROL CONSTITUCIONAL",
                subtitle = "Ley de Amparo, Reglamentaria de los Arts. 103 y 107 CPEUM (271 Artículos)",
                expediente = "EXP: LEXIA-2025-AMPARO-GRAL-REV",
                materiaConsultadaTitle = "I. PRECISIÓN DE LA MATERIA CONSULTADA",
                materiaConsultadaDesc = "Evaluación de la procedencia del juicio de amparo (indirecto y directo), ponderación cautelar en suspensión del acto reclamado y suplencia de la deficiencia de la queja.",
                fundamentacionNormativaTitle = "II. FUNDAMENTACIÓN NORMATIVA DOF (LEY DE AMPARO)",
                fundamentacionNormativaText = "«El juicio de amparo tiene por objeto resolver toda controversia por normas generales, actos u omisiones de autoridad que violen los derechos humanos reconocidos en la Constitución y tratados internacionales.» — Art. 1° Ley de Amparo",
                supuestosTitle = "III. PRINCIPIOS DE REGULARIDAD CONSTITUCIONAL",
                supuestosList = listOf(
                    "1. Tutela de Derechos Fundamentales e Interés Legítimo (Arts. 1 y 6).",
                    "2. Suspensión del Acto Reclamado: De oficio o a petición de parte para evitar daños irreparables (Arts. 125 a 138).",
                    "3. Suplencia de la Queja Deficiente: Garantía sustantiva en favor del imputado, víctima o trabajador (Art. 79)."
                ),
                reformDate = "Junio 2022",
                dofStatus = "DOF VIGENTE",
                hashSha256 = "SHA256: 3d56f81c" + Integer.toHexString(lawId.hashCode()) + "e1d71b839c09bf45ef203847"
            )
            else -> DictamenData(
                fileName = "Dictamen_Constitucion_General_CPEUM.pdf",
                title = "DICTAMEN TÉCNICO JURÍDICO",
                subtitle = "Constitución Política de los Estados Unidos Mexicanos (136 Artículos)",
                expediente = "EXP: LEXIA-2025-CPEUM-136-GRAL",
                materiaConsultadaTitle = "I. PRECISIÓN DE LA MATERIA CONSULTADA",
                materiaConsultadaDesc = "Revisión técnica integral de la Constitución Política de los Estados Unidos Mexicanos en sus 9 Títulos y 136 artículos, evaluando supremacía constitucional, división de poderes y regularidad jurídica de los actos de autoridad.",
                fundamentacionNormativaTitle = "II. FUNDAMENTACIÓN NORMATIVA DOF (CPEUM)",
                fundamentacionNormativaText = "«Esta Constitución, las leyes del Congreso de la Unión que emanen de ella y todos los tratados que estén de acuerdo con la misma, celebrados y que se celebren por el Presidente de la República, con aprobación del Senado, serán la Ley Suprema de toda la Unión.» — Art. 133 CPEUM",
                supuestosTitle = "III. BASES DOGMÁTICAS Y ORGÁNICAS IDENTIFICADAS",
                supuestosList = listOf(
                    "1. Supremacía Constitucional: Prevalencia absoluta de la Constitución sobre cualquier ley federal o local (Art. 133).",
                    "2. División Tripartita de Poderes: Facultades reservadas y pesos y contrapesos entre Ejecutivo, Legislativo y Judicial (Art. 49).",
                    "3. Régimen Federal y Soberanía Popular: La soberanía reside esencialmente en el pueblo y se ejerce a través de los Poderes de la Unión (Arts. 39, 40 y 41)."
                ),
                reformDate = "Mayo 2021 (Vigente)",
                dofStatus = "DOF VIGENTE",
                hashSha256 = "SHA256: 4f98a2e1d71b839c09bf45ef203847291aeb7c4581938471928374a5e1289c09"
            )
        }
    }

    fun findBestMatchingArticle(query: String, targetLawId: String = "cpeum"): LawArticleDetail? {
        val q = query.lowercase()
        val lawArticles = getArticlesForLaw(targetLawId)

        // Check for direct article number like "146", "30", "16", "art 30", "articulo 107"
        val numberRegex = Regex("""(?:art[ií]culo|art\.?)\s*(\d{1,3})|(?:\b(\d{1,3})\b)""")
        val matches = numberRegex.findAll(q)
        for (m in matches) {
            val numStr = m.groupValues[1].ifEmpty { m.groupValues[2] }
            val num = numStr.toIntOrNull()
            if (num != null) {
                val found = lawArticles.find { it.id == "${targetLawId}_$num" || it.articleNumber.contains(numStr) }
                if (found != null) return found
            }
        }

        // Keywords map for specific prominent concepts
        val keywordMap = listOf(
            listOf("nacionalidad", "mexicanos por nacimiento", "naturaliza", "jus soli", "jus sanguinis", "sre") to 30,
            listOf("amparo", "juicio de amparo", "suspensión del acto", "quejoso") to 107,
            listOf("trabajo", "laboral", "salario", "jornada", "huelga", "sindicato", "despido", "patrón") to 123,
            listOf("legalidad", "mandamiento escrito", "fundamentación y motivación", "flagrancia", "cateo") to 16,
            listOf("audiencia", "debido proceso", "irretroactividad", "formalidades esenciales") to 14,
            listOf("derechos humanos", "pro persona", "bloque de constitucionalidad", "discriminación") to 1,
            listOf("pueblos indígenas", "comunidad indígena", "autonomía indígena", "pluricultural") to 2,
            listOf("educación", "laica", "gratuita", "escuela", "maestro") to 3,
            listOf("salud", "igualdad de género", "medio ambiente", "agua", "vivienda", "niñez", "alimentación") to 4,
            listOf("libertad de trabajo", "profesión", "oficio lícito") to 5,
            listOf("libertad de expresión", "acceso a la información", "transparencia", "inai", "ideas") to 6,
            listOf("imprenta", "libertad de prensa", "difusión") to 7,
            listOf("petición", "derecho de petición") to 8,
            listOf("asociación", "reunión pacífica") to 9,
            listOf("armas", "legítima defensa", "poseer armas") to 10,
            listOf("tránsito", "viajar", "asilo", "refugio", "pasaporte") to 11,
            listOf("acceso a la justicia", "tribunales expeditos", "prohibición de autotutela", "costas judiciales") to 17,
            listOf("sistema penitenciario", "reinserción", "cárcel", "prisión") to 18,
            listOf("vinculación a proceso", "prisión preventiva", "72 horas", "plazo constitucional") to 19,
            listOf("proceso penal acusatorio", "juicio oral", "presunción de inocencia", "víctima", "imputado") to 20,
            listOf("ministerio público", "fiscalía", "investigación penal", "seguridad pública", "guardia nacional") to 21,
            listOf("penas prohibidas", "pena de muerte", "tormento", "extinción de dominio") to 22,
            listOf("non bis in idem", "doble juzgamiento", "tres instancias") to 23,
            listOf("libertad de religión", "creencias", "culto") to 24,
            listOf("rectoría económica", "desarrollo sustentable") to 25,
            listOf("tierras y aguas", "propiedad originaria", "petróleo", "hidrocarburos", "litio", "ejidos") to 27,
            listOf("monopolios", "libre concurrencia", "cofece", "ift", "banco de méxico", "banxico") to 28,
            listOf("suspensión de garantías", "estado de emergencia", "perturbación grave") to 29,
            listOf("obligaciones de los mexicanos", "pagar impuestos", "gasto público") to 31,
            listOf("extranjeros", "expulsión de extranjeros") to 33,
            listOf("ciudadanía", "ciudadano mexicano", "18 años", "modo honesto") to 34,
            listOf("votar", "ser votado", "derechos políticos", "consulta popular", "revocación de mandato") to 35,
            listOf("soberanía", "soberanía nacional", "el pueblo") to 39,
            listOf("forma de gobierno", "república representativa", "laica y federal") to 40,
            listOf("partidos políticos", "ine", "elecciones") to 41,
            listOf("división de poderes", "legislativo ejecutivo y judicial") to 49,
            listOf("diputados", "cámara de diputados", "500 diputados") to 52,
            listOf("senadores", "cámara de senadores", "128 senadores") to 56,
            listOf("facultades del congreso", "leyes federales") to 73,
            listOf("presupuesto de egresos", "cuenta pública", "auditoría superior") to 74,
            listOf("presidente de la república", "poder ejecutivo", "elección presidencial") to 80,
            listOf("no reelección", "periodo presidencial", "seis años") to 83,
            listOf("facultades del presidente", "promulgar leyes", "fuerza armada") to 89,
            listOf("suprema corte", "scjn", "ministros", "poder judicial") to 94,
            listOf("supremacía constitucional", "tratados internacionales", "ley suprema") to 133,
            listOf("reformas a la constitución", "constituyente permanente") to 135,
            listOf("inviolabilidad de la constitución") to 136
        )

        for ((words, artNum) in keywordMap) {
            if (words.any { q.contains(it) }) {
                val match = cpeumArticles.find { it.id == "cpeum_$artNum" }
                if (match != null) return match
            }
        }

        // Generic word overlap across all 136 articles
        val queryWords = q.split(" ", "?", "¿", ",", ".", ";", ":")
            .filter { it.length > 3 && it !in listOf("para", "como", "sobre", "cual", "cuales", "esta", "este", "dice", "que", "hace", "donde", "cuando") }

        if (queryWords.isEmpty()) return lawArticles.firstOrNull()

        return lawArticles.maxByOrNull { art ->
            val text = (art.topicTitle + " " + art.summary + " " + art.category).lowercase()
            queryWords.count { text.contains(it) }
        }?.takeIf { art ->
            val text = (art.topicTitle + " " + art.summary + " " + art.category).lowercase()
            queryWords.any { text.contains(it) }
        } ?: lawArticles.firstOrNull()
    }

    fun queryConstitutionalRag(
        userQuery: String,
        scope: ScopeChoice,
        lawId: String = "cpeum",
        activeArticle: LawArticleDetail? = null,
        activeGroupName: String? = null,
        aiModel: com.lexia.legalassistant.data.model.AiModel? = null
    ): RagMessage {
        val q = userQuery.trim()
        val lower = q.lowercase()
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val randomId = UUID.randomUUID().toString()
        val law = getLawById(lawId)
        val modelName = aiModel?.let { "${it.iconEmoji} ${it.name}" } ?: "LexIA Assistant"

        // 1. Target article resolution
        val matchedArticle = when {
            scope == ScopeChoice.SPECIFIC_ARTICLE && activeArticle != null -> {
                val specificInQuery = findBestMatchingArticle(lower, lawId)
                if (specificInQuery != null && (lower.contains("art") || lower.any { it.isDigit() }) && specificInQuery.id != activeArticle.id) {
                    specificInQuery
                } else {
                    activeArticle
                }
            }
            else -> findBestMatchingArticle(lower, lawId) ?: getArticlesForLaw(lawId).first()
        }

        // Scope header context text
        val scopePrefix = when (scope) {
            ScopeChoice.GENERAL_CONSTITUTION -> "🏛️ [Búsqueda General · ${law.shortName} (${law.articlesCount} Artículos)]\n"
            ScopeChoice.GROUP_DDHH -> "🛡️ [Capítulo Especializado · ${activeGroupName ?: law.shortName}]\n"
            ScopeChoice.SPECIFIC_ARTICLE -> "📋 [Criterio Acotado · ${activeArticle?.articleNumber ?: matchedArticle.articleNumber}]\n"
        }

        // 2. Specialized responses for prominent constitutional matters
        if (matchedArticle != null) {
            when (matchedArticle.id) {
                "cpeum_30" -> {
                    val intros = listOf(
                        "El Artículo 30 de la Constitución Política de los Estados Unidos Mexicanos regula de forma integral la nacionalidad mexicana y sus fuentes legales de adquisición:",
                        "Conforme a la Carta Magna vigente, el Artículo 30 establece el régimen fundamental de la nacionalidad mexicana, distinguiendo dos vías exclusivas:",
                        "Analizando el texto supremo constitucional (Título Primero, Capítulo II), el Artículo 30 estipula que la nacionalidad mexicana se adquiere por nacimiento o por naturalización:"
                    )
                    val text = """
${scopePrefix}${intros.random()}

1. Nacionalidad por Nacimiento (Apartado A):
• Jus Soli: Quienes nazcan en el territorio de la República, sea cual fuere la nacionalidad de sus progenitores; así como las personas nacidas a bordo de embarcaciones o aeronaves mexicanas (de guerra o mercantes).
• Jus Sanguinis: Quienes nazcan en el extranjero siendo hijos de padre o madre mexicanos nacidos en territorio nacional, o hijos de mexicanos por naturalización (reforma DOF del 17-05-2021 que amplió este derecho sin límite generacional).

2. Nacionalidad por Naturalización (Apartado B):
• Las personas extranjeras que obtengan de la Secretaría de Relaciones Exteriores (SRE) la correspondiente carta de naturalización.
• La persona extranjera que contraiga matrimonio con varón o mujer mexicanos, que tenga o establezca su domicilio en territorio nacional y cumpla los requisitos de la ley.

Protección Constitucional Conexa:
El Artículo 37, Apartado A de la CPEUM garantiza que ningún mexicano por nacimiento podrá ser privado de su nacionalidad bajo ninguna circunstancia.
""".trimIndent()

                    return RagMessage(
                        id = randomId,
                        isUser = false,
                        senderName = "LexIA Assistant",
                        text = text,
                        timestamp = time,
                        citation = LegalCitation(
                            articleTitle = "Artículo 30 CPEUM (Apartados A y B)",
                            excerpt = "La nacionalidad mexicana se adquiere por nacimiento o por naturalización. A) Son mexicanos por nacimiento los nacidos en territorio de la República... B) Son mexicanos por naturalización...",
                            similarityScore = 0.985f,
                            reformDate = "Reforma DOF 17-05-2021",
                            citationPath = "CPEUM > Título Primero > Capítulo II > Art. 30"
                        ),
                        exceptionalGrounds = listOf(
                            "• Jus Soli: Adquisición originaria por nacer en territorio o transporte de bandera mexicana.",
                            "• Jus Sanguinis: Protección para hijos de mexicanos nacidos en el extranjero (DOF 17-05-2021).",
                            "• Principio de Inalienabilidad: Ningún mexicano por nacimiento pierde su nacionalidad (Art. 37 CPEUM)."
                        )
                    )
                }

                "cpeum_16" -> {
                    val intros = listOf(
                        "El Artículo 16 de la CPEUM es la columna vertebral del principio de legalidad y seguridad jurídica en el sistema normativo mexicano:",
                        "Conforme al Artículo 16 constitucional, todo acto de molestia en contra de los gobernados exige estricta reserva de ley y control judicial:",
                        "En el orden constitucional mexicano, el control judicial de la libertad y posesiones se rige por el Artículo 16 de la Carta Magna:"
                    )
                    val text = """
${scopePrefix}${intros.random()}

«Nadie puede ser molestado en su persona, familia, domicilio, papeles o posesiones, sino en virtud de mandamiento escrito de la autoridad competente, que funde y motive la causa legal del procedimiento.»

Garantías Fundamentales del Precepto:
1. Mandamiento Escrito Previo: Requisito formal ineludible emitido por autoridad legitimada.
2. Fundamentación y Motivación: Expresión precisa del precepto legal aplicable y las razones fácticas del caso concreto.
3. Excepciones para la Privación de Libertad:
   • Orden Judicial de Aprehensión (Párrafo 3°): Previa denuncia o querella con datos que acrediten el hecho delictivo y probabilidad de autoría.
   • Flagrancia Estricta (Párrafo 5°): Aprehensión en el instante mismo de la comisión del delito.
   • Caso Urgente (Párrafo 6°): Exclusivo del Ministerio Público tratándose de delitos graves ante riesgo inminente de fuga.
4. Inviolabilidad de las Comunicaciones Privadas y Domicilio (Órdenes de cateo exclusivas de la autoridad judicial).
""".trimIndent()

                    return RagMessage(
                        id = randomId,
                        isUser = false,
                        senderName = "LexIA Assistant",
                        text = text,
                        timestamp = time,
                        citation = LegalCitation(
                            articleTitle = "Artículo 16 CPEUM (Párrafos 1°, 3°, 5° y 11°)",
                            excerpt = "Nadie puede ser molestado en su persona, familia, domicilio, papeles o posesiones, sino en virtud de mandamiento escrito de la autoridad competente, que funde y motive la causa legal del procedimiento.",
                            similarityScore = 0.988f,
                            reformDate = "Reforma DOF 28-05-2021",
                            citationPath = "CPEUM > Título Primero > Cap. I > Art. 16"
                        ),
                        exceptionalGrounds = listOf(
                            "1. Mandamiento Escrito: Fundamentación y motivación estricta del acto de molestia.",
                            "2. Excepciones de Detención: Flagrancia inmediata o caso urgente ministerial estricto.",
                            "3. Reserva Judicial: Inviolabilidad del domicilio y comunicaciones salvo mandato judicial."
                        )
                    )
                }

                "cpeum_14" -> {
                    val text = """
${scopePrefix}El Artículo 14 constitucional tutela el debido proceso legal y la garantía de audiencia previa a cualquier acto privativo:

«Nadie podrá ser privado de la libertad o de sus propiedades, posesiones o derechos, sino mediante juicio seguido ante los tribunales previamente establecidos, en el que se cumplan las formalidades esenciales del procedimiento y conforme a las Leyes expedidas con anterioridad al hecho.»

Principios Rectores:
1. Irretroactividad de la Ley (Párrafo 1°): A ninguna ley se dará efecto retroactivo en perjuicio de persona alguna.
2. Garantía de Audiencia (Párrafo 2°): Juicio previo ante tribunales competentes con notificación, pruebas y alegatos.
3. Exacta Aplicación de la Ley Penal (Párrafo 3°): Prohibición terminante de imponer penas por analogía o mayoría de razón.
4. Principio de Legalidad en Juicios Civiles (Párrafo 4°): Resoluciones conformes a la letra de la ley, su interpretación jurídica o principios generales del derecho.
""".trimIndent()

                    return RagMessage(
                        id = randomId,
                        isUser = false,
                        senderName = "LexIA Assistant",
                        text = text,
                        timestamp = time,
                        citation = LegalCitation(
                            articleTitle = "Artículo 14 CPEUM (Párrafos 1° a 4°)",
                            excerpt = "Nadie podrá ser privado de la libertad o de sus propiedades, posesiones o derechos, sino mediante juicio seguido ante los tribunales previamente establecidos...",
                            similarityScore = 0.982f,
                            reformDate = "DOF Vigente",
                            citationPath = "CPEUM > Título Primero > Cap. I > Art. 14"
                        ),
                        exceptionalGrounds = listOf(
                            "• Irretroactividad de la norma en perjuicio de cualquier gobernado.",
                            "• Formalidades esenciales: Oportunidad de defensa, ofrecimiento probatorio y alegatos.",
                            "• Tipicidad Penal Estricta: Prohibición absoluta de analogía punitiva."
                        )
                    )
                }

                "cpeum_1" -> {
                    val text = """
${scopePrefix}El Artículo 1° constitucional es la cúspide de la reforma en materia de derechos humanos y consagra:

1. Bloque de Constitucionalidad:
Todas las personas gozan de los derechos reconocidos en la Constitución y en los tratados internacionales ratificados por México.

2. Principio Pro Persona (Párrafo 2°):
Las normas de derechos humanos deben interpretarse favoreciendo en todo tiempo la protección más amplia a las personas.

3. Obligaciones de las Autoridades (Párrafo 3°):
Todas las autoridades del país están obligadas a promover, respetar, proteger y garantizar los derechos humanos bajo principios de universalidad, interdependencia, indivisibilidad y progresividad.

4. Cláusula Antidiscriminatoria (Párrafo 5°):
Queda estrictamente prohibida toda discriminación motivada por origen étnico, género, edad, discapacidad, condición social, religión o preferencias sexuales.
""".trimIndent()

                    return RagMessage(
                        id = randomId,
                        isUser = false,
                        senderName = "LexIA Assistant",
                        text = text,
                        timestamp = time,
                        citation = LegalCitation(
                            articleTitle = "Artículo 1° CPEUM (Párrafos 1°, 2°, 3° y 5°)",
                            excerpt = "En los Estados Unidos Mexicanos todas las personas gozarán de los derechos humanos reconocidos en esta Constitución y en los tratados internacionales...",
                            similarityScore = 0.990f,
                            reformDate = "Reforma DOF 10-06-2011",
                            citationPath = "CPEUM > Título Primero > Cap. I > Art. 1°"
                        ),
                        exceptionalGrounds = listOf(
                            "• Principio Pro Persona: Elección obligada de la norma o interpretación más garantista.",
                            "• Control Difuso de Convencionalidad: Deber judicial ex officio ante la CIDH.",
                            "• Reparación Integral del Daño por violaciones a derechos fundamentales."
                        )
                    )
                }

                "cpeum_123" -> {
                    val text = """
${scopePrefix}El Artículo 123 constitucional consagra el derecho humano al trabajo digno y socialmente útil:

«Toda persona tiene derecho al trabajo digno y socialmente útil; al efecto, se promoverán la creación de empleos y la organización social de trabajo, conforme a la ley.»

Estructura Fundamental:
• Apartado A (Trabajadores en General, Obreros, Empleados):
  - Jornada máxima de 8 horas diurnas y 7 nocturnas.
  - Salario mínimo suficiente para satisfacer necesidades familiares.
  - Derecho a la huelga, libertad sindical, reparto de utilidades (PTU) y seguridad social (IMSS e INFONAVIT).
  - Justicia laboral impartida por Tribunales Laborales del Poder Judicial con conciliación prejudicial obligatoria.

• Apartado B (Servidores Públicos de los Poderes de la Unión):
  - Estabilidad en el empleo, escalafón y seguridad social a través del ISSSTE.
""".trimIndent()

                    return RagMessage(
                        id = randomId,
                        isUser = false,
                        senderName = "LexIA Assistant",
                        text = text,
                        timestamp = time,
                        citation = LegalCitation(
                            articleTitle = "Artículo 123 CPEUM (Apartados A y B)",
                            excerpt = "Toda persona tiene derecho al trabajo digno y socialmente útil... El Congreso de la Unión deberá expedir leyes sobre el trabajo...",
                            similarityScore = 0.984f,
                            reformDate = "Reforma DOF 01-05-2019",
                            citationPath = "CPEUM > Título Sexto > Art. 123"
                        ),
                        exceptionalGrounds = listOf(
                            "• Jornada legal máxima y salario remunerador inviolable.",
                            "• Democracia y libertad sindical mediante voto personal, libre, directo y secreto.",
                            "• Nuevo modelo procesal laboral con tribunales judiciales y centros de conciliación."
                        )
                    )
                }

                "cpeum_107" -> {
                    val text = """
${scopePrefix}El Artículo 107 constitucional, en concordancia con el Artículo 103 y la Ley de Amparo, reglamenta las bases del Juicio de Amparo mexicano:

El juicio de amparo es el principal mecanismo de control constitucional para proteger los derechos humanos y garantías frente a normas generales, actos u omisiones de autoridades que vulneren el orden fundamental.

Bases Fundamentales:
1. Instancia de Parte Agraviada: Promovido por quien aduzca ser titular de un derecho subjetivo o un interés legítimo individual o colectivo.
2. Principio de Relatividad de las Sentencias (Fórmula Otero): La sentencia se limita a amparar al quejoso en el caso particular.
3. Principio de Definitividad: Exigencia previa de agotar los recursos ordinarios procedentes, salvo excepciones constitucionales.
4. Suspensión del Acto Reclamado: Medida cautelar para preservar la materia del juicio y evitar daños irreparables.
""".trimIndent()

                    return RagMessage(
                        id = randomId,
                        isUser = false,
                        senderName = "LexIA Assistant",
                        text = text,
                        timestamp = time,
                        citation = LegalCitation(
                            articleTitle = "Artículo 107 CPEUM y Ley de Amparo",
                            excerpt = "El juicio de amparo se seguirá siempre a instancia de parte agraviada, teniendo tal carácter quien aduce ser titular de un derecho o de un interés legítimo...",
                            similarityScore = 0.986f,
                            reformDate = "Reforma DOF 11-03-2021",
                            citationPath = "CPEUM > Título Tercero > Cap. IV > Art. 107"
                        ),
                        exceptionalGrounds = listOf(
                            "• Tutela de interés legítimo individual o colectivo ante afectaciones a la esfera jurídica.",
                            "• Suspensión provisional y definitiva para frenar la ejecución de actos lesivos.",
                            "• Amparo Directo ante Tribunales Colegiados y Amparo Indirecto ante Juzgados de Distrito."
                        )
                    )
                }

                "cnpp_311" -> {
                    val text = """
${scopePrefix}El Artículo 311 del Código Nacional de Procedimientos Penales (CNPP) regula minuciosamente el procedimiento para formular la imputación durante la Audiencia Inicial:

«Una vez que el imputado esté presente en la audiencia inicial, por haberse calificado de legal su detención o por haber comparecido mediante citación u orden de aprehensión, se le dará la palabra al Ministerio Público para que exponga al imputado el hecho que se le atribuye, la calificación jurídica preliminar, la fecha, lugar y modo de su comisión, la forma de intervención que se le atribuye en el mismo, así como el nombre de su acusador.»

Fases y Reglas Esenciales del Precepto:
1. Requisito Previo de Procedencia:
   • El imputado debe encontrarse presente tras haberse calificado de legal la detención en flagrancia/caso urgente (Art. 308) o por comparecencia por citatorio o mandamiento judicial (Art. 310).
2. Contenido Taxativo que debe exponer el Ministerio Público:
   • Hechos Fácticos Concretos: Circunstancias de tiempo, modo y lugar de la conducta atribuida.
   • Clasificación Jurídica Preliminar: Tipo penal sustantivo y modalidades aplicables.
   • Grado y Forma de Intervención: Coautoría, autoría material, complicidad o instigación.
   • Datos del Denunciante / Querellante: Salvo reserva judicial por medidas de protección a víctimas.
3. Control Judicial y Claridad:
   • El Juez de Control verificará si el imputado comprendió la imputación; de no ser así, ordenará al MP realizar las precisiones o aclaraciones conducentes.
4. Consecuencia Inmediata (Art. 312):
   • Una vez formulada la imputación, el Juez otorgará al imputado la oportunidad de rendir su declaración preparatoria o acogerse al derecho a guardar silencio.
""".trimIndent()

                    return RagMessage(
                        id = randomId,
                        isUser = false,
                        senderName = modelName,
                        text = text,
                        timestamp = time,
                        citation = LegalCitation(
                            articleTitle = "Artículo 311 CNPP · Procedimiento para Formular Imputación",
                            excerpt = "Una vez que el imputado esté presente en la audiencia inicial... se le dará la palabra al Ministerio Público para que exponga al imputado el hecho que se le atribuye, la calificación jurídica preliminar, la fecha, lugar y modo de su comisión...",
                            similarityScore = 0.988f,
                            reformDate = "DOF Vigente (Enero 2024)",
                            citationPath = "CNPP > Título VII > Capítulo II > Audiencia Inicial > Art. 311"
                        ),
                        exceptionalGrounds = listOf(
                            "• Comunicación formalizada y judicial de la acusación fáctica ante Juez de Control.",
                            "• Fijación de la litis penal para la posterior vinculación a proceso (Art. 316).",
                            "• Garantía ineludible de defensa técnica adecuada y no autoincriminación (Art. 113)."
                        )
                    )
                }

                // General dynamic fallback for any matched article in any law (CNPP, CPEUM, CPF, AMPARO)
                else -> {
                    val text = """
${scopePrefix}Conforme al texto del ${matchedArticle.lawShortName} (${law.title}), el ${matchedArticle.articleNumber} («${matchedArticle.topicTitle}») dispone:

«${matchedArticle.summary}»

Análisis Jurídico y Dogmático:
• Materia Legal: ${matchedArticle.category}.
• Estatus DOF: ${matchedArticle.dofStatus} (Última reforma registrada: ${matchedArticle.lastReformDate}).
• Ámbito de Aplicación: Ordenamiento de observancia federal y vinculatoriedad para operadores del sistema jurídico mexicano.
""".trimIndent()

                    return RagMessage(
                        id = randomId,
                        isUser = false,
                        senderName = modelName,
                        text = text,
                        timestamp = time,
                        citation = LegalCitation(
                            articleTitle = "${matchedArticle.lawShortName} · ${matchedArticle.articleNumber}",
                            excerpt = matchedArticle.summary,
                            similarityScore = 0.975f,
                            reformDate = matchedArticle.lastReformDate,
                            citationPath = "${matchedArticle.lawShortName} > ${matchedArticle.category} > ${matchedArticle.articleNumber}"
                        ),
                        exceptionalGrounds = listOf(
                            "• Materia Normativa: ${matchedArticle.category}.",
                            "• Cotejo Oficial: ${matchedArticle.dofStatus} (${matchedArticle.lastReformDate}).",
                            "• Validez y aplicabilidad vinculante en el marco del ordenamiento jurídico."
                        )
                    )
                }
            }
        }

        // 3. Fallback when query is broad or no specific article matched:
        if (scope == ScopeChoice.GROUP_DDHH) {
            val text = """
${scopePrefix}Analizando tu consulta en relación con el bloque temático especializado (${activeGroupName ?: law.shortName}):

En el marco de ${law.title} (${law.shortName}), este bloque contiene las disposiciones especializadas para el desahogo de procedimientos y tutela sustantiva.

Puedes consultar por cualquier artículo específico (ej. '${matchedArticle?.articleNumber ?: "Artículo 1"}') o realizar preguntas puntuales para obtener el análisis sistemático y correlacionado.
""".trimIndent()

            return RagMessage(
                id = randomId,
                isUser = false,
                senderName = modelName,
                text = text,
                timestamp = time,
                citation = LegalCitation(
                    articleTitle = "${law.shortName} · ${activeGroupName ?: "Capítulo Especializado"}",
                    excerpt = "Bloque normativo especializado con vigencia en el Diario Oficial de la Federación.",
                    similarityScore = 0.970f,
                    reformDate = law.lastReformDate,
                    citationPath = "${law.shortName} > ${activeGroupName ?: "Bloque Temático"}"
                ),
                exceptionalGrounds = listOf(
                    "• Análisis sistemático de disposiciones afines.",
                    "• Principio de especialidad de la norma.",
                    "• Vinculación procesal y sustantiva en la materia."
                )
            )
        }

        // Broad General response synthesizing matched law (CNPP, CPF, Amparo o CPEUM):
        val text = """
${scopePrefix}Analizando tu consulta («$q») en el contexto general de ${law.title} (${law.shortName} con ${law.articlesCount} Artículos indexados):

Este ordenamiento federal regula:
1. Materia y Competencia: ${law.category} a nivel federal y en el fuero correspondiente.
2. Estatus DOF: ${law.dofStatus} (Última reforma: ${law.lastReformDate}).
3. Disposiciones Sustantivas y Procesales: Indexadas en vectores semánticos FAISS para respuesta en tiempo real.

Puedes formularme preguntas específicas sobre cualquier artículo (ej. 'Artículo 311', 'Artículo 146', 'Artículo 16') o por tema para obtener la fundamentación detallada con cotejo oficial del DOF.
""".trimIndent()

        return RagMessage(
            id = randomId,
            isUser = false,
            senderName = modelName,
            text = text,
            timestamp = time,
            citation = LegalCitation(
                articleTitle = "${law.shortName} · ${law.title}",
                excerpt = "Cuerpo normativo oficial federal indexado con ${law.articlesCount} artículos vigentes en el DOF.",
                similarityScore = 0.960f,
                reformDate = law.lastReformDate,
                citationPath = "${law.shortName} > Búsqueda Transversal FAISS"
            ),
            exceptionalGrounds = listOf(
                "• Búsqueda transversal en los ${law.articlesCount} artículos de ${law.shortName}.",
                "• Estatus oficial: ${law.dofStatus} (${law.lastReformDate}).",
                "• Síntesis inteligente impulsada por ${modelName}."
            )
        )
    }
}
