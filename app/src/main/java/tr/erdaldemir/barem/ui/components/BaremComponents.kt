package tr.erdaldemir.barem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.domain.model.HomeTileStyle
import tr.erdaldemir.barem.ui.theme.AccentBurgundy
import tr.erdaldemir.barem.ui.theme.AccentGold
import tr.erdaldemir.barem.ui.theme.CardBorder
import tr.erdaldemir.barem.ui.theme.PrimaryBlue
import tr.erdaldemir.barem.ui.theme.SurfaceElevated
import tr.erdaldemir.barem.ui.theme.TileAsgariUcret
import tr.erdaldemir.barem.ui.theme.TileDigerMaas
import tr.erdaldemir.barem.ui.theme.TileHarcirah
import tr.erdaldemir.barem.ui.theme.TileYurtdisiMaas
import tr.erdaldemir.barem.ui.theme.TileEmekli
import tr.erdaldemir.barem.ui.theme.TileKamuIscisi
import tr.erdaldemir.barem.ui.theme.TileMemur
import tr.erdaldemir.barem.ui.theme.TileSozlesmeli
import tr.erdaldemir.barem.ui.theme.TextMuted
import tr.erdaldemir.barem.ui.theme.TileToolSurface
import androidx.compose.foundation.layout.defaultMinSize

@Composable
fun BaremHomePrimaryCard(
    title: String,
    subtitle: String,
    style: HomeTileStyle,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when (style) {
        HomeTileStyle.Maas -> TileMemur
        HomeTileStyle.Sozlesmeli -> TileSozlesmeli
        HomeTileStyle.KamuIscisi -> TileKamuIscisi
        HomeTileStyle.Emekli -> TileEmekli
        HomeTileStyle.AsgariUcret -> TileAsgariUcret
        HomeTileStyle.Harcirah -> TileHarcirah
        HomeTileStyle.YurtdisiMaas -> TileYurtdisiMaas
        HomeTileStyle.DigerMaas -> TileDigerMaas
        HomeTileStyle.Tool -> TileToolSurface
    }
    val alpha = if (enabled) 1f else 0.55f
    val shape = RoundedCornerShape(18.dp)
    Card(
        modifier = modifier
            .defaultMinSize(minHeight = 108.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = alpha)),
        border = BorderStroke(
            width = if (style == HomeTileStyle.Maas) 2.5.dp else 1.5.dp,
            color = if (style == HomeTileStyle.Maas) AccentGold else Color.White.copy(alpha = 0.22f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (style == HomeTileStyle.Maas) 8.dp else 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun BaremHomeSecondaryCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = if (enabled) 1f else 0.5f
    val shape = RoundedCornerShape(14.dp)
    Card(
        modifier = modifier
            .aspectRatio(1.05f)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = TileToolSurface.copy(alpha = 0.92f * alpha),
        ),
        border = BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.45f * alpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 3.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun BaremHomeGridCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = if (enabled) 1f else 0.5f
    val shape = RoundedCornerShape(16.dp)
    val borderColor = when {
        highlighted -> AccentGold
        enabled -> PrimaryBlue.copy(alpha = 0.6f)
        else -> CardBorder
    }
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = when {
                highlighted -> AccentBurgundy
                !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surface
            },
        ),
        border = BorderStroke(
            width = if (highlighted) 3.dp else 2.dp,
            color = when {
                highlighted -> AccentGold
                enabled -> PrimaryBlue
                else -> CardBorder
            }.copy(alpha = alpha),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlighted) 6.dp else 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {
                        highlighted -> AccentBurgundy
                        else -> MaterialTheme.colorScheme.surface
                    },
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (highlighted) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (highlighted) {
                        Color.White.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun BaremFieldLabel(
    label: String,
    required: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (required) {
                stringResource(R.string.field_required)
            } else {
                stringResource(R.string.field_optional)
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (required) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
fun BaremSelectionCard(
    title: String,
    subtitle: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val shape = RoundedCornerShape(if (compact) 12.dp else 14.dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                AccentBurgundy
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = BorderStroke(
            width = if (selected) 3.dp else 2.dp,
            color = if (selected) AccentGold else PrimaryBlue.copy(alpha = 0.45f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
    ) {
        val pad = if (compact) 10.dp else 16.dp
        Column(modifier = Modifier.padding(pad)) {
            Text(
                text = title,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        Color.White.copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun BaremSummaryBanner(
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        lines.filter { it.isNotBlank() }.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
fun BaremIntGridPicker(
    title: String,
    options: List<Int>,
    selected: Int?,
    columns: Int = 5,
    labelFor: (Int) -> String = { it.toString() },
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = true,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BaremFieldLabel(label = title, required = required)
        options.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { value ->
                    BaremMiniChoiceChip(
                        label = labelFor(value),
                        selected = selected == value,
                        onClick = { if (enabled) onSelect(value) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun BaremMiniChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(10.dp)
    val containerColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        selected -> AccentBurgundy
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        selected -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    selected -> AccentGold
                    else -> PrimaryBlue.copy(alpha = 0.35f)
                },
                shape = shape,
            )
            .background(containerColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun BaremStringGridPicker(
    title: String,
    options: List<String>,
    selected: String?,
    columns: Int = 3,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = true,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BaremFieldLabel(label = title, required = required)
        options.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { value ->
                    BaremMiniChoiceChip(
                        label = value,
                        selected = selected == value,
                        onClick = { if (enabled) onSelect(value) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaremUnvanAutocompleteField(
    label: String,
    selected: String?,
    onSearch: (String) -> List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchHint: String = stringResource(R.string.unvan_search_hint),
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(selected.orEmpty()) }

    androidx.compose.runtime.LaunchedEffect(selected) {
        if (selected != null) {
            query = selected
        }
    }

    val menuOptions = remember(query, expanded) {
        when {
            query.isNotEmpty() -> onSearch(query)
            expanded -> onSearch("")
            else -> emptyList()
        }
    }
    val showMenu = expanded && menuOptions.isNotEmpty()

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BaremFieldLabel(label = label, required = true)
        ExposedDropdownMenuBox(
            expanded = showMenu,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { new ->
                    query = new
                    expanded = true
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                placeholder = { Text(searchHint) },
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = CardBorder,
                ),
            )
            ExposedDropdownMenu(
                expanded = showMenu,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 360.dp),
            ) {
                menuOptions.forEach { unvan ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = unvan,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = {
                            query = unvan
                            onSelect(unvan)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> BaremDropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    required: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BaremFieldLabel(label = label, required = required)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) onExpandedChange(it) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                placeholder = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = CardBorder,
                ),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.heightIn(max = 360.dp),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel(option),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = {
                            onSelect(option)
                            onExpandedChange(false)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
fun <T> BaremSearchableListPicker(
    label: String,
    query: String,
    onQueryChange: (String) -> Unit,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    emptyHint: String,
    searchHint: String = stringResource(R.string.meslek_search_hint),
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BaremFieldLabel(label = label, required = true)
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(searchHint) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = CardBorder,
            ),
        )
        selected?.let {
            Text(
                text = stringResource(R.string.meslek_search_selected, optionLabel(it)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (options.isEmpty()) {
            Text(
                text = emptyHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(options, key = { optionLabel(it) }) { option ->
                    BaremSelectionCard(
                        title = optionLabel(option),
                        selected = selected == option,
                        onClick = { onSelect(option) },
                    )
                }
            }
        }
    }
}

@Composable
fun BaremAmountLine(
    label: String,
    amount: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (highlight) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = amount,
            style = if (highlight) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = if (highlight) AccentGold else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
fun BaremMonthlyNetTable(
    labels: List<String>,
    monthlyNet: List<Double>,
    formatAmount: (Double) -> String,
    modifier: Modifier = Modifier,
    valueColumnLabel: String = stringResource(R.string.result_table_net),
) {
    if (monthlyNet.isEmpty()) return
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, CardBorder, shape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.result_table_month),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = valueColumnLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        labels.zip(monthlyNet).forEachIndexed { index, (label, net) ->
            val isSecondHalf = index >= 6
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when {
                            index % 2 == 1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSecondHalf) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = formatAmount(net),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun BaremStatPairRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (highlight) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                },
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.55f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = if (highlight) AccentGold else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.45f),
            textAlign = TextAlign.End,
        )
    }
}
