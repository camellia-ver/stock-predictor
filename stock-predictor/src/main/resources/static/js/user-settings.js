document.addEventListener("DOMContentLoaded", () => {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // ===============================
    // 1️⃣ 비밀번호 토글 및 강도 체크
    // ===============================
    const passwordInput = document.getElementById('password');
    const toggleBtn = document.getElementById('togglePassword');
    const strengthBar = document.getElementById('strengthBar');
    const strengthText = document.getElementById('strengthText');
    const rules = {
        length: document.getElementById('rule-length'),
        upper: document.getElementById('rule-upper'),
        lower: document.getElementById('rule-lower'),
        number: document.getElementById('rule-number'),
        special: document.getElementById('rule-special')
    };

    toggleBtn.addEventListener('click', () => {
        passwordInput.type = passwordInput.type === 'password' ? 'text' : 'password';
        toggleBtn.querySelector('i').classList.toggle('bi-eye');
        toggleBtn.querySelector('i').classList.toggle('bi-eye-slash');
    });

    passwordInput.addEventListener('input', () => {
        const val = passwordInput.value;
        let score = 0;

        rules.length.classList.toggle('text-success', val.length >= 8);
        rules.length.classList.toggle('text-danger', val.length < 8);

        rules.upper.classList.toggle('text-success', /[A-Z]/.test(val));
        rules.upper.classList.toggle('text-danger', !/[A-Z]/.test(val));

        rules.lower.classList.toggle('text-success', /[a-z]/.test(val));
        rules.lower.classList.toggle('text-danger', !/[a-z]/.test(val));

        rules.number.classList.toggle('text-success', /\d/.test(val));
        rules.number.classList.toggle('text-danger', !/\d/.test(val));

        rules.special.classList.toggle('text-success', /[@$!%*?&]/.test(val));
        rules.special.classList.toggle('text-danger', !/[@$!%*?&]/.test(val));

        if (val.length >= 8) score++;
        if (/[A-Z]/.test(val)) score++;
        if (/[a-z]/.test(val)) score++;
        if (/\d/.test(val)) score++;
        if (/[@$!%*?&]/.test(val)) score++;

        const width = (score / 5) * 100;
        strengthBar.style.width = width + '%';

        if (score <= 2) {
            strengthBar.className = 'progress-bar bg-danger';
            strengthText.textContent = '약함';
        } else if (score === 3 || score === 4) {
            strengthBar.className = 'progress-bar bg-warning';
            strengthText.textContent = '보통';
        } else if (score === 5) {
            strengthBar.className = 'progress-bar bg-success';
            strengthText.textContent = '강함';
        } else {
            strengthBar.className = 'progress-bar';
            strengthText.textContent = '';
        }
    });

    // ===============================
    // 2️⃣ 즐겨찾기 관리 + 자동완성
    // ===============================
    const favoriteList = document.getElementById('favorite-list');
    const addForm = document.getElementById('add-favorite-form');
    const tickerInput = document.getElementById('ticker-input');
    const tickerList = document.getElementById('ticker-autocomplete-list');

    // 🔹 자동완성 함수
    async function fetchSuggestions(query) {
        if (!query) return [];
        const res = await fetch(`/api/stocks?query=${encodeURIComponent(query)}`);
        if (!res.ok) return [];
        return await res.json(); // [{ name, ticker }, ...]
    }

    let currentFocus = -1;

    async function updateDropdown() {
        const query = tickerInput.value.trim();
        tickerList.innerHTML = '';
        currentFocus = -1;
        if (!query) {
            tickerList.style.display = 'none';
            return;
        }

        const suggestions = await fetchSuggestions(query);
        suggestions.forEach(stock => {
            const li = document.createElement('li');
            li.className = 'list-group-item list-group-item-action';
            li.textContent = `${stock.name} (${stock.ticker})`; // name(ticker)
            li.style.cursor = 'pointer';

            li.addEventListener('click', () => {
                tickerInput.value = `${stock.name} (${stock.ticker})`;
                tickerList.innerHTML = '';
                tickerList.style.display = 'none';
            });

            tickerList.appendChild(li);
        });

        tickerList.style.display = suggestions.length > 0 ? 'block' : 'none';
    }

    tickerInput.addEventListener('input', updateDropdown);

    tickerInput.addEventListener('keydown', (e) => {
        const items = tickerList.getElementsByTagName('li');

        if (e.key === 'ArrowDown') {
            currentFocus++;
            addActive(items);
        } else if (e.key === 'ArrowUp') {
            currentFocus--;
            addActive(items);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (currentFocus > -1 && items[currentFocus]) {
                tickerInput.value = items[currentFocus].textContent; // name(ticker) 그대로
                tickerList.innerHTML = '';
                tickerList.style.display = 'none';
                currentFocus = -1;
            }
        }
    });

    function addActive(items) {
        if (!items) return;
        removeActive(items);
        if (currentFocus >= items.length) currentFocus = 0;
        if (currentFocus < 0) currentFocus = items.length - 1;
        items[currentFocus].classList.add('active');
        items[currentFocus].scrollIntoView({ block: 'nearest' });
    }

    function removeActive(items) {
        for (let i = 0; i < items.length; i++) {
            items[i].classList.remove('active');
        }
    }

    document.addEventListener('click', (e) => {
        if (!tickerInput.contains(e.target) && !tickerList.contains(e.target)) {
            tickerList.innerHTML = '';
            tickerList.style.display = 'none';
        }
    });

    // 🔹 즐겨찾기 제거
    favoriteList.addEventListener('click', (e) => {
        if (e.target.classList.contains('remove-favorite-btn')) {
            const li = e.target.closest('li');
            const ticker = li.getAttribute('data-ticker');

            fetch('/api/favorites/toggle?ticker=' + ticker, {
                method: 'POST',
                headers: { [csrfHeader]: csrfToken }
            })
            .then(res => {
                if (res.ok) li.remove();
                else if (res.status === 401) alert('로그인이 필요합니다.');
                else alert('삭제 실패');
            });
        }
    });

    // 🔹 즐겨찾기 추가
    addForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const value = tickerInput.value.trim();
        if (!value) return;

        // ticker만 추출 (뒤쪽 괄호 안)
        const match = value.match(/\((.*?)\)$/);
        const ticker = match ? match[1] : value;

        fetch('/api/favorites/toggle?ticker=' + ticker, {
                method: 'POST',
                headers: { [csrfHeader]: csrfToken }
            })
            .then(res => {
                if (!res.ok) throw new Error(res.status);
                return res.json();
            })
            .then(data => {
                const isFav = data.isFavorite ?? data.favorite;

                if (isFav) {
                    const nameMatch = value.match(/^(.*)\(/);  // 괄호 전까지 추출
                    const nameOnly = nameMatch ? nameMatch[1] : value;

                    const li = document.createElement('li');
                    li.className = 'list-group-item bg-transparent text-white d-flex justify-content-between align-items-center';
                    li.setAttribute('data-ticker', ticker);
                    li.innerHTML = `
                        <span>${nameOnly}</span>
                        <button class="btn btn-sm btn-outline-danger remove-favorite-btn">제거</button>
                    `;
                    favoriteList.appendChild(li);
                    tickerInput.value = '';
                    tickerList.innerHTML = '';
                    tickerList.style.display = 'none';
                } else {
                    alert('이미 즐겨찾기에 있습니다.');
                }

                tickerInput.value = '';
                tickerList.innerHTML = '';
                tickerList.style.display = 'none';
            });
    });
});
