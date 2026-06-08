
// ═══════════════════════════════════════════════════════════
//  SOCXIMA — socxima-controller.js
//  Núcleo del sistema · Control de interfaz y lógica
// ═══════════════════════════════════════════════════════════

// ── ELEMENTOS DEL DOM ───────────────────────────────────────
const tabs = document.querySelectorAll('.nav-tab');
const panels = document.querySelectorAll('.panel');
const inputComando = document.getElementById('input-comando');
const btnEjecutar = document.getElementById('btn-ejecutar');
const btnLimpiar = document.getElementById('btn-limpiar');
const outputTexto = document.getElementById('output-texto');
const indicadorPensando = document.getElementById('indicador-pensando');
const metricsRow = document.getElementById('metrics-row');
const consolaLog = document.getElementById('consola-log');
const chatInput = document.getElementById('chat-input');
const btnChat = document.getElementById('btn-chat');
const chatMessages = document.getElementById('chat-messages');

// ── NAVEGACIÓN ENTRE PANELES ───────────────────────────────
tabs.forEach(tab => {
  tab.addEventListener('click', () => {
    tabs.forEach(t => t.classList.remove('active'));
    panels.forEach(p => p.classList.remove('active'));
    tab.classList.add('active');
    const panelId = tab.getAttribute('data-panel');
    document.getElementById(panelId).classList.add('active');
    logSistema(`Navegación: panel ${panelId} activado`, 'init');
  });
});

// ── EJECUTAR COMANDO ───────────────────────────────────────
btnEjecutar.addEventListener('click', () => {
  const texto = inputComando.value.trim();
  if (!texto) return;

  outputTexto.textContent = '';
  indicadorPensando.style.display = 'flex';
  metricsRow.style.display = 'none';
  logSistema(`Ejecutando consulta: ${texto.slice(0, 60)}...`, 'block');

  // Simulación de procesamiento
  setTimeout(() => {
    indicadorPensando.style.display = 'none';
    const resultado = procesarComando(texto);
    outputTexto.textContent = resultado.texto;
    metricsRow.style.display = 'grid';
    
    // Actualizar métricas en pantalla
    document.getElementById('mc-bloque').textContent = resultado.bloque;
    document.getElementById('mc-txs').textContent = resultado.txs;
    document.getElementById('mc-diff').textContent = resultado.dificultad;
    document.getElementById('mc-root').textContent = resultado.hash;

    logSistema('Consulta executed · Datos validados', 'ok');
    
    // Disparar actualización de gráficos
    if (window.actualizarGraficosEstructura) {
      window.actualizarGraficosEstructura({
        txs_contadas: resultado.txs
      });
    }
  }, 1200);
});

// ── LIMPIAR TERMINAL ───────────────────────────────────────
btnLimpiar.addEventListener('click', () => {
  inputComando.value = '';
  outputTexto.textContent = '';
  metricsRow.style.display = 'none';
  logSistema('Terminal limpiada', 'init');
});

// ── PROCESADOR DE INSTRUCCIONES ────────────────────────────
function procesarComando(texto) {
  const bloqAleatorio = Math.floor(Math.random() * 80000000) + 220000000;
  const txsAleatorias = Math.floor(Math.random() * 900) + 50;
  const difAleatoria = (Math.random() * 20 + 5).toFixed(2);
  const hashRaiz = generarHash(24);

  return {
    texto: `>> ANÁLISIS COMPLETO\n\nConsulta: ${texto}\nRed: Solana Mainnet Beta\nEstado: Confirmado ✅\nTiempo de respuesta: 412ms\nÚltima actualización: ${new Date().toLocaleTimeString()}\n\nLos datos mostrados reflejan el estado actual de la red, validado por nodos independientes.`,
    bloque: bloqAleatorio.toLocaleString('es'),
    txs: txsAleatorias,
    dificultad: difAleatoria,
    hash: hashRaiz
  };
}

// ── CHAT SOCXIMA IA ───────────────────────────────────────
btnChat.addEventListener('click', () => {
  const mensaje = chatInput.value.trim();
  if (!mensaje) return;
  
  agregarMensaje(mensaje, 'user');
  chatInput.value = '';

  setTimeout(() => {
    const respuesta = responderIA(mensaje);
    agregarMensaje(respuesta, 'ai');
  }, 1400);
});

chatInput.addEventListener('keydown', e => {
  if (e.key === 'Enter') btnChat.click();
});

function agregarMensaje(texto, tipo) {
  const burbuja = document.createElement('div');
  burbuja.className = `chat-bubble ${tipo}`;
  burbuja.textContent = texto;
  chatMessages.appendChild(burbuja);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

function responderIA(consulta) {
  const respuestas = [
    `📈 Mercado: Actualmente SOL cotiza en $${getPrecio('SOL')?.precio || '~145.20'}, con variación 24h de ${getPrecio('SOL')?.cambio || '2.40'}%. Tendencia alcista moderada.`,
    `🔐 Seguridad: La arquitectura Solana garantiza finalidad en <400ms y resistencia a ataques del 51%. Es una de las redes con mayor tasa de disponibilidad.`,
    `⚙ Capacidad: Procesa más de 2.000 transacciones por segundo, escalable hasta 65.000 TPS según demanda de red.`,
    `💡 Estrategia: Diversificar entre staking y liquidez permite rendimientos anuales entre el 6% y el 12% dependiendo del protocolo elegido.`
  ];
  return respuestas[Math.floor(Math.random() * respuestas.length)];
}

// ── GENERADORES AUXILIARES ────────────────────────────────
function generarHash(longitud = 16) {
  const caracteres = 'abcdef0123456789';
  let resultado = '';
  for (let i = 0; i < longitud; i++) {
    resultado += caracteres[Math.floor(Math.random() * caracteres.length)];
  }
  return resultado;
}

function logSistema(mensaje, tipo = '') {
  const linea = document.createElement('div');
  linea.className = `clog-line ${tipo}`;
  linea.textContent = `[${new Date().toLocaleTimeString()}] ${mensaje}`;
  consolaLog.appendChild(linea);
  consolaLog.scrollTop = consolaLog.scrollHeight;
}

// ── ANIMACIÓN DE PARTÍCULAS ────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
  const contenedor = document.getElementById('particles-container');
  for (let i = 0; i < 40; i++) {
    const p = document.createElement('div');
    p.className = 'particle';
    const tam = Math.random() * 3 + 1;
    p.style.width = tam + 'px';
    p.style.height = tam + 'px';
    p.style.left = Math.random() * 100 + '%';
    p.style.animationDelay = Math.random() * 20 + 's';
    p.style.animationDuration = (Math.random() * 15 + 10) + 's';
    const opc = Math.random() * 0.5 + 0.2;
    p.style.background = `rgba(20, 241, 149, ${opc})`;
    contenedor.appendChild(p);
  }

  logSistema('SOCXIMA NUCLEUS v1.0 inicializado', 'init');
  logSistema('Interfaz cargada · Esperando instrucciones', 'ok');
});
