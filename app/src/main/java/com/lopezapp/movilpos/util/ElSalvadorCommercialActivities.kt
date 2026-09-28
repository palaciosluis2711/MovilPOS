package com.lopezapp.movilpos.util

object ElSalvadorCommercialActivities {

    val activities: List<String> = listOf(
        "Comercio al por menor de productos diversos",
        "Comercio al por menor de alimentos, bebidas y tabaco",
        "Comercio al por menor en comercios no especializados",
        "Comercio al por menor de prendas de vestir, calzado y artículos de cuero",
        "Comercio al por menor de productos farmacéuticos, medicinales y cosméticos",
        "Comercio al por menor de artículos de ferretería, pinturas y vidrio",
        "Comercio al por menor de equipo de computación, telecomunicaciones y programas informáticos",
        "Comercio al por menor de electrodomésticos, muebles y artículos para el hogar",
        "Comercio al por menor de repuestos y accesorios para vehículos",
        "Comercio al por mayor de alimentos, bebidas y tabaco",
        "Comercio al por mayor de productos farmacéuticos y cosméticos",
        "Comercio al por mayor de maquinaria, equipo y materiales de construcción",
        "Comercio al por mayor de productos textiles y prendas de vestir",
        "Comercio al por mayor y menor de vehículos automotores y motocicletas",
        "Mantenimiento y reparación de vehículos automotores",
        "Servicios de restaurantes, cafeterías y servicios móviles de comidas",
        "Servicios de catering y suministro de comidas por encargo",
        "Servicios de alojamiento, hoteles y hospedajes",
        "Actividades de programación, consultoría informática y actividades conexas",
        "Procesamiento de datos, hospedaje web y actividades conexas",
        "Servicios de contabilidad, teneduría de libros y auditoría",
        "Asesoramiento en materia de impuestos y consultoría fiscal",
        "Actividades de consultoría de gestión y asesoramiento empresarial",
        "Servicios jurídicos y legales",
        "Servicios médicos, odontológicos y atención de la salud humana",
        "Transporte de carga por carretera",
        "Transporte de pasajeros por carretera y servicios de taxi",
        "Servicios de alquiler y arrendamiento de vehículos y maquinaria",
        "Servicios de arquitectura, ingeniería y consultoría técnica",
        "Servicios de publicidad, mercadeo y estudios de mercado",
        "Venta, alquiler y explotación de bienes inmuebles",
        "Servicios de reparación de computadoras y enseres domésticos",
        "Actividades de diseño especializado, fotografía y producción audiovisual",
        "Servicios de educación, enseñanza y capacitación",
        "Servicios de belleza, peluquería y tratamientos de estética",
        "Servicios de limpieza general de edificios e instalaciones",
        "Servicios de seguridad privada, custodia e investigación",
        "Producción, impresión de artes gráficas y servicios de duplicación",
        "Fabricación de productos alimenticios y bebidas",
        "Fabricación de prendas de vestir, calzado y textiles",
        "Cultivo de productos agrícolas y crianza de animales",
        "Reparación e instalación de maquinaria y equipo industrial",
        "Otras actividades de servicios personales y comerciales",
    )

    fun filter(query: String): List<String> {
        if (query.isBlank()) return activities
        val normalizedQuery = query.trim().lowercase()
        return activities.filter { activity ->
            activity.lowercase().contains(normalizedQuery)
        }
    }
}
