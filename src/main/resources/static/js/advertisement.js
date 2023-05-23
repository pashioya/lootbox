let allAdvertisements = document.getElementsByClassName("advertisement-link");

async function getFullAdvertisement(advertisementId) {
    return  fetch(`/api/advertisement/${advertisementId}`)
}

export function setAdvertOnclick(advertisement){
    advertisement.addEventListener("click", async () => {
        let advertisementDiv = advertisement.parentNode
        let advertisementId = advertisementDiv.getAttribute("data-ad-id");
        let fullTitle = document.querySelector("#full-title");
        let fullDescription = document.querySelector("#full-description");
        let fullPhoneNumber = document.querySelector("#full-phone-number");
        let fullEmail = document.querySelector("#full-email");
        let response = await getFullAdvertisement(advertisementId);
        let advertisementJson = await response.json();


        fullTitle.innerText = advertisementJson.title;
        fullDescription.innerText = advertisementJson.description;
        fullPhoneNumber.innerText = advertisementJson.phoneNumber;
        fullEmail.innerText = advertisementJson.email;
    })
}

for (let advertisement of allAdvertisements) {
    setAdvertOnclick(advertisement);
}
