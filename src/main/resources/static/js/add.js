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
    card.classList.add("advertisement col border-none");

    card.innerHTML += `<div class="card shadow-sm border-">
                        <svg
                                class="bd-placeholder-img card-img-top"
                                width="100%"
                                height="225"
                                xmlns="http://www.w3.org/2000/svg"
                                role="img"
                                aria-label="Placeholder: Thumbnail"
                                preserveAspectRatio="xMidYMid slice"
                                focusable="false"
                        >
                            <title>Placeholder</title>
                            <rect
                                    width="100%"
                                    height="100%"
                                    fill="#55595c"
                            />
                            <text
                                    x="50%"
                                    y="50%"
                                    fill="#eceeef"
                                    dy=".3em"
                            >
                                Thumbnail
                            </text>
                        </svg>
                        <div class="card-body">
                            <p class="card-title text-bold fs-4">${ad.title}</p>
                            <p class="card-text">${ad.description}</p>
                        </div>`;
    feed.appendChild(card);
}

