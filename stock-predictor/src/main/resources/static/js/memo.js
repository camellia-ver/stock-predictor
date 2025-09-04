document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById('searchMemo');

    searchInput.addEventListener('input', () => {
        const query = searchInput.value.toLowerCase();
        document.querySelectorAll('#memoContainer .memo-card').forEach(card => {
            const title = card.querySelector('.card-title').innerText.toLowerCase();
            const content = card.querySelector('.card-text').innerText.toLowerCase();
            card.closest('.col-md-6').style.display =
                (title.includes(query) || content.includes(query)) ? '' : 'none';
        });
    });
});
