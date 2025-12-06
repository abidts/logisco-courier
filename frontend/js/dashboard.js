const API_URL = 'http://localhost:8080/api';
let currentUser = null;
let userShipments = [];

// Check authentication
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// Initialize dashboard
document.addEventListener('DOMContentLoaded', async () => {
    if (!checkAuth()) return;

    const username = localStorage.getItem('username');
    document.getElementById('userFullName').textContent = username;

    await loadUserData();
    await loadDashboardData();
});

async function loadUserData() {
    const userId = localStorage.getItem('userId');
    try {
        const response = await fetch(`${API_URL}/users/${userId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            currentUser = await response.json();
        }
    } catch (error) {
        console.error('Error loading user data:', error);
    }
}

async function loadDashboardData() {
    const userId = localStorage.getItem('userId');

    try {
        const response = await fetch(`${API_URL}/shipments/user/${userId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            userShipments = await response.json();
            updateDashboardStats();
            displayRecentShipments();
            displayAllShipments();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function updateDashboardStats() {
    document.getElementById('totalShipments').textContent = userShipments.length;

    const active = userShipments.filter(s =>
        !['DELIVERED', 'CANCELLED'].includes(s.status)).length;
    document.getElementById('activeShipments').textContent = active;

    const delivered = userShipments.filter(s => s.status === 'DELIVERED').length;
    document.getElementById('deliveredShipments').textContent = delivered;

    const total = userShipments.reduce((sum, s) => sum + (s.totalPrice || 0), 0);
    document.getElementById('totalSpent').textContent = `$${total.toFixed(2)}`;
}

function displayRecentShipments() {
    const container = document.getElementById('recentShipmentsList');
    const recent = userShipments.slice(0, 5);

    container.innerHTML = recent.map(shipment => `
        <div class="shipment-card">
            <div class="shipment-header">
                <h3>${shipment.trackingNumber}</h3>
                <span class="status-badge ${shipment.status}">${shipment.status.replace('_', ' ')}</span>
            </div>
            <p><strong>To:</strong> ${shipment.receiverName}</p>
            <p><strong>Created:</strong> ${new Date(shipment.createdAt).toLocaleDateString()}</p>
        </div>
    `).join('');
}

function displayAllShipments() {
    const container = document.getElementById('allShipmentsList');

    if (userShipments.length === 0) {
        container.innerHTML = '<p>No shipments found.</p>';
        return;
    }

    const table = `
        <table>
            <thead>
                <tr>
                    <th>Tracking Number</th>
                    <th>Receiver</th>
                    <th>Status</th>
                    <th>Amount</th>
                    <th>Date</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${userShipments.map(shipment => `
                    <tr>
                        <td>${shipment.trackingNumber}</td>
                        <td>${shipment.receiverName}</td>
                        <td><span class="status-badge ${shipment.status}">${shipment.status.replace('_', ' ')}</span></td>
                        <td>$${shipment.totalPrice?.toFixed(2) || '0.00'}</td>
                        <td>${new Date(shipment.createdAt).toLocaleDateString()}</td>
                        <td>
                            <button onclick="viewShipmentDetails(${shipment.id})" class="btn-small btn-primary">View</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;

    container.innerHTML = table;
}

// Create new shipment
document.getElementById('shipmentForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const shipmentData = {
        user: { id: localStorage.getItem('userId') },
        senderName: formData.get('senderName'),
        senderPhone: formData.get('senderPhone'),
        senderEmail: formData.get('senderEmail'),
        senderAddress: formData.get('senderAddress'),
        receiverName: formData.get('receiverName'),
        receiverPhone: formData.get('receiverPhone'),
        receiverEmail: formData.get('receiverEmail'),
        receiverAddress: formData.get('receiverAddress'),
        packageDescription: formData.get('packageDescription'),
        weight: parseFloat(formData.get('weight')),
        dimensions: formData.get('dimensions'),
        shipmentType: formData.get('shipmentType'),
        priority: formData.get('priority'),
        pickupDate: formData.get('pickupDate')
    };

    try {
        const response = await fetch(`${API_URL}/shipments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(shipmentData)
        });

        if (response.ok) {
            const newShipment = await response.json();
            showMessage('shipmentSuccess',
                `Shipment created successfully! Tracking Number: ${newShipment.trackingNumber}`,
                'success');
            e.target.reset();

            await loadDashboardData();

            setTimeout(() => {
                showPage('shipments');
            }, 2000);
        } else {
            const error = await response.json();
            showMessage('shipmentError', error.message || 'Failed to create shipment', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('shipmentError', 'An error occurred. Please try again.', 'error');
    }
});

// Navigation
document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', function(e) {
        e.preventDefault();
        const page = this.getAttribute('data-page');
        if (page) {
            showPage(page);
        }
    });
});

function showPage(pageName) {
    // Update nav items
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
        if (item.getAttribute('data-page') === pageName) {
            item.classList.add('active');
        }
    });

    // Update page sections
    document.querySelectorAll('.page-section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(pageName)?.classList.add('active');

    // Update page title
    const titles = {
        'overview': 'Dashboard',
        'shipments': 'My Shipments',
        'new-shipment': 'New Shipment',
        'invoices': 'Invoices',
        'profile': 'Profile'
    };
    document.getElementById('pageTitle').textContent = titles[pageName] || 'Dashboard';
}

function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}

function showMessage(elementId, message, type) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.classList.remove('hidden');

        setTimeout(() => {
            element.classList.add('hidden');
        }, 5000);
    }
}
