import {dashboardDataService} from './dashboardDataService.js';


export function init() {
    dashboardDataService.subscribe(data => {
        if (!data || !data.profile) {
            console.error("No profile data available");
        }

        const img = document.querySelector('.profile-pic')
        if (img && data.profile.images && data.profile.images.length > 0) {
            img.src = data.profile.images[0].url;
        }

        const greeting = document.querySelector('.header-left p');
        if (greeting && data.profile.displayName) {
            greeting.textContent = `Hello ${data.profile.displayName}, lets look at your dashboard!`;
        }
    });
    dashboardDataService.fetchAll();
}
