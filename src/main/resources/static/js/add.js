import {removeIdea} from "./remove.js"
import {setAdvertOnclick} from "./advertisement.js";

const adTitleInput = document.getElementById("title");
const adDescriptionInput = document.getElementById("description");
const adEmailInput = document.getElementById("email");
const adPhoneInput = document.getElementById("phoneNumber");

const fileInput = document.querySelector('input[type="file"]');
const form = document.getElementById("ad-form");
const clsBtn = document.querySelectorAll(".btn-close");

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
                clsBtn.forEach(btn => btn.click());
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
    card.classList.add("transformable");
    card.classList.add("position-relative");

    card.setAttribute("data-ad-id", ad.id)
    const gcloudUrl = "https://storage.googleapis.com/lootbox-bucketeu/";
    setAdvertOnclick(card);
    card.innerHTML += `
                        <a class="stretched-link advertisement-link" data-bs-toggle="modal" href="#full-advertisement-display" role="button"></a>
                        <div class="card shadow-sm border-">
                        <i id="remove-icon" class="bi bi-trash remove-icon"></i>
                        <img class="w-100 h-100" src="${gcloudUrl} + ${ad.image}">
                        <div class="card-body">
                            <p class="card-title text-bold fs-4">${ad.title}</p>
                            <p class="card-text">${ad.description}</p>
                        </div>`;
    const removeIcon = document.getElementById("remove-icon");
    removeIcon.addEventListener('click', removeIdea);

    feed.appendChild(card);
}

