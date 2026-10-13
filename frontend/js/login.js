/* ==========================================================================
   LeftoverLoop Login JavaScript Handler
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
  if (AuthManager.isAuthenticated()) {
    const user = AuthManager.getUser();
    AuthManager.redirectToUserDashboard(user.role);
    return;
  }

  const form = document.getElementById('loginForm');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    await processLogin(email, password);
  });
});

async function quickLogin(email, password) {
  document.getElementById('loginEmail').value = email;
  document.getElementById('loginPassword').value = password;
  await processLogin(email, password);
}

async function processLogin(email, password) {
  const submitBtn = document.getElementById('btnLoginSubmit');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Signing in...';

  try {
    const res = await ApiClient.login({ email, password });
    ApiClient.setToken(res.token);
    AuthManager.setUser(res.user);
    showToast(`Welcome back, ${res.user.fullName}!`, 'success');

    setTimeout(() => {
      AuthManager.redirectToUserDashboard(res.user.role);
    }, 800);
  } catch (err) {
    showToast(err.message || 'Login failed', 'error');
    submitBtn.disabled = false;
    submitBtn.textContent = 'Sign In';
  }
}
