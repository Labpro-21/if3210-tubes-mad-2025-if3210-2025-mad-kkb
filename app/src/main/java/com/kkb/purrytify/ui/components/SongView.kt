package com.kkb.purrytify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kkb.purrytify.R
import com.kkb.purrytify.data.model.Song


@Composable
fun SongView(song: Song, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val painter = rememberAsyncImagePainter(
        model = song.coverPath ?: R.drawable.album_placeholder
    )

    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(end = 16.dp)
            .width(120.dp)
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )
        Text(song.title, color = Color.White, fontSize = 14.sp)
        Text(song.artist, color = Color.Gray, fontSize = 12.sp)
    }
}
