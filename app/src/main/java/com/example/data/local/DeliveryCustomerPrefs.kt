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
    val customerPhone: String = "",
    val deliveryZone: DeliveryZone = DeliveryZone.INSIDE_HAY_AL_WURUD,
    val detailedAddress: String = "",
    val hasSavedData: Boolean = false
)

object DeliveryCustomerPrefs {
    private val KEY_CUSTOMER_NAME = stringPreferencesKey("customer_name")
    private val KEY_CUSTOMER_PHONE = stringPreferencesKey("delivery_customer_phone")
    private val KEY_DELIVERY_ZONE = stringPreferencesKey("delivery_zone")
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
        customerPhone: String,
        deliveryZone: DeliveryZone,
        detailedAddress: String
    ) {
        val safeName = customerName.trim().take(80)
        val safePhone = customerPhone.trim().take(30)
        val safeAddress = detailedAddress.trim().take(240)
        withContext(NonCancellable) {
            getStore(context).edit { prefs ->
                prefs[KEY_CUSTOMER_NAME] = safeName
                prefs[KEY_CUSTOMER_PHONE] = safePhone
                prefs[KEY_DELIVERY_ZONE] = deliveryZone.name
                prefs[KEY_DETAILED_ADDRESS] = safeAddress
            }
        }
    }

    fun saveCustomerInfoAsync(
        context: Context,
        customerName: String,
        customerPhone: String,
        deliveryZone: DeliveryZone,
        detailedAddress: String
    ): Job = persistenceScope.launch {
        saveCustomerInfo(context, customerName, customerPhone, deliveryZone, detailedAddress)
    }

    suspend fun clearCustomerInfo(context: Context) {
        withContext(NonCancellable) {
            getStore(context).edit { prefs ->
                prefs.remove(KEY_CUSTOMER_NAME)
                prefs.remove(KEY_CUSTOMER_PHONE)
                prefs.remove(KEY_DELIVERY_ZONE)
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
        val zoneRaw = this[KEY_DELIVERY_ZONE]
        val zone = if (zoneRaw != null) DeliveryZone.fromRaw(zoneRaw) else DeliveryZone.INSIDE_HAY_AL_WURUD
        val address = this[KEY_DETAILED_ADDRESS].orEmpty()
        val hasSaved = name.isNotBlank() || phone.isNotBlank() || address.isNotBlank() || zoneRaw != null
        return SavedDeliveryCustomerInfo(
            customerName = name,
            customerPhone = phone,
            deliveryZone = zone,
            detailedAddress = address,
            hasSavedData = hasSaved
        )
    }
}
