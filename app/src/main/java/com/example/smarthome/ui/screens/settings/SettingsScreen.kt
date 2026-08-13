package com.example.smarthome.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.ui.components.ScreenHeader
import com.example.smarthome.ui.components.SectionTitle
import com.example.smarthome.ui.theme.DarkBrown
import com.example.smarthome.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ScreenHeader(
                    title = "Profile",
                    subtitle = "Manage your account\nand preferences"
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture / Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD0B29A).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile Picture",
                            tint = DarkBrown,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Name
                    Text(
                        text = uiState.user?.displayName ?: "Guest User",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkBrown
                    )
                    
                    // Email
                    Text(
                        text = uiState.user?.email ?: "guest@example.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Edit Profile Button
                    OutlinedButton(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFD0B29A)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBrown)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Profile",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                SectionTitle(text = "Settings")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Account Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkBrown
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "View and update your personal details and app preferences.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var displayName by remember(uiState.user) { mutableStateOf(uiState.user?.displayName ?: "") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = DarkBrown, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBrown)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD0B29A).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = DarkBrown,
                    modifier = Modifier.size(70.dp)
                )
                
                // Camera icon overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp),
                    shape = CircleShape,
                    color = DarkBrown
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Change picture",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBrown,
                    focusedLabelColor = DarkBrown
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = uiState.user?.email ?: "",
                onValueChange = { },
                label = { Text("Email (Cannot be changed)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    isLoading = true
                    viewModel.updateProfile(displayName) {
                        isLoading = false
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBrown),
                enabled = !isLoading && displayName.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
