package com.lopezapp.movilpos.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lopezapp.movilpos.data.model.CartItem
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.ui.theme.PriceRuleColors
import com.lopezapp.movilpos.ui.viewmodel.POSViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsUiState
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(
    viewModel: POSViewModel,
    settingsViewModel: SettingsViewModel? = null,
    onNavigateToCheckout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCartItemIds by viewModel.selectedCartItemIds.collectAsState()
    val settingsUiState = settingsViewModel?.uiState?.collectAsState()?.value
        ?: SettingsUiState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    val handleCheckout = {
        if (onNavigateToCheckout != null) {
            onNavigateToCheckout()
        } else {
            viewModel.checkout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Punto de Venta") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .fillMaxSize()
        ) {
            if (maxWidth > 600.dp) {
                // Wide layout (tablet/landscape)
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1.5f).fillMaxHeight()) {
                        ProductSearchBar(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) }
                        )
                        ProductGrid(
                            products = uiState.products,
                            onAddClick = { viewModel.addToCart(it) },
                            currencySymbol = currencySymbol,
                            defaultDecimalPlaces = defaultDecimalPlaces,
                            allowExtraDecimals = allowExtraDecimals,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
                    CartSection(
                        cartItems = uiState.cartItems,
                        selectedCartItemIds = selectedCartItemIds,
                        total = uiState.total,
                        priceRules = uiState.priceRules,
                        selectedPriceRuleId = uiState.selectedPriceRuleId,
                        onRuleSelected = { viewModel.selectPriceRule(it) },
                        onAddClick = { viewModel.addToCart(it) },
                        onRemoveClick = { viewModel.removeFromCart(it) },
                        onDeleteClick = { viewModel.deleteFromCart(it) },
                        onToggleSelection = { viewModel.toggleItemSelection(it) },
                        onSelectAll = { viewModel.selectAll() },
                        onClearSelection = { viewModel.clearSelection() },
                        onCheckout = handleCheckout,
                        currencySymbol = currencySymbol,
                        defaultDecimalPlaces = defaultDecimalPlaces,
                        allowExtraDecimals = allowExtraDecimals,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Narrow layout (phone portrait) with Collapsible Bottom Cart Sheet
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize().padding(bottom = 72.dp)) {
                        ProductSearchBar(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) }
                        )
                        ProductGrid(
                            products = uiState.products,
                            onAddClick = { viewModel.addToCart(it) },
                            currencySymbol = currencySymbol,
                            defaultDecimalPlaces = defaultDecimalPlaces,
                            allowExtraDecimals = allowExtraDecimals,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Collapsible/Expandable Bottom Cart Sheet anchored at bottom
                    BottomCartSheet(
                        cartItems = uiState.cartItems,
                        selectedCartItemIds = selectedCartItemIds,
                        total = uiState.total,
                        priceRules = uiState.priceRules,
                        selectedPriceRuleId = uiState.selectedPriceRuleId,
                        onRuleSelected = { viewModel.selectPriceRule(it) },
                        onAddClick = { viewModel.addToCart(it) },
                        onRemoveClick = { viewModel.removeFromCart(it) },
                        onDeleteClick = { viewModel.deleteFromCart(it) },
                        onToggleSelection = { viewModel.toggleItemSelection(it) },
                        onSelectAll = { viewModel.selectAll() },
                        onClearSelection = { viewModel.clearSelection() },
                        onCheckout = handleCheckout,
                        currencySymbol = currencySymbol,
                        defaultDecimalPlaces = defaultDecimalPlaces,
                        allowExtraDecimals = allowExtraDecimals,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Buscar producto por nombre o código...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar"
                    )
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ProductGrid(
    products: List<Product>,
    onAddClick: (Product) -> Unit,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se encontraron productos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(products, key = { it.id }) { product ->
                Card(
                    onClick = { onAddClick(product) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatCurrency(product.price, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Stock: ${product.stock}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAddClick(product) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = "Agregar")
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getPriceColor(
    item: CartItem,
    priceRules: List<PriceRule> = emptyList(),
    isDark: Boolean = isSystemInDarkTheme()
): Color {
    return when {
        item.isRuleDiscounted -> PriceRuleColors.getRuleAccentColor(item.appliedRuleId, priceRules, isDark)
        item.isBundleDiscounted -> if (isDark) Color(0xFFEC407A) else Color(0xFFE91E63)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun PriceRuleCapsules(
    priceRules: List<PriceRule>,
    selectedPriceRuleId: String?,
    onRuleSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (priceRules.isEmpty()) return

    val isDark = isSystemInDarkTheme()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Reglas de Precio:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            itemsIndexed(priceRules, key = { _, rule -> rule.id }) { index, rule ->
                val isSelected = rule.id == selectedPriceRuleId
                val palette = PriceRuleColors.getPalette(index)
                val bgColor = palette.getBackgroundColor(isDark)
                val accentColor = palette.getAccentColor(isDark)

                Surface(
                    onClick = {
                        if (isSelected) {
                            onRuleSelected(null)
                        } else {
                            onRuleSelected(rule.id)
                        }
                    },
                    shape = CircleShape,
                    color = bgColor,
                    contentColor = accentColor,
                    border = if (isSelected) BorderStroke(2.dp, accentColor) else null,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(end = 2.dp)
                            )
                        }
                        Text(
                            text = rule.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomCartSheet(
    cartItems: List<CartItem>,
    selectedCartItemIds: Set<String> = emptySet(),
    total: Double,
    priceRules: List<PriceRule>,
    selectedPriceRuleId: String?,
    onRuleSelected: (String?) -> Unit,
    onAddClick: (CartItem) -> Unit,
    onRemoveClick: (CartItem) -> Unit,
    onDeleteClick: (CartItem) -> Unit,
    onToggleSelection: (String) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onCheckout: () -> Unit,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val totalCount = cartItems.sumOf { it.quantity }

    val draggableState = rememberDraggableState { delta ->
        if (delta < -10) {
            isExpanded = true
        } else if (delta > 10) {
            isExpanded = false
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }

            // Summary bar (always visible, header in collapsed state)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgedBox(
                        badge = {
                            if (totalCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(totalCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Carrito",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (totalCount == 1) "1 producto" else "$totalCount productos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isExpanded) {
                            Text(
                                text = "Total: ${formatCurrency(total, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isExpanded) {
                        Text(
                            text = formatCurrency(total, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) "Colapsar" else "Expandir"
                        )
                    }
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(250, easing = FastOutSlowInEasing)),
                exit = shrinkVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(250, easing = FastOutSlowInEasing))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    // Price Rule Capsules
                    PriceRuleCapsules(
                        priceRules = priceRules,
                        selectedPriceRuleId = selectedPriceRuleId,
                        onRuleSelected = onRuleSelected,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Selection Header
                    if (cartItems.isNotEmpty()) {
                        val validSelectedCount = cartItems.count { selectedCartItemIds.contains(it.id) }
                        val allSelected = cartItems.isNotEmpty() && validSelectedCount == cartItems.size
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    if (allSelected) onClearSelection() else onSelectAll()
                                }
                            ) {
                                Checkbox(
                                    checked = allSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) onSelectAll() else onClearSelection()
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val selectionText = if (validSelectedCount == 1) {
                                    "1 de ${cartItems.size} seleccionado"
                                } else {
                                    "$validSelectedCount de ${cartItems.size} seleccionados"
                                }
                                Text(
                                    text = if (validSelectedCount == 0) "Seleccionar todos" else selectionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (validSelectedCount > 0) {
                                TextButton(onClick = onClearSelection) {
                                    Text("Desmarcar todos")
                                }
                            }
                        }
                    }

                    // Cart Items List
                    if (cartItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "El carrito está vacío",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cartItems, key = { it.id }) { item ->
                                CartItemRow(
                                    item = item,
                                    isSelected = selectedCartItemIds.contains(item.id),
                                    priceRules = priceRules,
                                    onToggleSelection = { onToggleSelection(item.id) },
                                    onAddClick = { onAddClick(item) },
                                    onRemoveClick = { onRemoveClick(item) },
                                    onDeleteClick = { onDeleteClick(item) },
                                    currencySymbol = currencySymbol,
                                    defaultDecimalPlaces = defaultDecimalPlaces,
                                    allowExtraDecimals = allowExtraDecimals
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Total & Checkout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCurrency(total, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = {
                                onCheckout()
                                isExpanded = false
                            },
                            enabled = cartItems.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Finalizar Venta",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartSection(
    cartItems: List<CartItem>,
    selectedCartItemIds: Set<String> = emptySet(),
    total: Double,
    priceRules: List<PriceRule> = emptyList(),
    selectedPriceRuleId: String? = null,
    onRuleSelected: (String?) -> Unit = {},
    onAddClick: (CartItem) -> Unit,
    onRemoveClick: (CartItem) -> Unit,
    onDeleteClick: (CartItem) -> Unit = {},
    onToggleSelection: (String) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onCheckout: () -> Unit,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrito",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Carrito Actual",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Price Rule Capsules
        PriceRuleCapsules(
            priceRules = priceRules,
            selectedPriceRuleId = selectedPriceRuleId,
            onRuleSelected = onRuleSelected,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Selection Header
        if (cartItems.isNotEmpty()) {
            val validSelectedCount = cartItems.count { selectedCartItemIds.contains(it.id) }
            val allSelected = cartItems.isNotEmpty() && validSelectedCount == cartItems.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        if (allSelected) onClearSelection() else onSelectAll()
                    }
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { checked ->
                            if (checked) onSelectAll() else onClearSelection()
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val selectionText = if (validSelectedCount == 1) {
                        "1 de ${cartItems.size} seleccionado"
                    } else {
                        "$validSelectedCount de ${cartItems.size} seleccionados"
                    }
                    Text(
                        text = if (validSelectedCount == 0) "Seleccionar todos" else selectionText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (validSelectedCount > 0) {
                    TextButton(onClick = onClearSelection) {
                        Text("Desmarcar todos")
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "El carrito está vacío",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    CartItemRow(
                        item = item,
                        isSelected = selectedCartItemIds.contains(item.id),
                        priceRules = priceRules,
                        onToggleSelection = { onToggleSelection(item.id) },
                        onAddClick = { onAddClick(item) },
                        onRemoveClick = { onRemoveClick(item) },
                        onDeleteClick = { onDeleteClick(item) },
                        currencySymbol = currencySymbol,
                        defaultDecimalPlaces = defaultDecimalPlaces,
                        allowExtraDecimals = allowExtraDecimals
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(total, currencySymbol, defaultDecimalPlaces, allowExtraDecimals),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onCheckout,
                enabled = cartItems.isNotEmpty(),
                modifier = Modifier.height(56.dp)
            ) {
                Text(
                    text = "Finalizar Venta",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    isSelected: Boolean = false,
    priceRules: List<PriceRule> = emptyList(),
    onToggleSelection: (() -> Unit)? = null,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true
) {
    val priceColor = getPriceColor(item, priceRules)
    val formattedSubtotal = formatCurrency(item.subtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)
    val formattedUnitPrice = formatCurrency(item.effectiveUnitPrice, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection?.invoke() },
                modifier = Modifier.padding(end = 4.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleSelection?.invoke() }
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$formattedSubtotal ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = priceColor
                    )
                    Text(
                        text = "($formattedUnitPrice c/u)",
                        style = MaterialTheme.typography.bodySmall,
                        color = priceColor
                    )
                }
                if (item.isRuleDiscounted) {
                    val ruleName = item.appliedRuleName
                        ?: priceRules.find { it.id == item.appliedRuleId }?.name
                        ?: "Regla"
                    Text(
                        text = "Aplicado: $ruleName",
                        style = MaterialTheme.typography.labelSmall,
                        color = priceColor
                    )
                } else if (item.isBundleDiscounted) {
                    Text(
                        text = "Descuento por Combo",
                        style = MaterialTheme.typography.labelSmall,
                        color = priceColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Disminuir", modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                FilledTonalIconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar", modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
