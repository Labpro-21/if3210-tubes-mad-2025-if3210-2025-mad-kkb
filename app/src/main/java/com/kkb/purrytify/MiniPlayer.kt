package com.kkb.purrytify

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
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
    val isDefault: Boolean = false,
    val deviceInfo: AudioDeviceInfo? = null // Tambahkan referensi device asli
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
        selectedDeviceId = getCurrentAudioDevice(context, audioDevices)
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
//                val seconds = (player.currentPosition / 1000L)
//                if (seconds > lastReportedSeconds) {
//                    val delta = seconds - lastReportedSeconds
//                    if (delta > 0) {
//                        viewModel.updateTimeListened(currentSong.songId, delta)
//                        lastReportedSeconds = seconds
//                    }
//                }
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
                    selectedDeviceId = getCurrentAudioDevice(context, audioDevices)
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
                    val success = setAudioOutputDevice(context, deviceId, audioDevices)
                    android.util.Log.d("MiniPlayer", "setAudioOutputDevice success: $success, deviceId: $deviceId")
                    if (success) {
                        selectedDeviceId = deviceId
                        // Re-apply audio routing to MediaPlayer
                        applyAudioRoutingToMediaPlayer(context, deviceId, audioDevices)
                    }
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
    android.util.Log.d("AudioDevices", "getAvailableAudioDevices called")
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val devices = mutableListOf<AudioDeviceOption>()

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            android.util.Log.d("AudioDevices", "Total output devices detected: ${audioDevices.size}")

            audioDevices.forEach { device ->
                android.util.Log.d("AudioDevices", "Device: type=${device.type}, name=${device.productName}, id=${device.id}")
                when (device.type) {
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_USB_HEADSET,
                    AudioDeviceInfo.TYPE_USB_DEVICE -> {
                        val deviceName = device.productName?.toString()
                            ?: getDefaultDeviceName(device.type)
                        devices.add(AudioDeviceOption(
                            id = device.id,
                            name = deviceName,
                            type = device.type,
                            isDefault = device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                            deviceInfo = device
                        ))
                        android.util.Log.d("AudioDevices", "Added device: id=${device.id}, name=${deviceName}, type=${device.type}")
                    }
                    else -> {
                        android.util.Log.d("AudioDevices", "Skipped device type: ${device.type}")
                    }
                }
            }

            // Tambahkan fallback jika tidak ada speaker built-in
            if (!devices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }) {
                android.util.Log.d("AudioDevices", "No built-in speaker found, adding fallback")
                devices.add(AudioDeviceOption(
                    id = -1,
                    name = "Phone Speaker",
                    type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                    isDefault = true
                ))
            }
        } else {
            android.util.Log.d("AudioDevices", "Using fallback for Android < M")
            // Fallback untuk Android versi lama
            devices.add(AudioDeviceOption(
                id = -1,
                name = "Phone Speaker",
                type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                isDefault = true
            ))

            val wiredHeadsetOn = audioManager.isWiredHeadsetOn
            android.util.Log.d("AudioDevices", "isWiredHeadsetOn: $wiredHeadsetOn")
            if (wiredHeadsetOn) {
                devices.add(AudioDeviceOption(
                    id = -2,
                    name = "Wired Headset",
                    type = AudioDeviceInfo.TYPE_WIRED_HEADSET
                ))
            }

            val bluetoothA2dpOn = audioManager.isBluetoothA2dpOn
            android.util.Log.d("AudioDevices", "isBluetoothA2dpOn: $bluetoothA2dpOn")
            if (bluetoothA2dpOn) {
                devices.add(AudioDeviceOption(
                    id = -3,
                    name = "Bluetooth Audio",
                    type = AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                ))
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("AudioDevices", "Error getting audio devices", e)
        // Fallback jika terjadi error
        devices.clear()
        devices.add(AudioDeviceOption(
            id = -1,
            name = "Phone Speaker",
            type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            isDefault = true
        ))
    }

    val finalDevices = devices.distinctBy { "${it.name}_${it.type}" }
    android.util.Log.d("AudioDevices", "Final device list (${finalDevices.size} devices):")
    finalDevices.forEachIndexed { index, device ->
        android.util.Log.d("AudioDevices", "$index: id=${device.id}, name=${device.name}, type=${device.type}, isDefault=${device.isDefault}")
    }

    return finalDevices
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

fun getCurrentAudioDevice(context: Context, devices: List<AudioDeviceOption>): Int {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    return try {
        when {
            audioManager.isBluetoothA2dpOn -> {
                val deviceId = devices.find { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }?.id ?: -3
                android.util.Log.d("MiniPlayer", "Current audio device: Bluetooth A2DP, id=$deviceId")
                deviceId
            }
            audioManager.isWiredHeadsetOn -> {
                val deviceId = devices.find {
                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                }?.id ?: -2
                android.util.Log.d("MiniPlayer", "Current audio device: Wired Headset/Headphones, id=$deviceId")
                deviceId
            }
            else -> {
                val deviceId = devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }?.id ?: -1
                android.util.Log.d("MiniPlayer", "Current audio device: Built-in Speaker, id=$deviceId")
                deviceId
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("MiniPlayer", "Error getting current audio device", e)
        -1 // Default ke speaker
    }
}

fun setAudioOutputDevice(context: Context, deviceId: Int, devices: List<AudioDeviceOption>): Boolean {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    return try {
        val selectedDevice = devices.find { it.id == deviceId }

        if (selectedDevice == null) {
            // Fallback ke speaker default
            audioManager.isSpeakerphoneOn = true
            audioManager.isBluetoothScoOn = false
            audioManager.mode = AudioManager.MODE_NORMAL
            return true
        }

        // Reset semua audio routing terlebih dahulu
        audioManager.isSpeakerphoneOn = false
        audioManager.isBluetoothScoOn = false
        audioManager.stopBluetoothSco()

        when (selectedDevice.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                audioManager.isSpeakerphoneOn = true
                audioManager.mode = AudioManager.MODE_NORMAL
            }
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                audioManager.mode = AudioManager.MODE_NORMAL
                // Wired headset biasanya otomatis terdeteksi
            }
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
                audioManager.mode = AudioManager.MODE_NORMAL
                // A2DP biasanya handle otomatis oleh system
            }
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                audioManager.startBluetoothSco()
                audioManager.isBluetoothScoOn = true
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            }
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_USB_DEVICE -> {
                audioManager.mode = AudioManager.MODE_NORMAL
                // USB audio biasanya handle otomatis
            }
            else -> {
                audioManager.isSpeakerphoneOn = true
                audioManager.mode = AudioManager.MODE_NORMAL
            }
        }

        // Untuk Android API 23+ gunakan preferred device jika tersedia
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && selectedDevice.deviceInfo != null) {
            try {
                // Ini memerlukan MediaPlayer instance yang aktif
                val player = MediaPlayerManager.getPlayer()
                if (player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    player.setPreferredDevice(selectedDevice.deviceInfo)
                }
            } catch (e: Exception) {
                // Fallback jika setPreferredDevice gagal
            }
        }

        true
    } catch (e: Exception) {
        // Log error dan fallback ke speaker
        try {
            audioManager.isSpeakerphoneOn = true
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (fallbackException: Exception) {
            // Ignore fallback errors
        }
        false
    }
}

fun applyAudioRoutingToMediaPlayer(context: Context, deviceId: Int, devices: List<AudioDeviceOption>) {
    try {
        val player = MediaPlayerManager.getPlayer()
        if (player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val selectedDevice = devices.find { it.id == deviceId }
            selectedDevice?.deviceInfo?.let { deviceInfo ->
                player.setPreferredDevice(deviceInfo)
            }
        }
    } catch (e: Exception) {
        // Handle error - MediaPlayer mungkin belum siap
    }
}