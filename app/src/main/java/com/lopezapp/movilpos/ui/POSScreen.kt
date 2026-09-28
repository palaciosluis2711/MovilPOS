package com.lopezapp.movilpos.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lopezapp.movilpos.data.model.CartItem
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.ui.viewmodel.POSViewModel
import com.lopezapp.movilpos.ui.viewmodel.SettingsUiState
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import com.lopezapp.movilpos.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(
    viewModel: POSViewModel,
    settingsViewModel: SettingsViewModel? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsUiState = settingsViewModel?.uiState?.collectAsState()?.value
        ?: SettingsUiState()
    val currencySymbol = settingsUiState.currencySymbol
    val defaultDecimalPlaces = settingsUiState.defaultDecimalPlaces
    val allowExtraDecimals = settingsUiState.allowExtraDecimals

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Point of Sale") },
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
                    ProductGrid(
                        products = uiState.products,
                        onAddClick = { viewModel.addToCart(it) },
                        currencySymbol = currencySymbol,
                        defaultDecimalPlaces = defaultDecimalPlaces,
                        allowExtraDecimals = allowExtraDecimals,
                        modifier = Modifier.weight(1.5f)
                    )
                    VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
                    CartSection(
                        cartItems = uiState.cartItems,
                        total = uiState.total,
                        onAddClick = { viewModel.addToCart(it) },
                        onRemoveClick = { viewModel.removeFromCart(it) },
                        onCheckout = { viewModel.checkout() },
                        currencySymbol = currencySymbol,
                        defaultDecimalPlaces = defaultDecimalPlaces,
                        allowExtraDecimals = allowExtraDecimals,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Narrow layout (phone portrait)
                Column(modifier = Modifier.fillMaxSize()) {
                    ProductGrid(
                        products = uiState.products,
                        onAddClick = { viewModel.addToCart(it) },
                        currencySymbol = currencySymbol,
                        defaultDecimalPlaces = defaultDecimalPlaces,
                        allowExtraDecimals = allowExtraDecimals,
                        modifier = Modifier.weight(1.2f)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        CartSection(
                            cartItems = uiState.cartItems,
                            total = uiState.total,
                            onAddClick = { viewModel.addToCart(it) },
                            onRemoveClick = { viewModel.removeFromCart(it) },
                            onCheckout = { viewModel.checkout() },
                            currencySymbol = currencySymbol,
                            defaultDecimalPlaces = defaultDecimalPlaces,
                            allowExtraDecimals = allowExtraDecimals
                        )
                    }
                }
            }
        }
    }
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
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(products) { product ->
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
                        color = MaterialTheme.colorScheme.primary
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
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Add")
                        Spacer(Modifier.width(8.dp))
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun CartSection(
    cartItems: List<CartItem>,
    total: Double,
    onAddClick: (CartItem) -> Unit,
    onRemoveClick: (CartItem) -> Unit,
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
                contentDescription = "Cart",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Current Cart",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Cart is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onAddClick = { onAddClick(item) },
                        onRemoveClick = { onRemoveClick(item) },
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
                    text = "Checkout",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    currencySymbol: String = "$",
    defaultDecimalPlaces: Int = 2,
    allowExtraDecimals: Boolean = true
) {
    val discountColor = if (isSystemInDarkTheme()) Color(0xFFEC407A) else Color(0xFFE91E63)
    val priceColor = if (item.isBundleDiscounted) discountColor else MaterialTheme.colorScheme.onSurfaceVariant

    val formattedSubtotal = formatCurrency(item.subtotal, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)
    val formattedUnitPrice = formatCurrency(item.effectiveUnitPrice, currencySymbol, defaultDecimalPlaces, allowExtraDecimals)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$formattedSubtotal ($formattedUnitPrice c/u)",
                style = MaterialTheme.typography.bodyMedium,
                color = priceColor
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
            }
            Text(
                text = "${item.quantity}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.widthIn(min = 24.dp)
            )
            FilledTonalIconButton(
                onClick = onAddClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
            }
        }
    }
}
