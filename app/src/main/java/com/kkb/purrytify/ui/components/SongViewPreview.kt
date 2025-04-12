package com.kkb.purrytify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kkb.purrytify.UserSong
import com.kkb.purrytify.data.model.Song
import java.time.LocalDateTime

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SongViewPreview() {
    val defaultSong = UserSong(
        userId=1,
        songId = 1,
        title = "RATHER LIE jodjs osdkoadskokdsoaskdoskdsa oksdpskapsdkds pkdaspsdkapdkas dsap kdspdaskdspakdas pkksdp dsapk sdpk",
        artist = "Playboi Carti",
        filePath = "",
        coverPath = null ,// simulate missing cover image,
         isLiked= true,
        createdAt=LocalDateTime.now(),
        lastPlayed=LocalDateTime.now(),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        SongViewBig(song = defaultSong)
        SongView(song = defaultSong)
    }
}
