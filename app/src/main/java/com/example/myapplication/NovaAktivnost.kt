package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class NovaAktivnost: ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val REQUEST_SEND_SMS = 1
        const val REQUEST_LOCATION_PERMISSION = 2
        const val REQUEST_CAMERA_PERMISSION = 3
        const val REQUEST_VOICE_RECOGNITION = 4
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Fja()
                }
            }
        }
    }

    fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!")
        startActivityForResult(intent, REQUEST_VOICE_RECOGNITION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SEND_SMS || requestCode == REQUEST_LOCATION_PERMISSION || requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndSendSMS()
            } else {
                // Permission denied
                permissions.forEach { permission ->
                    when (permission) {
                        Manifest.permission.ACCESS_FINE_LOCATION -> {
                            Toast.makeText(this, "Location permission is required for this feature", Toast.LENGTH_SHORT).show()
                        }
                        Manifest.permission.SEND_SMS -> {
                            Toast.makeText(this, "SMS permission is required for this feature", Toast.LENGTH_SHORT).show()
                        }
                        Manifest.permission.CAMERA -> {
                            Toast.makeText(this, "Camera permission is required for this feature", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun getLocationAndSendSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val message = "Location: https://www.google.com/maps?q=${location.latitude},${location.longitude}"
                    sendSMSAndStartVideoRecording(this, "+381646743689", message)
                } else {
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun sendSMSAndStartVideoRecording(context: Context, phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_SEND_SMS)
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            try {
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                context.startActivity(intent)
                Toast.makeText(context, "SMS Sent and Video Recording Started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "SMS and Video Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun justSendSms() {
        // Check if coarse location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val message = "Help needed. Location: https://www.google.com/maps?q=${location.latitude},${location.longitude}"

                    // Check if SMS permission is granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS),
                            NovaAktivnost.REQUEST_SEND_SMS
                        )
                    } else {
                        sendSms(message)
                    }
                } else {
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                NovaAktivnost.REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun sendSms(message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage("+381646743689", null, message, null, null)
            Toast.makeText(this, "SMS Sent", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "SMS Failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VOICE_RECOGNITION && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { result ->
                if (result.contains("help", ignoreCase = true)) {
                    getLocationAndSendSMS()
                    Toast.makeText(this, "Help command recognized", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Fja(modifier: Modifier = Modifier){

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xfffcfbff)),
        contentAlignment = Alignment.Center
    ) {


        Box(
            modifier = Modifier
                .requiredWidth(width = 390.dp)
                .requiredHeight(height = 844.dp)
                .background(color = Color(0xfffcfbff))
        ) {
            Box(
                // MIKROFON DEO -> SOS JE DOLE
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 78.dp,
                        y = 559.dp
                    )
                    .requiredWidth(width = 227.dp)
                    .requiredHeight(height = 122.dp)
                    .clickable {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.SEND_SMS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                context as Activity,
                                arrayOf(Manifest.permission.SEND_SMS),
                                NovaAktivnost.REQUEST_SEND_SMS
                            )
                        } else {
                            (context as NovaAktivnost).startVoiceRecognition()
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(
                            x = 57.dp,
                            y = 48.dp
                        )
                        .requiredWidth(width = 120.dp)
                        .requiredHeight(height = 74.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 120.dp)
                            .requiredHeight(height = 74.dp)
                            .clip(shape = RoundedCornerShape(15.dp))
                            .background(color = Color(0xff6D55F5)))

                    Box(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(
                                x = 33.dp,
                                y = 11.dp
                            )
                            .requiredWidth(width = 51.dp)
                            .requiredHeight(height = 53.dp)
                            .clip(shape = RoundedCornerShape(11.dp))
                            .background(color = Color(0xff6d55f5)))
                }
                Image(
                    painter = painterResource(id = R.drawable.mikrofon_natpis),
                    contentDescription = "Frame",
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 93.dp, y = 65.dp)
                        .requiredWidth(width = 44.dp)
                        .requiredHeight(height = 40.dp)
                )

                Text(
                    text = "Do you feel unsafe?",
                    color = Color(0xff2f2f2f),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold)
                )
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 83.dp,
                        y = 284.dp
                    )
                    .requiredWidth(width = 223.dp)
                    .requiredHeight(height = 224.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 223.dp)
                        .requiredHeight(height = 224.dp)
                        .clickable {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.SEND_SMS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    context as Activity,
                                    arrayOf(Manifest.permission.SEND_SMS),
                                    NovaAktivnost.REQUEST_SEND_SMS
                                )
                            } else {
                                (context as NovaAktivnost).justSendSms()
                                //ovde treba samo sms
                            }
                        }
                    //OVDE TREBA DODATI SAMO SLANJE PORUKE I LOKACIJE
                ) {
                    Box(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(
                                x = 13.11773681640625.dp,
                                y = 13.11767578125.dp
                            )
                            .requiredWidth(width = 197.dp)
                            .requiredHeight(height = 198.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .requiredWidth(width = 197.dp)
                                .requiredHeight(height = 198.dp)
                                .clip(shape = CircleShape)
                                .background(color = Color(0xffed4c5c).copy(alpha = 0.12f)))
                        Box(
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(
                                    x = 17.49005126953125.dp,
                                    y = 17.490234375.dp
                                )
                                .requiredWidth(width = 162.dp)
                                .requiredHeight(height = 163.dp)
                                .clip(shape = CircleShape)
                                .background(color = Color(0xffed4c5c)))
                        Box(
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(
                                    x = 57.7423095703125.dp,
                                    y = 57.7176513671875.dp
                                )
                                .requiredSize(size = 82.dp)
                        ) {
                            Image(
                                //SOS
                                painter = painterResource(id = R.drawable.sos_natpis),
                                contentDescription = "tabler:sos",
                                modifier = Modifier
                                    .requiredSize(size = 82.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 223.dp)
                            .requiredHeight(height = 224.dp)
                            .clip(shape = CircleShape)
                            .background(color = Color(0xffed4c5c).copy(alpha = 0.05f)))
                }
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 55.dp,
                        y = 184.dp
                    )
                    .requiredWidth(width = 279.dp)
                    .requiredHeight(height = 82.dp)
            ) {
                Text(
                    text = "Are you in emergency?",
                    color = Color(0xff2f2f2f),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Press the button below help will\nreach you soon.",
                    color = Color(0xff2f2f2f).copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(
                            x = 39.dp,
                            y = 44.dp
                        ))
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = (-3).dp,
                        y = (-2).dp
                    )
                    .requiredWidth(width = 396.dp)
                    .requiredHeight(height = 184.dp)
            ) {
                Text(
                    text = "+60111 783 6655",
                    color = Color.White.copy(alpha = 0.41f),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(
                            x = 43.dp,
                            y = 98.dp
                        ))
                Image(
                    //tamna pozadina
                    painter = painterResource(id = R.drawable.ggprofile),
                    contentDescription = "Group 40",
                    modifier = Modifier
                        .requiredWidth(width = 396.dp)
                        .requiredHeight(height = 184.dp))
                Box(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(
                            x = 138.dp,
                            y = 45.dp
                        )
                        .requiredWidth(width = 120.dp)
                        .requiredHeight(height = 25.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 120.dp)
                            .requiredHeight(height = 25.dp)
                            .clip(shape = RoundedCornerShape(17.5.dp))
                            .background(color = Color(0xff181818).copy(alpha = 0.2f)))
                    Text(
                        text = "Security App",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(
                                x = 25.dp,
                                y = 4.dp
                            ))
                }
                Tab(
                    selected = false,
                    onClick = {  },
                    text = {
                        Text(
                            text = "HEAR ME",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold)
                        )
                    },
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(
                            x = 118.dp,
                            y = 70.dp
                        )
                        .requiredWidth(width = 160.dp)
                        .requiredHeight(height = 49.dp))
            }
        }

        BottomNavigation(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Custom Icon"
                    )
                },

                selected = false,
                onClick = { /* Handle item 1 click */ }
            )
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Custom Icon"
                    )
                },


                selected = false,
                onClick = { /* Handle item 2 click */ }
            )
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "Custom Icon"
                    )
                },

                selected = false,
                onClick = { /* Handle item 3 click */ }
            )
        }
    }


}