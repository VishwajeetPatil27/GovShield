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

async function loadProjectEvidenceReview() {
    const role = (localStorage.getItem('userRole') || '').toUpperCase();
    const body = document.getElementById('projectEvidenceTableBody');
    if (!body) return;

    if (role !== 'AUDITOR' && role !== 'ADMIN') {
        body.innerHTML = '<tr><td colspan="7">Access restricted to auditor and admin roles.</td></tr>';
        return;
    }

    const evidence = await loadAllProjectEvidence();
    if (!evidence || evidence.length === 0) {
        body.innerHTML = '<tr><td colspan="7">No public evidence submitted yet</td></tr>';
        return;
    }

    body.innerHTML = evidence.map(item => {
        const projectLabel = `${item.project?.projectCode || '-'} / ${item.project?.projectName || '-'}`;
        const citizenLabel = item.citizen?.ugid || 'Anonymous';
        const photoAction = item.photoBase64
            ? `<button class="btn btn-sm" onclick="previewEvidencePhoto('${escapeForJs(item.fileName || 'evidence.png')}', '${escapeForJs(item.photoBase64)}')">Preview</button>`
            : '-';
        const geo = item.geoLat != null && item.geoLng != null ? `${item.geoLat}, ${item.geoLng}` : '-';
        return `
            <tr>
                <td>${item.createdAt ? new Date(item.createdAt).toLocaleString() : '-'}</td>
                <td>${escapeHtml(projectLabel)}</td>
                <td>${escapeHtml(citizenLabel)}</td>
                <td><span class="status-badge ${safeClass(item.evidenceType)}">${escapeHtml(item.evidenceType || '-')}</span></td>
                <td>${escapeHtml(item.message || '-')}</td>
                <td>${photoAction}</td>
                <td>${escapeHtml(geo)}</td>
            </tr>
        `;
    }).join('');
}

async function loadAllProjectEvidence() {
    const bulkEvidence = await apiCall('/projects/evidence/all');
    if (Array.isArray(bulkEvidence)) {
        return bulkEvidence;
    }

    const projects = await apiCall('/projects');
    if (!Array.isArray(projects) || projects.length === 0) {
        return [];
    }

    const evidenceLists = await Promise.all(
        projects.map(async project => {
            const list = await apiCall(`/projects/${project.id}/evidence`);
            return Array.isArray(list) ? list : [];
        })
    );

    return evidenceLists
        .flat()
        .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
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
        'SATISFACTORY': 'pending',
        'AVERAGE': 'pending',
        'POOR': 'rejected'
    };
    return classMap[quality] || 'pending';
}

function formatCurrency(amount) {
    const value = Number(amount || 0);
    return value.toLocaleString('en-IN', {
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
    populateEvidenceProjectSelect(projects || []);
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

function populateEvidenceProjectSelect(projects) {
    const select = document.getElementById('citizenEvidenceProject');
    if (!select) return;

    const current = select.value;
    select.innerHTML = projects.length === 0
        ? '<option value="">No projects available</option>'
        : projects.map(project => `
            <option value="${project.id}">${escapeHtml(project.projectCode)} - ${escapeHtml(project.projectName)}</option>
        `).join('');

    if (current && projects.some(project => String(project.id) === String(current))) {
        select.value = current;
    }
}

async function submitProjectEvidence(projectId = null) {
    const selectedProjectId = projectId || document.getElementById('citizenEvidenceProject')?.value;
    if (!selectedProjectId) {
        alert('Please choose a project before submitting evidence.');
        return;
    }
    const evidenceType = (document.getElementById('citizenEvidenceType')?.value || 'PHOTO').trim().toUpperCase();
    const message = document.getElementById('citizenEvidenceMessage')?.value || '';
    const progressEstimate = document.getElementById('citizenEvidenceProgress')?.value || '';
    const contractorRating = document.getElementById('citizenEvidenceRating')?.value || '';
    const photoInput = document.getElementById('citizenEvidencePhoto');
    const photoFile = photoInput?.files?.[0] || null;

    if (!evidenceType) return;
    if (evidenceType === 'PHOTO' && !photoFile) {
        alert('Please select a photo to submit a progress update.');
        return;
    }

    const location = await getGeoLocationSafe();
    const photoBase64 = photoFile ? await fileToBase64(photoFile) : null;

    const payload = {
        ugid: localStorage.getItem('userUgid') || '',
        evidenceType,
        message,
        progressEstimate: progressEstimate === '' ? null : Number(progressEstimate),
        contractorRating: contractorRating === '' ? null : Number(contractorRating),
        photoBase64,
        geoLat: location?.lat ?? null,
        geoLng: location?.lng ?? null
    };

    const result = await apiCall(`/projects/${selectedProjectId}/evidence`, 'POST', payload);
    if (result) {
        alert('Evidence submitted. Thank you for helping improve transparency.');
        document.getElementById('citizenEvidenceMessage') && (document.getElementById('citizenEvidenceMessage').value = '');
        document.getElementById('citizenEvidenceProgress') && (document.getElementById('citizenEvidenceProgress').value = '');
        document.getElementById('citizenEvidenceRating') && (document.getElementById('citizenEvidenceRating').value = '');
        if (photoInput) photoInput.value = '';
    } else {
        alert('Unable to submit evidence right now.');
    }
}

function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
            const result = String(reader.result || '');
            resolve(result.split(',').pop() || '');
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
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

function escapeForJs(value) {
    return String(value ?? '')
        .replace(/\\/g, '\\\\')
        .replace(/'/g, "\\'")
        .replace(/\r/g, '\\r')
        .replace(/\n/g, '\\n');
}

function previewEvidencePhoto(fileName, base64) {
    const mime = mimeFromFileName(fileName);
    const win = window.open('', '_blank', 'noopener,noreferrer');
    if (!win) {
        alert('Popup blocked. Please allow popups to preview the uploaded photo.');
        return;
    }

    const src = `data:${mime};base64,${base64}`;
    win.document.write(`
        <html>
            <head>
                <title>Evidence Preview</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background: #f5f7fb; }
                    .wrap { max-width: 1000px; margin: 0 auto; }
                    img, iframe { width: 100%; height: 80vh; object-fit: contain; border: 0; background: white; }
                    .meta { margin-bottom: 12px; color: #334155; }
                </style>
            </head>
            <body>
                <div class="wrap">
                    <div class="meta">${escapeHtml(fileName)}</div>
                    ${mime.startsWith('image/') ? `<img src="${src}" alt="Evidence preview">` : `<iframe src="${src}"></iframe>`}
                </div>
            </body>
        </html>
    `);
    win.document.close();
}

function mimeFromFileName(fileName) {
    const value = String(fileName || '').toLowerCase();
    if (value.endsWith('.png')) return 'image/png';
    if (value.endsWith('.jpg') || value.endsWith('.jpeg')) return 'image/jpeg';
    if (value.endsWith('.webp')) return 'image/webp';
    if (value.endsWith('.pdf')) return 'application/pdf';
    return 'application/octet-stream';
}
