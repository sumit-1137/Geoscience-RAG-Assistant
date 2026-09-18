// =====================================================
// GEOMIND AI - CHAT HISTORY
// =====================================================

const HISTORY_KEY = "geomindHistory";


// =====================================================
// START
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadHistory();


        const clearButton =
            document.getElementById(
                "clearHistoryBtn"
            );


        if (clearButton) {

            clearButton.addEventListener(
                "click",
                clearHistory
            );

        }

    }
);


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
            "History error:",
            error
        );

        return [];
    }
}


// =====================================================
// LOAD HISTORY
// =====================================================

function loadHistory() {

    const history =
        getHistory();


    const list =
        document.getElementById(
            "historyList"
        );


    const empty =
        document.getElementById(
            "emptyState"
        );


    const count =
        document.getElementById(
            "conversationCount"
        );


    if (
        !list ||
        !empty ||
        !count
    ) {

        return;

    }


    list.innerHTML = "";


    // =================================================
    // COUNT
    // =================================================

    count.textContent =
        history.length === 1
            ? "1 conversation"
            : history.length +
            " conversations";


    // =================================================
    // EMPTY
    // =================================================

    if (history.length === 0) {

        empty.style.display =
            "block";

        list.style.display =
            "none";

        return;
    }


    // =================================================
    // SHOW LIST
    // =================================================

    empty.style.display =
        "none";

    list.style.display =
        "flex";


    // Newest first

    for (
        let i = history.length - 1;
        i >= 0;
        i--
    ) {

        const card =
            createHistoryCard(
                history[i],
                i
            );


        list.appendChild(card);

    }
}


// =====================================================
// CREATE HISTORY CARD
// =====================================================

function createHistoryCard(
    chat,
    index
) {

    const card =
        document.createElement(
            "div"
        );


    card.className =
        "history-card";


    const question =
        chat.question ||
        "Untitled conversation";


    const answer =
        chat.answer ||
        "No answer available.";


    const timestamp =
        chat.timestamp ||
        chat.date;


    let dateText =
        "Unknown date";


    if (timestamp) {

        const date =
            new Date(timestamp);


        if (
            !isNaN(
                date.getTime()
            )
        ) {

            dateText =
                date.toLocaleString(
                    "en-IN",
                    {
                        day: "numeric",
                        month: "short",
                        year: "numeric",
                        hour: "numeric",
                        minute: "2-digit",
                        hour12: true
                    }
                );

        }
    }


    card.innerHTML = `

        <div class="history-card-header">

            <div class="history-question">

                <div class="history-question-icon">

                    <i class="fa-regular fa-message"></i>

                </div>

                <div>

                    <h3>
                        ${escapeHtml(question)}
                    </h3>

                </div>

            </div>


            <div class="history-date">

                ${escapeHtml(dateText)}

            </div>

        </div>


        <div class="history-answer">

            ${escapeHtml(
        removeHtml(answer)
    )}

        </div>


        <div class="history-actions">

            <button
                class="view-btn"
                onclick="viewConversation(${index})">

                <i class="fa-regular fa-eye"></i>

                View

            </button>


            <button
                class="delete-btn"
                onclick="deleteConversation(${index})">

                <i class="fa-regular fa-trash-can"></i>

                Delete

            </button>

        </div>

    `;


    return card;
}


// =====================================================
// VIEW CONVERSATION
// =====================================================

function viewConversation(index) {

    const history =
        getHistory();


    if (!history[index]) {

        return;

    }


    window.location.href =
        "index.html?history=" +
        encodeURIComponent(index);
}


// =====================================================
// DELETE CONVERSATION
// =====================================================

function deleteConversation(index) {

    const history =
        getHistory();


    if (!history[index]) {

        return;

    }


    const confirmed =
        confirm(
            "Delete this conversation?"
        );


    if (!confirmed) {

        return;

    }


    history.splice(
        index,
        1
    );


    localStorage.setItem(
        HISTORY_KEY,
        JSON.stringify(history)
    );


    loadHistory();
}


// =====================================================
// CLEAR ALL HISTORY
// =====================================================

function clearHistory() {

    const history =
        getHistory();


    if (history.length === 0) {

        return;

    }


    const confirmed =
        confirm(
            "Are you sure you want to clear all chat history?"
        );


    if (!confirmed) {

        return;

    }


    localStorage.removeItem(
        HISTORY_KEY
    );


    loadHistory();
}


// =====================================================
// REMOVE HTML
// =====================================================

function removeHtml(value) {

    const div =
        document.createElement(
            "div"
        );


    div.innerHTML =
        value == null
            ? ""
            : String(value);


    return (
        div.textContent ||
        div.innerText ||
        ""
    );
}


// =====================================================
// ESCAPE HTML
// =====================================================

function escapeHtml(value) {

    const div =
        document.createElement(
            "div"
        );


    div.textContent =
        value == null
            ? ""
            : String(value);


    return div.innerHTML;
}