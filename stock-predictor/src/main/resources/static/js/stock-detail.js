document.addEventListener("DOMContentLoaded", () => {
    const favBtn = document.querySelector('.favorite-btn');
    if (!favBtn) return;

    favBtn.addEventListener('click', async () => {
        const ticker = favBtn.dataset.ticker;

        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        const csrfToken = tokenMeta ? tokenMeta.content : null;
        const csrfHeader = headerMeta ? headerMeta.content : null;

        const headers = {};
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        try {
            const res = await fetch(`/api/favorites/toggle?ticker=${ticker}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
                }
            });

            if (res.ok) {
                const data = await res.json();
                if (data.isFavorite) {
                    favBtn.classList.add('active');
                } else {
                    favBtn.classList.remove('active');
                }
            } else if (res.status === 401) {
                alert('로그인이 필요합니다.');
            } else {
                console.error('즐겨찾기 요청 실패', res.status, await res.text());
            }
        } catch (err) {
            console.error('즐겨찾기 요청 중 에러', err);
        }
    });

        let chart;
        const ctx = document.getElementById('priceChart').getContext('2d');
        const ticker = document.getElementById("chartSection").dataset.ticker;

        async function loadChart(period) {
            const response = await fetch(`/api/stock-prices/${ticker}/prices?period=${period}`);
            const data = await response.json();

            const labels = data.map(p => p.date);
            const prices = data.map(p => p.closePrice);

            if (chart) chart.destroy();

            chart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: `${period} 종가`,
                        data: prices,
                        borderColor: 'rgba(54, 162, 235, 1)',
                        backgroundColor: 'rgba(54, 162, 235, 0.2)',
                        fill: true,
                        tension: 0.3,   // 곡선 부드럽게
                        pointRadius: 3, // 데이터 포인트 표시
                        pointHoverRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: true,
                            labels: {
                                font: {
                                    size: 14
                                }
                            }
                        },
                        tooltip: {
                            mode: 'index',
                            intersect: false,
                            padding: 10,
                            bodyFont: { size: 14 },
                            titleFont: { size: 14 }
                        }
                    },
                    scales: {
                        x: {
                            display: true,
                            title: {
                                display: true,
                                text: '날짜',
                                font: { size: 14 }
                            },
                            ticks: { maxRotation: 45, minRotation: 45 }
                        },
                        y: {
                            display: true,
                            title: {
                                display: true,
                                text: '가격 (원)',
                                font: { size: 14 }
                            },
                            beginAtZero: false
                        }
                    }
                }
            });
        }

        // ✅ 기본: 1주차트 로딩
        loadChart("week");

        // ✅ 탭 클릭 시 데이터 갱신
        document.querySelectorAll('#chartTab button').forEach(btn => {
            btn.addEventListener('click', () => {
                loadChart(btn.dataset.period);
            });
        });
});
