package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.data.model.DeliveryZone
import com.example.data.model.PhoneEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.WeakHashMap

data class SavedDeliveryCustomerInfo(
    val customerName: String = "",
    val phones: List<PhoneEntry> = emptyList(),
    val customerPhone: String = "",
    val deliveryZone: DeliveryZone = DeliveryZone.INSIDE_HAY_AL_WURUD,
    val customZoneLabel: String = "",
    val detailedAddress: String = "",
    val hasSavedData: Boolean = false
)

object DeliveryCustomerPrefs {
    private val KEY_CUSTOMER_NAME = stringPreferencesKey("customer_name")
    private val KEY_CUSTOMER_PHONE = stringPreferencesKey("delivery_customer_phone")
    private val KEY_CUSTOMER_PHONES_LIST = stringPreferencesKey("delivery_customer_phones_list")
    private val KEY_DELIVERY_ZONE = stringPreferencesKey("delivery_zone")
    private val KEY_CUSTOM_ZONE_LABEL = stringPreferencesKey("custom_delivery_zone_label")
    private val KEY_DETAILED_ADDRESS = stringPreferencesKey("detailed_address")

    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val storesByAppContext = WeakHashMap<Context, DataStore<Preferences>>()

    private fun getStore(context: Context): DataStore<Preferences> {
        val appContext = context.applicationContext ?: context
        return synchronized(storesByAppContext) {
            storesByAppContext.getOrPut(appContext) {
                PreferenceDataStoreFactory.create(
                    produceFile = { appContext.preferencesDataStoreFile("delivery_customer_prefs") }
                )
            }
        }
    }

    suspend fun readSavedCustomerInfo(context: Context): SavedDeliveryCustomerInfo {
        return getStore(context).data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { it.toSavedInfo() }
            .first()
    }

    suspend fun saveCustomerInfo(
        context: Context,
        customerName: String,
        phones: List<PhoneEntry>,
        deliveryZone: DeliveryZone,
        detailedAddress: String,
        customZoneLabel: String = ""
    ) {
        val safeName = customerName.trim().take(80)
        val serializedPhones = PhoneEntry.serializeList(phones)
        val primaryPhone = phones.firstOrNull { it.number.isNotBlank() }?.number?.trim()?.take(30).orEmpty()
        val safeAddress = detailedAddress.trim().take(240)
        val safeCustomZone = customZoneLabel.trim().take(60)
        withContext(NonCancellable) {
            getStore(context).edit { prefs ->
                prefs[KEY_CUSTOMER_NAME] = safeName
                prefs[KEY_CUSTOMER_PHONE] = primaryPhone
                prefs[KEY_CUSTOMER_PHONES_LIST] = serializedPhones
                prefs[KEY_DELIVERY_ZONE] = deliveryZone.name
                prefs[KEY_CUSTOM_ZONE_LABEL] = safeCustomZone
                prefs[KEY_DETAILED_ADDRESS] = safeAddress
            }
        }
    }

    fun saveCustomerInfoAsync(
        context: Context,
        customerName: String,
        phones: List<PhoneEntry>,
        deliveryZone: DeliveryZone,
        detailedAddress: String,
        customZoneLabel: String = ""
    ): Job = persistenceScope.launch {
        saveCustomerInfo(context, customerName, phones, deliveryZone, detailedAddress, customZoneLabel)
    }

    suspend fun clearCustomerInfo(context: Context) {
        withContext(NonCancellable) {
            getStore(context).edit { prefs ->
                prefs.remove(KEY_CUSTOMER_NAME)
                prefs.remove(KEY_CUSTOMER_PHONE)
                prefs.remove(KEY_CUSTOMER_PHONES_LIST)
                prefs.remove(KEY_DELIVERY_ZONE)
                prefs.remove(KEY_CUSTOM_ZONE_LABEL)
                prefs.remove(KEY_DETAILED_ADDRESS)
            }
        }
    }

    fun clearCustomerInfoAsync(context: Context): Job = persistenceScope.launch {
        clearCustomerInfo(context)
    }

    private fun Preferences.toSavedInfo(): SavedDeliveryCustomerInfo {
        val name = this[KEY_CUSTOMER_NAME].orEmpty()
        val phone = this[KEY_CUSTOMER_PHONE].orEmpty()
        val phonesListRaw = this[KEY_CUSTOMER_PHONES_LIST]
        val phones = if (!phonesListRaw.isNullOrBlank()) {
            PhoneEntry.parseList(phonesListRaw)
        } else if (phone.isNotBlank()) {
            listOf(PhoneEntry(phone, "موبايل"))
        } else {
            emptyList()
        }
        val zoneRaw = this[KEY_DELIVERY_ZONE]
        val zone = if (zoneRaw != null) DeliveryZone.fromRaw(zoneRaw) else DeliveryZone.INSIDE_HAY_AL_WURUD
        val customZone = this[KEY_CUSTOM_ZONE_LABEL].orEmpty()
        val address = this[KEY_DETAILED_ADDRESS].orEmpty()
        val hasSaved = name.isNotBlank() || phones.isNotEmpty() || address.isNotBlank() || zoneRaw != null || customZone.isNotBlank()
        return SavedDeliveryCustomerInfo(
            customerName = name,
            phones = phones,
            customerPhone = phone,
            deliveryZone = zone,
            customZoneLabel = customZone,
            detailedAddress = address,
            hasSavedData = hasSaved
        )
    }
}
