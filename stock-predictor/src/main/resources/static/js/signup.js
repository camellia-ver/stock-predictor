document.addEventListener("DOMContentLoaded", () => {
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const strengthBar = document.getElementById('strengthBar');
    const strengthText = document.getElementById('strengthText');
    const form = document.querySelector("form");

    // === 규칙 체크 표시용 ===
    const ruleLength = document.getElementById("rule-length");
    const ruleUpper = document.getElementById("rule-upper");
    const ruleLower = document.getElementById("rule-lower");
    const ruleNumber = document.getElementById("rule-number");
    const ruleSpecial = document.getElementById("rule-special");

    // 규칙 체크 함수
    function updateRules(password) {
        ruleLength.textContent = (password.length >= 8 ? "✅ 최소 8자 이상" : "❌ 최소 8자 이상");
        ruleLength.className = password.length >= 8 ? "text-success" : "text-danger";

        ruleUpper.textContent = (/[A-Z]/.test(password) ? "✅ 대문자 1자 이상 포함" : "❌ 대문자 1자 이상 포함");
        ruleUpper.className = /[A-Z]/.test(password) ? "text-success" : "text-danger";

        ruleLower.textContent = (/[a-z]/.test(password) ? "✅ 소문자 1자 이상 포함" : "❌ 소문자 1자 이상 포함");
        ruleLower.className = /[a-z]/.test(password) ? "text-success" : "text-danger";

        ruleNumber.textContent = (/\d/.test(password) ? "✅ 숫자 1자 이상 포함" : "❌ 숫자 1자 이상 포함");
        ruleNumber.className = /\d/.test(password) ? "text-success" : "text-danger";

        ruleSpecial.textContent = (/[!@#$%^&*]/.test(password) ? "✅ 특수문자 1자 이상 포함" : "❌ 특수문자 1자 이상 포함");
        ruleSpecial.className = /[!@#$%^&*]/.test(password) ? "text-success" : "text-danger";
    }

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

    // 입력 이벤트
    passwordInput.addEventListener('input', function() {
        const password = passwordInput.value;
        const score = checkStrength(password);

        // 규칙 업데이트
        updateRules(password);

        // 강도 표시
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

    // === 비밀번호 표시/숨기기 토글 ===
    const togglePassword = document.getElementById("togglePassword");
    const toggleConfirmPassword = document.getElementById("toggleConfirmPassword");

    togglePassword.addEventListener("click", function() {
        const type = passwordInput.type === "password" ? "text" : "password";
        passwordInput.type = type;
        this.querySelector("i").classList.toggle("bi-eye");
        this.querySelector("i").classList.toggle("bi-eye-slash");
    });

    toggleConfirmPassword.addEventListener("click", function() {
        const type = confirmPasswordInput.type === "password" ? "text" : "password";
        confirmPasswordInput.type = type;
        this.querySelector("i").classList.toggle("bi-eye");
        this.querySelector("i").classList.toggle("bi-eye-slash");
    });
})