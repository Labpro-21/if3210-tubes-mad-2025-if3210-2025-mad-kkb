package com.kkb.purrytify

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.kkb.purrytify.viewmodel.EditProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var location by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isGettingLocation by remember { mutableStateOf(false) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var selectedLocationName by remember { mutableStateOf<String?>(null) }

    // Initialize form with current profile data
    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { profile ->
            location = profile.location ?: ""
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Google Maps place picker launcher
    val mapPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val latitude = data.getDoubleExtra("latitude", 0.0)
                val longitude = data.getDoubleExtra("longitude", 0.0)
                val placeName = data.getStringExtra("place_name")

                if (latitude != 0.0 && longitude != 0.0) {
                    selectedLatLng = LatLng(latitude, longitude)
                    selectedLocationName = placeName

                    // Convert coordinates to country code
                    scope.launch {
                        val countryCode = withContext(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                                addresses?.firstOrNull()?.countryCode
                            } catch (e: Exception) {
                                null
                            }
                        }
                        countryCode?.let { location = it }
                    }
                }
            }
        }
    }

    // Function to open Google Maps for location selection
    fun openMapsForLocationSelection() {
        try {
            // Try to use Google Maps app first
            val mapsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=")
                setPackage("com.google.android.apps.maps")
            }

            if (mapsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapsIntent)
            } else {
                // Fallback to web maps
                val webMapsIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://maps.google.com/")
                }
                context.startActivity(webMapsIntent)
            }
        } catch (e: Exception) {
            viewModel.setLocationError("Unable to open maps. Please enter location manually.")
        }
    }

    // Alternative: Create a custom map picker activity
    fun openCustomMapPicker() {
        val intent = Intent(context, MapPickerActivity::class.java)
        mapPickerLauncher.launch(intent)
    }

    // Function to get current location
    fun getCurrentLocation(context: Context, callback: (String?) -> Unit) {
        scope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            // Use Geocoder to get country code
                            scope.launch {
                                val countryCode = withContext(Dispatchers.IO) {
                                    try {
                                        val geocoder = Geocoder(context, Locale.getDefault())
                                        val addresses = geocoder.getFromLocation(
                                            location.latitude,
                                            location.longitude,
                                            1
                                        )
                                        addresses?.firstOrNull()?.countryCode
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                callback(countryCode)
                            }
                        } else {
                            callback(null)
                        }
                    }.addOnFailureListener {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, get location
            getCurrentLocation(context) { countryCode ->
                if (countryCode != null) {
                    location = countryCode
                    isGettingLocation = false
                } else {
                    isGettingLocation = false
                    // Show error message
                    viewModel.setLocationError("Unable to get location. Please enter manually.")
                }
            }
        } else {
            isGettingLocation = false
            // Permission denied
            viewModel.setLocationError("Location permission denied. Please enter location manually.")
        }
    }

    // Function to handle location button click
    fun handleLocationButtonClick() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                isGettingLocation = true
                getCurrentLocation(context) { countryCode ->
                    if (countryCode != null) {
                        location = countryCode
                        isGettingLocation = false
                    } else {
                        isGettingLocation = false
                        viewModel.setLocationError("Unable to get location. Please enter manually.")
                    }
                }
            }
            else -> {
                // Request permission
                isGettingLocation = true
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Load profile data on first composition
    LaunchedEffect(Unit) {
        val token = TokenStorage.getAccessToken(context)
        if (token != null) {
            viewModel.loadProfile(token)
        }
    }

    // Handle successful update
    LaunchedEffect(uiState.isUpdateSuccessful) {
        if (uiState.isUpdateSuccessful) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Photo Section
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    // Show selected image
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Profile Photo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.3f)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Show current profile photo using ProfileAvatar component
                    ProfileAvatar(
                        profilePhotoPath = uiState.profile?.profilePhoto,
                        modifier = Modifier.size(120.dp)
                    )
                }

                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF007BFF))
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Username (read-only)
            OutlinedTextField(
                value = uiState.profile?.username ?: "",
                onValueChange = { },
                label = { Text("Username", color = Color.Gray) },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email (read-only)
            OutlinedTextField(
                value = uiState.profile?.email ?: "",
                onValueChange = { },
                label = { Text("Email", color = Color.Gray) },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location field with auto-detect and map selection buttons
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        if (it.length <= 2) {
                            location = it.uppercase()
                            // Clear selected map location when manually editing
                            selectedLatLng = null
                            selectedLocationName = null
                        }
                    },
                    label = { Text("Location (Country Code)", color = Color.Gray) },
                    placeholder = { Text("e.g., US, ID, UK", color = Color.Gray.copy(alpha = 0.7f)) },
                    supportingText = {
                        Column {
                            Text(
                                "Use ISO 3166-1 alpha-2 country codes (2 letters)",
                                color = Color.Gray.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            selectedLocationName?.let {
                                Text(
                                    "Selected: $it",
                                    color = Color(0xFF007BFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF007BFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF007BFF),
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color(0xFF007BFF)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location selection buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Auto-detect location button
                    Button(
                        onClick = { handleLocationButtonClick() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF28A745)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isGettingLocation && !uiState.isLoading
                    ) {
                        if (isGettingLocation) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Auto-detect Location",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Auto",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Open maps button
                    Button(
                        onClick = { openCustomMapPicker() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007BFF)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isGettingLocation && !uiState.isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Select on Map",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Map",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Error message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x20FF0000))
                ) {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Save button
            Button(
                onClick = {
                    val token = TokenStorage.getAccessToken(context)
                    if (token != null) {
                        viewModel.updateProfile(
                            token = token,
                            location = location.ifEmpty { null },
                            profilePhotoUri = selectedImageUri,
                            context = context
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007BFF)
                ),
                shape = RoundedCornerShape(24.dp),
                enabled = !uiState.isLoading && !isGettingLocation
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Save Changes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel button
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}