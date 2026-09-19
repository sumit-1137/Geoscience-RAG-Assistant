// =====================================================
// GEOMIND AI - CHAT SCRIPT
// =====================================================

const RAG_API = "/api/rag";
const HISTORY_KEY = "geomindHistory";


// =====================================================
// QUESTION SUGGESTION
// =====================================================

function useQuestion(button) {

    const question =
        button.querySelector("span").innerText;

    const input =
        document.getElementById("questionInput");

    input.value = question;

    input.focus();

    autoResize(input);
}


// =====================================================
// AUTO RESIZE
// =====================================================

function autoResize(textarea) {

    textarea.style.height = "auto";

    textarea.style.height =
        Math.min(textarea.scrollHeight, 120) + "px";
}


// =====================================================
// ENTER KEY
// =====================================================

function handleEnter(event) {

    if (
        event.key === "Enter" &&
        !event.shiftKey
    ) {

        event.preventDefault();

        sendQuestion();
    }
}


// =====================================================
// SEND QUESTION
// =====================================================

async function sendQuestion() {

    const input =
        document.getElementById("questionInput");

    if (!input) {
        return;
    }

    const question =
        input.value.trim();

    if (!question) {
        return;
    }

    const chatMessages =
        document.getElementById("chatMessages");

    if (!chatMessages) {
        return;
    }


    // =================================================
    // SHOW USER QUESTION
    // =================================================

    chatMessages.innerHTML += `

        <div class="message user-message">

            <div class="message-label">
                You
            </div>

            <div class="message-content">
                ${escapeHtml(question)}
            </div>

        </div>

    `;


    // =================================================
    // CLEAR INPUT
    // =================================================

    input.value = "";

    input.style.height = "auto";


    // =================================================
    // LOADING
    // =================================================

    const loadingId =
        "loading-" + Date.now();

    chatMessages.innerHTML += `

        <div
            class="message ai-message"
            id="${loadingId}">

            <div class="message-label">
                GeoMind AI
            </div>

            <div class="message-content">

                <div class="loading-dots">

                    <span></span>
                    <span></span>
                    <span></span>

                </div>

            </div>

        </div>

    `;

    scrollToBottom();


    // =================================================
    // CALL RAG API
    // =================================================

    try {

        const response =
            await fetch(
                RAG_API +
                "?question=" +
                encodeURIComponent(question)
            );


        // =================================================
        // CHECK RESPONSE
        // =================================================

        if (!response.ok) {

            throw new Error(
                "HTTP " + response.status
            );
        }


        // =================================================
        // READ JSON
        // =================================================

        const data =
            await response.json();

        console.log(
            "RAG response:",
            data
        );


        // =================================================
        // REMOVE LOADING
        // =================================================

        const loading =
            document.getElementById(loadingId);

        if (loading) {
            loading.remove();
        }


        // =================================================
        // GET ANSWER
        // =================================================

        const answer =
            data.answer ||
            "No answer was generated.";


        // =================================================
        // CREATE SOURCES
        // =================================================

        const sourcesHtml =
            createSourcesHtml(
                data.sources || []
            );


        // =================================================
        // SHOW AI ANSWER
        // =================================================

        chatMessages.innerHTML += `

            <div class="message ai-message">

                <div class="message-label">
                    GeoMind AI
                </div>

                <div class="message-content">
                    ${formatAnswer(answer)}
                </div>

                ${sourcesHtml}

            </div>

        `;


        // =================================================
        // SAVE HISTORY
        // =================================================

        saveConversation({

            question: question,

            answer: answer,

            sources:
                data.sources || [],

            timestamp:
                new Date().toISOString()

        });


        scrollToBottom();


    } catch (error) {

        console.error(
            "RAG API Error:",
            error
        );


        // =================================================
        // REMOVE LOADING
        // =================================================

        const loading =
            document.getElementById(loadingId);

        if (loading) {
            loading.remove();
        }


        // =================================================
        // SHOW USER-FRIENDLY ERROR
        // =================================================

        chatMessages.innerHTML += `

            <div class="message ai-message">

                <div class="message-label">
                    GeoMind AI
                </div>

                <div class="message-content">

                    <strong>
                        Unable to generate an answer.
                    </strong>

                    <br><br>

                    Please try again in a moment.

                </div>

            </div>

        `;

        scrollToBottom();
    }
}


// =====================================================
// CREATE SOURCES HTML
// =====================================================

function createSourcesHtml(sources) {

    if (
        !sources ||
        !Array.isArray(sources) ||
        sources.length === 0
    ) {

        return "";
    }


    let sourcesHtml = `

        <div class="sources">

            <div class="sources-title">

                <i class="bi bi-book"></i>

                <span>
                    Sources
                </span>

            </div>

            <ul>
    `;


    sources.forEach(source => {

        if (!source) {
            return;
        }


        const fileName =
            source.fileName ||
            "Unknown document";

        const chunkIndex =
            source.chunkIndex !== undefined &&
            source.chunkIndex !== null
                ? source.chunkIndex
                : "Unknown";


        sourcesHtml += `

            <li>

                ${escapeHtml(fileName)}

                |

                Chunk
                ${escapeHtml(chunkIndex)}

            </li>

        `;
    });


    sourcesHtml += `

            </ul>

        </div>

    `;


    return sourcesHtml;
}


// =====================================================
// SAVE CONVERSATION
// =====================================================

function saveConversation(conversation) {

    try {

        let history =
            JSON.parse(
                localStorage.getItem(
                    HISTORY_KEY
                )
            );


        if (!Array.isArray(history)) {

            history = [];

        }


        history.push(conversation);


        localStorage.setItem(

            HISTORY_KEY,

            JSON.stringify(history)

        );


        console.log(
            "Conversation saved:",
            conversation
        );


    } catch (error) {

        console.error(
            "Could not save history:",
            error
        );
    }
}


// =====================================================
// SCROLL TO BOTTOM
// =====================================================

function scrollToBottom() {

    const chatMessages =
        document.getElementById(
            "chatMessages"
        );


    if (!chatMessages) {
        return;
    }


    chatMessages.scrollTop =
        chatMessages.scrollHeight;


    window.scrollTo({

        top:
        document.body.scrollHeight,

        behavior:
            "smooth"

    });
}


// =====================================================
// FORMAT AI ANSWER
// =====================================================

function formatAnswer(answer) {

    if (!answer) {

        return "No answer was generated.";

    }


    // -------------------------------------------------
    // Escape HTML first for security
    // -------------------------------------------------

    let text =
        escapeHtml(answer);


    // -------------------------------------------------
    // Convert bold text
    // Example: **Geoscience**
    // -------------------------------------------------

    text =
        text.replace(
            /\*\*(.*?)\*\*/g,
            "<strong>$1</strong>"
        );


    // -------------------------------------------------
    // Convert bullet points
    // Example:
    // - Physics
    // - Chemistry
    // -------------------------------------------------

    text =
        text.replace(
            /^\s*[-*]\s+(.*)$/gm,
            "<li>$1</li>"
        );


    // -------------------------------------------------
    // Wrap bullet lists
    // -------------------------------------------------

    text =
        text.replace(
            /((?:<li>.*?<\/li>\s*)+)/gs,
            "<ul>$1</ul>"
        );


    // -------------------------------------------------
    // Convert numbered lists
    // Example:
    // 1. First point
    // 2. Second point
    // -------------------------------------------------

    text =
        text.replace(
            /^\s*\d+\.\s+(.*)$/gm,
            "<li>$1</li>"
        );


    // -------------------------------------------------
    // Wrap numbered list items
    // -------------------------------------------------

    text =
        text.replace(
            /((?:<li>.*?<\/li>\s*)+)/gs,
            "<ul>$1</ul>"
        );


    // -------------------------------------------------
    // Convert line breaks
    // -------------------------------------------------

    text =
        text.replace(
            /\n/g,
            "<br>"
        );


    return text;
}


// =====================================================
// ESCAPE HTML
// =====================================================

function escapeHtml(text) {

    if (
        text === null ||
        text === undefined
    ) {

        return "";

    }


    const div =
        document.createElement("div");


    div.textContent =
        String(text);


    return div.innerHTML;
}


// =====================================================
// NEW CHAT
// =====================================================

function newChat() {

    const chatMessages =
        document.getElementById(
            "chatMessages"
        );


    const input =
        document.getElementById(
            "questionInput"
        );


    if (chatMessages) {

        chatMessages.innerHTML = "";

    }


    if (input) {

        input.value = "";

        input.style.height = "auto";

        input.focus();

    }


    // -------------------------------------------------
    // Remove history query parameter
    // -------------------------------------------------

    window.history.replaceState(
        {},
        document.title,
        "index.html"
    );
}


// =====================================================
// LOAD HISTORY CONVERSATION
// =====================================================

function loadSelectedConversation() {

    const params =
        new URLSearchParams(
            window.location.search
        );


    const historyIndex =
        params.get("history");


    if (
        historyIndex === null
    ) {

        return;

    }


    const history =
        getHistory();


    const index =
        parseInt(
            historyIndex,
            10
        );


    if (
        isNaN(index) ||
        !history[index]
    ) {

        return;

    }


    const chat =
        history[index];


    const chatMessages =
        document.getElementById(
            "chatMessages"
        );


    if (!chatMessages) {

        return;

    }


    // =================================================
    // SHOW OLD QUESTION
    // =================================================

    chatMessages.innerHTML = `

        <div class="message user-message">

            <div class="message-label">
                You
            </div>

            <div class="message-content">

                ${escapeHtml(
        chat.question
    )}

            </div>

        </div>

    `;


    // =================================================
    // CREATE OLD SOURCES
    // =================================================

    const sourcesHtml =
        createSourcesHtml(
            chat.sources || []
        );


    // =================================================
    // SHOW OLD ANSWER
    // =================================================

    chatMessages.innerHTML += `

        <div class="message ai-message">

            <div class="message-label">
                GeoMind AI
            </div>

            <div class="message-content">

                ${formatAnswer(
        chat.answer
    )}

            </div>

            ${sourcesHtml}

        </div>

    `;


    scrollToBottom();
}


// =====================================================
// GET HISTORY
// =====================================================

function getHistory() {

    try {

        const data =
            localStorage.getItem(
                HISTORY_KEY
            );


        if (!data) {

            return [];

        }


        const history =
            JSON.parse(data);


        return Array.isArray(history)
            ? history
            : [];


    } catch (error) {

        console.error(
            "History read error:",
            error
        );

        return [];
    }
}


// =====================================================
// START APPLICATION
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadSelectedConversation();

    }
);