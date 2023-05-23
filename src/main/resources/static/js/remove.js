const removeButtons = document.querySelectorAll('.advertisement #remove-icon');

removeButtons.forEach((removeBtn) => {
    removeBtn.addEventListener('click', removeIdea);
});

export function removeIdea(event) {
    if (event.target.tagName.toLowerCase() === 'a') {
        return;
    }

    const clickedCard = event.target.closest('div.advertisement');
    const cardId = clickedCard.getAttribute('data-ad-id')

    fetch(`/api/delete-advertisement/${cardId}`, {
        method: 'DELETE'
    }).then()
}