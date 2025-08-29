// src/main/resources/static/js/autocomplete.js

async function fetchSuggestions(query) {
    if (!query) return [];
    const res = await fetch(`/api/stocks?query=${encodeURIComponent(query)}`);
    if (!res.ok) return [];
    return await res.json(); // [{name: "삼성전자", symbol: "005930"}, ...]
}

function setupAutocomplete(inputEl, listEl) {
    inputEl.addEventListener("input", async () => {
        const query = inputEl.value.trim();
        listEl.innerHTML = "";

        if (!query) return;

        const suggestions = await fetchSuggestions(query);
        suggestions.forEach(stock => {
            const li = document.createElement("li");
            li.className = "list-group-item list-group-item-action";
            li.textContent = `${stock.name} (${stock.symbol})`;
            li.style.cursor = "pointer";

            li.addEventListener("click", () => {
                inputEl.value = stock.name;
                listEl.innerHTML = "";
                inputEl.form.submit(); // 클릭 시 바로 검색
            });

            listEl.appendChild(li);
        });
    });

    // 입력창 밖 클릭 시 드롭다운 닫기
    document.addEventListener("click", (e) => {
        if (!inputEl.contains(e.target) && !listEl.contains(e.target)) {
            listEl.innerHTML = "";
        }
    });
}

// PC
document.addEventListener("DOMContentLoaded", () => {
    const pcInput = document.getElementById("searchInput");
    const pcList = document.getElementById("autocompleteList");
    if (pcInput && pcList) setupAutocomplete(pcInput, pcList);

    // 모바일
    const mobileInput = document.getElementById("mobileSearchInput");
    const mobileList = document.getElementById("mobileAutocompleteList");
    if (mobileInput && mobileList) setupAutocomplete(mobileInput, mobileList);
});
