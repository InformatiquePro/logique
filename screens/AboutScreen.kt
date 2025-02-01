package fr.charleselie.logique.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.charleselie.logique.BuildConfig
import fr.charleselie.logique.R

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/InformatiquePro/logique"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Ce logiciel est développé par Charles-Elie\nCrédit logo de l'app : DALL-E (chat GPT). Cette application est gratuite et open-source.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Son dépôt GitHub : ",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = githubUrl,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
                
                Text(
                    text = "\nVersion ${BuildConfig.VERSION_NAME}",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
} 