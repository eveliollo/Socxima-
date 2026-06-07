// Función real para procesar el chat con Inteligencia Artificial
async function enviarMensajeAI() {
    const inputChat = document.getElementById("input-mensaje-usuario");
    const outputChat = document.getElementById("chat-output"); // El contenedor donde se muestran los textos
    
    if (!inputChat || !inputChat.value.trim()) return;
    
    const mensaje = inputChat.value;
    console.log("Enviando mensaje a SOCXIMA IA:", mensaje);
    
    // Obtener las billeteras que están guardadas en la pantalla del HTML
    const ethAddr = document.getElementById('address-evm')?.innerText || "";
    const solAddr = document.getElementById('address-solana')?.innerText || "";
    
    // Limpiar el input de la pantalla para simular fluidez
    inputChat.value = "";
    if (outputChat) outputChat.innerHTML += `<p><b>Tú:</b> ${mensaje}</p>`;

    try {
        const respuesta = await fetch("http://localhost:8000/api/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                eth_address: ethAddr === "Ninguna" ? "" : ethAddr,
                sol_address: solAddr === "Ninguna" ? "" : solAddr,
                mensaje_usuario: mensaje
            })
        });

        const data = await respuesta.json();
        
        if (outputChat) {
            outputChat.innerHTML += `<p style="color: #00ffcc;"><b>SOCXIMA IA:</b> ${data.respuesta}</p>`;
            // Auto-scrollear hacia abajo en el chat
            outputChat.scrollTop = outputChat.scrollHeight;
        }
    } catch (error) {
        console.error("Error conectando con el módulo de IA:", error);
        if (outputChat) outputChat.innerHTML += `<p style="color: red;"><i>Error de conexión con el motor de IA.</i></p>`;
    }
}
