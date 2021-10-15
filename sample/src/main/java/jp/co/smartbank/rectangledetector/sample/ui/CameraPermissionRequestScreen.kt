package jp.co.smartbank.rectangledetector.sample.ui

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import jp.co.smartbank.rectangledetector.sample.ui.theme.RectangleDetectorTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionRequestScreen(
    permittedContent: @Composable () -> Unit
) {
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var launchPermissionRequest by rememberSaveable { mutableStateOf(false) }
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }
    when {
        permissionState.hasPermission -> {
            permittedContent()
        }
        !permissionState.permissionRequested -> {
            launchPermissionRequest = true
        }
        else -> {
            val context = LocalContext.current
            CameraPermissionDeniedContent(
                doNotShowRationale = !permissionState.shouldShowRationale || doNotShowRationale,
                onClickRequestPermission = { permissionState.launchPermissionRequest() },
                onClickDoNotShowRationale = { doNotShowRationale = true },
                onClickSettings = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
        }
    }

    if (launchPermissionRequest) {
        LaunchedEffect(permissionState) {
            permissionState.launchPermissionRequest()
            launchPermissionRequest = false
        }
    }
}

@Composable
private fun CameraPermissionDeniedContent(
    doNotShowRationale: Boolean,
    onClickRequestPermission: () -> Unit = {},
    onClickDoNotShowRationale: () -> Unit = {},
    onClickSettings: () -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Grant Access",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.h5
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Allow this app to access Camera.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.body1
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (doNotShowRationale) {
            Button(
                onClick = onClickSettings,
                Modifier.defaultMinSize(minHeight = 48.dp)
            ) { Text(text = "Open Settings") }
        } else {
            Row {
                TextButton(
                    onClick = onClickDoNotShowRationale,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) { Text(text = "Deny") }

                Spacer(Modifier.width(24.dp))

                Button(
                    onClick = onClickRequestPermission,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) { Text(text = "Allow") }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CameraPermissionDeniedContentPreview() {
    RectangleDetectorTheme {
        Surface(color = MaterialTheme.colors.background) {
            CameraPermissionDeniedContent(doNotShowRationale = false)
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CameraPermissionDeniedContentPreview_DoNotShowRationale() {
    RectangleDetectorTheme {
        Surface(color = MaterialTheme.colors.background) {
            CameraPermissionDeniedContent(doNotShowRationale = true)
        }
    }
}
