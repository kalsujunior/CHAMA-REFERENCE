package com.kalsu.relive

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun extractExplicitMemory(message: String): String? {
    val text = message.trim()
    val starts = listOf(
        "chama, retiens que ", "retiens que ", "souviens-toi que ",
        "souviens toi que ", "mémorise que ", "memorise que ",
        "n'oublie pas que ", "n’oublie pas que ", "chama, sonje ke ",
        "sonje ke ", "pa bliye ke ", "kenbe nan memwa ou ke "
    )
    val prefix = starts.firstOrNull { text.startsWith(it, ignoreCase = true) } ?: return null
    return text.substring(prefix.length).trim().takeIf { it.isNotBlank() }
}

@Composable
fun MarkdownMessageText(value: String, modifier: Modifier = Modifier) {
    val annotated = remember(value) {
        buildAnnotatedString {
            val marker = "**"
            var cursor = 0
            while (cursor < value.length) {
                val start = value.indexOf(marker, cursor)
                if (start < 0) {
                    append(value.substring(cursor))
                    break
                }
                val end = value.indexOf(marker, start + marker.length)
                if (end < 0) {
                    append(value.substring(cursor))
                    break
                }
                append(value.substring(cursor, start))
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(value.substring(start + marker.length, end))
                pop()
                cursor = end + marker.length
            }
        }
    }
    Text(annotated, color = Color(0xFFF5EBDD), modifier = modifier)
}
