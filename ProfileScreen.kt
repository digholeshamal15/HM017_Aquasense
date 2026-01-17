// ProfileScreen.kt
// Create this file in: app/src/main/java/com/example/health/

package com.example.health

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val user = UserManager.currentUser ?: return

    var showEditDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6A1B9A),
                        Color(0xFF8E24AA).copy(alpha = 0.6f),
                        Color.White
                    )
                )
            )
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Profile", fontWeight = FontWeight.Bold, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF8E24AA),
                                Color(0xFFAB47BC)
                            )
                        )
                    )
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user.name.first().uppercase(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                user.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                "@${user.username}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Personal Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileInfoRow(
                        icon = Icons.Default.Person,
                        label = "Name",
                        value = user.name
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    ProfileInfoRow(
                        icon = Icons.Default.AccountCircle,
                        label = "Username",
                        value = user.username
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    ProfileInfoRow(
                        icon = Icons.Default.Cake,
                        label = "Date of Birth",
                        value = user.dateOfBirth
                    )

                    if (user.email.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = user.email
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Theme Setting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showThemeDialog = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = "Theme",
                            tint = Color(0xFF6A1B9A)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                UserManager.currentTheme.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, "Change", tint = Color(0xFF6A1B9A))
                    }

                    HorizontalDivider()

                    // Edit Profile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEditDialog = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF6A1B9A)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Edit Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ChevronRight, "Edit", tint = Color(0xFF6A1B9A))
                    }

                    HorizontalDivider()

                    // Logout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Logout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ChevronRight, "Logout", tint = Color(0xFFD32F2F))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme", color = Color(0xFF6A1B9A)) },
            text = {
                Column {
                    ThemePreference.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    UserManager.changeTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = UserManager.currentTheme == theme,
                                onClick = {
                                    UserManager.changeTheme(theme)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF6A1B9A)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close", color = Color(0xFF6A1B9A))
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        var editedName by remember { mutableStateOf(user.name) }
        var editedEmail by remember { mutableStateOf(user.email) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", color = Color(0xFF6A1B9A)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6A1B9A),
                            focusedLabelColor = Color(0xFF6A1B9A),
                            cursorColor = Color(0xFF6A1B9A)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedEmail,
                        onValueChange = { editedEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6A1B9A),
                            focusedLabelColor = Color(0xFF6A1B9A),
                            cursorColor = Color(0xFF6A1B9A)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedUser = user.copy(
                            name = editedName,
                            email = editedEmail
                        )
                        UserManager.updateUserProfile(updatedUser)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A1B9A)
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color(0xFF6A1B9A))
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", color = Color(0xFFD32F2F)) },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        UserManager.logout()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF6A1B9A))
                }
            }
        )
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color(0xFF6A1B9A),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}