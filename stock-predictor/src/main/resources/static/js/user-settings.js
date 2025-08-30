document.addEventListener("DOMContentLoaded", () => {
    const togglePassword = document.querySelector('#togglePassword');
    const password = document.querySelector('#password');

    togglePassword.addEventListener('click', () => {
        // 비밀번호 타입 토글
        const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
        password.setAttribute('type', type);

        // 아이콘 토글 (눈 ↔ 눈에 대각선)
        const icon = togglePassword.querySelector('i');
        icon.classList.toggle('bi-eye');
        icon.classList.toggle('bi-eye-slash');
    });

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

        // 비밀번호 토글
        toggleBtn.addEventListener('click', () => {
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
            } else {
                passwordInput.type = 'password';
            }
        });

        // 비밀번호 실시간 체크
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

    // 즐겨찾기 제거
        document.querySelectorAll('.remove-favorite-btn').forEach(button => {
            button.addEventListener('click', function () {
                const li = this.closest('li');
                const ticker = li.getAttribute('data-ticker');

                fetch('/api/favorites/toggle?ticker=' + ticker, {
                    method: 'POST'
                })
                .then(res => {
                    if (res.ok) {
                        li.remove();
                    } else if (res.status === 401) {
                        alert('로그인이 필요합니다.');
                    }
                });
            });
        });

        // 즐겨찾기 추가
        document.getElementById('add-favorite-form').addEventListener('submit', function (e) {
            e.preventDefault();
            const ticker = document.getElementById('ticker-input').value.trim();
            if (!ticker) return;

            fetch('/api/favorites/toggle?ticker=' + ticker, {
                method: 'POST'
            })
            .then(res => res.json())
            .then(data => {
                if (data.isFavorite) {
                    const ul = document.getElementById('favorite-list');
                    const li = document.createElement('li');
                    li.className = 'list-group-item bg-transparent text-white d-flex justify-content-between align-items-center';
                    li.setAttribute('data-ticker', ticker);
                    li.innerHTML = `
                        <span>${ticker}</span>
                        <button class="btn btn-sm btn-outline-danger remove-favorite-btn">제거</button>
                    `;
                    ul.appendChild(li);

                    // 새로 추가된 버튼에 이벤트 재등록
                    li.querySelector('.remove-favorite-btn').addEventListener('click', function () {
                        fetch('/api/favorites/toggle?ticker=' + ticker, { method: 'POST' })
                            .then(res => { if(res.ok) li.remove(); });
                    });
                }
                document.getElementById('ticker-input').value = '';
            });
        });
})