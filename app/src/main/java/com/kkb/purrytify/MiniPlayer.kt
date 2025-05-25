package com.kkb.purrytify

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kkb.purrytify.R
import com.kkb.purrytify.util.MediaPlayerManager
import com.kkb.purrytify.viewmodel.SongViewModel
import kotlinx.coroutines.delay

data class AudioDeviceOption(
    val id: Int,
    val name: String,
    val type: Int
)

@Composable
fun MiniPlayer(
    currentSong: UserSong,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit
) {
    val viewModel = hiltViewModel<SongViewModel>()
    val context = LocalContext.current
    var playbackProgress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(1f) }
    var lastReportedSeconds by remember { mutableStateOf(0L) }
    var showAudioDeviceMenu by remember { mutableStateOf(false) }
    val audioDevices = remember { getAvailableAudioDevices(context) }
    var selectedDeviceId by remember { mutableStateOf(-1) }

    // Update progress while playing
    LaunchedEffect(isPlaying, currentSong.songId) {
        lastReportedSeconds = 0L
        while (isPlaying) {
            val player = MediaPlayerManager.getPlayer()
            if (player != null && player.isPlaying) {
                val current = player.currentPosition
                val total = player.duration.takeIf { it > 0 } ?: 1
                playbackProgress = current.toFloat() / total
                duration = total.toFloat()
                val seconds = (player.currentPosition / 1000L)
                if (seconds > lastReportedSeconds) {
                    val delta = seconds - lastReportedSeconds
                    if (delta > 0) {
                        viewModel.updateTimeListened(currentSong.songId, delta)
                        lastReportedSeconds = seconds
                    }
                }
            }
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF1E1E1E))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Album Art
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(currentSong.coverPath)
                        .placeholder(R.drawable.album_placeholder)
                        .error(R.drawable.album_placeholder)
                        .build()
                ),
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Song Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currentSong.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = currentSong.artist,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Audio output selector button
            IconButton(onClick = { showAudioDeviceMenu = true }) {
                Icon(
                    imageVector = Icons.Default.Headset,
                    contentDescription = "Select Audio Output",
                    tint = Color.White
                )
            }

            // Favorite Button
            val songState by viewModel.userSongList.collectAsState()
            val song = songState.find { it.songId == currentSong.songId }
            val isLiked = song?.isLiked ?: false
            IconButton(onClick = {
                viewModel.toggleLike(currentSong.songId)
            }) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    tint = if (isLiked) Color.Red else Color.White
                )
            }

            // Play/Pause Button
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }
        }

        // Audio Device Selection Dropdown
        if (showAudioDeviceMenu) {
            AudioDeviceSelectionDialog(
                devices = audioDevices,
                selectedDeviceId = selectedDeviceId,
                onDeviceSelected = { deviceId ->
                    selectedDeviceId = deviceId
                    setAudioOutputDevice(context, deviceId, audioDevices)
                    showAudioDeviceMenu = false
                },
                onDismiss = { showAudioDeviceMenu = false }
            )
        }
    }

    // Progress Bar as a line
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(playbackProgress)
                .background(Color.White)
        )
    }
}

@Composable
fun AudioDeviceSelectionDialog(
    devices: List<AudioDeviceOption>,
    selectedDeviceId: Int,
    onDeviceSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Audio Output") },
        text = {
            Column {
                devices.forEach { device ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDeviceSelected(device.id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (device.type) {
                                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> Icons.Default.Speaker
                                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> Icons.Default.Headset
                                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> Icons.Default.Bluetooth
                                else -> Icons.Default.DeviceUnknown
                            },
                            contentDescription = null,
                            tint = if (selectedDeviceId == device.id) Color.Blue else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = device.name,
                            color = if (selectedDeviceId == device.id) Color.Blue else Color.Black
                        )
                        if (selectedDeviceId == device.id) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Blue
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getAvailableAudioDevices(context: Context): List<AudioDeviceOption> {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val devices = mutableListOf<AudioDeviceOption>()

    // Always add the default speaker
    devices.add(AudioDeviceOption(
        id = -1,
        name = "Phone Speaker",
        type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
    ))

    // Get connected devices on newer Android versions
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        audioDevices.forEach { device ->
            // Filter to only include useful audio output devices
            if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER ||
                device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {

                devices.add(AudioDeviceOption(
                    id = device.id,
                    name = device.productName.toString(),
                    type = device.type
                ))
            }
        }
    }

    return devices
}

fun setAudioOutputDevice(context: Context, deviceId: Int, devices: List<AudioDeviceOption>) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    if (deviceId == -1) {
        // Default speaker
        audioManager.isSpeakerphoneOn = true
        audioManager.mode = AudioManager.MODE_NORMAL
    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        val selectedDevice = devices.find { it.id == deviceId }
        if (selectedDevice != null) {
            when (selectedDevice.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                    audioManager.isSpeakerphoneOn = false
                    audioManager.mode = AudioManager.MODE_NORMAL
                }
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                    audioManager.startBluetoothSco()
                    audioManager.isBluetoothScoOn = true
                    audioManager.isSpeakerphoneOn = false
                }
                else -> {
                    audioManager.isSpeakerphoneOn = true
                    audioManager.mode = AudioManager.MODE_NORMAL
                }
            }
        }
    }
}