// Load all enrollments
async function loadEnrollments() {
    const userRole = localStorage.getItem('userRole');
    const userId = localStorage.getItem('userId');

    let endpoint = '/eligibility/citizen/' + userId;
    if (userRole === 'ADMIN' || userRole === 'OFFICER' || userRole === 'AUDITOR') {
        endpoint = '/eligibility/all';
    }
    const enrollments = await apiCall(endpoint);

    const applicationsTableBody = document.getElementById('applicationsTableBody');
    const enrollmentsTableBody = document.getElementById('enrollmentsTableBody');

    if (!enrollments || enrollments.length === 0) {
        if (applicationsTableBody) applicationsTableBody.innerHTML = '<tr><td colspan="6">No applications</td></tr>';
        if (enrollmentsTableBody) enrollmentsTableBody.innerHTML = '<tr><td colspan="7">No enrollments</td></tr>';
        updateCitizenApplicationMetrics([]);
        return;
    }

    if (applicationsTableBody) {
        applicationsTableBody.innerHTML = enrollments.map(enrollment => `
            <tr>
                <td>${enrollment.enrollmentNumber}</td>
                <td>${enrollment.scheme.schemeName}</td>
                <td><span class="status-badge ${safeClass(enrollment.status)}">${enrollment.status}</span></td>
                <td><span class="status-badge ${safeClass(enrollment.eligibilityStatus)}">${enrollment.eligibilityStatus}</span></td>
                <td><span class="risk-badge ${safeClass(enrollment.fraudRiskLevel)}">${enrollment.fraudRiskLevel}</span></td>
                <td>${new Date(enrollment.applicationDate).toLocaleDateString()}</td>
            </tr>
        `).join('');
        setupCitizenApplicationSearch();
    }

    if (enrollmentsTableBody) {
        enrollmentsTableBody.innerHTML = enrollments.map(enrollment => `
            <tr>
                <td>${enrollment.enrollmentNumber}</td>
                <td>${enrollment.citizen.ugid}</td>
                <td>${enrollment.scheme.schemeName}</td>
                <td><span class="status-badge ${safeClass(enrollment.status)}">${enrollment.status}</span></td>
                <td><span class="status-badge ${safeClass(enrollment.eligibilityStatus)}">${enrollment.eligibilityStatus}</span></td>
                <td><span class="risk-badge ${safeClass(enrollment.fraudRiskLevel)}">${enrollment.fraudRiskLevel}</span></td>
                <td>
                    <div class="action-buttons">
                        ${getEnrollmentActionButtons(enrollment, userRole)}
                    </div>
                </td>
            </tr>
        `).join('');
    }

    updateEligibilityMetrics(enrollments);
    updateCitizenApplicationMetrics(enrollments);
    setupEnrollmentSearchFilter();
}

async function approveEnrollment(enrollmentId) {
    const userRole = localStorage.getItem('userRole');
    if (!confirm('Proceed with approval action?')) return;

    let endpoint = `/eligibility/review/admin/${enrollmentId}?approved=true`;
    if (userRole === 'AUDITOR') endpoint = `/eligibility/review/auditor/${enrollmentId}?approved=true`;
    if (userRole === 'OFFICER') endpoint = `/eligibility/review/officer/${enrollmentId}?approved=true`;

    const result = await apiCall(endpoint, 'POST');
    if (result) {
        loadEnrollments();
        if (typeof loadFraudAlerts === 'function') loadFraudAlerts();
    }
}

async function rejectEnrollment(enrollmentId) {
    const userRole = localStorage.getItem('userRole');
    const reason = prompt('Enter rejection reason:');
    if (!reason) return;

    let endpoint = `/eligibility/review/admin/${enrollmentId}?approved=false&remarks=${encodeURIComponent(reason)}`;
    if (userRole === 'AUDITOR') endpoint = `/eligibility/review/auditor/${enrollmentId}?approved=false&remarks=${encodeURIComponent(reason)}`;
    if (userRole === 'OFFICER') endpoint = `/eligibility/review/officer/${enrollmentId}?approved=false&remarks=${encodeURIComponent(reason)}`;

    const result = await apiCall(endpoint, 'POST');
    if (result) {
        loadEnrollments();
        if (typeof loadFraudAlerts === 'function') loadFraudAlerts();
    }
}

async function checkEligibility(ugid, schemeId) {
    return await apiCall(`/eligibility/check?ugid=${encodeURIComponent(ugid)}&schemeId=${schemeId}`, 'POST');
}

function updateEligibilityMetrics(enrollments) {
    const pendingStatuses = new Set(['SUBMITTED', 'AUDITOR_APPROVED', 'OFFICER_APPROVED', 'FLAGGED_FOR_AUDIT']);
    const pending = enrollments.filter(e => {
        if (e.currentStage) {
            return e.currentStage !== 'CLOSED';
        }
        return pendingStatuses.has((e.status || '').toUpperCase());
    }).length;

    const flagged = enrollments.filter(e => {
        const status = (e.status || '').toUpperCase();
        const eligibility = (e.eligibilityStatus || '').toUpperCase();
        return status.includes('FLAGGED') || eligibility === 'FLAGGED';
    }).length;
    const approvedToday = enrollments.filter(e => {
        if (!e.approvalDate) return false;
        const approved = new Date(e.approvalDate);
        const now = new Date();
        return approved.getFullYear() === now.getFullYear() &&
            approved.getMonth() === now.getMonth() &&
            approved.getDate() === now.getDate();
    }).length;
    const auditedRecords = enrollments.filter(e => e.auditorDecision === 'APPROVED' || e.auditorDecision === 'REJECTED').length;
    const issuesReported = enrollments.filter(e =>
        e.status === 'REJECTED' ||
        e.status === 'AUDITOR_REJECTED' ||
        e.status === 'OFFICER_REJECTED' ||
        e.status === 'FLAGGED' ||
        e.status === 'FLAGGED_FOR_AUDIT'
    ).length;

    const pendingElement = document.getElementById('pendingApplications');
    const flaggedElement = document.getElementById('flaggedApplications');
    const approvedTodayElement = document.getElementById('approvedToday');
    const auditedElement = document.getElementById('auditedRecords');
    const issuesElement = document.getElementById('issuesReported');

    if (pendingElement) pendingElement.textContent = pending;
    if (flaggedElement) flaggedElement.textContent = flagged;
    if (approvedTodayElement) approvedTodayElement.textContent = approvedToday;
    if (auditedElement) auditedElement.textContent = auditedRecords;
    if (issuesElement) issuesElement.textContent = issuesReported;
}

function updateCitizenApplicationMetrics(enrollments) {
    const total = enrollments.length;
    const approved = enrollments.filter(e => e.status === 'APPROVED').length;
    const rejected = enrollments.filter(e =>
        e.status === 'REJECTED' || e.status === 'AUDITOR_REJECTED' || e.status === 'OFFICER_REJECTED'
    ).length;

    setText('totalApplications', total);
    setText('approvedCount', approved);
    setText('rejectedCount', rejected);
}

function getEnrollmentActionButtons(enrollment, userRole) {
    if (enrollment.currentStage === 'AUDITOR_REVIEW' && userRole === 'AUDITOR') {
        return `
            <button class="btn btn-approve btn-sm" onclick="approveEnrollment(${enrollment.id})">Forward</button>
            <button class="btn btn-reject btn-sm" onclick="rejectEnrollment(${enrollment.id})">Reject</button>
        `;
    }
    if ((enrollment.currentStage === 'OFFICER_REVIEW' || enrollment.currentStage === 'AUDITOR_REVIEW') && userRole === 'OFFICER') {
        return `
            <button class="btn btn-approve btn-sm" onclick="approveEnrollment(${enrollment.id})">Forward</button>
            <button class="btn btn-reject btn-sm" onclick="rejectEnrollment(${enrollment.id})">Reject</button>
        `;
    }
    if (enrollment.currentStage === 'ADMIN_REVIEW' && userRole === 'ADMIN') {
        return `
            <button class="btn btn-approve btn-sm" onclick="approveEnrollment(${enrollment.id})">Approve</button>
            <button class="btn btn-reject btn-sm" onclick="rejectEnrollment(${enrollment.id})">Reject</button>
        `;
    }
    return '<span class="status-badge pending">No Action</span>';
}

function setupEnrollmentSearchFilter() {
    const searchInput = document.getElementById('enrollmentSearch');
    const statusFilter = document.getElementById('statusFilter');

    const applyFilter = () => {
        const rows = document.querySelectorAll('#enrollmentsTableBody tr');
        const q = (searchInput?.value || '').toLowerCase();
        const status = (statusFilter?.value || '').toLowerCase();
        rows.forEach(row => {
            const rowText = row.textContent.toLowerCase();
            const rowStatus = row.children[3]?.textContent?.toLowerCase() || '';
            const bySearch = !q || rowText.includes(q);
            const byStatus = !status || rowStatus.includes(status);
            row.style.display = bySearch && byStatus ? '' : 'none';
        });
    };

    if (searchInput && !searchInput.dataset.bound) {
        searchInput.addEventListener('input', applyFilter);
        searchInput.dataset.bound = 'true';
    }
    if (statusFilter && !statusFilter.dataset.bound) {
        statusFilter.addEventListener('change', applyFilter);
        statusFilter.dataset.bound = 'true';
    }
}

function setupCitizenApplicationSearch() {
    const searchInput = document.getElementById('citizenAppSearch');
    if (!searchInput || searchInput.dataset.bound) return;
    searchInput.addEventListener('input', function() {
        const q = this.value.toLowerCase();
        document.querySelectorAll('#applicationsTableBody tr').forEach(row => {
            row.style.display = row.textContent.toLowerCase().includes(q) ? '' : 'none';
        });
    });
    searchInput.dataset.bound = 'true';
}

async function loadCitizenProfile() {
    const userRole = localStorage.getItem('userRole');
    if (userRole !== 'CITIZEN') return;
    const citizenId = localStorage.getItem('userId');
    if (!citizenId) return;

    const citizen = await apiCall(`/citizens/${citizenId}`);
    if (!citizen) return;

    const fullName = `${citizen.firstName || ''} ${citizen.lastName || ''}`.trim();
    setText('userName', fullName ? `Welcome, ${fullName}!` : 'Welcome!');

    setText('profileName', fullName || '-');
    setText('profileEmail', citizen.email || '-');
    setText('profileAadhaar', citizen.aadhaar || '-');
    setText('profileUgid', citizen.ugid || '-');
    setText('profilePhone', citizen.phoneNumber || '-');
    setText('profileDob', citizen.dateOfBirth || '-');
    setText('profileGender', citizen.gender || '-');
    setText('profileLocation', `${citizen.state || '-'} / ${citizen.district || '-'}`);
    setText('profileEmployment', citizen.employmentStatus || '-');
    setText('profileIncome', citizen.annualIncome != null ? `₹${citizen.annualIncome}` : '-');
    setText('profileAddress', citizen.address || '-');
    setText('profileVerificationStatus', citizen.verificationStatus || 'PENDING');
    setText('ugidDisplay', citizen.ugid || '-');
    loadCitizenDocuments(citizen.id);
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function safeClass(value) {
    return (value || '').toLowerCase().replace(/[^a-z0-9_-]/g, '');
}

async function loadCitizenDocuments(citizenId) {
    const docsBody = document.getElementById('citizenDocumentsBody');
    if (!docsBody) return;

    const docs = await apiCall(`/citizen-documents/citizen/${citizenId}`);
    if (!docs || docs.length === 0) {
        docsBody.innerHTML = '<tr><td colspan="5">No documents uploaded</td></tr>';
        return;
    }

    docsBody.innerHTML = docs.map(doc => `
        <tr>
            <td>${doc.documentType}</td>
            <td>${doc.documentNumber || '-'}</td>
            <td>${doc.fileName}</td>
            <td><span class="status-badge ${safeClass(doc.verificationStatus)}">${doc.verificationStatus}</span></td>
            <td>${doc.verificationRemarks || '-'}</td>
        </tr>
    `).join('');
}

function generateCitizenCard() {
    const preview = document.getElementById('citizenCardPreview');
    if (!preview) return;

    const name = document.getElementById('profileName')?.textContent || '-';
    const ugid = document.getElementById('profileUgid')?.textContent || '-';
    const aadhaar = document.getElementById('profileAadhaar')?.textContent || '-';
    const location = document.getElementById('profileLocation')?.textContent || '-';
    const status = document.getElementById('profileVerificationStatus')?.textContent || '-';

    preview.style.display = 'block';
    preview.innerHTML = `
        <h3>GovShield Beneficiary Card</h3>
        <div class="id-grid">
            <div><strong>Name:</strong> ${name}</div>
            <div><strong>UGID:</strong> ${ugid}</div>
            <div><strong>Aadhaar:</strong> ${aadhaar}</div>
            <div><strong>Location:</strong> ${location}</div>
            <div><strong>Verification:</strong> ${status}</div>
            <div><strong>Issued:</strong> ${new Date().toLocaleDateString()}</div>
        </div>
    `;

    const downloadBtn = document.getElementById('downloadCitizenCardBtn');
    if (downloadBtn) {
        downloadBtn.style.display = 'inline-block';
        downloadBtn.onclick = () => downloadCitizenCardPdf({ name, ugid, aadhaar, location, status });
    }
}

function downloadCitizenCardPdf(card) {
    const lines = [
        'GovShield Beneficiary Card',
        `Name: ${card.name}`,
        `UGID: ${card.ugid}`,
        `Aadhaar: ${card.aadhaar}`,
        `Location: ${card.location}`,
        `Verification: ${card.status}`,
        `Issued: ${new Date().toLocaleDateString()}`
    ];
    const blob = createSimplePdfBlob(lines);
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `${card.ugid || 'beneficiary'}-card.pdf`;
    link.click();
    URL.revokeObjectURL(link.href);
}

function createSimplePdfBlob(lines) {
    const textLines = lines.map((line, index) =>
        `BT /F1 12 Tf 50 ${780 - (index * 20)} Td (${escapePdfText(line)}) Tj ET`
    ).join('\n');
    const stream = `${textLines}\n`;

    const objects = [];
    objects.push('1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n');
    objects.push('2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n');
    objects.push('3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 5 0 R /Resources << /Font << /F1 4 0 R >> >> >>\nendobj\n');
    objects.push('4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n');
    objects.push(`5 0 obj\n<< /Length ${stream.length} >>\nstream\n${stream}endstream\nendobj\n`);

    let pdf = '%PDF-1.4\n';
    const offsets = [0];
    for (const object of objects) {
        offsets.push(pdf.length);
        pdf += object;
    }

    const xrefStart = pdf.length;
    pdf += `xref\n0 ${objects.length + 1}\n`;
    pdf += '0000000000 65535 f \n';
    for (let i = 1; i <= objects.length; i++) {
        pdf += `${String(offsets[i]).padStart(10, '0')} 00000 n \n`;
    }
    pdf += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefStart}\n%%EOF`;
    return new Blob([pdf], { type: 'application/pdf' });
}

function escapePdfText(value) {
    return String(value || '')
        .replace(/\\/g, '\\\\')
        .replace(/\(/g, '\\(')
        .replace(/\)/g, '\\)');
}

// -----------------------------
// CEPS (Citizen Economic Profile Score)
// -----------------------------

function getUserUgid() {
    return localStorage.getItem('userUgid') || '';
}

async function loadCeps(recalculate = false) {
    const ugid = getUserUgid();
    if (!ugid) return;

    const chip = document.getElementById('cepsChip');
    if (chip) chip.textContent = 'CEPS: Loading...';

    if (recalculate) {
        return await saveCeps();
    }

    const ceps = await apiCall(`/ceps/${encodeURIComponent(ugid)}`);
    if (!ceps) {
        if (chip) chip.textContent = 'CEPS: Not calculated';
        return;
    }

    updateCepsUi(ceps);
}

async function saveCeps() {
    const ugid = getUserUgid();
    if (!ugid) return;

    const payload = {
        vehiclesCount: parseInt(document.getElementById('cepsVehicles')?.value || '0', 10),
        landAcres: parseFloat(document.getElementById('cepsLand')?.value || '0'),
        electricityUnitsMonthly: parseInt(document.getElementById('cepsElectricity')?.value || '0', 10),
        declaredAssetsValue: parseFloat(document.getElementById('cepsAssets')?.value || '0')
    };

    const ceps = await apiCall(`/ceps/${encodeURIComponent(ugid)}/calculate`, 'POST', payload);
    if (!ceps) {
        alert('Unable to calculate CEPS right now.');
        return;
    }

    updateCepsUi(ceps);
    alert(`CEPS updated: ${ceps.cepsScore} (${ceps.cepsCategory})`);
    return ceps;
}

function updateCepsUi(ceps) {
    const chip = document.getElementById('cepsChip');
    if (chip) {
        chip.textContent = `CEPS: ${ceps.cepsScore} (${ceps.cepsCategory})`;
        chip.classList.remove('ceps-low', 'ceps-mid', 'ceps-high');
        if (ceps.cepsScore <= 30) chip.classList.add('ceps-low');
        else if (ceps.cepsScore <= 60) chip.classList.add('ceps-mid');
        else chip.classList.add('ceps-high');
    }
}

// -----------------------------
// Real-time eligibility checker
// -----------------------------

function initRealtimeEligibilityForm() {
    const ugid = getUserUgid();
    const rtUgid = document.getElementById('rtUgid');
    if (rtUgid && ugid && !rtUgid.value) rtUgid.value = ugid;
    const resultsEl = document.getElementById('rtResults');
    if (resultsEl && !resultsEl.dataset.bound) {
        const search = document.getElementById('rtResultSearch');
        if (search) {
            search.addEventListener('input', () => filterRealtimeResults(search.value));
        }
        resultsEl.dataset.bound = 'true';
    }
}

function filterRealtimeResults(q) {
    const query = String(q || '').toLowerCase();
    document.querySelectorAll('#rtResults .result-item').forEach(el => {
        el.style.display = el.textContent.toLowerCase().includes(query) ? '' : 'none';
    });
}

async function runRealtimeEligibilityCheck() {
    const payload = {
        ugid: (document.getElementById('rtUgid')?.value || '').trim(),
        age: numberOrNull(document.getElementById('rtAge')?.value),
        annualIncome: numberOrNull(document.getElementById('rtIncome')?.value),
        isGovernmentEmployee: boolOrNull(document.getElementById('rtGov')?.value),
        vehiclesCount: numberOrNull(document.getElementById('rtVehicles')?.value),
        landAcres: floatOrNull(document.getElementById('rtLand')?.value),
        electricityUnitsMonthly: numberOrNull(document.getElementById('rtElectricity')?.value),
        declaredAssetsValue: floatOrNull(document.getElementById('rtAssets')?.value),
        employmentStatus: null
    };

    if (!payload.ugid) delete payload.ugid;

    const response = await apiCall('/eligibility/realtime-check', 'POST', payload);
    const summary = document.getElementById('rtCepsSummary');
    const container = document.getElementById('rtResults');
    if (!container) return;

    if (!response) {
        if (summary) summary.textContent = 'CEPS: -';
        container.innerHTML = '<p class="muted">Unable to run check right now.</p>';
        return;
    }

    if (summary) {
        const s = response.cepsScore != null ? `${response.cepsScore} (${response.cepsCategory || '-'})` : '-';
        summary.textContent = `CEPS: ${s}`;
    }

    const rows = (response.results || []).map(r => {
        const badge = r.eligible ? 'approved' : 'rejected';
        const label = r.eligible ? 'ELIGIBLE' : 'NOT ELIGIBLE';
        return `
            <div class="result-item">
                <div class="result-main">
                    <div class="result-title">${escapeHtml(r.schemeName)} <span class="mini">(${escapeHtml(r.sector || '-')})</span></div>
                    <div class="result-sub">${escapeHtml(r.schemeCode || '')}</div>
                </div>
                <div class="result-meta">
                    <span class="status-badge ${badge}">${label}</span>
                    <div class="muted small">${escapeHtml(r.reason || '')}</div>
                </div>
            </div>
        `;
    }).join('');

    container.innerHTML = rows || '<p class="muted">No schemes found.</p>';
    filterRealtimeResults(document.getElementById('rtResultSearch')?.value || '');
}

function numberOrNull(value) {
    if (value == null || String(value).trim() === '') return null;
    const n = parseInt(value, 10);
    return Number.isFinite(n) ? n : null;
}

function floatOrNull(value) {
    if (value == null || String(value).trim() === '') return null;
    const n = parseFloat(value);
    return Number.isFinite(n) ? n : null;
}

function boolOrNull(value) {
    if (value === 'true') return true;
    if (value === 'false') return false;
    return null;
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
