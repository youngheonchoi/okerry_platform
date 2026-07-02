document.addEventListener("DOMContentLoaded", () => {
    const email = document.getElementById("email");
    const code = document.getElementById("verification-code");
    const sendButton = document.getElementById("email-verification-trigger");
    const confirmButton = document.getElementById("email-verification-confirm");
    const panel = document.querySelector("[data-email-verification-panel]");
    const status = document.getElementById("email-verification-status");
    const csrf = document.querySelector('form[action="/signup"] input[name="_csrf"]');

    if (!email || !code || !sendButton || !confirmButton || !panel || !status || !csrf) {
        return;
    }

    const request = async (url, values) => {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-CSRF-TOKEN": csrf.value
            },
            body: new URLSearchParams(values)
        });
        if (!response.ok) {
            throw new Error("Email verification request failed");
        }
        return response.json();
    };

    const showResult = (result) => {
        status.textContent = result.message;
        status.className = result.success ? "okerry-help-text" : "okerry-error-text";
    };

    sendButton.addEventListener("click", async () => {
        sendButton.disabled = true;
        try {
            const result = await request("/signup/email-verification/send", {email: email.value});
            showResult(result);
            if (result.success) {
                panel.hidden = false;
                sendButton.setAttribute("aria-expanded", "true");
            }
        } catch (error) {
            showResult({success: false, message: "인증번호를 전송하지 못했습니다."});
        } finally {
            sendButton.disabled = false;
        }
    });

    confirmButton.addEventListener("click", async () => {
        confirmButton.disabled = true;
        try {
            const result = await request("/signup/email-verification/confirm", {
                email: email.value,
                code: code.value
            });
            showResult(result);
            if (result.success) {
                email.readOnly = true;
                code.readOnly = true;
            }
        } catch (error) {
            showResult({success: false, message: "이메일 인증을 확인하지 못했습니다."});
        } finally {
            confirmButton.disabled = false;
        }
    });
});
