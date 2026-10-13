/* ==========================================================================
   LeftoverLoop Organization Portal JavaScript
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
  AuthManager.requireAuth(['ROLE_ORGANIZATION', 'ROLE_ADMIN']);
  loadOrganizationData();

  document.getElementById('orgDonationForm').addEventListener('submit', handleOrgDonationSubmit);
});

let orgDonations = [];
let orgRequests = [];

async function loadOrganizationData() {
  try {
    orgDonations = await ApiClient.getOrganizationDonations();
    orgRequests = await ApiClient.getIncomingRequests();
    const stats = await ApiClient.getImpactStats();

    updateOrganizationStats(stats);
    renderOrgListings();
    renderOrgRequests();
  } catch (err) {
    showToast(err.message || 'Failed to load organization data', 'error');
  }
}

function updateOrganizationStats(stats) {
  const available = orgDonations.filter(d => d.status === 'AVAILABLE' || d.status === 'PARTIALLY_RESERVED');
  const pending = orgRequests.filter(r => r.status === 'PENDING');
  const activeDist = orgRequests.filter(r => r.status === 'ACCEPTED' || r.status === 'READY_FOR_COLLECTION');
  const completedDist = orgRequests.filter(r => r.status === 'COLLECTED');
  const remainingQty = orgDonations.reduce((sum, d) => sum + d.remainingQuantity, 0);

  document.getElementById('orgAvailableDonations').textContent = available.length;
  document.getElementById('orgPendingRequests').textContent = pending.length;
  document.getElementById('orgActiveDistributions').textContent = activeDist.length;
  document.getElementById('orgCompletedDistributions').textContent = completedDist.length;

  document.getElementById('orgRemainingFood').textContent = remainingQty;
  document.getElementById('orgPeopleServed').textContent = stats.peopleServed;
  document.getElementById('orgFoodSaved').textContent = `${stats.foodSavedKg} kg`;
}

function renderOrgListings() {
  const grid = document.getElementById('orgListingsGrid');
  if (!orgDonations || orgDonations.length === 0) {
    grid.innerHTML = '<div class="glass-card" style="grid-column: 1/-1; text-align: center; padding: 2rem;"><p style="color: var(--text-muted);">No organization bulk food batches posted yet.</p></div>';
    return;
  }

  const currentUser = AuthManager.getUser();

  grid.innerHTML = orgDonations.map(item => {
    const isOwner = currentUser && (currentUser.id === item.donorId || currentUser.role === 'ROLE_ADMIN');
    const risk = typeof getFoodRiskInfo === 'function' ? getFoodRiskInfo(item) : null;
    const expiryAlert = typeof getExpiryAlertBadge === 'function' ? getExpiryAlertBadge(item.expiryDate) : '';

    const imageHtml = item.imageUrl ? `
      <img src="${item.imageUrl}" class="donation-image" alt="${item.title}" onerror="this.onerror=null; this.style.display='none';">
    ` : `
      <div style="height: 120px; background: rgba(255,255,255,0.03); border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; margin-bottom: 1rem; color: var(--text-dim); font-size: 0.9rem;">
        <span>📦 Bulk Redistribution Batch</span>
      </div>
    `;

    return `
      <div class="glass-card donation-card">
        ${imageHtml}
        <div class="donation-header">
          <h3 class="donation-title">${item.title}</h3>
          ${renderStatusBadge(item.statusColor, item.status)}
        </div>
        <div class="donation-meta">
          <div class="donation-meta-item">📍 ${item.location}</div>
          <div class="donation-meta-item">🕒 Expiry: ${formatDate(item.expiryDate)} ${expiryAlert}</div>
          <div class="donation-meta-item">🏢 Posted by: ${item.donorOrganization || item.donorName}</div>
          ${risk ? `
            <div style="margin-top: 0.5rem;">
              <span class="badge ${risk.badgeClass}" title="${risk.disclaimer}" style="font-size: 0.75rem;">🛡️ ${risk.label}: ${risk.reason}</span>
            </div>
          ` : ''}
        </div>
        <div class="quantity-bar">
          <div class="quantity-fill" style="width: ${(item.remainingQuantity / item.initialQuantity) * 100}%;"></div>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; font-size: 0.9rem; font-weight: 600; margin-bottom: 0.75rem;">
          <span style="color: var(--accent-green);">Remaining: ${item.remainingQuantity} ${item.unit}</span>
          <span style="color: var(--text-muted);">Initial: ${item.initialQuantity}</span>
        </div>
        ${isOwner ? `
          <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: auto; padding-top: 0.5rem; border-top: 1px solid var(--glass-border);">
            <button onclick="editOrgDonation(${item.id})" class="btn btn-secondary" style="padding: 0.35rem 0.85rem; font-size: 0.85rem;">✏️ Edit</button>
          </div>
        ` : ''}
      </div>
    `;
  }).join('');
}

function renderOrgRequests() {
  const tbody = document.getElementById('orgRequestsTbody');
  if (!orgRequests || orgRequests.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No receiver requests received yet.</td></tr>';
    return;
  }

  tbody.innerHTML = orgRequests.map(req => `
    <tr>
      <td><strong>${req.donationTitle}</strong></td>
      <td>
        <div><strong>${req.receiverName}</strong></div>
        <div style="font-size: 0.8rem; color: var(--text-muted);">${req.receiverOrganization || req.receiverEmail}</div>
      </td>
      <td><strong>${req.requestedQuantity} ${req.donationUnit}</strong></td>
      <td>${renderStatusBadge(req.statusColor, req.status)}</td>
      <td><code style="background: rgba(255,255,255,0.1); padding: 0.2rem 0.5rem; border-radius: 4px; color: var(--accent-green);">${req.collectionCode}</code></td>
      <td>
        <div style="display: flex; gap: 0.4rem; align-items: center; flex-wrap: wrap;">
          ${req.status === 'PENDING' ? `
            <button onclick="updateOrgRequestStatus(${req.id}, 'ACCEPTED')" class="btn btn-primary" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">Accept</button>
            <button onclick="updateOrgRequestStatus(${req.id}, 'REJECTED')" class="btn btn-danger" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">Reject</button>
          ` : `
            <span style="font-size: 0.85rem; color: var(--text-muted);">${req.status}</span>
          `}
          <a href="receipt.html?id=${req.id}" class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">Receipt</a>
        </div>
      </td>
    </tr>
  `).join('');
}

function openOrgDonationModal() {
  document.getElementById('orgDonationForm').reset();
  document.getElementById('editOrgDonationId').value = '';
  document.getElementById('modalOrgDonationTitle').textContent = 'Post Bulk Food Donation';
  const saveBtn = document.getElementById('btnSaveOrgDonation');
  if (saveBtn) saveBtn.textContent = 'Post Bulk Batch';

  const defaultExpiry = new Date(Date.now() + 48 * 60 * 60 * 1000).toISOString().slice(0, 16);
  document.getElementById('orgExpiry').value = defaultExpiry;
  document.getElementById('orgDonationModal').classList.add('active');
}

function closeOrgDonationModal() {
  document.getElementById('orgDonationModal').classList.remove('active');
}

function editOrgDonation(id) {
  const donation = orgDonations.find(d => d.id === id);
  if (!donation) return;

  const currentUser = AuthManager.getUser();
  if (currentUser && currentUser.id !== donation.donorId && currentUser.role !== 'ROLE_ADMIN') {
    showToast('You can only edit bulk food donations created by your organization', 'error');
    return;
  }

  document.getElementById('modalOrgDonationTitle').textContent = 'Edit Bulk Food Donation';
  const saveBtn = document.getElementById('btnSaveOrgDonation');
  if (saveBtn) saveBtn.textContent = 'Save Changes';

  document.getElementById('editOrgDonationId').value = donation.id;
  document.getElementById('orgFoodTitle').value = donation.title || '';
  document.getElementById('orgCategory').value = donation.category || 'Fresh Produce';
  document.getElementById('orgUnit').value = donation.unit || '';
  document.getElementById('orgInitialQty').value = donation.initialQuantity || 1;
  document.getElementById('orgExpiry').value = donation.expiryDate ? new Date(donation.expiryDate).toISOString().slice(0, 16) : '';
  document.getElementById('orgLocation').value = donation.location || '';
  document.getElementById('orgInstructions').value = donation.pickupInstructions || '';
  document.getElementById('orgImageUrl').value = donation.imageUrl || '';

  document.getElementById('orgDonationModal').classList.add('active');
}

async function handleOrgDonationSubmit(e) {
  e.preventDefault();
  const editId = document.getElementById('editOrgDonationId').value;
  const imageUrlInput = document.getElementById('orgImageUrl').value.trim();

  const payload = {
    title: document.getElementById('orgFoodTitle').value.trim(),
    category: document.getElementById('orgCategory').value,
    unit: document.getElementById('orgUnit').value.trim(),
    initialQuantity: parseInt(document.getElementById('orgInitialQty').value),
    expiryDate: document.getElementById('orgExpiry').value,
    location: document.getElementById('orgLocation').value.trim(),
    pickupInstructions: document.getElementById('orgInstructions').value.trim(),
    imageUrl: imageUrlInput || null
  };

  try {
    if (editId) {
      await ApiClient.updateDonation(editId, payload);
      showToast('Bulk food batch updated successfully!', 'success');
    } else {
      await ApiClient.createDonation(payload);
      showToast('Bulk food batch posted successfully!', 'success');
    }
    closeOrgDonationModal();
    loadOrganizationData();
  } catch (err) {
    showToast(err.message || 'Failed to save bulk food batch', 'error');
  }
}

async function updateOrgRequestStatus(id, status) {
  try {
    await ApiClient.updateRequestStatus(id, status);
    showToast(`Request ${status.toLowerCase()}!`, 'success');
    loadOrganizationData();
  } catch (err) {
    showToast(err.message || 'Failed to update request', 'error');
  }
}
