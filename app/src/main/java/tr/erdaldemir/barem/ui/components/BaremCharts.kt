package tr.erdaldemir.barem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.domain.analytics.DeductionYearPoint
import tr.erdaldemir.barem.domain.analytics.DualYearPoint
import tr.erdaldemir.barem.domain.analytics.YearPoint
import tr.erdaldemir.barem.domain.model.DeductionBreakdown
import tr.erdaldemir.barem.ui.theme.AccentGold
import tr.erdaldemir.barem.ui.theme.PrimaryBlue
import tr.erdaldemir.barem.ui.theme.SurfaceElevated
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun BaremYearLineChart(
    points: List<YearPoint>,
    valueFormatter: (Double) -> String,
    lineColor: Color = PrimaryBlue,
    modifier: Modifier = Modifier,
    highlightLast: Boolean = true,
) {
    if (points.isEmpty()) return
    val values = points.map { it.value.toFloat() }
    val minVal = values.minOrNull() ?: 0f
    val maxVal = max(values.maxOrNull() ?: 1f, minVal + 1f)
    val range = max(maxVal - minVal, 1f)
    val labels = points.map { it.year.toString() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            val padX = 12f
            val padY = 16f
            val w = size.width - padX * 2
            val h = size.height - padY * 2
            if (points.size == 1) {
                val y = padY + h * (1f - (values[0] - minVal) / range)
                drawCircle(color = lineColor, radius = 8f, center = Offset(size.width / 2f, y))
                return@Canvas
            }
            val path = Path()
            points.forEachIndexed { index, _ ->
                val x = padX + w * index / (points.size - 1).coerceAtLeast(1)
                val y = padY + h * (1f - (values[index] - minVal) / range)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round),
            )
            points.forEachIndexed { index, _ ->
                val x = padX + w * index / (points.size - 1).coerceAtLeast(1)
                val y = padY + h * (1f - (values[index] - minVal) / range)
                val color = if (highlightLast && index == points.lastIndex) AccentGold else lineColor
                drawCircle(color = color, radius = if (index == points.lastIndex) 6f else 4f, center = Offset(x, y))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val step = (labels.size / 6).coerceAtLeast(1)
            labels.forEachIndexed { index, label ->
                if (index % step == 0 || index == labels.lastIndex) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                }
            }
        }
        Text(
            text = "${points.last().year}: ${valueFormatter(points.last().value)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun BaremMonthlyLineChart(
    labels: List<String>,
    values: List<Double>,
    valueFormatter: (Double) -> String,
    lineColor: Color = PrimaryBlue,
    modifier: Modifier = Modifier,
) {
    if (values.isEmpty()) return
    val floatValues = values.map { it.toFloat() }
    val minVal = floatValues.minOrNull() ?: 0f
    val maxVal = max(floatValues.maxOrNull() ?: 1f, minVal + 1f)
    val range = max(maxVal - minVal, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.result_chart_monthly_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        ) {
            val padX = 12f
            val padY = 16f
            val w = size.width - padX * 2
            val h = size.height - padY * 2
            if (values.size == 1) {
                val y = padY + h * (1f - (floatValues[0] - minVal) / range)
                drawCircle(color = lineColor, radius = 8f, center = Offset(size.width / 2f, y))
                return@Canvas
            }
            val path = Path()
            floatValues.forEachIndexed { index, v ->
                val x = padX + w * index / (floatValues.size - 1).coerceAtLeast(1)
                val y = padY + h * (1f - (v - minVal) / range)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round),
            )
            floatValues.forEachIndexed { index, v ->
                val x = padX + w * index / (floatValues.size - 1).coerceAtLeast(1)
                val y = padY + h * (1f - (v - minVal) / range)
                val color = if (index == floatValues.lastIndex) AccentGold else lineColor
                drawCircle(color = color, radius = if (index == floatValues.lastIndex) 5f else 3f, center = Offset(x, y))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val step = (labels.size / 6).coerceAtLeast(1)
            labels.forEachIndexed { index, label ->
                if (index % step == 0 || index == labels.lastIndex) {
                    Text(
                        text = label.take(3),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            text = valueFormatter(values.last()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun BaremDualLineChart(
    points: List<DualYearPoint>,
    primaryLabel: String,
    secondaryLabel: String,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return
    val primary = points.map { it.primary.toFloat() }
    val secondary = points.map { it.secondary.toFloat() }
    val minVal = minOf(primary.minOrNull() ?: 0f, secondary.minOrNull() ?: 0f)
    val maxVal = maxOf(primary.maxOrNull() ?: 1f, secondary.maxOrNull() ?: 1f, minVal + 1f)
    val range = max(maxVal - minVal, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(color = PrimaryBlue, label = primaryLabel)
            LegendDot(color = AccentGold, label = secondaryLabel)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            val padX = 12f
            val padY = 16f
            val w = size.width - padX * 2
            val h = size.height - padY * 2
            fun drawSeries(values: List<Float>, color: Color) {
                if (values.size < 2) return
                val path = Path()
                values.forEachIndexed { index, v ->
                    val x = padX + w * index / (values.size - 1)
                    val y = padY + h * (1f - (v - minVal) / range)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            }
            drawSeries(primary, PrimaryBlue)
            drawSeries(secondary, AccentGold)
        }
        Text(
            text = points.last().year.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun BaremDeductionPieChart(
    breakdown: DeductionBreakdown,
    modifier: Modifier = Modifier,
) {
    val vergiPct = breakdown.gvPct + breakdown.dvPct
    val slices = listOf(
        PieSlice("Prim", breakdown.primPct, Color(0xFF1565C0)),
        PieSlice("Vergi", vergiPct, Color(0xFFC62828)),
        PieSlice("Net", breakdown.netPct, Color(0xFF2E7D32)),
    ).filter { it.percent > 0.0 }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.result_deduction_pie_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (slices.isEmpty()) return@Column
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) {
            val diameter = min(size.width, size.height)
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.percent / 100.0 * 360.0).toFloat()
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                )
                startAngle += sweep
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            slices.forEach { slice ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(12.dp)
                                .background(slice.color, RoundedCornerShape(50)),
                        )
                        Text(
                            text = slice.label,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        text = "%${formatPct(slice.percent)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

private data class PieSlice(val label: String, val percent: Double, val color: Color)

private fun formatPct(value: Double): String {
    val fmt = java.text.NumberFormat.getNumberInstance(Locale("tr", "TR"))
    fmt.minimumFractionDigits = 1
    fmt.maximumFractionDigits = 1
    return fmt.format(value)
}

@Composable
fun BaremDeductionStackChart(
    points: List<DeductionYearPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return
    val recent = points.takeLast(12)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LegendDot(color = Color(0xFF2E7D32), label = "Net")
            LegendDot(color = Color(0xFF1565C0), label = "Prim")
            LegendDot(color = Color(0xFFC62828), label = "GV")
            LegendDot(color = Color(0xFF6A1B9A), label = "DV")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            recent.forEach { point ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    val segments = listOf(
                        point.netPct to Color(0xFF2E7D32),
                        point.primPct to Color(0xFF1565C0),
                        point.gvPct to Color(0xFFC62828),
                        point.dvPct to Color(0xFF6A1B9A),
                    )
                    segments.forEach { (pct, color) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height((pct * 1.6f).dp.coerceAtLeast(2.dp))
                                .background(color, RoundedCornerShape(2.dp)),
                        )
                    }
                    Text(
                        text = point.year.toString().takeLast(2),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(10.dp)
                .background(color, RoundedCornerShape(50)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
