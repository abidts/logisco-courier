const API_URL = 'http://localhost:8080/api';

let trackingMap = null;
let trackingMarkers = [];
let trackingPath = null;
let livePollTimer = null;

async function trackShipment() {
    const trackingNumber = document.getElementById('trackingNumber').value.trim();
    if (!trackingNumber) {
        alert('Please enter a tracking number');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/track/${trackingNumber}`);
        if (!response.ok) {
            showError();
            return;
        }
        const shipment = await response.json();
        displayTrackingResult(shipment);

        const historyResponse = await fetch(`${API_URL}/shipments/${shipment.id}/history`);
        const history = await historyResponse.json();
        displayTrackingHistory(history);

        initTrackingMap(shipment, history);
        startLiveTracking(shipment.id);
    } catch (error) {
        console.error('Error:', error);
        showError();
    }
}

function displayTrackingResult(shipment) {
    document.getElementById('trackingResult').classList.remove('hidden');
    document.getElementById('trackingError').classList.add('hidden');

    document.getElementById('detailTrackingNumber').textContent = shipment.trackingNumber;
    const statusBadge = document.getElementById('detailStatus');
    statusBadge.textContent = shipment.status.replace('_', ' ');
    statusBadge.className = `status-badge ${shipment.status}`;
    document.getElementById('detailSender').textContent = `${shipment.senderName}, ${shipment.senderAddress}`;
    document.getElementById('detailReceiver').textContent = `${shipment.receiverName}, ${shipment.receiverAddress}`;

    const estimatedDate = shipment.estimatedDelivery ? new Date(shipment.estimatedDelivery).toLocaleDateString() : 'TBD';
    document.getElementById('detailEstimatedDelivery').textContent = estimatedDate;
}

function displayTrackingHistory(history) {
    const container = document.getElementById('trackingHistory');
    container.innerHTML = '';
    history.forEach(item => {
        const timelineItem = document.createElement('div');
        timelineItem.className = 'timeline-item';
        const timestamp = new Date(item.timestamp).toLocaleString();
        timelineItem.innerHTML = `
            <div class="timeline-content">
                <h4>${item.status.replace('_', ' ')}</h4>
                <p><i class="fas fa-map-marker-alt"></i> ${item.location || 'N/A'}</p>
                <p>${item.description || ''}</p>
                <p class="text-muted"><i class="fas fa-clock"></i> ${timestamp}</p>
            </div>
        `;
        container.appendChild(timelineItem);
    });
}

function showError() {
    document.getElementById('trackingResult').classList.add('hidden');
    document.getElementById('trackingError').classList.remove('hidden');
}

function initTrackingMap(shipment, history) {
    if (!trackingMap) {
        trackingMap = L.map('trackingMap');
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(trackingMap);
    }

    // Clear previous markers/path
    trackingMarkers.forEach(m => trackingMap.removeLayer(m));
    trackingMarkers = [];
    if (trackingPath) {
        trackingMap.removeLayer(trackingPath);
        trackingPath = null;
    }

    // Geocode sender and receiver and fit bounds
    Promise.all([
        geocodeAddress(`${shipment.senderAddress}`),
        geocodeAddress(`${shipment.receiverAddress}`)
    ]).then(points => {
        const [from, to] = points;
        const coords = [];
        if (from) {
            const marker = L.marker([from.lat, from.lon]).addTo(trackingMap).bindPopup('Pickup');
            trackingMarkers.push(marker);
            coords.push([from.lat, from.lon]);
        }
        if (to) {
            const marker = L.marker([to.lat, to.lon]).addTo(trackingMap).bindPopup('Destination');
            trackingMarkers.push(marker);
            coords.push([to.lat, to.lon]);
        }
        if (coords.length) {
            trackingMap.fitBounds(coords);
            if (coords.length === 2) {
                trackingPath = L.polyline(coords, { color: '#3498db' }).addTo(trackingMap);
            }
        }
    }).catch(() => {});

    // Plot history waypoints when locations provided
    const historyLocations = history
        .map(h => h.location)
        .filter(Boolean)
        .slice(0, 10); // limit to recent
    historyLocations.forEach(loc => {
        geocodeAddress(loc).then(p => {
            if (p) {
                const marker = L.circleMarker([p.lat, p.lon], { radius: 6, color: '#e67e22' })
                    .addTo(trackingMap)
                    .bindPopup(loc);
                trackingMarkers.push(marker);
            }
        }).catch(() => {});
    });
}

async function geocodeAddress(query) {
    try {
        const url = `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(query)}`;
        const res = await fetch(url, { headers: { 'Accept-Language': 'en' } });
        const data = await res.json();
        if (Array.isArray(data) && data.length > 0) {
            const { lat, lon } = data[0];
            return { lat: parseFloat(lat), lon: parseFloat(lon) };
        }
        return null;
    } catch (e) {
        return null;
    }
}

function startLiveTracking(shipmentId) {
    if (livePollTimer) {
        clearInterval(livePollTimer);
    }
    livePollTimer = setInterval(async () => {
        try {
            const historyResponse = await fetch(`${API_URL}/shipments/${shipmentId}/history`);
            const history = await historyResponse.json();
            displayTrackingHistory(history);
            // Attempt to plot only latest location
            const last = history.find(h => h.location);
            if (last) {
                const p = await geocodeAddress(last.location);
                if (p) {
                    const marker = L.circleMarker([p.lat, p.lon], { radius: 6, color: '#27ae60' })
                        .addTo(trackingMap)
                        .bindPopup(`${last.status.replace('_',' ')}: ${last.location}`);
                    trackingMarkers.push(marker);
                }
            }
        } catch (e) {}
    }, 15000); // 15s
}

document.querySelector('.hamburger')?.addEventListener('click', function() {
    document.querySelector('.nav-menu').classList.toggle('active');
});

document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({ behavior: 'smooth' });
        }
    });
});
