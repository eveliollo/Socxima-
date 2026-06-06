// ⚡ SOCXIMA v9 · INTERACTIVIDAD TOTAL DEFINITIVA
// Creado por: Evelio Llovera | 4 Junio 2026
package com.socxima.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.shadow
import com.socxima.core.SocximaBrain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun NexoTerminalScreen() {
    val cerebro = SocximaBrain()
    var activo by remember { mutableStateOf("TERMINAL") }
    var color by remember { mutableStateOf(Color(0xFF14F195)) }
    var entrada by remember { mutableStateOf("") }
    var destello by remember { mutableStateOf(false) }
    val interaccion = remember { MutableInteractionSource() }
    val colores = mapOf(
        "TERMINAL" to Color(0xFF14F195),
        "INTELIGENCIA" to Color(0xFF9945FF),
        "UNION" to Color(0xFFD946EF),
        "MONITOR" to Color(0xFF00F0FF),
        "HISTORIAL" to Color(0xFF3B82F6)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0B0518), Color(0xFF150B30))))
            .padding(16.dp)
    ) {
        // 🧭 NAVEGACIÓN DINÁMICA
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            listOf("TERMINAL", "100", "119", "MONITOR", "LEDGER").forEach { item ->
                val clave = when(item) {
                    "100" -> "INTELIGENCIA"
                    "119" -> "UNION"
                    "LEDGER" -> "HISTORIAL"
                    else -> item
                }
                Box(
                    modifier = Modifier
                        .clickable { activo = clave; color = colores[clave]!! }
                        .padding(vertical = 10.dp, horizontal = 14.dp)
                        .border(1.dp, if (activo == clave) colores[clave]!! else Color.Transparent, RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = item,
                        color = if (activo == clave) colores[clave]!! else Color(0xFF8B7FD0),
                        fontWeight = if (activo == clave) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // 📦 CONTENIDO DINÁMICO SEGÚN PESTAÑA SELECCIONADA
        var resultadoTerminal by remember { mutableStateOf("") }
        val alcance = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (activo) {
                "TERMINAL" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ⌨️ ENTRADA CON BORDE CAMBIANTE
                        OutlinedTextField(
                            value = entrada,
                            onValueChange = { entrada = it },
                            placeholder = { Text("Escribe tu solicitud soberana aquí...", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(8.dp)
                                .border(
                                    width = if (destello) 3.dp else 2.dp,
                                    color = if (destello) Color.White else color,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedContainerColor = Color(0xFF1E1044),
                                unfocusedContainerColor = Color(0xFF1E1044)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🔘 BOTÓN PRINCIPAL CON EFECTO NEÓN Y VIBRACIÓN
                        Button(
                            onClick = {
                                if (entrada.isNotEmpty()) {
                                    alcance.launch {
                                        destello = true
                                        val orden = entrada
                                        resultadoTerminal = com.socxima.cloud.SocximaNube().procesarOrden(orden)
                                        entrada = ""
                                        delay(700)
                                        destello = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .pointerInput(Unit) { detectTapGestures(onPress = { destello = true }) }
                                .shadow(12.dp, shape = RoundedCornerShape(10.dp), ambientColor = color.copy(alpha = 0.6f), spotColor = color.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF251452))
                        ) {
                            Text(
                                text = "PROCESAR Y SELLAR SESIÓN",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (resultadoTerminal.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, color, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1044))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "CONSOLE LOG:",
                                        color = color,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = resultadoTerminal,
                                        color = Color.White,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
                "INTELIGENCIA" -> {
                    com.socxima.cloud.RedNubesPantalla()
                }
                "UNION" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "LA GRAN FUSIÓN",
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = com.socxima.union.GranMente.TOTAL_IAS,
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1044))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "ESTRUCTURA INTERNA:",
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = com.socxima.union.GranMente.composicion,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, color, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF150B30))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "CONEXIÓN DE SOBERANÍA CUÁNTICA:",
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = com.socxima.union.GranMente.activarUnion(),
                                    color = Color.White,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
                "MONITOR" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "DIAGNÓSTICO EN TIEMPO REAL",
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, color, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1044))
                        ) {
                            Text(
                                text = com.socxima.cloud.SocximaNube().verificarRed(),
                                color = Color.White,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
                "HISTORIAL" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "REGISTRO DE SESIÓN (LEDGER HISTORIAL)",
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val logs = listOf(
                            "03:00:54 - INICIO DE SISTEMA SOCXIMA v9",
                            "03:02:11 - CONEXIÓN CON LAS 10 NUBES COMPLETADA",
                            "03:05:00 - CERTIFICADO DIGITAL EVELIO LLOVERA OK",
                            "03:06:59 - ACTUALIZACIÓN CUÁNTICA CON EXITO",
                            "03:14:39 - COMANDO DE PRUEBA EJECUTADO CORRECTAMENTE"
                        )
                        
                        logs.forEach { logLine ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF150B30))
                            ) {
                                Text(
                                    text = logLine,
                                    color = Color.LightGray,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔒 IDENTIDAD FIJA
        Text(
            text = "CREADO POR: EVELIO LLOVERA · SOCXIMA v9",
            color = color,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Light
        )
    }
}
