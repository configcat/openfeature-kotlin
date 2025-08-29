package com.example.configcat_openfeature_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.configcat.*
import com.configcat.log.LogLevel
import com.example.configcat_openfeature_sample.ui.theme.ConfigcatopenfeaturesampleTheme
import dev.openfeature.kotlin.sdk.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            OpenFeatureAPI.setProviderAndWait(ConfigCatProvider("PKDVCLf-Hq-h-kCzMp-L7Q/HhOWfwVtZ0mb30i9wi17GQ") {
                // Use ConfigCat's shared preferences cache.
                configCache = SharedPreferencesCache(this@MainActivity)

                // Info level logging helps to inspect the feature flag evaluation process.
                // Use the default Warning level to avoid too detailed logging in your application.
                logLevel = LogLevel.INFO
            }, initialContext = ImmutableContext(targetingKey = "configcat@example.com", attributes = mapOf(
                "Email" to Value.String("configcat@example.com"),
            )))
        }

        enableEdgeToEdge()
        setContent {
            ConfigcatopenfeaturesampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "ConfigCat / OpenFeature",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column {
        Row {
            Text(
                text = "Welcome to $name!",
                modifier = modifier
            )
        }
        Row {
            Text(
                text = "isPOCFeatureEnabled: ${OpenFeatureAPI.getClient().getBooleanValue("isPOCFeatureEnabled", false)}",
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ConfigcatopenfeaturesampleTheme {
        Greeting("ConfigCat / OpenFeature")
    }
}