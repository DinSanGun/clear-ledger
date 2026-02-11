package com.dinyairsadot.taxtracker.feature.category

/**
 * Static catalog of optional fields grouped by topics.
 * Used to help users add common fields to categories.
 */
object FieldCatalog {
    
    data class FieldTopic(
        val id: String,
        val nameResId: Int,
        val fields: List<String>
    )
    
    fun getTopics(): List<FieldTopic> = listOf(
        FieldTopic(
            id = "general",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_general,
            fields = listOf(
                "תאריך הפקה",
                "תאריך אחרון לתשלום",
                "מספר חשבון",
                "מספר ת.ז.",
                "שם ספק"
            )
        ),
        FieldTopic(
            id = "electricity",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_electricity,
            fields = listOf(
                "מספר מונה",
                "צריכה (קוט\"ש)",
                "מספר חשבון",
                "מספר נכס"
            )
        ),
        FieldTopic(
            id = "water",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_water,
            fields = listOf(
                "מספר מונה",
                "צריכה (מ\"ק)",
                "מספר חשבון",
                "מספר נכס"
            )
        ),
        FieldTopic(
            id = "arnona",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_arnona,
            fields = listOf(
                "מספר נכס",
                "כתובת נכס",
                "מספר ת.ז."
            )
        ),
        FieldTopic(
            id = "gas",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_gas,
            fields = listOf(
                "מספר מונה",
                "צריכה (מ\"ק)",
                "מספר חשבון"
            )
        ),
        FieldTopic(
            id = "communication",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_communication,
            fields = listOf(
                "מספר מנוי",
                "מספר טלפון",
                "חבילה"
            )
        ),
        FieldTopic(
            id = "national_insurance",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_national_insurance,
            fields = listOf(
                "מספר ת.ז.",
                "תקופת חיוב"
            )
        ),
        FieldTopic(
            id = "income_tax",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_income_tax,
            fields = listOf(
                "מספר ת.ז.",
                "שנת מס",
                "תקופת חיוב"
            )
        ),
        FieldTopic(
            id = "health_fund",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_health_fund,
            fields = listOf(
                "מספר ת.ז.",
                "מספר חבר",
                "סוג ביטוח משלים"
            )
        ),
        FieldTopic(
            id = "insurances",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_insurances,
            fields = listOf(
                "מספר פוליסה",
                "תאריך תוקף",
                "סכום כיסוי"
            )
        ),
        FieldTopic(
            id = "car_insurance",
            nameResId = com.dinyairsadot.taxtracker.R.string.topic_car_insurance,
            fields = listOf(
                "מספר פוליסה",
                "מספר רכב",
                "תאריך תוקף"
            )
        )
    )
}
