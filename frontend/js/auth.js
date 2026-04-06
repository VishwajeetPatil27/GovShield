const API_BASE_URL =
    (window.GOVSHIELD && window.GOVSHIELD.API_BASE_URL)
        ? window.GOVSHIELD.API_BASE_URL
        : 'http://localhost:8080/api';
let currentUser = null;
let currentToken = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();
    setupNavigation();
});

// Setup navigation for dashboard
function setupNavigation() {
    const navLinks = document.querySelectorAll('.sidebar-nav a');
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            if (this.getAttribute('href') !== '#' && !this.getAttribute('href').includes('logout')) {
                e.preventDefault();
                const section = this.getAttribute('href').substring(1);
                showSection(section);
            }
        });
    });
}

function showSection(sectionId) {
    const sections = document.querySelectorAll('.content-section');
    sections.forEach(section => section.style.display = 'none');
    
    const activeSection = document.getElementById(sectionId);
    if (activeSection) {
        activeSection.style.display = 'block';
        
        // Load data based on section
        if (sectionId === 'enrollments') {
            if (typeof loadEnrollments === 'function') loadEnrollments();
        } else if (sectionId === 'applications') {
            if (typeof loadEnrollments === 'function') loadEnrollments();
        } else if (sectionId === 'schemes') {
            if (typeof loadSchemes === 'function') loadSchemes();
        } else if (sectionId === 'eligibility-checker') {
            if (typeof initRealtimeEligibilityForm === 'function') initRealtimeEligibilityForm();
        } else if (sectionId === 'transparency') {
            if (typeof loadCitizenProjectsForTransparency === 'function') loadCitizenProjectsForTransparency();
        } else if (sectionId === 'citizens') {
            if (typeof loadCitizens === 'function') loadCitizens();
        } else if (sectionId === 'fraud') {
            if (typeof loadFraudAlerts === 'function') loadFraudAlerts();
        } else if (sectionId === 'projects') {
            if (typeof loadProjects === 'function') loadProjects();
        } else if (sectionId === 'transparency-alerts') {
            if (typeof loadProjectAlerts === 'function') loadProjectAlerts();
        } else if (sectionId === 'audits') {
            if (typeof loadAuditLogs === 'function') loadAuditLogs();
        } else if (sectionId === 'profile') {
            if (typeof loadCitizenProfile === 'function') loadCitizenProfile();
            if (typeof loadCeps === 'function') loadCeps(false);
        } else if (sectionId === 'dashboard') {
            if (localStorage.getItem('userRole') === 'CITIZEN') {
                if (typeof loadCitizenProfile === 'function') loadCitizenProfile();
                if (typeof loadCeps === 'function') loadCeps(false);
                if (typeof loadEnrollments === 'function') loadEnrollments();
                if (typeof loadSchemes === 'function') loadSchemes();
                if (typeof loadCitizenProjectsForTransparency === 'function') loadCitizenProjectsForTransparency();
            }
        }
    }
}

// Check authentication status
function checkAuthStatus() {
    const token = localStorage.getItem('authToken');
    const userRole = localStorage.getItem('userRole');
    
    if (!token) {
        // Redirect to login if not already there
        if (!window.location.pathname.includes('login')) {
            window.location.href = 'login.html';
        }
    } else {
        currentToken = token;
        const userEmail = localStorage.getItem('userEmail');
        const userRole = localStorage.getItem('userRole');
        const userUgid = localStorage.getItem('userUgid');
        if (document.getElementById('userName')) {
            document.getElementById('userName').textContent = `Welcome, ${userEmail}!`;
        }
        const heading = document.querySelector('.top-bar h1');
        if (heading && userRole === 'ADMIN') heading.textContent = 'Admin Dashboard';
        if (heading && userRole === 'OFFICER') heading.textContent = 'Officer Dashboard';
        if (heading && userRole === 'AUDITOR') heading.textContent = 'Audit Dashboard';
        if (heading && userRole === 'CITIZEN') heading.textContent = 'Citizen Dashboard';
        if (userUgid && document.getElementById('ugidDisplay')) {
            document.getElementById('ugidDisplay').textContent = userUgid;
        }
        if (userUgid && document.getElementById('profileUgid')) {
            document.getElementById('profileUgid').textContent = userUgid;
        }
        
        // Redirect to appropriate dashboard based on role
        const currentPage = window.location.pathname.split('/').pop();
        if (currentPage === 'login.html') {
            window.location.href = roleToPage(userRole);
        }
    }
}

// Convert role to dashboard page
function roleToPage(role) {
    switch(role) {
        case 'ADMIN':
        case 'OFFICER':
            return 'officer-dashboard.html';
        case 'AUDITOR':
            return 'audit-dashboard.html';
        default:
            return 'citizen-dashboard.html';
    }
}

// Login function
async function login(email, password, loginType = 'employee', aadhaar = null, ugid = null) {
    try {
        const endpoint = loginType === 'citizen' ? '/auth/citizen-login' : '/auth/login';
        const body = loginType === 'citizen'
            ? { aadhaar, ugid }
            : { email, password };

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            let errorMessage = 'Login failed';
            try {
                const errorData = await response.json();
                if (errorData && (errorData.message || errorData.error)) {
                    errorMessage = errorData.message || errorData.error;
                }
            } catch (e) {
                // Keep generic message when response is not JSON
            }
            throw new Error(errorMessage);
        }

        const data = await response.json();
        
        // Store authentication data
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('userEmail', data.email || aadhaar || '');
        localStorage.setItem('userRole', data.role);
        localStorage.setItem('userId', data.userId);
        if (data.ugid) {
            localStorage.setItem('userUgid', data.ugid);
        }

        // Redirect to appropriate dashboard
        window.location.href = roleToPage(data.role);
    } catch (error) {
        console.error('Login error:', error);
        const errorDiv = document.getElementById('loginError');
        if (errorDiv) {
            errorDiv.textContent = error.message || 'Login failed. Please try again.';
            errorDiv.style.display = 'block';
        }
    }
}

// Logout function
function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userId');
    localStorage.removeItem('userUgid');
    window.location.href = 'login.html';
}

// Generic API call function
async function apiCall(endpoint, method = 'GET', data = null) {
    const token = localStorage.getItem('authToken');
    const role = localStorage.getItem('userRole');
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            'X-User-Role': role || ''
        }
    };

    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        
        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return null;
            }
            throw new Error(`API Error: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API Call Error:', error);
        return null;
    }
}

// Setup login form
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const loginType = document.querySelector('input[name="loginType"]:checked')?.value || 'employee';

        if (loginType === 'citizen') {
            const aadhaar = document.getElementById('aadhaar').value.replace(/\s+/g, '').trim();
            const ugid = document.getElementById('ugid').value.trim();
            if (!aadhaar || !ugid) {
                const errorDiv = document.getElementById('loginError');
                if (errorDiv) {
                    errorDiv.textContent = 'Citizen login requires Aadhaar and UGID';
                    errorDiv.style.display = 'block';
                }
                return;
            }
            login(null, null, 'citizen', aadhaar, ugid);
            return;
        }

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        login(email, password, 'employee');
    });
}

document.addEventListener('DOMContentLoaded', function() {
    const typeRadios = document.querySelectorAll('input[name="loginType"]');
    const employeeFields = document.getElementById('employeeLoginFields');
    const citizenFields = document.getElementById('citizenLoginFields');

    typeRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            const isCitizen = this.value === 'citizen';
            if (employeeFields) employeeFields.style.display = isCitizen ? 'none' : 'block';
            if (citizenFields) citizenFields.style.display = isCitizen ? 'block' : 'none';
            const email = document.getElementById('email');
            const password = document.getElementById('password');
            const aadhaar = document.getElementById('aadhaar');
            const ugid = document.getElementById('ugid');
            if (email) email.required = !isCitizen;
            if (password) password.required = !isCitizen;
            if (aadhaar) aadhaar.required = isCitizen;
            if (ugid) ugid.required = isCitizen;
        });
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const role = localStorage.getItem('userRole');
    if (!role || !localStorage.getItem('authToken')) return;
    if (role === 'CITIZEN') {
        if (typeof loadCitizenProfile === 'function') loadCitizenProfile();
        if (typeof loadEnrollments === 'function') loadEnrollments();
        if (typeof loadSchemes === 'function') loadSchemes();
    } else if (role === 'ADMIN' || role === 'OFFICER') {
        if (typeof loadEnrollments === 'function') loadEnrollments();
        if (typeof loadFraudAlerts === 'function') loadFraudAlerts();
        if (typeof loadProjects === 'function') loadProjects();
        if (typeof loadCitizens === 'function') loadCitizens();
        if (typeof loadAuditLogs === 'function') loadAuditLogs();
    } else {
        if (typeof loadFraudAlerts === 'function') loadFraudAlerts();
        if (typeof loadProjects === 'function') loadProjects();
        if (typeof loadAuditLogs === 'function') loadAuditLogs();
    }

    setInterval(() => {
        const activeRole = localStorage.getItem('userRole');
        if (!activeRole || !localStorage.getItem('authToken')) return;
        if (activeRole === 'CITIZEN') {
            if (typeof loadEnrollments === 'function') loadEnrollments();
        } else if (activeRole === 'ADMIN' || activeRole === 'OFFICER') {
            if (typeof loadEnrollments === 'function') loadEnrollments();
            if (typeof loadFraudAlerts === 'function') loadFraudAlerts();
            if (typeof loadProjects === 'function') loadProjects();
            if (typeof loadCitizens === 'function') loadCitizens();
            if (typeof loadAuditLogs === 'function') loadAuditLogs();
        } else {
            if (typeof loadFraudAlerts === 'function') loadFraudAlerts();
            if (typeof loadProjects === 'function') loadProjects();
            if (typeof loadAuditLogs === 'function') loadAuditLogs();
        }
    }, 15000);
});
