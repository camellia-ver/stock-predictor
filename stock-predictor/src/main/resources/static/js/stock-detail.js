document.addEventListener("DOMContentLoaded", () => {
    const favBtn = document.querySelector('.favorite-btn');
    if(favBtn){
        favBtn.addEventListener('click',() => {
            favBtn.classList.toggle('active')
        });
    }
});