document.addEventListener("DOMContentLoaded", () => {

  // ===============================
  // 1️⃣ 도움말 / 줌 초기화
  // ===============================
  const helpBtn = document.getElementById("helpBtn");
  const resetZoomBtn = document.getElementById("resetZoomBtn");
  let chart;

  if (helpBtn) helpBtn.addEventListener("click", () => {
    const helpCard = document.getElementById("helpCard");
    if (helpCard) helpCard.style.display = helpCard.style.display === "none" ? "block" : "none";
  });

  if (resetZoomBtn) resetZoomBtn.addEventListener("click", () => {
    if (chart && chart.resetZoom) chart.resetZoom();
  });

  // ===============================
  // 2️⃣ 차트 관련
  // ===============================
  const indexSelector = document.getElementById("indexSelector");
  const tabs = document.querySelectorAll("#chartTab .nav-link");

  async function loadOptions() {
    if (!indexSelector) return;
    const res = await fetch("/api/index/names");
    const names = await res.json();
    indexSelector.innerHTML = names.map(name => `<option value="${name}">${name}</option>`).join("");
    loadChart("week");
  }

  function toCandles(rows) {
    return rows.map(r => ({
      x: new Date(r.date ?? r.time ?? r.t ?? r.timestamp).getTime(),
      o: Number(r.open ?? r.o ?? r.openPrice),
      h: Number(r.high ?? r.h ?? r.highPrice),
      l: Number(r.low ?? r.l ?? r.lowPrice),
      c: Number(r.close ?? r.c ?? r.closePrice),
      v: Number(r.volume ?? r.v ?? r.vol)
    })).filter(d => [d.o,d.h,d.l,d.c].every(Number.isFinite));
  }

  function inferTimeUnit(period) {
    return period === "year" ? "month" : "day";
  }

  function computeSMA(candles, period) {
    let sum = 0;
    return candles.map((c, i) => {
      sum += c.c;
      if (i >= period) sum -= candles[i - period].c;
      return { x: c.x, y: i >= period - 1 ? sum / period : null };
    });
  }

  async function loadChart(period = "week") {
    if (!indexSelector) return;
    const indexName = indexSelector.value;
    const res = await fetch(`/api/index/${encodeURIComponent(indexName)}?period=${period}`);
    const data = await res.json();
    const candles = toCandles(data);

    const canvas = document.getElementById("chartCanvas");
    if (!canvas) return;
    const ctx = canvas.getContext("2d");

    if (chart) chart.destroy();

    chart = new Chart(ctx, {
      type: "candlestick",
      data: {
        datasets: [
          {
            label: indexName,
            data: candles,
            color: { up:"rgba(25,190,125,1)", down:"rgba(235,90,90,1)", unchanged:"rgba(128,128,128,1)" },
            borderColor: "rgba(0,0,0,0.6)"
          },
          {
            label: "Volume",
            type: "bar",
            data: candles.map(c => ({ x: c.x, y: c.v ?? 0 })),
            yAxisID: "volume",
            barPercentage: 1.0,
            categoryPercentage: 1.0,
            backgroundColor: "rgba(100,100,255,0.3)"
          },
          {
            label: "MA20",
            type: "line",
            data: computeSMA(candles, 20),
            borderColor: "orange",
            borderWidth: 1.3,
            pointRadius: 0,
            tension: 0
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          zoom: { pan:{enabled:true,mode:"x",modifierKey:"ctrl"}, zoom:{wheel:{enabled:true},pinch:{enabled:true},mode:"x"} },
          legend: { display:true },
          tooltip: {
            mode: "nearest",
            intersect: true,
            callbacks: {
              label: ctx => {
                const v = ctx.raw;
                if (ctx.dataset.label === "Volume") return `Volume: ${v.y.toLocaleString()}`;
                const prev = ctx.dataset.data[ctx.dataIndex-1];
                const pct = prev?.c ? ((v.c-prev.c)/prev.c*100).toFixed(2)+"%" : "";
                return `O:${v.o} H:${v.h} L:${v.l} C:${v.c} (${pct})`;
              }
            }
          }
        },
        scales: {
          x: { type:"time", time:{ unit: inferTimeUnit(period), tooltipFormat:"yyyy-MM-dd" }, ticks:{ maxRotation:0, autoSkip:true } },
          y: { position:"right", ticks:{ callback: v => Number(v).toLocaleString() } },
          volume: { position:"left", grid:{ display:false }, ticks:{ callback: v => v.toLocaleString() }, beginAtZero:true }
        },
        onClick: evt => {
          const elements = chart.getElementsAtEventForMode(evt,'nearest',{intersect:true},false);
          if (!elements.length) return;
          const d = chart.data.datasets[elements[0].datasetIndex].data[elements[0].index];
          if (d?.o !== undefined) {
            const date = new Date(d.x);
            const detailPanel = document.getElementById("detailPanel");
            if (detailPanel) detailPanel.innerHTML = `
              <strong>${indexSelector.value} (${date.toLocaleDateString()})</strong><br>
              O: ${d.o} / H: ${d.h} / L: ${d.l} / C: ${d.c} / V: ${d.v ?? "-"}
            `;
          }
        }
      }
    });
  }

  if (indexSelector) indexSelector.addEventListener("change", () => loadChart("week"));
  tabs.forEach(tab => tab.addEventListener("click", () => {
    tabs.forEach(t => t.classList.remove("active"));
    tab.classList.add("active");
    loadChart(tab.id.replace("-tab",""));
  }));

  loadOptions();

  // ===============================
  // 3️⃣ 메모 모달
  // ===============================
  const tokenMeta = document.querySelector('meta[name="_csrf"]');
  const headerMeta = document.querySelector('meta[name="_csrf_header"]');
  const token = tokenMeta?.getAttribute("content");
  const header = headerMeta?.getAttribute("content");

  const memoModalEl = document.getElementById("memoModal");
  if (!memoModalEl) return;
  const memoModal = new bootstrap.Modal(memoModalEl);

  const memoTitle = memoModalEl.querySelector("#memoTitle");
  const memoContent = memoModalEl.querySelector("#memoContent");
  const memoDate = memoModalEl.querySelector("#memoDate");
  const editForm = memoModalEl.querySelector("#editForm");
  const editTitle = memoModalEl.querySelector("#editTitle");
  const editContent = memoModalEl.querySelector("#editContent");
  const editBtn = memoModalEl.querySelector("#editBtn");
  const deleteBtn = memoModalEl.querySelector("#deleteBtn");
  const saveEditBtn = memoModalEl.querySelector("#saveEditBtn");
  const cancelEditBtn = memoModalEl.querySelector("#cancelEditBtn");

  let currentMemoId = null;
  let currentMemoData = null;

  function closeEditForm() {
    editForm.classList.add("d-none");
    memoTitle.classList.remove("d-none");
    memoContent.classList.remove("d-none");
    editBtn.classList.remove("d-none");
    deleteBtn.classList.remove("d-none");
    saveEditBtn.classList.add("d-none");
    cancelEditBtn.classList.add("d-none");
  }

  document.querySelectorAll(".memo-card .btn-outline-primary").forEach(btn => {
    btn.addEventListener("click", async () => {
      currentMemoId = btn.dataset.memoId;
      if (!currentMemoId || isNaN(currentMemoId)) { alert("메모 ID가 유효하지 않습니다."); return; }

      try {
        const res = await fetch(`/api/memos/${currentMemoId}`);
        if (!res.ok) throw new Error("메모를 불러올 수 없습니다.");
        currentMemoData = await res.json();

        memoTitle.textContent = currentMemoData.title || "(제목 없음)";
        memoContent.textContent = currentMemoData.content || "(내용 없음)";
        memoDate.textContent = currentMemoData.stockDate || "";

        closeEditForm();
        memoModal.show();
      } catch (err) {
        console.error(err);
        alert(err.message);
      }
    });
  });

  editBtn.addEventListener("click", () => {
    editForm.classList.remove("d-none");
    editTitle.value = memoTitle.textContent;
    editContent.value = memoContent.textContent;
    memoTitle.classList.add("d-none");
    memoContent.classList.add("d-none");
    editBtn.classList.add("d-none");
    deleteBtn.classList.add("d-none");
    saveEditBtn.classList.remove("d-none");
    cancelEditBtn.classList.remove("d-none");
  });

  saveEditBtn.addEventListener("click", async () => {
    if (!currentMemoId || !currentMemoData) return;
    try {
      const res = await fetch(`/api/memos/${currentMemoId}`, {
        method:"PUT",
        headers: { "Content-Type":"application/json", [header]: token },
        body: JSON.stringify({
          ticker: currentMemoData.ticker,
          title: editTitle.value,
          content: editContent.value,
          stockDate: currentMemoData.stockDate
        })
      });
      if (!res.ok) throw new Error("메모 수정 실패");
      currentMemoData = await res.json();
      memoTitle.textContent = currentMemoData.title;
      memoContent.textContent = currentMemoData.content;
      closeEditForm();
    } catch (err) { console.error(err); alert(err.message); }
  });

  cancelEditBtn.addEventListener("click", closeEditForm);

  deleteBtn.addEventListener("click", async () => {
    if (!currentMemoId || !confirm("정말로 메모를 삭제하시겠습니까?")) return;
    try {
      const res = await fetch(`/api/memos/${currentMemoId}`, { method:"DELETE", headers:{ [header]:token } });
      if (!res.ok) throw new Error("메모 삭제 실패");
      memoModal.hide();
      const card = document.querySelector(`.memo-card button[data-memo-id="${currentMemoId}"]`)?.closest(".memo-card");
      if (card) card.remove();
      location.reload();
    } catch (err) { console.error(err); alert(err.message); }
  });

  memoModalEl.addEventListener("shown.bs.modal", () => memoModalEl.querySelector(".btn-close")?.focus());
  memoModalEl.addEventListener("hide.bs.modal", () => document.activeElement?.blur());

});
