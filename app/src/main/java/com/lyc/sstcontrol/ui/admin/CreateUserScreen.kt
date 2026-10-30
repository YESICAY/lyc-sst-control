package com.lyc.sstcontrol.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.lyc.sstcontrol.R
import com.lyc.sstcontrol.data.CreateUserError
import com.lyc.sstcontrol.data.UserRole

@Composable
fun CreateUserScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val viewModel = remember(activity) {
        ViewModelProvider(activity)[CreateUserViewModel::class.java]
    }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.create_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.create_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = viewModel.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = { Text(stringResource(R.string.create_full_name)) },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    singleLine = true,
                    enabled = !viewModel.isLoading,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text(stringResource(R.string.create_email)) },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    singleLine = true,
                    enabled = !viewModel.isLoading,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.create_role),
                    style = MaterialTheme.typography.labelLarge
                )
                UserRole.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = viewModel.role == option,
                                enabled = !viewModel.isLoading,
                                role = Role.RadioButton,
                                onClick = { viewModel.onRoleChange(option) }
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = viewModel.role == option, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                if (option == UserRole.ADMIN) R.string.role_admin else R.string.role_inspector
                            )
                        )
                    }
                }

                viewModel.error?.let { error ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(error.messageRes()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = viewModel::onCreateClick,
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.create_button),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    viewModel.createdCredentials?.let { credentials ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.credentials_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.credentials_message, credentials.fullName))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.credentials_email_label),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(text = credentials.email, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.credentials_password_label),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = credentials.password,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onCredentialsAcknowledged) {
                    Text(stringResource(R.string.credentials_done))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val text = context.getString(
                            R.string.credentials_clipboard_text,
                            credentials.email,
                            credentials.password
                        )
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("credentials", text))
                        Toast.makeText(context, R.string.credentials_copied, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.credentials_copy))
                }
            }
        )
    }
}

private fun CreateUserError.messageRes(): Int = when (this) {
    CreateUserError.EMPTY_FIELDS -> R.string.create_error_empty
    CreateUserError.INVALID_EMAIL -> R.string.create_error_invalid_email
    CreateUserError.EMAIL_IN_USE -> R.string.create_error_email_in_use
    CreateUserError.NO_NETWORK -> R.string.create_error_no_network
    CreateUserError.NOT_ALLOWED -> R.string.create_error_not_allowed
    CreateUserError.UNKNOWN -> R.string.create_error_unknown
}