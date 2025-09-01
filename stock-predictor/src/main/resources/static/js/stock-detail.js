document.addEventListener("DOMContentLoaded", () => {

    // -------------------- 즐겨찾기 토글 --------------------
    const favBtn = document.querySelector('.favorite-btn');
    if (favBtn) {
        favBtn.addEventListener('click', async () => {
            const ticker = favBtn.dataset.ticker;

            const tokenMeta = document.querySelector('meta[name="_csrf"]');
            const headerMeta = document.querySelector('meta[name="_csrf_header"]');
            const csrfToken = tokenMeta ? tokenMeta.content : null;
            const csrfHeader = headerMeta ? headerMeta.content : null;

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
                    favBtn.classList.toggle('active', data.isFavorite);
                } else if (res.status === 401) {
                    alert('로그인이 필요합니다.');
                } else {
                    console.error('즐겨찾기 요청 실패', res.status, await res.text());
                }
            } catch (err) {
                console.error('즐겨찾기 요청 중 에러', err);
            }
        });
    }

    // -------------------- 도움말 카드 토글 --------------------
    const helpBtn = document.getElementById("helpBtn");
    const helpCard = document.getElementById("helpCard");
    if (helpBtn && helpCard) {
        helpBtn.addEventListener("click", () => {
            helpCard.style.display = helpCard.style.display === "none" ? "block" : "none";
        });
    }

    // -------------------- 캔들 차트 --------------------
    let chart;
    const ctx = document.getElementById('candleChart').getContext('2d');
    const ticker = document.getElementById("chartSection").dataset.ticker;

    function toCandles(data) {
        return data.map(p => ({
            x: new Date(p.date).getTime(),
            o: Number(p.openPrice ?? p.o ?? p.open),
            h: Number(p.highPrice ?? p.h ?? p.high),
            l: Number(p.lowPrice ?? p.l ?? p.low),
            c: Number(p.closePrice ?? p.c ?? p.close),
            v: Number(p.volume ?? p.v ?? 0)
        })).filter(d => Number.isFinite(d.o) && Number.isFinite(d.h) && Number.isFinite(d.l) && Number.isFinite(d.c));
    }

    function computeSMA(candles, period) {
        const res = [];
        let sum = 0;
        for (let i = 0; i < candles.length; i++) {
            sum += candles[i].c;
            if (i >= period) sum -= candles[i - period].c;
            res.push({ x: candles[i].x, y: i >= period - 1 ? sum / period : null });
        }
        return res;
    }

    function inferTimeUnit(period) {
        switch(period) {
            case "week": return "day";
            case "month": return "day";
            case "year": return "month";
            default: return "day";
        }
    }

    async function loadChart(period = "week") {
        const response = await fetch(`/api/stock-prices/${ticker}/prices?period=${period}`);
        const data = await response.json();
        if (!data || data.length === 0) return;

        const stockName = data[0].name;
        const candles = toCandles(data);

        if(chart) chart.destroy();

        chart = new Chart(ctx, {
            type: "candlestick",
            data: {
                datasets: [
                    {
                        label: stockName,
                        data: candles,
                        color: { up: "rgba(25,190,125,1)", down: "rgba(235,90,90,1)" },
                        borderColor: "rgba(0,0,0,0.6)"
                    },
                    {
                        label: "MA20",
                        type: "line",
                        data: computeSMA(candles, 20),
                        borderColor: "orange",
                        borderWidth: 1.3,
                        pointRadius: 0,
                        tension: 0
                    },
                    {
                        label:"Volume",
                        type:"bar",
                        data:candles.map(c=>({x:c.x, y:c.v})),
                        yAxisID:"volume",
                        backgroundColor:"rgba(100,100,255,0.3)"
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: true },
                    zoom:{
                        pan:{
                            enabled: true,
                            mode: 'x',
                            modifierKey: 'ctrl'
                        },
                        zoom: {
                            wheel: { enabled: true },
                            pinch: { enabled: true },
                            mode: 'x'
                        }
                    },
                    tooltip: {
                        mode: "nearest",
                        intersect: true,
                        callbacks: {
                            label: (ctx) => {
                                const v = ctx.raw;
                                if(ctx.dataset.label==="Volume") return `Volume: ${v.y.toLocaleString()}`;
                                const prev = ctx.dataset.data[ctx.dataIndex-1];
                                const prevClose = prev ? prev.c : null;
                                const pct = prevClose ? ((v.c - prevClose)/prevClose*100).toFixed(2)+"%" : "";
                                return `O:${v.o} H:${v.h} L:${v.l} C:${v.c} (${pct})`;
                            }
                        }
                    }
                },
                scales: {
                    x: { type:"time", time: { unit: inferTimeUnit(period) }, ticks:{ maxRotation:0, autoSkip:true } },
                    y: { position:"right" },
                    volume: { position:"left", beginAtZero:true, grid:{ display:false } }
                },
                onClick: (evt) => {
                    const elements = chart.getElementsAtEventForMode(evt, 'nearest', { intersect: true }, false);
                    if(!elements.length) return;
                    const e = elements[0];
                    const d = chart.data.datasets[e.datasetIndex].data[e.index];
                    if(d.o!==undefined){
                        const date = new Date(d.x);
                        document.getElementById("detailPanel").innerHTML = `
                            <strong>${stockName} (${date.toLocaleDateString()})</strong><br>
                            O: ${d.o} / H: ${d.h} / L: ${d.l} / C: ${d.c} / V: ${d.v ?? "-"}
                        `;
                    }
                }
            }
        });
    }

    // -------------------- 초기 로드 --------------------
    loadChart("week");

    // -------------------- 리셋 줌 버튼 --------------------
    const resetZoomBtn = document.getElementById("resetZoomBtn");
    if(resetZoomBtn){
        resetZoomBtn.addEventListener("click", () => {
            if(chart) chart.resetZoom(); // chartjs-plugin-zoom 메서드
        });
    }

    // -------------------- 탭 클릭 이벤트 --------------------
    document.querySelectorAll('#chartTab button').forEach(btn=>{
        btn.addEventListener('click', ()=>{
            document.querySelectorAll('#chartTab button').forEach(b=>b.classList.remove('active'));
            btn.classList.add('active');
            loadChart(btn.dataset.period);
        });
    });

});
