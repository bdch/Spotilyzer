import {dashboardDataService} from './dashboardDataService.js';


export function init() {
    dashboardDataService.subscribe(data => {
        if (!data || !data.profile) {
            console.error("No profile data available");
        }
        updateProfile(data.profile);

        displayTopTracks(data.topTracks)
    });
    dashboardDataService.fetchAll();
}

function updateProfile(profile) {
    const img = document.querySelector('.profile-pic');
    if (img && profile.images && profile.images.length > 0) {
        img.src = profile.images[0].url;
    }

    const greeting = document.querySelector('.header-left p');
    if (greeting && profile.displayName) {
        greeting.textContent = `Hello ${profile.displayName}, let's look at your dashboard!`;
    }
}

function displayTopTracks(tracks) {
    const dashboardLayout = document.querySelector('.dashboard-layout');
    if (!dashboardLayout) {
        console.error("Dashboard layout container not found");
    }
    // Create or update the top tracks section
    let topTracksSection = document.querySelector('.top-tracks-section');
    if (!topTracksSection) {
        topTracksSection = createTopTracksSection();
        dashboardLayout.appendChild(topTracksSection);
    }

    // Populate the tracks
    const tracksList = topTracksSection.querySelector('.tracks-list');
    tracksList.innerHTML = ''; // Clear existing content

    tracks.forEach((track, index) => {
        const trackElement = createTrackElement(track, index + 1);
        tracksList.appendChild(trackElement);
    });
}

function createTopTracksSection() {
    const section = document.createElement('div');
    section.className = 'top-tracks-section';
    section.innerHTML = `
        <div class="section-header">
            <h2>Your Top Tracks</h2>
            <p class="section-subtitle">Your most played songs</p>
        </div>
        <div class="tracks-list"></div>
    `;
    return section;
}

function createTrackElement(track, position) {
    const trackDiv = document.createElement('div');
    trackDiv.className = 'track-item';

    // Get album image
    let albumImage = '';
    if (track.album && track.album.images && track.album.images.length > 0) {
        // Use the smallest available image (usually the last one)
        albumImage = track.album.images[track.album.images.length - 1].url;
    }

    // Format artists
    const artistNames = track.artists ?
        track.artists.map(artist => artist.name).join(', ') : 'Unknown Artist';

    // Format duration
    const duration = formatDuration(track.duration_ms);

    trackDiv.innerHTML = `
        ${albumImage ? `<img class="track-album-image" src="${albumImage}" alt="${track.album.name}">` : ''}
            <div class="track-name">${track.name}</div>
                <div class="track-artist">${artistNames}</div>
                    ${track.external_urls && track.external_urls.spotify ?
                        `<a href="${track.external_urls.spotify}" target="_blank" class="track-actions">Open in Spotify</a>` : ''
    }
    `;

    return trackDiv;
}

function formatDuration(durationMs) {
    if (!durationMs) return '0:00';

    const totalSeconds = Math.floor(durationMs / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}
