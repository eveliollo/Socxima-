// ═══════════════════════════════════════════════════════════
//  SOCXIMA — graficos.js
//  Generación y actualización de gráficos en tiempo real
// ═══════════════════════════════════════════════════════════

let graficoTxs;
let graficoEstructura;

// Paleta Solana
const colores = {
  solPurple: 'rgba(153, 69, 255, 0.85)',
  solGreen: 'rgba(20, 241, 149, 0.85)',
  solCyan: 'rgba(0, 194, 255, 0.85)',
  solMagenta: 'rgba(220, 31, 255, 0.85)',
  fondo: 'rgba(3, 0, 10, 0.4)',
  linea: 'rgba(180, 160, 255, 0.2)'
};

// ── INICIALIZACIÓN ──────────────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
  crearGraficoTransacciones();
  crearGraficoEstructura();

  setInterval(actualizarDatosGraficos, 6000);
});

// ── GRÁFICO DE TRANSACCIONES ────────────────────────────────
function crearGraficoTransacciones() {
  const ctx = document.getElementById('chart-txs').getContext('2d');

  const datosIniciales = Array(12).fill(0).map(() => Math.floor(Math.random() * 400) + 100);
  const etiquetas = Array(12).fill('').map((_, i) => `${i * 5}s`);

  graficoTxs = new Chart(ctx, {
    type: 'line',
    data: {
      labels: etiquetas,
      datasets: [{
        label: 'Transacciones confirmadas',
        data: datosIniciales,
        borderColor: colores.solGreen,
        backgroundColor: colores.solGreen.replace('0.85', '0.12'),
        borderWidth: 2,
        pointRadius: 3,
        pointBackgroundColor: colores.solGreen,
        pointBorderColor: 'transparent',
        tension: 0.35,
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 600 },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#0A0518',
          titleColor: '#FFF',
          bodyColor: '#FFF',
          borderColor: colores.solPurple,
          borderWidth: 1,
          padding: 10,
          font: { family: "'Rajdhani', sans-serif" }
        }
      },
      scales: {
        x: {
          grid: { color: colores.linea },
          ticks: { color: '#9988BB', font: { family: "'Share Tech Mono', monospace" } }
        },
        y: {
          grid: { color: colores.linea },
          ticks: { color: '#9988BB', font: { family: "'Share Tech Mono', monospace" } },
          beginAtZero: true
        }
      }
    }
  });
}

// ── GRÁFICO DE ESTRUCTURA DE BLOQUES ────────────────────────
function crearGraficoEstructura() {
  const ctx = document.getElementById('chart-estructura').getContext('2d');

  graficoEstructura = new Chart(ctx, {
    type: 'radar',
    data: {
      labels: ['Velocidad', 'Seguridad', 'Escalabilidad', 'Liquidez', 'Descentralización', 'Utilidad'],
      datasets: [{
        label: 'SOCXIMA · Nivel de Red',
        data: [92, 88, 95, 79, 83, 91],
        backgroundColor: colores.solPurple.replace('0.85', '0.18'),
        borderColor: colores.solPurple,
        pointBackgroundColor: colores.solPurple,
        pointBorderColor: '#03000A',
        pointHoverRadius: 5,
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 800 },
      plugins: { legend: { display: false } },
      scales: {
        r: {
          angleLines: { color: colores.linea },
          grid: { color: colores.linea },
          pointLabels: { color: '#E0D6FF', font: { size: 12, family: "'Orbitron', sans-serif" } },
          ticks: { display: false, beginAtZero: true }
        }
      }
    }
  });
}

// ── ACTUALIZACIÓN PERIÓDICA ─────────────────────────────────
function actualizarDatosGraficos() {
  const nuevoValor = Math.floor(Math.random() * 450) + 50;

  graficoTxs.data.datasets[0].data.shift();
  graficoTxs.data.datasets[0].data.push(nuevoValor);
  graficoTxs.update('none');
}

// ── ACTUALIZAR DESDE EL CONTROLADOR ─────────────────────────
window.actualizarGraficosEstructura = function(datos) {
  const nuevosValores = [
    90 + Math.floor(Math.random() * 8),
    85 + Math.floor(Math.random() * 7),
    91 + Math.floor(Math.random() * 6),
    76 + Math.floor(Math.random() * 9),
    80 + Math.floor(Math.random() * 8),
    88 + Math.floor(Math.random() * 7)
  ];

  graficoEstructura.data.datasets[0].data = nuevosValores;
  graficoEstructura.update();
};
