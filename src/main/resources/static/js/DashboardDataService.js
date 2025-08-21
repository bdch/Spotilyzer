class DashboardDataService {

    constructor() {
        this.subscribers = [];
        this.data = {}
    }

    async fetchAll() {
        try {
            // Parallel API calls to fetch data
            const [profile, topTracks,] = await Promise.all([
                fetch('/api/spotify/currentUserProfile').then(r => r.json()),
                fetch('/api/spotify/currentUserTopTracks').then(r => r.json())
            ])
            this.data = {
                profile: profile,
                topTracks: topTracks,
            }
            this.notifySubscribers();
        } catch (e) {
            console.error("Failed to fetch dashboard data", e);
            return null;
        }
    }

    subscribe(callback) {
        if (typeof callback === 'function') {
            this.subscribers.push(callback);
        } else {
            console.error("Subscriber must be a function");
        }
    }

    notifySubscribers() {
        this.subscribers.forEach(callback => callback(this.data));
    }
}

// Singleton instance
export const dashboardDataService = new DashboardDataService();
