document.addEventListener("DOMContentLoaded", () => {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    const memoModalEl = document.getElementById("memoModal");
    if (!memoModalEl) return;
    const memoModal = new bootstrap.Modal(memoModalEl);

    const memoTitle = memoModalEl.querySelector("#memoTitle");
    const memoContent = memoModalEl.querySelector("#memoContent");
    const memoDate = memoModalEl.querySelector("#memoDate");
    const editForm = memoModalEl.querySelector("#editForm");
    const editTitle = memoModalEl.querySelector("#editTitle");
    const editContent = memoModalEl.querySelector("#editContent");
    const editBtn = memoModalEl.querySelector("#editBtn");
    const deleteBtn = memoModalEl.querySelector("#deleteBtn");
    const saveEditBtn = memoModalEl.querySelector("#saveEditBtn");
    const cancelEditBtn = memoModalEl.querySelector("#cancelEditBtn");

    let currentMemoId = null;

    // 메모 카드 클릭
    document.querySelectorAll(".memo-card .btn-outline-primary").forEach(btn => {
        btn.addEventListener("click", async () => {
            currentMemoId = btn.dataset.memoId;
            if (!currentMemoId || isNaN(currentMemoId)) {
                alert("메모 ID가 유효하지 않습니다.");
                return;
            }

            try {
                const response = await fetch(`/api/memos/${currentMemoId}`);
                if (!response.ok) throw new Error("메모를 불러올 수 없습니다.");

                const data = await response.json();
                memoTitle.textContent = data.title || "(제목 없음)";
                memoContent.textContent = data.content || "(내용 없음)";
                memoDate.textContent = data.stockDate || "";

                closeEditForm();
                memoModal.show();
            } catch (err) {
                console.error(err);
                alert(err.message);
            }
        });
    });

    // 수정 버튼
    editBtn.addEventListener("click", () => {
        editForm.classList.remove("d-none");
        editTitle.value = memoTitle.textContent;
        editContent.value = memoContent.textContent;
        memoTitle.classList.add("d-none");
        memoContent.classList.add("d-none");
        editBtn.classList.add("d-none");
        deleteBtn.classList.add("d-none");
    });

    // 수정 저장
    saveEditBtn.addEventListener("click", async () => {
        if (!currentMemoId) return;

        try {
            const response = await fetch(`/api/memos/${currentMemoId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json" ,
                    [header]: token
                },
                body: JSON.stringify({ title: editTitle.value, content: editContent.value })
            });
            if (!response.ok) throw new Error("메모 수정 실패");

            const data = await response.json();
            memoTitle.textContent = data.title;
            memoContent.textContent = data.content;
            closeEditForm();
        } catch (err) {
            console.error(err);
            alert(err.message);
        }
    });

    // 수정 취소
    cancelEditBtn.addEventListener("click", closeEditForm);

    function closeEditForm() {
        editForm.classList.add("d-none");
        memoTitle.classList.remove("d-none");
        memoContent.classList.remove("d-none");
        editBtn.classList.remove("d-none");
        deleteBtn.classList.remove("d-none");
    }

    // 삭제 버튼
    deleteBtn.addEventListener("click", async () => {
        if (!currentMemoId || !confirm("정말로 메모를 삭제하시겠습니까?")) return;

        try {
            const response = await fetch(`/api/memos/${currentMemoId}`, {
                method: "DELETE",
                headers: {
                        [header]: token   // CSRF 토큰 헤더
                }
            });
            if (!response.ok) throw new Error("메모 삭제 실패");

            memoModal.hide();
            const memoCard = document.querySelector(`.memo-card button[data-memo-id="${currentMemoId}"]`)?.closest(".memo-card");
            if (memoCard) memoCard.remove();

            location.reload();
        } catch (err) {
            console.error(err);
            alert(err.message);
        }
    });

    // 모달 포커스
    memoModalEl.addEventListener("shown.bs.modal", () => memoModalEl.querySelector(".btn-close")?.focus());
    memoModalEl.addEventListener("hide.bs.modal", () => document.activeElement?.blur());
});
