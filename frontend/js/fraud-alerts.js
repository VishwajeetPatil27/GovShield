// Load fraud alerts
async function loadFraudAlerts() {
    const fraudAlerts = await apiCall('/fraud/alerts');
    const fraudAlertsTableBody = document.getElementById('fraudAlertsTableBody');
    const fraudAnalysisTableBody = document.getElementById('fraudAnalysisTableBody');
    
    if (!fraudAlerts || fraudAlerts.length === 0) {
        if (fraudAlertsTableBody) fraudAlertsTableBody.innerHTML = '<tr><td colspan="7">No fraud alerts</td></tr>';
        if (fraudAnalysisTableBody) fraudAnalysisTableBody.innerHTML = '<tr><td colspan="7">No fraud patterns detected</td></tr>';
        return;
    }

    // For officer dashboard
    if (fraudAlertsTableBody) {
        fraudAlertsTableBody.innerHTML = fraudAlerts.map(alert => `
            <tr class="${alert.fraudRiskLevel.toLowerCase()}-risk">
                <td>${alert.id}</td>
                <td>${alert.citizen.ugid}</td>
                <td>${alert.enrollmentNumber}</td>
                <td>
                    <span class="risk-badge ${alert.fraudRiskLevel.toLowerCase()}">${alert.fraudRiskLevel}</span>
                    <div class="muted small">${alert.fraudRiskScore != null ? `Score: ${alert.fraudRiskScore}` : ''}</div>
                </td>
                <td>${alert.rejectionReason || 'Suspicious enrollment pattern'}</td>
                <td><span class="status-badge ${alert.status.toLowerCase()}">${alert.status}</span></td>
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-flag btn-sm" onclick="flagAsfraud(${alert.id})">Investigate</button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    // For auditor dashboard
    if (fraudAnalysisTableBody) {
        fraudAnalysisTableBody.innerHTML = fraudAlerts.map(alert => {
            let recommendation = '';
            if (alert.fraudRiskLevel === 'HIGH') {
                recommendation = 'Immediate investigation required';
            } else if (alert.fraudRiskLevel === 'MEDIUM') {
                recommendation = 'Further verification needed';
            } else {
                recommendation = 'Monitor and review';
            }

            return `
                <tr>
                    <td>${alert.enrollmentNumber}</td>
                    <td>${alert.citizen.ugid}</td>
                    <td>${alert.scheme.schemeName}</td>
                    <td>
                        <span class="risk-badge ${alert.fraudRiskLevel.toLowerCase()}">${alert.fraudRiskLevel}</span>
                        <div class="muted small">${alert.fraudRiskScore != null ? `Score: ${alert.fraudRiskScore}` : ''}</div>
                    </td>
                    <td>${alert.rejectionReason || '-'}</td>
                    <td><span class="status-badge ${alert.status.toLowerCase()}">${alert.status}</span></td>
                    <td>${recommendation}</td>
                </tr>
            `;
        }).join('');
    }

    updateFraudMetrics(fraudAlerts);
}

// Flag enrollment as fraud (officer)
async function flagAsfraud(enrollmentId) {
    const reason = prompt('Enter reason for flagging:');
    if (!reason) return;

    const result = await apiCall(`/fraud/flag/${enrollmentId}?reason=${encodeURIComponent(reason)}`, 'POST');
    if (result) {
        alert('Enrollment flagged for fraud investigation');
        loadFraudAlerts();
    }
}

// Detect fraud patterns for citizen (auditor)
async function detectFraudPatterns(citizenId) {
    const result = await apiCall(`/fraud/detect/${citizenId}`);
    if (result && result.length > 0) {
        alert(`Found ${result.length} suspicious patterns for this citizen`);
    } else {
        alert('No suspicious patterns detected');
    }
}

// Detect all frauds (auditor)
async function detectAllFrauds() {
    const result = await apiCall('/fraud/detect-all');
    if (result) {
        alert(`Found ${result.length} potential fraud cases`);
        loadFraudAlerts();
    }
}

// Load citizens with filtering
async function loadCitizens() {
    let citizens = await apiCall('/citizens/registrations');
    if (!Array.isArray(citizens) || citizens.length === 0) {
        const rawCitizens = await apiCall('/citizens');
        if (rawCitizens && Array.isArray(rawCitizens)) {
            citizens = rawCitizens.map(citizen => ({
                citizenId: citizen.id,
                ugid: citizen.ugid,
                name: `${citizen.firstName || ''} ${citizen.lastName || ''}`.trim(),
                email: citizen.email,
                registeredDate: citizen.createdAt,
                verificationStatus: citizen.verificationStatus || 'PENDING',
                verifiedDocuments: 0,
                totalDocuments: 0
            }));
        }
    }
    if (!Array.isArray(citizens)) {
        citizens = [];
    }

    citizens = citizens
        .map(citizen => ({
            citizenId: citizen.citizenId ?? citizen.id,
            ugid: citizen.ugid,
            name: citizen.name ?? `${citizen.firstName || ''} ${citizen.lastName || ''}`.trim(),
            email: citizen.email,
            registeredDate: citizen.registeredDate ?? citizen.createdAt,
            verificationStatus: citizen.verificationStatus || 'PENDING',
            verifiedDocuments: citizen.verifiedDocuments ?? 0,
            totalDocuments: citizen.totalDocuments ?? 0
        }))
        .filter(citizen => citizen && citizen.ugid)
        .sort((a, b) => new Date(b.registeredDate || 0) - new Date(a.registeredDate || 0));

    const citizensTableBody = document.getElementById('citizensTableBody');
    const registeredCitizens = document.getElementById('registeredCitizens');
    
    if (!citizens || citizens.length === 0) {
        if (citizensTableBody) citizensTableBody.innerHTML = '<tr><td colspan="7">No citizens found</td></tr>';
        const docsBody = document.getElementById('citizenDocsTableBody');
        if (docsBody) docsBody.innerHTML = '<tr><td colspan="7">No pending documents</td></tr>';
        if (registeredCitizens) registeredCitizens.textContent = '0';
        return;
    }

    if (citizensTableBody) {
        citizensTableBody.innerHTML = citizens.map(citizen => `
            <tr>
                <td>${citizen.ugid}</td>
                <td>${citizen.name}</td>
                <td>${citizen.email}</td>
                <td>${formatDate(citizen.registeredDate)}</td>
                <td><span class="status-badge ${statusClass(citizen.verificationStatus)}">${citizen.verificationStatus || 'PENDING'}</span></td>
                <td>${citizen.verifiedDocuments}/${citizen.totalDocuments} verified</td>
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-sm" onclick="viewCitizenDetails(${citizen.citizenId})">View</button>
                        <button class="btn btn-sm" onclick="viewCitizenDocuments(${citizen.citizenId})">Docs</button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    if (registeredCitizens) registeredCitizens.textContent = String(citizens.length);

    setupCitizenSearch(citizens);
    loadPendingCitizenDocuments();
}

// Setup citizen search
function setupCitizenSearch(citizens) {
    const searchInput = document.getElementById('citizenSearch');
    if (!searchInput || searchInput.dataset.bound) return;

    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        const rows = document.querySelectorAll('#citizensTableBody tr');
        
        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(searchTerm) ? '' : 'none';
        });
    });
    searchInput.dataset.bound = 'true';
}

// View citizen details
async function viewCitizenDetails(citizenId) {
    const citizen = await apiCall(`/citizens/${citizenId}`);
    if (citizen) {
        const details = `
            Name: ${citizen.firstName} ${citizen.lastName}
            Email: ${citizen.email}
            UGID: ${citizen.ugid}
            Aadhaar: ${citizen.aadhaar}
            Annual Income: ₹${citizen.annualIncome}
            Employment: ${citizen.employmentStatus}
            Government Employee: ${citizen.isGovernmentEmployee ? 'Yes' : 'No'}
        `;
        alert(details);
    }
}

async function viewCitizenDocuments(citizenId) {
    const docs = await apiCall(`/citizen-documents/citizen/${citizenId}`);
    if (!docs || docs.length === 0) {
        alert('No documents uploaded yet.');
        return;
    }

    const preview = docs.slice(0, 6).map(d =>
        `${d.documentType} (${d.documentNumber || 'N/A'}) - ${d.verificationStatus}`
    ).join('\n');
    alert(`Citizen Documents\n${preview}`);
}

async function loadPendingCitizenDocuments() {
    const docs = await apiCall('/citizen-documents/pending');
    const body = document.getElementById('citizenDocsTableBody');
    if (!body) return;

    if (!docs || docs.length === 0) {
        body.innerHTML = '<tr><td colspan="7">No pending documents</td></tr>';
        return;
    }

    body.innerHTML = docs.map(doc => `
        <tr>
            <td>${doc.id}</td>
            <td>${doc.citizenUgid}</td>
            <td>${doc.documentType}</td>
            <td>${doc.fileName}</td>
            <td><span class="status-badge pending">${doc.verificationStatus}</span></td>
            <td>${formatDateTime(doc.uploadedAt)}</td>
            <td>
                <button class="btn btn-sm btn-approve" onclick="verifyCitizenDocument(${doc.id}, true)">Verify</button>
                <button class="btn btn-sm btn-reject" onclick="verifyCitizenDocument(${doc.id}, false)">Reject</button>
            </td>
        </tr>
    `).join('');
}

async function verifyCitizenDocument(documentId, approved) {
    const remarks = approved ? 'Verified by authority' : (prompt('Enter rejection reason') || 'Rejected');
    const result = await apiCall(`/citizen-documents/${documentId}/verify`, 'POST', {
        approved: approved,
        remarks: remarks
    });
    if (result) {
        alert(`Document ${approved ? 'verified' : 'rejected'} successfully`);
        loadPendingCitizenDocuments();
        loadCitizens();
    }
}

// Update fraud metrics
function updateFraudMetrics(alerts) {
    const highRisk = alerts.filter(a => a.fraudRiskLevel === 'HIGH').length;
    const mediumRisk = alerts.filter(a => a.fraudRiskLevel === 'MEDIUM').length;
    const flagged = alerts.filter(a => a.status === 'FLAGGED').length;

    const highRiskElement = document.getElementById('highRiskCases');
    const mediumRiskElement = document.getElementById('mediumRiskCases');
    const flaggedElement = document.getElementById('casesFlaged');
    const fraudCountElement = document.getElementById('fraudAlerts');
    const suspectedFraudElement = document.getElementById('suspectedFraud');

    if (highRiskElement) highRiskElement.textContent = highRisk;
    if (mediumRiskElement) mediumRiskElement.textContent = mediumRisk;
    if (flaggedElement) flaggedElement.textContent = flagged;
    if (fraudCountElement) fraudCountElement.textContent = alerts.length;
    if (suspectedFraudElement) suspectedFraudElement.textContent = alerts.length;
}

function formatDate(value) {
    if (!value) return '-';
    return new Date(value).toLocaleDateString();
}

function formatDateTime(value) {
    if (!value) return '-';
    return new Date(value).toLocaleString();
}

function statusClass(status) {
    const value = (status || '').toUpperCase();
    if (value === 'VERIFIED') return 'approved';
    if (value === 'REJECTED') return 'rejected';
    return 'pending';
}

async function loadAuditLogs() {
    const logs = await apiCall('/audit');
    const tableBody = document.getElementById('auditLogsTableBody');
    if (!tableBody) return;

    if (!logs || logs.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7">No audit logs found</td></tr>';
        return;
    }

    const sorted = [...logs].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    tableBody.innerHTML = sorted.map(log => `
        <tr>
            <td>${formatDateTime(log.createdAt)}</td>
            <td>${log.action || '-'}</td>
            <td>${log.entityType || '-'}</td>
            <td>${log.entityId ?? '-'}</td>
            <td>${log.performedBy || '-'}</td>
            <td><span class="status-badge ${statusClass(log.status || 'PENDING')}">${log.status || 'PENDING'}</span></td>
            <td>${log.details || '-'}</td>
        </tr>
    `).join('');

    setupAuditLogFilters();
}

function setupAuditLogFilters() {
    const searchInput = document.getElementById('auditSearch');
    const actionFilter = document.getElementById('actionFilter');
    if (!searchInput || !actionFilter) return;

    const applyFilter = () => {
        const q = (searchInput.value || '').toLowerCase();
        const action = (actionFilter.value || '').toLowerCase();
        document.querySelectorAll('#auditLogsTableBody tr').forEach(row => {
            const text = row.textContent.toLowerCase();
            const rowAction = row.children[1]?.textContent?.toLowerCase() || '';
            const bySearch = !q || text.includes(q);
            const byAction = !action || rowAction === action;
            row.style.display = bySearch && byAction ? '' : 'none';
        });
    };

    if (!searchInput.dataset.bound) {
        searchInput.addEventListener('input', applyFilter);
        searchInput.dataset.bound = 'true';
    }
    if (!actionFilter.dataset.bound) {
        actionFilter.addEventListener('change', applyFilter);
        actionFilter.dataset.bound = 'true';
    }
    applyFilter();
}

// Generate fraud report (auditor)
function generateFraudReport() {
    const content = document.getElementById('reportContent');
    if (!content) return;

    content.innerHTML = `
        <div class="report-section">
            <h3>Fraud Detection Report</h3>
            <p><strong>Report Date:</strong> ${new Date().toLocaleDateString()}</p>
            <p><strong>Total Cases Analyzed:</strong> 542</p>
            <p><strong>High Risk Cases:</strong> 23</p>
            <p><strong>Medium Risk Cases:</strong> 67</p>
            <p><strong>Cases Under Investigation:</strong> 15</p>
            <p><strong>Cases Resolved:</strong> 8</p>
        </div>
        <div class="report-section">
            <h3>Key Findings</h3>
            <p>1. Multiple applications from same income bracket in short time period - HIGH RISK</p>
            <p>2. Inconsistencies between declared income and bank records - MEDIUM RISK</p>
            <p>3. Duplicate benefits across multiple schemes - HIGH RISK</p>
            <p>4. Government employee applying for schemes - MEDIUM RISK</p>
        </div>
        <div class="report-section">
            <h3>Recommendations</h3>
            <p>• Implement real-time cross-verification with income tax records</p>
            <p>• Strengthen family record verification process</p>
            <p>• Increase monitoring frequency for repeat applicants</p>
        </div>
    `;
}

// Generate compliance report (auditor)
function generateComplianceReport() {
    const content = document.getElementById('reportContent');
    if (!content) return;

    content.innerHTML = `
        <div class="report-section">
            <h3>Compliance Report</h3>
            <p><strong>Report Date:</strong> ${new Date().toLocaleDateString()}</p>
            <p><strong>Total Transactions Monitored:</strong> 1,245</p>
            <p><strong>Compliance Rate:</strong> 94.5%</p>
            <p><strong>Non-Compliance Cases:</strong> 68</p>
        </div>
        <div class="report-section">
            <h3>Compliance Metrics</h3>
            <p><strong>Rule Adherence:</strong> 96.2%</p>
            <p><strong>Documentation Completeness:</strong> 92.8%</p>
            <p><strong>Timely Processing:</strong> 95.1%</p>
        </div>
        <div class="report-section">
            <h3>Issues Identified</h3>
            <p>• 34 cases with incomplete documentation</p>
            <p>• 22 cases with processing delays</p>
            <p>• 12 cases with policy violations</p>
        </div>
    `;
}
