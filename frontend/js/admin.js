const API_URL = 'http://localhost:8080/api';
let allShipments = [];
let allUsers = [];
let allInvoices = [];
let shipmentStatusFilter = '';
let shipmentSearchQuery = '';

// Check admin authentication
function checkAdminAuth() {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (!token || role !== 'ADMIN') {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// Initialize admin panel
document.addEventListener('DOMContentLoaded', async () => {
    if (!checkAdminAuth()) return;

    const username = localStorage.getItem('username');
    document.getElementById('adminName').textContent = username;

    await loadAdminData();
    loadSettings();
});

async function loadAdminData() {
    await Promise.all([
        loadAllShipments(),
        loadAllUsers(),
        loadAllInvoices()
    ]);

    updateAdminStats();
}

async function loadAllShipments() {
    try {
        const response = await fetch(`${API_URL}/shipments`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            allShipments = await response.json();
            adminFilterShipments();
        }
    } catch (error) {
        console.error('Error loading shipments:', error);
    }
}

async function loadAllUsers() {
    try {
        const response = await fetch(`${API_URL}/admin/users`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            allUsers = await response.json();
            displayAdminUsers();
        }
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

async function loadAllInvoices() {
    try {
        const response = await fetch(`${API_URL}/invoices`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            allInvoices = await response.json();
            displayAdminInvoices();
        }
    } catch (error) {
        console.error('Error loading invoices:', error);
    }
}

function updateAdminStats() {
    document.getElementById('adminTotalShipments').textContent = allShipments.length;
    document.getElementById('adminTotalUsers').textContent = allUsers.length;

    const active = allShipments.filter(s =>
        !['DELIVERED', 'CANCELLED'].includes(s.status)).length;
    document.getElementById('adminActiveShipments').textContent = active;

    const revenue = allInvoices
        .filter(i => i.paymentStatus === 'PAID')
        .reduce((sum, i) => sum + i.totalAmount, 0);
    document.getElementById('adminTotalRevenue').textContent = `$${revenue.toFixed(2)}`;
}

function displayAdminShipments(displayList = null) {
    const container = document.getElementById('adminShipmentsList');
    const list = Array.isArray(displayList) ? displayList : allShipments;

    const table = `
        <table>
            <thead>
                <tr>
                    <th>Tracking #</th>
                    <th>Sender</th>
                    <th>Receiver</th>
                    <th>Status</th>
                    <th>Amount</th>
                    <th>Date</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${list.map(shipment => `
                    <tr>
                        <td>${shipment.trackingNumber}</td>
                        <td>${shipment.senderName}</td>
                        <td>${shipment.receiverName}</td>
                        <td><span class="status-badge ${shipment.status}">${shipment.status.replace('_', ' ')}</span></td>
                        <td>$${shipment.totalPrice?.toFixed(2) || '0.00'}</td>
                        <td>${new Date(shipment.createdAt).toLocaleDateString()}</td>
                        <td>
                            <button onclick="updateShipmentStatus(${shipment.id})" class="btn-small btn-primary">Update</button>
                            <button onclick="deleteShipment(${shipment.id})" class="btn-small btn-danger">Delete</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;

    container.innerHTML = table;
}

function adminFilterShipments() {
    const statusEl = document.getElementById('adminStatusFilter');
    shipmentStatusFilter = statusEl ? statusEl.value : '';

    const searchEl = document.getElementById('adminSearch');
    shipmentSearchQuery = searchEl ? searchEl.value.trim().toLowerCase() : '';

    let filtered = allShipments;

    if (shipmentStatusFilter) {
        filtered = filtered.filter(s => s.status === shipmentStatusFilter);
    }

    if (shipmentSearchQuery) {
        filtered = filtered.filter(s =>
            (s.trackingNumber || '').toLowerCase().includes(shipmentSearchQuery) ||
            (s.senderName || '').toLowerCase().includes(shipmentSearchQuery) ||
            (s.receiverName || '').toLowerCase().includes(shipmentSearchQuery)
        );
    }

    displayAdminShipments(filtered);
}

document.addEventListener('DOMContentLoaded', () => {
    const searchEl = document.getElementById('adminSearch');
    if (searchEl) {
        searchEl.addEventListener('input', adminFilterShipments);
    }
});

function displayAdminUsers() {
    const container = document.getElementById('adminUsersList');

    const table = `
        <table>
            <thead>
                <tr>
                    <th>Username</th>
                    <th>Full Name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${allUsers.map(user => `
                    <tr>
                        <td>${user.username}</td>
                        <td>${user.fullName || 'N/A'}</td>
                        <td>${user.email}</td>
                        <td>${user.role}</td>
                        <td>${user.active ? 'Active' : 'Inactive'}</td>
                        <td>
                            <button onclick="toggleUserStatus(${user.id}, ${user.active})" class="btn-small btn-primary">
                                ${user.active ? 'Deactivate' : 'Activate'}
                            </button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;

    container.innerHTML = table;
}

function displayAdminInvoices() {
    const container = document.getElementById('adminInvoicesList');

    const table = `
        <table>
            <thead>
                <tr>
                    <th>Invoice #</th>
                    <th>Tracking #</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Issued Date</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${allInvoices.map(invoice => `
                    <tr>
                        <td>${invoice.invoiceNumber}</td>
                        <td>${invoice.shipment?.trackingNumber || 'N/A'}</td>
                        <td>$${invoice.totalAmount?.toFixed(2) || '0.00'}</td>
                        <td>${invoice.paymentStatus}</td>
                        <td>${new Date(invoice.issuedDate).toLocaleDateString()}</td>
                        <td>
                            <button onclick="viewInvoice('${invoice.invoiceNumber}')" class="btn-small btn-primary">View</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;

    container.innerHTML = table;
}

// Update shipment status
function updateShipmentStatus(shipmentId) {
    const modal = document.getElementById('updateShipmentModal');
    document.getElementById('updateShipmentId').value = shipmentId;
    modal.style.display = 'block';
}

function closeUpdateModal() {
    document.getElementById('updateShipmentModal').style.display = 'none';
}

document.getElementById('updateShipmentForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const shipmentId = document.getElementById('updateShipmentId').value;
    const status = document.getElementById('updateStatus').value;
    const location = document.getElementById('updateLocation').value;
    const description = document.getElementById('updateDescription').value;

    try {
        const response = await fetch(`${API_URL}/shipments/${shipmentId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({ status, location, description })
        });

        if (response.ok) {
            alert('Shipment status updated successfully!');
            closeUpdateModal();
            await loadAllShipments();
        } else {
            alert('Failed to update shipment status');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('An error occurred');
    }
});

// Settings management
async function loadSettings() {
    try {
        const response = await fetch(`${API_URL}/admin/settings`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            const settings = await response.json();
            settings.forEach(setting => {
                const element = document.getElementById(setting.settingKey);
                if (element && element.type === 'checkbox') {
                    element.checked = setting.enabled;
                }
            });
        }
    } catch (error) {
        console.error('Error loading settings:', error);
    }
}

async function updateSetting(key, value) {
    const settingData = {
        settingKey: key,
        settingValue: value.toString(),
        enabled: value,
        type: 'BOOLEAN'
    };

    try {
        const response = await fetch(`${API_URL}/admin/settings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(settingData)
        });

        if (response.ok) {
            console.log('Setting updated successfully');
        }
    } catch (error) {
        console.error('Error updating setting:', error);
    }
}

// Navigation
document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', function(e) {
        e.preventDefault();
        const page = this.getAttribute('data-page');
        if (page) {
            showAdminPage(page);
        }
    });
});

function showAdminPage(pageName) {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
        if (item.getAttribute('data-page') === pageName) {
            item.classList.add('active');
        }
    });

    document.querySelectorAll('.page-section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(pageName)?.classList.add('active');

    const titles = {
        'admin-overview': 'Admin Overview',
        'admin-shipments': 'All Shipments',
        'admin-users': 'Users Management',
        'admin-invoices': 'All Invoices',
        'admin-settings': 'System Settings'
    };
    document.getElementById('adminPageTitle').textContent = titles[pageName] || 'Admin Panel';
}

function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}
