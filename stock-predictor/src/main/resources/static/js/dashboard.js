document.addEventListener("DOMContentLoaded", () => {
  let chart;
  const indexSelector = document.getElementById("indexSelector");
  const tabs = document.querySelectorAll("#chartTab .nav-link");

  async function loadOptions() {
    const res = await fetch("/api/index/names");
    const names = await res.json();

    indexSelector.innerHTML = names.map(name =>
      `<option value="${name}">${name}</option>`).join("");

    loadChart(); // 첫 로드 시 차트 그리기
  }

  function toCandles(rows) {
    return rows.map(r => {
      const date = r.date ?? r.time ?? r.t ?? r.timestamp;
      const o = r.open ?? r.o ?? r.openPrice;
      const h = r.high ?? r.h ?? r.highPrice;
      const l = r.low ?? r.l ?? r.lowPrice;
      const c = r.close ?? r.c ?? r.closePrice;

      return {
        x: new Date("2025-08-22").getTime(),
        o: Number(o),
        h: Number(h),
        l: Number(l),
        c: Number(c),
      };
    }).filter(d =>
      Number.isFinite(d.o) && Number.isFinite(d.h) &&
      Number.isFinite(d.l) && Number.isFinite(d.c)
    );
  }

  function inferTimeUnit(period) {
    switch (period) {
      case "week":  return "day";
      case "month": return "day";
      case "year":  return "month";
      default:      return "day";
    }
  }

  async function loadChart(period = "week") {
    const indexName = indexSelector.value;
    const res = await fetch(`/api/index/${encodeURIComponent(indexName)}?period=${period}`);
    const data = await res.json();

    const candles = toCandles(data);
    console.log("candles:", candles);

    const ctx = document.getElementById("chartCanvas").getContext("2d");
    if (chart) chart.destroy();

    chart = new Chart(ctx, {
      type: "candlestick",
      data: {
        // 캔들 차트는 datasets만 사용합니다 (labels 불필요)
        datasets: [{
          label: indexName,
          data: candles,
//          parsing: false,
          // 색상(업/다운) 설정: 플러그인 버전에 따라 자동 적용
          color: {
            up: "rgba(25,190,125,1)",
            down: "rgba(235,90,90,1)",
            unchanged: "rgba(128,128,128,1)"
          },
          borderColor: "rgba(0,0,0,0.6)"
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: true },
          tooltip: {
            mode: "index",
            intersect: false,
            callbacks: {
              // 툴팁에 OHLC 보기 좋게 표시
              label: (ctx) => {
                const v = ctx.raw;
                return ` O:${v.o} H:${v.h} L:${v.l} C:${v.c}`;
              }
            }
          }
        },
        scales: {
          x: {
            type: "time",                 // date-fns adapter 필요
            time: {
              unit: inferTimeUnit(period),
              tooltipFormat: "yyyy-MM-dd"
            },
            ticks: { maxRotation: 0, autoSkip: true }
          },
          y: {
            position: "right",
            ticks: {
              callback: (v) => Number(v).toLocaleString()
            }
          }
        }
      }
    });
  }

  // 이벤트 바인딩
  indexSelector.addEventListener("change", () => {
    tabs.forEach(t => t.classList.remove("active"));
    const weekTab = document.getElementById("week-tab");
    weekTab.classList.add("active");
    loadChart("week");
  });

  loadOptions();

  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      tabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");

      const period = tab.id.replace("-tab", ""); // week, month, year
      loadChart(period);
    });
  });
});
