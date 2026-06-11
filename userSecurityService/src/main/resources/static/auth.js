const API_BASE_URL = "http://localhost:8080";
const output = document.getElementById("output");

function log(message, data) {
    output.textContent += `${message}\n`;
    if (data) {
        output.textContent += `${JSON.stringify(data, null, 2)}\n\n`;
    }
}

function loginWithGoogle() {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
}

function handleOAuthCallback() {
    const params = new URLSearchParams(window.location.search);
    const accessToken = params.get("accessToken");
    const refreshToken = params.get("refreshToken");
    const error = params.get("message");

    if (error) {
        log("OAuth error:", { error });
        return;
    }

    if (accessToken && refreshToken) {
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("refreshToken", refreshToken);
        log("OAuth success:", { accessToken, refreshToken });
        return;
    }

    log("No callback tokens found.");
}

async function refreshAccessToken() {
    const refreshToken = localStorage.getItem("refreshToken");

    if (!refreshToken) {
        log("No refresh token found in localStorage.");
        return;
    }

    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ refreshToken })
    });

    const data = await response.json();
    log("Refresh response:", data);

    if (response.ok) {
        localStorage.setItem("accessToken", data.token);
        localStorage.setItem("refreshToken", data.refreshToken);
    }
}

document.getElementById("googleLoginBtn").addEventListener("click", loginWithGoogle);
document.getElementById("refreshBtn").addEventListener("click", refreshAccessToken);

handleOAuthCallback();