// ====================================================================
// @project      SOCXIMA NEURAL UI - Core Quantum-Agnostic v3.5
// @description  Pipeline de Renderizado Tensorial con Física de Resortes
// ====================================================================

class ElasticTensorNode {
    constructor(valorInicial, masa = 1.2, amortiguacion = 0.15, tension = 0.3) {
        this.target = valorInicial;
        this.current = valorInicial;
        this.velocity = 0;
        this.m = masa;
        this.d = amortiguacion;
        this.k = tension;
    }

    update() {
        const fuerzaResorte = -this.k * (this.current - this.target);
        const fuerzaAmortiguacion = -this.d * this.velocity;
        const aceleracion = (fuerzaResorte + fuerzaAmortiguacion) / this.m;
        
        this.velocity += aceleracion;
        this.current += this.velocity;
    }
}

class SOCXIMANeuralRenderer {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) return;
        this.ctx = this.canvas.getContext('2d');
        this.nodosInterpolados = Array.from({ length: 12 }, () => new ElasticTensorNode(15));
        
        this.initViewportQuantum();
        this.orchestrateRenderLoop();
    }

    initViewportQuantum() {
        const resizeObserver = new ResizeObserver(entries => {
            for (let entry of entries) {
                const dpr = window.devicePixelRatio || 1;
                const { width, height } = entry.contentRect;
                this.canvas.width = width * dpr;
                this.canvas.height = (height || 320) * dpr;
                this.ctx.scale(dpr, dpr);
            }
        });
        resizeObserver.observe(this.canvas.parentElement);
    }

    streamTensorData(nuevosTensores) {
        if (!Array.isArray(nuevosTensores) || nuevosTensores.length !== 12) return;
        nuevosTensores.forEach((valor, i) => {
            this.nodosInterpolados[i].target = valor;
        });
    }

    orchestrateRenderLoop() {
        const render = () => {
            const w = this.canvas.clientWidth;
            const h = this.canvas.clientHeight;
            this.ctx.clearRect(0, 0, w, h);

            // Rejilla de fondo
            this.ctx.strokeStyle = 'rgba(0, 240, 255, 0.04)';
            this.ctx.lineWidth = 0.5;
            const stepX = w / 11;
            const stepY = h / 7;
            
            for (let i = 0; i <= w; i += stepX) {
                this.ctx.beginPath(); this.ctx.moveTo(i, 0); this.ctx.lineTo(i, h); this.ctx.stroke();
            }
            for (let i = 0; i <= h; i += stepY) {
                this.ctx.beginPath(); this.ctx.moveTo(0, i); this.ctx.lineTo(w, i); this.ctx.stroke();
            }

            this.nodosInterpolados.forEach(nodo => nodo.update());

            // Trazado de la curva neón
            this.ctx.beginPath();
            this.ctx.lineWidth = 4.5;
            this.ctx.lineCap = 'round';

            const gradient = this.ctx.createLinearGradient(0, 0, w, 0);
            gradient.addColorStop(0, '#00e5ff');
            gradient.addColorStop(0.5, '#00ff66');
            gradient.addColorStop(1, '#7f00ff');
            this.ctx.strokeStyle = gradient;

            this.ctx.shadowBlur = 15;
            this.ctx.shadowColor = 'rgba(0, 255, 102, 0.4)';

            for (let i = 0; i < this.nodosInterpolados.length - 1; i++) {
                const x1 = i * stepX;
                const y1 = h - (this.nodosInterpolados[i].current * (h / 32));
                const x2 = (i + 1) * stepX;
                const y2 = h - (this.nodosInterpolados[i + 1].current * (h / 32));

                const cpX1 = x1 + (x2 - x1) / 2;
                const cpY1 = y1;
                const cpX2 = x1 + (x2 - x1) / 2;
                const cpY2 = y2;

                if (i === 0) this.ctx.moveTo(x1, y1);
                this.ctx.bezierCurveTo(cpX1, cpY1, cpX2, cpY2, x2, y2);
            }
            this.ctx.stroke();
            this.ctx.shadowBlur = 0;

            requestAnimationFrame(render);
        };
        requestAnimationFrame(render);
    }
}

// Inicialización e Interacción
document.addEventListener("DOMContentLoaded", () => {
    window.SOCXIMA_CORE_UI = new SOCXIMANeuralRenderer("miGrafica");
});

window.procesarPromptLocal = function() {
    const input = document.getElementById("promptInput");
    if (!input) return;

    const texto = input.value.trim();
    
    // Si está vacío, genera un pulso caótico aleatorio de prueba
    if (texto === "") {
        const pulsoAleatorio = Array.from({length: 12}, () => Math.floor(Math.random() * 22) + 5);
        window.SOCXIMA_CORE_UI.streamTensorData(pulsoAleatorio);
        return;
    }

    const numChars = texto.length;
    let nuevosTensores = [];
    
    // Algoritmo armónico local para calcular la deformación de la onda
    for(let i = 0; i < 12; i++) {
        let onda1 = Math.sin(i * 0.5 + numChars) * 8;
        let onda2 = Math.cos(i * 0.3 + numChars * 2) * 4;
        let amplitud = onda1 + onda2 + 15;
        nuevosTensores.push(Math.max(3, Math.min(30, amplitud)));
    }

    window.SOCXIMA_CORE_UI.streamTensorData(nuevosTensores);
    input.value = ""; 
};
