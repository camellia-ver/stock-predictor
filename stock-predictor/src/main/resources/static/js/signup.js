document.addEventListener("DOMContentLoaded", () => {
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const strengthBar = document.getElementById('strengthBar');
    const strengthText = document.getElementById('strengthText');
    const form = document.querySelector("form");

    // 비밀번호 강도 체크 함수
    function checkStrength(password) {
        let score = 0;
        if (password.length >= 8) score++;
        if (/[A-Z]/.test(password)) score++;
        if (/[a-z]/.test(password)) score++;
        if (/\d/.test(password)) score++;
        if (/[!@#$%^&*]/.test(password)) score++;
        return score;
    }

    passwordInput.addEventListener('input', function() {
        const score = checkStrength(passwordInput.value);
        let strength = "";
        let barClass = "";
        let percent = (score / 5) * 100;

        if (score <= 2) {
            strength = "Weak 🔴";
            barClass = "bg-danger";
        } else if (score === 3 || score === 4) {
            strength = "Medium 🟡";
            barClass = "bg-warning";
        } else if (score === 5) {
            strength = "Strong 🟢";
            barClass = "bg-success";
        }

        strengthBar.style.width = percent + "%";
        strengthBar.className = "progress-bar " + barClass;
        strengthText.textContent = strength;

        checkPasswordMatch();
    });

    // 비밀번호 일치 검사
    function checkPasswordMatch() {
        if (confirmPasswordInput.value === "") {
            confirmPasswordInput.classList.remove("is-valid", "is-invalid");
            return;
        }
        if (passwordInput.value === confirmPasswordInput.value) {
            confirmPasswordInput.classList.remove("is-invalid");
            confirmPasswordInput.classList.add("is-valid");
        } else {
            confirmPasswordInput.classList.remove("is-valid");
            confirmPasswordInput.classList.add("is-invalid");
        }
    }

    confirmPasswordInput.addEventListener('input', checkPasswordMatch);

    form.addEventListener('submit', function(e) {
        if (passwordInput.value !== confirmPasswordInput.value) {
            e.preventDefault();
            confirmPasswordInput.classList.remove("is-valid");
            confirmPasswordInput.classList.add("is-invalid");
        }
    });

    // 비밀번호 확인 검사
    form.addEventListener('submit', function(e) {
        if (passwordInput.value !== confirmPasswordInput.value) {
            e.preventDefault();
            confirmPasswordInput.classList.add("is-invalid");
        } else {
            confirmPasswordInput.classList.remove("is-invalid");
        }
    });
})