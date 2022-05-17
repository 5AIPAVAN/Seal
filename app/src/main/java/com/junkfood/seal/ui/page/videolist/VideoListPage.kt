package com.junkfood.seal.ui.page.videolist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.AudioListItem
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.VideoListItem
import com.junkfood.seal.util.FileUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListPage(
    navController: NavController, videoListViewModel: VideoListViewModel = hiltViewModel()
) {
    val viewState = videoListViewModel.viewState.collectAsState()
    val videoList = viewState.value.videoListFlow.collectAsState(ArrayList())
    val audioList = viewState.value.audioListFlow.collectAsState(ArrayList())
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    val scope = rememberCoroutineScope()
    val audioFilter = remember { mutableStateOf(false) }
    val videoFilter = remember { mutableStateOf(false) }
    val ytbFilter = remember { mutableStateOf(false) }
    val bilibiliFilter = remember { mutableStateOf(false) }
    val nicoFilter = remember { mutableStateOf(false) }
    fun websiteFilter(url: String, pattern: String, filterEnabled: Boolean): Boolean {
        return (!filterEnabled or url.contains(Regex(pattern)))
    }

    val filterList = listOf(ytbFilter, bilibiliFilter, nicoFilter)
    fun urlFilter(url: String): Boolean {
        return websiteFilter(url, "youtu", ytbFilter.value) and websiteFilter(
            url,
            "(b23\\.tv)|(bilibili)",
            bilibiliFilter.value
        ) and websiteFilter(url, "nico", nicoFilter.value)
    }


    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.downloads_history)
                    )
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }, scrollBehavior = scrollBehavior, contentPadding = PaddingValues()
            )

        }
    ) { innerPadding ->

        Column(
            Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.padding(
                    bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                ),
            ) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp)
                    ) {
                        FilterChipWithIcon(
                            select = audioFilter.value,
                            onClick = {
                                audioFilter.value = !audioFilter.value
                                if (videoFilter.value) videoFilter.value = false
                            },
                            label = stringResource(id = R.string.audio)
                        )

                        FilterChipWithIcon(
                            select = videoFilter.value,
                            onClick = {
                                videoFilter.value = !videoFilter.value
                                if (audioFilter.value) audioFilter.value = false
                            },
                            label = stringResource(id = R.string.video)
                        )

                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .height(24.dp)
                                .width(1.5f.dp)
                                .align(Alignment.CenterVertically),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )

                        with(bilibiliFilter) {
                            FilterChipWithIcon(
                                select = value,
                                onClick = {
                                    filterList.forEach { if (it != this) it.value = false }
                                    value = !value
                                },
                                label = stringResource(id = com.junkfood.seal.R.string.bilibili)
                            )
                        }
                        with(ytbFilter) {
                            FilterChipWithIcon(
                                select = value,
                                onClick = {
                                    filterList.forEach { if (it != this) it.value = false }
                                    value = !value
                                },
                                label = "YouTube"
                            )
                        }

                        with(nicoFilter) {
                            FilterChipWithIcon(
                                select = value,
                                onClick = {
                                    filterList.forEach { if (it != this) it.value = false }
                                    value = !value
                                },
                                label = "ニコニコ動画"
                            )
                        }
                    }
                }
                items(videoList.value.reversed()) {
                    AnimatedVisibility(
                        visible = !audioFilter.value and urlFilter(it.videoUrl)
                    )
                    {
                        with(it) {
                            VideoListItem(
                                title = videoTitle,
                                author = videoAuthor,
                                thumbnailUrl = thumbnailUrl,
                                videoUrl = videoUrl,
                                onClick = { FileUtil.openFile(videoPath) }
                            ) { videoListViewModel.showDrawer(scope, this@with) }
                        }
                    }
                }
                items(audioList.value.reversed()) {
                    AnimatedVisibility(
                        visible = !videoFilter.value and urlFilter(it.videoUrl)
                    ) {
                        with(it) {
                            AudioListItem(
                                title = videoTitle,
                                author = videoAuthor,
                                thumbnailUrl = thumbnailUrl,
                                videoUrl = videoUrl,
                                onClick = { FileUtil.openFile(videoPath) }
                            ) { videoListViewModel.showDrawer(scope, this@with) }
                        }
                    }
                }
            }
        }
    }
    VideoDetailDrawer()
    if (viewState.value.showDialog)
        RemoveItemDialog()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipWithIcon(select: Boolean, onClick: () -> Unit, label: String) {
    FilterChip(
        modifier = Modifier.padding(horizontal = 6.dp),
        selected = select,
        onClick = onClick,
        label = {
            Text(text = label)
        },
        trailingIcon = {
            AnimatedVisibility(visible = select) {
                Icon(
                    Icons.Outlined.Check,
                    stringResource(R.string.checked),
                    modifier = Modifier.size(18.dp)
                )
            }
        },
    )
}


