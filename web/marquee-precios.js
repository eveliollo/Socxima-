// ═══════════════════════════════════════════════════════════
//  SOCXIMA — marquee-precios.js
//  Ticker de mercado en tiempo real · Datos cripto
// ═══════════════════════════════════════════════════════════

// Lista de activos a monitorear
const activos = [
  { id: "solana", nombre: "SOL", color: "var(--sol-purple)" },
  { id: "bitcoin", nombre: "BTC", color: "#F7931A" },
  { id: "ethereum", nombre: "ETH", color: "#627EEA" },
  { id: "cardano", nombre: "ADA", color: "#0033AD" },
  { id: "polygon", nombre: "MATIC", color: "#8247E5" },
  { id: "avalanche", nombre: "AVAX", color: "#E84142" },
  { id: "chainlink", nombre: "LINK", color: "#2A5ADA" },
  { id: "uniswap", nombre: "UNI", color: "#FF007A" },
  { id: "cosmos", nombre: "ATOM", color: "#2E3148" },
  { id: "near", nombre: "NEAR", color: "#000000" }
];

// Datos simulados que se actualizan
let datosMercado = [];

// Referencia al DOM
const contenedorMarquee = document.getElementById('marquee-inner');
window.getPrecio = (simbolo) => datosMercado.find(d => d.simbolo === simbolo);

// ── INICIALIZACIÓN ──────────────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
  actualizarDatos();
  setInterval(actualizarDatos, 7500); // refrescar cada 7.5 segundos
});

// ── ACTUALIZAR VALORES ──────────────────────────────────────
function actualizarDatos() {
  datosMercado = activos.map(activo => {
    const base = basePrecio(activo.nombre);
    const variacion = (Math.random() * 10 - 5).toFixed(2); // ±5%
    const precio = (base * (1 + variacion / 100)).toFixed(2);

    return {
      simbolo: activo.nombre,
      precio: precio,
      cambio: variacion,
      tendencia: variacion >= 0 ? 'mup' : 'mdown'
    };
  });

  dibujarMarquee();
  actualizarCabecera();
}

// ── VALOR BASE SEGÚN ACTIVO ─────────────────────────────────
function basePrecio(simbolo) {
  switch (simbolo) {
    case 'SOL': return 147.25;
    case 'BTC': return 68420.12;
    case 'ETH': return 3480.50;
    case 'ADA': return 0.48;
    case 'MATIC': return 0.62;
    case 'AVAX': return 32.15;
    case 'LINK': return 14.78;
    case 'UNI': return 7.32;
    case 'ATOM': return 9.45;
    case 'NEAR': return 5.12;
    default: return 10.00;
  }
}

// ── RENDERIZAR TICKER ───────────────────────────────────────
function dibujarMarquee() {
  contenedorMarquee.innerHTML = '';

  datosMercado.forEach(act => {
    const item = document.createElement('div');
    item.className = 'marquee-item';
    item.innerHTML = `
      <span class="mticker">${act.simbolo}</span>
      <span class="mprice">$${act.precio}</span>
      <span class="mchange ${act.tendencia}">${act.cambio}%</span>
    `;
    contenedorMarquee.appendChild(item);
  });

  // Duplicar contenido para animación continua sin cortes
  datosMercado.forEach(act => {
    const item = document.createElement('div');
    item.className = 'marquee-item';
    item.innerHTML = `
      <span class="mticker">${act.simbolo}</span>
      <span class="mprice">$${act.precio}</span>
      <span class="mchange ${act.tendencia}">${act.cambio}%</span>
    `;
    contenedorMarquee.appendChild(item);
  });
}

// ── ACTUALIZAR PRECIO EN CABECERA ───────────────────────────
function actualizarCabecera() {
  const sol = datosMercado.find(d => d.simbolo === 'SOL');
  const label = document.getElementById('live-tps');
  if (sol) {
    label.textContent = `SOL $${sol.precio} | ${sol.cambio >= 0 ? '↑' : '↓'} ${sol.cambio}%`;
    label.className = sol.tendencia;
  }
}
