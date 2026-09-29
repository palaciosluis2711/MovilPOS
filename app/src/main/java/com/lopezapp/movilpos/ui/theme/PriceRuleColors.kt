package com.lopezapp.movilpos.ui.theme

import androidx.compose.ui.graphics.Color
import com.lopezapp.movilpos.data.model.PriceRule

import kotlin.math.abs

data class PriceRulePalette(
    val backgroundColor: Color,
    val darkBackgroundColor: Color,
    val accentColor: Color,
    val darkAccentColor: Color,
) {
    fun getBackgroundColor(isDark: Boolean): Color = if (isDark) darkBackgroundColor else backgroundColor
    fun getAccentColor(isDark: Boolean): Color = if (isDark) darkAccentColor else accentColor
}

object PriceRuleColors {
    val palettes = listOf(
        // 1. Soft Blue
        PriceRulePalette(
            backgroundColor = Color(0xFFE3F2FD),
            darkBackgroundColor = Color(0xFF15293E),
            accentColor = Color(0xFF1565C0),
            darkAccentColor = Color(0xFF64B5F6)
        ),
        // 2. Soft Green
        PriceRulePalette(
            backgroundColor = Color(0xFFE8F5E9),
            darkBackgroundColor = Color(0xFF1B3320),
            accentColor = Color(0xFF2E7D32),
            darkAccentColor = Color(0xFF81C784)
        ),
        // 3. Soft Amber / Yellow
        PriceRulePalette(
            backgroundColor = Color(0xFFFFF8E1),
            darkBackgroundColor = Color(0xFF3B3219),
            accentColor = Color(0xFFD84315),
            darkAccentColor = Color(0xFFFFD54F)
        ),
        // 4. Soft Purple
        PriceRulePalette(
            backgroundColor = Color(0xFFF3E5F5),
            darkBackgroundColor = Color(0xFF2E1938),
            accentColor = Color(0xFF6A1B9A),
            darkAccentColor = Color(0xFFBA68C8)
        ),
        // 5. Soft Teal
        PriceRulePalette(
            backgroundColor = Color(0xFFE0F2F1),
            darkBackgroundColor = Color(0xFF133230),
            accentColor = Color(0xFF00695C),
            darkAccentColor = Color(0xFF4DB6AC)
        ),
        // 6. Soft Orange
        PriceRulePalette(
            backgroundColor = Color(0xFFFFF3E0),
            darkBackgroundColor = Color(0xFF3E2312),
            accentColor = Color(0xFFE65100),
            darkAccentColor = Color(0xFFFFB74D)
        ),
        // 7. Soft Indigo
        PriceRulePalette(
            backgroundColor = Color(0xFFE8EAF6),
            darkBackgroundColor = Color(0xFF1C203B),
            accentColor = Color(0xFF283593),
            darkAccentColor = Color(0xFF7986CB)
        ),
        // 8. Soft Cyan
        PriceRulePalette(
            backgroundColor = Color(0xFFE0F7FA),
            darkBackgroundColor = Color(0xFF123438),
            accentColor = Color(0xFF00838F),
            darkAccentColor = Color(0xFF4DD0E1)
        )
    )

    fun getPalette(index: Int): PriceRulePalette {
        val safeIndex = if (index >= 0) index % palettes.size else ((-index) % palettes.size)
        return palettes[safeIndex]
    }

    fun getPaletteForRule(ruleId: String?, priceRules: List<PriceRule>): PriceRulePalette {
        if (ruleId == null) return palettes[0]
        val index = priceRules.indexOfFirst { it.id == ruleId }
        return if (index != -1) {
            getPalette(index)
        } else {
            val hashIndex = abs(ruleId.hashCode()) % palettes.size
            palettes[hashIndex]
        }
    }

    fun getRuleAccentColor(
        ruleId: String?,
        priceRules: List<PriceRule>,
        isDark: Boolean
    ): Color {
        return getPaletteForRule(ruleId, priceRules).getAccentColor(isDark)
    }
}
