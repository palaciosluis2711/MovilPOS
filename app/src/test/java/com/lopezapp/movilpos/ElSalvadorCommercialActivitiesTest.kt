package com.lopezapp.movilpos

import com.lopezapp.movilpos.util.ElSalvadorCommercialActivities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElSalvadorCommercialActivitiesTest {

    @Test
    fun activitiesList_isNotEmpty() {
        val activities = ElSalvadorCommercialActivities.activities
        assertTrue(activities.isNotEmpty())
        assertTrue(activities.contains("Comercio al por menor de productos diversos"))
        assertTrue(activities.contains("Actividades de programación, consultoría informática y actividades conexas"))
    }

    @Test
    fun filter_withEmptyQuery_returnsAllActivities() {
        val result = ElSalvadorCommercialActivities.filter("")
        assertEquals(ElSalvadorCommercialActivities.activities.size, result.size)
    }

    @Test
    fun filter_withQuery_returnsMatchingActivities() {
        val result = ElSalvadorCommercialActivities.filter("restaurantes")
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.contains("restaurantes", ignoreCase = true) })
    }

    @Test
    fun filter_caseInsensitive_returnsMatches() {
        val uppercaseResult = ElSalvadorCommercialActivities.filter("TRANSPORTE")
        val lowercaseResult = ElSalvadorCommercialActivities.filter("transporte")
        assertEquals(uppercaseResult, lowercaseResult)
        assertTrue(uppercaseResult.isNotEmpty())
    }
}
