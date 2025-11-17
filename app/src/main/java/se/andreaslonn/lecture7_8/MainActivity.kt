package se.andreaslonn.lecture7_8

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import se.andreaslonn.lecture7_8.ui.theme.Lecture78Theme

// DataStore
val EXAMPLE_COUNTER = intPreferencesKey("example_counter")
// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

//Networking
const val BASE_URL = "http://172.24.224.1:8000"

private val retrofit = Retrofit
    .Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

@Serializable
data class MyDataType(val myKey: String)

interface MyApiService {
    @GET("data.json")
    suspend fun getData(): MyDataType
}

object MyApi {
    val retrofitService: MyApiService by lazy {
        retrofit.create(MyApiService::class.java)
    }
}

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
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Lecture 7-8")
                        }
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate("datastore")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DataStore")
                    }
                    Button(
                        onClick = {
                            navController.navigate("networking")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Networking")
                    }
                    Button(
                        onClick = {
                            navController.navigate("sensors")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sensors")
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

        composable("networking") {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text("networking")
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
                var resultString by rememberSaveable { mutableStateOf<String?>(null) }

                Column(
                    modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)
                ) {
                    Text(resultString?: "null")

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                resultString = MyApi.retrofitService.getData().myKey
                            }
                        }
                    ) {
                        Text("Make request")
                    }
                }
            }
        }

        composable("sensors") {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Sensors")
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

                var resultString by rememberSaveable { mutableStateOf<String?>(null) }
                var errorString by rememberSaveable { mutableStateOf<String?>(null) }

                Column(
                    modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)
                ) {
                    Text("Error: $errorString")
                    Text(resultString?: "null")

                    val availableSensors = rememberSaveable {
                        mutableStateListOf<Sensor>()
                    }

                    // LaunchedEffect used for demonstration purposes
                    // Usually, you would put this code in the onCreate
                    LaunchedEffect(0) {
                        val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

                        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
                        availableSensors.addAll(deviceSensors)

                        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

                        if(accelerometer == null) {
                            errorString = "No accelerometer"
                            return@LaunchedEffect
                        }
                    }

                    LazyColumn {
                        items(availableSensors) { sensor ->
                            ListItem(
                                headlineContent = { Text(sensor.name, fontWeight = MaterialTheme.typography.headlineMedium.fontWeight) },
                                supportingContent = { Text(sensor.stringType) }
                            )
                        }
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