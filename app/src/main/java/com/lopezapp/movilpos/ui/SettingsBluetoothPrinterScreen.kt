package com.lopezapp.movilpos.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lopezapp.movilpos.data.model.BluetoothPrinterConfig
import com.lopezapp.movilpos.data.model.TicketPaperSize
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBluetoothPrinterScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.bluetoothPrinterConfig
    val paperSize = uiState.ticketConfig.paperSize
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasPermissions by remember {
        mutableStateOf(checkBluetoothPermissions(context))
    }

    val bluetoothAdapter: BluetoothAdapter? = remember {
        try {
            (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
                ?: @Suppress("DEPRECATION") BluetoothAdapter.getDefaultAdapter()
        } catch (_: Exception) {
            null
        }
    }

    var isBluetoothEnabled by remember {
        mutableStateOf(bluetoothAdapter?.isEnabled == true)
    }

    var pairedDevices by remember {
        mutableStateOf<List<BluetoothDevice>>(emptyList())
    }

    var discoveredDevices by remember {
        mutableStateOf<List<BluetoothDevice>>(emptyList())
    }

    var isScanning by remember {
        mutableStateOf(false)
    }

    fun refreshPairedDevices() {
        hasPermissions = checkBluetoothPermissions(context)
        isBluetoothEnabled = bluetoothAdapter?.isEnabled == true
        pairedDevices = if (hasPermissions && isBluetoothEnabled) {
            getPairedBluetoothDevices(context, bluetoothAdapter)
        } else {
            emptyList()
        }
    }

    LaunchedEffect(hasPermissions, isBluetoothEnabled) {
        refreshPairedDevices()
    }

    // Register broadcast receiver for discovery
    DisposableEffect(bluetoothAdapter, hasPermissions) {
        if (!hasPermissions || bluetoothAdapter == null) {
            onDispose {}
        } else {
            val receiver = object : BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothDevice.ACTION_FOUND -> {
                            val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                            } else {
                                @Suppress("DEPRECATION")
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            }
                            if (device != null) {
                                val address = device.address
                                if (discoveredDevices.none { it.address == address } &&
                                    pairedDevices.none { it.address == address }) {
                                    discoveredDevices = discoveredDevices + device
                                }
                            }
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            isScanning = false
                        }
                    }
                }
            }
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
            onDispose {
                try {
                    context.unregisterReceiver(receiver)
                } catch (_: Exception) {}
                try {
                    @SuppressLint("MissingPermission")
                    if (bluetoothAdapter.isDiscovering) {
                        bluetoothAdapter.cancelDiscovery()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    @SuppressLint("MissingPermission")
    val startDiscovery: () -> Unit = {
        if (hasPermissions && isBluetoothEnabled && bluetoothAdapter != null) {
            try {
                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }
                discoveredDevices = emptyList()
                val started = bluetoothAdapter.startDiscovery()
                if (started) {
                    isScanning = true
                    Toast.makeText(context, "Buscando nuevos dispositivos...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No se pudo iniciar la búsqueda", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Error de permisos al buscar: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al buscar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Bluetooth o permisos no disponibles", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionsResult ->
        val granted = permissionsResult.values.all { it }
        hasPermissions = granted || checkBluetoothPermissions(context)
        refreshPairedDevices()
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        isBluetoothEnabled = bluetoothAdapter?.isEnabled == true
        refreshPairedDevices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impresora Bluetooth") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { refreshPairedDevices() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar dispositivos",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Bluetooth Permission & Connection Status Card
            BluetoothStatusCard(
                bluetoothAdapter = bluetoothAdapter,
                hasPermissions = hasPermissions,
                isBluetoothEnabled = isBluetoothEnabled,
                onRequestPermissions = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN,
                            ),
                        )
                    }
                },
                onEnableBluetooth = {
                    try {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        enableBluetoothLauncher.launch(enableBtIntent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "No se pudo solicitar la activación de Bluetooth: ${e.message}",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )

            // 2. Impresora Predeterminada Seleccionada
            SelectedPrinterCard(
                config = config,
                paperSize = paperSize,
                isBluetoothReady = hasPermissions && isBluetoothEnabled,
                onTestPrintClick = {
                    val mac = config.macAddress
                    if (mac.isNullOrBlank()) {
                        Toast.makeText(context, "Ninguna impresora seleccionada", Toast.LENGTH_SHORT).show()
                        return@SelectedPrinterCard
                    }
                    Toast.makeText(context, "Imprimiendo ticket de prueba...", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            viewModel.sendTestPrint(mac, paperSize)
                        }
                        if (result.isSuccess) {
                            Toast.makeText(context, "¡Ticket de prueba impreso correctamente!", Toast.LENGTH_LONG).show()
                        } else {
                            val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                            Toast.makeText(context, "Error al imprimir: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onAutoPrintChange = { autoPrint ->
                    viewModel.updateBluetoothPrinterConfig(
                        config.copy(autoPrintSales = autoPrint),
                    )
                    Toast.makeText(context, "Configuración de impresora guardada exitosamente", Toast.LENGTH_SHORT).show()
                },
                onDeselectPrinter = {
                    viewModel.updateBluetoothPrinterConfig(
                        config.copy(macAddress = null, deviceName = null, isConnected = false),
                    )
                    Toast.makeText(context, "Configuración de impresora guardada exitosamente", Toast.LENGTH_SHORT).show()
                },
            )

            // 3. Dispositivos Bluetooth (con filtrado inteligente y búsqueda)
            BluetoothDevicesSection(
                pairedDevices = pairedDevices,
                discoveredDevices = discoveredDevices,
                isScanning = isScanning,
                selectedMacAddress = config.macAddress,
                hasPermissions = hasPermissions,
                isBluetoothEnabled = isBluetoothEnabled,
                onStartDiscovery = startDiscovery,
                onSelectDevice = { device ->
                    @SuppressLint("MissingPermission")
                    val devName = device.name ?: "Impresora Bluetooth"
                    viewModel.updateBluetoothPrinterConfig(
                        config.copy(
                            macAddress = device.address,
                            deviceName = devName,
                            isConnected = true,
                        ),
                    )
                    Toast.makeText(context, "Configuración de impresora guardada exitosamente", Toast.LENGTH_SHORT).show()
                },
                onOpenBluetoothSettings = {
                    try {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "No se pudo abrir la configuración de Bluetooth", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }
}

@Composable
fun BluetoothStatusCard(
    bluetoothAdapter: BluetoothAdapter?,
    hasPermissions: Boolean,
    isBluetoothEnabled: Boolean,
    onRequestPermissions: () -> Unit,
    onEnableBluetooth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isHardwareAvailable = bluetoothAdapter != null

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isHardwareAvailable || !hasPermissions || !isBluetoothEnabled -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = when {
                        !isHardwareAvailable || !isBluetoothEnabled -> Icons.Default.BluetoothDisabled
                        !hasPermissions -> Icons.Default.Warning
                        else -> Icons.Default.BluetoothConnected
                    },
                    contentDescription = null,
                    tint = when {
                        !isHardwareAvailable || !hasPermissions || !isBluetoothEnabled -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.size(28.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Estado de Bluetooth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            !isHardwareAvailable || !hasPermissions || !isBluetoothEnabled -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                    Text(
                        text = when {
                            !isHardwareAvailable -> "El dispositivo no soporta Bluetooth hardware."
                            !hasPermissions -> "Permisos de Bluetooth necesarios (BLUETOOTH_CONNECT / BLUETOOTH_SCAN)."
                            !isBluetoothEnabled -> "El Bluetooth está desactivado."
                            else -> "Bluetooth activo y con permisos concedidos."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            !isHardwareAvailable || !hasPermissions || !isBluetoothEnabled -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                }
            }

            if (isHardwareAvailable && !hasPermissions) {
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conceder Permisos de Bluetooth")
                }
            } else if (isHardwareAvailable && !isBluetoothEnabled) {
                Button(
                    onClick = onEnableBluetooth,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activar Bluetooth")
                }
            }
        }
    }
}

@Composable
fun SelectedPrinterCard(
    config: BluetoothPrinterConfig,
    paperSize: TicketPaperSize,
    isBluetoothReady: Boolean,
    onTestPrintClick: () -> Unit,
    onAutoPrintChange: (Boolean) -> Unit,
    onDeselectPrinter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasSelectedPrinter = !config.macAddress.isNullOrBlank()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Impresora Predeterminada Seleccionada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hasSelectedPrinter) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = if (hasSelectedPrinter) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize(),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasSelectedPrinter) {
                            config.deviceName ?: "Impresora Bluetooth"
                        } else {
                            "Ninguna impresora seleccionada"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (hasSelectedPrinter) {
                            "MAC: ${config.macAddress} • ${paperSize.label}"
                        } else {
                            "Seleccione un dispositivo abajo"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Impresión automática al vender",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Imprime el ticket automáticamente al completar la venta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = config.autoPrintSales,
                    onCheckedChange = onAutoPrintChange,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onTestPrintClick,
                    enabled = hasSelectedPrinter && isBluetoothReady,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Probar Impresión")
                }

                if (hasSelectedPrinter) {
                    OutlinedButton(
                        onClick = onDeselectPrinter,
                    ) {
                        Text("Quitar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothDevicesSection(
    pairedDevices: List<BluetoothDevice>,
    discoveredDevices: List<BluetoothDevice>,
    isScanning: Boolean,
    selectedMacAddress: String?,
    hasPermissions: Boolean,
    isBluetoothEnabled: Boolean,
    onStartDiscovery: () -> Unit,
    onSelectDevice: (BluetoothDevice) -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allDevices = remember(pairedDevices, discoveredDevices) {
        (pairedDevices + discoveredDevices).distinctBy { it.address }
    }

    val printerDevices = remember(allDevices) {
        allDevices.filter { isBluetoothPrinter(it) }
    }

    val displayDevices = remember(printerDevices, pairedDevices) {
        if (printerDevices.isNotEmpty()) {
            printerDevices
        } else {
            pairedDevices
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dispositivos Bluetooth Disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Filtrado inteligente de impresoras térmicas y POS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onOpenBluetoothSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes de Bluetooth",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Button(
            onClick = onStartDiscovery,
            enabled = hasPermissions && isBluetoothEnabled && !isScanning,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscando nuevos dispositivos...")
            } else {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar nuevos dispositivos")
            }
        }

        if (!hasPermissions || !isBluetoothEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = "Active el Bluetooth y conceda los permisos necesarios para ver y buscar dispositivos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else if (displayDevices.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = if (isScanning) "Buscando dispositivos cercanos..." else "No se encontraron impresoras Bluetooth.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Pulse 'Buscar nuevos dispositivos' o vincule su impresora en la configuración de Bluetooth de Android.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(
                        onClick = onOpenBluetoothSettings,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir Ajustes de Bluetooth del Sistema")
                    }
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                displayDevices.forEach { device ->
                    @SuppressLint("MissingPermission")
                    val devName = device.name ?: "Dispositivo sin nombre"
                    val devMac = device.address
                    val isSelected = devMac == selectedMacAddress
                    val isBonded = pairedDevices.any { it.address == devMac }

                    BluetoothDeviceItem(
                        deviceName = devName,
                        macAddress = devMac,
                        isBonded = isBonded,
                        isSelected = isSelected,
                        onSelect = { onSelectDevice(device) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothDeviceItem(
    deviceName: String,
    macAddress: String,
    isBonded: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, false),
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isBonded) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Text(
                            text = if (isBonded) "Vinculado" else "Nuevo",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBonded) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    text = macAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FilterChip(
                selected = isSelected,
                onClick = onSelect,
                label = {
                    Text(if (isSelected) "Seleccionada" else "Seleccionar")
                },
                leadingIcon = if (isSelected) {
                    { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
            )
        }
    }
}

private fun checkBluetoothPermissions(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val connectPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT,
        ) == PackageManager.PERMISSION_GRANTED

        val scanPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN,
        ) == PackageManager.PERMISSION_GRANTED

        connectPermission && scanPermission
    } else {
        true
    }
}

@SuppressLint("MissingPermission")
private fun getPairedBluetoothDevices(
    context: Context,
    bluetoothAdapter: BluetoothAdapter?,
): List<BluetoothDevice> {
    if ((bluetoothAdapter == null) || !bluetoothAdapter.isEnabled) return emptyList()
    if (!checkBluetoothPermissions(context)) return emptyList()

    return try {
        bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
    } catch (_: SecurityException) {
        emptyList()
    }
}

@SuppressLint("MissingPermission")
private fun isBluetoothPrinter(device: BluetoothDevice): Boolean {
    val majorClass = try {
        device.bluetoothClass?.majorDeviceClass
    } catch (_: SecurityException) {
        null
    }
    val name = try {
        device.name?.lowercase() ?: ""
    } catch (_: SecurityException) {
        ""
    }
    val keywords = listOf(
        "printer", "pos", "thermal", "mpt", "print",
        "epson", "bixolon", "rongta", "xprinter", "zjiang", "hoin"
    )

    val isImaging = majorClass == BluetoothClass.Device.Major.IMAGING
    val hasKeyword = keywords.any { name.contains(it) }
    return isImaging || hasKeyword
}

@Preview(showBackground = true)
@Composable
fun SettingsBluetoothPrinterScreenPreview() {
    MovilPOSTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BluetoothStatusCard(
                    bluetoothAdapter = null,
                    hasPermissions = true,
                    isBluetoothEnabled = true,
                    onRequestPermissions = {},
                    onEnableBluetooth = {},
                )

                SelectedPrinterCard(
                    config = BluetoothPrinterConfig(
                        macAddress = "00:11:22:33:44:55",
                        deviceName = "Impresora Térmica POS58",
                        isConnected = true,
                        autoPrintSales = true,
                    ),
                    paperSize = TicketPaperSize.SIZE_80MM,
                    isBluetoothReady = true,
                    onTestPrintClick = {},
                    onAutoPrintChange = {},
                    onDeselectPrinter = {},
                )

                BluetoothDeviceItem(
                    deviceName = "Impresora Térmica POS58",
                    macAddress = "00:11:22:33:44:55",
                    isBonded = true,
                    isSelected = true,
                    onSelect = {},
                )

                BluetoothDeviceItem(
                    deviceName = "BT Printer 80mm",
                    macAddress = "AA:BB:CC:DD:EE:FF",
                    isBonded = false,
                    isSelected = false,
                    onSelect = {},
                )
            }
        }
    }
}
