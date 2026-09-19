const API_URL = "/api/documents";


// =====================================================
// LOAD DOCUMENTS
// =====================================================

async function loadDocuments() {

    const container =
        document.getElementById("documentsContainer");

    if (!container) {
        return;
    }


    container.innerHTML = `

        <div class="loading-state">

            <div
                class="spinner-border"
                role="status">
            </div>

            <p>
                Loading documents...
            </p>

        </div>

    `;


    try {

        const response =
            await fetch(API_URL);


        if (!response.ok) {

            throw new Error(
                "HTTP " + response.status
            );

        }


        const documents =
            await response.json();


        document.getElementById(
            "totalDocuments"
        ).textContent =
            documents.length;


        displayDocuments(documents);


    } catch (error) {

        console.error(
            "Load documents error:",
            error
        );


        container.innerHTML = `

            <div class="alert alert-danger">

                Failed to load documents.

                <br>

                <small>
                    Check whether Spring Boot is running.
                </small>

            </div>

        `;

    }

}


// =====================================================
// DISPLAY DOCUMENTS
// =====================================================

function displayDocuments(documents) {

    const container =
        document.getElementById(
            "documentsContainer"
        );


    if (!documents || documents.length === 0) {

        container.innerHTML = `

            <div class="empty-state">

                <i class="bi bi-folder2-open"></i>

                <p>
                    No documents found.
                </p>

            </div>

        `;

        return;

    }


    let html =
        `<div class="document-grid">`;


    documents.forEach(doc => {

        html += `

            <div class="document-item">


                <div class="document-title">


                    <div class="document-icon">

                        <i class="bi bi-file-earmark-pdf"></i>

                    </div>


                    <div>

                        <h6>
                            ${escapeHtml(
            doc.title
        )}
                        </h6>


                        <span class="document-file">

                            ${escapeHtml(
            doc.fileName ||
            "PDF document"
        )}

                        </span>

                    </div>


                </div>



                <div class="document-description">

                    ${escapeHtml(
            doc.description ||
            "No description provided."
        )}

                </div>



                <div class="document-actions">


                    <button
                        class="delete-btn"
                        onclick="deleteDocument(${doc.id})">

                        <i class="bi bi-trash"></i>

                        &nbsp; Delete

                    </button>


                </div>


            </div>

        `;

    });


    html += `</div>`;


    container.innerHTML =
        html;

}


// =====================================================
// PDF UPLOAD
// =====================================================

const documentForm =
    document.getElementById(
        "documentForm"
    );


if (documentForm) {

    documentForm.addEventListener(
        "submit",
        uploadDocument
    );

}


// =====================================================
// UPLOAD FUNCTION
// =====================================================

async function uploadDocument(event) {

    event.preventDefault();


    const title =
        document.getElementById(
            "title"
        ).value.trim();


    const description =
        document.getElementById(
            "description"
        ).value.trim();


    const fileInput =
        document.getElementById(
            "pdfFile"
        );


    const file =
        fileInput.files[0];


    // =================================================
    // VALIDATION
    // =================================================

    if (!title) {

        alert(
            "Please enter a document title."
        );

        return;

    }


    if (!file) {

        alert(
            "Please select a PDF file."
        );

        return;

    }


    const isPdf =
        file.type === "application/pdf" ||
        file.name
            .toLowerCase()
            .endsWith(".pdf");


    if (!isPdf) {

        alert(
            "Only PDF files are allowed."
        );

        return;

    }



    // =================================================
    // FORM DATA
    // =================================================

    const formData =
        new FormData();


    formData.append(
        "file",
        file
    );


    formData.append(
        "title",
        title
    );


    formData.append(
        "description",
        description
    );



    const submitButton =
        document.querySelector(
            "#documentForm button[type='submit']"
        );


    try {

        submitButton.disabled =
            true;


        submitButton.innerHTML = `

            <span
                class="spinner-border spinner-border-sm">
            </span>

            &nbsp; Processing PDF...

        `;


        console.log(
            "Uploading PDF:",
            file.name
        );


        // =================================================
        // SPRING BOOT
        // =================================================

        const response =
            await fetch(
                "/api/documents/upload",
                {
                    method: "POST",
                    body: formData
                }
            );


        console.log(
            "Upload HTTP status:",
            response.status
        );


        if (!response.ok) {

            const errorText =
                await response.text();


            throw new Error(
                errorText ||
                "Upload failed"
            );

        }


        // =================================================
        // RESPONSE
        // =================================================

        const contentType =
            response.headers.get(
                "content-type"
            );


        let result;


        if (
            contentType &&
            contentType.includes(
                "application/json"
            )
        ) {

            result =
                await response.json();

        } else {

            result =
                await response.text();

        }


        console.log(
            "Upload result:",
            result
        );


        // =================================================
        // RESET
        // =================================================

        documentForm.reset();


        await loadDocuments();


        alert(
            "PDF uploaded successfully."
        );


    } catch (error) {

        console.error(
            "PDF upload error:",
            error
        );


        alert(
            "PDF upload failed.\n\n" +
            error.message
        );


    } finally {

        submitButton.disabled =
            false;


        submitButton.innerHTML = `

            <i class="bi bi-cloud-upload"></i>

            &nbsp; Upload PDF

        `;

    }

}


// =====================================================
// DELETE DOCUMENT
// =====================================================

async function deleteDocument(id) {

    const confirmed =
        confirm(
            "Delete this document?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_URL}/${id}`,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {

            throw new Error(
                "HTTP " +
                response.status
            );

        }


        await loadDocuments();


    } catch (error) {

        console.error(
            "Delete error:",
            error
        );


        alert(
            "Failed to delete document."
        );

    }

}


// =====================================================
// ESCAPE HTML
// =====================================================

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }


    return String(value)

        .replaceAll(
            "&",
            "&amp;"
        )

        .replaceAll(
            "<",
            "&lt;"
        )

        .replaceAll(
            ">",
            "&gt;"
        )

        .replaceAll(
            '"',
            "&quot;"
        )

        .replaceAll(
            "'",
            "&#039;"
        );

}


// =====================================================
// START
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadDocuments();

    }
);