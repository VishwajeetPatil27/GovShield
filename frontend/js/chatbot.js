(function() {
    if (window.__govshieldChatbotMounted) return;
    window.__govshieldChatbotMounted = true;

    const API_BASE_URL =
        (window.GOVSHIELD && window.GOVSHIELD.API_BASE_URL)
            ? window.GOVSHIELD.API_BASE_URL
            : 'http://localhost:8080/api';

    const widget = document.createElement('div');
    widget.className = 'chatbot-widget';
    widget.innerHTML = `
        <div class="chatbot-header">
            <strong>GovShield Assistant</strong>
            <button id="chatToggleBtn" class="btn btn-sm" style="padding:0.2rem 0.5rem;">_</button>
        </div>
        <div id="chatBody" class="chatbot-body">
            <div class="chat-msg bot">Hi. Ask about login, registration, UGID, document verification, or scheme application.</div>
        </div>
        <div id="chatInputArea" class="chatbot-input">
            <input id="chatInput" type="text" placeholder="Type your question...">
            <button id="chatSendBtn" class="btn btn-primary">Send</button>
        </div>
    `;
    document.body.appendChild(widget);

    const chatBody = document.getElementById('chatBody');
    const chatInput = document.getElementById('chatInput');
    const chatSendBtn = document.getElementById('chatSendBtn');
    const chatToggleBtn = document.getElementById('chatToggleBtn');
    const chatInputArea = document.getElementById('chatInputArea');

    function pushMessage(text, role) {
        const div = document.createElement('div');
        div.className = `chat-msg ${role}`;
        div.textContent = text;
        chatBody.appendChild(div);
        chatBody.scrollTop = chatBody.scrollHeight;
    }

    async function fetchRealtimeSummary() {
        const token = localStorage.getItem('authToken');
        const role = localStorage.getItem('userRole');
        if (!token || !role) {
            return 'Login first, then I can provide your live dashboard summary.';
        }

        try {
            if (role === 'CITIZEN') {
                const citizenId = localStorage.getItem('userId');
                const res = await fetch(`${API_BASE_URL}/eligibility/citizen/${citizenId}`, {
                    headers: { 'Authorization': `Bearer ${token}`, 'X-User-Role': role }
                });
                const apps = await res.json();
                const total = Array.isArray(apps) ? apps.length : 0;
                const approved = Array.isArray(apps) ? apps.filter(a => a.status === 'APPROVED').length : 0;
                return `Real-time status: ${total} applications, ${approved} approved.`;
            }

            const res = await fetch(`${API_BASE_URL}/citizen-documents/pending`, {
                headers: { 'Authorization': `Bearer ${token}`, 'X-User-Role': role }
            });
            const pendingDocs = await res.json();
            const pendingCount = Array.isArray(pendingDocs) ? pendingDocs.length : 0;
            return `Real-time status: ${pendingCount} citizen documents pending verification.`;
        } catch (e) {
            return 'I could not fetch live data right now. Please retry in a few seconds.';
        }
    }

    async function getAssistantReply(rawInput) {
        const input = (rawInput || '').toLowerCase().trim();
        if (!input) return 'Please type a question.';

        if (input.includes('real time') || input.includes('summary') || input.includes('live')) {
            return fetchRealtimeSummary();
        }
        if (input.includes('login')) {
            return 'Employees use email + password. Citizens use Aadhaar + UGID.';
        }
        if (input.includes('ugid')) {
            return 'UGID is generated during onboarding and remains the citizen primary identifier.';
        }
        if (input.includes('document') || input.includes('verify')) {
            return 'Citizens upload Aadhaar/PAN/other docs during onboarding. Officers/Admin verify from Citizens section.';
        }
        if (input.includes('register')) {
            return 'Open Citizen Onboarding, fill details, upload documents, submit, then keep your generated UGID.';
        }
        if (input.includes('scheme') || input.includes('apply')) {
            return 'After citizen login, open View Schemes and click Apply Now.';
        }
        return 'Try: "live summary", "how to register", "how UGID works", "verify documents".';
    }

    async function handleSend() {
        const text = chatInput.value;
        if (!text.trim()) return;
        pushMessage(text, 'user');
        chatInput.value = '';
        const reply = await getAssistantReply(text);
        pushMessage(reply, 'bot');
    }

    chatSendBtn.addEventListener('click', handleSend);
    chatInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') handleSend();
    });

    let minimized = false;
    chatToggleBtn.addEventListener('click', function() {
        minimized = !minimized;
        chatBody.style.display = minimized ? 'none' : 'block';
        chatInputArea.style.display = minimized ? 'none' : 'flex';
        chatToggleBtn.textContent = minimized ? '+' : '_';
    });
})();
