// Booking form JavaScript
const API_URL = 'http://localhost:8080/api';
const PINCODE_API = 'https://api.postalpincode.in/pincode/';
let currentStep = 1;
const totalSteps = 5;
let selectedCourierPartnerId = null;
let calculatedPrices = [];
let bookingData = {};

// Set minimum date to today
document.addEventListener('DOMContentLoaded', function() {
    const today = new Date().toISOString().split('T')[0];
    document.querySelector('input[name="preferredPickupDate"]').setAttribute('min', today);

    const originInput = document.getElementById('originPincode');
    const destInput = document.getElementById('destPincode');
    const startBtn = document.getElementById('routeStart');
    if (originInput) {
        originInput.addEventListener('input', () => {
            const v = originInput.value.replace(/[^0-9]/g, '');
            originInput.value = v;
            if (v.length === 6) lookupAndShowPincode(v, 'origin');
        });
        originInput.addEventListener('blur', () => {
            const v = originInput.value;
            if (v && v.length === 6) lookupAndShowPincode(v, 'origin');
        });
    }
    if (destInput) {
        destInput.addEventListener('input', () => {
            const v = destInput.value.replace(/[^0-9]/g, '');
            destInput.value = v;
            if (v.length === 6) lookupAndShowPincode(v, 'dest');
        });
        destInput.addEventListener('blur', () => {
            const v = destInput.value;
            if (v && v.length === 6) lookupAndShowPincode(v, 'dest');
        });
    }
    if (startBtn) {
        startBtn.addEventListener('click', () => {
            document.getElementById('step1').scrollIntoView({ behavior: 'smooth' });
        });
    }
});

function nextStep() {
    if (validateCurrentStep()) {
        if (currentStep < totalSteps) {
            // Save current step data
            saveStepData();
            
            // Special handling for step 4 - calculate price
            if (currentStep === 3) {
                calculatePrice();
            }
            
            // Special handling for step 5 - show review
            if (currentStep === 4) {
                showReview();
            }
            
            currentStep++;
            updateProgressBar();
            showStep(currentStep);
        }
    }
}

function prevStep() {
    if (currentStep > 1) {
        currentStep--;
        updateProgressBar();
        showStep(currentStep);
    }
}

function showStep(step) {
    // Hide all steps
    document.querySelectorAll('.step-content').forEach(content => {
        content.classList.remove('active');
    });
    
    // Show current step
    document.getElementById(`step${step}`).classList.add('active');
}

function updateProgressBar() {
    document.querySelectorAll('.progress-step').forEach((step, index) => {
        const stepNum = index + 1;
        step.classList.remove('active', 'completed');
        
        if (stepNum < currentStep) {
            step.classList.add('completed');
        } else if (stepNum === currentStep) {
            step.classList.add('active');
        }
    });
}

function validateCurrentStep() {
    const currentStepElement = document.getElementById(`step${currentStep}`);
    const requiredFields = currentStepElement.querySelectorAll('[required]');
    let isValid = true;
    
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            isValid = false;
            field.style.borderColor = '#dc3545';
        } else {
            field.style.borderColor = '#ddd';
        }
    });
    
    // Special validation for pincodes
    if (currentStep === 1) {
        const pincode = document.querySelector('input[name="senderPincode"]').value;
        if (pincode && !/^[0-9]{6}$/.test(pincode)) {
            document.getElementById('pickupPincodeError').textContent = 'Pincode must be 6 digits';
            isValid = false;
        } else {
            document.getElementById('pickupPincodeError').textContent = '';
        }
    }
    
    if (currentStep === 2) {
        const pincode = document.querySelector('input[name="receiverPincode"]').value;
        if (pincode && !/^[0-9]{6}$/.test(pincode)) {
            document.getElementById('deliveryPincodeError').textContent = 'Pincode must be 6 digits';
            isValid = false;
        } else {
            document.getElementById('deliveryPincodeError').textContent = '';
        }
    }
    
    return isValid;
}

function saveStepData() {
    const formData = new FormData(document.getElementById('bookingForm'));
    for (let [key, value] of formData.entries()) {
        bookingData[key] = value;
    }
}

async function checkServiceability() {
    const pickupPincode = document.querySelector('input[name="senderPincode"]').value;
    const deliveryPincode = document.querySelector('input[name="receiverPincode"]').value;
    
    if (!pickupPincode || !deliveryPincode) {
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/booking/check-serviceability`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                pickupPincode: pickupPincode,
                deliveryPincode: deliveryPincode
            })
        });
        const text = await response.text();
        const data = text ? JSON.parse(text) : null;
        if (!response.ok) {
            throw new Error((data && data.error) || 'Request failed');
        }
        console.log('Serviceability:', data);
    } catch (error) {
        console.error('Error checking serviceability:', error);
    }
}

async function lookupPincode(pincode) {
    try {
        const res = await fetch(PINCODE_API + encodeURIComponent(pincode));
        const data = await res.json();
        if (Array.isArray(data) && data[0] && data[0].Status === 'Success' && Array.isArray(data[0].PostOffice) && data[0].PostOffice[0]) {
            const po = data[0].PostOffice[0];
            return { district: po.District, state: po.State, city: po.Name };
        }
        return null;
    } catch (e) {
        return null;
    }
}

async function lookupAndShowPincode(pincode, type) {
    const statusEl = document.getElementById(type === 'origin' ? 'originPincodeStatus' : 'destPincodeStatus');
    statusEl.textContent = '';
    statusEl.classList.remove('invalid');
    const info = await lookupPincode(pincode);
    if (info) {
        statusEl.textContent = `${(info.district || info.city || '').toUpperCase()} - ${info.state.toUpperCase()} ✓`;
        // Autofill corresponding form fields
        if (type === 'origin') {
            const pinField = document.querySelector('input[name="senderPincode"]');
            const cityField = document.querySelector('input[name="senderCity"]');
            const stateField = document.querySelector('input[name="senderState"]');
            if (pinField) pinField.value = pincode;
            if (cityField) cityField.value = info.district || info.city || '';
            if (stateField) stateField.value = info.state || '';
            checkServiceability();
        } else {
            const pinField = document.querySelector('input[name="receiverPincode"]');
            const cityField = document.querySelector('input[name="receiverCity"]');
            const stateField = document.querySelector('input[name="receiverState"]');
            if (pinField) pinField.value = pincode;
            if (cityField) cityField.value = info.district || info.city || '';
            if (stateField) stateField.value = info.state || '';
            checkServiceability();
        }
    } else {
        statusEl.textContent = 'Invalid pincode';
        statusEl.classList.add('invalid');
    }
}

async function calculatePrice() {
    const formData = new FormData(document.getElementById('bookingForm'));
    const data = {};
    
    for (let [key, value] of formData.entries()) {
        if (value) {
            data[key] = value;
        }
    }
    
    // Get required fields
    const weight = parseFloat(data.weight) || 0;
    const length = parseFloat(data.length) || 0;
    const width = parseFloat(data.width) || 0;
    const height = parseFloat(data.height) || 0;
    const deliveryType = data.deliveryType || 'STANDARD';
    const packageType = data.packageType || 'PARCEL';
    const pickupPincode = data.senderPincode;
    const deliveryPincode = data.receiverPincode;
    const codEnabled = data.codEnabled === 'on';
    const codAmount = parseFloat(data.codAmount) || 0;
    const insuranceRequired = data.insuranceRequired === 'true';
    const declaredValue = parseFloat(data.declaredValue) || 0;
    
    if (!weight || !pickupPincode || !deliveryPincode) {
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/booking/calculate-price`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                weight: weight,
                length: length,
                width: width,
                height: height,
                deliveryType: deliveryType,
                packageType: packageType,
                pickupPincode: pickupPincode,
                deliveryPincode: deliveryPincode,
                codEnabled: codEnabled,
                codAmount: codAmount,
                insuranceRequired: insuranceRequired,
                declaredValue: declaredValue
            })
        });
        const text = await response.text();
        const prices = text ? JSON.parse(text) : null;
        if (!response.ok) {
            throw new Error((prices && prices.error) || 'Request failed');
        }
        
        if (Array.isArray(prices)) {
            calculatedPrices = prices;
            displayCourierOptions(prices);
        } else {
            // Single price result
            displayPriceSummary(prices);
            selectedCourierPartnerId = prices.courierPartnerId;
        }
    } catch (error) {
        console.error('Error calculating price:', error);
        alert('Error calculating price. Please try again.');
    }
}

function displayCourierOptions(prices) {
    const container = document.getElementById('courierOptions');
    container.innerHTML = '';
    
    if (prices.length === 0) {
        container.innerHTML = '<p>No courier options available for this route.</p>';
        return;
    }
    if (prices.length === 1) {
        // Auto-select the only available option
        selectedCourierPartnerId = prices[0].courierPartnerId || null;
        displayPriceSummary(prices[0]);
    }
    
    prices.forEach(price => {
        const card = document.createElement('div');
        card.className = 'courier-card';
        card.onclick = () => selectCourier(price.courierPartnerId, price);
        
        card.innerHTML = `
            <h4>${price.courierPartner || 'Courier Partner'}</h4>
            <div style="font-size: 24px; font-weight: bold; color: #4CAF50; margin: 10px 0;">
                ₹${price.totalPrice.toFixed(2)}
            </div>
            <div style="color: #666; font-size: 14px;">
                Estimated: ${price.estimatedDays || 'N/A'} days
            </div>
        `;
        
        container.appendChild(card);
    });
}

function selectCourier(partnerId, price) {
    selectedCourierPartnerId = partnerId;
    
    // Update UI
    document.querySelectorAll('.courier-card').forEach(card => {
        card.classList.remove('selected');
    });
    event.currentTarget.classList.add('selected');
    
    // Display price summary
    displayPriceSummary(price);
}

function displayPriceSummary(price) {
    document.getElementById('priceSummary').style.display = 'block';
    document.getElementById('basePrice').textContent = `₹${(price.basePrice || 0).toFixed(2)}`;
    document.getElementById('codCharge').textContent = `₹${(price.codCharge || 0).toFixed(2)}`;
    document.getElementById('insuranceCharge').textContent = `₹${(price.insuranceCharge || 0).toFixed(2)}`;
    document.getElementById('fuelSurcharge').textContent = `₹${(price.fuelSurcharge || 0).toFixed(2)}`;
    document.getElementById('serviceTax').textContent = `₹${(price.serviceTax || 0).toFixed(2)}`;
    document.getElementById('totalPrice').textContent = `₹${(price.totalPrice || 0).toFixed(2)}`;
    document.getElementById('estimatedDays').textContent = price.estimatedDays || 'N/A';
    
    // Store price for final step
    bookingData.price = price;
}

function toggleCOD() {
    const codEnabled = document.querySelector('input[name="codEnabled"]').checked;
    const codAmountGroup = document.getElementById('codAmountGroup');
    
    if (codEnabled) {
        codAmountGroup.style.display = 'block';
    } else {
        codAmountGroup.style.display = 'none';
        document.querySelector('input[name="codAmount"]').value = '';
        calculatePrice();
    }
}

function showReview() {
    const formData = new FormData(document.getElementById('bookingForm'));
    const reviewDiv = document.getElementById('reviewSummary');
    
    let html = '<div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;">';
    
    // Pickup Details
    html += '<h4>Pickup Details</h4>';
    html += `<p><strong>Name:</strong> ${formData.get('senderName')}</p>`;
    html += `<p><strong>Address:</strong> ${formData.get('senderAddress')}, ${formData.get('senderCity')}, ${formData.get('senderState')} - ${formData.get('senderPincode')}</p>`;
    html += `<p><strong>Phone:</strong> ${formData.get('senderPhone')}</p>`;
    
    // Delivery Details
    html += '<h4 style="margin-top: 20px;">Delivery Details</h4>';
    html += `<p><strong>Name:</strong> ${formData.get('receiverName')}</p>`;
    html += `<p><strong>Address:</strong> ${formData.get('receiverAddress')}, ${formData.get('receiverCity')}, ${formData.get('receiverState')} - ${formData.get('receiverPincode')}</p>`;
    html += `<p><strong>Phone:</strong> ${formData.get('receiverPhone')}</p>`;
    
    // Package Details
    html += '<h4 style="margin-top: 20px;">Package Details</h4>';
    html += `<p><strong>Type:</strong> ${formData.get('packageType')}</p>`;
    html += `<p><strong>Weight:</strong> ${formData.get('weight')} kg</p>`;
    html += `<p><strong>Delivery Type:</strong> ${formData.get('deliveryType')}</p>`;
    
    if (formData.get('codEnabled') === 'on') {
        html += `<p><strong>COD Amount:</strong> ₹${formData.get('codAmount') || 0}</p>`;
    }
    
    html += '</div>';
    
    reviewDiv.innerHTML = html;
    
    // Set final price
    if (bookingData.price) {
        document.getElementById('finalTotalPrice').textContent = `₹${bookingData.price.totalPrice.toFixed(2)}`;
    }
}

// Handle form submission
document.getElementById('bookingForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    if (!localStorage.getItem('userId')) {
        openOtpModal();
        return;
    }
    
    const formData = new FormData(this);
    const data = {};
    
    for (let [key, value] of formData.entries()) {
        if (key === 'codEnabled' || key === 'emailNotification' || key === 'smsNotification' || key === 'whatsappNotification') {
            data[key] = value === 'on';
        } else if (key === 'insuranceRequired') {
            data[key] = value === 'true';
        } else if (['weight', 'length', 'width', 'height', 'declaredValue', 'codAmount', 'numberOfPackages'].includes(key)) {
            data[key] = value ? parseFloat(value) : null;
        } else {
            data[key] = value || null;
        }
    }
    
    // Add userId if verified
    if (localStorage.getItem('userId')) {
        data.userId = parseInt(localStorage.getItem('userId'), 10);
    }

    // Add courier partner ID if selected; otherwise omit (self-service)
    if (selectedCourierPartnerId) {
        data.courierPartnerId = selectedCourierPartnerId;
    }
    
    // Add preferred pickup date if set
    if (data.preferredPickupDate) {
        data.preferredPickupDate = new Date(data.preferredPickupDate).toISOString();
    }
    
    try {
        const response = await fetch(`${API_URL}/booking/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        if (response.ok) {
            // Show success message
            const successDiv = document.getElementById('successMessage');
            successDiv.style.display = 'block';
            successDiv.innerHTML = `
                <h3>Booking Successful!</h3>
                <p><strong>Booking ID:</strong> ${result.bookingId}</p>
                <p><strong>Tracking Number:</strong> ${result.trackingNumber}</p>
                <p><strong>AWB Number:</strong> ${result.awbNumber}</p>
                <p><strong>Total Amount:</strong> ₹${result.totalPrice}</p>
                <p><a href="dashboard.html">View in Dashboard</a> | <a href="booking.html">Book Another</a></p>
            `;
            
            // Scroll to top
            window.scrollTo(0, 0);
            
            // Reset form
            this.reset();
            currentStep = 1;
            updateProgressBar();
            showStep(1);
        } else {
            alert('Error: ' + (result.error || 'Failed to create booking'));
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error creating booking. Please try again.');
    }
});

// Auto-check serviceability when pincodes change
document.querySelector('input[name="senderPincode"]').addEventListener('blur', checkServiceability);
document.querySelector('input[name="receiverPincode"]').addEventListener('blur', checkServiceability);

document.querySelector('input[name="senderPincode"]').addEventListener('blur', async (e) => {
    const v = e.target.value;
    if (v && v.length === 6) {
        const info = await lookupPincode(v);
        if (info) {
            const cityField = document.querySelector('input[name="senderCity"]');
            const stateField = document.querySelector('input[name="senderState"]');
            if (cityField && !cityField.value) cityField.value = info.district || info.city || '';
            if (stateField && !stateField.value) stateField.value = info.state || '';
        }
    }
});

document.querySelector('input[name="receiverPincode"]').addEventListener('blur', async (e) => {
    const v = e.target.value;
    if (v && v.length === 6) {
        const info = await lookupPincode(v);
        if (info) {
            const cityField = document.querySelector('input[name="receiverCity"]');
            const stateField = document.querySelector('input[name="receiverState"]');
            if (cityField && !cityField.value) cityField.value = info.district || info.city || '';
            if (stateField && !stateField.value) stateField.value = info.state || '';
        }
    }
});
function openOtpModal() {
    const modal = document.getElementById('otpModal');
    modal.style.display = 'flex';
}

function closeOtpModal() {
    const modal = document.getElementById('otpModal');
    modal.style.display = 'none';
}

async function requestOtp() {
    const phone = document.getElementById('otpPhone').value.replace(/[^0-9]/g, '');
    const name = document.getElementById('otpFullName').value.trim();
    const msg = document.getElementById('otpMessage');
    msg.textContent = '';
    if (!phone || phone.length !== 10) {
        msg.textContent = 'Enter a valid 10-digit mobile number';
        return;
    }
    try {
        const res = await fetch(`${API_URL}/auth/request-otp`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phone, fullName: name })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Failed to request OTP');
        document.getElementById('otpVerify').disabled = false;
        msg.textContent = 'OTP sent to your mobile number';
        msg.style.color = '#0a8f4c';
    } catch (err) {
        msg.textContent = err.message;
        msg.style.color = '#dc3545';
    }
}

async function verifyOtp() {
    const phone = document.getElementById('otpPhone').value.replace(/[^0-9]/g, '');
    const code = document.getElementById('otpCode').value.replace(/[^0-9]/g, '');
    const name = document.getElementById('otpFullName').value.trim();
    const msg = document.getElementById('otpMessage');
    msg.textContent = '';
    if (!code || code.length !== 6) {
        msg.textContent = 'Enter the 6-digit OTP';
        return;
    }
    try {
        const res = await fetch(`${API_URL}/auth/verify-otp`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phone, otp: code, fullName: name })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Invalid OTP');
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('authToken', data.token);
        closeOtpModal();
        // Retry submit
        document.getElementById('bookingForm').dispatchEvent(new Event('submit'));
    } catch (err) {
        msg.textContent = err.message;
        msg.style.color = '#dc3545';
    }
}

document.getElementById('otpRequest')?.addEventListener('click', requestOtp);
document.getElementById('otpVerify')?.addEventListener('click', verifyOtp);
document.getElementById('otpClose')?.addEventListener('click', closeOtpModal);
