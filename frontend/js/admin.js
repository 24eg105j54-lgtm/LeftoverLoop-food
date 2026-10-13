/* ==========================================================================
   LeftoverLoop Admin Panel JavaScript
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
  AuthManager.requireAuth(['ROLE_ADMIN']);
  loadAdminDashboard();
});

let adminUsers = [];
let adminDonations = [];
let adminRequests = [];

async function loadAdminDashboard() {
  try {
    adminUsers = await ApiClient.getAdminUsers();
    adminDonations = await ApiClient.getAdminDonations();
    adminRequests = await ApiClient.getAdminRequests();

    updateAdminMetrics();
    renderUsersTable();
    renderListingsTable();
  } catch (err) {
    showToast(err.message || 'Failed to load admin data', 'error');
  }
}

function updateAdminMetrics() {
  document.getElementById('adminTotalUsers').textContent = adminUsers.length;
  document.getElementById('adminTotalDonations').textContent = adminDonations.length;
  document.getElementById('adminTotalRequests').textContent = adminRequests.length;
  
  const expired = adminDonations.filter(d => d.status === 'EXPIRED' || d.expired);
  document.getElementById('adminExpiredCount').textContent = expired.length;
}

function renderUsersTable() {
  const tbody = document.getElementById('adminUsersTbody');
  if (!adminUsers || adminUsers.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No users found.</td></tr>';
    return;
  }

  tbody.innerHTML = adminUsers.map(user => `
    <tr>
      <td>${user.id}</td>
      <td><strong>${user.fullName}</strong></td>
      <td>${user.email}</td>
      <td>
        <select onchange="updateRole(${user.id}, this.value)" class="form-control" style="padding: 0.25rem 0.5rem; font-size: 0.85rem;">
          <option value="ROLE_DONOR" ${user.role === 'ROLE_DONOR' ? 'selected' : ''}>DONOR</option>
          <option value="ROLE_RECEIVER" ${user.role === 'ROLE_RECEIVER' ? 'selected' : ''}>RECEIVER</option>
          <option value="ROLE_ORGANIZATION" ${user.role === 'ROLE_ORGANIZATION' ? 'selected' : ''}>ORGANIZATION</option>
          <option value="ROLE_ADMIN" ${user.role === 'ROLE_ADMIN' ? 'selected' : ''}>ADMIN</option>
        </select>
      </td>
      <td>${user.organizationName || 'N/A'}</td>
      <td>${user.active ? renderStatusBadge('GREEN', 'ACTIVE') : renderStatusBadge('RED', 'DEACTIVATED')}</td>
      <td>
        <button onclick="toggleUser(${user.id})" class="btn ${user.active ? 'btn-danger' : 'btn-primary'}" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">
          ${user.active ? 'Deactivate' : 'Activate'}
        </button>
      </td>
    </tr>
  `).join('');
}

function renderListingsTable() {
  const tbody = document.getElementById('adminListingsTbody');
  if (!adminDonations || adminDonations.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No donations found.</td></tr>';
    return;
  }

  tbody.innerHTML = adminDonations.map(d => `
    <tr>
      <td>${d.id}</td>
      <td><strong>${d.title}</strong></td>
      <td>${d.category}</td>
      <td>${d.donorOrganization || d.donorName}</td>
      <td>${d.remainingQuantity} / ${d.initialQuantity} ${d.unit}</td>
      <td>${formatDate(d.expiryDate)}</td>
      <td>${renderStatusBadge(d.statusColor, d.status)}</td>
    </tr>
  `).join('');
}

async function toggleUser(userId) {
  try {
    const updatedUser = await ApiClient.toggleUserStatus(userId);
    showToast(`User account status updated for ${updatedUser.fullName}`, 'success');
    loadAdminDashboard();
  } catch (err) {
    showToast(err.message || 'Failed to update user status', 'error');
  }
}

async function updateRole(userId, newRole) {
  try {
    const updatedUser = await ApiClient.updateUserRole(userId, newRole);
    showToast(`Role updated to ${newRole} for ${updatedUser.fullName}`, 'success');
    loadAdminDashboard();
  } catch (err) {
    showToast(err.message || 'Failed to update user role', 'error');
  }
}
