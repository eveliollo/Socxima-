
// Archivo: SOCXIMA/graficos.js
// Renderiza gráficos reales basados en los saldos verídicos de la Blockchain

function renderizarGraficoBalance(moneda, balance) {
    console.log(`Dibujando gráfico real para ${moneda}: ${balance}`);
    
    // Busca o crea un contenedor visual en tu HTML para la gráfica
    let contenedor = document.getElementById("contenedor-graficas");
    if (!contenedor) {
        contenedor = document.createElement("div");
        contenedor.id = "contenedor-graficas";
        document.body.appendChild(contenedor);
    }

    // ID único para la barra de esta moneda
    let idBarra = `barra-${moneda}`;
    let elementoBarra = document.getElementById(idBarra);

    if (!elementoBarra) {
        // Crear la estructura de la gráfica si no existe
        elementoBarra = document.createElement("div");
        elementoBarra.id = idBarra;
        elementoBarra.style.margin = "10px";
        elementoBarra.style.padding = "10px";
        elementoBarra.style.background = "#1e1e1e";
        elementoBarra.style.borderRadius = "8px";
        elementoBarra.style.color = "#fff";
        contenedor.appendChild(elementoBarra);
    }

    // Cambia el tamaño y texto de forma proporcional al balance real
    // Limitamos el ancho visual para que se vea estético
    let anchoVisual = Math.min(balance * 50, 300); 
    
    elementoBarra.innerHTML = `
        <div style="font-weight: bold; margin-bottom: 5px;">${moneda}: ${balance.toFixed(4)}</div>
        <div style="background: #00ffcc; height: 20px; width: ${anchoVisual}px; transition: width 0.5s ease-in-out; border-radius: 4px;"></div>
    `;
}
