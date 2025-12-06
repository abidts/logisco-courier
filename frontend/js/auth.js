const API_URL = 'http://localhost:8080/api';

// Login
document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const error = await response.json();
            showMessage('loginError', error.message || 'Invalid credentials', 'error');
            return;
        }

        const data = await response.json();

        // Store user data
        localStorage.setItem('token', data.token);
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('username', data.username);
        localStorage.setItem('role', data.role);

        // Redirect based on role
        if (data.role === 'ADMIN') {
            window.location.href = 'admin.html';
        } else {
            window.location.href = 'dashboard.html';
        }

    } catch (error) {
        console.error('Error:', error);
        showMessage('loginError', 'An error occurred. Please try again.', 'error');
    }
});

// Register
document.getElementById('registerForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
        showMessage('registerError', 'Passwords do not match', 'error');
        return;
    }

    const userData = {
        fullName: document.getElementById('fullName').value,
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        phoneNumber: document.getElementById('phoneNumber').value,
        password: password,
        role: 'USER'
    };

    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        const data = await response.json();

        if (!response.ok) {
            showMessage('registerError', data.message || 'Registration failed', 'error');
            return;
        }

        showMessage('registerSuccess', 'Registration successful! Redirecting to login...', 'success');

        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);

    } catch (error) {
        console.error('Error:', error);
        showMessage('registerError', 'An error occurred. Please try again.', 'error');
    }
});

function showMessage(elementId, message, type) {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.classList.remove('hidden');

    if (type === 'success') {
        element.classList.add('success-message');
        element.classList.remove('error-message');
    } else {
        element.classList.add('error-message');
        element.classList.remove('success-message');
    }
}
