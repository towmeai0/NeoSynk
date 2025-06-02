package com.ayudevices.neosynkparent.data.model

data class MileStoneDataResponse(
    val milestone_results: MilestoneResults,
    val vital_trends: VitalTrends,
    val milestone_report: String
)


data class MilestoneResults(
    val Motor: Motor,
    val Sensory: Sensory,
    val Cognitive: Cognitive,
    val Feeding: Feeding
)

data class Motor(
    val percentage: Double,
    val completed: List<String>,
    val pending: List<String>
)

data class Sensory(
    val percentage: Double,
    val completed: List<String>,
    val pending: List<String>
)

data class Cognitive(
    val percentage: Double,
    val completed: List<String>,
    val pending: List<String>
)

data class Feeding(
    val percentage: Double,
    val completed: List<String>,
    val pending: List<String>
)

data class VitalTrends(
    val weight_kg: List<WeightKg>,
    val height_cm: List<HeightCm>,
    val heart_rate: List<HeartRate>,
    val spo2: List<Spo2>
)

data class WeightKg(
    val date: String,
    val value: Double
)

data class HeightCm(
    val date: String,
    val value: Double
)

data class HeartRate(
    val date: String,
    val value: Double
)

data class Spo2(
    val date: String,
    val value: Double
)
