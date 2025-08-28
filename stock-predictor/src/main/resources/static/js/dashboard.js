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

    async function loadChart(period = "week") {
        const indexName = indexSelector.value;
        const res = await fetch(`/api/index/${indexName}?period=${period}`);
        const data = await res.json();

        const labels = data.map(d => d.date);
        const prices = data.map(d => d.closePrice);

        const ctx = document.getElementById("chartCanvas").getContext("2d");
        if (chart) chart.destroy(); // 기존 차트 제거

        chart = new Chart(ctx, {
            type: "line",
            data: {
                labels,
                datasets: [{
                    label: indexName,
                    data: prices,
                    borderColor: "blue",
                    fill: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false, // 비율 고정 해제
            }
        });
    }

    // 이벤트 바인딩
    indexSelector.addEventListener("change", () => {
        // 모든 탭에서 active 제거
        tabs.forEach(t => t.classList.remove("active"));

        // week 탭 활성화
        const weekTab = document.getElementById("week-tab");
        weekTab.classList.add("active");

        // 차트 로드 (week로)
        loadChart("week");
    });

    // 초기 실행 (여기서는 그냥 직접 실행)
    loadOptions();

    tabs.forEach(tab => {
      tab.addEventListener("click", () => {
        // 기존 active 제거
        tabs.forEach(t => t.classList.remove("active"));
        // 현재 클릭된 탭에 active 추가
        tab.classList.add("active");

        const period = tab.id.replace("-tab", ""); // week, month, year
        loadChart(period);
      });
    });
});