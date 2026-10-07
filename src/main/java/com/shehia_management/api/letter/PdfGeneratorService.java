package com.shehia_management.api.letter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.annotation.PostConstruct;
import com.shehia_management.api.identity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    // =========================================================
    // LETTERHEAD LOGO
    // =========================================================
    // Loaded once at startup from the classpath (static/images/smz.png)
    // and kept as a base64 string, which every template embeds directly
    // via a data: URI (see ${logoBase64} below).
    //
    // This replaces the old relative <img src="images/smz.png">. A
    // relative path only ever resolves when something is actually
    // serving that exact path relative to wherever the HTML is being
    // interpreted from - which is neither true of the React preview
    // (wrong origin entirely - it's the frontend's own dev server/host,
    // not the backend) nor reliably true of the PDF renderer once this
    // app is packaged as a jar (there's no more src/main/resources on
    // disk to find). A data: URI has no such dependency and works
    // identically in both places.
    private String logoBase64;

    @PostConstruct
    private void loadLogo() {
        try {
            byte[] bytes = new ClassPathResource("static/images/smz.png").getInputStream().readAllBytes();
            this.logoBase64 = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.err.println("Warning: could not load letterhead logo (static/images/smz.png): " + e.getMessage());
            this.logoBase64 = "";
        }
    }

    // =========================================================
    // OFFICE-LEVEL DEFAULTS
    // =========================================================
    // Every template's header/footer reads ${shehaName} and
    // ${shehiaPhone} for the OFFICE's signing officer and contact
    // number — not the resident's own details. These are configurable
    // per Shehia deployment.
    //
    // Add to application.properties (defaults below if omitted):
    //   app.office.sheha-name=Khamis Juma Ali
    //   app.office.phone=+255 770 000 000
    // =========================================================

    @Value("${app.office.sheha-name:Sheha wa Shehia}")
    private String defaultShehaName;

    @Value("${app.office.phone:}")
    private String defaultOfficePhone;

    /**
     * Generate fully populated HTML for the selected letter template.
     */
    public String generateLetterHtml(LetterApplication letter) {

        // If an admin has hand-edited this letter's content (via the
        // "Edit letter" control in the review screen), that saved HTML
        // is the source of truth from then on - both for what gets
        // previewed and for what gets baked into the PDF - until it is
        // reset. Skip regenerating from the template in that case.
        if (letter.getEditedContentHtml() != null && !letter.getEditedContentHtml().isBlank()) {
            return letter.getEditedContentHtml();
        }

        Context context = new Context();

        User resident = letter.getResident();

        // =========================================================
        // 1. BASIC DATA FROM RESIDENT AND LETTER APPLICATION
        // =========================================================

        context.setVariable("refNo", letter.getReferenceNumber());

        context.setVariable(
                "letterDate",
                letter.getSubmittedAt() != null
                        ? letter.getSubmittedAt()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : ""
        );

        context.setVariable(
                "citizenName",
                resident.getFullName() != null
                        ? resident.getFullName()
                        : ""
        );

        context.setVariable(
                "nationalId",
                resident.getZanId() != null
                        ? resident.getZanId()
                        : ""
        );

        context.setVariable(
                "houseNo",
                resident.getHouseNumber() != null
                        ? resident.getHouseNumber()
                        : ""
        );

        context.setVariable(
                "street",
                resident.getStreet() != null
                        ? resident.getStreet()
                        : ""
        );

        context.setVariable(
                "shehiaName",
                resident.getShehia() != null
                        ? resident.getShehia()
                        : ""
        );

        context.setVariable(
                "districtName",
                resident.getDistrict() != null
                        ? resident.getDistrict()
                        : ""
        );

        context.setVariable(
                "regionName",
                resident.getRegion() != null
                        ? resident.getRegion()
                        : ""
        );

        context.setVariable(
                "phoneNo",
                resident.getPhoneNumber() != null
                        ? resident.getPhoneNumber()
                        : ""
        );

        // Office contact number shown in every letterhead. Falls back to
        // the resident's own phone only if no office phone is configured,
        // to avoid ever showing a fully blank field.
        context.setVariable(
                "shehiaPhone",
                (defaultOfficePhone != null && !defaultOfficePhone.isBlank())
                        ? defaultOfficePhone
                        : (resident.getPhoneNumber() != null ? resident.getPhoneNumber() : "")
        );

        // Signing officer's name, printed in the signature block. Purely
        // typed/representational — no physical or cryptographic signing.
        context.setVariable("shehaName", defaultShehaName);

        // Letterhead logo, embedded inline - see loadLogo() above.
        context.setVariable("logoBase64", logoBase64);

        // Verification code, printed under "Scan Ku-verify" in every
        // letter. Deterministically derived from the reference number, so
        // it's identical every time this letter is previewed/regenerated,
        // and can be recomputed by the verification endpoint without
        // needing to store it anywhere.
        context.setVariable(
                "hashCode",
                LetterVerificationUtil.generateCode(letter.getReferenceNumber())
        );

        // =========================================================
        // 2. DYNAMIC FORM DATA
        // =========================================================

        if (letter.getDynamicFormData() != null
                && !letter.getDynamicFormData().isBlank()) {

            try {

                Map<String, Object> dynamicFields =
                        objectMapper.readValue(
                                letter.getDynamicFormData(),
                                new TypeReference<Map<String, Object>>() {}
                        );

                context.setVariables(dynamicFields);

            } catch (Exception e) {

                // Do not stop PDF generation because of invalid
                // dynamic JSON data.
                System.err.println(
                        "Warning: Failed to parse dynamicFormData: "
                                + e.getMessage()
                );
            }
        }

        // =========================================================
        // 3. SELECT THYMELEAF TEMPLATE
        // =========================================================

        String templateName = switch (letter.getLetterType()) {

            case RESIDENCE ->
                    "letters/residence";

            case BANK_KYC ->
                    "letters/bank";

            case CONDUCT ->
                    "letters/conduct";

            case GUARANTOR ->
                    "letters/guarantor";

            case TRAVEL_CLEARANCE ->
                    "letters/permit";

            case BUSINESS_PERMIT ->
                    "letters/event";
        };

        // =========================================================
        // 4. GENERATE HTML
        // =========================================================

        return templateEngine.process(
                templateName,
                context
        );
    }

    /**
     * Convert generated HTML into PDF bytes.
     *
     * The base URI allows OpenHTMLToPDF to find:
     * - Images
     * - CSS files
     * - Fonts
     * - Other static resources
     */
    public byte[] generateLetterPdf(LetterApplication letter) {

        String htmlContent = generateLetterHtml(letter);

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.useFastMode();

            // The logo is embedded as a base64 data: URI (see loadLogo()
            // above) and the QR code the same way, so nothing in the
            // generated HTML actually needs to be resolved against this
            // base URI anymore. It's kept only as a fallback for any
            // future template that references an on-disk/classpath
            // resource by relative path, resolved safely so it doesn't
            // depend on the JVM's working directory (which broke when
            // this ran as a packaged jar instead of from the project
            // root in an IDE).
            //
            // Parsed via jsoup rather than passed as a raw string to
            // builder.withHtmlContent(...): that method parses with a
            // strict XML parser requiring every tag to be explicitly
            // closed, which fresh Thymeleaf output satisfies but
            // browser-serialized HTML (from the letter-edit feature)
            // does not - browsers always write void elements like
            // <meta>, <br>, <img> without a self-closing slash. jsoup
            // parses tolerantly and we hand openhtmltopdf a DOM tree
            // instead, sidestepping strict well-formedness entirely.
            org.jsoup.nodes.Document jsoupDoc =
                    org.jsoup.Jsoup.parse(htmlContent, resolveBaseUri());
            org.w3c.dom.Document w3cDoc =
                    new org.jsoup.helper.W3CDom().fromJsoup(jsoupDoc);

            builder.withW3cDocument(w3cDoc, resolveBaseUri());

            builder.toStream(outputStream);

            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Kosa wakati wa kutengeneza PDF: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private String resolveBaseUri() {
        try {
            return new ClassPathResource("static/").getURI().toString();
        } catch (Exception e) {
            // Happens when resources are packed inside a jar rather than
            // exploded on disk (getURI() can't return a jar: URI here).
            // Harmless as long as no template relies on relative-path
            // resources - which, with the logo/QR now inlined as
            // base64, none of them do.
            return Paths.get(".").toAbsolutePath().toUri().toString();
        }
    }
}
