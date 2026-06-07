function enviar() {
    const input = document.getElementById("mensaje");
    const historial = document.getElementById("historial");
    const textoMensaje = input.value.trim();

    if (textoMensaje === "") return;

    // 1. Mostrar mensaje del usuario en el chat
    const usuarioDiv = document.createElement("div");
    usuarioDiv.className = "msg-usuario";
    usuarioDiv.innerText = textoMensaje;
    historial.appendChild(usuarioDiv);

    // Limpiar el input
    input.value = "";

    // Auto-scroll al fondo del chat
    const chatBox = document.querySelector(".chat-box");
    chatBox.scrollTop = chatBox.scrollHeight;

    // 2. Procesar respuesta de SOCXIMA IA (Simulación de Web3/Termux Engine)
    setTimeout(() => {
        const iaDiv = document.createElement("div");
        iaDiv.className = "msg-ia";
        
        // Respuesta lógica basada en palabras clave
        iaDiv.innerText = generarRespuestaIA(textoMensaje);
        
        historial.appendChild(iaDiv);
        chatBox.scrollTop = chatBox.scrollHeight;
    }, 600); // Pequeño retraso para simular "pensamiento" de la IA
}

function generarRespuestaIA(consulta) {
    const texto = consulta.toLowerCase();

    if (texto.includes("bloque")) {
        // Simula datos de la blockchain
        const bloqueRandom = Math.floor(Math.random() * 50000) + 700000;
        return `[SOCXIMA] El bloque actual en la red es el #${bloqueRandom}. Hash verificado por Termux.`;
    } 
    
    if (texto.includes("saldo") || texto.includes("balance") || texto.includes("tokens")) {
        return `[SOCXIMA] Consultando billetera... Saldo: 1,420 SXM (Tokens de red).`;
    }

    if (texto.includes("seguridad") || texto.includes("hack") || texto.includes("encriptado")) {
        return `[SOCXIMA] Alerta: Protocolo TLS/SHA-256 activo. Los nodos en Termux reportan 0 vulnerabilidades actuales.`;
    }

    // Respuesta por defecto
    return `[SOCXIMA] Comando recibido. Estoy analizando la red Web3 para responder a: "${consulta}".`;
}
