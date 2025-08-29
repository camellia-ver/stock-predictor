document.addEventListener("DOMContentLoaded", function() {
    const searchInput = document.getElementById("searchStock");
    const sortOption = document.getElementById("sortOption");
    const tableRows = document.querySelectorAll("tbody tr");

    function filterAndSort() {
        const keyword = searchInput.value.toLowerCase();
        const option = sortOption.value;

        let rows = Array.from(tableRows);

        // 검색 필터
        rows.forEach(row => {
            const name = row.querySelector("td").innerText.toLowerCase();
            row.style.display = name.includes(keyword) ? "" : "none";
        });

        // 정렬
        rows.sort((a, b) => {
            if (option === "name") {
                return a.cells[0].innerText.localeCompare(b.cells[0].innerText);
            } else if (option === "price") {
                return parseFloat(b.cells[1].innerText.replace(/,/g, "")) - 
                       parseFloat(a.cells[1].innerText.replace(/,/g, ""));
            } else if (option === "change") {
                return parseFloat(b.cells[2].innerText.replace(/[▲▼ %]/g, "")) - 
                       parseFloat(a.cells[2].innerText.replace(/[▲▼ %]/g, ""));
            }
            return 0;
        });

        // 테이블에 반영
        const tbody = document.querySelector("tbody");
        rows.forEach(row => tbody.appendChild(row));
    }

    searchInput.addEventListener("input", filterAndSort);
    sortOption.addEventListener("change", filterAndSort);
});