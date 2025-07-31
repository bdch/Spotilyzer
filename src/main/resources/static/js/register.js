document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("registerForm");
    form.addEventListener("submit", async (event) => {
        event.preventDefault()

        const username = document.getElementById("usernameInputField").value;
        const password = document.getElementById("passwordInputField").value;
        const confirmPassword = document.getElementById("confirmPasswordInputField").value;

        if (password !== confirmPassword) {
            alert("Passwords do not match!");
        }

        const payload = {
            username: username,
            password: password
        };

        try {
            const response = await fetch('/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            })

            const data = await response.json();

            if (data.status === 'success') {
                alert('Registration successful! You can now log in.');
                window.location.href = '/'; // Redirect to login page
            } else {
                alert('Error at registration: ' + data.message);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred during registration.');
        }
    })
})
