// =====================================================
// GEOMIND AI - CHAT SCRIPT
// =====================================================

const RAG_API = "/api/rag";
const HISTORY_KEY = "geomindHistory";


// =====================================================
// QUESTION SUGGESTION
// =====================================================

function useQuestion(button) {

    const question = button.querySelector("span").innerText;

    const input = document.getElementById("questionInput");

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

    const question =
        input.value.trim();

    if (!question) {
        return;
    }


    const chatMessages =
        document.getElementById("chatMessages");


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


    // Clear input

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


    try {

        // =================================================
        // CALL SPRING BOOT
        // =================================================

        const response =
            await fetch(
                RAG_API +
                "?question=" +
                encodeURIComponent(question)
            );


        if (!response.ok) {

            throw new Error(
                "HTTP " + response.status
            );
        }


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
        // SOURCES
        // =================================================

        let sourcesHtml = "";


        if (
            data.sources &&
            data.sources.length > 0
        ) {

            sourcesHtml = `

                <div class="sources">

                    <div class="sources-title">

                        <i class="bi bi-book"></i>

                        <span>
                            Sources
                        </span>

                    </div>

                    <ul>
            `;


            data.sources.forEach(source => {

                sourcesHtml += `

                    <li>

                        ${escapeHtml(
                    source.fileName
                )}

                        |

                        Chunk
                        ${escapeHtml(
                    source.chunkIndex
                )}

                    </li>

                `;

            });


            sourcesHtml += `

                    </ul>

                </div>

            `;
        }


        // =================================================
        // AI ANSWER
        // =================================================

        const answer =
            data.answer ||
            "No answer was generated.";


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

            sources: data.sources || [],

            timestamp:
                new Date().toISOString()

        });


        scrollToBottom();


    } catch (error) {

        console.error(
            "RAG API Error:",
            error
        );


        // Remove loading

        const loading =
            document.getElementById(loadingId);

        if (loading) {
            loading.remove();
        }


        // Show error

        chatMessages.innerHTML += `

            <div class="message ai-message">

                <div class="message-label">

                    GeoMind AI

                </div>

                <div class="message-content">

                    <strong>
                        Error
                    </strong>

                    <br>

                    Could not connect to the
                    GeoMind AI backend.

                    <br><br>

                    Please make sure Spring Boot
                    and Qdrant are running.

                </div>

            </div>

        `;


        scrollToBottom();
    }
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
// SCROLL
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
// FORMAT ANSWER
// =====================================================

function formatAnswer(answer) {

    if (!answer) {

        return "No answer was generated.";

    }


    return escapeHtml(answer)
        .replace(/\n/g, "<br>");
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


    // Remove history query parameter

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
    // SOURCES
    // =================================================

    let sourcesHtml = "";


    if (
        chat.sources &&
        chat.sources.length > 0
    ) {

        sourcesHtml = `

            <div class="sources">

                <div class="sources-title">

                    <i class="bi bi-book"></i>

                    <span>
                        Sources
                    </span>

                </div>

                <ul>

        `;


        chat.sources.forEach(source => {

            sourcesHtml += `

                <li>

                    ${escapeHtml(
                source.fileName
            )}

                    |

                    Chunk
                    ${escapeHtml(
                source.chunkIndex
            )}

                </li>

            `;

        });


        sourcesHtml += `

                </ul>

            </div>

        `;
    }


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
// START
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadSelectedConversation();

    }
);