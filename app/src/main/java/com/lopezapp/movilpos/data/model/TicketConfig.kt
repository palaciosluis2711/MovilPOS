package com.lopezapp.movilpos.data.model

enum class TicketPaperSize(val label: String, val mm: Int) {
    SIZE_57MM("57 mm", 57),
    SIZE_80MM("80 mm", 80)
}

data class TicketConfig(
    val showBusinessName: Boolean = true,
    val showNit: Boolean = true,
    val showNrc: Boolean = true,
    val showAddress: Boolean = true,
    val showPhone: Boolean = true,
    val showSocialMedia: Boolean = true,
    val showLogo: Boolean = true,
    val footerMessage: String = "¡Gracias por su compra! Vuelva pronto.",
    val paperSize: TicketPaperSize = TicketPaperSize.SIZE_80MM
)
