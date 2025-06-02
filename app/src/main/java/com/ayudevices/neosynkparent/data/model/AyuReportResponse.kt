package com.ayudevices.neosynkparent.data.model

data class AyuReportResponse(
    val response_text: ResponseText,
    val intent: String
)


data class ResponseText(
    val Age: Int,
    val Vitals: Vitals,
    val Milestones: Milestones,
    val Health_Events: List<HealthEvent>
)

data class Vitals(
    val height_cm: List<VitalEntry>,
    val weight_kg: List<VitalEntry>,
    val heart_rate: List<VitalEntry>,
    val spo2: List<VitalEntry>
)

data class VitalEntry(
    val date: String,
    val value: Double
)

data class Milestones(
    val Completion_Percentage: CompletionPercentage,
    val Pending: List<String>,
    val Completed: List<String>
)

data class CompletionPercentage(
    val Motor: Double,
    val Sensory: Double,
    val Cognitive: Double,
    val Feeding: Double
)

data class HealthEvent(
    val date: String,
    val condition: String,
    val symptoms: String,
    val diagnosis_summary: String
)