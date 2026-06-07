async function enviar() {
    const input = document.getElementById("mensaje");
    const textoMensaje = input.value.trim();

    if (textoMensaje === "") return;

    imprimirEnConsola(`> ${textoMensaje}`, "msg-usuario");
    input.value = "";

    try {
        const response = await fetch('http://127.0.0.1:8000/consultar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ consulta: textoMensaje })
        });

        if (!response.ok) throw new Error("Sin conexión");

        const data = await response.json();
        imprimirEnConsola(`[SOCXIMA] ${data.respuesta}`, "msg-ia");

    } catch (error) {
        imprimirEnConsola(`❌ ERROR: Inicia primero "python backend-socxima.py" en Termux`, "msg-sistema");
    }
}

function imprimirEnConsola(texto, clase) {
    const historial = document.getElementById("historial");
    const div = document.createElement("div");
    div.className = clase;
    div.innerText = texto;
    historial.appendChild(div);
    
    const chatBox = document.querySelector(".chat-box");
    chatBox.scrollTop = chatBox.scrollHeight;
}
