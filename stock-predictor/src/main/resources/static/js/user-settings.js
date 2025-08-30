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

    // 비밀번호 표시 토글
    toggleBtn.addEventListener('click', () => {
        passwordInput.type = passwordInput.type === 'password' ? 'text' : 'password';
        toggleBtn.querySelector('i').classList.toggle('bi-eye');
        toggleBtn.querySelector('i').classList.toggle('bi-eye-slash');
    });

    // 실시간 비밀번호 체크
    passwordInput.addEventListener('input', () => {
        const val = passwordInput.value;
        let score = 0;

        // 룰 체크
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

        // 강도 계산
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
    // 2️⃣ 즐겨찾기 관리 (추가/제거)
    // ===============================
    const favoriteList = document.getElementById('favorite-list');
    const addForm = document.getElementById('add-favorite-form');
    const tickerInput = document.getElementById('ticker-input');

    // 이벤트 위임으로 제거 버튼 처리
    favoriteList.addEventListener('click', (e) => {
        if (e.target.classList.contains('remove-favorite-btn')) {
            const li = e.target.closest('li');
            const ticker = li.getAttribute('data-ticker');

            fetch('/api/favorites/toggle?ticker=' + ticker, {
                method: 'POST',
                headers: {
                        [csrfHeader]: csrfToken
                }
            })
            .then(res => {
                if (res.ok) {
                    li.remove();
                } else if (res.status === 401) {
                    alert('로그인이 필요합니다.');
                } else {
                    alert('삭제 실패');
                }
            });
        }
    });

    // 즐겨찾기 추가
    addForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const ticker = tickerInput.value.trim();
        if (!ticker) return;

        fetch('/api/favorites/toggle?ticker=' + ticker, { method: 'POST' })
            .then(res => res.json())
            .then(data => {
                if (data.isFavorite) {
                    const li = document.createElement('li');
                    li.className = 'list-group-item bg-transparent text-white d-flex justify-content-between align-items-center';
                    li.setAttribute('data-ticker', ticker);
                    li.innerHTML = `
                        <span>${ticker}</span>
                        <button class="btn btn-sm btn-outline-danger remove-favorite-btn">제거</button>
                    `;
                    favoriteList.appendChild(li);
                    tickerInput.value = '';
                } else {
                    alert('이미 즐겨찾기에 있습니다.');
                }
            });
    });
});
