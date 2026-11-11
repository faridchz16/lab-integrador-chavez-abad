package com.tecsup.productos.data.remote

import com.tecsup.productos.data.remote.dto.ItemDto

private data class FichaEspanol(
    val descripcion: String,
    val titulo: String? = null,
    val marca: String? = null
)

private const val MARCA_ANTERIOR = "Realme"
private const val MARCA_NUEVA = "Xiaomi"
private const val SERIE_NUEVA = "Redmi"

private val fichas: Map<String, FichaEspanol> = mapOf(
    "iphone 5s" to FichaEspanol(
        "Un clásico de Apple con diseño compacto de 4 pulgadas, lector de huellas Touch ID y el " +
            "chip A7. Ideal para quien busca un equipo ligero y fácil de usar con una sola mano."
    ),
    "iphone 6" to FichaEspanol(
        "Elegante y delgado, con pantalla Retina HD de 4,7 pulgadas, chip A8 y Touch ID. Ofrece " +
            "un rendimiento fluido para redes sociales, fotos y uso diario."
    ),
    "iphone 13 pro" to FichaEspanol(
        "Gama alta de Apple con pantalla Super Retina XDR ProMotion de 120 Hz, chip A15 Bionic y " +
            "sistema de triple cámara con modo Cine y modo macro. Batería para todo el día."
    ),
    "iphone x" to FichaEspanol(
        "El primer iPhone sin bordes: pantalla OLED Super Retina de 5,8 pulgadas, reconocimiento " +
            "facial Face ID, doble cámara con modo retrato y carga inalámbrica."
    ),
    "oppo a57" to FichaEspanol(
        "Gama media con diseño moderno, pantalla de 6,5 pulgadas, batería de 5000 mAh con carga " +
            "rápida y cámara con inteligencia artificial para fotos nítidas en el día a día."
    ),
    "oppo f19 pro plus" to FichaEspanol(
        "Pensado para la fotografía: cámara principal de 48 MP con modo nocturno, pantalla AMOLED " +
            "de 6,4 pulgadas, conectividad 5G y carga rápida de 50 W."
    ),
    "oppo k1" to FichaEspanol(
        "Equilibrio entre precio y prestaciones: pantalla AMOLED con lector de huellas integrado, " +
            "doble cámara trasera y batería de larga duración."
    ),
    "realme c35" to FichaEspanol(
        "Económico y confiable: pantalla de 6,74 pulgadas a 90 Hz, cámara principal de 50 MP y " +
            "batería de 5000 mAh que dura más de un día de uso normal.",
        titulo = "Redmi 13C",
        marca = MARCA_NUEVA
    ),
    "realme x" to FichaEspanol(
        "Gama media con pantalla AMOLED de 6,67 pulgadas a 120 Hz, cámara de 108 MP y carga rápida " +
            "de 33 W. Diseño delgado y gran autonomía.",
        titulo = "Redmi Note 13",
        marca = MARCA_NUEVA
    ),
    "realme xt" to FichaEspanol(
        "El más completo de la serie: cámara de 200 MP con estabilización óptica, pantalla AMOLED " +
            "de 120 Hz, resistencia al polvo y al agua y carga rápida de 67 W.",
        titulo = "Redmi Note 13 Pro",
        marca = MARCA_NUEVA
    ),
    "samsung galaxy s10" to FichaEspanol(
        "Gama alta de Samsung con pantalla Dynamic AMOLED Infinity-O, triple cámara versátil, " +
            "lector de huellas ultrasónico en pantalla y carga inalámbrica reversible."
    ),
    "samsung galaxy s7" to FichaEspanol(
        "Diseño premium en vidrio y metal, pantalla Super AMOLED de 5,1 pulgadas, cámara de gran " +
            "rendimiento con poca luz y resistencia al agua y al polvo."
    ),
    "samsung galaxy s8" to FichaEspanol(
        "Pantalla Infinity curva de 5,8 pulgadas, cámara de 12 MP con enfoque rápido, " +
            "reconocimiento de iris y resistencia al agua IP68."
    ),
    "vivo s1" to FichaEspanol(
        "Estilo y rendimiento en gama media: pantalla Super AMOLED de 6,38 pulgadas, lector de " +
            "huellas en pantalla y triple cámara con inteligencia artificial."
    ),
    "vivo v9" to FichaEspanol(
        "Especialista en selfies: cámara frontal de 24 MP con embellecimiento por inteligencia " +
            "artificial, pantalla FullView de 6,3 pulgadas y diseño delgado."
    ),
    "vivo x21" to FichaEspanol(
        "Tecnología de vanguardia con lector de huellas bajo la pantalla, pantalla Super AMOLED de " +
            "6,28 pulgadas, doble cámara con modo nocturno y audio Hi-Fi."
    ),
    "ipad mini 2021 starlight" to FichaEspanol(
        "Tablet compacta de 8,3 pulgadas con pantalla Liquid Retina, chip A15 Bionic, conexión USB-C " +
            "y compatibilidad con Apple Pencil. Ideal para leer, tomar apuntes y ver series.",
        titulo = "iPad mini 2021 blanco estelar"
    ),
    "samsung galaxy tab s8 plus grey" to FichaEspanol(
        "Tablet de gama alta con pantalla Super AMOLED de 12,4 pulgadas a 120 Hz, S Pen incluido y " +
            "cuatro parlantes AKG. Pensada para estudiar, dibujar y trabajar en varias apps a la vez.",
        titulo = "Samsung Galaxy Tab S8+ gris"
    ),
    "samsung galaxy tab white" to FichaEspanol(
        "Tablet ligera para toda la familia, con pantalla amplia, batería de larga duración y " +
            "almacenamiento ampliable. Ideal para clases virtuales y entretenimiento.",
        titulo = "Samsung Galaxy Tab blanca"
    ),
    "apple watch series 4 gold" to FichaEspanol(
        "Reloj inteligente con pantalla más grande, electrocardiograma, detección de caídas y " +
            "monitoreo del ritmo cardiaco. Recibe notificaciones y mide tu actividad diaria.",
        titulo = "Apple Watch Series 4 dorado"
    ),
    "apple airpods" to FichaEspanol(
        "Audífonos inalámbricos que se conectan al instante con tu iPhone, con estuche de carga y " +
            "hasta 24 horas de uso. Incluyen micrófonos para llamadas y control con Siri.",
        titulo = "Apple AirPods"
    ),
    "apple airpods max silver" to FichaEspanol(
        "Audífonos de diadema con cancelación activa de ruido, modo ambiente y audio espacial. " +
            "Almohadillas de espuma viscoelástica y hasta 20 horas de batería.",
        titulo = "Apple AirPods Max plateados"
    ),
    "apple airpower wireless charger" to FichaEspanol(
        "Base de carga inalámbrica para cargar a la vez tu iPhone, Apple Watch y AirPods con solo " +
            "colocarlos encima.",
        titulo = "Cargador inalámbrico Apple AirPower"
    ),
    "apple homepod mini cosmic grey" to FichaEspanol(
        "Parlante inteligente compacto con sonido de 360 grados, control por voz con Siri y " +
            "conexión con los dispositivos de tu casa.",
        titulo = "Apple HomePod mini gris espacial"
    ),
    "apple iphone charger" to FichaEspanol(
        "Adaptador de corriente USB-C de 20 W para cargar tu iPhone de forma rápida y segura.",
        titulo = "Cargador Apple para iPhone"
    ),
    "apple magsafe battery pack" to FichaEspanol(
        "Batería externa magnética que se adhiere a la parte trasera del iPhone y lo carga sin " +
            "cables mientras lo usas.",
        titulo = "Batería externa Apple MagSafe"
    ),
    "iphone 12 silicone case with magsafe plum" to FichaEspanol(
        "Funda de silicona suave al tacto con imanes MagSafe integrados que protege tu iPhone 12 sin " +
            "perder la carga inalámbrica.",
        titulo = "Funda de silicona MagSafe para iPhone 12 ciruela"
    )
)

fun ItemDto.enEspanol(): ItemDto {
    val ficha = fichas[title.trim().lowercase()]
    val esMarcaAnterior = brand.equals(MARCA_ANTERIOR, ignoreCase = true)
    val tituloFinal = ficha?.titulo ?: if (esMarcaAnterior) {
        title.replace(MARCA_ANTERIOR, SERIE_NUEVA, ignoreCase = true)
    } else {
        title
    }
    return copy(
        title = tituloFinal,
        brand = ficha?.marca ?: if (esMarcaAnterior) MARCA_NUEVA else brand,
        description = ficha?.descripcion ?: descripcionGenerica(brand)
    )
}

private fun descripcionGenerica(marca: String?): String {
    val nombreMarca = marca?.takeIf { it.isNotBlank() }?.let { " de $it" }.orEmpty()
    return "Producto original$nombreMarca disponible en Stockify. Revisa su precio en soles y el " +
        "stock actualizado antes de agregarlo a tu carrito."
}
