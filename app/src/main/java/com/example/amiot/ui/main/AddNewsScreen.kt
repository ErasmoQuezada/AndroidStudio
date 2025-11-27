package com.example.amiot.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddNewsScreen(
    onSaveClick: (String, String, String, String, String) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var shortContent by remember { mutableStateOf("") }
    var fullContent by remember { mutableStateOf("") }
    
    val categories = listOf("Tecnología", "Innovación", "Ciudad", "Gobierno", "Negocios", "Medio Ambiente", "Minería", "Educación", "Salud", "Deportes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Noticia") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Comparte tu noticia sobre Chile",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la noticia") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = "Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            
            var expanded by remember { mutableStateOf(false) }
            var categoryDropdownAnchor by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
            
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category.ifEmpty { "Selecciona una categoría" },
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Categoría") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = "Categoría") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Seleccionar categoría"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            categoryDropdownAnchor = coordinates
                        },
                    singleLine = true,
                    colors = if (category.isEmpty()) {
                        OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        OutlinedTextFieldDefaults.colors()
                    }
                )
                
                categoryDropdownAnchor?.let {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            OutlinedTextField(
                value = shortContent,
                onValueChange = { shortContent = it },
                label = { Text("Resumen (aparece en la lista)") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = "Resumen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            
            OutlinedTextField(
                value = fullContent,
                onValueChange = { fullContent = it },
                label = { Text("Contenido completo") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = "Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (title.isNotBlank() && category.isNotBlank() && shortContent.isNotBlank() && fullContent.isNotBlank()) {
                        val date = "Hace unos momentos"
                        onSaveClick(title, shortContent, fullContent, category, date)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && title.isNotBlank() && category.isNotBlank() && 
                         shortContent.isNotBlank() && fullContent.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Publicar Noticia", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

