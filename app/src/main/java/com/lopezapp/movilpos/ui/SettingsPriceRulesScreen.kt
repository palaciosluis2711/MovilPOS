package com.lopezapp.movilpos.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lopezapp.movilpos.data.model.ArithmeticOperator
import com.lopezapp.movilpos.data.model.BaseVariable
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.sanitizeDecimalTextFieldValue
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsPriceRulesScreen(
    viewModel: SettingsViewModel,
    onNavigateToEditRule: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var ruleToDelete by remember { mutableStateOf<PriceRule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reglas de Precio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gestión de Reglas de Precio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = {
                        onNavigateToEditRule(null)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadir Regla")
                }
            }

            if (uiState.priceRules.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Calculate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay reglas de precio configuradas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Crea fórmulas dinámicas para ajustar los precios por categorías, clientes o características del producto.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                uiState.priceRules.forEach { rule ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Text(
                                        text = rule.formulaRepresentation(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rule.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (rule.isActive) "Activa" else "Inactiva",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (rule.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }

                                Switch(
                                    checked = rule.isActive,
                                    onCheckedChange = { isActive ->
                                        viewModel.updatePriceRule(rule.copy(isActive = isActive))
                                    }
                                )

                                IconButton(
                                    onClick = {
                                        onNavigateToEditRule(rule.id)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar regla ${rule.name}",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        ruleToDelete = rule
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar regla ${rule.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val catSummary = if (rule.applyToAllCategories) {
                                    "Todas las categorías"
                                } else {
                                    "Categorías: ${rule.categoryNames.joinToString(", ")}"
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ) {
                                    Text(
                                        text = catSummary,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                val custSummary = if (rule.applyToAllCustomers) {
                                    "Todos los clientes"
                                } else {
                                    val customerNames = rule.customerIds.map { id ->
                                        uiState.customers.find { it.id == id }?.name ?: id
                                    }
                                    "Clientes: ${customerNames.joinToString(", ")}"
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ) {
                                    Text(
                                        text = custSummary,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                if (rule.applyToBundles) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ) {
                                        Text(
                                            text = "Bundles",
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (rule.applyToServices) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ) {
                                        Text(
                                            text = "Servicios",
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (rule.applyToAlreadyDiscounted) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ) {
                                        Text(
                                            text = "Con Descuentos",
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (ruleToDelete != null) {
            val rule = ruleToDelete!!
            AlertDialog(
                onDismissRequest = { ruleToDelete = null },
                title = { Text("Eliminar regla de precio") },
                text = { Text("¿Deseas eliminar la regla de precio \"${rule.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePriceRule(rule.id)
                            ruleToDelete = null
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
                    TextButton(onClick = { ruleToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PriceRuleEditScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    ruleId: String? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingRule = remember(uiState.priceRules, ruleId) {
        if (ruleId != null) uiState.priceRules.find { it.id == ruleId } else null
    }

    var name by rememberSaveable(existingRule?.id) { mutableStateOf(existingRule?.name ?: "") }
    var baseVariable by remember(existingRule?.id) { mutableStateOf(existingRule?.baseVariable ?: BaseVariable.COST) }
    var operator by remember(existingRule?.id) { mutableStateOf(existingRule?.operator ?: ArithmeticOperator.DIVIDE) }
    var valueState by rememberSaveable(existingRule?.id, stateSaver = TextFieldValue.Saver) {
        val initialValStr = existingRule?.let {
            if ((it.value % 1.0) == 0.0) it.value.toInt().toString() else it.value.toString()
        } ?: "0.80"
        mutableStateOf(TextFieldValue(initialValStr, selection = TextRange(initialValStr.length)))
    }

    var applyToAllCategories by rememberSaveable(existingRule?.id) { mutableStateOf(existingRule?.applyToAllCategories ?: true) }
    var selectedCategories by remember(existingRule?.id) { mutableStateOf(existingRule?.categoryNames?.toSet() ?: emptySet()) }

    var applyToAllCustomers by rememberSaveable(existingRule?.id) { mutableStateOf(existingRule?.applyToAllCustomers ?: true) }
    var selectedCustomerIds by remember(existingRule?.id) { mutableStateOf(existingRule?.customerIds?.toSet() ?: emptySet()) }

    var applyToBundles by rememberSaveable(existingRule?.id) { mutableStateOf(existingRule?.applyToBundles ?: false) }
    var applyToServices by rememberSaveable(existingRule?.id) { mutableStateOf(existingRule?.applyToServices ?: false) }
    var applyToAlreadyDiscounted by rememberSaveable(existingRule?.id) { mutableStateOf(existingRule?.applyToAlreadyDiscounted ?: false) }

    var nameError by rememberSaveable { mutableStateOf(false) }
    var valueError by rememberSaveable { mutableStateOf(false) }

    val previewBaseText = if (baseVariable == BaseVariable.COST) "[Costo]" else "[Precio]"
    val previewOpText = when (operator) {
        ArithmeticOperator.ADD -> "+"
        ArithmeticOperator.SUBTRACT -> "-"
        ArithmeticOperator.MULTIPLY -> "×"
        ArithmeticOperator.DIVIDE -> "÷"
    }
    val previewValText = valueState.text.ifBlank { "0" }
    val previewFormula = "$previewBaseText $previewOpText $previewValText"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (ruleId == null) "Nueva Regla de Precio" else "Editar Regla de Precio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Nombre de la Regla") },
                placeholder = { Text("Ej. Precio Mayoreo") },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("El nombre es obligatorio") }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Constructor de Fórmula",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(text = "Variable base:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = baseVariable == BaseVariable.COST,
                            onClick = { baseVariable = BaseVariable.COST },
                            label = { Text("[Costo]") }
                        )
                        FilterChip(
                            selected = baseVariable == BaseVariable.PRICE,
                            onClick = { baseVariable = BaseVariable.PRICE },
                            label = { Text("[Precio]") }
                        )
                    }

                    Text(text = "Operador aritmético:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val operators = listOf(
                            ArithmeticOperator.ADD to "+",
                            ArithmeticOperator.SUBTRACT to "-",
                            ArithmeticOperator.MULTIPLY to "×",
                            ArithmeticOperator.DIVIDE to "÷"
                        )
                        operators.forEach { (op, label) ->
                            FilterChip(
                                selected = operator == op,
                                onClick = { operator = op },
                                label = { Text(label, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = valueState,
                        onValueChange = { newValue ->
                            val updated = sanitizeDecimalTextFieldValue(newValue, valueState)
                            valueState = updated
                            if (updated.text.toDoubleOrNull() != null) valueError = false
                        },
                        label = { Text("Valor Numérico") },
                        placeholder = { Text("Ej. 0.80 o 1.25") },
                        isError = valueError,
                        supportingText = if (valueError) {
                            { Text("Ingrese un valor numérico válido") }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Functions,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Fórmula:",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Text(
                                text = previewFormula,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Aplica a Categorías",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(
                            selected = applyToAllCategories,
                            onClick = { applyToAllCategories = true },
                            role = Role.RadioButton
                        )
                    ) {
                        RadioButton(selected = applyToAllCategories, onClick = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Todas las categorías", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(
                            selected = !applyToAllCategories,
                            onClick = { applyToAllCategories = false },
                            role = Role.RadioButton
                        )
                    ) {
                        RadioButton(selected = !applyToAllCategories, onClick = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Especificar", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                AnimatedVisibility(
                    visible = !applyToAllCategories,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    )
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        uiState.categories.map { it.name }.forEach { catName ->
                            val isSelected = selectedCategories.contains(catName)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategories = if (isSelected) {
                                        selectedCategories - catName
                                    } else {
                                        selectedCategories + catName
                                    }
                                },
                                label = { Text(catName) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Aplica a Clientes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(
                            selected = applyToAllCustomers,
                            onClick = { applyToAllCustomers = true },
                            role = Role.RadioButton
                        )
                    ) {
                        RadioButton(selected = applyToAllCustomers, onClick = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Todos los clientes", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(
                            selected = !applyToAllCustomers,
                            onClick = { applyToAllCustomers = false },
                            role = Role.RadioButton
                        )
                    ) {
                        RadioButton(selected = !applyToAllCustomers, onClick = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Especificar", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                AnimatedVisibility(
                    visible = !applyToAllCustomers,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    )
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        uiState.customers.forEach { customer ->
                            val isSelected = selectedCustomerIds.contains(customer.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCustomerIds = if (isSelected) {
                                        selectedCustomerIds - customer.id
                                    } else {
                                        selectedCustomerIds + customer.id
                                    }
                                },
                                label = { Text(customer.name) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Validaciones adicionales",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { applyToBundles = !applyToBundles },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = applyToBundles,
                        onCheckedChange = { applyToBundles = it }
                    )
                    Text(
                        text = "Aplicar sobre Bundles",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { applyToServices = !applyToServices },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = applyToServices,
                        onCheckedChange = { applyToServices = it }
                    )
                    Text(
                        text = "Aplicar sobre Servicios",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { applyToAlreadyDiscounted = !applyToAlreadyDiscounted },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = applyToAlreadyDiscounted,
                        onCheckedChange = { applyToAlreadyDiscounted = it }
                    )
                    Text(
                        text = "Aplicar sobre productos que ya tengan descuentos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val isNameValid = name.isNotBlank()
                        val parsedVal = valueState.text.toDoubleOrNull()
                        val isValValid = parsedVal != null

                        nameError = !isNameValid
                        valueError = !isValValid

                        if (isNameValid && isValValid) {
                            val newRule = PriceRule(
                                id = existingRule?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                baseVariable = baseVariable,
                                operator = operator,
                                value = parsedVal,
                                applyToAllCategories = applyToAllCategories,
                                categoryNames = if (applyToAllCategories) emptyList() else selectedCategories.toList(),
                                applyToAllCustomers = applyToAllCustomers,
                                customerIds = if (applyToAllCustomers) emptyList() else selectedCustomerIds.toList(),
                                applyToBundles = applyToBundles,
                                applyToServices = applyToServices,
                                applyToAlreadyDiscounted = applyToAlreadyDiscounted,
                                isActive = existingRule?.isActive ?: true
                            )
                            if (existingRule == null) {
                                viewModel.addPriceRule(newRule)
                            } else {
                                viewModel.updatePriceRule(newRule)
                            }
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (ruleId == null) "Guardar" else "Actualizar")
                }
            }
        }
    }
}
