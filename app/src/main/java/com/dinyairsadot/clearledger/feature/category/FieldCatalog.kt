package com.dinyairsadot.clearledger.feature.category

import com.dinyairsadot.clearledger.R

/**
 * Static catalog of optional fields grouped by topics.
 * Used to help users add common fields to categories.
 * Field names are string resource IDs so they follow the app's language locale.
 */
object FieldCatalog {

    data class FieldTopic(
        val id: String,
        val nameResId: Int,
        val fieldNameResIds: List<Int>
    )

    fun getTopics(): List<FieldTopic> = listOf(
        FieldTopic(
            id = "general",
            nameResId = R.string.topic_general,
            fieldNameResIds = listOf(
                R.string.field_invoice_date,
                R.string.field_payment_due_date,
                R.string.field_account_number,
                R.string.field_id_number,
                R.string.field_vendor_name
            )
        ),
        FieldTopic(
            id = "electricity",
            nameResId = R.string.topic_electricity,
            fieldNameResIds = listOf(
                R.string.field_meter_id,
                R.string.field_consumption_kwh,
                R.string.field_account_number,
                R.string.field_property_id
            )
        ),
        FieldTopic(
            id = "water",
            nameResId = R.string.topic_water,
            fieldNameResIds = listOf(
                R.string.field_meter_id,
                R.string.field_consumption_cubic_meters,
                R.string.field_account_number,
                R.string.field_property_id
            )
        ),
        FieldTopic(
            id = "arnona",
            nameResId = R.string.topic_arnona,
            fieldNameResIds = listOf(
                R.string.field_property_id,
                R.string.field_property_address,
                R.string.field_id_number
            )
        ),
        FieldTopic(
            id = "gas",
            nameResId = R.string.topic_gas,
            fieldNameResIds = listOf(
                R.string.field_meter_id,
                R.string.field_consumption_cubic_meters,
                R.string.field_account_number
            )
        ),
        FieldTopic(
            id = "communication",
            nameResId = R.string.topic_communication,
            fieldNameResIds = listOf(
                R.string.field_subscription_number,
                R.string.field_phone_number,
                R.string.field_package
            )
        ),
        FieldTopic(
            id = "national_insurance",
            nameResId = R.string.topic_national_insurance,
            fieldNameResIds = listOf(
                R.string.field_id_number,
                R.string.field_billing_period
            )
        ),
        FieldTopic(
            id = "income_tax",
            nameResId = R.string.topic_income_tax,
            fieldNameResIds = listOf(
                R.string.field_id_number,
                R.string.field_tax_year,
                R.string.field_billing_period
            )
        ),
        FieldTopic(
            id = "health_fund",
            nameResId = R.string.topic_health_fund,
            fieldNameResIds = listOf(
                R.string.field_id_number,
                R.string.field_member_number,
                R.string.field_supplementary_insurance_type
            )
        ),
        FieldTopic(
            id = "insurances",
            nameResId = R.string.topic_insurances,
            fieldNameResIds = listOf(
                R.string.field_policy_number,
                R.string.field_validity_date,
                R.string.field_coverage_amount
            )
        ),
        FieldTopic(
            id = "car_insurance",
            nameResId = R.string.topic_car_insurance,
            fieldNameResIds = listOf(
                R.string.field_policy_number,
                R.string.field_vehicle_number,
                R.string.field_validity_date
            )
        )
    )
}
