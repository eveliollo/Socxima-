document.addEventListener("DOMContentLoaded", () => {
    const ctx = document.getElementById('liveChart').getContext('2d');
    
    const liveChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: Array(12).fill(''),
            datasets: [{
                label: 'Actividad Red',
                data: Array(12).fill(0),
                borderColor: '#00ff66',
                backgroundColor: 'rgba(0, 255, 102, 0.08)',
                borderWidth: 2,
                pointRadius: 0,
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { display: false },
                y: { display: false, beginAtZero: true }
            },
            interaction: { intersect: false }
        }
    });

    // Actualización en vivo
    setInterval(() => {
        const valor = Math.floor(Math.random() * 35) + 50;
        liveChart.data.datasets[0].data.push(valor);
        liveChart.data.datasets[0].data.shift();
        liveChart.update('none');

        document.getElementById('tps-counter').innerText = (valor / 3.8).toFixed(1) + " TPS";
        document.getElementById('actions-counter').innerText = Math.floor(Math.random() * 500) + 9500;
    }, 1000);
});
