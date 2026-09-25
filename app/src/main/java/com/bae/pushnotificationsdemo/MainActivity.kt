package com.bae.pushnotificationsdemo

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.bae.pushnotificationsdemo.ui.theme.PushNotificationsDemoTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private var permissionGranted by mutableStateOf(false)
    private var notificationsEnabled by mutableStateOf(false)
    private var feedback by mutableStateOf<String?>(null)

    // capta movementId cuando FCM abre la app desde el background
    private var pendingMovementId by mutableStateOf<String?>(null)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        refreshNotificationState()

        feedback = if (isGranted) {
            null
        } else {
            "Se necesita el permiso para mostrar la transferencia."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)
        refreshNotificationState()

        // Si FCM abrió la aplicación desde una notificación en background,
        // los datos personalizados llegan como extras del Intent
        pendingMovementId = intent.getStringExtra("movementId")

        Log.d(
            TAG,
            "onCreate movementId recibido desde Intent: $pendingMovementId"
        )

        enableEdgeToEdge()

        setContent {
            PushNotificationsDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()

                    /*
                     * Esperamos a que exista NavController y después
                     * procesamos el movementId recibido desde FCM
                     */
                    LaunchedEffect(pendingMovementId) {
                        pendingMovementId?.let { movementId ->

                            Log.d(
                                TAG,
                                "Navegando desde FCM hacia movement/$movementId"
                            )

                            navController.navigate("movement/$movementId") {
                                launchSingleTop = true
                            }

                            // Evita volver a navegar por recomposiciones.
                            pendingMovementId = null
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {

                        composable("home") {
                            HomeScreen(
                                permissionGranted = permissionGranted,
                                notificationsEnabled = notificationsEnabled,
                                feedback = feedback,
                                firebaseStatus = FirebaseDemoState.status,
                                registrationToken = FirebaseDemoState.token,
                                onRequestPermission = ::requestNotificationPermission,
                                onSimulateTransfer = ::simulateTransfer,
                                onCopyToken = ::copyToken
                            )
                        }

                        composable(
                            route = "movement/{movementId}",
                            arguments = listOf(
                                navArgument("movementId") {
                                    type = NavType.StringType
                                }
                            ),
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern =
                                        "notificationdemo://movements/{movementId}"
                                }
                            )
                        ) { entry ->

                            val movementId = requireNotNull(
                                entry.arguments?.getString("movementId")
                            )

                            MovementDetailScreen(
                                movementId = movementId,
                                onBack = {
                                    navController.navigate("home") {
                                        popUpTo("home") {
                                            inclusive = true
                                        }

                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /*
    * Solo si main activity ya existe
    */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        pendingMovementId = intent.getStringExtra("movementId")

        Log.d(
            TAG,
            "onNewIntent movementId recibido: $pendingMovementId"
        )
    }

    override fun onResume() {
        super.onResume()

        refreshNotificationState()
        refreshFirebaseToken()
    }

    private fun refreshNotificationState() {
        permissionGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        notificationsEnabled =
            NotificationHelper.notificationsEnabled(this)
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !permissionGranted
        ) {
            permissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else if (!notificationsEnabled) {

            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(
                        Settings.EXTRA_APP_PACKAGE,
                        packageName
                    )
            )

        } else {
            feedback = "Las notificaciones ya están habilitadas."
        }
    }

    private fun simulateTransfer() {
        refreshNotificationState()

        feedback = when {

            !permissionGranted ->
                "Concede el permiso de notificaciones antes de simular la transferencia."

            !notificationsEnabled ->
                "Activa las notificaciones de esta app en Ajustes."

            NotificationHelper.publishTransferNotification(
                context = this,
                title = "Transferencia recibida",
                body = "Recibiste S/ 250.00",
                movementId = NotificationHelper.DEMO_MOVEMENT_ID
            ) ->
                "Notificación enviada. Abre el panel de notificaciones y tócala."

            else ->
                "No se pudo mostrar la notificación. Revisa el canal Transferencias."
        }
    }

    private fun copyToken() {
        val token = FirebaseDemoState.token ?: return

        val clipboard =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                "FCM registration token",
                token
            )
        )

        feedback = "Token copiado."
    }

    private fun refreshFirebaseToken() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDemoState.status =
                "Sin configurar: agrega app/google-services.json."

            FirebaseDemoState.token = null
            return
        }

        FirebaseDemoState.status =
            "Firebase configurado; obteniendo token…"

        @Suppress("DEPRECATION")
        FirebaseMessaging
            .getInstance()
            .token
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    FirebaseDemoState.token = task.result

                    FirebaseDemoState.status =
                        "Firebase configurado; token disponible."

                    Log.d(
                        TAG,
                        "FCM registration token disponible: ${task.result}"
                    )

                } else {

                    FirebaseDemoState.token = null

                    FirebaseDemoState.status =
                        "Firebase configurado; no se pudo obtener el token."

                    Log.w(
                        TAG,
                        "Error al obtener FCM registration token",
                        task.exception
                    )
                }
            }
    }

    companion object {
        private const val TAG = "NotificationDemo"
    }
}