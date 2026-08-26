package com.educalab.logicamate.data.local

import androidx.room.TypeConverter

/**
 * La mayoría de entidades ya usan tipos primitivos/String directamente
 * (decisión deliberada: los enums de dominio se guardan como su `.name`,
 * ver CoreEntities.kt/ChallengeEntities.kt), por lo que Converters.kt se
 * mantiene deliberadamente pequeño. Solo se necesita para listas simples de
 * String (p.ej. IDs excluidos en consultas dinámicas se pasan como List<String>
 * de forma nativa por Room, así que ni siquiera eso requiere conversión aquí).
 * Esta clase existe para futuras extensiones (p.ej. si se añadiera un campo
 * List<Int> a alguna entidad) y está cubierta por ConvertersTest.
 */
class Converters {
    @TypeConverter
    fun fromCsv(value: String?): List<String> = value?.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList()

    @TypeConverter
    fun toCsv(list: List<String>?): String = list?.joinToString(",") ?: ""
}
