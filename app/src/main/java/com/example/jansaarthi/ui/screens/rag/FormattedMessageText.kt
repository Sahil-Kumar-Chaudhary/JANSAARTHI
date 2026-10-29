package com.example.jansaarthi.ui.screens.rag

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Sealed structure representing parsed semantic blocks from an AI response.
 */
sealed class MessageBlock {
    data class Heading(val text: String, val level: Int) : MessageBlock()
    data class BulletItem(val text: String) : MessageBlock()
    data class NumberedItem(val number: String, val text: String) : MessageBlock()
    data class TableCard(val title: String, val entries: List<Pair<String, String>>) : MessageBlock()
    data class Paragraph(val text: String) : MessageBlock()
}

/**
 * Parses raw inline Markdown formatting (**bold**, *italic*, `code`) into a Jetpack Compose AnnotatedString.
 * Gracefully strips unclosed or extraneous formatting artifacts so no raw markdown syntax leaks to the user.
 */
fun parseInlineMarkdown(
    rawText: String
): AnnotatedString {
    if (rawText.isBlank()) return AnnotatedString("")

    // Regex to match **bold**, *italic*, or `code`
    val tokenRegex = Regex("""(\*\*.*?\*\*|\*.*?\*|`.*?`)""")
    val builder = AnnotatedString.Builder()

    var lastIndex = 0
    tokenRegex.findAll(rawText).forEach { matchResult ->
        val range = matchResult.range
        // Append plain text before token
        if (range.first > lastIndex) {
            val plain = rawText.substring(lastIndex, range.first)
            builder.append(cleanMarkdownArtifacts(plain))
        }

        val token = matchResult.value
        when {
            token.startsWith("**") && token.endsWith("**") && token.length >= 4 -> {
                val inner = token.substring(2, token.length - 2)
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                builder.append(cleanMarkdownArtifacts(inner))
                builder.pop()
            }
            token.startsWith("*") && token.endsWith("*") && token.length >= 2 -> {
                val inner = token.substring(1, token.length - 1)
                builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                builder.append(cleanMarkdownArtifacts(inner))
                builder.pop()
            }
            token.startsWith("`") && token.endsWith("`") && token.length >= 2 -> {
                val inner = token.substring(1, token.length - 1)
                builder.pushStyle(SpanStyle(fontFamily = FontFamily.Monospace))
                builder.append(cleanMarkdownArtifacts(inner))
                builder.pop()
            }
            else -> {
                builder.append(cleanMarkdownArtifacts(token))
            }
        }
        lastIndex = range.last + 1
    }

    if (lastIndex < rawText.length) {
        val remaining = rawText.substring(lastIndex)
        builder.append(cleanMarkdownArtifacts(remaining))
    }

    return builder.toAnnotatedString()
}

/**
 * Strips remaining unparsed markdown artifacts (e.g. unclosed asterisks, pipe characters, trailing dashes).
 */
fun cleanMarkdownArtifacts(text: String): String {
    var result = text.replace(Regex("""\*{3,}"""), "")
    if (result.trim().startsWith("|") || result.trim().endsWith("|")) {
        result = result.replace(Regex("""^\s*\|\s*"""), "").replace(Regex("""\s*\|\s*$"""), "")
    }
    return result
}

/**
 * Splits raw message text into structured MessageBlocks for clean Material 3 presentation.
 * Detects headings, bullet items, numbered items, and transforms wide Markdown tables into mobile-friendly cards.
 */
fun parseMessageBlocks(rawText: String): List<MessageBlock> {
    if (rawText.isBlank()) return emptyList()

    val lines = rawText.lines()
    val blocks = mutableListOf<MessageBlock>()

    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()

        if (line.isEmpty()) {
            i++
            continue
        }

        // 1. Markdown Table Detection
        if (line.startsWith("|") && line.endsWith("|")) {
            val tableLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                tableLines.add(lines[i].trim())
                i++
            }

            if (tableLines.size >= 2) {
                // Parse headers
                val headers = tableLines[0]
                    .split("|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                // Skip line 1 if it's separator (|---|---|)
                val dataStartIndex = if (tableLines.size > 1 && tableLines[1].replace("-", "").replace("|", "").trim().isEmpty()) 2 else 1

                for (rowIdx in dataStartIndex until tableLines.size) {
                    val cells = tableLines[rowIdx]
                        .split("|")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    if (cells.isNotEmpty()) {
                        val title = cells[0]
                        val entries = mutableListOf<Pair<String, String>>()
                        for (c in 1 until cells.size) {
                            val header = if (c < headers.size) headers[c] else ""
                            entries.add(Pair(header, cells[c]))
                        }
                        blocks.add(MessageBlock.TableCard(title = title, entries = entries))
                    }
                }
            }
            continue
        }

        // 2. Headings (# Heading, ## Heading, ### Heading)
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val headingText = line.drop(level).trim()
            blocks.add(MessageBlock.Heading(text = headingText, level = level))
            i++
            continue
        }

        // 3. Standalone bold line as heading (e.g. "**Financial Grants for Women Entrepreneurs**" or "**Notes**")
        if (line.startsWith("**") && line.endsWith("**") && line.length > 4 && !line.substring(2, line.length - 2).contains("**")) {
            val headingText = line.substring(2, line.length - 2).trim()
            blocks.add(MessageBlock.Heading(text = headingText, level = 3))
            i++
            continue
        }

        // 4. Bullet Item (- text, * text, • text)
        if (line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ")) {
            val itemText = line.substring(2).trim()
            blocks.add(MessageBlock.BulletItem(text = itemText))
            i++
            continue
        }

        // 5. Numbered Item (1. text, 2. text)
        val numberedMatch = Regex("""^(\d+)\.\s+(.*)""").matchEntire(line)
        if (numberedMatch != null) {
            val num = numberedMatch.groupValues[1]
            val itemText = numberedMatch.groupValues[2]
            blocks.add(MessageBlock.NumberedItem(number = num, text = itemText))
            i++
            continue
        }

        // 6. Standard Paragraph
        val paragraphLines = mutableListOf<String>()
        while (i < lines.size) {
            val nextLine = lines[i].trim()
            if (nextLine.isEmpty() ||
                nextLine.startsWith("|") ||
                nextLine.startsWith("#") ||
                nextLine.startsWith("- ") ||
                nextLine.startsWith("* ") ||
                nextLine.startsWith("• ") ||
                Regex("""^\d+\.\s+""").containsMatchIn(nextLine) ||
                (nextLine.startsWith("**") && nextLine.endsWith("**") && !nextLine.substring(2, nextLine.length - 2).contains("**"))
            ) {
                break
            }
            paragraphLines.add(nextLine)
            i++
        }

        if (paragraphLines.isNotEmpty()) {
            blocks.add(MessageBlock.Paragraph(text = paragraphLines.joinToString(" ")))
        }
    }

    return blocks
}

/**
 * Composable that formats and renders an AI RAG answer cleanly using Jetpack Compose Material 3.
 * Supports headings, bullet lists, numbered lists, key-value table cards, and styled inline bold/italic.
 * Works seamlessly across Light and Dark themes, English and Hindi.
 */
@Composable
fun FormattedMessageText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val blocks = parseMessageBlocks(text)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MessageBlock.Heading -> {
                    val typo = when (block.level) {
                        1 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        2 -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        else -> MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = parseInlineMarkdown(block.text),
                        style = typo.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                is MessageBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MessageBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MessageBlock.TableCard -> {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = parseInlineMarkdown(block.title),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            block.entries.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (label.isNotBlank()) {
                                        Text(
                                            text = "$label: ",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                    Text(
                                        text = parseInlineMarkdown(value),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                            }
                        }
                    }
                }

                is MessageBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
