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
});
