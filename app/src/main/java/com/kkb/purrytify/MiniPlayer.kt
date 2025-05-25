package com.kkb.purrytify

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaRouter
import android.os.Build
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
    val type: Int,
    val isDefault: Boolean = false
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
    var audioDevices by remember { mutableStateOf(emptyList<AudioDeviceOption>()) }
    var selectedDeviceId by remember { mutableStateOf(-1) }

    // Initialize audio devices
    LaunchedEffect(Unit) {
        audioDevices = getAvailableAudioDevices(context)
        selectedDeviceId = getCurrentAudioDevice(context)
    }

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

    Column {
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
                IconButton(onClick = {
                    audioDevices = getAvailableAudioDevices(context)
                    selectedDeviceId = getCurrentAudioDevice(context)
                    showAudioDeviceMenu = true
                }) {
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
        }

        // Progress Bar
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

        // Audio Device Selection Dialog
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
        title = {
            Text(
                text = "Select Audio Output",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                devices.forEach { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onDeviceSelected(device.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedDeviceId == device.id)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getDeviceIcon(device.type),
                                contentDescription = null,
                                tint = if (selectedDeviceId == device.id)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedDeviceId == device.id)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                if (device.isDefault) {
                                    Text(
                                        text = "Default",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (selectedDeviceId == device.id) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

fun getDeviceIcon(deviceType: Int): androidx.compose.ui.graphics.vector.ImageVector {
    return when (deviceType) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> Icons.Default.Speaker
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> Icons.Default.Headset
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> Icons.Default.Bluetooth
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_USB_DEVICE -> Icons.Default.Usb
        else -> Icons.Default.VolumeUp
    }
}

fun getAvailableAudioDevices(context: Context): List<AudioDeviceOption> {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val devices = mutableListOf<AudioDeviceOption>()

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

            // Add phone speaker as default
            val hasBuiltinSpeaker = audioDevices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
            if (!hasBuiltinSpeaker) {
                devices.add(AudioDeviceOption(
                    id = -1,
                    name = "Phone Speaker",
                    type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                    isDefault = true
                ))
            }

            audioDevices.forEach { device ->
                when (device.type) {
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_USB_HEADSET,
                    AudioDeviceInfo.TYPE_USB_DEVICE -> {
                        val deviceName = device.productName?.toString() ?: getDefaultDeviceName(device.type)
                        devices.add(AudioDeviceOption(
                            id = device.id,
                            name = deviceName,
                            type = device.type,
                            isDefault = device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                        ))
                    }
                }
            }
        } else {
            // Fallback for older Android versions
            devices.add(AudioDeviceOption(
                id = -1,
                name = "Phone Speaker",
                type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                isDefault = true
            ))

            if (audioManager.isWiredHeadsetOn) {
                devices.add(AudioDeviceOption(
                    id = -2,
                    name = "Wired Headset",
                    type = AudioDeviceInfo.TYPE_WIRED_HEADSET
                ))
            }

            if (audioManager.isBluetoothA2dpOn) {
                devices.add(AudioDeviceOption(
                    id = -3,
                    name = "Bluetooth Audio",
                    type = AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                ))
            }
        }
    } catch (e: Exception) {
        // Fallback in case of any errors
        devices.clear()
        devices.add(AudioDeviceOption(
            id = -1,
            name = "Phone Speaker",
            type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            isDefault = true
        ))
    }

    return devices.distinctBy { it.name }
}

fun getDefaultDeviceName(deviceType: Int): String {
    return when (deviceType) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Phone Speaker"
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Phone Earpiece"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth Audio"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB Headset"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB Audio Device"
        else -> "Audio Device"
    }
}

fun getCurrentAudioDevice(context: Context): Int {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    return when {
        audioManager.isBluetoothA2dpOn -> -3
        audioManager.isWiredHeadsetOn -> -2
        else -> -1
    }
}

fun setAudioOutputDevice(context: Context, deviceId: Int, devices: List<AudioDeviceOption>) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    try {
        when (deviceId) {
            -1 -> {
                // Phone speaker
                audioManager.isSpeakerphoneOn = true
                audioManager.isBluetoothScoOn = false
                audioManager.mode = AudioManager.MODE_NORMAL
            }
            -2 -> {
                // Wired headset (legacy)
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = false
                audioManager.mode = AudioManager.MODE_NORMAL
            }
            -3 -> {
                // Bluetooth (legacy)
                audioManager.startBluetoothSco()
                audioManager.isBluetoothScoOn = true
                audioManager.isSpeakerphoneOn = false
            }
            else -> {
                // Modern API (Android M+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val selectedDevice = devices.find { it.id == deviceId }
                    selectedDevice?.let { device ->
                        when (device.type) {
                            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                                audioManager.isSpeakerphoneOn = true
                                audioManager.isBluetoothScoOn = false
                                audioManager.mode = AudioManager.MODE_NORMAL
                            }
                            AudioDeviceInfo.TYPE_WIRED_HEADSET,
                            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                            AudioDeviceInfo.TYPE_USB_HEADSET,
                            AudioDeviceInfo.TYPE_USB_DEVICE -> {
                                audioManager.isSpeakerphoneOn = false
                                audioManager.isBluetoothScoOn = false
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

                        // For Android 12+ (API 31+), we could use AudioDeviceInfo.TYPE_REMOTE_SUBMIX
                        // and more advanced routing, but this requires additional permissions
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            // Could implement more advanced audio routing here
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        // Handle any security exceptions or other errors
        // Fall back to default speaker
        try {
            audioManager.isSpeakerphoneOn = true
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (fallbackException: Exception) {
            // Log error if needed
        }
    }
}