document.addEventListener("DOMContentLoaded", () => {
    async function fetchSuggestions(query) {
        if (!query) return [];
        const res = await fetch(`/api/stocks?query=${encodeURIComponent(query)}`);
        if (!res.ok) return [];
        return await res.json(); // [{ name: "삼성전자", ticker: "005930" }, ...]
    }

    function setupAutocomplete(inputEl, listEl) {
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
                    // 클릭 시 바로 stock-detail 페이지로 이동
                    window.location.href = `/stock-detail?ticker=${stock.ticker}`;
                });

                listEl.appendChild(li);
            });

            // 위치 및 높이 조정
            const rect = inputEl.getBoundingClientRect();
            const windowHeight = window.innerHeight;
            const spaceBelow = windowHeight - rect.bottom - 10;
            listEl.style.top = inputEl.offsetHeight + "px";
            listEl.style.maxHeight = spaceBelow > 150 ? "150px" : spaceBelow + "px";
            listEl.style.overflowY = "auto";
            listEl.style.display = "block";

            // 모바일 키보드 대응
            if (window.innerWidth < 992) {
                const dropdownBottom = rect.top + inputEl.offsetHeight + listEl.offsetHeight;
                if (dropdownBottom > windowHeight) {
                    window.scrollBy(0, dropdownBottom - windowHeight + 10);
                }
            }
        }

        inputEl.addEventListener("input", updateDropdown);

        inputEl.addEventListener("keydown", (e) => {
            const items = listEl.getElementsByTagName("li");
            if (!items) return;

            if (e.key === "ArrowDown") {
                currentFocus++;
                addActive(items);
            } else if (e.key === "ArrowUp") {
                currentFocus--;
                addActive(items);
            } else if (e.key === "Enter") {
                e.preventDefault();
                if (currentFocus > -1 && items[currentFocus]) {
                    items[currentFocus].click();
                }
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

    // PC
    const pcInput = document.getElementById("searchInput");
    const pcList = document.getElementById("autocompleteList");
    if (pcInput && pcList) setupAutocomplete(pcInput, pcList);

    // 모바일
    const mobileInput = document.getElementById("mobileSearchInput");
    const mobileList = document.getElementById("mobileAutocompleteList");
    if (mobileInput && mobileList) setupAutocomplete(mobileInput, mobileList);

    // 화면 크기 변경 시 검색폼 상태 조정
    function adjustNavbarOnResize() {
        const mobileSearch = document.getElementById('mobileSearch');
        const pcSearch = document.getElementById('searchForm');
        const windowWidth = window.innerWidth;

        if (windowWidth >= 992) { // lg 이상
            // 모바일 검색창이 열려 있으면 닫기
            if (mobileSearch.classList.contains('show')) {
                const bsCollapse = bootstrap.Collapse.getInstance(mobileSearch);
                if (bsCollapse) {
                    bsCollapse.hide();
                }
            }
            // PC 검색창 flex로 보이게
            pcSearch.classList.remove('d-none');
            pcSearch.classList.add('d-flex');
        } else {
            // PC 검색창 숨기기
            pcSearch.classList.remove('d-flex');
            pcSearch.classList.add('d-none');
        }
    }

    // 이벤트 등록
    window.addEventListener('resize', adjustNavbarOnResize);

    // 초기 실행
    adjustNavbarOnResize();
});