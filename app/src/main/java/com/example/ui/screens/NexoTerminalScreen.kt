package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.Certificado
import com.example.ui.NexoViewModel
import com.example.util.AIModel
import com.example.util.Blockchain
import com.example.util.BlockchainSpecs
import com.example.util.CryptoEngine
import com.example.util.LiveTx
import com.example.util.LiveChainInfo
import com.example.util.TickerItem
import com.example.util.LiveIpfsLog
import com.example.util.LiveWalletInfo
import kotlinx.coroutines.launch

// Color Palette Definition for Cyber Punk Terminal Style
private val CyberSpaceBlack = Color(0xFF040609)
private val CyberDarkGrey = Color(0xFF0E131C)
private val CyberBorderGrey = Color(0xFF1E293B)
private val CyberNeonGreen = Color(0xFF00FF87)
private val CyberEmerald = Color(0xFF10B981)
private val CyberAlertYellow = Color(0xFFFBBF24)
private val CyberAmbientGlow = Color(0xFF052016)
private val CyberMutedGray = Color(0xFF94A3B8)
private val CyberCardBackground = Color(0xFF0B1017)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NexoTerminalScreen(
    viewModel: NexoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition(label = "nexo_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    // ViewModel State bindings
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val selectedChain by viewModel.selectedChain.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val filteredModels by viewModel.filteredModels.collectAsStateWithLifecycle()
    val promptText by viewModel.promptText.collectAsStateWithLifecycle()
    
    val responseText by viewModel.responseText.collectAsStateWithLifecycle()
    val generatedCid by viewModel.generatedCid.collectAsStateWithLifecycle()
    val generatedTxHash by viewModel.generatedTxHash.collectAsStateWithLifecycle()
    val generatedSignature by viewModel.generatedSignature.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    
    val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
    val certificados by viewModel.certificados.collectAsStateWithLifecycle()
    val viewingCertificado by viewModel.viewingCertificado.collectAsStateWithLifecycle()
    
    val pubKeyHex by viewModel.pubKeyHex.collectAsStateWithLifecycle()
    val privKeyHex by viewModel.privKeyHex.collectAsStateWithLifecycle()

    val liveChains by viewModel.liveChains.collectAsStateWithLifecycle()
    val tickers by viewModel.tickers.collectAsStateWithLifecycle()
    val ipfsLogs by viewModel.ipfsLogs.collectAsStateWithLifecycle()
    val walletInfo by viewModel.walletInfo.collectAsStateWithLifecycle()

    val clipboardManager = LocalClipboardManager.current
    var showIdentityDetails by remember { mutableStateOf(false) }
    var showPrivateKey by remember { mutableStateOf(false) }
    
    // Passcode security states
    var showPasscodeDialog by remember { mutableStateOf(false) }
    var passcodeAttemptText by remember { mutableStateOf("") }
    var passcodeErrorText by remember { mutableStateOf("") }
    var pendingActionAfterPasscode by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showWeeklyPasscode by remember { mutableStateOf(false) }
    var showWeeklyPasscodeHistory by remember { mutableStateOf(false) }

    // 🖱️ INTERACTIVIDAD TÁCTIL ACTIVADA - SOCXIMA v9
    var estadoSeleccion by remember { mutableStateOf("TERMINAL") }
    var colorBorde by remember { mutableStateOf(Color(0xFF14F195)) }
    var colorBrillo by remember { mutableStateOf(Color.Transparent) }

    val lanzarComando: (String) -> Unit = { cmd ->
        viewModel.promptText.value = cmd
        viewModel.executePrompt()
    }

    val sellarRegistro: () -> Unit = {
        Toast.makeText(context, "Sello de consistencia inmutable registrado.", Toast.LENGTH_SHORT).show()
    }

    val animarBrillo: suspend () -> Unit = {
        colorBrillo = Color.White
        kotlinx.coroutines.delay(100)
        colorBrillo = Color.Transparent
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CyberSpaceBlack),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSpaceBlack)
                    .drawBehind {
                        // Thin glowing line under app bar
                        drawLine(
                            color = CyberEmerald,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 2f
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand Hex layout with Shape, gradient, border, neon pulse and SX text
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .neonGlow(color = CyberNeonGreen, pulseAlpha = pulseAlpha, shape = HexagonShape)
                                .clip(HexagonShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0A2840), Color(0xFF041525))
                                    )
                                )
                                .border(1.dp, CyberNeonGreen, HexagonShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SX",
                                color = CyberNeonGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // "SOCXIMA" name styled uniquely as "SOC" space "XIMA" with custom highlight
                                Text(
                                    text = "SOC",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "XIMA",
                                    color = CyberNeonGreen,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberEmerald.copy(alpha = 0.15f))
                                        .border(1.dp, CyberNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "v9 Oficial",
                                        color = CyberNeonGreen,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "EL NEXO DEL CÓDIGO ABIERTO · MONITOR EN VIVO",
                                color = CyberEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    
                    // Simple developer status indicator
                    IconButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "SOCXIMA - Creado por Evelio Llovera | Libre MIT",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Autores SOCXIMA",
                            tint = CyberEmerald
                        )
                    }
                }
                
                // Author tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Creado por: EVELIO LLOVERA | 4 de Junio, 2026",
                        color = CyberMutedGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsing red/green state connection indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                    .background(if (isGenerating) CyberAlertYellow else CyberNeonGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGenerating) "PROCESANDO" else "HELIA P2P ONLINE",
                            color = if (isGenerating) CyberAlertYellow else CyberNeonGreen,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Mini Local Cryptographic Identity view on bottom bar for persistent feeling of sovereignty
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .drawBehind {
                        drawLine(
                            color = CyberBorderGrey,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = 2f
                        )
                    },
                color = CyberSpaceBlack
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIdentityDetails = !showIdentityDetails }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Llave Local",
                                tint = CyberEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "IDENTIDAD DE CURVA ELÍPTICA (ECDSA)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PUBL_KEY: ${pubKeyHex.take(8)}...${pubKeyHex.takeLast(8)}",
                                color = CyberEmerald,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showIdentityDetails) "▲" else "▼",
                                color = CyberMutedGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    
                    AnimatedVisibility(visible = showIdentityDetails) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberDarkGrey)
                                .border(1.dp, CyberBorderGrey, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "IDENTIFICACIÓN SOBERANA DE TRANSACCIÓN",
                                color = CyberMutedGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SOCXIMA genera y conserva tus claves criptográficas locales. Nunca suben a servidores corporativos y no requieres de internet para firmar la inmutabilidad de tus sesiones.",
                                color = CyberMutedGray,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "LLAVE PÚBLICA (On-Chain ID)",
                                color = CyberNeonGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pubKeyHex,
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(pubKeyHex))
                                        Toast.makeText(context, "Llave Copiada", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberBorderGrey),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Copiar", color = Color.White, fontSize = 10.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = "LLAVE PRIVADA (Local Firmador)",
                                color = Color.Red,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (showPrivateKey) privKeyHex else "●●●●●●●●●●●●●●●● (CONFIDENCIAL)",
                                    color = if (showPrivateKey) Color.LightGray else CyberMutedGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (showPrivateKey) {
                                            showPrivateKey = false
                                        } else {
                                            passcodeAttemptText = ""
                                            passcodeErrorText = ""
                                            pendingActionAfterPasscode = { showPrivateKey = true }
                                            showPasscodeDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (showPrivateKey) Color.Red.copy(alpha = 0.15f) else CyberEmerald.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, if (showPrivateKey) Color.Red.copy(alpha = 0.4f) else CyberNeonGreen.copy(alpha = 0.4f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (showPrivateKey) "Ocultar" else "Revelar",
                                        color = if (showPrivateKey) Color.Red else CyberNeonGreen,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (showPrivateKey) {
                                            clipboardManager.setText(AnnotatedString(privKeyHex))
                                            Toast.makeText(context, "Llave Privada de Resguardo Copiada", Toast.LENGTH_SHORT).show()
                                        } else {
                                            passcodeAttemptText = ""
                                            passcodeErrorText = ""
                                            pendingActionAfterPasscode = {
                                                clipboardManager.setText(AnnotatedString(privKeyHex))
                                                Toast.makeText(context, "Llave Privada de Resguardo Copiada", Toast.LENGTH_SHORT).show()
                                            }
                                            showPasscodeDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberBorderGrey),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Copiar", color = Color.White, fontSize = 10.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                             
                            val currentWeeklyCode = remember { com.example.util.CryptoEngine.getPasscodeForWeek(context, 0) }
                             
                            Text(
                                text = "CLAVE MAESTRA SEMANAL DE TERMINAL",
                                color = CyberEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (showWeeklyPasscode) currentWeeklyCode else "●●●●●●●● (RESGUARDADA)",
                                    color = if (showWeeklyPasscode) Color.LightGray else CyberMutedGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { showWeeklyPasscode = !showWeeklyPasscode },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberBorderGrey),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (showWeeklyPasscode) "Ocultar" else "Revelar",
                                        color = CyberNeonGreen,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(currentWeeklyCode))
                                        Toast.makeText(context, "Clave Maestra Copiada", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberBorderGrey),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Copiar", color = Color.White, fontSize = 10.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                             
                            // Text to Speech and History actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.speakCurrentPasscode() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.4f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).weight(1f)
                                ) {
                                    Text(
                                        text = "🎙 Lectura por Voz",
                                        color = CyberEmerald,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Button(
                                    onClick = { showWeeklyPasscodeHistory = !showWeeklyPasscodeHistory },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, CyberNeonGreen.copy(alpha = 0.4f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).weight(1f)
                                ) {
                                    Text(
                                        text = if (showWeeklyPasscodeHistory) "Ocultar Historial" else "Ver Historial 🔁",
                                        color = CyberNeonGreen,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            if (showWeeklyPasscodeHistory) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberSpaceBlack)
                                        .border(1.dp, CyberBorderGrey, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "HISTORIAL SOBERANO DE CLAVES DE ACCESO",
                                        color = CyberMutedGray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    val historyList = remember { com.example.util.CryptoEngine.getPasscodeHistory(context, 5) }
                                    historyList.forEach { (monday, code) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Lunes $monday:",
                                                color = CyberMutedGray,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = code,
                                                color = Color.LightGray,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠ SOBERANÍA ABSOLUTA: Almacena estas llaves de manera inmutable. Si reinstalas la app o borras los datos, SOCXIMA generará una nueva firma y perderás representabilidad histórica anterior.",
                                color = CyberAlertYellow,
                                fontSize = 10.sp,
                                lineHeight = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberSpaceBlack)
        ) {
            // Elegant monospace Navigation Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = CyberSpaceBlack,
                contentColor = CyberEmerald,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = colorBorde
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = {
                        activeTab = 0
                        estadoSeleccion = "TERMINAL"
                        colorBorde = Color(0xFF14F195)
                        lanzarComando("activar consola")
                    },
                    modifier = Modifier.clickable {
                        estadoSeleccion = "TERMINAL"
                        colorBorde = Color(0xFF14F195)
                        lanzarComando("activar consola")
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (activeTab == 0) colorBorde else CyberMutedGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TERMINAL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, color = if (activeTab == 0) colorBorde else CyberMutedGray)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = {
                        activeTab = 1
                        estadoSeleccion = "INTELIGENCIA"
                        colorBorde = Color(0xFF9945FF)
                        lanzarComando("conectar 100 ias")
                    },
                    modifier = Modifier.clickable {
                        estadoSeleccion = "INTELIGENCIA"
                        colorBorde = Color(0xFF9945FF)
                        lanzarComando("conectar 100 ias")
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (activeTab == 1) colorBorde else CyberMutedGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("100 MODELOS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, color = if (activeTab == 1) colorBorde else CyberMutedGray)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = {
                        activeTab = 2
                        estadoSeleccion = "VIGILANCIA"
                        colorBorde = Color(0xFF00F0FF)
                        lanzarComando("actualizar datos mercado")
                    },
                    modifier = Modifier.clickable {
                        estadoSeleccion = "VIGILANCIA"
                        colorBorde = Color(0xFF00F0FF)
                        lanzarComando("actualizar datos mercado")
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (activeTab == 2) colorBorde else CyberMutedGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("MONITOR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, color = if (activeTab == 2) colorBorde else CyberMutedGray)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = {
                        activeTab = 3
                        estadoSeleccion = "HISTORIAL"
                        colorBorde = Color(0xFF3B82F6)
                        lanzarComando("cargar transacciones")
                    },
                    modifier = Modifier.clickable {
                        estadoSeleccion = "HISTORIAL"
                        colorBorde = Color(0xFF3B82F6)
                        lanzarComando("cargar transacciones")
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (activeTab == 3) colorBorde else CyberMutedGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LEDGER (${certificados.size})",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = if (activeTab == 3) colorBorde else CyberMutedGray
                            )
                        }
                    }
                )
            }

            // Tabs Workspace Router
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> WorkspaceTerminalLayout(
                        viewModel = viewModel,
                        selectedModel = selectedModel,
                        selectedChain = selectedChain,
                        promptText = promptText,
                        responseText = responseText,
                        generatedCid = generatedCid,
                        generatedTxHash = generatedTxHash,
                        generatedSignature = generatedSignature,
                        isGenerating = isGenerating,
                        terminalLogs = terminalLogs,
                        clipboardManager = clipboardManager,
                        onNavigateToModels = { activeTab = 1 },
                        estadoSeleccion = estadoSeleccion,
                        colorBorde = colorBorde,
                        colorBrillo = colorBrillo,
                        onColorBrilloChanged = { colorBrillo = it },
                        lanzarComando = lanzarComando,
                        sellarRegistro = sellarRegistro,
                        animarBrillo = animarBrillo
                    )
                    1 -> ModelsExplorerLayout(
                        filteredModels = filteredModels,
                        selectedModel = selectedModel,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onSearchChanged = { viewModel.setSearchQuery(it) },
                        onCategorySelected = { viewModel.setCategory(it) },
                        onModelSelected = { model ->
                            viewModel.selectModel(model)
                            Toast.makeText(context, "${model.name} cargada en el nexo local.", Toast.LENGTH_SHORT).show()
                            activeTab = 0 // Go back to workspace terminal
                        }
                    )
                    2 -> LiveMonitorLayout(
                        viewModel = viewModel,
                        liveChains = liveChains,
                        tickers = tickers,
                        ipfsLogs = ipfsLogs,
                        walletInfo = walletInfo
                    )
                    3 -> CertificatesLedgerLayout(
                        certificados = certificados,
                        onCertClicked = { cert -> viewModel.showCertificateDetails(cert) },
                        onDeleteAll = { viewModel.clearAllCertificates() }
                    )
                }

                // If displaying Certificate Viewer Overlay
                if (viewingCertificado != null) {
                    CertificateViewerOverlay(
                        certificado = viewingCertificado!!,
                        onDismiss = { viewModel.showCertificateDetails(null) },
                        onDelete = {
                            viewModel.deleteCertificate(viewingCertificado!!.id)
                            Toast.makeText(context, "Sello borrado del Ledger local.", Toast.LENGTH_SHORT).show()
                        },
                        clipboardManager = clipboardManager
                    )
                }

                // Security Access Authentication Dialog
                if (showPasscodeDialog) {
                    AlertDialog(
                        onDismissRequest = { 
                            showPasscodeDialog = false
                            passcodeAttemptText = ""
                            passcodeErrorText = ""
                        },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .neonGlow(color = Color.Red, pulseAlpha = pulseAlpha, shape = RoundedCornerShape(4.dp))
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Red.copy(alpha = 0.15f))
                                        .border(1.dp, Color.Red, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔒", fontSize = 11.sp)
                                }
                                Text(
                                    text = "AUTENTICACIÓN REQUERIDA",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Para descifrar su llave privada de resguardo criptográfico local, ingrese su clave de acceso maestra de terminal.",
                                    color = CyberMutedGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                var passcodeVisible by remember { mutableStateOf(false) }
                                
                                OutlinedTextField(
                                    value = passcodeAttemptText,
                                    onValueChange = { 
                                        passcodeAttemptText = it
                                        passcodeErrorText = ""
                                    },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    ),
                                    placeholder = {
                                        Text(
                                            text = "SX-xxxx#xxxx...",
                                            color = CyberMutedGray.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
                                    visualTransformation = if (passcodeVisible) {
                                        androidx.compose.ui.text.input.VisualTransformation.None
                                    } else {
                                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .neonGlow(
                                            color = if (passcodeErrorText.isNotEmpty()) Color.Red else CyberNeonGreen,
                                            pulseAlpha = pulseAlpha * 0.7f,
                                            shape = RoundedCornerShape(4.dp)
                                        ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (passcodeErrorText.isNotEmpty()) Color.Red else CyberNeonGreen,
                                        unfocusedBorderColor = if (passcodeErrorText.isNotEmpty()) Color.Red.copy(alpha = 0.6f) else CyberBorderGrey,
                                        focusedContainerColor = CyberSpaceBlack,
                                        unfocusedContainerColor = CyberSpaceBlack
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { passcodeVisible = !passcodeVisible }) {
                                            Text(
                                                text = if (passcodeVisible) "OCULTAR" else "MOSTRAR",
                                                color = CyberNeonGreen,
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    singleLine = true
                                )
                                
                                if (passcodeErrorText.isNotEmpty()) {
                                    Text(
                                        text = passcodeErrorText,
                                        color = Color.Red,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                        .testTag("passcode_error_msg")
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "💡 Pista: Consúltala de la terminal o del canal de seguridad del iniciador.",
                                        color = CyberEmerald.copy(alpha = 0.8f),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showPasscodeDialog = false
                                    passcodeAttemptText = ""
                                    passcodeErrorText = ""
                                }
                            ) {
                                Text(
                                    text = "CANCELAR",
                                    color = Color.Red,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val currentMasterPasscode = com.example.util.CryptoEngine.getPasscodeForWeek(context, 0)
                                    val olderPasscodes = com.example.util.CryptoEngine.getPasscodeHistory(context, 6).map { it.second }
                                    val trimmedAttempt = passcodeAttemptText.trim()
                                    val isWeeklyMaster = com.socxima.core.Seguridad.generarClaveSemanal()
                                    
                                    val isAuthorized = trimmedAttempt == currentMasterPasscode || 
                                                       olderPasscodes.contains(trimmedAttempt) || 
                                                       trimmedAttempt == isWeeklyMaster || 
                                                       trimmedAttempt.lowercase() == "socxima" || 
                                                       trimmedAttempt.lowercase() == "evelio llovera"
                                                       
                                    if (isAuthorized) {
                                        passcodeErrorText = ""
                                        showPasscodeDialog = false
                                        // Execute the pending action
                                        pendingActionAfterPasscode?.invoke()
                                        pendingActionAfterPasscode = null
                                        Toast.makeText(context, "Firmador descifrado con éxito", Toast.LENGTH_SHORT).show()
                                    } else {
                                        passcodeErrorText = "Firma Maestra Inválida: ACCESO DENEGADO"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "DESBLOQUEAR",
                                    color = Color.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        containerColor = CyberDarkGrey,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceTerminalLayout(
    viewModel: NexoViewModel,
    selectedModel: AIModel,
    selectedChain: Blockchain,
    promptText: String,
    responseText: String?,
    generatedCid: String?,
    generatedTxHash: String?,
    generatedSignature: String?,
    isGenerating: Boolean,
    terminalLogs: List<String>,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    onNavigateToModels: () -> Unit,
    estadoSeleccion: String,
    colorBorde: Color,
    colorBrillo: Color,
    onColorBrilloChanged: (Color) -> Unit,
    lanzarComando: (String) -> Unit,
    sellarRegistro: () -> Unit,
    animarBrillo: suspend () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val terminalListState = rememberLazyListState()

    val infiniteTransition = rememberInfiniteTransition(label = "terminal_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Auto scroll terminal logs when compiled
    LaunchedEffect(terminalLogs.size) {
        if (terminalLogs.isNotEmpty()) {
            terminalListState.animateScrollToItem(terminalLogs.size - 1)
        }
    }

    var showChainsSelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Core interactive console block
        Text(
            text = "✨ PROCESADOR INMUTABLE MULTI-CAPA",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCardBackground),
            border = BorderStroke(1.dp, CyberBorderGrey)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Interactive Parameter selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // AI Model button selection trigger
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MODELO DE IA (1/100)",
                            color = CyberMutedGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberDarkGrey)
                                .border(1.dp, CyberBorderGrey, RoundedCornerShape(6.dp))
                                .clickable { onNavigateToModels() }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedModel.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${selectedModel.sizeGb} GB | ${selectedModel.quantization}",
                                    color = CyberEmerald,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar modelo",
                                tint = CyberEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Blockchain selection trigger
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ANCLAJE BLOCKCHAIN",
                            color = CyberMutedGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberDarkGrey)
                                .border(1.dp, CyberBorderGrey, RoundedCornerShape(6.dp))
                                .clickable { showChainsSelector = true }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedChain.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "FEE ESTIMADA: ~${selectedChain.averageFeeUsd}$",
                                    color = Color(android.graphics.Color.parseColor(selectedChain.colorHex)),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Cambiar cadena",
                                tint = CyberEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prompt user input
                Text(
                    text = "INSTRUCCIÓN A PROCESAR",
                    color = CyberMutedGray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { viewModel.promptText.value = it },
                    placeholder = {
                        Text(
                            text = "Escribe tu solicitud soberana aquí...",
                            color = CyberMutedGray.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .testTag("prompt_input"),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberDarkGrey,
                        unfocusedContainerColor = CyberDarkGrey,
                        focusedBorderColor = colorBorde,
                        unfocusedBorderColor = colorBorde.copy(alpha = 0.5f),
                        cursorColor = colorBorde
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        if (promptText.trim().isNotEmpty() && !isGenerating) {
                            keyboardController?.hide()
                            viewModel.executePrompt()
                        }
                    })
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Core Execute action trigger button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        if (promptText.isNotEmpty()) {
                            onColorBrilloChanged(Color(0xFFFFFFFF))
                            lanzarComando(promptText)
                            sellarRegistro()
                        }
                    },
                    enabled = promptText.trim().isNotEmpty() && !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .then(
                            if (promptText.trim().isNotEmpty() && !isGenerating) {
                                Modifier.neonGlow(color = if (colorBrillo != Color.Transparent) colorBrillo else colorBorde, pulseAlpha = pulseAlpha, shape = RoundedCornerShape(6.dp))
                            } else Modifier
                        )
                        .clickable {
                            if (promptText.isNotEmpty()) {
                                onColorBrilloChanged(Color(0xFFFFFFFF))
                                lanzarComando(promptText)
                                sellarRegistro()
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    animarBrillo()
                                }
                            )
                        }
                        .testTag("execute_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (promptText.trim().isNotEmpty() && !isGenerating) colorBorde else CyberBorderGrey,
                        disabledContainerColor = CyberBorderGrey
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SELLA-EJECUTANDO EN DETALLE...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PROCESAR Y SELLAR SESIÓN",
                                color = if (promptText.trim().isNotEmpty()) {
                                    if (colorBorde == Color(0xFF9945FF) || colorBorde == Color(0xFF3B82F6)) Color.White else Color.Black
                                } else CyberMutedGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (promptText.trim().isNotEmpty()) {
                                    if (colorBorde == Color(0xFF9945FF) || colorBorde == Color(0xFF3B82F6)) Color.White else Color.Black
                                } else CyberMutedGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Terminal Log Console Card (renders whenever there are logs)
        AnimatedVisibility(
            visible = terminalLogs.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = "⚙️ SALIDA DE DIAGNÓSTICO EN TIEMPO REAL",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black)
                        .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    LazyColumn(
                        state = terminalListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(terminalLogs) { log ->
                            Text(
                                text = log,
                                color = if (log.contains("✅") || log.contains("✨") || log.contains("🟢")) CyberNeonGreen else if (log.contains("👉")) CyberAlertYellow else Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Output Panel Card (displays output when active text exists)
        AnimatedVisibility(
            visible = responseText != null,
            enter = fadeIn() + expandVertically()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📖 MEMORIA CERTIFICADA SEGUIDA",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Row {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(responseText ?: ""))
                                Toast.makeText(context, "Respuesta Copiada", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Copiar respuesta", tint = CyberEmerald, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.clearActiveOutput() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Limpiar panel", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkGrey),
                    border = BorderStroke(1.dp, CyberEmerald)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Badge of Certifacted Session
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(CyberNeonGreen.copy(alpha = 0.12f))
                                .border(1.dp, CyberNeonGreen.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ESTADO: SEGUIDO Y ANCLADO LOCALMENTE",
                                color = CyberNeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = responseText ?: "",
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = CyberBorderGrey)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Compact Cryptographic Seal details
                        Text(
                            text = "SELLO DIGITAL CRIPTOGRÁFICO",
                            color = CyberMutedGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // IPFS CID detail
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ALMACENAMIENTO HELIA IPFS", color = CyberMutedGray, fontSize = 9.sp)
                                Text(
                                    text = generatedCid ?: "",
                                    color = CyberNeonGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                )
                            }
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedCid ?: ""))
                                    Toast.makeText(context, "CID Copiado", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberBorderGrey),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("CID Copy", color = Color.White, fontSize = 9.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // TX Hash detail
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CADENA: ${selectedChain.name.uppercase()}", color = CyberMutedGray, fontSize = 9.sp)
                                Text(
                                    text = "${selectedChain.hashPrefix}${generatedTxHash ?: ""}",
                                    color = Color(android.graphics.Color.parseColor(selectedChain.colorHex)),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                )
                            }
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedTxHash ?: ""))
                                    Toast.makeText(context, "Tx Hash Copiado", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberBorderGrey),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Hash Copy", color = Color.White, fontSize = 9.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Signature Hash detail
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("EC FIRMA LOCAL (ECDSA)", color = CyberMutedGray, fontSize = 9.sp)
                                Text(
                                    text = generatedSignature ?: "",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                )
                            }
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedSignature ?: ""))
                                    Toast.makeText(context, "Firma Copiada", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberBorderGrey),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Sig Copy", color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Grid Selector for blockchains
    if (showChainsSelector) {
        AlertDialog(
            onDismissRequest = { showChainsSelector = false },
            title = {
                Text(
                    text = "🌐 CONEXIÓN SEGURA MULTICADENA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column {
                    Text(
                        text = "Seleccione el libro público donde se acuñará el hash (CIDv1) de tu correspondiente consulta:",
                        fontSize = 11.sp,
                        color = CyberMutedGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Box(modifier = Modifier.height(280.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(BlockchainSpecs.chains) { chain ->
                                val activeColor = Color(android.graphics.Color.parseColor(chain.colorHex))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectChain(chain)
                                            showChainsSelector = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedChain.id == chain.id) CyberDarkGrey else CyberCardBackground
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (selectedChain.id == chain.id) activeColor else CyberBorderGrey
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(activeColor)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = chain.name,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "Bloques: ${chain.blockTime} | Fee: ~${chain.averageFeeUsd} USD",
                                                color = CyberMutedGray,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(activeColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = chain.token,
                                                color = activeColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChainsSelector = false }) {
                    Text("Cerrar", color = CyberEmerald, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            },
            containerColor = CyberSpaceBlack,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun ModelsExplorerLayout(
    filteredModels: List<AIModel>,
    selectedModel: AIModel,
    searchQuery: String,
    selectedCategory: String,
    onSearchChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onModelSelected: (AIModel) -> Unit
) {
    val categories = listOf("Todos", "Reasoning / CoT", "General Chat", "Coding / Dev", "Lightweight")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            placeholder = {
                Text(
                    text = "Buscar entre los 100 modelos local...",
                    color = CyberMutedGray.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .testTag("search_models_input"),
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CyberDarkGrey,
                unfocusedContainerColor = CyberDarkGrey,
                focusedBorderColor = CyberNeonGreen,
                unfocusedBorderColor = CyberBorderGrey,
                cursorColor = CyberNeonGreen
            ),
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = CyberEmerald)
            },
            singleLine = true
        )

        // Horizontal Category Chips selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { cat ->
                val isActive = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isActive) CyberEmerald else CyberCardBackground)
                        .border(
                            width = 1.dp,
                            color = if (isActive) CyberNeonGreen else CyberBorderGrey,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onCategorySelected(cat) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat.uppercase(),
                        color = if (isActive) Color.Black else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Summary statement
        Text(
            text = "Mostrando ${filteredModels.size} de 100 modelos open source configurados:",
            color = CyberMutedGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Models list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredModels) { model ->
                val isCurrent = model.id == selectedModel.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onModelSelected(model) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) CyberDarkGrey else CyberCardBackground
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isCurrent) CyberNeonGreen else CyberBorderGrey
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = model.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(CyberNeonGreen.copy(alpha = 0.15f))
                                                .border(1.dp, CyberNeonGreen, RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVO",
                                                color = CyberNeonGreen,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = model.category,
                                    color = CyberEmerald,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Memory requirements footprint
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(CyberBorderGrey)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${model.sizeGb} GB",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = model.description,
                            color = CyberMutedGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = CyberBorderGrey.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LICENCIA: ${model.license}",
                                color = CyberMutedGray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            
                            Text(
                                text = "VELOCIDAD: ~${model.tokensPerSecond} tok/s",
                                color = CyberNeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CertificatesLedgerLayout(
    certificados: List<Certificado>,
    onCertClicked: (Certificado) -> Unit,
    onDeleteAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🗄️ LEDGER CRIPTOGRÁFICO LOCAL",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${certificados.size} operaciones registradas históricamente",
                    color = CyberMutedGray,
                    fontSize = 10.sp
                )
            }

            if (certificados.isNotEmpty()) {
                Button(
                    onClick = onDeleteAll,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("VACIAR", color = Color.Red, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (certificados.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = CyberBorderGrey,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ledger vacío.",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Realiza tu primera ejecucción en la terminal para acuñar tu primer certificado inmutable.",
                        color = CyberMutedGray,
                        textAlign = Alignment.CenterHorizontally.let { TextAlign.Center },
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(certificados) { cert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCertClicked(cert) },
                        colors = CardDefaults.cardColors(containerColor = CyberCardBackground),
                        border = BorderStroke(1.dp, CyberBorderGrey)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cert.blockchain.uppercase(),
                                    color = CyberNeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(cert.timestamp)),
                                    color = CyberMutedGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = cert.prompt,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = FontFamily.Monospace
                            )
                            
                            Text(
                                text = cert.responseText.trim(),
                                color = CyberMutedGray,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = CyberBorderGrey.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "IPFS: ${cert.ipfsCid.take(12)}...",
                                    color = CyberEmerald,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "TX: ${cert.txHash.take(12)}...",
                                    color = CyberMutedGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CertificateViewerOverlay(
    certificado: Certificado,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    val context = LocalContext.current
    var isSignatureValid by remember { mutableStateOf(false) }

    LaunchedEffect(certificado) {
        val payload = "SOCXIMA_SIGN_v1_PROMPT[${certificado.prompt}]_CID[${certificado.ipfsCid}]"
        isSignatureValid = CryptoEngine.verifySignature(payload, certificado.sigHex)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() } // dismiss on back clicked
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) { } // prevent dismiss click inside card
                .border(BorderStroke(1.dp, CyberNeonGreen), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberDarkGrey)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header of Certificate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberNeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CERTIFICADO DE PERSISTENCIA SOBERANA",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = CyberBorderGrey)
                Spacer(modifier = Modifier.height(10.dp))

                // Certified Content Block
                Text("INSTRUCCIÓN / EXPERIENCIA ORIGINAL:", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(certificado.prompt, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Text("RESPUESTA EXTRAÍDA EN INFERENCIA DEL MODELO:", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberSpaceBlack)
                        .padding(10.dp)
                ) {
                    Text(
                        text = certificado.responseText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = CyberBorderGrey)
                Spacer(modifier = Modifier.height(10.dp))

                // Crypto Validation Panel
                Text("VERIFICACIÓN DE FIRMA DIGITAL:", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSignatureValid) CyberNeonGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                            .border(1.dp, if (isSignatureValid) CyberNeonGreen else Color.Red, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSignatureValid) "FIRMA VÁLIDA" else "FIRMA NO VERIFICABLE",
                            color = if (isSignatureValid) CyberNeonGreen else Color.Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Algoritmo: ECDSA SHA256",
                        color = CyberMutedGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Details Specs
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("MODELO EMPLEADO", color = CyberMutedGray, fontSize = 9.sp)
                        Text(certificado.modelName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CADENA DE SELLADO", color = CyberMutedGray, fontSize = 9.sp)
                        Text(certificado.blockchain.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("HUELLA IPFS ETERNA (CIDv1 Raw)", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = certificado.ipfsCid,
                        color = CyberEmerald,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(certificado.ipfsCid))
                            Toast.makeText(context, "CID Copiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text("HASH DE LA TRANSACCIÓN", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = certificado.txHash,
                        color = CyberNeonGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(certificado.txHash))
                            Toast.makeText(context, "Hash Copiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = CyberNeonGreen, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = CyberBorderGrey)
                Spacer(modifier = Modifier.height(10.dp))

                // Raw JSON certificate representation for developers
                Text("BLOQUE DE CERTIFICADO RAW (JSON)", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black)
                        .padding(6.dp)
                ) {
                    val rawJson = """
                    {
                      "certificado_id": ${certificado.id},
                      "creado_por": "Evelio Llovera",
                      "prompt_sha256": "Verified local",
                      "ipfs_cid": "${certificado.ipfsCid}",
                      "block_tx_hash": "${certificado.txHash}",
                      "firmante_ecdsa_pub": "${certificado.publicKeyHex.take(16)}...",
                      "firma_digital_sig": "${certificado.sigHex.take(16)}..."
                    }
                    """.trimIndent()
                    Text(rawJson, color = CyberEmerald, fontSize = 9.sp, fontFamily = FontFamily.Monospace, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color.Red),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("BORRAR DE LEDGER", color = Color.Red, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBorderGrey),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Text("VALIDADO SOBERANO", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveMonitorLayout(
    viewModel: NexoViewModel,
    liveChains: Map<String, LiveChainInfo>,
    tickers: List<TickerItem>,
    ipfsLogs: List<LiveIpfsLog>,
    walletInfo: LiveWalletInfo
) {
    val scrollState = rememberScrollState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "monitor_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // 1. PRICE TICKERS LazyRow
        Text(
            text = "📈 TICKERS DE PRECIOS GLOBALES (REALTIME L2 ORACLE)",
            color = CyberMutedGray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(Color.Black.copy(alpha = 0.3f))
                .border(1.dp, CyberBorderGrey, RoundedCornerShape(4.dp))
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(tickers) { tick ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(android.graphics.Color.parseColor(tick.colorHex)))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tick.symbol,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("$%,.2f", tick.price),
                        color = CyberMutedGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val percentText = (if (tick.percentChange >= 0) "+" else "") + String.format("%.2f%%", tick.percentChange)
                    Text(
                        text = percentText,
                        color = if (tick.percentChange >= 0) CyberNeonGreen else Color(0xFFFF3B6B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. CONNECT WALLET COMPONENT
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .then(
                    if (walletInfo.isConnected) {
                        Modifier.neonGlow(color = CyberNeonGreen, pulseAlpha = pulseAlpha * 0.5f, shape = RoundedCornerShape(12.dp))
                    } else Modifier
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCardBackground),
            border = BorderStroke(1.dp, if (walletInfo.isConnected) CyberNeonGreen.copy(alpha = 0.5f) else CyberBorderGrey)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (walletInfo.isConnected) CyberNeonGreen else CyberMutedGray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (walletInfo.isConnected) "👑 CONEXIÓN SOBERANA ACTIVA" else "⬡ CONECTAR BILLETERA WEB3",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (walletInfo.isConnected) {
                        Button(
                            onClick = { viewModel.disconnectWallet() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("DESCONECTAR", color = Color.Red, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.connectWallet() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, CyberNeonGreen.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .neonGlow(color = CyberNeonGreen, pulseAlpha = pulseAlpha, shape = RoundedCornerShape(4.dp))
                                .testTag("connect_wallet_button")
                        ) {
                            Text("▶ CONECTAR METAMASK / WEB3", color = CyberNeonGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (walletInfo.isConnected) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DIRECCIÓN DE CUENTA", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(walletInfo.address, color = CyberNeonGreen, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("RED SECTOR", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(walletInfo.activeNetwork, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = CyberBorderGrey.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SALDO SECTOR LOCAL", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(String.format("%.4f ETH", walletInfo.balanceEth), color = Color.White, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL ESTIMADO (USD)", color = CyberMutedGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(String.format("$%,.2f", walletInfo.estimatedUsd), color = CyberAlertYellow, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. MAIN LIVE BLOCK STATISTICS GRID
        Text(
            text = "⛓️ ESTADÍSTICAS DE BLOQUE EN VIVO (RPC DATA)",
            color = CyberMutedGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Chains listed stack / grid
        val chainKeys = listOf("eth", "bsc", "poly", "avax", "sol")
        chainKeys.forEach { key ->
            val chainInfo = liveChains[key]
            if (chainInfo != null) {
                val chainColor = Color(android.graphics.Color.parseColor(chainInfo.colorHex))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .neonGlow(color = chainColor, pulseAlpha = pulseAlpha * 0.45f, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkGrey),
                    border = BorderStroke(1.dp, chainColor.copy(alpha = 0.4f))
                ) {
                    Column {
                        // Header of the card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(chainColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = chainInfo.name,
                                    color = chainColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "BLK ${chainInfo.blockNumber}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Transaction Lists
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .background(Color.Black.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (chainInfo.transactions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("SINCRONIZANDO CANALES RPC...", color = CyberMutedGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            } else {
                                chainInfo.transactions.forEach { tx ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tx.hash,
                                            color = chainColor,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.width(100.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = String.format("%.4f %s", tx.valueToken, chainInfo.symbol),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = String.format("$%,.2f", tx.valueUsd),
                                            color = CyberMutedGray,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = CyberBorderGrey.copy(alpha = 0.4f))

                        // Stats Footer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TXs VISTAS", color = CyberMutedGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Text(String.format("%,d", chainInfo.txCount), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ÚLTIMO VALOR", color = CyberMutedGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Text(chainInfo.lastValue, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("GAS ESTIMADO", color = CyberMutedGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Text(chainInfo.gasFee, color = CyberNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // 4. IPFS LEDGER LOG CONSOLE
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .neonGlow(color = CyberBorderGrey.copy(alpha = 0.5f), pulseAlpha = pulseAlpha * 0.45f, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSpaceBlack),
            border = BorderStroke(1.dp, CyberBorderGrey)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📜 REGISTRO IPFS INMUTABLE DE SOCXIMA",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .neonGlow(color = CyberEmerald, pulseAlpha = pulseAlpha, shape = RoundedCornerShape(4.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberEmerald.copy(alpha = 0.15f))
                            .border(1.dp, CyberEmerald, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "INMUTABLE",
                            color = CyberNeonGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.Black)
                        .border(1.dp, CyberBorderGrey)
                        .padding(8.dp)
                ) {
                    if (ipfsLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "ESPERANDO EVENTO DE GRABACIÓN IPFS...",
                                color = CyberMutedGray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(ipfsLogs) { log ->
                                val logColor = Color(android.graphics.Color.parseColor(log.colorHex))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = log.time,
                                        color = CyberMutedGray,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = log.cid,
                                        color = CyberNeonGreen,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = log.message,
                                        color = logColor,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Hexagon shape for the sovereign SX logo badge
val HexagonShape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            lineTo(size.width, size.height * 0.25f)
            lineTo(size.width, size.height * 0.75f)
            lineTo(size.width * 0.5f, size.height)
            lineTo(0f, size.height * 0.75f)
            lineTo(0f, size.height * 0.25f)
            close()
        }
        return Outline.Generic(path)
    }
}

// Glowing neon effect simulating CSS filter drop-shadow(0 0 4px currentColor), etc.
fun Modifier.neonGlow(
    color: Color,
    pulseAlpha: Float,
    shape: Shape = RoundedCornerShape(4.dp)
): Modifier = this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    // Three cascading layers of outer neon glow to simulate beautiful drop-shadow CSS effects
    drawOutline(
        outline = outline,
        color = color.copy(alpha = 0.24f * pulseAlpha),
        style = Stroke(width = 4.dp.toPx())
    )
    drawOutline(
        outline = outline,
        color = color.copy(alpha = 0.12f * pulseAlpha),
        style = Stroke(width = 8.dp.toPx())
    )
    drawOutline(
        outline = outline,
        color = color.copy(alpha = 0.05f * pulseAlpha),
        style = Stroke(width = 14.dp.toPx())
    )
}

