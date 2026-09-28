package com.lopezapp.movilpos.util

object ElSalvadorGeography {

    val departments: List<String> = listOf(
        "Ahuachapán",
        "Cabañas",
        "Chalatenango",
        "Cuscatlán",
        "La Libertad",
        "La Paz",
        "La Unión",
        "Morazán",
        "San Miguel",
        "San Salvador",
        "San Vicente",
        "Santa Ana",
        "Sonsonate",
        "Usulután"
    )

    private val data: Map<String, Map<String, List<String>>> = mapOf(
        "Ahuachapán" to mapOf(
            "Ahuachapán Norte" to listOf("Atiquizaya", "El Refugio", "San Lorenzo", "Turín"),
            "Ahuachapán Centro" to listOf("Ahuachapán", "Apaneca", "Concepción de Ataco", "Tacuba"),
            "Ahuachapán Sur" to listOf("Guaymango", "Jujutla", "San Francisco Menéndez", "San Pedro Puxtla")
        ),
        "Cabañas" to mapOf(
            "Cabañas Este" to listOf("Sensuntepeque", "Victoria", "Dolores", "Guacotecti", "San Isidro"),
            "Cabañas Oeste" to listOf("Ilobasco", "Tejutepeque", "Jutiapa", "Cinquera")
        ),
        "Chalatenango" to mapOf(
            "Chalatenango Norte" to listOf("La Palma", "Citalá", "San Ignacio"),
            "Chalatenango Centro" to listOf("Nueva Concepción", "Agua Caliente", "El Paraíso", "La Reina", "San Fernando", "San Francisco Morazán", "San Rafael", "Santa Rita"),
            "Chalatenango Sur" to listOf("Chalatenango", "Arcatao", "Azacualpa", "Cancasque", "Comalapa", "Concepción Quezaltepeque", "El Carrizal", "Las Vueltas", "Nombre de Jesús", "Ojos de Agua", "Potorico", "San Antonio de la Cruz", "San Antonio Los Ranchos", "San Francisco Lempa", "San Isidro Labrador", "San José Cancasque", "San José Las Flores", "San Luis del Carmen", "San Miguel de Mercedes")
        ),
        "Cuscatlán" to mapOf(
            "Cuscatlán Norte" to listOf("Suchitoto", "San José Guayabal", "Oratorio de Concepción", "San Bartolomé Perulapía", "San Pedro Perulapán"),
            "Cuscatlán Sur" to listOf("Cojutepeque", "San Rafael Cedros", "Candelaria", "Monte San Juan", "El Carmen", "San Cristóbal", "Santa Cruz Michapa", "Santa Cruz Analquito", "El Rosario", "San Ramón", "Tenancingo")
        ),
        "La Libertad" to mapOf(
            "La Libertad Norte" to listOf("Quezaltepeque", "San Matías", "San Pablo Tacachico"),
            "La Libertad Centro" to listOf("San Juan Opico", "Ciudad Arce"),
            "La Libertad Oeste" to listOf("Colón", "Jayaque", "Sacacoyo", "Tepecoyo", "Talnique"),
            "La Libertad Este" to listOf("Antiguo Cuscatlán", "Huizúcar", "Nuevo Cuscatlán", "San José Villanueva", "Zaragoza"),
            "La Libertad Sur" to listOf("Santa Tecla", "Comasagua"),
            "La Libertad Costa" to listOf("Chiltiupán", "Jicalapa", "La Libertad", "Tamanique", "Teotepeque")
        ),
        "La Paz" to mapOf(
            "La Paz Oeste" to listOf("Cuyultitán", "Olocuilta", "San Juan Talpa", "San Juan Tepezontes", "San Luis Talpa", "San Pedro Masahuat", "Tapalhuaca"),
            "La Paz Centro" to listOf("El Rosario", "Jerusalén", "Mercedes La Ceiba", "Paraíso de Osorio", "San Antonio Masahuat", "San Emigdio", "San Juan Nonualco", "San Pedro Nonualco", "Santa María Ostuma", "Santiago Nonualco"),
            "La Paz Este" to listOf("San Rafael Obrajuelo", "Zacatecoluca")
        ),
        "La Unión" to mapOf(
            "La Unión Norte" to listOf("Anamorós", "Bolívar", "Concepción de Oriente", "El Sauce", "Lislique", "Nueva Esparta", "Pasaquina", "Polorós", "San José", "Santa Rosa de Lima"),
            "La Unión Sur" to listOf("Conchagua", "El Carmen", "Intipucá", "La Unión", "Meanguera del Golfo", "San Alejo", "Yayuantique")
        ),
        "Morazán" to mapOf(
            "Morazán Norte" to listOf("Arambala", "Cacaopera", "Corinto", "El Rosario", "Joateca", "Jocoaitique", "Meanguera", "Perquín", "San Fernando", "San Isidro", "Torola"),
            "Morazán Sur" to listOf("Chilanga", "Delicias de Concepción", "El Divisadero", "Gualococti", "Guatajiagua", "Jocoro", "Lolotique", "Osicala", "San Carlos", "San Francisco Gotera", "San Simón", "Sensembra", "Sociedad", "Yamabal", "Yoloaiquín")
        ),
        "San Miguel" to mapOf(
            "San Miguel Norte" to listOf("Ciudad Barrios", "Sesori", "Nuevo Edén de San Juan", "San Gerardo", "San Luis de la Reina", "Carolina", "San Antonio del Mosco", "Chapeltique"),
            "San Miguel Centro" to listOf("San Miguel", "Comacarán", "Uluazapa", "Moncagua", "Quelepa", "Chirilagua"),
            "San Miguel Oeste" to listOf("Chinameca", "El Tránsito", "Lolotique", "Nueva Guadalupe", "San Jorge", "San Rafael Oriente")
        ),
        "San Salvador" to mapOf(
            "San Salvador Centro" to listOf("San Salvador", "Mejicanos", "Ayutuxtepeque", "Cuscatancingo", "Delgado"),
            "San Salvador Norte" to listOf("Aguilares", "El Paisnal", "Guazapa"),
            "San Salvador Este" to listOf("Soyapango", "Ilopango", "San Martín", "Tonacatepeque"),
            "San Salvador Oeste" to listOf("Apopa", "Nejapa"),
            "San Salvador Sur" to listOf("Panchimalco", "Rosario de Mora", "San Marcos", "Santo Tomás", "Santiago Texacuangos")
        ),
        "San Vicente" to mapOf(
            "San Vicente Norte" to listOf("Apastepeque", "Santa Clara", "San Ildefonso", "San Esteban Catarina", "San Sebastián", "San Lorenzo", "Santo Domingo"),
            "San Vicente Sur" to listOf("San Vicente", "Guadalupe", "Verapaz", "Tepetitán", "Tecoluca", "San Cayetano Istepeque")
        ),
        "Santa Ana" to mapOf(
            "Santa Ana Norte" to listOf("Masahuat", "Metapán", "Santa Rosa Guachipilín", "Texistepeque"),
            "Santa Ana Centro" to listOf("Santa Ana"),
            "Santa Ana Este" to listOf("Coatepeque", "El Congo"),
            "Santa Ana Oeste" to listOf("Candelaria de la Frontera", "Chalchuapa", "El Porvenir", "San Antonio Pajonal", "San Sebastián Salitrillo", "Santiago de la Frontera")
        ),
        "Sonsonate" to mapOf(
            "Sonsonate Norte" to listOf("Juayúa", "Nahuizalco", "Salcoatitán", "Santa Catarina Masahuat"),
            "Sonsonate Centro" to listOf("Sonsonate", "Sonzacate", "Nahulingo", "San Antonio del Monte", "Santo Domingo de Guzmán"),
            "Sonsonate Este" to listOf("Izalco", "Armenia", "Caluco", "Cuisnahuat", "Santa Isabel Ishuatán", "San Julián"),
            "Sonsonate Oeste" to listOf("Acajutla")
        ),
        "Usulután" to mapOf(
            "Usulután Norte" to listOf("Alegria", "Berlín", "El Triunfo", "Estanzuelas", "Jucuapa", "Mercedes Umaña", "Nueva Granada", "Santiago de María"),
            "Usulután Este" to listOf("California", "Concepción Batres", "Dionisio", "Ereguayquín", "Jucuarán", "Ozatlán", "San Dionisio", "Santa Elena", "Santa María", "Tecapán", "Usulután"),
            "Usulután Oeste" to listOf("Jiquilisco", "Puerto El Triunfo", "San Agustín", "San Francisco Javier")
        )
    )

    fun getMunicipalities(department: String): List<String> {
        return data[department]?.keys?.toList() ?: emptyList()
    }

    fun getDistricts(department: String, municipality: String): List<String> {
        return data[department]?.get(municipality) ?: emptyList()
    }
}
