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
})