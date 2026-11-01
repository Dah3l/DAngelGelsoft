package com.dangel.gelsoft.ui
import com.dangel.gelsoft.R
import com.dangel.gelsoft.ui.theme.DangelType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bottom navigation with a raised center action button (reservation),
 * following the reference design: floating dark rounded bar, the center
 * circle in brand teal elevated above the bar.
 *
 * Tabs: 0 Inicio · 1 Operaciones · 2 En acción · 3 Soldado.
 */
@Composable
fun DangelBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onReserve: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val teal = MaterialTheme.colorScheme.primary
    val items = listOf(
        BottomTab("Inicio", 0) { c, m -> Icon(Icons.Filled.Home, null, tint = c, modifier = m) },
        BottomTab("Operaciones", 1) { c, m ->
            Icon(Icons.Filled.AttachMoney, null, tint = c, modifier = m)
        },
        BottomTab("En acción", 2) { c, m ->
            Icon(Icons.Filled.PhotoLibrary, null, tint = c, modifier = m)
        },
        BottomTab("Soldado", 3) { c, m ->
            Icon(
                painter = painterResource(id = R.drawable.ic_helmet),
                contentDescription = null,
                tint = c,
                modifier = m,
            )
        },
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Color(0xFF3A3A3A), RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left tabs.
            for (tab in items.take(2)) {
                BarItem(
                    tab = tab,
                    selected = selectedTab == tab.index,
                    teal = teal,
                    onClick = { onTabSelected(tab.index) },
                    modifier = Modifier.weight(1f),
                )
            }
            // Spacer for the center button.
            Box(modifier = Modifier.size(64.dp))
            // Right tabs.
            for (tab in items.drop(2)) {
                BarItem(
                    tab = tab,
                    selected = selectedTab == tab.index,
                    teal = teal,
                    onClick = { onTabSelected(tab.index) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        // Raised center reservation button.
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(64.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(teal)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onReserve,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.EventAvailable,
                contentDescription = "Reservar",
                tint = Color.Black,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

private data class BottomTab(
    val label: String,
    val index: Int,
    val icon: @Composable (color: Color, modifier: Modifier) -> Unit,
)

@Composable
private fun BarItem(
    tab: BottomTab,
    selected: Boolean,
    teal: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) teal else Color(0xFF8E8E8E)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        tab.icon(color, Modifier.size(24.dp))
        Text(
            tab.label,
            style = DangelType.Tiny,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = color,
        )
    }
}
