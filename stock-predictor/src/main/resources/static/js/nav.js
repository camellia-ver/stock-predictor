document.addEventListener("DOMContentLoaded", () => {

    async function fetchSuggestions(query) {
        if (!query) return [];
        const res = await fetch(`/api/stocks?query=${encodeURIComponent(query)}`);
        if (!res.ok) return [];
        return await res.json(); // [{ name: "삼성전자", ticker: "005930" }, ...]
    }

    function setupAutocomplete(inputEl, listEl, options = {}) {
        let currentFocus = -1;

        async function updateDropdown() {
            const query = inputEl.value.trim();
            listEl.innerHTML = "";
            currentFocus = -1;
            if (!query) return;

            const suggestions = await fetchSuggestions(query);

            suggestions.forEach(stock => {
                const li = document.createElement("li");
                li.className = "list-group-item list-group-item-action";
                li.textContent = `${stock.name} (${stock.ticker})`;
                li.style.cursor = "pointer";

                li.addEventListener("click", () => {
                    if (options.onSelect) {
                        options.onSelect(stock);
                    } else {
                        inputEl.value = `${stock.name} (${stock.ticker})`;
                    }
                    listEl.innerHTML = "";
                });

                listEl.appendChild(li);
            });

            if (suggestions.length > 0) {
                listEl.style.display = "block";
                listEl.style.maxHeight = "150px";
                listEl.style.overflowY = "auto";
            } else {
                listEl.style.display = "none";
            }
        }

        inputEl.addEventListener("input", updateDropdown);

        inputEl.addEventListener("keydown", (e) => {
            const items = listEl.getElementsByTagName("li");

            if (e.key === "ArrowDown") {
                currentFocus++;
                addActive(items);
            } else if (e.key === "ArrowUp") {
                currentFocus--;
                addActive(items);
            } else if (e.key === "Enter") {
                e.preventDefault(); // Enter 시 자동완성 선택 방지
            }
        });

        document.addEventListener("click", (e) => {
            if (!inputEl.contains(e.target) && !listEl.contains(e.target)) {
                listEl.innerHTML = "";
            }
        });

        function addActive(items) {
            if (!items) return;
            removeActive(items);
            if (currentFocus >= items.length) currentFocus = 0;
            if (currentFocus < 0) currentFocus = items.length - 1;
            items[currentFocus].classList.add("active");
            items[currentFocus].scrollIntoView({ block: "nearest" });
        }

        function removeActive(items) {
            for (let i = 0; i < items.length; i++) {
                items[i].classList.remove("active");
            }
        }
    }

    // ticker-input 자동완성 적용
    const tickerInput = document.getElementById("ticker-input");
    const tickerList = document.getElementById("ticker-autocomplete-list");

    if (tickerInput && tickerList) {
        setupAutocomplete(tickerInput, tickerList, {
            onSelect: (stock) => {
                // 클릭 시 input에 값만 넣기
                tickerInput.value = `${stock.name} (${stock.ticker})`;
            }
        });
    }

    // 폼 제출 처리
    const addFavoriteForm = document.getElementById("add-favorite-form");
    addFavoriteForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const ticker = tickerInput.value.trim();
        if (ticker) {
            // 여기에 AJAX 요청이나 리스트 추가 처리
        }
    });
});
