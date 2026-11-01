package com.dangel.gelsoft.ui
import com.dangel.gelsoft.ui.theme.DangelType
import androidx.compose.foundation.background
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
 * Tabs: 0 Inicio · 1 Precios · 2 Galería · 3 Perfil.
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
        BottomTab("Inicio", Icons.Filled.Home, 0),
        BottomTab("Precios", Icons.Filled.AttachMoney, 1),
        BottomTab("Galería", Icons.Filled.PhotoLibrary, 2),
        BottomTab("Perfil", Icons.Filled.Person, 3),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
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

private data class BottomTab(val label: String, val icon: ImageVector, val index: Int)

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
        Icon(tab.icon, contentDescription = tab.label, tint = color, modifier = Modifier.size(24.dp))
        Text(
            tab.label,
            style = DangelType.Tiny,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = color,
        )
    }
}
