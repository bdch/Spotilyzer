async function register() {
    document.querySelector('form').addEventListener('submit', function (event) {

        const username = document.getElementById('usernameInputField').value;
        const password = document.getElementById('passwordInputField').value;
        const confirmPassword = document.getElementById('confirmPasswordInputField').value;

        if (password !== confirmPassword) {
            alert('Passwords do not match!');
            event.preventDefault();

        }

        const payload = {
            username: username,
            password: password
        };
        fetch('/registerPage',
            {
                method: ' POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    //  TODO Redirect to the login page
                    alert('Login successful!');
                } else {
                    alert('Error at login:' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred during login.');
            });

    })
}