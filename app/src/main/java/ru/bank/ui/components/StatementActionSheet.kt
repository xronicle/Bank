package ru.bank.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.bank.ui.theme.Accent
import ru.bank.ui.theme.AccentSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementActionSheet(
    fileName: String,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onEmail: () -> Unit,
    onMessenger: () -> Unit,
    onMoreOptions: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text("Выписка готова", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(2.dp))
            Text(
                fileName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))

            ActionRow(Icons.Outlined.Download, "Скачать", "Сохранить в «Загрузки» на устройстве", onDownload)
            ActionRow(Icons.Outlined.Email, "Отправить по почте", "Через установленное почтовое приложение", onEmail)
            ActionRow(Icons.Outlined.Chat, "Отправить в мессенджер", "WhatsApp, Telegram и другие", onMessenger)
            ActionRow(Icons.Outlined.Share, "Ещё способы", "Полный системный список приложений", onMoreOptions)
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).background(AccentSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
