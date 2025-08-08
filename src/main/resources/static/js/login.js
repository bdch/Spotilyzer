document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    form.addEventListener('submit', async function (event) {
        event.preventDefault();

        const username = document.getElementById('usernameInputField').value;
        const password = document.getElementById('passwordInputField').value;
        const payload = { username, password };

        try {
            const response = await fetch('/loginPage', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (data.status === 'success') {
                localStorage.setItem('sessionKey', data.sessionKey);
                window.location.href = `/home?sessionKey=${encodeURIComponent(data.sessionKey)}`;
            } else {
                alert("Login failed: " + data.message);
            }
        } catch (error) {
            console.error("Error:", error);
            alert("An error occurred during login.");
        }
    });
});
