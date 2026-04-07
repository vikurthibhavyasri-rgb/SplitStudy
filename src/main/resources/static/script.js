document.addEventListener('DOMContentLoaded', () => {
    // UI Elements
    const studyBtn = document.getElementById('study-btn');
    const updateBtn = document.getElementById('update-btn');
    const backBtn = document.getElementById('back-btn');
    const landingScreen = document.getElementById('landing-screen');
    const workspaceScreen = document.getElementById('workspace-screen');
    const videoFrame = document.getElementById('video-frame');
    const suggestionsList = document.getElementById('suggestions-list');
    const notesContent = document.getElementById('notes-content');
    const resizer = document.getElementById('resizer');
    const leftPane = document.querySelector('.left-pane');
    const syncPill = document.getElementById('sync-pill');
    const topicInput = document.getElementById('topic-input');
    const updateTopicInput = document.getElementById('update-topic-input');
    const tagChips = document.querySelectorAll('.tag-chip');
    const themeToggles = document.querySelectorAll('.theme-toggle');
    const bgToggleBtn = document.getElementById('bg-toggle-btn');
    const bgOptions = document.getElementById('bg-options');
    const bgOptionElements = document.querySelectorAll('.bg-option');
    const suggestionsDrawer = document.getElementById('suggestions-pane');
    const moreVideosBtn = document.getElementById('more-videos-btn');
    const closeSuggestionsBtn = document.getElementById('close-suggestions-btn');

    // --- BACKGROUND SELECTOR LOGIC ---
    bgToggleBtn.addEventListener('click', () => {
        bgOptions.classList.toggle('hidden');
    });

    bgOptionElements.forEach(option => {
        option.addEventListener('click', (e) => {
            const bgFile = e.target.getAttribute('data-bg');
            if (bgFile === 'none') {
                landingScreen.style.backgroundImage = 'radial-gradient(circle at center, var(--surface) 0%, var(--bg-dark) 80%)';
                document.body.classList.remove('force-dark-text');
            } else {
                landingScreen.style.backgroundImage = `url(${bgFile})`;
                document.body.classList.add('force-dark-text');
            }
            bgOptions.classList.add('hidden');
        });
    });

    // Close options if clicked outside
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.bg-selector')) {
            bgOptions.classList.add('hidden');
        }
    });

    // --- MORE VIDEOS DRAWER LOGIC ---
    function toggleDrawer(isOpenOverride) {
        let isOpen = suggestionsDrawer.classList.contains('drawer-open');
        
        // Use override if provided, otherwise standard toggle logic
        if (isOpenOverride !== undefined) {
            isOpen = !isOpenOverride; // So the below negation makes it exactly what was requested
        }

        if (!isOpen) { // Means it is currently closed, so open it
            suggestionsDrawer.classList.add('drawer-open');
            moreVideosBtn.innerHTML = '<i data-lucide="x" style="width: 16px;"></i> Close';
            moreVideosBtn.style.background = 'rgba(255, 59, 48, 0.75)';
            moreVideosBtn.style.borderColor = 'rgba(255, 59, 48, 0.4)';
        } else { // Means it is currently open, so close it
            suggestionsDrawer.classList.remove('drawer-open');
            moreVideosBtn.innerHTML = '<i data-lucide="list-video" style="width: 16px;"></i> More Videos';
            moreVideosBtn.style.background = 'rgba(10, 11, 13, 0.75)';
            moreVideosBtn.style.borderColor = 'rgba(255, 255, 255, 0.15)';
        }
        if (window.lucide) lucide.createIcons();
    }

    moreVideosBtn.addEventListener('click', () => toggleDrawer());
    
    if (closeSuggestionsBtn) {
        closeSuggestionsBtn.addEventListener('click', () => toggleDrawer(false));
    }


    // --- THEME TOGGLE LOGIC ---
    function toggleTheme() {
        document.body.classList.toggle('light-mode');
        const isLight = document.body.classList.contains('light-mode');
        
        document.querySelectorAll('.sun-icon').forEach(el => {
            isLight ? el.classList.remove('hidden') : el.classList.add('hidden');
        });
        document.querySelectorAll('.moon-icon').forEach(el => {
            isLight ? el.classList.add('hidden') : el.classList.remove('hidden');
        });
        
        // Refresh Icons
        if (window.lucide) lucide.createIcons();
    }

    themeToggles.forEach(toggle => {
        toggle.addEventListener('click', toggleTheme);
    });

    // --- RESIZABLE PANE LOGIC ---
    let isResizing = false;

    resizer.addEventListener('mousedown', (e) => {
        isResizing = true;
        resizer.classList.add('dragging');
        document.body.style.cursor = 'col-resize';
    });

    document.addEventListener('mousemove', (e) => {
        if (!isResizing) return;
        
        let containerWidth = document.querySelector('.split-container').offsetWidth;
        let percentage = (e.clientX / containerWidth) * 100;

        if (percentage > 20 && percentage < 80) {
            leftPane.style.width = `${percentage}%`;
        }
    });

    document.addEventListener('mouseup', () => {
        isResizing = false;
        resizer.classList.remove('dragging');
        document.body.style.cursor = 'default';
    });

    // --- LOADING & DATA ---
    function loadData(topic) {
        if (!topic) return;
        
        // Show Sync Pill (Pulsing)
        syncPill.classList.remove('hidden');
        notesContent.innerHTML = "<div class='stagger-in' style='color: var(--text-secondary); font-size: 0.9rem;'>Generating Academic Intelligence...</div>";
        suggestionsList.innerHTML = "";

        fetch(`http://localhost:8080/api/summarize?topic=${encodeURIComponent(topic)}`)
            .then(res => res.json()) 
            .then(data => {
                // Mock formatting for demo if backend returns plain text
                // Let's wrap standard content with our academic classes
                let formattedNotes = `
                    <div class="stagger-in" style="animation-delay: 0.1s">
                        <h1>${topic}</h1>
                    </div>
                `;
                
                // If backend provides real HTML, use it, otherwise use fallback
                if (data.notes && data.notes.length > 20) {
                     formattedNotes += `<div class="stagger-in" style="animation-delay: 0.2s">${data.notes}</div>`;
                } else {
                     formattedNotes += `
                        <div class="stagger-in" style="animation-delay: 0.2s">
                            <p>Exploring the fundamental concepts of <span class="key-term">Synthesis</span> and <span class="key-term">Core Methodology</span>.</p>
                            <h2>Key Principles</h2>
                            <p>The study revolves around architectural integrity and stateless execution environments.</p>
                        </div>
                     `;
                }

                notesContent.innerHTML = formattedNotes;

                // Handle YouTube Videos
                if (data.videos && data.videos.length > 0) {
                    const mainVideo = data.videos[0];
                    videoFrame.src = `https://www.youtube.com/embed/${mainVideo.videoId}?autoplay=1&mute=1&modestbranding=1`;
                    moreVideosBtn.style.display = 'flex';

                    const extraVideos = data.videos.slice(1, 5); 
                    extraVideos.forEach((video, index) => {
                        const item = document.createElement('div');
                        item.className = 'suggestion-item stagger-in';
                        item.style.animationDelay = `${0.3 + (index * 0.1)}s`;
                        
                        item.innerHTML = `
                            <img src="https://img.youtube.com/vi/${video.videoId}/mqdefault.jpg" class="suggestion-thumb" alt="thumb">
                            <div class="suggestion-info">
                                <h3>${video.title || 'Related Source'}</h3>
                                <div class="suggestion-meta">${video.channelTitle || 'Perspective Channel'}</div>
                            </div>
                        `;

                        item.onclick = () => {
                            videoFrame.src = `https://www.youtube.com/embed/${video.videoId}?autoplay=1&modestbranding=1`;
                        };

                        suggestionsList.appendChild(item);
                    });
                }
                
                // Refresh Icons
                if (window.lucide) lucide.createIcons();
                
                // Settle Sync Pill
                setTimeout(() => {
                    syncPill.style.animation = 'none';
                    syncPill.style.opacity = '0.8';
                }, 2000);
            })
            .catch(err => {
                notesContent.innerHTML = "<p style='color:var(--accent-teal);'>Backend offline. Ensure SpiltStudy API is active on port 8080.</p>";
                syncPill.classList.add('hidden');
            });
    }

    // --- NAVIGATION ---
    studyBtn.addEventListener('click', () => {
        const topic = topicInput.value;
        if(topic) {
            loadData(topic);
            landingScreen.classList.add('hidden');
            workspaceScreen.classList.remove('hidden');
        }
    });

    updateBtn.addEventListener('click', () => {
        const topic = updateTopicInput.value;
        if(topic) {
            loadData(topic);
        }
    });

    backBtn.addEventListener('click', () => {
        workspaceScreen.classList.add('hidden');
        landingScreen.classList.remove('hidden');
        videoFrame.src = "";
        moreVideosBtn.style.display = 'none';
        suggestionsDrawer.classList.remove('drawer-open');
        moreVideosBtn.innerHTML = '<i data-lucide="list-video" style="width: 16px;"></i> More Videos';
        moreVideosBtn.style.background = 'rgba(10, 11, 13, 0.75)';
        moreVideosBtn.style.borderColor = 'rgba(255, 255, 255, 0.15)';
    });

    // Tag Chip shortcuts
    tagChips.forEach(chip => {
        chip.addEventListener('click', () => {
            topicInput.value = chip.textContent;
            studyBtn.click();
        });
    });

    // Enter key support
    topicInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') studyBtn.click();
    });
});