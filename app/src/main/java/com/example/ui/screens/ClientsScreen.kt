// Add these imports at the top of ClientsScreen.kt if not already present:
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

// Inside your ClientsScreen Composable top bar actions or layout buttons, add:

val context = androidx.compose.ui.platform.LocalContext.get()
val gson = Gson()

// Export Launcher
val exportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
) { uri: Uri? ->
    uri?.let {
        try {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    val json = gson.toJson(clients)
                    writer.write(json)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Import Launcher
val importLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let {
        try {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val type = object : TypeToken<List<Client>>() {}.type
                    val importedClients: List<Client> = gson.fromJson(reader, type)
                    importedClients.forEach { client ->
                        viewModel.insertClient(client)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Add these buttons into your screen actions or layout:
Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    Button(onClick = { exportLauncher.launch("clients_backup.json") }) {
        Text("Export Clients")
    }
    Button(onClick = { importLauncher.launch("application/json") }) {
        Text("Import Clients")
    }
}
