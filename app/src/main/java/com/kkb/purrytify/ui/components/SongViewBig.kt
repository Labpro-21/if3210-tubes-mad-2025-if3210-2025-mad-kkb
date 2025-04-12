package com.kkb.purrytify.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kkb.purrytify.R
import com.kkb.purrytify.UserSong
import com.kkb.purrytify.data.model.Song

@Composable
fun SongViewBig(song: UserSong, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Column( modifier = modifier.clickable { onClick() } .padding(horizontal = 10.dp, vertical = 10.dp) .width(120.dp) ) {
        AsyncImage( model = song.coverPath, contentDescription = null, modifier = Modifier .height(120.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
            placeholder = painterResource(R.drawable.album_placeholder),
            error = painterResource(R.drawable.album_placeholder) )
        Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(song.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}