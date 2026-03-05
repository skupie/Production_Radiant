package com.radiant.sms.ui.screens.admin

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminMemberDetailsDto
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.util.MultipartUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

data class AdminMemberDetailsState(
    val loading: Boolean = true,
    val error: String? = null,
    val member: AdminMemberDetailsDto? = null
)

class AdminMemberDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)

    private val _state = MutableStateFlow(AdminMemberDetailsState())
    val state: StateFlow<AdminMemberDetailsState> = _state

    fun load(memberId: Long) {
        viewModelScope.launch {
            try {
                _state.value = AdminMemberDetailsState(loading = true)
                val resp = repo.adminMemberDetails(memberId)
                _state.value = AdminMemberDetailsState(loading = false, member = resp.member)
            } catch (e: Exception) {
                _state.value = AdminMemberDetailsState(loading = false, error = e.message ?: "Failed")
            }
        }
    }

    fun update(memberId: Long, parts: List<MultipartBody.Part>, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.adminUpdateMember(memberId, parts)
                onDone()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), e.message ?: "Update failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun delete(memberId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.adminDeleteMember(memberId)
                onDone()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), e.message ?: "Delete failed", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMemberDetailsScreen(
    nav: NavController,
    memberId: Long,
    vm: AdminMemberDetailsViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(memberId) { vm.load(memberId) }

    val scroll = rememberScrollState()
    val cardShape = RoundedCornerShape(18.dp)

    // ✅ FIX: use RELATIVE fields and convert to absolute URL
    // This fixes member/nominee photo not showing on Edit/Update screen.
    val memberPhotoUrl = NetworkModule.absoluteUrl(s.member?.image)
    val nomineePhotoUrl = NetworkModule.absoluteUrl(s.member?.nomineePhoto)

    AdminScaffold(nav = nav, title = "Update Member", hideTitle = false, showBack = true) {
        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            s.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(s.error!!) }
            s.member == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Member not found") }
            else -> {
                var fullName by remember { mutableStateOf(s.member?.fullName ?: "") }
                var email by remember { mutableStateOf(s.member?.email ?: "") }
                var mobile by remember { mutableStateOf(s.member?.mobileNumber ?: "") }
                var nid by remember { mutableStateOf(s.member?.nid ?: "") }
                var share by remember { mutableStateOf((s.member?.share ?: 0).toString()) }

                var nomineeName by remember { mutableStateOf(s.member?.nomineeName ?: "") }
                var nomineeNid by remember { mutableStateOf(s.member?.nomineeNid ?: "") }

                var password by remember { mutableStateOf("") }
                var passwordConfirmation by remember { mutableStateOf("") }

                var memberPhotoUri by remember { mutableStateOf<Uri?>(null) }
                var nomineePhotoUri by remember { mutableStateOf<Uri?>(null) }

                val pickMemberPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    memberPhotoUri = uri
                }
                val pickNomineePhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    nomineePhotoUri = uri
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Card(shape = cardShape) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PhotoPreview(localUri = memberPhotoUri, remoteUrl = memberPhotoUrl, height = 190.dp, shape = cardShape)
                            Button(onClick = { pickMemberPhoto.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                                Text("Change Member Photo")
                            }
                        }
                    }

                    Card(shape = cardShape) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PhotoPreview(localUri = nomineePhotoUri, remoteUrl = nomineePhotoUrl, height = 190.dp, shape = cardShape)
                            Button(onClick = { pickNomineePhoto.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                                Text("Change Nominee Photo")
                            }
                        }
                    }

                    Card(shape = cardShape) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = nid, onValueChange = { nid = it }, label = { Text("NID") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(
                                value = share,
                                onValueChange = { share = it },
                                label = { Text("Share") },
                                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Card(shape = cardShape) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = nomineeName, onValueChange = { nomineeName = it }, label = { Text("Nominee Name") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = nomineeNid, onValueChange = { nomineeNid = it }, label = { Text("Nominee NID") }, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Card(shape = cardShape) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("New Password (optional)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = passwordConfirmation, onValueChange = { passwordConfirmation = it }, label = { Text("Confirm Password") }, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Button(
                        onClick = {
                            val parts = mutableListOf<MultipartBody.Part>()

                            parts += MultipartUtil.text("full_name", fullName)
                            parts += MultipartUtil.text("email", email)
                            parts += MultipartUtil.text("mobile_number", mobile)
                            parts += MultipartUtil.text("nid", nid)
                            parts += MultipartUtil.text("share", share)

                            parts += MultipartUtil.text("nominee_name", nomineeName)
                            parts += MultipartUtil.text("nominee_nid", nomineeNid)

                            if (password.isNotBlank()) {
                                parts += MultipartUtil.text("password", password)
                                parts += MultipartUtil.text("password_confirmation", passwordConfirmation)
                            }

                            memberPhotoUri?.let { uri ->
                                MultipartUtil.file(ctx, "image", uri)?.let { parts += it }
                            }
                            nomineePhotoUri?.let { uri ->
                                MultipartUtil.file(ctx, "nominee_photo", uri)?.let { parts += it }
                            }

                            vm.update(memberId, parts) {
                                Toast.makeText(ctx, "Member updated", Toast.LENGTH_LONG).show()
                                nav.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Member")
                    }

                    OutlinedButton(
                        onClick = {
                            vm.delete(memberId) {
                                Toast.makeText(ctx, "Member deleted", Toast.LENGTH_LONG).show()
                                nav.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Member")
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoPreview(
    localUri: Uri?,
    remoteUrl: String?,
    height: Dp,
    shape: RoundedCornerShape
) {
    val context = LocalContext.current

    val model = when {
        localUri != null -> localUri
        !remoteUrl.isNullOrBlank() -> remoteUrl
        else -> null
    }

    if (model == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("No photo uploaded")
        }
        return
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context).data(model).crossfade(true).build(),
        contentDescription = "Photo",
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Failed to load photo")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScaffold(
    nav: NavController,
    title: String,
    hideTitle: Boolean,
    showBack: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { if (!hideTitle) Text(title) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        content = content
    )
}
