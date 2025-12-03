package com.tewelde.stdout.core.designsystem.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.tewelde.stdout.core.model.Story

@Composable
fun StoryItem(
    story: Story,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    showComments: Boolean = true,
    onCommentClick: () -> Unit = {},
    onUrlClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Whole item click -> Detail with comments (passed as onClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Image on the left
        if (!story.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = story.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
                    .clickable { onUrlClick() }, // Image click -> Open URL
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder or empty box if no image
             Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
                    .clickable { onUrlClick() },
                 contentAlignment = Alignment.Center
            ) {
                 Text("No Image", color = Color.Gray, fontSize = 10.sp)
             }
        }

        // Details on the right
        Column(
            modifier = Modifier.weight(1f)
        ) {
            if (showTitle) {
                Text(
                    text = story.title,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onUrlClick() } // Title click -> Open URL
                )
            }

            if (!story.siteName.isNullOrEmpty()) {
                Text(
                    text = story.siteName ?: "",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (!story.url.isNullOrEmpty()) {
                 Text(
                    text = story.url!!.substringAfter("://").substringBefore("/"),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = story.score.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
                FullStop()
                Text(
                    text = "@${story.by}",
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                FullStop()
                RelativeTimeText(
                    timestamp = story.time,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                if (showComments) {
                    FullStop()
                    Text(
                        text = "${story.descendants ?: 0} comments",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable(onClick = onCommentClick) // Comment click -> Open comments (Detail)
                    )
                }
            }
        }
    }
}

@Composable
fun FullStop() {
    Text(
        text = "::",
        color = MaterialTheme.colorScheme.secondary,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp
    )
}
