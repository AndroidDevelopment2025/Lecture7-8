package se.andreaslonn.lecture7_8

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import se.andreaslonn.lecture7_8.ui.theme.Lecture78Theme

val EXAMPLE_COUNTER = intPreferencesKey("example_counter")
// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lecture78Theme {

                App(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(context: Context) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start",
        enterTransition = { fadeIn() }
    ) {
        composable("start") {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    Button(
                        onClick = {
                            navController.navigate("datastore")
                        }
                    ) {
                        Text("DataStore")

                    }
                    Text("Android")
                }
            }
        }

        composable("datastore") {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text("DataStore")
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    navController.navigateUp()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->

                val coroutineScope = rememberCoroutineScope()

                fun counterFlow(): Flow<Int> = context.dataStore.data.map { preferences ->
                    preferences[EXAMPLE_COUNTER] ?: 0
                }

                suspend fun incrementCounter() {
                    context.dataStore.updateData {
                        it.toMutablePreferences().also { preferences ->
                            preferences[EXAMPLE_COUNTER] = (preferences[EXAMPLE_COUNTER] ?: 0) + 1
                        }
                    }
                }

                val myValue by counterFlow().collectAsState(
                    initial = 0,
                    coroutineScope.coroutineContext
                )

                Column(
                    modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)
                ) {
                    Text("DataStore")

                    Button(
                        onClick = {
                            // Increment counter
                            coroutineScope.launch {
                                incrementCounter()
                            }
                        }
                    ) {
                        Text("Current value: $myValue")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Lecture78Theme {
        //App()
    }
}