package com.lopezapp.movilpos.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import com.lopezapp.movilpos.ui.model.AnimationType
import com.lopezapp.movilpos.ui.theme.MovilPOSTheme
import com.lopezapp.movilpos.ui.viewmodel.SettingsUiState
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreenContent(
        uiState = uiState,
        onDurationChange = viewModel::updateAnimationDuration,
        onAnimationTypeChange = viewModel::updateAnimationType,
        onCurrencySymbolChange = viewModel::updateCurrencySymbol,
        onDefaultDecimalPlacesChange = viewModel::updateDefaultDecimalPlaces,
        onAllowExtraDecimalsChange = viewModel::updateAllowExtraDecimals,
        onAddCategory = viewModel::addCategory,
        onUpdateCategory = viewModel::updateCategory,
        onDeleteCategory = viewModel::deleteCategory,
        onAddBrand = viewModel::addBrand,
        onUpdateBrand = viewModel::updateBrand,
        onDeleteBrand = viewModel::deleteBrand,
        onAddUnitOfMeasure = viewModel::addUnitOfMeasure,
        onUpdateUnitOfMeasure = viewModel::updateUnitOfMeasure,
        onDeleteUnitOfMeasure = viewModel::deleteUnitOfMeasure,
        onAddTax = viewModel::addTax,
        onUpdateTax = viewModel::updateTax,
        onDeleteTax = viewModel::deleteTax,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    uiState: SettingsUiState,
    onDurationChange: (Int) -> Unit,
    onAnimationTypeChange: (AnimationType) -> Unit,
    onCurrencySymbolChange: (String) -> Unit = {},
    onDefaultDecimalPlacesChange: (Int) -> Unit = {},
    onAllowExtraDecimalsChange: (Boolean) -> Unit = {},
    onAddCategory: (Category) -> Unit = {},
    onUpdateCategory: (Category) -> Unit = {},
    onDeleteCategory: (String) -> Unit = {},
    onAddBrand: (Brand) -> Unit = {},
    onUpdateBrand: (Brand) -> Unit = {},
    onDeleteBrand: (String) -> Unit = {},
    onAddUnitOfMeasure: (UnitOfMeasure) -> Unit = {},
    onUpdateUnitOfMeasure: (UnitOfMeasure) -> Unit = {},
    onDeleteUnitOfMeasure: (String) -> Unit = {},
    onAddTax: (Tax) -> Unit = {},
    onUpdateTax: (Tax) -> Unit = {},
    onDeleteTax: (String) -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var categoryToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryName by rememberSaveable { mutableStateOf("") }
    var categoryDescription by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }

    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    var showBrandDialog by rememberSaveable { mutableStateOf(false) }
    var brandToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var brandName by rememberSaveable { mutableStateOf("") }
    var brandDescription by rememberSaveable { mutableStateOf("") }
    var brandLogoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var brandNameError by rememberSaveable { mutableStateOf(false) }

    var brandToDelete by remember { mutableStateOf<Brand?>(null) }

    var showUnitDialog by rememberSaveable { mutableStateOf(false) }
    var unitToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var unitName by rememberSaveable { mutableStateOf("") }
    var unitAbbreviation by rememberSaveable { mutableStateOf("") }
    var unitIsPackageOrBox by rememberSaveable { mutableStateOf(false) }
    var unitNameError by rememberSaveable { mutableStateOf(false) }

    var unitToDelete by remember { mutableStateOf<UnitOfMeasure?>(null) }

    var showTaxDialog by rememberSaveable { mutableStateOf(false) }
    var taxToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var taxName by rememberSaveable { mutableStateOf("") }
    var taxDescription by rememberSaveable { mutableStateOf("") }
    var taxValueType by rememberSaveable { mutableStateOf(TaxValueType.PERCENTAGE) }
    var taxValueStr by rememberSaveable { mutableStateOf("") }
    var taxNameError by rememberSaveable { mutableStateOf(false) }
    var taxValueError by rememberSaveable { mutableStateOf(false) }
    var taxTypeExpanded by remember { mutableStateOf(false) }

    var taxToDelete by remember { mutableStateOf<Tax?>(null) }

    val brandLogoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            brandLogoUri = uri.toString()
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Expandable Section: Categorías
            ExpandableSettingsSection(
                title = "Categorías",
                summary = if (uiState.categories.isEmpty()) "Sin categorías" else "${uiState.categories.size} categorías creadas",
                icon = Icons.Default.Category,
                initialExpanded = false,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gestión de Categorías",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Button(
                            onClick = {
                                categoryToEditId = null
                                categoryName = ""
                                categoryDescription = ""
                                nameError = false
                                showCategoryDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
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
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        uiState.categories.forEachIndexed { index, category ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (!category.description.isNullOrBlank()) {
                                        Text(
                                            text = category.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar categoría ${category.name}",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        categoryToDelete = category
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar categoría ${category.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Section: Marcas
            ExpandableSettingsSection(
                title = "Marcas",
                summary = if (uiState.brands.isEmpty()) "Sin marcas" else "${uiState.brands.size} marcas creadas",
                icon = Icons.Default.Sell,
                initialExpanded = false,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gestión de Marcas",
                            style = MaterialTheme.typography.titleSmall,
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
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
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
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        uiState.brands.forEachIndexed { index, brand ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!brand.logoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = brand.logoUri,
                                        contentDescription = "Logo de ${brand.name}",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Surface(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = brand.name.firstOrNull()?.uppercase() ?: "?",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = brand.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (!brand.description.isNullOrBlank()) {
                                        Text(
                                            text = brand.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar marca ${brand.name}",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        brandToDelete = brand
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar marca ${brand.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Section: Unidades de Medida
            ExpandableSettingsSection(
                title = "Unidades de Medida",
                summary = if (uiState.unitsOfMeasure.isEmpty()) "Sin unidades de medida" else "${uiState.unitsOfMeasure.size} unidades de medida creadas",
                icon = Icons.Default.Straighten,
                initialExpanded = false,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gestión de Unidades de Medida",
                            style = MaterialTheme.typography.titleSmall,
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
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
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
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        uiState.unitsOfMeasure.forEachIndexed { index, unit ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val displayName = if (!unit.abbreviation.isNullOrBlank()) {
                                            "${unit.name} (${unit.abbreviation})"
                                        } else {
                                            unit.name
                                        }
                                        Text(
                                            text = displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        if (unit.isPackageOrBox) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            ) {
                                                Text(
                                                    text = "Paquete / Caja",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar unidad ${unit.name}",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        unitToDelete = unit
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar unidad ${unit.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Section: Impuestos
            ExpandableSettingsSection(
                title = "Impuestos",
                summary = if (uiState.taxes.isEmpty()) "Sin impuestos" else "${uiState.taxes.size} impuestos creados",
                icon = Icons.Default.Percent,
                initialExpanded = false,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gestión de Impuestos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Button(
                            onClick = {
                                taxToEditId = null
                                taxName = ""
                                taxDescription = ""
                                taxValueType = TaxValueType.PERCENTAGE
                                taxValueStr = ""
                                taxNameError = false
                                taxValueError = false
                                showTaxDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
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
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        uiState.taxes.forEachIndexed { index, tax ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val formattedValue = if (tax.valueType == TaxValueType.PERCENTAGE) {
                                        "${if (tax.value % 1.0 == 0.0) tax.value.toInt().toString() else tax.value.toString()}%"
                                    } else {
                                        formatCurrency(
                                            tax.value,
                                            uiState.currencySymbol,
                                            uiState.defaultDecimalPlaces,
                                            uiState.allowExtraDecimals
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = tax.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ) {
                                            Text(
                                                text = formattedValue,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    if (!tax.description.isNullOrBlank()) {
                                        Text(
                                            text = tax.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        taxToEditId = tax.id
                                        taxName = tax.name
                                        taxDescription = tax.description ?: ""
                                        taxValueType = tax.valueType
                                        taxValueStr = if (tax.value % 1.0 == 0.0) tax.value.toInt().toString() else tax.value.toString()
                                        taxNameError = false
                                        taxValueError = false
                                        showTaxDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar impuesto ${tax.name}",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        taxToDelete = tax
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar impuesto ${tax.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Section: Animaciones y Transiciones
            ExpandableSettingsSection(
                title = "Animaciones y Transiciones",
                summary = "${uiState.animationDurationMs} ms • ${uiState.animationType.displayName}",
                icon = Icons.Rounded.Animation,
                initialExpanded = false,
            ) {
                // Duration controls
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${uiState.animationDurationMs} ms",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }

                    Slider(
                        value = uiState.animationDurationMs.toFloat(),
                        onValueChange = { onDurationChange(it.toInt()) },
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
                                onClick = { onDurationChange(duration) },
                                label = { Text("${duration}ms") },
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )

                // Animation Type controls
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
                            style = MaterialTheme.typography.titleSmall,
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
                                    onClick = { onAnimationTypeChange(type) },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            // Section: Formato y Moneda
            ExpandableSettingsSection(
                title = "Formato y Moneda",
                summary = "Símbolo: ${uiState.currencySymbol} • ${uiState.defaultDecimalPlaces} decimales",
                icon = Icons.Rounded.AttachMoney,
                initialExpanded = false,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Símbolo de Moneda",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = uiState.currencySymbol,
                        onValueChange = { onCurrencySymbolChange(it) },
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
                                onClick = { onCurrencySymbolChange(symbol) },
                                label = { Text(symbol) },
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )

                    Text(
                        text = "Decimales predeterminados",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(2, 3, 4).forEach { decimals ->
                            FilterChip(
                                selected = uiState.defaultDecimalPlaces == decimals,
                                onClick = { onDefaultDecimalPlacesChange(decimals) },
                                label = { Text("$decimals decimales") },
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAllowExtraDecimalsChange(!uiState.allowExtraDecimals) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "Permitir decimales adicionales",
                                style = MaterialTheme.typography.titleSmall,
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
                            onCheckedChange = { onAllowExtraDecimalsChange(it) },
                        )
                    }
                }
            }

            // Future Section: General
            ExpandableSettingsSection(
                title = "General",
                summary = "Preferencias generales de la aplicación",
                icon = Icons.Rounded.Settings,
                initialExpanded = false,
            ) {
                Text(
                    text = "Ajustes generales del sistema y preferencias locales.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = categoryDescription,
                            onValueChange = { categoryDescription = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth()
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
                                    onAddCategory(
                                        Category(
                                            name = categoryName.trim(),
                                            description = categoryDescription.trim().ifBlank { null }
                                        )
                                    )
                                } else {
                                    onUpdateCategory(
                                        Category(
                                            id = editId,
                                            name = categoryName.trim(),
                                            description = categoryDescription.trim().ifBlank { null }
                                        )
                                    )
                                }
                                showCategoryDialog = false
                            }
                        }
                    ) {
                        Text(if (categoryToEditId == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCategoryDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
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
                            onDeleteCategory(cat.id)
                            categoryToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { categoryToDelete = null }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showBrandDialog) {
            AlertDialog(
                onDismissRequest = { showBrandDialog = false },
                title = {
                    Text(if (brandToEditId == null) "Añadir marca" else "Editar marca")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Logo selection & preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!brandLogoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = brandLogoUri,
                                    contentDescription = "Logo de la marca",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = { brandLogoPickerLauncher.launch("image/*") }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cambiar logo")
                                    }
                                    TextButton(
                                        onClick = { brandLogoUri = null },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Quitar logo")
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { brandLogoPickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Añadir logo"
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
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = brandDescription,
                            onValueChange = { brandDescription = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth()
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
                                    onAddBrand(
                                        Brand(
                                            name = brandName.trim(),
                                            description = brandDescription.trim().ifBlank { null },
                                            logoUri = brandLogoUri?.trim()?.ifBlank { null }
                                        )
                                    )
                                } else {
                                    onUpdateBrand(
                                        Brand(
                                            id = editId,
                                            name = brandName.trim(),
                                            description = brandDescription.trim().ifBlank { null },
                                            logoUri = brandLogoUri?.trim()?.ifBlank { null }
                                        )
                                    )
                                }
                                showBrandDialog = false
                            }
                        }
                    ) {
                        Text(if (brandToEditId == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBrandDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
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
                            onDeleteBrand(brand.id)
                            brandToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { brandToDelete = null }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showUnitDialog) {
            AlertDialog(
                onDismissRequest = { showUnitDialog = false },
                title = {
                    Text(if (unitToEditId == null) "Añadir unidad de medida" else "Editar unidad de medida")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = unitAbbreviation,
                            onValueChange = { unitAbbreviation = it },
                            label = { Text("Abreviatura (opcional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { unitIsPackageOrBox = !unitIsPackageOrBox }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = unitIsPackageOrBox,
                                onCheckedChange = { unitIsPackageOrBox = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¿Es un paquete o caja?",
                                style = MaterialTheme.typography.bodyMedium
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
                                    onAddUnitOfMeasure(
                                        UnitOfMeasure(
                                            name = unitName.trim(),
                                            abbreviation = unitAbbreviation.trim().ifBlank { null },
                                            isPackageOrBox = unitIsPackageOrBox
                                        )
                                    )
                                } else {
                                    onUpdateUnitOfMeasure(
                                        UnitOfMeasure(
                                            id = editId,
                                            name = unitName.trim(),
                                            abbreviation = unitAbbreviation.trim().ifBlank { null },
                                            isPackageOrBox = unitIsPackageOrBox
                                        )
                                    )
                                }
                                showUnitDialog = false
                            }
                        }
                    ) {
                        Text(if (unitToEditId == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showUnitDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
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
                            onDeleteUnitOfMeasure(unit.id)
                            unitToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { unitToDelete = null }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showTaxDialog) {
            AlertDialog(
                onDismissRequest = { showTaxDialog = false },
                title = {
                    Text(if (taxToEditId == null) "Añadir impuesto" else "Editar impuesto")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = taxDescription,
                            onValueChange = { taxDescription = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenuBox(
                            expanded = taxTypeExpanded,
                            onExpandedChange = { taxTypeExpanded = it },
                            modifier = Modifier.fillMaxWidth()
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
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = taxTypeExpanded,
                                onDismissRequest = { taxTypeExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Porcentaje %") },
                                    onClick = {
                                        taxValueType = TaxValueType.PERCENTAGE
                                        taxTypeExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Monto Fijo $") },
                                    onClick = {
                                        taxValueType = TaxValueType.FIXED_AMOUNT
                                        taxTypeExpanded = false
                                    }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = taxValueStr,
                            onValueChange = {
                                taxValueStr = it
                                if (it.toDoubleOrNull() != null) taxValueError = false
                            },
                            label = { Text("Valor") },
                            isError = taxValueError,
                            supportingText = if (taxValueError) {
                                { Text("Ingrese un valor numérico válido") }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsedValue = taxValueStr.toDoubleOrNull()
                            val nameValid = taxName.isNotBlank()
                            val valueValid = parsedValue != null && parsedValue >= 0.0

                            taxNameError = !nameValid
                            taxValueError = !valueValid

                            if (nameValid && parsedValue != null && parsedValue >= 0.0) {
                                val editId = taxToEditId
                                if (editId == null) {
                                    onAddTax(
                                        Tax(
                                            name = taxName.trim(),
                                            description = taxDescription.trim().ifBlank { null },
                                            valueType = taxValueType,
                                            value = parsedValue
                                        )
                                    )
                                } else {
                                    onUpdateTax(
                                        Tax(
                                            id = editId,
                                            name = taxName.trim(),
                                            description = taxDescription.trim().ifBlank { null },
                                            valueType = taxValueType,
                                            value = parsedValue
                                        )
                                    )
                                }
                                showTaxDialog = false
                            }
                        }
                    ) {
                        Text(if (taxToEditId == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTaxDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
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
                            onDeleteTax(tax.id)
                            taxToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { taxToDelete = null }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * Componente reutilizable para secciones de configuración desplegables.
 */
@Composable
fun ExpandableSettingsSection(
    title: String,
    summary: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initialExpanded) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (summary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Plegar sección" else "Desplegar sección",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MovilPOSTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(
                animationDurationMs = 400,
                animationType = AnimationType.SLIDE_AND_FADE,
            ),
            onDurationChange = {},
            onAnimationTypeChange = {},
            onNavigateBack = {},
        )
    }
}
