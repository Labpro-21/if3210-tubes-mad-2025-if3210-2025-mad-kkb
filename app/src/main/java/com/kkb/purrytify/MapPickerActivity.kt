package com.kkb.purrytify

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
class MapPickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MapPickerScreen(
                onLocationSelected = { latLng, placeName ->
                    val resultIntent = Intent().apply {
                        putExtra("latitude", latLng.latitude)
                        putExtra("longitude", latLng.longitude)
                        putExtra("place_name", placeName)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                },
                onCancel = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    onLocationSelected: (LatLng, String?) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedLocationName by remember { mutableStateOf<String?>(null) }
    var isLoadingAddress by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }

    // Function to get address from coordinates
    fun getAddressFromLocation(latLng: LatLng) {
        scope.launch {
            isLoadingAddress = true
            val address = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1
                    )
                    addresses?.firstOrNull()?.let { address ->
                        listOfNotNull(
                            address.featureName,
                            address.locality,
                            address.adminArea,
                            address.countryName
                        ).joinToString(", ")
                    }
                } catch (e: Exception) {
                    "Selected Location"
                }
            }
            selectedLocationName = address
            isLoadingAddress = false
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Select Location",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Cancel",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        bottomBar = {
            if (selectedLocation != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Location info card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E1E1E)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Selected Location",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                if (isLoadingAddress) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF007BFF),
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Getting location details...",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = selectedLocationName ?: "Unknown Location",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Lat: %.6f, Lng: %.6f".format(
                                        selectedLocation!!.latitude,
                                        selectedLocation!!.longitude
                                    ),
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm button
                        Button(
                            onClick = {
                                selectedLocation?.let { location ->
                                    onLocationSelected(location, selectedLocationName)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF007BFF)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            enabled = !isLoadingAddress
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Confirm",
                                    tint = Color.White
                                )
                                Text(
                                    text = "Confirm Location",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Google Maps
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                        onResume()
                        getMapAsync { map ->
                            googleMap = map

                            // Configure map
                            map.uiSettings.apply {
                                isZoomControlsEnabled = true
                                isCompassEnabled = true
                                isMyLocationButtonEnabled = true
                            }

                            // Set initial camera position (can be user's current location or default)
                            val defaultLocation = LatLng(-6.2088, 106.8456) // Jakarta, Indonesia
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

                            // Set map click listener
                            map.setOnMapClickListener { latLng ->
                                // Remove previous marker
                                currentMarker?.remove()

                                // Add new marker
                                currentMarker = map.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title("Selected Location")
                                )

                                // Update selected location
                                selectedLocation = latLng
                                getAddressFromLocation(latLng)
                            }
                        }
                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Instructions overlay
            if (selectedLocation == null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Tap on the map to select a location",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Handle lifecycle
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }
}