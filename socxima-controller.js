// Función para enviar la billetera y la pregunta al servidor de Pydroid 3
async function enviarMensajeAI() {
    const inputChat = document.getElementById("input-mensaje-usuario");
    const outputChat = document.getElementById("chat-output");
    
    if (!inputChat || !inputChat.value.trim()) return;
    
    const mensaje = inputChat.value;
    
    // Captura la dirección si el usuario generó una llave
    const ethAddr = document.getElementById('pub-address')?.innerText || "0x0";
    
    // Limpiar input y pintar mensaje del usuario en la pantalla
    inputChat.value = "";
    if (outputChat) {
        outputChat.innerHTML += `<div class="chat-bubble-user"><b>Tú:</b> ${mensaje}</div>`;
    }

    try {
        // Petición HTTP directa al puerto 8000 de tu servidor en el teléfono
        const urlAPI = `http://127.0.0.1:8000/api/v1/procesar?wallet=${ethAddr}&pregunta=${encodeURIComponent(mensaje)}`;
        
        const respuesta = await fetch(urlAPI);
        const data = await respuesta.json();

        // Pintar la respuesta inteligente que devolvió Gemini
        if (outputChat) {
            outputChat.innerHTML += `<div class="chat-bubble-ia"><b>SOCXIMA IA:</b> ${data.output_ia}</div>`;
            outputChat.scrollTop = outputChat.scrollHeight; // Auto-scroll al fondo
        }

        // Si la blockchain devolvió un balance real, actualizamos la gráfica
        if (data.blockchain && data.blockchain.balance !== undefined) {
            renderizarGraficoBalance("ETH", data.blockchain.balance);
        }

    } catch (error) {
        console.error("Error conectando con el servidor Pydroid:", error);
        if (outputChat) {
            outputChat.innerHTML += `<div class="chat-bubble-ia" style="color: #ff5555;"><b>Sistema:</b> Error al conectar con el backend. Asegúrate de que Pydroid 3 esté en ejecución.</div>`;
        }
    }
}
