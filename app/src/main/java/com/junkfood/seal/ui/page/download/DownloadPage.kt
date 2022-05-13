package com.junkfood.seal.ui.page.download

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Scale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.util.TextUtil


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DownloadPage(
    navController: NavController,
    downloadViewModel: DownloadViewModel= hiltViewModel(),
) {

    val storagePermission =
        rememberPermissionState(
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) { b: Boolean ->
            if (b) {
                downloadViewModel.startDownloadVideo()
            } else {
                TextUtil.makeToast(context.resources.getString(R.string.permission_denied))
            }
        }

    val viewState = downloadViewModel.viewState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    val checkPermission = {
        if (storagePermission.status == PermissionStatus.Granted)
            downloadViewModel.startDownloadVideo()
        else {
            storagePermission.launchPermissionRequest()
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(9.dp)
        ) {
                TitleBar(title = context.getString(R.string.app_name), onClick = {
                    navController.navigate("settings") {
                        launchSingleTop = true
                        popUpTo("home")
                    }
                }) { navController.navigate("videolist") }
                Box(
                    modifier = Modifier
                        .padding(16f.dp)
                        .fillMaxSize()
                )
                {
                    with(viewState.value) {
                    Column {
                        AnimatedVisibility(visible = isDownloading) {
                            VideoCard(
                                videoTitle,
                                videoAuthor,
                                videoThumbnailUrl,
                                progress = progress, onClick = { downloadViewModel.openVideoFile() }
                            )

                        }

                        InputUrl(
                            url = url,
                            hint = context.getString(R.string.video_url),
                            error = isDownloadError,
                            errorMessage = errorMessage
                        ) { downloadViewModel.updateUrl(it) }
                    }
                    Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                        FABs(downloadCallback = checkPermission) {
                            TextUtil.matchUrlFromClipboard(clipboardManager.getText().toString())
                                ?.let {
                                    downloadViewModel.updateUrl(it)
                                }
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun SimpleText(text: String) {
    with(MaterialTheme.typography.titleLarge) {
        Text(text = text, fontSize = fontSize, fontWeight = fontWeight)
    }
}

@Composable
fun InputUrl(
    url: String,
    hint: String,
    error: Boolean,
    errorMessage: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = url,
        isError = error,
        onValueChange = onValueChange,
        label = { Text(hint) },
        modifier = Modifier
            .padding(0f.dp, 16f.dp)
            .fillMaxWidth()
    )
    AnimatedVisibility(visible = error) {
        Row {
            Icon(
                Icons.Outlined.Error,
                contentDescription = "error",
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                modifier = Modifier.padding(horizontal = 6.dp),
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCard(
    title: String = "videotitle",
    author: String = "author",
    thumbnailUrl: String,
    onClick: () -> Unit,
    progress: Float = 0f
) {
    ElevatedCard(modifier = Modifier
        .fillMaxWidth(), onClick = { onClick() }
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .padding()
                .fillMaxWidth(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .scale(Scale.FIT)
                .crossfade(true)
                .build(),
            contentDescription = null
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                CircularProgressIndicator(modifier = Modifier.requiredSize(32.dp))
            } else {
                SubcomposeAsyncImageContent()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                modifier = Modifier.padding(top = 3.dp),
                text = author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

        }
        val progressAnimationValue by animateFloatAsState(
            targetValue = progress / 100f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            LinearProgressIndicator(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                progress = progressAnimationValue
            )
        }
    }
}

@Composable
fun TitleBar(title: String, onClick: () -> Unit, videoList: () -> Unit) {
    LargeTopAppBar(title = {
        Text(
            text = title,
            fontSize = MaterialTheme.typography.displaySmall.fontSize
        )
    }, navigationIcon =
    {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(id = R.string.settings)
            )
        }

    }, actions = {
        IconButton(onClick = videoList) {
            Icon(
                imageVector = Icons.Outlined.Subscriptions,
                contentDescription = stringResource(id = R.string.downloads_history)
            )
        }
    }
    )
}

@Composable
fun FABs(downloadCallback: () -> Unit, pasteCallback: () -> Unit) {
    FloatingActionButton(
        onClick = pasteCallback,
        content = {
            Icon(
                Icons.Outlined.ContentPaste,
                contentDescription = "paste"
            )
        }, modifier = Modifier
            .padding(vertical = 12f.dp)
    )
    FloatingActionButton(
        onClick = downloadCallback,
        content = {
            Icon(
                Icons.Outlined.FileDownload,
                contentDescription = "download"
            )
        }, modifier = Modifier
            .padding(vertical = 12f.dp)
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlCard(url: String, pasteCallback: (() -> Unit)) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { pasteCallback() }
            .clip(RoundedCornerShape(32f.dp)),
        elevation = CardDefaults.cardElevation(),
        shape = RoundedCornerShape(32f.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16f.dp)
        )
        {
            with(MaterialTheme.typography.bodyLarge) {
                Text(text = url, fontSize = fontSize, fontWeight = fontWeight)
            }
        }
    }
}