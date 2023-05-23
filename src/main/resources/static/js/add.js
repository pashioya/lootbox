const adTitleInput = document.getElementById("title");
const adDescriptionInput = document.getElementById("description");
const adEmailInput = document.getElementById("email");
const adPhoneInput = document.getElementById("phoneNumber");

const fileInput = document.querySelector('input[type="file"]');
const form = document.getElementById("ad-form");

form.addEventListener("submit", trySubmitAd);

function trySubmitAd(event) {
    event.preventDefault();

    const formData = new FormData();
    formData.append('title', adTitleInput.value);
    formData.append('description', adDescriptionInput.value);
    formData.append('email', adEmailInput.value);
    formData.append('phoneNumber', adPhoneInput.value);
    formData.append('image', fileInput.files[0]);

    const formIsValid = form.checkValidity();
    console.log(formIsValid);
    if (formIsValid) {
        fetch('/api/add-advertisement', {
            method: "POST",
            body: formData
        }).then(async response => {
            if (response.status === 201) {
                form.reset();
                form.classList.remove('was-validated');

                const newAd = await response.json();
                addAdvertisement(newAd);
            }
        });
    }
}

async function addAdvertisement(ad) {
    const feed = document.getElementById("advertisement-container");

    const card = document.createElement("div");
    card.classList.add("advertisement");
    card.classList.add("col");
    card.classList.add("border-none");


    card.innerHTML += `<div class="card shadow-sm border-">
                        <img class="w-100 h-100" src="${ad.image}"
                        <div class="card-body">
                            <p class="card-title text-bold fs-4">${ad.title}</p>
                            <p class="card-text">${ad.description}</p>
                        </div>`;
    feed.appendChild(card);
}

