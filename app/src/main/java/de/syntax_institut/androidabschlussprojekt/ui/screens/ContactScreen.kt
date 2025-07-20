package de.syntax_institut.androidabschlussprojekt.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import de.syntax_institut.androidabschlussprojekt.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ContactScreen(navController: NavController) {
    val context = LocalContext.current

    var imageVisible by remember { mutableStateOf(false) }
    var iconsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        imageVisible = true
        delay(300)
        iconsVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AnimatedVisibility(
                visible = imageVisible,
                enter = fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.7f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cv),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Eduardo Rodrigues da Cruz",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Android & iOS Developer",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "I'm a dedicated Android Developer driven by a passion for building user-friendly and impactful mobile applications. I specialize in designing and developing elegant solutions that prioritize both aesthetic appeal and efficient functionality.\n" +
                        "\n" +
                        "For collaborations, inquiries, or to connect with me professionally, please reach out via my social media links provided below.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = iconsVisible,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(500))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconLink(
                        icon = painterResource(id = R.drawable.ic_linkedin),
                        url = "https://www.linkedin.com/in/eduardo-rodriguescruz/",
                        contentDescription = "LinkedIn"
                    )
                    IconLink(
                        icon = painterResource(id = R.drawable.ic_github),
                        url = "https://github.com/eduSwift?tab=overview&from=2025-07-01&to=2025-07-19",
                        contentDescription = "GitHub"
                    )
                }
            }
        }
    }
}

@Composable
fun IconLink(icon: Painter, url: String, contentDescription: String) {
    val context = LocalContext.current
    Icon(
        painter = icon,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(36.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
        tint = MaterialTheme.colorScheme.primary
    )
}