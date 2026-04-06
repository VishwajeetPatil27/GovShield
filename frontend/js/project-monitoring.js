// Load all projects
async function loadProjects() {
    const projects = await apiCall('/projects');
    const userRole = localStorage.getItem('userRole');
    const projectsTableBody = document.getElementById('projectsTableBody');
    const projectAnalysisTableBody = document.getElementById('projectAnalysisTableBody');
    
    if (!projects || projects.length === 0) {
        if (projectsTableBody) projectsTableBody.innerHTML = '<tr><td colspan="8">No projects found</td></tr>';
        if (projectAnalysisTableBody) projectAnalysisTableBody.innerHTML = '<tr><td colspan="9">No projects found</td></tr>';
        return;
    }

    // For officer dashboard
    if (projectsTableBody) {
        projectsTableBody.innerHTML = projects.map(project => `
            <tr>
                <td>${project.projectCode}</td>
                <td>${project.projectName}</td>
                <td>${project.category}</td>
                <td><span class="status-badge ${getStatusClass(project.status)}">${project.status}</span></td>
                <td>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${project.progressPercentage}%"></div>
                    </div>
                    ${project.progressPercentage}%
                </td>
                <td>₹${formatCurrency(project.totalBudget)}</td>
                <td><span class="status-badge ${getQualityClass(project.qualityStatus)}">${project.qualityStatus}</span></td>
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-sm" onclick="viewProjectDetails(${project.id})">Details</button>
                        ${(userRole === 'OFFICER' || userRole === 'ADMIN') && (project.status === 'APPROVED' || project.status === 'ONGOING') ? `
                            <button class="btn btn-sm" onclick="releaseFunds(${project.id})">Release</button>
                            <button class="btn btn-sm" onclick="recordExpenditure(${project.id})">Spend</button>
                            <button class="btn btn-sm" onclick="updateProjectProgress(${project.id})">Progress</button>
                        ` : ''}
                        ${getProjectActionButtons(project, userRole)}
                    </div>
                </td>
            </tr>
        `).join('');
    }

    // For auditor dashboard
    if (projectAnalysisTableBody) {
        projectAnalysisTableBody.innerHTML = projects.map(project => {
            const released = Number(project.releasedAmount || 0);
            const spent = Number(project.spentAmount || 0);
            const baseline = released * 0.1;
            const variance = spent - baseline;
            const variancePercent = released > 0 ? ((variance / released) * 100).toFixed(2) : '0.00';
            return `
                <tr>
                    <td>${project.projectCode}</td>
                    <td>${project.projectName}</td>
                    <td>${project.category}</td>
                    <td>₹${formatCurrency(project.totalBudget)}</td>
                    <td>₹${formatCurrency(project.releasedAmount)}</td>
                    <td>₹${formatCurrency(project.spentAmount)}</td>
                    <td>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${project.progressPercentage}%"></div>
                        </div>
                        ${project.progressPercentage}%
                    </td>
                    <td><span class="status-badge ${getQualityClass(project.qualityStatus)}">${project.qualityStatus}</span></td>
                    <td>${variancePercent}%</td>
                </tr>
            `;
        }).join('');
    }

    updateProjectMetrics(projects);
}

// Update project metrics
function updateProjectMetrics(projects) {
    const totalBudget = projects.reduce((sum, p) => sum + parseFloat(p.totalBudget), 0);
    const totalSpent = projects.reduce((sum, p) => sum + parseFloat(p.spentAmount), 0);
    const ongoing = projects.filter(p => p.status === 'ONGOING').length;

    const projectsUnderReview = document.getElementById('projectsUnderReview');
    if (projectsUnderReview) {
        projectsUnderReview.textContent = ongoing;
    }
}

// View project details
async function viewProjectDetails(projectId) {
    const project = await apiCall(`/projects/${projectId}`);
    if (project) {
        alert(`Project: ${project.projectName}\nStatus: ${project.status}\nProgress: ${project.progressPercentage}%\nBudget: ₹${project.totalBudget}`);
    }
}

// Release funds (officer)
async function releaseFunds(projectId) {
    const amount = prompt('Enter amount to release (in rupees):');
    if (!amount || isNaN(amount)) return;

    const result = await apiCall(`/projects/${projectId}/release-funds?amount=${amount}`, 'POST');
    if (result) {
        alert('Funds released successfully');
        loadProjects();
    }
}

// Record expenditure (officer)
async function recordExpenditure(projectId) {
    const amount = prompt('Enter expenditure amount (in rupees):');
    if (!amount || isNaN(amount)) return;

    const result = await apiCall(`/projects/${projectId}/expenditure?amount=${amount}`, 'POST');
    if (result) {
        alert('Expenditure recorded successfully');
        loadProjects();
    }
}

// Update project progress (officer)
async function updateProjectProgress(projectId) {
    const progress = prompt('Enter progress percentage (0-100):');
    if (!progress || isNaN(progress) || progress < 0 || progress > 100) {
        alert('Please enter a valid number between 0 and 100');
        return;
    }

    const result = await apiCall(`/projects/${projectId}/progress?progressPercentage=${progress}`, 'PUT');
    if (result) {
        alert('Project progress updated successfully');
        loadProjects();
    }
}

// Utility functions
function getStatusClass(status) {
    const classMap = {
        'PLANNED': 'pending',
        'ONGOING': 'pending',
        'COMPLETED': 'approved',
        'SUSPENDED': 'rejected',
        'SUBMITTED': 'pending',
        'AUDITOR_APPROVED': 'pending',
        'OFFICER_APPROVED': 'pending',
        'APPROVED': 'approved',
        'REJECTED': 'rejected',
        'AUDITOR_REJECTED': 'rejected',
        'OFFICER_REJECTED': 'rejected'
    };
    return classMap[status] || 'pending';
}

function getProjectActionButtons(project, role) {
    if (project.currentStage === 'AUDITOR_REVIEW' && role === 'AUDITOR') {
        return `
            <button class="btn btn-sm" onclick="reviewProject(${project.id}, true)">Forward</button>
            <button class="btn btn-sm btn-reject" onclick="reviewProject(${project.id}, false)">Reject</button>
        `;
    }
    if (project.currentStage === 'OFFICER_REVIEW' && role === 'OFFICER') {
        return `
            <button class="btn btn-sm" onclick="reviewProject(${project.id}, true)">Forward</button>
            <button class="btn btn-sm btn-reject" onclick="reviewProject(${project.id}, false)">Reject</button>
        `;
    }
    if (project.currentStage === 'ADMIN_REVIEW' && role === 'ADMIN') {
        return `
            <button class="btn btn-sm" onclick="reviewProject(${project.id}, true)">Approve</button>
            <button class="btn btn-sm btn-reject" onclick="reviewProject(${project.id}, false)">Reject</button>
        `;
    }
    return '';
}

async function reviewProject(projectId, approved) {
    const role = localStorage.getItem('userRole');
    const remarks = approved ? '' : (prompt('Enter rejection remarks:') || 'Rejected');
    let endpoint = '';
    if (role === 'AUDITOR') endpoint = `/projects/${projectId}/review/auditor?approved=${approved}&remarks=${encodeURIComponent(remarks)}`;
    if (role === 'OFFICER') endpoint = `/projects/${projectId}/review/officer?approved=${approved}&remarks=${encodeURIComponent(remarks)}`;
    if (role === 'ADMIN') endpoint = `/projects/${projectId}/review/admin?approved=${approved}&remarks=${encodeURIComponent(remarks)}`;
    if (!endpoint) return;

    const result = await apiCall(endpoint, 'POST');
    if (result) {
        alert('Project workflow updated');
        loadProjects();
    }
}

function getQualityClass(quality) {
    const classMap = {
        'GOOD': 'approved',
        'AVERAGE': 'pending',
        'POOR': 'rejected'
    };
    return classMap[quality] || 'pending';
}

function formatCurrency(amount) {
    return parseFloat(amount).toLocaleString('en-IN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    });
}

// -----------------------------
// Public Transparency (Citizen Evidence) + Alerts (Officer)
// -----------------------------

async function loadCitizenProjectsForTransparency() {
    const projects = await apiCall('/projects');
    const body = document.getElementById('citizenProjectsBody');
    if (!body) return;

    if (!projects || projects.length === 0) {
        body.innerHTML = '<tr><td colspan="5">No projects found</td></tr>';
        return;
    }

    body.innerHTML = projects.map(p => `
        <tr>
            <td>${p.projectCode}</td>
            <td>${p.projectName}</td>
            <td><span class="status-badge ${getStatusClass(p.status)}">${p.status}</span></td>
            <td>${p.progressPercentage}%</td>
            <td>
                <button class="btn btn-sm" onclick="submitProjectEvidence(${p.id})">Submit Evidence</button>
            </td>
        </tr>
    `).join('');
}

async function submitProjectEvidence(projectId) {
    const evidenceType = (prompt('Evidence type: PHOTO / COMPLAINT / REVIEW', 'PHOTO') || '').trim().toUpperCase();
    if (!evidenceType) return;
    const message = prompt('Describe what you observed (optional):', '') || '';
    const progressEstimate = prompt('Progress estimate % (optional):', '');
    const contractorRating = prompt('Contractor rating 1-5 (optional):', '');
    const photoBase64 = prompt('Photo (base64, optional):', '') || '';

    const location = await getGeoLocationSafe();

    const payload = {
        ugid: localStorage.getItem('userUgid') || '',
        evidenceType,
        message,
        progressEstimate: progressEstimate === '' ? null : Number(progressEstimate),
        contractorRating: contractorRating === '' ? null : Number(contractorRating),
        photoBase64: photoBase64 || null,
        geoLat: location?.lat ?? null,
        geoLng: location?.lng ?? null
    };

    const result = await apiCall(`/projects/${projectId}/evidence`, 'POST', payload);
    if (result) {
        alert('Evidence submitted. Thank you for helping improve transparency.');
    } else {
        alert('Unable to submit evidence right now.');
    }
}

function getGeoLocationSafe() {
    return new Promise(resolve => {
        if (!navigator.geolocation) return resolve(null);
        navigator.geolocation.getCurrentPosition(
            pos => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
            () => resolve(null),
            { enableHighAccuracy: false, timeout: 4000, maximumAge: 300000 }
        );
    });
}

async function loadProjectAlerts() {
    const alerts = await apiCall('/projects/alerts');
    const body = document.getElementById('projectAlertsTableBody');
    if (!body) return;

    if (!alerts || alerts.length === 0) {
        body.innerHTML = '<tr><td colspan="6">No active alerts</td></tr>';
        bindProjectAlertSearch();
        return;
    }

    body.innerHTML = alerts.map(a => `
        <tr>
            <td>${a.id}</td>
            <td>${a.project?.projectCode || '-'} / ${a.project?.projectName || '-'}</td>
            <td><span class="risk-badge ${safeClass(a.severity)}">${a.severity}</span></td>
            <td>${escapeHtml(a.reason || '')}</td>
            <td>${a.createdAt ? new Date(a.createdAt).toLocaleString() : '-'}</td>
            <td>
                <button class="btn btn-sm" onclick="resolveProjectAlert(${a.id})">Resolve</button>
            </td>
        </tr>
    `).join('');

    bindProjectAlertSearch();
}

function bindProjectAlertSearch() {
    const input = document.getElementById('projectAlertSearch');
    if (!input || input.dataset.bound) return;
    input.addEventListener('input', () => {
        const q = input.value.toLowerCase();
        document.querySelectorAll('#projectAlertsTableBody tr').forEach(tr => {
            tr.style.display = tr.textContent.toLowerCase().includes(q) ? '' : 'none';
        });
    });
    input.dataset.bound = 'true';
}

async function resolveProjectAlert(alertId) {
    const remarks = prompt('Resolution remarks (optional):', '') || '';
    const result = await apiCall(`/projects/alerts/${alertId}/resolve`, 'POST', { remarks });
    if (result) {
        alert('Alert resolved');
        loadProjectAlerts();
    }
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
