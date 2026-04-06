// Load all schemes
async function loadSchemes() {
    const schemes = await apiCall('/schemes/active/all');
    const schemesList = document.getElementById('schemesList');
    const schemesTableBody = document.getElementById('schemesTableBody');
    
    if (!schemes || schemes.length === 0) {
        if (schemesList) schemesList.innerHTML = '<p>No schemes available</p>';
        if (schemesTableBody) schemesTableBody.innerHTML = '<tr><td colspan="7">No schemes available</td></tr>';
        return;
    }
    const activeSchemes = document.getElementById('activeSchemes');
    if (activeSchemes) activeSchemes.textContent = schemes.length;

    // Display for citizen
    if (schemesList) {
        schemesList.innerHTML = schemes.map(scheme => `
            <div class="scheme-card">
                <h3>${scheme.schemeName}</h3>
                <div class="scheme-details">
                    <p><strong>Code:</strong> ${scheme.schemeCode}</p>
                    <p><strong>Sector:</strong> ${scheme.sector}</p>
                    <p><strong>Benefit:</strong> ₹${scheme.benefitAmount}</p>
                    <p><strong>Max Income:</strong> ₹${scheme.maxAnnualIncome}</p>
                    <p><strong>Age Range:</strong> ${scheme.minAge} - ${scheme.maxAge} years</p>
                    ${scheme.usesCeps ? `<p><strong>CEPS Policy:</strong> ${scheme.minCepsScore} - ${scheme.maxCepsScore}</p>` : ``}
                </div>
                <button class="btn btn-primary" onclick="applyForScheme(${scheme.id})">Apply Now</button>
            </div>
        `).join('');
        setupSchemeSearch();
    }

    // Display for officer
    if (schemesTableBody) {
        schemesTableBody.innerHTML = schemes.map(scheme => `
            <tr>
                <td>${scheme.schemeCode}</td>
                <td>${scheme.schemeName}</td>
                <td>${scheme.sector}</td>
                <td>₹${scheme.maxAnnualIncome}</td>
                <td>₹${scheme.benefitAmount}</td>
                <td><span class="status-badge approved">Active</span></td>
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-sm" onclick="editScheme(${scheme.id})">Edit</button>
                    </div>
                </td>
            </tr>
        `).join('');
    }
}

// Get citizen info for scheme application
async function getCitizenInfo() {
    const userRole = localStorage.getItem('userRole');
    if (userRole === 'ADMIN' || userRole === 'OFFICER' || userRole === 'AUDITOR') {
        return null;
    }

    // For citizen, we need to get their info
    // In a real app, this would fetch from a proper endpoint
    return {
        ugid: localStorage.getItem('userUgid')
    };
}

// Apply for scheme
async function applyForScheme(schemeId) {
    const citizen = await getCitizenInfo();
    if (!citizen || !citizen.ugid) {
        alert('Please complete your profile first');
        return;
    }

    const request = {
        schemeId: schemeId,
        ugid: citizen.ugid
    };

    const result = await apiCall('/eligibility/apply', 'POST', request);
    
    if (result) {
        alert(`Application submitted! Enrollment #: ${result.enrollmentNumber}`);
        if (result.eligible) {
            alert(`Eligibility Status: ${result.eligibilityStatus}`);
        } else {
            alert(`Eligibility Status: ${result.eligibilityStatus}\nReason: ${result.message}`);
        }
        loadEnrollments();
    } else {
        alert('Error submitting application');
    }
}

// Edit scheme (officer)
async function editScheme(schemeId) {
    const existing = await apiCall(`/schemes/${schemeId}`);
    if (!existing) return;

    const schemeName = prompt('Scheme name:', existing.schemeName);
    if (!schemeName) return;
    const description = prompt('Description:', existing.description || '');
    const maxAnnualIncome = prompt('Max annual income:', existing.maxAnnualIncome);
    const benefitAmount = prompt('Benefit amount:', existing.benefitAmount);
    const minAge = prompt('Min age:', existing.minAge);
    const maxAge = prompt('Max age:', existing.maxAge);
    const isGovEligible = confirm('Allow government employees?');
    const usesCeps = confirm('Enable CEPS-based eligibility policy for this scheme?');
    let minCepsScore = existing.minCepsScore ?? 0;
    let maxCepsScore = existing.maxCepsScore ?? 100;
    if (usesCeps) {
        minCepsScore = Number(prompt('Min CEPS score (0-100):', String(minCepsScore)));
        maxCepsScore = Number(prompt('Max CEPS score (0-100):', String(maxCepsScore)));
    }

    const payload = {
        ...existing,
        schemeName,
        description,
        maxAnnualIncome: Number(maxAnnualIncome),
        benefitAmount: Number(benefitAmount),
        minAge: Number(minAge),
        maxAge: Number(maxAge),
        isGovernmentEmployeeEligible: isGovEligible,
        usesCeps,
        minCepsScore: usesCeps ? minCepsScore : 0,
        maxCepsScore: usesCeps ? maxCepsScore : 100
    };

    const result = await apiCall(`/schemes/${schemeId}`, 'PUT', payload);
    if (result) {
        alert('Scheme updated');
        loadSchemes();
    }
}

function setupSchemeSearch() {
    const searchInput = document.getElementById('schemeSearch');
    if (!searchInput || searchInput.dataset.bound) return;
    searchInput.addEventListener('input', function() {
        const q = this.value.toLowerCase();
        const schemeCards = document.querySelectorAll('#schemesList .scheme-card');
        schemeCards.forEach(card => {
            card.style.display = card.textContent.toLowerCase().includes(q) ? '' : 'none';
        });
    });
    searchInput.dataset.bound = 'true';
}

// Create new scheme modal
function openCreateSchemeModal() {
    createSchemeFlow();
}

async function createSchemeFlow() {
    const schemeCode = prompt('Scheme code (unique):');
    if (!schemeCode) return;
    const schemeName = prompt('Scheme name:');
    if (!schemeName) return;
    const description = prompt('Description:') || '';
    const sector = prompt('Sector (e.g., HEALTH, HOUSING, EDUCATION):');
    if (!sector) return;
    const schemeType = prompt('Scheme type (e.g., FINANCIAL):') || 'FINANCIAL';
    const benefitAmount = Number(prompt('Benefit amount:', '100000'));
    const maxAnnualIncome = Number(prompt('Max annual income:', '300000'));
    const minAge = Number(prompt('Min age:', '18'));
    const maxAge = Number(prompt('Max age:', '65'));
    const isGovEligible = confirm('Government employees eligible?');
    const usesCeps = confirm('Enable CEPS-based eligibility policy for this scheme?');
    let minCepsScore = 0;
    let maxCepsScore = 100;
    if (usesCeps) {
        minCepsScore = Number(prompt('Min CEPS score (0-100):', '0'));
        maxCepsScore = Number(prompt('Max CEPS score (0-100):', '30'));
    }
    const launchDate = prompt('Launch date (YYYY-MM-DD):', new Date().toISOString().slice(0, 10));

    const payload = {
        schemeCode,
        schemeName,
        description,
        sector,
        schemeType,
        benefitAmount,
        maxAnnualIncome,
        minAge,
        maxAge,
        isGovernmentEmployeeEligible: isGovEligible,
        usesCeps,
        minCepsScore: usesCeps ? minCepsScore : 0,
        maxCepsScore: usesCeps ? maxCepsScore : 100,
        launchDate
    };

    const result = await apiCall('/schemes', 'POST', payload);
    if (result) {
        alert('Scheme created');
        loadSchemes();
    }
}
