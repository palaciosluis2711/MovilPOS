package com.lopezapp.movilpos.data.repository

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.lopezapp.movilpos.data.model.AuthSession
import com.lopezapp.movilpos.data.model.BarcodeLabelConfig
import com.lopezapp.movilpos.data.model.BatchLabelItem
import com.lopezapp.movilpos.data.model.BluetoothPrinterConfig
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.CashShift
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.CustomerPayment
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.Expense
import com.lopezapp.movilpos.data.model.InventoryOutflow
import com.lopezapp.movilpos.data.model.LabelSize
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.ProductReturn
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.Quotation
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import com.lopezapp.movilpos.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class LocalDatabaseState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val brands: List<Brand> = emptyList(),
    val unitsOfMeasure: List<UnitOfMeasure> = emptyList(),
    val taxes: List<Tax> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val purchases: List<Purchase> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val contingencyDtes: List<Sale> = emptyList(),
    val quotations: List<Quotation> = emptyList(),
    val priceRules: List<PriceRule> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val businessInfo: BusinessInfo = BusinessInfo(),
    val ticketConfig: TicketConfig = TicketConfig(),
    val electronicBillingConfig: ElectronicBillingConfig = ElectronicBillingConfig(),
    val bluetoothPrinterConfig: BluetoothPrinterConfig = BluetoothPrinterConfig(),
    val labelSizes: List<LabelSize> = emptyList(),
    val barcodeLabelConfig: BarcodeLabelConfig = BarcodeLabelConfig(),
    val batchItems: List<BatchLabelItem> = emptyList(),
    val users: List<User> = emptyList(),
    val authSession: AuthSession? = null,
    val rememberMe: Boolean = true,
    val expenses: List<Expense> = emptyList(),
    val inventoryOutflows: List<InventoryOutflow> = emptyList(),
    val productReturns: List<ProductReturn> = emptyList(),
    val customerPayments: List<CustomerPayment> = emptyList(),
    val activeShift: CashShift? = null,
    val shiftHistory: List<CashShift> = emptyList()
)

object LocalDiskPersistence {
    private const val TAG = "LocalDiskPersistence"
    const val FILE_NAME = "local_movilpos_database.json"
    private val saveLock = Any()

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val jsonAdapter by lazy {
        moshi.adapter(LocalDatabaseState::class.java)
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        runCatching {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        }.onFailure {
            println("[$TAG] ERROR: $message ${throwable?.message ?: ""}")
        }
    }

    private fun logDebug(message: String) {
        runCatching {
            Log.d(TAG, message)
        }.onFailure {
            println("[$TAG] DEBUG: $message")
        }
    }

    suspend fun saveToDisk(context: Context, state: LocalDatabaseState) = withContext(Dispatchers.IO) {
        saveToDiskSync(context, state)
    }

    fun saveToDiskSync(context: Context, state: LocalDatabaseState) {
        synchronized(saveLock) {
            try {
                val jsonString = jsonAdapter.toJson(state)
                val filesDir = runCatching { context.filesDir }.getOrNull() ?: return
                val file = File(filesDir, FILE_NAME)
                val tmpFile = File(filesDir, "$FILE_NAME.tmp")
                tmpFile.writeText(jsonString, Charsets.UTF_8)
                if (tmpFile.exists()) {
                    if (file.exists()) {
                        file.delete()
                    }
                    tmpFile.renameTo(file)
                }
            } catch (e: Exception) {
                logError("Error saving local database to disk", e)
            }
        }
    }

    suspend fun loadFromDisk(context: Context): LocalDatabaseState? = withContext(Dispatchers.IO) {
        loadFromDiskSync(context)
    }

    fun loadFromDiskSync(context: Context): LocalDatabaseState? {
        synchronized(saveLock) {
            return try {
                val filesDir = runCatching { context.filesDir }.getOrNull() ?: return null
                val file = File(filesDir, FILE_NAME)
                if (!file.exists()) {
                    logDebug("Local database file does not exist.")
                    return null
                }
                val jsonString = file.readText(Charsets.UTF_8)
                if (jsonString.isBlank()) {
                    return null
                }
                jsonAdapter.fromJson(jsonString)
            } catch (e: Exception) {
                logError("Error loading local database from disk", e)
                null
            }
        }
    }

    fun hasSavedDatabase(context: Context): Boolean {
        synchronized(saveLock) {
            val filesDir = runCatching { context.filesDir }.getOrNull() ?: return false
            val file = File(filesDir, FILE_NAME)
            return file.exists() && file.length() > 0
        }
    }
}
