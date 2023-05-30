const removeButton = document.getElementById('delete-button');


removeButton.addEventListener('click', removeIdea);

export function removeIdea(event) {
    const clickedCard = event.target.closest('button');
    const cardId = clickedCard.getAttribute('data-ad-id')

    fetch(`/api/delete-advertisement/${cardId}`, {
        method: 'DELETE'
    }).then(
        response => {
            if (response.ok) {
                window.location.reload();
            }
        }
    )
}