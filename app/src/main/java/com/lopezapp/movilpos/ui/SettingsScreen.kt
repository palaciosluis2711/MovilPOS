package com.lopezapp.movilpos.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.DteEnvironment
import com.lopezapp.movilpos.util.ElSalvadorCommercialActivities
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import com.lopezapp.movilpos.ui.model.AnimationType
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.NitVisualTransformation
import com.lopezapp.movilpos.util.PhoneVisualTransformation
import com.lopezapp.movilpos.util.formatCurrency
import com.lopezapp.movilpos.util.formatNit
import com.lopezapp.movilpos.util.formatPhone
import com.lopezapp.movilpos.util.sanitizeDecimalTextFieldValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.TicketPaperSize

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") viewModel: SettingsViewModel? = null,
    onNavigateToAnimation: () -> Unit = {},
    onNavigateToCurrency: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToBrands: () -> Unit = {},
    onNavigateToUnits: () -> Unit = {},
    onNavigateToTaxes: () -> Unit = {},
    onNavigateToPriceRules: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToBusinessInfo: () -> Unit = {},
    onNavigateToTicket: () -> Unit = {},
    onNavigateToElectronicBilling: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    SettingsHomeScreen(
        onNavigateToAnimation = onNavigateToAnimation,
        onNavigateToCurrency = onNavigateToCurrency,
        onNavigateToCategories = onNavigateToCategories,
        onNavigateToBrands = onNavigateToBrands,
        onNavigateToUnits = onNavigateToUnits,
        onNavigateToTaxes = onNavigateToTaxes,
        onNavigateToPriceRules = onNavigateToPriceRules,
        onNavigateToPaymentMethods = onNavigateToPaymentMethods,
        onNavigateToBusinessInfo = onNavigateToBusinessInfo,
        onNavigateToTicket = onNavigateToTicket,
        onNavigateToElectronicBilling = onNavigateToElectronicBilling,
        onNavigateToUsers = onNavigateToUsers,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    onNavigateToAnimation: () -> Unit,
    onNavigateToCurrency: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBrands: () -> Unit,
    onNavigateToUnits: () -> Unit,
    onNavigateToTaxes: () -> Unit,
    onNavigateToPriceRules: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToBusinessInfo: () -> Unit,
    onNavigateToTicket: () -> Unit = {},
    onNavigateToElectronicBilling: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingEntryCard(
                title = "Información del Negocio",
                subtitle = "Nombre del negocio, NIT, NRC, dirección, teléfono, logo y redes sociales",
                icon = Icons.Rounded.Storefront,
                onClick = onNavigateToBusinessInfo,
            )

            SettingEntryCard(
                title = "Configuración de Ticket",
                subtitle = "Encabezado, mensaje de pie de página, tamaño 57mm/80mm y previsualización",
                icon = Icons.Rounded.Receipt,
                onClick = onNavigateToTicket,
            )

            SettingEntryCard(
                title = "Usuarios y Permisos",
                subtitle = "Administrar usuarios, roles de acceso y PINs de seguridad",
                icon = Icons.Rounded.Group,
                onClick = onNavigateToUsers,
            )

            SettingEntryCard(
                title = "Animaciones y Transiciones",
                subtitle = "Personaliza la velocidad y tipo de transición entre pantallas",
                icon = Icons.Rounded.Tune,
                onClick = onNavigateToAnimation,
            )

            SettingEntryCard(
                title = "Formato y Moneda",
                subtitle = "Símbolo de moneda, decimales y redondeo",
                icon = Icons.Rounded.AttachMoney,
                onClick = onNavigateToCurrency,
            )

            SettingEntryCard(
                title = "Categorías",
                subtitle = "Crear, editar y eliminar categorías de productos",
                icon = Icons.Rounded.Category,
                onClick = onNavigateToCategories,
            )

            SettingEntryCard(
                title = "Marcas",
                subtitle = "Administrar marcas y logos de productos",
                icon = Icons.Rounded.Store,
                onClick = onNavigateToBrands,
            )

            SettingEntryCard(
                title = "Unidades de Medida",
                subtitle = "Administrar unidades de medida y paquetes/cajas",
                icon = Icons.Rounded.Straighten,
                onClick = onNavigateToUnits,
            )

            SettingEntryCard(
                title = "Impuestos",
                subtitle = "Administrar impuestos aplicables a productos",
                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                onClick = onNavigateToTaxes,
            )

            SettingEntryCard(
                title = "Reglas de Precio",
                subtitle = "Construir fórmulas dinámicas de precio por categorías, clientes o tipo de producto",
                icon = Icons.Rounded.Calculate,
                onClick = onNavigateToPriceRules,
            )

            SettingEntryCard(
                title = "Métodos de Pago",
                subtitle = "Configurar los métodos de pago aceptados",
                icon = Icons.Rounded.Payments,
                onClick = onNavigateToPaymentMethods,
            )

            SettingEntryCard(
                title = "Facturación Electrónica",
                subtitle = "Configuración DTE Ministerio de Hacienda de El Salvador",
                icon = Icons.Rounded.CloudSync,
                onClick = onNavigateToElectronicBilling,
            )
        }
    }
}

@Composable
fun SettingEntryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ExpandableSectionContainer(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        ),
        exit = shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        ),
        modifier = modifier,
        content = content
    )
}

@Composable
fun ExpandableSettingCard(
    title: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!isExpanded) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(onClick = { onExpandedChange(!isExpanded) }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAnimationScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animaciones y Transiciones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Duración de la animación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${uiState.animationDurationMs} ms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                Slider(
                    value = uiState.animationDurationMs.toFloat(),
                    onValueChange = { viewModel.updateAnimationDuration(it.toInt()) },
                    valueRange = 100f..1000f,
                    steps = 17,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val presetDurations = listOf(200, 400, 600, 800)
                    presetDurations.forEach { duration ->
                        FilterChip(
                            selected = uiState.animationDurationMs == duration,
                            onClick = { viewModel.updateAnimationDuration(duration) },
                            label = { Text("${duration}ms") },
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Animation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tipo de animación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                AnimationType.entries.forEach { type ->
                    val isSelected = uiState.animationType == type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { viewModel.updateAnimationType(type) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCurrencyScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Formato y Moneda") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Símbolo de Moneda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = uiState.currencySymbol,
                    onValueChange = { viewModel.updateCurrencySymbol(it) },
                    label = { Text("Símbolo de moneda") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("$", "€", "MXN$", "USD$").forEach { symbol ->
                        FilterChip(
                            selected = uiState.currencySymbol == symbol,
                            onClick = { viewModel.updateCurrencySymbol(symbol) },
                            label = { Text(symbol) },
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Decimales predeterminados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(2, 3, 4).forEach { decimals ->
                        FilterChip(
                            selected = uiState.defaultDecimalPlaces == decimals,
                            onClick = { viewModel.updateDefaultDecimalPlaces(decimals) },
                            label = { Text("$decimals decimales") },
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.updateAllowExtraDecimals(!uiState.allowExtraDecimals) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Permitir decimales adicionales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Mostrar más decimales cuando el usuario los especifique de forma explícita",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.allowExtraDecimals,
                    onCheckedChange = { viewModel.updateAllowExtraDecimals(it) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCategoriesScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCategoryDialog by rememberSaveable { mutableStateOf(value = false) }
    var categoryToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryName by rememberSaveable { mutableStateOf("") }
    var categoryDescription by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(value = false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Gestión de Categorías",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(
                    onClick = {
                        categoryToEditId = null
                        categoryName = ""
                        categoryDescription = ""
                        nameError = false
                        showCategoryDialog = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadir categoría")
                }
            }

            if (uiState.categories.isEmpty()) {
                Text(
                    text = "No hay categorías disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                uiState.categories.forEachIndexed { index, category ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (!category.description.isNullOrBlank()) {
                                Text(
                                    text = category.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                categoryToEditId = category.id
                                categoryName = category.name
                                categoryDescription = category.description ?: ""
                                nameError = false
                                showCategoryDialog = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar categoría ${category.name}",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(
                            onClick = {
                                categoryToDelete = category
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar categoría ${category.name}",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                title = {
                    Text(if (categoryToEditId == null) "Añadir categoría" else "Editar categoría")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = categoryName,
                            onValueChange = {
                                categoryName = it
                                if (it.isNotBlank()) nameError = false
                            },
                            label = { Text("Nombre") },
                            isError = nameError,
                            supportingText = if (nameError) {
                                { Text("El nombre es obligatorio") }
                            } else null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = categoryDescription,
                            onValueChange = { categoryDescription = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (categoryName.isBlank()) {
                                nameError = true
                            } else {
                                val editId = categoryToEditId
                                if (editId == null) {
                                    viewModel.addCategory(categoryName, categoryDescription)
                                } else {
                                    viewModel.updateCategory(editId, categoryName, categoryDescription)
                                }
                                showCategoryDialog = false
                            }
                        },
                    ) {
                        Text(if (categoryToEditId == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCategoryDialog = false },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }

        if (categoryToDelete != null) {
            val cat = categoryToDelete!!
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = { Text("Eliminar categoría") },
                text = { Text("¿Deseas eliminar la categoría \"${cat.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(cat.id)
                            categoryToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { categoryToDelete = null },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBrandsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showBrandDialog by rememberSaveable { mutableStateOf(value = false) }
    var brandToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var brandName by rememberSaveable { mutableStateOf("") }
    var brandDescription by rememberSaveable { mutableStateOf("") }
    var brandLogoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var brandNameError by rememberSaveable { mutableStateOf(value = false) }
    var brandToDelete by remember { mutableStateOf<Brand?>(null) }

    val brandLogoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { brandLogoUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marcas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Gestión de Marcas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(
                    onClick = {
                        brandToEditId = null
                        brandName = ""
                        brandDescription = ""
                        brandLogoUri = null
                        brandNameError = false
                        showBrandDialog = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadir marca")
                }
            }

            if (uiState.brands.isEmpty()) {
                Text(
                    text = "No hay marcas disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                uiState.brands.forEachIndexed { index, brand ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!brand.logoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = brand.logoUri,
                                contentDescription = "Logo de ${brand.name}",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = brand.name.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = brand.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (!brand.description.isNullOrBlank()) {
                                Text(
                                    text = brand.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                brandToEditId = brand.id
                                brandName = brand.name
                                brandDescription = brand.description ?: ""
                                brandLogoUri = brand.logoUri
                                brandNameError = false
                                showBrandDialog = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar marca ${brand.name}",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(
                            onClick = {
                                brandToDelete = brand
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar marca ${brand.name}",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        if (showBrandDialog) {
            AlertDialog(
                onDismissRequest = { showBrandDialog = false },
                title = {
                    Text(if (brandToEditId == null) "Añadir marca" else "Editar marca")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (!brandLogoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = brandLogoUri,
                                    contentDescription = "Logo de la marca",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = { brandLogoPickerLauncher.launch("image/*") },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cambiar logo")
                                    }
                                    TextButton(
                                        onClick = { brandLogoUri = null },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error,
                                        ),
                                    ) {
                                        Text("Quitar logo")
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { brandLogoPickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Añadir logo",
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Añadir logo (opcional)")
                                }
                            }
                        }

                        OutlinedTextField(
                            value = brandName,
                            onValueChange = {
                                brandName = it
                                if (it.isNotBlank()) brandNameError = false
                            },
                            label = { Text("Nombre") },
                            isError = brandNameError,
                            supportingText = if (brandNameError) {
                                { Text("El nombre es obligatorio") }
                            } else null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = brandDescription,
                            onValueChange = { brandDescription = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (brandName.isBlank()) {
                                brandNameError = true
                            } else {
                                val editId = brandToEditId
                                if (editId == null) {
                                    viewModel.addBrand(brandName, brandDescription, brandLogoUri)
                                } else {
                                    viewModel.updateBrand(editId, brandName, brandDescription, brandLogoUri)
                                }
                                showBrandDialog = false
                            }
                        },
                    ) {
                        Text(if (brandToEditId == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBrandDialog = false },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }

        if (brandToDelete != null) {
            val brand = brandToDelete!!
            AlertDialog(
                onDismissRequest = { brandToDelete = null },
                title = { Text("Eliminar marca") },
                text = { Text("¿Deseas eliminar la marca \"${brand.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteBrand(brand.id)
                            brandToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { brandToDelete = null },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUnitsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showUnitDialog by rememberSaveable { mutableStateOf(value = false) }
    var unitToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var unitName by rememberSaveable { mutableStateOf("") }
    var unitAbbreviation by rememberSaveable { mutableStateOf("") }
    var unitIsPackageOrBox by rememberSaveable { mutableStateOf(value = false) }
    var unitNameError by rememberSaveable { mutableStateOf(value = false) }
    var unitToDelete by remember { mutableStateOf<UnitOfMeasure?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unidades de Medida") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Gestión de Unidades de Medida",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(
                    onClick = {
                        unitToEditId = null
                        unitName = ""
                        unitAbbreviation = ""
                        unitIsPackageOrBox = false
                        unitNameError = false
                        showUnitDialog = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadir")
                }
            }

            if (uiState.unitsOfMeasure.isEmpty()) {
                Text(
                    text = "No hay unidades de medida disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                uiState.unitsOfMeasure.forEachIndexed { index, unit ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                val displayName = if (!unit.abbreviation.isNullOrBlank()) {
                                    "${unit.name} (${unit.abbreviation})"
                                } else {
                                    unit.name
                                }
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )

                                if (unit.isPackageOrBox) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    ) {
                                        Text(
                                            text = "Paquete / Caja",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        )
                                    }
                                }
                            }
                        }
                        IconButton(
                            onClick = {
                                unitToEditId = unit.id
                                unitName = unit.name
                                unitAbbreviation = unit.abbreviation ?: ""
                                unitIsPackageOrBox = unit.isPackageOrBox
                                unitNameError = false
                                showUnitDialog = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar unidad ${unit.name}",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(
                            onClick = {
                                unitToDelete = unit
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar unidad ${unit.name}",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        if (showUnitDialog) {
            AlertDialog(
                onDismissRequest = { showUnitDialog = false },
                title = {
                    Text(if (unitToEditId == null) "Añadir unidad de medida" else "Editar unidad de medida")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = unitName,
                            onValueChange = {
                                unitName = it
                                if (it.isNotBlank()) unitNameError = false
                            },
                            label = { Text("Nombre") },
                            isError = unitNameError,
                            supportingText = if (unitNameError) {
                                { Text("El nombre es obligatorio") }
                            } else null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = unitAbbreviation,
                            onValueChange = { unitAbbreviation = it },
                            label = { Text("Abreviatura (opcional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { unitIsPackageOrBox = !unitIsPackageOrBox }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = unitIsPackageOrBox,
                                onCheckedChange = { unitIsPackageOrBox = it },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¿Es un paquete o caja?",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (unitName.isBlank()) {
                                unitNameError = true
                            } else {
                                val editId = unitToEditId
                                if (editId == null) {
                                    viewModel.addUnitOfMeasure(unitName, unitAbbreviation, unitIsPackageOrBox)
                                } else {
                                    viewModel.updateUnitOfMeasure(editId, unitName, unitAbbreviation, unitIsPackageOrBox)
                                }
                                showUnitDialog = false
                            }
                        },
                    ) {
                        Text(if (unitToEditId == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showUnitDialog = false },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }

        if (unitToDelete != null) {
            val unit = unitToDelete!!
            AlertDialog(
                onDismissRequest = { unitToDelete = null },
                title = { Text("Eliminar unidad de medida") },
                text = { Text("¿Deseas eliminar la unidad de medida \"${unit.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteUnitOfMeasure(unit.id)
                            unitToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { unitToDelete = null },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTaxesScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showTaxDialog by rememberSaveable { mutableStateOf(value = false) }
    var taxToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var taxName by rememberSaveable { mutableStateOf("") }
    var taxDescription by rememberSaveable { mutableStateOf("") }
    var taxValueType by rememberSaveable { mutableStateOf(TaxValueType.PERCENTAGE) }
    var taxValueState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var taxNameError by rememberSaveable { mutableStateOf(value = false) }
    var taxValueError by rememberSaveable { mutableStateOf(value = false) }
    var taxTypeExpanded by remember { mutableStateOf(value = false) }
    var taxToDelete by remember { mutableStateOf<Tax?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impuestos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Gestión de Impuestos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(
                    onClick = {
                        taxToEditId = null
                        taxName = ""
                        taxDescription = ""
                        taxValueType = TaxValueType.PERCENTAGE
                        taxValueState = TextFieldValue("")
                        taxNameError = false
                        taxValueError = false
                        showTaxDialog = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadir")
                }
            }

            if (uiState.taxes.isEmpty()) {
                Text(
                    text = "No hay impuestos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                uiState.taxes.forEachIndexed { index, tax ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            val formattedValue = if (tax.valueType == TaxValueType.PERCENTAGE) {
                                "${if ((tax.value % 1.0) == 0.0) tax.value.toInt().toString() else tax.value.toString()}%"
                            } else {
                                formatCurrency(
                                    tax.value,
                                    uiState.currencySymbol,
                                    uiState.defaultDecimalPlaces,
                                    uiState.allowExtraDecimals,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = tax.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ) {
                                    Text(
                                        text = formattedValue,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }
                            if (!tax.description.isNullOrBlank()) {
                                Text(
                                    text = tax.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                taxToEditId = tax.id
                                taxName = tax.name
                                taxDescription = tax.description ?: ""
                                taxValueType = tax.valueType
                                val initialTaxVal = if ((tax.value % 1.0) == 0.0) tax.value.toInt().toString() else tax.value.toString()
                                taxValueState = TextFieldValue(initialTaxVal, selection = TextRange(initialTaxVal.length))
                                taxNameError = false
                                taxValueError = false
                                showTaxDialog = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar impuesto ${tax.name}",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(
                            onClick = {
                                taxToDelete = tax
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar impuesto ${tax.name}",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        if (showTaxDialog) {
            AlertDialog(
                onDismissRequest = { showTaxDialog = false },
                title = {
                    Text(if (taxToEditId == null) "Añadir impuesto" else "Editar impuesto")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = taxName,
                            onValueChange = {
                                taxName = it
                                if (it.isNotBlank()) taxNameError = false
                            },
                            label = { Text("Nombre") },
                            isError = taxNameError,
                            supportingText = if (taxNameError) {
                                { Text("El nombre es obligatorio") }
                            } else null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value = taxDescription,
                            onValueChange = { taxDescription = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        ExposedDropdownMenuBox(
                            expanded = taxTypeExpanded,
                            onExpandedChange = { taxTypeExpanded = it },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = when (taxValueType) {
                                    TaxValueType.PERCENTAGE -> "Porcentaje %"
                                    TaxValueType.FIXED_AMOUNT -> "Monto Fijo $"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de valor") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taxTypeExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                            )
                            ExposedDropdownMenu(
                                expanded = taxTypeExpanded,
                                onDismissRequest = { taxTypeExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Porcentaje %") },
                                    onClick = {
                                        taxValueType = TaxValueType.PERCENTAGE
                                        taxTypeExpanded = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Monto Fijo $") },
                                    onClick = {
                                        taxValueType = TaxValueType.FIXED_AMOUNT
                                        taxTypeExpanded = false
                                    },
                                )
                            }
                        }

                        OutlinedTextField(
                            value = taxValueState,
                            onValueChange = { newValue ->
                                val updated = sanitizeDecimalTextFieldValue(newValue, taxValueState)
                                taxValueState = updated
                                if (updated.text.toDoubleOrNull() != null) taxValueError = false
                            },
                            label = { Text("Valor") },
                            isError = taxValueError,
                            supportingText = if (taxValueError) {
                                { Text("Ingrese un valor numérico válido") }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsedValue = taxValueState.text.toDoubleOrNull()
                            val nameValid = taxName.isNotBlank()
                            val valueValid = (parsedValue != null) && (parsedValue >= 0.0)

                            taxNameError = !nameValid
                            taxValueError = !valueValid

                            if (nameValid && valueValid) {
                                val editId = taxToEditId
                                if (editId == null) {
                                    viewModel.addTax(taxName, taxDescription, taxValueType, parsedValue)
                                } else {
                                    viewModel.updateTax(editId, taxName, taxDescription, taxValueType, parsedValue)
                                }
                                showTaxDialog = false
                            }
                        },
                    ) {
                        Text(if (taxToEditId == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTaxDialog = false },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }

        if (taxToDelete != null) {
            val tax = taxToDelete!!
            AlertDialog(
                onDismissRequest = { taxToDelete = null },
                title = { Text("Eliminar impuesto") },
                text = { Text("¿Deseas eliminar el impuesto \"${tax.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTax(tax.id)
                            taxToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { taxToDelete = null },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPaymentMethodsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showMethodDialog by rememberSaveable { mutableStateOf(false) }
    var methodToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var methodName by rememberSaveable { mutableStateOf("") }
    var methodNameError by rememberSaveable { mutableStateOf(false) }
    var methodToDelete by remember { mutableStateOf<PaymentMethod?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métodos de Pago") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Gestión de Métodos de Pago",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(
                    onClick = {
                        methodToEditId = null
                        methodName = ""
                        methodNameError = false
                        showMethodDialog = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nuevo")
                }
            }

            if (uiState.paymentMethods.isEmpty()) {
                Text(
                    text = "No hay métodos de pago disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                uiState.paymentMethods.forEachIndexed { index, method ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = method.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        IconButton(
                            onClick = {
                                methodToEditId = method.id
                                methodName = method.name
                                methodNameError = false
                                showMethodDialog = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar método de pago ${method.name}",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(
                            onClick = {
                                methodToDelete = method
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar método de pago ${method.name}",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        if (showMethodDialog) {
            AlertDialog(
                onDismissRequest = { showMethodDialog = false },
                title = {
                    Text(if (methodToEditId == null) "Añadir Método de Pago" else "Editar Método de Pago")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = methodName,
                            onValueChange = {
                                methodName = it
                                if (it.isNotBlank()) methodNameError = false
                            },
                            label = { Text("Nombre del Método de Pago") },
                            isError = methodNameError,
                            supportingText = if (methodNameError) {
                                { Text("El nombre es obligatorio") }
                            } else null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (methodName.isBlank()) {
                                methodNameError = true
                            } else {
                                val editId = methodToEditId
                                if (editId == null) {
                                    viewModel.addPaymentMethod(methodName)
                                } else {
                                    viewModel.updatePaymentMethod(editId, methodName)
                                }
                                showMethodDialog = false
                            }
                        },
                    ) {
                        Text(if (methodToEditId == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showMethodDialog = false },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }

        if (methodToDelete != null) {
            val method = methodToDelete!!
            AlertDialog(
                onDismissRequest = { methodToDelete = null },
                title = { Text("Eliminar método de pago") },
                text = { Text("¿Deseas eliminar el método de pago \"${method.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePaymentMethod(method.id)
                            methodToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { methodToDelete = null },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBusinessInfoScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf(uiState.businessInfo.name) }
    var nit by remember(uiState.businessInfo) {
        mutableStateOf(uiState.businessInfo.nit.filter { it.isDigit() }.take(14))
    }
    var nrc by rememberSaveable { mutableStateOf(uiState.businessInfo.nrc) }
    var address by rememberSaveable { mutableStateOf(uiState.businessInfo.address) }
    var phone by rememberSaveable { mutableStateOf(uiState.businessInfo.phone) }
    var email by rememberSaveable { mutableStateOf(uiState.businessInfo.email) }
    var socialMedia by rememberSaveable { mutableStateOf(uiState.businessInfo.socialMedia) }
    var commercialName by rememberSaveable { mutableStateOf(uiState.businessInfo.commercialName) }
    var logoUri by rememberSaveable { mutableStateOf(uiState.businessInfo.logoUri) }
    var nameError by rememberSaveable { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { logoUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información del Negocio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Logo del Negocio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (!logoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Logo del Negocio",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { logoPickerLauncher.launch("image/*") },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cambiar logo")
                            }
                            TextButton(
                                onClick = { logoUri = null },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Text("Quitar logo")
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Storefront,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { logoPickerLauncher.launch("image/*") },
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Seleccionar logo")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Nombre del Negocio *") },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("El nombre del negocio es obligatorio") }
                } else null,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = commercialName,
                onValueChange = { commercialName = it },
                label = { Text("Nombre Comercial") },
                placeholder = { Text("Ej. Mi Negocio Comercial") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = nit,
                onValueChange = { input ->
                    nit = input.filter { it.isDigit() }.take(14)
                },
                visualTransformation = NitVisualTransformation(),
                label = { Text("NIT") },
                placeholder = { Text("0000-000000-000-0") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Badge, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = nrc,
                onValueChange = { nrc = it },
                label = { Text("NRC") },
                placeholder = { Text("Ej. 123456-7") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Business, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección de la Tienda") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    phone = input.filter { it.isDigit() }.take(8)
                },
                visualTransformation = PhoneVisualTransformation(),
                label = { Text("Teléfono") },
                placeholder = { Text("0000-0000") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = socialMedia,
                onValueChange = { socialMedia = it },
                label = { Text("Redes Sociales") },
                placeholder = { Text("@minegocio, fb.com/minegocio") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        val updatedInfo = BusinessInfo(
                            name = name.trim(),
                            nit = formatNit(nit),
                            nrc = nrc.trim(),
                            address = address.trim(),
                            phone = phone,
                            email = email.trim(),
                            socialMedia = socialMedia.trim(),
                            commercialName = commercialName.trim(),
                            logoUri = logoUri,
                        )
                        viewModel.updateBusinessInfo(updatedInfo)
                        Toast.makeText(
                            context,
                            "Información del negocio actualizada exitosamente",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text(
                    text = "Guardar Cambios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTicketScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showBusinessName by rememberSaveable { mutableStateOf(uiState.ticketConfig.showBusinessName) }
    var showNit by rememberSaveable { mutableStateOf(uiState.ticketConfig.showNit) }
    var showNrc by rememberSaveable { mutableStateOf(uiState.ticketConfig.showNrc) }
    var showAddress by rememberSaveable { mutableStateOf(uiState.ticketConfig.showAddress) }
    var showPhone by rememberSaveable { mutableStateOf(uiState.ticketConfig.showPhone) }
    var showSocialMedia by rememberSaveable { mutableStateOf(uiState.ticketConfig.showSocialMedia) }
    var showLogo by rememberSaveable { mutableStateOf(uiState.ticketConfig.showLogo) }
    var footerMessage by rememberSaveable { mutableStateOf(uiState.ticketConfig.footerMessage) }
    var paperSize by rememberSaveable { mutableStateOf(uiState.ticketConfig.paperSize) }

    LaunchedEffect(uiState.ticketConfig) {
        showBusinessName = uiState.ticketConfig.showBusinessName
        showNit = uiState.ticketConfig.showNit
        showNrc = uiState.ticketConfig.showNrc
        showAddress = uiState.ticketConfig.showAddress
        showPhone = uiState.ticketConfig.showPhone
        showSocialMedia = uiState.ticketConfig.showSocialMedia
        showLogo = uiState.ticketConfig.showLogo
        footerMessage = uiState.ticketConfig.footerMessage
        paperSize = uiState.ticketConfig.paperSize
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Ticket") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Encabezado del Ticket",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Selecciona la información del negocio que se mostrará en el ticket",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TicketSwitchRow(
                        title = "Nombre del Negocio",
                        checked = showBusinessName,
                        onCheckedChange = { showBusinessName = it },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    TicketSwitchRow(
                        title = "NIT",
                        checked = showNit,
                        onCheckedChange = { showNit = it },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    TicketSwitchRow(
                        title = "NRC",
                        checked = showNrc,
                        onCheckedChange = { showNrc = it },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    TicketSwitchRow(
                        title = "Dirección",
                        checked = showAddress,
                        onCheckedChange = { showAddress = it },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    TicketSwitchRow(
                        title = "Teléfono",
                        checked = showPhone,
                        onCheckedChange = { showPhone = it },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    TicketSwitchRow(
                        title = "Redes Sociales",
                        checked = showSocialMedia,
                        onCheckedChange = { showSocialMedia = it },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    TicketSwitchRow(
                        title = "Logo",
                        checked = showLogo,
                        onCheckedChange = { showLogo = it },
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Mensaje de Pie de Página",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = footerMessage,
                        onValueChange = { footerMessage = it },
                        label = { Text("Mensaje final del ticket") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Tamaño de Papel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TicketPaperSize.entries.forEach { size ->
                            FilterChip(
                                selected = paperSize == size,
                                onClick = { paperSize = size },
                                label = { Text(size.label) },
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Previsualización del Ticket",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start),
                    )

                    val previewWidth = if (paperSize == TicketPaperSize.SIZE_57MM) 240.dp else 300.dp
                    Surface(
                        modifier = Modifier
                            .width(previewWidth)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFAFAFA),
                        shadowElevation = 3.dp,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            val businessInfo = uiState.businessInfo

                            if (showLogo) {
                                if (!businessInfo.logoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = businessInfo.logoUri,
                                        contentDescription = "Logo Ticket",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Storefront,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = Color.DarkGray,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }

                            if (showBusinessName && businessInfo.name.isNotBlank()) {
                                Text(
                                    text = businessInfo.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                )
                            }

                            if (showNit && businessInfo.nit.isNotBlank()) {
                                Text(
                                    text = "NIT: ${formatNit(businessInfo.nit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray,
                                )
                            }

                            if (showNrc && businessInfo.nrc.isNotBlank()) {
                                Text(
                                    text = "NRC: ${businessInfo.nrc}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray,
                                )
                            }

                            if (showAddress && businessInfo.address.isNotBlank()) {
                                Text(
                                    text = businessInfo.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                )
                            }

                            if (showPhone && businessInfo.phone.isNotBlank()) {
                                Text(
                                    text = "Tel: ${formatPhone(businessInfo.phone)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray,
                                )
                            }

                            if (showSocialMedia && businessInfo.socialMedia.isNotBlank()) {
                                Text(
                                    text = businessInfo.socialMedia,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray,
                                )
                            }

                            Text(
                                text = "- - - - - - - - - - - - - - - - - - - - - - -",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                            )

                            Text(
                                text = "TICKET #0001",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                            )
                            Text(
                                text = "Fecha: 24/10/2023 10:30 AM",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.DarkGray,
                            )

                            Text(
                                text = "- - - - - - - - - - - - - - - - - - - - - - -",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("2x Café Express", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                Text("$5.00", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("1x Sándwich", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                Text("$4.00", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                            }

                            Text(
                                text = "- - - - - - - - - - - - - - - - - - - - - - -",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("SUBTOTAL:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("$9.00", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("TOTAL:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("$9.00", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            Text(
                                text = "- - - - - - - - - - - - - - - - - - - - - - -",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                            )

                            if (footerMessage.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = footerMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val updatedConfig = TicketConfig(
                        showBusinessName = showBusinessName,
                        showNit = showNit,
                        showNrc = showNrc,
                        showAddress = showAddress,
                        showPhone = showPhone,
                        showSocialMedia = showSocialMedia,
                        showLogo = showLogo,
                        footerMessage = footerMessage.trim(),
                        paperSize = paperSize,
                    )
                    viewModel.updateTicketConfig(updatedConfig)
                    Toast.makeText(
                        context,
                        "Configuración de ticket guardada exitosamente",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text(
                    text = "Guardar Cambios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun TicketSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsElectronicBillingScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val config = uiState.electronicBillingConfig

    var isEnabled by rememberSaveable { mutableStateOf(config.isEnabled) }
    var environment by rememberSaveable { mutableStateOf(config.environment) }
    var nit by rememberSaveable { mutableStateOf(config.nit.filter { it.isDigit() }.take(14)) }
    var apiToken by rememberSaveable { mutableStateOf(config.apiToken) }
    var establishmentCode by rememberSaveable { mutableStateOf(config.establishmentCode) }
    var posCode by rememberSaveable { mutableStateOf(config.posCode) }
    var economicActivity by rememberSaveable { mutableStateOf(config.economicActivity) }
    var certificatePassword by rememberSaveable { mutableStateOf(config.certificatePassword) }
    var certificateUri by rememberSaveable { mutableStateOf(config.certificateUri) }
    var certificateFileName by rememberSaveable { mutableStateOf(config.certificateFileName) }
    var isSimulationMode by rememberSaveable { mutableStateOf(config.isSimulationMode) }

    var apiTokenVisible by rememberSaveable { mutableStateOf(false) }
    var certPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val certificatePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            certificateUri = uri.toString()
            certificateFileName = getFileNameFromUri(context, uri)
        }
    }

    LaunchedEffect(config) {
        isEnabled = config.isEnabled
        environment = config.environment
        nit = config.nit.filter { it.isDigit() }.take(14)
        apiToken = config.apiToken
        establishmentCode = config.establishmentCode
        posCode = config.posCode
        economicActivity = config.economicActivity
        certificatePassword = config.certificatePassword
        certificateUri = config.certificateUri
        certificateFileName = config.certificateFileName
        isSimulationMode = config.isSimulationMode
    }

    val contingencyDtes by viewModel.contingencyDtes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Facturación Electrónica") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
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
            if (contingencyDtes.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "DTEs de Contingencia Pendientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "Existen ${contingencyDtes.size} factura(s) pendiente(s) de transmisión al Ministerio de Hacienda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFBF360C)
                        )
                        Button(
                            onClick = {
                                val result = viewModel.retryContingencyTransmissions()
                                val count = result.getOrDefault(0)
                                Toast.makeText(context, "Retransmitidas $count facturas de contingencia", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retransmitir DTEs de Contingencia")
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Habilitar Facturación Electrónica (DTE)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Emisión de Documentos Tributarios Electrónicos con el MH",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo Simulación DTE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Solo Desarrollo: Simula la emisión sin transmitir al MH",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isSimulationMode,
                        onCheckedChange = { isSimulationMode = it },
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Ambiente DTE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FilterChip(
                            selected = environment == DteEnvironment.SANDBOX,
                            onClick = { environment = DteEnvironment.SANDBOX },
                            label = { Text("Pruebas / Sandbox") },
                            modifier = Modifier.weight(1f),
                        )
                        FilterChip(
                            selected = environment == DteEnvironment.PRODUCTION,
                            onClick = { environment = DteEnvironment.PRODUCTION },
                            label = { Text("Producción") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            OutlinedTextField(
                value = nit,
                onValueChange = { input ->
                    nit = input.filter { it.isDigit() }.take(14)
                },
                visualTransformation = NitVisualTransformation(),
                label = { Text("NIT del Emisor") },
                placeholder = { Text("0614-000000-000-0") },
                leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = apiToken,
                onValueChange = { apiToken = it },
                label = { Text("Token / API Key MH") },
                placeholder = { Text("Ingrese el token de autenticación") },
                leadingIcon = { Icon(imageVector = Icons.Default.Key, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { apiTokenVisible = !apiTokenVisible }) {
                        Icon(
                            imageVector = if (apiTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (apiTokenVisible) "Ocultar token" else "Mostrar token",
                        )
                    }
                },
                visualTransformation = if (apiTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = establishmentCode,
                    onValueChange = { establishmentCode = it },
                    label = { Text("Código de Establecimiento") },
                    placeholder = { Text("0001") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = posCode,
                    onValueChange = { posCode = it },
                    label = { Text("Código de Punto de Venta") },
                    placeholder = { Text("0001") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            SettingsEditableSearchableDropdownField(
                value = economicActivity,
                onValueChange = { economicActivity = it },
                label = "Actividad Económica",
                options = ElSalvadorCommercialActivities.activities,
                placeholder = "Buscar actividad económica...",
                leadingIcon = Icons.Default.Work,
                modifier = Modifier.fillMaxWidth(),
            )

            // Certificado Digital (.p12 / .pfx)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Certificado Digital (.p12 / .pfx)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    if (certificateFileName != null && certificateUri != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = certificateFileName ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Row {
                                TextButton(onClick = { certificatePickerLauncher.launch("*/*") }) {
                                    Text("Cambiar")
                                }
                                IconButton(
                                    onClick = {
                                        certificateUri = null
                                        certificateFileName = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar certificado",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No se ha cargado ningún certificado .p12",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { certificatePickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Seleccionar Certificado (.p12 / .pfx)")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = certificatePassword,
                onValueChange = { certificatePassword = it },
                label = { Text("Contraseña de Certificado") },
                placeholder = { Text("Contraseña del certificado MH") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { certPasswordVisible = !certPasswordVisible }) {
                        Icon(
                            imageVector = if (certPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (certPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        )
                    }
                },
                visualTransformation = if (certPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val updatedConfig = ElectronicBillingConfig(
                        isEnabled = isEnabled,
                        environment = environment,
                        nit = formatNit(nit),
                        apiToken = apiToken.trim(),
                        establishmentCode = establishmentCode.trim().ifBlank { "0001" },
                        posCode = posCode.trim().ifBlank { "0001" },
                        economicActivity = economicActivity.trim(),
                        certificatePassword = certificatePassword,
                        certificateUri = certificateUri,
                        certificateFileName = certificateFileName,
                        isSimulationMode = isSimulationMode,
                    )
                    viewModel.updateElectronicBillingConfig(updatedConfig)
                    Toast.makeText(
                        context,
                        "Configuración de facturación electrónica guardada exitosamente",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text(
                    text = "Guardar Configuración",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = cursor.getString(index)
                }
            }
        }
    }
    if (name == null) {
        name = uri.path?.let { path ->
            val cut = path.lastIndexOf('/')
            if (cut != -1) path.substring(cut + 1) else path
        }
    }
    return name ?: "certificado.p12"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsEditableSearchableDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    placeholder: String = "",
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = remember(value, options) {
        if (value.isBlank()) {
            options
        } else {
            val query = value.trim().lowercase()
            options.filter { option ->
                option.lowercase().contains(query)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (filteredOptions.isNotEmpty()) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                    )
                }
            } else if (value.isNotBlank()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Sin sugerencias. Se usará el texto ingresado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsHomeScreenPreview() {
    MovilPOSTheme {
        SettingsHomeScreen(
            onNavigateToAnimation = {},
            onNavigateToCurrency = {},
            onNavigateToCategories = {},
            onNavigateToBrands = {},
            onNavigateToUnits = {},
            onNavigateToTaxes = {},
            onNavigateToPriceRules = {},
            onNavigateToPaymentMethods = {},
            onNavigateToBusinessInfo = {},
            onNavigateToTicket = {},
            onNavigateToElectronicBilling = {},
            onNavigateBack = {},
        )
    }
}
