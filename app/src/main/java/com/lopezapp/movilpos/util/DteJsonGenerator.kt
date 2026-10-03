package com.lopezapp.movilpos.util

import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.Sale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DteJsonGenerator {
    fun generateDteJson(
        sale: Sale,
        businessInfo: BusinessInfo,
        config: ElectronicBillingConfig
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("America/El_Salvador") }
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("America/El_Salvador") }
        val date = Date(sale.dateMillis)

        val ambiente = if (config.environment.name == "PRODUCTION") "01" else "00"
        val tipoDte = sale.dteType ?: "01"
        val numeroControl = sale.dteControlNumber ?: "DTE-01-${sale.id.take(8).uppercase()}-000000000000001"
        val codigoGeneracion = sale.dteGenerationCode ?: "01234567-89AB-CDEF-0123-456789ABCDEF"
        val nitEmisor = businessInfo.nit.ifBlank { config.nit.ifBlank { "0614-010190-101-5" } }
        val nrcEmisor = businessInfo.nrc.ifBlank { "123456-7" }
        val nombreEmisor = businessInfo.name
        val direccionEmisor = businessInfo.address.ifBlank { "San Salvador, El Salvador" }
        val telefonoEmisor = businessInfo.phone.ifBlank { "2222-2222" }
        val correoEmisor = businessInfo.email.ifBlank { "contacto@negocio.com" }
        val actividadEmisor = config.economicActivity.ifBlank { "47110" }

        val itemsBuilder = StringBuilder()
        sale.items.forEachIndexed { index, item ->
            val ivaItem = item.subtotal * 0.13
            itemsBuilder.append("""
        {
            "numItem": ${index + 1},
            "tipoItem": 1,
            "cantidad": ${item.quantity},
            "codigo": "${item.productId.take(8)}",
            "uniMedida": 59,
            "descripcion": "${item.productName}",
            "precioUnitario": ${item.unitPrice},
            "montoDescuento": 0.0,
            "ventaNoSuj": 0.0,
            "ventaExenta": 0.0,
            "ventaGravada": ${item.subtotal},
            "ivaItem": $ivaItem
        }${if (index < sale.items.size - 1) "," else ""}""")
        }

        val totalGravada = sale.totalAmount
        val selloRecibido = sale.dteReceptionSeal ?: "MH-DTE-2025-00000000000001"

        return """{
    "identificacion": {
        "version": 1,
        "ambiente": "$ambiente",
        "tipoDte": "$tipoDte",
        "numeroControl": "$numeroControl",
        "codigoGeneracion": "$codigoGeneracion",
        "tipoModelo": 1,
        "tipoOperacion": 1,
        "fecEmi": "${dateFormat.format(date)}",
        "horEmi": "${timeFormat.format(date)}",
        "tipoMoneda": "USD"
    },
    "emisor": {
        "nit": "$nitEmisor",
        "nrc": "$nrcEmisor",
        "nombre": "$nombreEmisor",
        "nombreComercial": "$nombreEmisor",
        "tipoEstablecimiento": "01",
        "departamento": "06",
        "municipio": "14",
        "complemento": "$direccionEmisor",
        "telefono": "$telefonoEmisor",
        "correo": "$correoEmisor",
        "actividadEconomica": "$actividadEmisor"
    },
    "receptor": {
        "nombre": "${sale.customerName}",
        "tipoDocumento": "36",
        "numDocumento": "0210-010190-102-1",
        "nrc": "",
        "correo": "cliente@ejemplo.com",
        "telefono": "2200-0000",
        "direccion": "San Salvador"
    },
    "cuerpoDocumento": [
        $itemsBuilder
    ],
    "resumen": {
        "totalNoSuj": 0.0,
        "totalExenta": 0.0,
        "totalGravada": $totalGravada,
        "subTotalVentas": $totalGravada,
        "descuDescu": 0.0,
        "porcDescu": 0.0,
        "totalDescu": 0.0,
        "subTotal": $totalGravada,
        "ivaRete1": 0.0,
        "reteRenta": 0.0,
        "montoTotalOperacion": $totalGravada,
        "totalNoGravado": 0.0,
        "totalPagar": $totalGravada,
        "totalLetras": "${sale.totalAmount} DÓLARES",
        "condicionOperacion": 1,
        "pagos": [
            {
                "codigo": "01",
                "montoPago": $totalGravada,
                "referencia": "${sale.paymentMethodName}",
                "plazo": null,
                "periodo": null
            }
        ]
    },
    "selloRecibido": "$selloRecibido"
}"""
    }
}
