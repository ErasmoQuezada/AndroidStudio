package com.example.amiot.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amiot.data.NewsItemFirestore
import com.example.amiot.ui.components.Logo
import com.example.amiot.viewmodel.NewsViewModel
import com.example.amiot.viewmodel.NewsViewModelFactory
import java.util.*

data class NewsItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val fullContent: String = "",
    val date: String,
    val category: String,
    val isUserCreated: Boolean = false
)

@Composable
fun NewsScreen(
    onNewsClick: (NewsItem) -> Unit,
    onAddNewsClick: () -> Unit
) {
    val context = LocalContext.current
    val application = remember { context.applicationContext as android.app.Application }
    val viewModel: NewsViewModel = viewModel(factory = NewsViewModelFactory(application))
    val userNews by viewModel.userNews.collectAsState()
    
    val defaultNews = rememberNewsItems()
    val allNews = remember(defaultNews, userNews) {
        val userNewsItems = userNews.map { it.toNewsItem() }
        (userNewsItems + defaultNews).sortedByDescending { 
            when (it.date) {
                "Hace unos momentos" -> 0
                "Hace 2 horas" -> 1
                "Hace 5 horas" -> 2
                "Hace 1 día" -> 3
                "Hace 2 días" -> 4
                "Hace 3 días" -> 5
                "Hace 4 días" -> 6
                "Hace 5 días" -> 7
                "Hace 1 semana" -> 8
                else -> 9
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Logo(modifier = Modifier.size(60.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Noticias de Chile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mantente informado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // News List
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                items(allNews) { news ->
                    NewsCard(
                        news = news,
                        onClick = { onNewsClick(news) }
                    )
                }
            }
            
            // Floating Action Button
            FloatingActionButton(
                onClick = onAddNewsClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Noticia"
                )
            }
        }
    }
}

fun NewsItemFirestore.toNewsItem(): NewsItem {
    return NewsItem(
        id = this.id,
        title = this.title,
        content = this.content,
        fullContent = this.fullContent,
        date = this.date,
        category = this.category,
        isUserCreated = this.isUserCreated
    )
}

@Composable
fun NewsCard(
    news: NewsItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (news.isUserCreated) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = "Noticia",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = news.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = news.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = news.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Leer más", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun rememberNewsItems(): List<NewsItem> {
    return listOf(
        NewsItem(
            title = "Chile lidera en energías renovables en Latinoamérica",
            content = "Chile se posiciona como uno de los países líderes en la transición hacia energías renovables en la región. El país ha alcanzado un 40% de su matriz energética proveniente de fuentes limpias, superando las metas establecidas para 2025.",
            date = "Hace 2 horas",
            category = "Tecnología"
        ),
        NewsItem(
            title = "Nuevo proyecto de IoT en la agricultura chilena",
            content = "Se lanza un innovador proyecto que utiliza sensores IoT para optimizar el riego y monitoreo de cultivos en el Valle Central. Esta tecnología permitirá a los agricultores reducir el consumo de agua en un 30%.",
            date = "Hace 5 horas",
            category = "Innovación"
        ),
        NewsItem(
            title = "Santiago implementa sistema inteligente de transporte",
            content = "La capital chilena anuncia la implementación de un sistema de transporte público inteligente que utilizará sensores IoT para optimizar rutas y reducir tiempos de espera. El proyecto comenzará en las comunas del sector oriente.",
            date = "Hace 1 día",
            category = "Ciudad"
        ),
        NewsItem(
            title = "Chile avanza en digitalización de servicios públicos",
            content = "El gobierno anuncia nuevas plataformas digitales que facilitarán el acceso a servicios públicos. Se espera que más de 2 millones de chilenos se beneficien de estos avances tecnológicos en los próximos meses.",
            date = "Hace 2 días",
            category = "Gobierno"
        ),
        NewsItem(
            title = "Startups chilenas destacan en feria tecnológica internacional",
            content = "Cinco startups chilenas fueron reconocidas en la feria tecnológica más importante de Latinoamérica. Las empresas se enfocan en soluciones IoT para minería, agricultura y ciudades inteligentes.",
            date = "Hace 3 días",
            category = "Negocios"
        ),
        NewsItem(
            title = "Proyecto de monitoreo ambiental con IoT en la Patagonia",
            content = "Científicos chilenos implementan una red de sensores IoT en la Patagonia para monitorear el cambio climático y la biodiversidad. El proyecto es el más grande de su tipo en Sudamérica.",
            date = "Hace 4 días",
            category = "Medio Ambiente"
        ),
        NewsItem(
            title = "Chile desarrolla tecnología IoT para la minería",
            content = "Empresas mineras chilenas adoptan soluciones IoT para mejorar la seguridad y eficiencia en sus operaciones. Se espera reducir accidentes en un 25% y aumentar la productividad.",
            date = "Hace 5 días",
            category = "Minería"
        ),
        NewsItem(
            title = "Valparaíso se convierte en ciudad inteligente",
            content = "Valparaíso inicia su transformación hacia una ciudad inteligente con la instalación de sensores IoT para gestión de residuos, iluminación pública y monitoreo de calidad del aire. El proyecto será un modelo para otras ciudades portuarias.",
            date = "Hace 1 semana",
            category = "Ciudad"
        )
    )
}

