document.addEventListener('DOMContentLoaded', () => {
    console.log("SpiltStudy Initialized");

    // --- CONFIG & UTILS ---
    const API_BASE = window.location.origin + "/api";
    
    const elements = {
        authScreen: document.getElementById('auth-screen'),
        landingScreen: document.getElementById('landing-screen'),
        workspaceScreen: document.getElementById('workspace-screen'),
        topicInput: document.getElementById('topic-input'),
        initBtn: document.getElementById('initialize-btn'),
        notesContent: document.getElementById('notes-content'),
        mainVideo: document.getElementById('main-video'),
        videoSuggestions: document.getElementById('video-suggestions'),
        updateInput: document.getElementById('update-topic-input'),
        updateBtn: document.getElementById('update-btn'),
        backBtn: document.getElementById('back-to-landing'),
        resizer: document.getElementById('layout-resizer'),
        leftPane: document.querySelector('.pane-left'),
        themeToggle: document.getElementById('theme-toggle'),
        studyThemeToggle: document.getElementById('study-theme-toggle'),
        logoutBtn: document.getElementById('logout-btn'),
        
        // Auth
        usernameInput: document.getElementById('username'),
        passwordInput: document.getElementById('password'),
        authSubmit: document.getElementById('auth-submit'),
        authToggleBtn: document.getElementById('auth-toggle-btn'),
        authSubtitle: document.getElementById('auth-subtitle'),
        authToggleText: document.getElementById('auth-toggle-text'),
        
        // Chat
        openChatBtn: document.getElementById('open-chat'),
        chatWidget: document.getElementById('chat-widget'),
        closeChatBtn: document.getElementById('close-chat'),
        chatInput: document.getElementById('chat-input'),
        sendChatBtn: document.getElementById('send-chat'),
        chatMessages: document.getElementById('chat-messages'),
        searchHistory: document.getElementById('search-history'),
        
        // Notebook
        openNotebookBtns: document.querySelectorAll('#open-notebook, #open-notebook-landing'),
        notebookModal: document.getElementById('notebook-modal'),
        closeNotebookBtn: document.getElementById('close-notebook'),
        notesList: document.getElementById('notes-list'),
        noteTitle: document.getElementById('note-title'),
        noteContent: document.getElementById('note-content'),
        saveNoteBtn: document.getElementById('save-note'),
        downloadNoteBtn: document.getElementById('download-note')
    };

    let isLoginMode = true;

    // --- AUTH LOGIC ---
    function checkAuth() {
        const user = localStorage.getItem('spiltstudy_user');
        if (!user) {
            elements.authScreen.classList.remove('hidden');
            elements.landingScreen.classList.add('hidden');
            elements.workspaceScreen.classList.add('hidden');
        } else {
            elements.authScreen.classList.add('hidden');
            elements.landingScreen.classList.remove('hidden');
            loadSearchHistory();
            loadChatHistory();
            loadNotes();
        }
    }

    async function loadSearchHistory() {
        const username = localStorage.getItem('spiltstudy_user');
        if (!username) return;
        try {
            const res = await fetch(`${API_BASE}/history?username=${username}`);
            const history = await res.json();
            
            const displayList = history.length > 0 
                ? history.slice(0, 8).map(item => item.topic)
                : ["Neural Networks", "Macroeconomics", "Cell Biology", "React Hooks"];

            elements.searchHistory.innerHTML = "";
            displayList.forEach(topic => {
                const chip = document.createElement('span');
                chip.className = 'chip';
                chip.textContent = topic;
                chip.onclick = () => {
                    elements.topicInput.value = topic;
                    elements.initBtn.click();
                };
                elements.searchHistory.appendChild(chip);
            });
        } catch (err) {
            console.error("History error:", err);
        }
    }

    async function loadChatHistory() {
        const username = localStorage.getItem('spiltstudy_user');
        if (!username) return;
        try {
            const res = await fetch(`${API_BASE}/chat/history?username=${username}`);
            const history = await res.json();
            elements.chatMessages.innerHTML = "";
            history.forEach(msg => addChatMessage(msg.sender, msg.content, false));
        } catch (err) {
            console.error("Chat history error:", err);
        }
    }

    elements.authToggleBtn.addEventListener('click', (e) => {
        e.preventDefault();
        isLoginMode = !isLoginMode;
        elements.authSubtitle.textContent = isLoginMode ? "Welcome Back" : "Create Account";
        elements.authSubmit.textContent = isLoginMode ? "Login" : "Sign Up";
        elements.authToggleText.textContent = isLoginMode ? "Don't have an account?" : "Already have an account?";
        elements.authToggleBtn.textContent = isLoginMode ? "Sign Up" : "Login";
    });

    elements.authSubmit.addEventListener('click', async () => {
        const username = elements.usernameInput.value;
        const password = elements.passwordInput.value;
        if (!username || !password) return alert("Please fill all fields");

        const endpoint = isLoginMode ? "/auth/login" : "/auth/register";
        try {
            const res = await fetch(API_BASE + endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            const data = await res.json();
            
            if (data.success) {
                if (isLoginMode) {
                    localStorage.setItem('spiltstudy_user', username);
                    checkAuth();
                } else {
                    alert("Registration successful! Please login.");
                    isLoginMode = true;
                    elements.authToggleBtn.click();
                }
            } else {
                alert(data.message);
            }
        } catch (err) {
            console.error("Auth error:", err);
            alert("Backend Connection Error. Check console.");
        }
    });

    elements.logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('spiltstudy_user');
        checkAuth();
    });

    // --- THEME LOGIC ---
    elements.themeToggle.addEventListener('click', () => {
        document.body.classList.toggle('light-mode');
        const icon = elements.themeToggle.querySelector('i');
        const isLight = document.body.classList.contains('light-mode');
        icon.setAttribute('data-lucide', isLight ? 'sun' : 'moon');
        lucide.createIcons();
    });

    elements.studyThemeToggle.addEventListener('click', () => {
        document.body.classList.toggle('study-theme');
        lucide.createIcons();
    });

    // --- DATA FETCHING ---
    async function loadWorkspace(topic) {
        if (!topic) return;
        console.log("Loading workspace for:", topic);
        
        elements.notesContent.innerHTML = "<p style='color: var(--text-secondary);'>Re-imagining the academic landscape for " + topic + "...</p>";
        elements.videoSuggestions.innerHTML = "";
        
        elements.landingScreen.classList.add('hidden');
        elements.workspaceScreen.classList.remove('hidden');

        try {
            const username = localStorage.getItem('spiltstudy_user');
            const res = await fetch(`${API_BASE}/summarize?topic=${encodeURIComponent(topic)}&username=${username}`);
            const data = await res.json();
            
            loadSearchHistory(); // Refresh history
            
            // Render Notes
            if (data.notes) {
                elements.notesContent.innerHTML = data.notes;
            } else {
                elements.notesContent.innerHTML = "<h1>" + topic + "</h1><p>No detailed notes found. Try another topic.</p>";
            }

            // Render Videos
            if (data.videos && data.videos.length > 0) {
                const first = data.videos[0];
                elements.mainVideo.src = `https://www.youtube.com/embed/${first.videoId}?autoplay=1&mute=1`;
                
                data.videos.slice(1).forEach((v, i) => {
                    const card = document.createElement('div');
                    card.className = 'video-card stagger';
                    card.style.animationDelay = (i * 0.1) + "s";
                    card.innerHTML = `
                        <img src="https://img.youtube.com/vi/${v.videoId}/mqdefault.jpg">
                        <div>
                            <h4 style="font-size: 0.85rem; margin-bottom: 0.2rem;">${v.title || topic}</h4>
                            <span style="font-size: 0.7rem; color: var(--text-secondary);">Academic Source</span>
                        </div>
                    `;
                    card.onclick = () => elements.mainVideo.src = `https://www.youtube.com/embed/${v.videoId}?autoplay=1`;
                    elements.videoSuggestions.appendChild(card);
                });
            }
        } catch (err) {
            console.error("Fetch error:", err);
            elements.notesContent.innerHTML = "<h2 style='color: var(--accent-teal);'>Connection Interrupted</h2><p>The study server is currently unreachable. Ensure port 8080 is active.</p>";
        }
        
        lucide.createIcons();
    }

    elements.initBtn.addEventListener('click', () => loadWorkspace(elements.topicInput.value));
    elements.updateBtn.addEventListener('click', () => loadWorkspace(elements.updateInput.value));
    elements.backBtn.addEventListener('click', () => {
        elements.workspaceScreen.classList.add('hidden');
        elements.landingScreen.classList.remove('hidden');
        elements.mainVideo.src = "";
    });

    // Chips
    document.querySelectorAll('.chip').forEach(c => {
        c.addEventListener('click', () => {
            elements.topicInput.value = c.textContent;
            elements.initBtn.click();
        });
    });

    // Keyboard support
    elements.topicInput.addEventListener('keypress', (e) => { if(e.key === 'Enter') elements.initBtn.click(); });
    elements.updateInput.addEventListener('keypress', (e) => { if(e.key === 'Enter') elements.updateBtn.click(); });

    // --- CHAT LOGIC ---
    elements.openChatBtn.addEventListener('click', () => elements.chatWidget.classList.toggle('hidden'));
    elements.closeChatBtn.addEventListener('click', () => elements.chatWidget.classList.add('hidden'));

    async function sendChat() {
        const msg = elements.chatInput.value.trim();
        if (!msg) return;

        addChatMessage("user", msg);
        elements.chatInput.value = "";

        try {
            const username = localStorage.getItem('spiltstudy_user');
            const res = await fetch(API_BASE + "/chat", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: msg, username: username })
            });
            const data = await res.json();
            addChatMessage("bot", data.response);
        } catch (err) {
            console.error("Chat error:", err);
            addChatMessage("bot", "Error: My connection to the brain is offline.");
        }
    }

    function addChatMessage(role, text, scroll = true) {
        const div = document.createElement('div');
        div.style.padding = "0.75rem 1rem";
        div.style.borderRadius = "12px";
        div.style.maxWidth = "80%";
        div.style.fontSize = "0.9rem";
        div.style.alignSelf = role === "user" ? "flex-end" : "flex-start";
        div.style.background = role === "user" ? "var(--accent-blue)" : "var(--surface-light)";
        div.style.color = "white";
        div.textContent = text;
        elements.chatMessages.appendChild(div);
        if (scroll) elements.chatMessages.scrollTop = elements.chatMessages.scrollHeight;
    }

    elements.sendChatBtn.addEventListener('click', sendChat);
    elements.chatInput.addEventListener('keypress', (e) => { if(e.key === 'Enter') sendChat(); });

    // --- NOTEBOOK LOGIC ---
    elements.openNotebookBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            elements.notebookModal.classList.remove('hidden');
            loadNotes();
        });
    });
    elements.closeNotebookBtn.addEventListener('click', () => elements.notebookModal.classList.add('hidden'));

    async function loadNotes() {
        const username = localStorage.getItem('spiltstudy_user');
        if (!username) return;
        try {
            const res = await fetch(`${API_BASE}/notes?username=${username}`);
            let notes = await res.json();
            // Sort by date (descending) in frontend to avoid Firestore index requirement
            notes.sort((a, b) => b.createdAt._seconds - a.createdAt._seconds);
            
            elements.notesList.innerHTML = "";
            notes.forEach(note => {
                const item = document.createElement('div');
                item.style.padding = "0.75rem";
                item.style.borderRadius = "8px";
                item.style.background = "var(--surface-light)";
                item.style.cursor = "pointer";
                item.style.fontSize = "0.85rem";
                item.style.border = "1px solid var(--border)";
                item.style.position = "relative";
                item.style.display = "flex";
                item.style.justifyContent = "space-between";
                item.style.alignItems = "center";
                
                const textContainer = document.createElement('div');
                textContainer.innerHTML = `<div style="font-weight: 700;">${note.title}</div><div style="font-size: 0.7rem; opacity: 0.6;">${new Date(note.createdAt).toLocaleDateString()}</div>`;
                textContainer.onclick = () => {
                    elements.noteTitle.value = note.title;
                    elements.noteContent.value = note.content;
                };
                
                const deleteBtn = document.createElement('button');
                deleteBtn.innerHTML = '<i data-lucide="trash-2" style="width: 14px;"></i>';
                deleteBtn.style.background = "transparent";
                deleteBtn.style.border = "none";
                deleteBtn.style.color = "#ef4444";
                deleteBtn.style.cursor = "pointer";
                deleteBtn.style.padding = "4px";
                deleteBtn.onclick = async (e) => {
                    e.stopPropagation();
                    if (confirm("Delete this note?")) {
                        await fetch(`${API_BASE}/notes/${note.id}`, { method: 'DELETE' });
                        loadNotes();
                    }
                };

                item.appendChild(textContainer);
                item.appendChild(deleteBtn);
                elements.notesList.appendChild(item);
            });
            lucide.createIcons();
        } catch (err) {
            console.error("Notes error:", err);
        }
    }

    elements.saveNoteBtn.addEventListener('click', async () => {
        const username = localStorage.getItem('spiltstudy_user');
        const title = elements.noteTitle.value;
        const content = elements.noteContent.value;
        if (!title || !content) return alert("Please enter title and content");

        try {
            const res = await fetch(`${API_BASE}/notes`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, title, content })
            });
            const data = await res.json();
            if (data.success) {
                alert("Note saved successfully!");
                loadNotes();
            } else {
                alert(data.message); // Show "Space Exceeded"
            }
        } catch (err) {
            console.error("Save note error:", err);
        }
    });

    elements.downloadNoteBtn.addEventListener('click', () => {
        const title = elements.noteTitle.value || "Untitled Note";
        const content = elements.noteContent.value;
        if (!content) return alert("No content to download");

        const blob = new Blob([content], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${title}.txt`;
        a.click();
        URL.revokeObjectURL(url);
    });

    // --- RESIZER ---
    let isResizing = false;
    elements.resizer.addEventListener('mousedown', () => isResizing = true);
    document.addEventListener('mousemove', (e) => {
        if (!isResizing) return;
        let percentage = (e.clientX / window.innerWidth) * 100;
        if (percentage > 25 && percentage < 75) elements.leftPane.style.width = percentage + "%";
    });
    document.addEventListener('mouseup', () => isResizing = false);

    // Startup
    checkAuth();
    lucide.createIcons();
});