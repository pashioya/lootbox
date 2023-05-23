const searchBar = document.getElementById("search-bar");
advertisementContainer = document.getElementById("advertisement-container");

searchBar.addEventListener("keyup", async () => {
    if (searchBar.value.length > 0) {
        songTableBody.innerHTML = "";
        let filteredSongs = await getSongByTitle(searchBar.value);
        buildSongsTable(filteredSongs);
    } else {
        songTableBody.innerHTML = "";
        let songs = await getSongs();
        buildSongsTable(songs);
    }

    //     if the table is empty, set the error to visible
    if (songTableBody.innerHTML === "") {
        songsError.style.display = "block";
    } else {
        songsError.style.display = "none";
    }
});

function buildSongsTable(songs) {
    for (let song of songs) {
        let row = document.createElement("tr");
        row.setAttribute("data-href", `/allSongs/fullSong/${song.id}`);
        row.classList.add("table-row");
        row.innerHTML = `
                <td>${song.songTitle}</td>
                <td>${song.trackNumber}</td>
                <td>${song.durationMs}</td>
                <td>${song.explicit}</td>
            `;
        songTableBody.appendChild(row);
    }
}
