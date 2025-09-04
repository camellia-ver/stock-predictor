document.addEventListener("DOMContentLoaded", () => {
    // 모달 요소 선택
    const memoModalEl = document.getElementById("memoModal");
    if (!memoModalEl) return;

    const memoModal = new bootstrap.Modal(memoModalEl);

    // 모달 내부 요소 선택 (fragment 삽입 후에도 항상 모달 내부에서 선택)
    const memoTitle = memoModalEl.querySelector("#memoTitle");
    const memoContent = memoModalEl.querySelector("#memoContent");
    const memoDate = memoModalEl.querySelector("#memoDate");

    // 메모 카드 버튼 선택
    const memoButtons = document.querySelectorAll(".memo-card .btn-outline-primary");

    // 모달 열릴 때: 닫기 버튼 포커스
    memoModalEl.addEventListener("shown.bs.modal", () => {
        const closeBtn = memoModalEl.querySelector(".btn-close");
        if (closeBtn) closeBtn.focus();
    });

    // 모달 닫기 직전: 내부 포커스 제거
    memoModalEl.addEventListener("hide.bs.modal", () => {
        if (document.activeElement && memoModalEl.contains(document.activeElement)) {
            document.activeElement.blur();
        }
    });

    // 모달 닫힌 후: 외부 버튼으로 포커스 이동
    memoModalEl.addEventListener("hidden.bs.modal", () => {
        const firstButton = document.querySelector(".memo-card .btn-outline-primary");
        if (firstButton) firstButton.focus();
    });

    // 각 메모 버튼 클릭 이벤트
    memoButtons.forEach(btn => {
        btn.addEventListener("click", async (e) => {
            e.preventDefault();
            const memoId = btn.getAttribute("data-memo-id");

            try {
                const response = await fetch(`/api/memos/${memoId}`);
                if (!response.ok) throw new Error("메모를 불러올 수 없습니다.");

                const data = await response.json();
                console.log("받은 데이터:", data);

                // 모달 데이터 채우기
                memoTitle.textContent = data.title || "(제목 없음)";
                memoContent.textContent = data.content || "(내용 없음)";
                memoDate.textContent = data.stockDate;

                // 모달 표시
                memoModal.show();
            } catch (err) {
                console.error(err);
                alert(err.message);
            }
        });
    });
});
