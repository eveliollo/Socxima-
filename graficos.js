/* — GRÁFICOS SOCXIMA GOLDEN — */
function drawSparkline(canvasId, data, color = '#00fff7') {
    const canvas = document.getElementById(canvasId);
    if (!canvas || data.length < 2) return;

    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();

    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);

    const W = rect.width;
    const H = rect.height;
    const min = Math.min(...data);
    const max = Math.max(...data);
    const rango = max - min || 1;

    ctx.clearRect(0, 0, W, H);

    // Cuadrícula tecnológica
    ctx.strokeStyle = 'rgba(32, 53, 80, 0.25)';
    ctx.lineWidth = 0.6;
    for (let x = 0; x < W; x += 12) { ctx.beginPath(); ctx.moveTo(x,0); ctx.lineTo(x,H); ctx.stroke(); }
    for (let y = 0; y < H; y += 12) { ctx.beginPath(); ctx.moveTo(0,y); ctx.lineTo(W,y); ctx.stroke(); }

    const points = data.map((val,i) => ({
        x: (i/(data.length-1)) * W,
        y: H - ((val - min)/rango) * (H*0.85) - (H*0.075)
    }));

    // Línea curva neón
    ctx.beginPath();
    for (let i=1; i < points.length-2; i++) {
        const cpX = (points[i].x + points[i+1].x)/2;
        const cpY = (points[i].y + points[i+1].y)/2;
        ctx.bezierCurveTo(cpX, points[i].y, cpX, points[i+1].y, points[i+1].x, points[i+1].y);
    }

    const grad = ctx.createLinearGradient(0,0,W,0);
    grad.addColorStop(0, '#39ff14');
    grad.addColorStop(0.5, color);
    grad.addColorStop(1, '#bf00ff');
    ctx.strokeStyle = grad;
    ctx.lineWidth = 2.5;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.stroke();

    // Punto brillante final
    const lastP = points.at(-1);
    ctx.beginPath();
    ctx.arc(lastP.x, lastP.y, 6, 0, Math.PI*2);
    ctx.fillStyle = color + '44';
    ctx.fill();

    ctx.beginPath();
    ctx.arc(lastP.x, lastP.y, 3, 0, Math.PI*2);
    ctx.fillStyle = '#ffffff';
    ctx.strokeStyle = color;
    ctx.lineWidth = 1.5;
    ctx.fill();
    ctx.stroke();
}
