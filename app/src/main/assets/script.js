function createRipple(event) {
    const button = event.currentTarget;
    const circle = document.createElement("span");
    const diameter = Math.max(button.clientWidth, button.clientHeight);
    const radius = diameter / 2;

    circle.style.width = circle.style.height = `${diameter}px`;
    circle.style.left = `${event.clientX - button.offsetLeft - radius}px`;
    circle.style.top = `${event.clientY - button.offsetTop - radius}px`;
    circle.classList.add("ripple");

    const ripple = button.getElementsByClassName("ripple")[0];
    if (ripple) {
        ripple.remove();
    }

    button.appendChild(circle);
}

let isEnabled = false;

// Check the state when page loads
function checkNotificationState() {
    if (window.AndroidInterface && window.AndroidInterface.isNotificationsEnabled) {
        const enabled = window.AndroidInterface.isNotificationsEnabled();
        if (enabled) {
            notificationsEnabled();
        }
    }
}

// Call checkNotificationState when the page is ready
document.addEventListener('DOMContentLoaded', function() {
    // Small delay to ensure AndroidInterface is ready
    setTimeout(checkNotificationState, 100);
});

function toggleNotifications() {
    const btn = document.getElementById('mainBtn');
    if (isEnabled) {
        if (window.AndroidInterface) {
            window.AndroidInterface.disableNotifications();
        }
        isEnabled = false;
        btn.textContent = 'Enable Daily Popups';
        btn.classList.remove('enabled');
        updateStatusIndicator(false);
    } else {
        if (window.AndroidInterface) {
            window.AndroidInterface.enableNotifications();
        }
    }
}

function updateStatusIndicator(enabled) {
    const statusEmoji = document.getElementById('statusEmoji');
    const statusText = document.getElementById('statusText');
    
    if (enabled) {
        statusEmoji.textContent = '🔔';
        statusText.textContent = 'Notifications Enabled';
    } else {
        statusEmoji.textContent = '🔕';
        statusText.textContent = 'Notifications Disabled';
    }
}

function notificationsEnabled() {
    const btn = document.getElementById('mainBtn');
    isEnabled = true;
    btn.textContent = 'Disable Daily Popups';
    btn.classList.add('enabled');
    updateStatusIndicator(true);
}

