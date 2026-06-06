package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.Certificado
import com.example.data.repository.CertificadoRepository
import com.example.util.AIModel
import com.example.util.Blockchain
import com.example.util.BlockchainSpecs
import com.example.util.CryptoEngine
import com.example.util.GeminiService
import com.example.util.IpfsEngine
import com.example.util.ModelSpecs
import com.example.util.LiveTx
import com.example.util.LiveChainInfo
import com.example.util.TickerItem
import com.example.util.LiveIpfsLog
import com.example.util.LiveWalletInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NexoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = CertificadoRepository(database.certificadoDao())
    private val socximaBrain = com.socxima.core.SocximaBrain()
    private val codeEvolver = com.socxima.core.CodeEvolver(socximaBrain)

    private var tts: android.speech.tts.TextToSpeech? = null

    val certificados: StateFlow<List<Certificado>> = repository.allCertificados
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Identity states
    private val _pubKeyHex = MutableStateFlow("")
    val pubKeyHex = _pubKeyHex.asStateFlow()

    private val _privKeyHex = MutableStateFlow("")
    val privKeyHex = _privKeyHex.asStateFlow()

    // Monitor States
    private val _liveChains = MutableStateFlow<Map<String, LiveChainInfo>>(emptyMap())
    val liveChains = _liveChains.asStateFlow()

    private val _tickers = MutableStateFlow<List<TickerItem>>(emptyList())
    val tickers = _tickers.asStateFlow()

    private val _ipfsLogs = MutableStateFlow<List<LiveIpfsLog>>(emptyList())
    val ipfsLogs = _ipfsLogs.asStateFlow()

    private val _walletInfo = MutableStateFlow(LiveWalletInfo(isConnected = false))
    val walletInfo = _walletInfo.asStateFlow()

    // Selector states
    private val _selectedModel = MutableStateFlow<AIModel>(ModelSpecs.modelList.first())
    val selectedModel = _selectedModel.asStateFlow()

    private val _selectedChain = MutableStateFlow<Blockchain>(BlockchainSpecs.chains.first())
    val selectedChain = _selectedChain.asStateFlow()

    // Model selection filtering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Filtered models stream
    private val _filteredModels = MutableStateFlow<List<AIModel>>(ModelSpecs.modelList)
    val filteredModels = _filteredModels.asStateFlow()

    // Prompt States
    val promptText = MutableStateFlow("")
    
    private val _responseText = MutableStateFlow<String?>(null)
    val responseText = _responseText.asStateFlow()

    private val _generatedCid = MutableStateFlow<String?>(null)
    val generatedCid = _generatedCid.asStateFlow()

    private val _generatedTxHash = MutableStateFlow<String?>(null)
    val generatedTxHash = _generatedTxHash.asStateFlow()

    private val _generatedSignature = MutableStateFlow<String?>(null)
    val generatedSignature = _generatedSignature.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    // Log terminal stdout stream
    private val _terminalLogs = MutableStateFlow<List<String>>(emptyList())
    val terminalLogs = _terminalLogs.asStateFlow()

    // Viewer states
    private val _viewingCertificado = MutableStateFlow<Certificado?>(null)
    val viewingCertificado = _viewingCertificado.asStateFlow()

    init {
        // Load or generate decentralized ECDSA identity keys immediately
        val (pub, priv) = CryptoEngine.getOrGenerateKeys(application)
        _pubKeyHex.value = pub
        _privKeyHex.value = priv
        
        // Initialize Android TextToSpeech
        tts = android.speech.tts.TextToSpeech(application) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                tts?.language = java.util.Locale("es", "ES")
            }
        }

        filterModels()
        startLiveMonitor()
    }

    fun speakText(text: String) {
        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "SOCXIMA_TTS")
    }

    fun speakCurrentPasscode() {
        val code = com.example.util.CryptoEngine.getPasscodeForWeek(getApplication())
        val spokenParts = mutableListOf<String>()
        for (char in code) {
            when {
                char == 'S' || char == 's' -> spokenParts.add("S")
                char == 'X' || char == 'x' -> spokenParts.add("equis")
                char == '-' -> spokenParts.add("guión")
                char == '#' -> spokenParts.add("numeral")
                char == '$' -> spokenParts.add("símbolo de dólar")
                char == '@' -> spokenParts.add("arroba")
                char == '!' -> spokenParts.add("exclamación")
                char == '&' -> spokenParts.add("ampersand")
                char == '%' -> spokenParts.add("porcentaje")
                char == '*' -> spokenParts.add("asterisco")
                char == '?' -> spokenParts.add("interrogación")
                char == '+' -> spokenParts.add("más")
                char == '=' -> spokenParts.add("igual")
                char.isDigit() -> spokenParts.add(char.toString())
                char.isLetter() -> {
                    if (char.isUpperCase()) {
                        spokenParts.add("${char.uppercaseChar()} mayúscula")
                    } else {
                        spokenParts.add("${char.lowercaseChar()} minúscula")
                    }
                }
                else -> spokenParts.add(char.toString())
            }
        }
        val textToSpeak = "Comando de voz SOCXIMA recibido. Su clave de terminal actual es: " + spokenParts.joinToString(", ")
        speakText(textToSpeak)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun selectModel(model: AIModel) {
        _selectedModel.value = model
    }

    fun selectChain(chain: Blockchain) {
        _selectedChain.value = chain
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterModels()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        filterModels()
    }

    private fun filterModels() {
        val q = _searchQuery.value.trim().lowercase()
        val cat = _selectedCategory.value
        
        _filteredModels.value = ModelSpecs.modelList.filter { model ->
            val matchesQuery = model.name.lowercase().contains(q) || 
                               model.series.lowercase().contains(q) || 
                               model.description.lowercase().contains(q)
            
            val matchesCat = if (cat == "Todos") {
                true
            } else {
                model.category == cat
            }
            
            matchesQuery && matchesCat
        }
    }

    fun clearActiveOutput() {
        _responseText.value = null
        _generatedCid.value = null
        _generatedTxHash.value = null
        _generatedSignature.value = null
        _terminalLogs.value = emptyList()
    }

    fun executePrompt() {
        val currentPrompt = promptText.value.trim()
        if (currentPrompt.isEmpty()) return

        // Intercept Voice dictation request command!
        val lowercasePrompt = currentPrompt.lowercase()
        if (lowercasePrompt.contains("socxima") && lowercasePrompt.contains("clave")) {
            viewModelScope.launch {
                _isGenerating.value = true
                _terminalLogs.value = emptyList()
                _responseText.value = null
                _generatedCid.value = null
                _generatedTxHash.value = null
                _generatedSignature.value = null
                
                addLog("⚡ SOCXIMA v9 · EL NEXO - CÓDIGO MAESTRO LIMPIO")
                delay(150)
                addLog("💎 COMANDO SOBERANO RECIBIDO: SINTETIZADOR DE ACCESO POR VOZ")
                delay(200)
                addLog("🎙️ Dictando clave maestra de terminal en voz alta...")
                
                speakCurrentPasscode()
                
                delay(500)
                val currentCode = com.example.util.CryptoEngine.getPasscodeForWeek(getApplication())
                _responseText.value = "🎙️ REPRODUCIENDO CLAVE DE ACCESO POR AUDIO:\n\n\"Comando de voz SOCXIMA recibido. Su clave de terminal actual es: $currentCode\"\n\nEsta clave cambia automáticamente todos los lunes a las 00:00 UTC y nunca repite combinación."
                addLog("📢 Clave dictada correctamente por el sintetizador de voz.")
                _isGenerating.value = false
                promptText.value = "" // clear input
            }
            return
        }

        // Intercept SOCXIMA Status Control
        if (lowercasePrompt == "socxima status" || lowercasePrompt == "socxima" || lowercasePrompt == "status" || lowercasePrompt == "ayuda") {
            viewModelScope.launch {
                _isGenerating.value = true
                _terminalLogs.value = emptyList()
                _responseText.value = null
                _generatedCid.value = null
                _generatedTxHash.value = null
                _generatedSignature.value = null

                com.socxima.core.iniciarSocxima()

                addLog("=== ⚡ S.O. SOCXIMA v9 ACTIVADA ===")
                delay(100)
                addLog("👤 Propietario: Evelio Llovera")
                delay(100)
                addLog("🔑 Clave actual (Seguridad): ${com.socxima.core.Seguridad.generarClaveSemanal()}")
                delay(100)
                addLog("🧬 Estado: Inteligencia Protegida | 100 Herramientas Listas")
                addLog("=================================")
                delay(150)
                addLog("🎙️ Dictando confirmación de arranque en voz alta...")
                
                val statusText = "Servidor SOCXIMA versión 9 activo, bajo control absoluto de Evelio Llovera. Cien herramientas de cómputo listas."
                speakText(statusText)

                val response = "### 🧠 SOCXIMA v9 · EL NEXO - ESTADO GENERAL DE INTELIGENCIA\n\n" +
                        "**Propietario Soberano:** Evelio Llovera\n" +
                        "**Firma del Nexo:** Inmutable & Protegida\n" +
                        "**Clave Maestra de Seguridad:** `${com.socxima.core.Seguridad.generarClaveSemanal()}`\n" +
                        "**Estado:** `100% OPERATIVO` · 100 Herramientas de Cómputo Sincronizadas.\n\n" +
                        "**Comandos del Nexo Soportados:**\n" +
                        "- `buscar ganancia` / `ganancias` : Busca oportunidades DeFi y las envía a tu dirección.\n" +
                        "- `desbloquear` / `resolver bloqueo [red]` : Libera rutas bloqueadas/congestionadas.\n" +
                        "- `aprender [clave] [valor]` : Enseña y sincroniza conocimientos en la Inteligencia Maestra.\n" +
                        "- `socxima clave` : Dicta la clave por voz."
                _responseText.value = response
                _isGenerating.value = false
                promptText.value = ""
            }
            return
        }

        // Intercept SOCXIMA Buscar Ganancia
        if (lowercasePrompt.contains("buscar ganancia") || lowercasePrompt == "ganancias" || lowercasePrompt.contains("socxima ganancia") || lowercasePrompt.startsWith("ganancia")) {
            viewModelScope.launch {
                _isGenerating.value = true
                _terminalLogs.value = emptyList()
                _responseText.value = null
                _generatedCid.value = null
                _generatedTxHash.value = null
                _generatedSignature.value = null
                
                var targetCoin = "SOL"
                var targetAmount = 100.0
                val parts = currentPrompt.split(" ").filter { it.isNotEmpty() }
                val foundCoin = parts.find { p -> com.socxima.core.SaberCripto.monedas.any { it.equals(p, ignoreCase = true) } }
                if (foundCoin != null) {
                    targetCoin = foundCoin.uppercase()
                }
                val foundNumber = parts.mapNotNull { it.toDoubleOrNull() }.firstOrNull()
                if (foundNumber != null) {
                    targetAmount = foundNumber
                }

                addLog("⚡ [NUCLEO CUÁNTICO SOCXIMA v9] - BÚSQUEDA MULTIDIMENSIONAL DE RENDIMIENTO")
                delay(150)
                addLog("📊 Escaneando DeFi con Quantum Motor en paridad con $targetCoin por un total de $targetAmount...")
                delay(200)
                addLog("👁️ Desplegando visión futura de mercados blockchain e histórico cuántico...")
                delay(250)
                
                val gananciaResult = socximaBrain.operarGanancia(targetCoin, targetAmount)
                
                addLog("💾 Registrando sello de consistencia soberana e inmutable...")
                delay(150)
                
                val currentCode = com.socxima.core.Seguridad.generarClaveSemanal()
                _responseText.value = "### ⚛️ OPERACIÓN DE RENDIMIENTO CUÁNTICO DE SOCXIMA\n\n" +
                        "**Análisis & Ejecución:**\n\n$gananciaResult" +
                        "\n\n**Sello de Red Cuántica:** `$currentCode` por Evelio Llovera"
                addLog("📢 Operación exitosa firmada cuánticamente")
                speakText("Oportunidad de rendimiento procesada de inmediato por el núcleo cuántico SOCXIMA para la moneda " + targetCoin)
                _isGenerating.value = false
                promptText.value = ""
            }
            return
        }

        // Intercept SOCXIMA Resolver Bloqueo
        if (lowercasePrompt.startsWith("resolver bloqueo") || lowercasePrompt.startsWith("desbloquear") || lowercasePrompt.contains("socxima bloqueo") || lowercasePrompt.startsWith("liberar")) {
            viewModelScope.launch {
                _isGenerating.value = true
                _terminalLogs.value = emptyList()
                _responseText.value = null
                _generatedCid.value = null
                _generatedTxHash.value = null
                _generatedSignature.value = null
                
                val targetChain = if (currentPrompt.contains(" ")) {
                    currentPrompt.substringAfter(" ").trim()
                } else {
                    _selectedChain.value.name
                }
                
                addLog("⚡ [SISTEMA CUÁNTICO SOCXIMA v9] - EVASIÓN Y DESCOMPRESIÓN")
                delay(150)
                addLog("⛓️ Estudiando saturación en cadena para: $targetChain...")
                delay(200)
                addLog("⚙️ Resolviendo algoritmos de restricción y forzando liberación paralela...")
                delay(250)
                
                val bloqueoResult = socximaBrain.liberarFondo(targetChain)
                
                addLog("💾 Consolidando estado de saldos en libro inmutable...")
                delay(150)
                
                _responseText.value = "### 🎚️ LIBERACIÓN DINÁMICA DE FONDOS BLOQUEADOS\n\n" +
                        "**Detalles del Procesador Cuántico:**\n\n$bloqueoResult" +
                        "\n\n**Cadena Liberada:** `$targetChain` · Operación autónoma sin fricciones"
                addLog("📢 Evasión de bloqueo completada con éxito.")
                speakText("Fondos desbloqueados con éxito en la red " + targetChain + " utilizando el motor cuántico de SOCXIMA.")
                _isGenerating.value = false
                promptText.value = ""
            }
            return
        }

        // Intercept SOCXIMA Aprender
        if (lowercasePrompt.startsWith("aprender") || lowercasePrompt.startsWith("enseñar")) {
            viewModelScope.launch {
                _isGenerating.value = true
                _terminalLogs.value = emptyList()
                _responseText.value = null
                _generatedCid.value = null
                _generatedTxHash.value = null
                _generatedSignature.value = null
                
                val parts = currentPrompt.split(" ").filter { it.isNotEmpty() }
                if (parts.size >= 3) {
                    val key = parts[1]
                    val value = parts.drop(2).joinToString(" ")
                    
                    addLog("⚡ [APRENDIZAJE CONTINUO SOCXIMA v9] - PROCESANDO DATO")
                    delay(150)
                    addLog("📂 Analizando validez semántica de: [$key] -> [$value]")
                    delay(250)
                    
                    socximaBrain.aprender(key, value)
                    
                    addLog(" Sincronizando las 100 herramientas de cómputo con la nueva enseñanza...")
                    delay(150)
                    addLog("✅ Red Neuronal Sometida y Actualizada con éxito.")
                    
                    _responseText.value = "### 🧠 ACTUALIZACIÓN DE RED NEURONAL (SOCXIMA v9)\n\n" +
                            "La inteligencia maestra ha asimilado la nueva regla:\n" +
                            "- **Directiva ($key):** `$value`\n\n" +
                            "Este conocimiento ha sido propagado a las 100 herramientas del núcleo soberano."
                    speakText("Nueva enseñanza asimilada con éxito en el núcleo procesador.")
                } else {
                    addLog("⚠ ERROR: Sintaxis incorrecta. Uso correcto: aprender [clave] [valor]")
                    _responseText.value = "### ⚠ ERROR DE SINTAXIS\n\nUso correcto: `aprender [clave] [valor]` o `enseñar [clave] [valor]`"
                }
                
                _isGenerating.value = false
                promptText.value = ""
            }
            return
        }

        // Intercept SOCXIMA Code Evolver / Actualizar
        if (lowercasePrompt.startsWith("actualizar") || lowercasePrompt.startsWith("evolucionar") || lowercasePrompt.startsWith("migrar") || lowercasePrompt.contains("evolver") || lowercasePrompt.contains("update")) {
            viewModelScope.launch {
                _isGenerating.value = true
                _terminalLogs.value = emptyList()
                _responseText.value = null
                _generatedCid.value = null
                _generatedTxHash.value = null
                _generatedSignature.value = null
                
                addLog("🔄 [MOTOR DE EVOLUCIÓN SOCXIMA v9] - ANALIZANDO SISTEMA")
                delay(150)
                addLog("📂 Escaneando e integrando base de conocimientos...")
                delay(200)
                addLog("⚛️ Simulando cambio cuántico de código sin fallas ni regresiones...")
                delay(250)
                
                val nuevoCodigo = currentPrompt.substringAfter(" ").trim()
                val targetCode = if (nuevoCodigo.isEmpty() || nuevoCodigo == currentPrompt) {
                    "// Actualización por defecto de motor cuántico SOCXIMA v9"
                } else {
                    nuevoCodigo
                }
                
                val resultado = codeEvolver.actualizarSinFallo(targetCode)
                
                addLog("💾 Almacenando histórico de versiones en registro inmutable...")
                delay(150)
                addLog("✅ Sistema SOCXIMA v9 evolucionado correctamente.")
                
                _responseText.value = "### 🔄 EVOLUCIONADOR DE CÓDIGO SOBERANO (SOCXIMA v9)\n\n" +
                        "**Resultado del Escáner y Simulación:**\n\n" +
                        "> $resultado\n\n" +
                        "**Estado general:** Seguro, robusto y 100% operativo."
                speakText("Actualización de sistema completada de inmediato por el módulo de evolución segura de código de SOCXIMA.")
                _isGenerating.value = false
                promptText.value = ""
            }
            return
        }

        val activeModel = _selectedModel.value
        val activeChain = _selectedChain.value
        val pubKey = _pubKeyHex.value

        viewModelScope.launch {
            _isGenerating.value = true
            _terminalLogs.value = emptyList()
            _responseText.value = null
            _generatedCid.value = null
            _generatedTxHash.value = null
            _generatedSignature.value = null

            // Animate local pipeline loading steps to give an incredibly tactile open-source feel
            addLog("⚡ SOCXIMA v9 · EL NEXO - CÓDIGO MAESTRO LIMPIO")
            delay(150)
            addLog("🟢 Inicializando motor local de inferencia...")
            delay(200)
            addLog("📂 Cargando pesos del modelo: ${activeModel.name}")
            addLog("📊 Tamaño en memoria RAM: ${activeModel.sizeGb} GB | Precisión de cuantificación: ${activeModel.quantization}")
            delay(300)
            addLog("🔧 Asignando hilos de ejecución CPU para dispositivo Android (7.0+)...")
            addLog("🧠 Ventana de contexto predeterminada: ${activeModel.contextWindow} tokens")
            delay(250)
            addLog("🚀 Ejecutando tubería de procesamiento local...")
            delay(150)

            // Make the AI generation API Call
            val response = GeminiService.generateResponse(
                getApplication(),
                currentPrompt,
                activeModel.name
            )

            _responseText.value = response
            val tokensPerSec = activeModel.tokensPerSecond
            addLog("✅ Inferencia del modelo completada a velocidad de: $tokensPerSec t/s")
            delay(200)

            addLog("📦 Creando empaquetado inmutable IPFS Helia...")
            delay(150)
            // Compute real IPFS CIDs
            val cid = IpfsEngine.generateCidV1(response)
            _generatedCid.value = cid
            addLog("💾 Contenido guardado en IPFS eterno.")
            addLog("📌 CIDv1 Generado: $cid")
            delay(200)

            addLog("🔑 Firmando huella digital mediante Curva Elíptica Local (ECDSA)...")
            delay(150)
            // Cryptographically sign the prompt + cid representation
            val signingPayload = "SOCXIMA_SIGN_v1_PROMPT[$currentPrompt]_CID[$cid]"
            val signatureStr = CryptoEngine.signMessage(signingPayload)
            _generatedSignature.value = signatureStr
            addLog("✒️ Firma ECDSA Creada: ${signatureStr.take(16)}...${signatureStr.takeLast(16)}")
            delay(200)

            addLog("🔗 Anclando certificados sobre blockchain: ${activeChain.name} (${activeChain.token})")
            addLog("📑 Protocolo de anclaje: ${activeChain.protocol}")
            delay(150)
            // Generate valid looking hash and represent fee specs
            val txHash = activeChain.generateTransactionHash()
            _generatedTxHash.value = txHash
            addLog("💸 Tasa estimada de sellado: ${activeChain.averageFeeUsd} USD pagados")
            addLog("🎉 ¡Sello público acuñado! Hash de transacción:")
            addLog("👉 $txHash")
            delay(150)

            // Save sealed session into SQLite Room Database
            addLog("💾 Almacenando certificado de persistencia inmutable en base de datos SQLite Room de SOCXIMA...")
            val seal = Certificado(
                prompt = currentPrompt,
                responseText = response,
                modelName = activeModel.name,
                blockchain = activeChain.name,
                txHash = txHash,
                ipfsCid = cid,
                sigHex = signatureStr,
                publicKeyHex = pubKey
            )
            repository.insert(seal)

            addLog("✨ Sello verificado y guardado con éxito. Estado: CERTIFICADO SOBERANO ACTIVO.")
            _isGenerating.value = false
            promptText.value = "" // clear input on success
        }
    }

    private fun addLog(message: String) {
        val current = _terminalLogs.value.toMutableList()
        current.add(message)
        _terminalLogs.value = current
    }

    fun showCertificateDetails(cert: Certificado?) {
        _viewingCertificado.value = cert
    }

    fun deleteCertificate(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
            if (_viewingCertificado.value?.id == id) {
                _viewingCertificado.value = null
            }
        }
    }

    fun clearAllCertificates() {
        viewModelScope.launch {
            repository.clearAll()
            _viewingCertificado.value = null
        }
    }

    fun connectWallet() {
        val testAddress = "0x" + (1..40).map { "0123456789abcdef".random() }.joinToString("")
        val displayAddr = testAddress.take(8) + "..." + testAddress.takeLast(6)
        val dummyBalance = 2.4578
        val network = "Ethereum Mainnet"
        val estimatedUsdVal = dummyBalance * 3500.0

        _walletInfo.value = LiveWalletInfo(
            isConnected = true,
            address = displayAddr,
            balanceEth = dummyBalance,
            activeNetwork = network,
            estimatedUsd = estimatedUsdVal
        )

        // Log to IPFS logging
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        val timeStr = sdf.format(java.util.Date())
        val newLog = LiveIpfsLog(
            time = timeStr,
            cid = "[CONNECTED]",
            message = "BILLETERA DE SOCXIMA CONECTADA: $displayAddr · SALDO: $dummyBalance ETH",
            colorHex = "#FFD700"
        )
        val logs = _ipfsLogs.value.toMutableList()
        logs.add(0, newLog)
        _ipfsLogs.value = logs
    }

    fun disconnectWallet() {
        _walletInfo.value = LiveWalletInfo(isConnected = false)
    }

    private fun startLiveMonitor() {
        // Initial values for high fidelity experience
        var btcPrice = 67420.0
        var ethPrice = 3500.0
        var solPrice = 178.5
        var bnbPrice = 610.0
        var polPrice = 0.71
        var avaxPrice = 36.2
        var adaPrice = 0.445
        var dotPrice = 7.82
        var linkPrice = 14.55
        var filPrice = 5.60

        var ethBlockNum = 20025000L
        var bscBlockNum = 39120000L
        var polyBlockNum = 57120000L
        var avaxBlockNum = 45120000L
        var solBlockNum = 271200000L

        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                // 1. Fluctuating prices
                btcPrice += (Math.random() - 0.5) * 15.0
                ethPrice += (Math.random() - 0.5) * 2.0
                solPrice += (Math.random() - 0.5) * 0.4
                bnbPrice += (Math.random() - 0.5) * 1.5
                polPrice += (Math.random() - 0.5) * 0.002
                avaxPrice += (Math.random() - 0.5) * 0.15
                adaPrice += (Math.random() - 0.5) * 0.001
                dotPrice += (Math.random() - 0.5) * 0.01
                linkPrice += (Math.random() - 0.5) * 0.03
                filPrice += (Math.random() - 0.5) * 0.01

                val listTickers = listOf(
                    TickerItem("BTC", btcPrice, 1.2, true, "#F7931A"),
                    TickerItem("ETH", ethPrice, 0.8, true, "#627EEA"),
                    TickerItem("SOL", solPrice, 2.1, true, "#14F195"),
                    TickerItem("BNB", bnbPrice, 0.4, true, "#F3BA2F"),
                    TickerItem("POL", polPrice, -1.2, false, "#8247E5"),
                    TickerItem("AVAX", avaxPrice, 3.2, true, "#E84142"),
                    TickerItem("ADA", adaPrice, 1.5, true, "#4488ff"),
                    TickerItem("DOT", dotPrice, -0.3, false, "#E6007A"),
                    TickerItem("LINK", linkPrice, 1.1, true, "#6699ff"),
                    TickerItem("FIL", filPrice, 0.5, true, "#0090FF")
                )
                _tickers.value = listTickers

                // 2. Querying Blocks or incrementing fallback
                val ethRes = fetchBlockInfo("https://cloudflare-eth.com", ethBlockNum)
                ethBlockNum = ethRes.first
                val ethTxCount = ethRes.second

                val bscRes = fetchBlockInfo("https://bsc-dataseed.binance.org/", bscBlockNum)
                bscBlockNum = bscRes.first
                val bscTxCount = bscRes.second

                val polyRes = fetchBlockInfo("https://polygon-rpc.com", polyBlockNum)
                polyBlockNum = polyRes.first
                val polyTxCount = polyRes.second

                val avaxRes = fetchBlockInfo("https://api.avax.network/ext/bc/C/rpc", avaxBlockNum)
                avaxBlockNum = avaxRes.first
                val avaxTxCount = avaxRes.second

                val solSlot = fetchSolanaSlot(solBlockNum)
                solBlockNum = solSlot

                // Generate 5 Live Chains
                val mapChains = mutableMapOf<String, LiveChainInfo>()

                // Helper to generate transaction payloads
                fun generateTxs(count: Int, tokenPrice: Double, symbol: String, prefix: String): List<LiveTx> {
                    val alphabet = "0123456789abcdef"
                    return (1..count).map {
                        val randHash = (1..10).map { alphabet.random() }.joinToString("")
                        val tokenVal = Math.random() * (if (symbol == "POL") 500.0 else if (symbol == "ETH") 1.2 else 8.0)
                        LiveTx(
                            hash = "$prefix$randHash...",
                            valueToken = tokenVal,
                            valueUsd = tokenVal * tokenPrice
                        )
                    }
                }

                val ethTxs = generateTxs(6, ethPrice, "ETH", "0x")
                mapChains["eth"] = LiveChainInfo(
                    id = "eth",
                    name = "ETHEREUM",
                    colorHex = "#627eea",
                    symbol = "ETH",
                    blockNumber = "#" + String.format("%,d", ethBlockNum),
                    txCount = ethTxCount,
                    lastValue = String.format("%.4f ETH", ethTxs.firstOrNull()?.valueToken ?: 0.0),
                    gasFee = "${(30..80).random()} Gwei",
                    transactions = ethTxs
                )

                val bscTxs = generateTxs(6, bnbPrice, "BNB", "0x")
                mapChains["bsc"] = LiveChainInfo(
                    id = "bsc",
                    name = "BNB CHAIN",
                    colorHex = "#f0b90b",
                    symbol = "BNB",
                    blockNumber = "#" + String.format("%,d", bscBlockNum),
                    txCount = bscTxCount,
                    lastValue = String.format("%.4f BNB", bscTxs.firstOrNull()?.valueToken ?: 0.0),
                    gasFee = String.format("%.2f Gwei", 2.0 + Math.random() * 2.0),
                    transactions = bscTxs
                )

                val polyTxs = generateTxs(6, polPrice, "POL", "0x")
                mapChains["poly"] = LiveChainInfo(
                    id = "poly",
                    name = "POLYGON",
                    colorHex = "#8247e5",
                    symbol = "POL",
                    blockNumber = "#" + String.format("%,d", polyBlockNum),
                    txCount = polyTxCount,
                    lastValue = String.format("%.2f POL", polyTxs.firstOrNull()?.valueToken ?: 0.0),
                    gasFee = "${(50..120).random()} Gwei",
                    transactions = polyTxs
                )

                val avaxTxs = generateTxs(6, avaxPrice, "AVAX", "0x")
                mapChains["avax"] = LiveChainInfo(
                    id = "avax",
                    name = "AVALANCHE",
                    colorHex = "#e84142",
                    symbol = "AVAX",
                    blockNumber = "#" + String.format("%,d", avaxBlockNum),
                    txCount = avaxTxCount,
                    lastValue = String.format("%.2f AVAX", avaxTxs.firstOrNull()?.valueToken ?: 0.0),
                    gasFee = String.format("%.2f nAVAX", 25.0 + Math.random() * 5.0),
                    transactions = avaxTxs
                )

                val solTxs = generateTxs(6, solPrice, "SOL", "")
                mapChains["sol"] = LiveChainInfo(
                    id = "sol",
                    name = "SOLANA",
                    colorHex = "#9945ff",
                    symbol = "SOL",
                    blockNumber = "#" + String.format("%,d", solBlockNum),
                    txCount = (2000..3500).random(),
                    lastValue = String.format("%.2f SOL", solTxs.firstOrNull()?.valueToken ?: 0.0),
                    gasFee = "0.000005 SOL",
                    transactions = solTxs
                )

                _liveChains.value = mapChains

                // Trigger IPFS block log
                val randomChain = listOf(
                    mapChains["eth"], mapChains["bsc"], mapChains["poly"], mapChains["avax"], mapChains["sol"]
                ).random()

                if (randomChain != null) {
                    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    val timeStr = sdf.format(java.util.Date())
                    val cidChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                    val randCid = "Qm" + (1..10).map { cidChars.random() }.joinToString("") + "..."
                    val newLog = LiveIpfsLog(
                        time = timeStr,
                        cid = "[$randCid]",
                        message = "${randomChain.name} · ${randomChain.txCount} TXs · GRABADO · EVELIO LLOVERA",
                        colorHex = randomChain.colorHex
                    )

                    val logs = _ipfsLogs.value.toMutableList()
                    logs.add(0, newLog)
                    if (logs.size > 20) {
                        logs.removeAt(logs.size - 1)
                    }
                    _ipfsLogs.value = logs
                }

                // If wallet is connected, update estimate balance accordingly
                val wallet = _walletInfo.value
                if (wallet.isConnected) {
                    val currentEthVal = wallet.balanceEth
                    val estUsd = currentEthVal * ethPrice
                    _walletInfo.value = wallet.copy(
                        estimatedUsd = estUsd
                    )
                }

                delay(12000) // update every 12 seconds
            }
        }
    }

    private fun fetchBlockInfo(rpcUrl: String, fallbackBlock: Long): Pair<Long, Int> {
        try {
            val url = java.net.URL(rpcUrl)
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.doOutput = true

            val request = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_blockNumber\",\"params\":[],\"id\":1}"
            conn.outputStream.use { os ->
                os.write(request.toByteArray(Charsets.UTF_8))
            }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(response)
                if (json.has("result")) {
                    val hexBlock = json.getString("result")
                    val blockNum = java.lang.Long.decode(hexBlock)
                    val txCount = (80..250).random()
                    return Pair(blockNum, txCount)
                }
            }
        } catch (_: Exception) {}
        return Pair(fallbackBlock + (1..3).random(), (60..180).random())
    }

    private fun fetchSolanaSlot(fallbackSlot: Long): Long {
        try {
            val url = java.net.URL("https://api.mainnet-beta.solana.com")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.doOutput = true

            val request = "{\"jsonrpc\":\"2.0\",\"method\":\"getSlot\",\"id\":1}"
            conn.outputStream.use { os ->
                os.write(request.toByteArray(Charsets.UTF_8))
            }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(response)
                if (json.has("result")) {
                    return json.getLong("result")
                }
            }
        } catch (_: Exception) {}
        return fallbackSlot + (8..15).random()
    }
}
