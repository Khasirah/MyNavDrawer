package dev.peppo.mynavdrawer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.peppo.mynavdrawer.ui.theme.MyNavDrawerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyNavDrawerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyNavDrawerApp()
                }
            }
        }
    }
}

data class MenuItem(val title: String, val icon: ImageVector)

@Composable
fun BackPressHandler(enabled: Boolean = true, onBackPressed: () -> Unit) {
    val currentOnBackPressed by rememberUpdatedState(onBackPressed)
    val backCallback = remember {
        object: OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBackPressed()
            }
        }
    }

    SideEffect {
        backCallback.isEnabled = enabled
    }

    val backDispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.onBackPressedDispatcher

    val lifecycleOwener = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwener, backDispatcher) {
        backDispatcher.addCallback(lifecycleOwener, backCallback)
        onDispose { backCallback.remove() }
    }
}

@Composable
fun MyNavDrawerApp() {
//    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//    val scope = rememberCoroutineScope()
//    val snackbarHostState = remember {
//        SnackbarHostState()
//    }
//    val context = LocalContext.current
    val appState = rememberMyNavDrawerState()

    // ketika tombol back di tekan akan menutup navigation drawer yang terbuka
    BackPressHandler(enabled = appState.drawerState.isOpen) {
        appState.onBackPress()
    }

    val items = listOf(
        MenuItem(
            title = stringResource(id = R.string.home),
            icon = Icons.Default.Home
        ),
        MenuItem(
            title = stringResource(id = R.string.favourite),
            icon = Icons.Default.Favorite
        ),
        MenuItem(
            title = stringResource(id = R.string.profile),
            icon = Icons.Default.AccountCircle
        ),
    )

    val selectedItem = remember {
        mutableStateOf(items[0])
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = appState.snackbarHostState) },
        topBar = {
            MyTopBar(
                onMenuClick = appState::onMenuClick
            )
        }
    ) { paddingValues ->
        ModalNavigationDrawer(
            modifier = Modifier.padding(paddingValues),
            drawerState = appState.drawerState,
            gesturesEnabled = appState.drawerState.isOpen, // gesture hanya bisa dilakukan ketika ingin menutup navigation drawer saja
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(12.dp))
                    items.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(text = item.title) },
                            selected = item == selectedItem.value,
                            onClick = {
                                appState.onItemSelected(item)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (appState.drawerState.isClosed) {
                            stringResource(id = R.string.swipe_to_open)
                        } else {
                            stringResource(id = R.string.swipe_to_close)
                        }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                onMenuClick()
            }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(id = R.string.menu)
                )
            }
        },
        title = {
            Text(stringResource(id = R.string.app_name))
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyNavDrawerTheme {
        MyNavDrawerApp()
    }
}