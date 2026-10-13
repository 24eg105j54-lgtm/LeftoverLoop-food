/* ==========================================================================
   LeftoverLoop Donor Dashboard JavaScript
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
  AuthManager.requireAuth(['ROLE_DONOR', 'ROLE_ORGANIZATION', 'ROLE_ADMIN']);
  loadDonorData();

  document.getElementById('donationForm').addEventListener('submit', handleSaveDonation);
  document.getElementById('verifyForm').addEventListener('submit', handleVerifyCode);
});

let myDonations = [];
let incomingRequests = [];

async function loadDonorData() {
  try {
    myDonations = await ApiClient.getMyDonations();
    incomingRequests = await ApiClient.getIncomingRequests();

    updateDonorStats();
    renderDonationsGrid();
    renderIncomingRequestsTable();
  } catch (err) {
    showToast(err.message || 'Failed to load donor data', 'error');
  }
}

function updateDonorStats() {
  const active = myDonations.filter(d => d.status === 'AVAILABLE' || d.status === 'PARTIALLY_RESERVED');
  const pending = incomingRequests.filter(r => r.status === 'PENDING');
  const completed = incomingRequests.filter(r => r.status === 'COLLECTED');
  const totalQty = myDonations.reduce((sum, d) => sum + d.initialQuantity, 0);

  document.getElementById('donorActiveCount').textContent = active.length;
  document.getElementById('donorPendingRequests').textContent = pending.length;
  document.getElementById('donorCompletedPickups').textContent = completed.length;
  document.getElementById('donorTotalQuantity').textContent = totalQty;
  document.getElementById('incomingBadge').textContent = pending.length;
}

function renderDonationsGrid() {
  const grid = document.getElementById('donorDonationsGrid');
  if (!myDonations || myDonations.length === 0) {
    grid.innerHTML = '<div class="glass-card" style="grid-column: 1/-1; text-align: center; padding: 2rem;"><p style="color: var(--text-muted);">You have no food listings yet. Click "Post New Donation" to start!</p></div>';
    return;
  }

  grid.innerHTML = myDonations.map(item => {
    const risk = typeof getFoodRiskInfo === 'function' ? getFoodRiskInfo(item) : null;
    const expiryAlert = typeof getExpiryAlertBadge === 'function' ? getExpiryAlertBadge(item.expiryDate) : '';

    return `
    <div class="glass-card donation-card">
      <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1555244162-803834f70033?w=600'}" class="donation-image" alt="${item.title}" onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1555244162-803834f70033?w=600';">
      <div class="donation-header">
        <h3 class="donation-title">${item.title}</h3>
        ${renderStatusBadge(item.statusColor, item.status)}
      </div>
      <div class="donation-meta">
        <div class="donation-meta-item">📍 ${item.location}</div>
        <div class="donation-meta-item">🕒 Expiry: ${formatDate(item.expiryDate)} (${getTimeRemainingText(item.expiryDate)}) ${expiryAlert}</div>
        <div class="donation-meta-item">🏷️ Category: ${item.category}</div>
        ${risk ? `
          <div style="margin-top: 0.4rem;">
            <span class="badge ${risk.badgeClass}" title="${risk.disclaimer}" style="font-size: 0.75rem;">🛡️ ${risk.label}: ${risk.reason}</span>
          </div>
        ` : ''}
      </div>
      <div class="quantity-bar">
        <div class="quantity-fill" style="width: ${(item.remainingQuantity / item.initialQuantity) * 100}%;"></div>
      </div>
      <div style="display: flex; justify-content: space-between; align-items: center; font-size: 0.9rem; font-weight: 600; margin-bottom: 0.75rem;">
        <span style="color: var(--accent-green);">Available: ${item.remainingQuantity} ${item.unit}</span>
        <span style="color: var(--text-muted);">Total: ${item.initialQuantity}</span>
      </div>
      <div style="display: flex; gap: 0.5rem; justify-content: flex-end;">
        <button onclick="editDonation(${item.id})" class="btn btn-secondary" style="padding: 0.35rem 0.75rem; font-size: 0.85rem;">Edit</button>
        <button onclick="deleteDonation(${item.id})" class="btn btn-danger" style="padding: 0.35rem 0.75rem; font-size: 0.85rem;">Delete</button>
      </div>
    </div>
  `;
  }).join('');
}

function renderIncomingRequestsTable() {
  const tbody = document.getElementById('incomingRequestsTbody');
  if (!incomingRequests || incomingRequests.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No incoming requests for your listings.</td></tr>';
    return;
  }

  tbody.innerHTML = incomingRequests.map(req => `
    <tr>
      <td><strong>${req.donationTitle}</strong></td>
      <td>
        <div><strong>${req.receiverName}</strong></div>
        <div style="font-size: 0.8rem; color: var(--text-muted);">${req.receiverOrganization || req.receiverEmail}</div>
      </td>
      <td>${req.requestedQuantity} ${req.donationUnit}</td>
      <td>${formatDate(req.requestedAt)}</td>
      <td><code style="background: rgba(255,255,255,0.1); padding: 0.2rem 0.5rem; border-radius: 4px; color: var(--accent-green);">${req.collectionCode}</code></td>
      <td>${renderStatusBadge(req.statusColor, req.status)}</td>
      <td>
        <div style="display: flex; gap: 0.35rem; align-items: center; flex-wrap: wrap;">
          ${req.status === 'PENDING' ? `
            <button onclick="updateRequestStatus(${req.id}, 'ACCEPTED')" class="btn btn-primary" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">Accept</button>
            <button onclick="updateRequestStatus(${req.id}, 'REJECTED')" class="btn btn-danger" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">Reject</button>
          ` : req.status === 'ACCEPTED' ? `
            <button onclick="openVerifyModalWithCode('${req.collectionCode}')" class="btn btn-ai" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">Verify Pickup</button>
          ` : `
            <span style="font-size: 0.85rem; color: var(--text-muted);">${req.status}</span>
          `}
          <a href="receipt.html?id=${req.id}" class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">Receipt</a>
        </div>
      </td>
    </tr>
  `).join('');
}

function switchDonorTab(tab) {
  const listingsBtn = document.getElementById('tabBtnListings');
  const requestsBtn = document.getElementById('tabBtnRequests');
  const listingsContent = document.getElementById('tabContentListings');
  const requestsContent = document.getElementById('tabContentRequests');

  if (tab === 'listings') {
    listingsBtn.classList.add('active');
    requestsBtn.classList.remove('active');
    listingsContent.style.display = 'block';
    requestsContent.style.display = 'none';
  } else {
    requestsBtn.classList.add('active');
    listingsBtn.classList.remove('active');
    requestsContent.style.display = 'block';
    listingsContent.style.display = 'none';
  }
}

function openCreateDonationModal() {
  document.getElementById('modalDonationTitle').textContent = 'Post Food Donation';
  document.getElementById('editDonationId').value = '';
  document.getElementById('donationForm').reset();

  // Set default expiry date to tomorrow this time
  const defaultExpiry = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().slice(0, 16);
  document.getElementById('foodExpiry').value = defaultExpiry;

  document.getElementById('donationModal').classList.add('active');
}

function closeDonationModal() {
  document.getElementById('donationModal').classList.remove('active');
}

function editDonation(id) {
  const donation = myDonations.find(d => d.id === id);
  if (!donation) return;

  document.getElementById('modalDonationTitle').textContent = 'Edit Food Donation';
  document.getElementById('editDonationId').value = donation.id;
  document.getElementById('foodTitle').value = donation.title;
  document.getElementById('foodCategory').value = donation.category;
  document.getElementById('foodUnit').value = donation.unit;
  document.getElementById('foodInitialQty').value = donation.initialQuantity;
  document.getElementById('foodExpiry').value = new Date(donation.expiryDate).toISOString().slice(0, 16);
  document.getElementById('foodLocation').value = donation.location;
  document.getElementById('foodInstructions').value = donation.pickupInstructions || '';
  document.getElementById('foodImageUrl').value = donation.imageUrl || '';

  document.getElementById('donationModal').classList.add('active');
}

async function handleSaveDonation(e) {
  e.preventDefault();
  const id = document.getElementById('editDonationId').value;
  const payload = {
    title: document.getElementById('foodTitle').value,
    category: document.getElementById('foodCategory').value,
    unit: document.getElementById('foodUnit').value,
    initialQuantity: parseInt(document.getElementById('foodInitialQty').value),
    expiryDate: document.getElementById('foodExpiry').value,
    location: document.getElementById('foodLocation').value,
    pickupInstructions: document.getElementById('foodInstructions').value,
    imageUrl: document.getElementById('foodImageUrl').value
  };

  try {
    if (id) {
      await ApiClient.updateDonation(id, payload);
      showToast('Donation updated successfully!', 'success');
    } else {
      await ApiClient.createDonation(payload);
      showToast('Food donation posted successfully!', 'success');
    }
    closeDonationModal();
    loadDonorData();
  } catch (err) {
    showToast(err.message || 'Failed to save donation', 'error');
  }
}

async function deleteDonation(id) {
  if (!confirm('Are you sure you want to delete/cancel this donation listing?')) return;
  try {
    await ApiClient.deleteDonation(id);
    showToast('Donation deleted', 'info');
    loadDonorData();
  } catch (err) {
    showToast(err.message || 'Failed to delete donation', 'error');
  }
}

async function updateRequestStatus(requestId, status) {
  try {
    await ApiClient.updateRequestStatus(requestId, status);
    showToast(`Request ${status.toLowerCase()}!`, 'success');
    loadDonorData();
  } catch (err) {
    showToast(err.message || 'Failed to update status', 'error');
  }
}

function openVerifyModal() {
  document.getElementById('verifyCodeInput').value = '';
  document.getElementById('verifyModal').classList.add('active');
}

function openVerifyModalWithCode(code) {
  document.getElementById('verifyCodeInput').value = code;
  document.getElementById('verifyModal').classList.add('active');
}

function closeVerifyModal() {
  document.getElementById('verifyModal').classList.remove('active');
}

async function handleVerifyCode(e) {
  e.preventDefault();
  const code = document.getElementById('verifyCodeInput').value.trim();

  try {
    const res = await ApiClient.verifyCollection(code);
    showToast(`Collection Verified! Item marked as COLLECTED for ${res.receiverName}`, 'success');
    closeVerifyModal();
    loadDonorData();
  } catch (err) {
    showToast(err.message || 'Invalid or expired collection code', 'error');
  }
}
