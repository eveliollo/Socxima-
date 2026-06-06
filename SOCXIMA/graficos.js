/* ── MINI SPARKLINE CHARTS ── */
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
    const range = max - min || 1;
    ctx.clearRect(0, 0, W, H);

    ctx.strokeStyle = 'rgba(10, 32, 53, 0.3)';
    ctx.lineWidth = 0.5;
    for (let i = 1; i < 4; i++) { ctx.beginPath(); ctx.moveTo(0, (H / 4) * i); ctx.lineTo(W, (H / 4) * i); ctx.stroke(); }
    for (let i = 1; i < 6; i++) { ctx.beginPath(); ctx.moveTo((W / 6) * i, 0); ctx.lineTo((W / 6) * i, H); ctx.stroke(); }

    const points = data.map((v, i) => ({
        x: (i / (data.length - 1)) * W,
        y: H - ((v - min) / range) * H * 0.75 - H * 0.12
    }));

    const areaGrad = ctx.createLinearGradient(0, 0, 0, H);
    areaGrad.addColorStop(0, color + '33');
    areaGrad.addColorStop(0.5, color + '0D');
    areaGrad.addColorStop(1, 'transparent');
    ctx.beginPath(); ctx.moveTo(0, H);
    points.forEach((p, i) => {
        if (i === 0) ctx.lineTo(p.x, p.y);
        else { const cpX = (points[i-1].x + p.x)/2; ctx.bezierCurveTo(cpX, points[i-1].y, cpX, p.y, p.x, p.y); }
    });
    ctx.lineTo(W, H); ctx.closePath(); ctx.fillStyle = areaGrad; ctx.fill();

    ctx.shadowColor = color; ctx.shadowBlur = 10;
    ctx.beginPath();
    points.forEach((p, i) => {
        if (i === 0) ctx.moveTo(p.x, p.y);
        else { const cpX = (points[i-1].x + p.x)/2; ctx.bezierCurveTo(cpX, points[i-1].y, cpX, p.y, p.x, p.y); }
    });
    ctx.strokeStyle = color; ctx.lineWidth = 2.5; ctx.lineCap = 'round'; ctx.stroke();
    ctx.shadowBlur = 0;

    const lastP = points.at(-1);
    ctx.beginPath(); ctx.arc(lastP.x, lastP.y, 6, 0, Math.PI*2); ctx.fillStyle = color + '44'; ctx.fill();
    ctx.beginPath(); ctx.arc(lastP.x, lastP.y, 3, 0, Math.PI*2); ctx.fillStyle = '#ffffff'; ctx.strokeStyle = color; ctx.lineWidth = 1.5; ctx.fill(); ctx.stroke();
}
