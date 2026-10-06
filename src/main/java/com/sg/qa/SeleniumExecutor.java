package com.sg.qa;

import com.sg.ai.TestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SeleniumExecutor {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final Duration DEFAULT_WAIT =
            Duration.ofSeconds(10);

    private static final Duration PAGE_READY_WAIT =
            Duration.ofSeconds(10);

    private static final String INTERACTIVE_ELEMENTS =
            "a,button,input,select,textarea,[role='button'],[role='link']";

    /*
     * ============================================================
     * INTERNAL LOCATOR CANDIDATE
     * ============================================================
     */

    private static class LocatorCandidate {

        private final WebElement element;
        private final int score;
        private final String matchedBy;
        private final String matchedValue;

        private LocatorCandidate(
                WebElement element,
                int score,
                String matchedBy,
                String matchedValue) {

            this.element = element;
            this.score = score;
            this.matchedBy = matchedBy;
            this.matchedValue = matchedValue;
        }
    }

    /*
     * ============================================================
     * BROWSER START
     * ============================================================
     */

    public void startBrowser() {

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);

        wait = new WebDriverWait(
                driver,
                DEFAULT_WAIT
        );

        System.out.println(
                "Chrome browser started successfully."
        );
    }

    /*
     * ============================================================
     * OPEN WEBSITE
     * ============================================================
     */

    public void openWebsite(String website) {

        if (website == null || website.isBlank()) {
            throw new IllegalArgumentException(
                    "Website URL cannot be empty."
            );
        }

        driver.get(website);

        waitForPageReady();

        System.out.println(
                "Website opened: " + website
        );

        System.out.println(
                "Current URL: " + driver.getCurrentUrl()
        );
    }

    /*
     * ============================================================
     * PAGE READY WAIT
     * ============================================================
     */

    private void waitForPageReady() {

        if (driver == null) {
            return;
        }

        try {

            new WebDriverWait(
                    driver,
                    PAGE_READY_WAIT
            ).until(currentDriver -> {

                try {

                    Object readyState =
                            ((JavascriptExecutor) currentDriver)
                                    .executeScript(
                                            "return document.readyState"
                                    );

                    return "complete".equals(
                            String.valueOf(readyState)
                    );

                } catch (Exception e) {
                    return false;
                }
            });

        } catch (Exception e) {

            System.out.println(
                    "Page ready-state wait completed with timeout; continuing."
            );
        }
    }

    /*
     * ============================================================
     * WAIT FOR URL
     * ============================================================
     */

    private boolean waitForExpectedUrl(
            String expectedUrl) {

        if (expectedUrl == null || expectedUrl.isBlank()) {
            return false;
        }

        try {

            return new WebDriverWait(
                    driver,
                    DEFAULT_WAIT
            ).until(currentDriver -> {

                try {

                    String currentUrl =
                            currentDriver.getCurrentUrl();

                    return expectedUrl.equals(currentUrl);

                } catch (Exception e) {
                    return false;
                }
            });

        } catch (Exception e) {
            return false;
        }
    }

    /*
     * ============================================================
     * PAGE REFRESH
     * ============================================================
     */

    public void refreshPage() {

        if (driver == null) {

            throw new IllegalStateException(
                    "Browser is not started."
            );
        }

        System.out.println(
                "\n[ACTION] REFRESH_PAGE"
        );

        String currentUrlBeforeRefresh =
                driver.getCurrentUrl();

        System.out.println(
                "Current URL before refresh: "
                        + currentUrlBeforeRefresh
        );

        driver.navigate().refresh();

        waitForPageReady();

        System.out.println(
                "Page refreshed successfully."
        );

        System.out.println(
                "Current URL after refresh: "
                        + driver.getCurrentUrl()
        );
    }

    /*
     * ============================================================
     * LEGACY COMPATIBILITY METHOD
     * ============================================================
     */

    public void executeLoginTest(TestCase testCase) {

        System.out.println("\n================================");
        System.out.println(
                "Executing: " + testCase.getTitle()
        );
        System.out.println("================================");

        try {

            String website =
                    testCase.getWebsite();

            if (website == null || website.isBlank()) {

                throw new IllegalArgumentException(
                        "Website URL is missing."
                );
            }

            openWebsite(website);

            if (testCase.getUsername() != null) {

                typeByPurpose(
                        "username",
                        testCase.getUsername()
                );
            }

            if (testCase.getPassword() != null) {

                typeByPurpose(
                        "password",
                        testCase.getPassword()
                );
            }

            clickByPurpose("login");

            System.out.println(
                    "Login test executed successfully."
            );

        } catch (Exception e) {

            System.out.println(
                    "Login execution failed: "
                            + e.getMessage()
            );
        }
    }

    /*
     * ============================================================
     * GENERIC ACTION ELEMENT FINDER
     * ============================================================
     *
     * This finder is intended for ACTIONS such as:
     *
     * CLICK
     * TYPE
     * CLEAR
     * SELECT
     * etc.
     *
     * Validation actions have their own dedicated finder because
     * validation targets do not necessarily have to be interactive.
     */

    public WebElement findElementByPurpose(String purpose) {

        if (purpose == null || purpose.isBlank()) {

            throw new IllegalArgumentException(
                    "Element purpose cannot be empty."
            );
        }

        String originalPurpose =
                purpose.trim();

        String normalizedPurpose =
                normalizeText(originalPurpose);

        System.out.println(
                "\nSearching element for purpose: "
                        + purpose
        );

        List<WebElement> elements =
                driver.findElements(
                        By.cssSelector(
                                INTERACTIVE_ELEMENTS
                        )
                );

        List<LocatorCandidate> candidates =
                new ArrayList<>();

        for (WebElement element : elements) {

            try {

                if (!element.isDisplayed()
                        || !element.isEnabled()) {

                    continue;
                }

                int score =
                        calculateElementScore(
                                element,
                                normalizedPurpose
                        );

                if (score <= 0) {
                    continue;
                }

                String matchedBy =
                        determineMatchReason(
                                element,
                                normalizedPurpose
                        );

                String matchedValue =
                        getBestMatchedValue(
                                element,
                                normalizedPurpose
                        );

                candidates.add(
                        new LocatorCandidate(
                                element,
                                score,
                                matchedBy,
                                matchedValue
                        )
                );

            } catch (Exception ignored) {
                // Ignore stale/unavailable elements.
            }
        }

        if (!candidates.isEmpty()) {

            candidates.sort(
                    Comparator.comparingInt(
                            (LocatorCandidate candidate) ->
                                    candidate.score
                    ).reversed()
            );

            LocatorCandidate best =
                    candidates.get(0);

            System.out.println(
                    "Element selected using semantic locator scoring."
            );

            System.out.println(
                    "Match strategy: "
                            + best.matchedBy
            );

            System.out.println(
                    "Matched value: "
                            + best.matchedValue
            );

            System.out.println(
                    "Locator confidence score: "
                            + best.score
            );

            return best.element;
        }

        /*
         * Broad fallback for unusual controls.
         */

        WebElement fallback =
                findBroadPurposeMatch(
                        normalizedPurpose
                );

        if (fallback != null) {

            System.out.println(
                    "Element found using broad DOM fallback."
            );

            return fallback;
        }

        throw new RuntimeException(
                "Could not find visible element for purpose: "
                        + purpose
        );
    }

    /*
     * ============================================================
     * LOCATOR SCORING
     * ============================================================
     */

    private int calculateElementScore(
            WebElement element,
            String normalizedPurpose) {

        int score = 0;

        String tag =
                safeAttribute(
                        element,
                        "tagName"
                ).toLowerCase(Locale.ROOT);

        String id =
                normalizeText(
                        element.getAttribute("id")
                );

        String name =
                normalizeText(
                        element.getAttribute("name")
                );

        String type =
                normalizeText(
                        element.getAttribute("type")
                );

        String placeholder =
                normalizeText(
                        element.getAttribute("placeholder")
                );

        String ariaLabel =
                normalizeText(
                        element.getAttribute("aria-label")
                );

        String value =
                normalizeText(
                        element.getAttribute("value")
                );

        String text =
                normalizeText(
                        element.getText()
                );

        String role =
                normalizeText(
                        element.getAttribute("role")
                );

        String purposeWithoutRole =
                removeControlWords(
                        normalizedPurpose
                );

        /*
         * --------------------------------------------------------
         * Strong semantic matches
         * --------------------------------------------------------
         */

        if (text.equals(normalizedPurpose)) {
            score += 100;
        }

        if (!purposeWithoutRole.isBlank()
                && text.equals(purposeWithoutRole)) {

            score += 120;
        }

        if (ariaLabel.equals(normalizedPurpose)) {
            score += 95;
        }

        if (placeholder.equals(normalizedPurpose)) {
            score += 90;
        }

        if (value.equals(normalizedPurpose)) {
            score += 85;
        }

        if (name.equals(normalizedPurpose)) {
            score += 75;
        }

        if (id.equals(normalizedPurpose)) {
            score += 65;
        }

        /*
         * --------------------------------------------------------
         * Token-based semantic matching
         * --------------------------------------------------------
         */

        Set<String> purposeTokens =
                tokenize(purposeWithoutRole);

        if (!purposeTokens.isEmpty()) {

            score += tokenMatchScore(
                    text,
                    purposeTokens,
                    45
            );

            score += tokenMatchScore(
                    ariaLabel,
                    purposeTokens,
                    40
            );

            score += tokenMatchScore(
                    placeholder,
                    purposeTokens,
                    35
            );

            score += tokenMatchScore(
                    value,
                    purposeTokens,
                    35
            );

            score += tokenMatchScore(
                    name,
                    purposeTokens,
                    30
            );

            score += tokenMatchScore(
                    id,
                    purposeTokens,
                    25
            );
        }

        /*
         * --------------------------------------------------------
         * Control-type intelligence
         * --------------------------------------------------------
         */

        boolean loginLike =
                containsAny(
                        purposeWithoutRole,
                        "login",
                        "log in",
                        "sign in",
                        "signin"
                );

        boolean submitLike =
                containsAny(
                        purposeWithoutRole,
                        "submit",
                        "continue",
                        "proceed"
                );

        boolean requestedButton =
                normalizedPurpose.contains("button")
                        || normalizedPurpose.contains("btn");

        if (loginLike
                && ("button".equals(tag)
                || "submit".equals(type)
                || "button".equals(type))) {

            score += 100;
        }

        if (submitLike
                && ("button".equals(tag)
                || "submit".equals(type))) {

            score += 80;
        }

        if (requestedButton
                && "button".equals(tag)) {

            score += 75;
        }

        if (requestedButton
                && "submit".equals(type)) {

            score += 70;
        }

        if ("button".equals(role)
                || "link".equals(role)) {

            score += 20;
        }

        /*
         * --------------------------------------------------------
         * Interactive control preference
         * --------------------------------------------------------
         */

        if ("button".equals(tag)
                || "a".equals(tag)
                || "input".equals(tag)
                || "select".equals(tag)
                || "textarea".equals(tag)) {

            score += 10;
        }

        return score;
    }

    /*
     * ============================================================
     * TOKEN MATCH SCORE
     * ============================================================
     */

    private int tokenMatchScore(
            String candidateText,
            Set<String> purposeTokens,
            int weight) {

        if (candidateText == null
                || candidateText.isBlank()
                || purposeTokens.isEmpty()) {

            return 0;
        }

        Set<String> candidateTokens =
                tokenize(candidateText);

        if (candidateTokens.isEmpty()) {
            return 0;
        }

        int matched = 0;

        for (String token : purposeTokens) {

            if (candidateTokens.contains(token)) {
                matched++;
            }
        }

        if (matched == 0) {
            return 0;
        }

        double ratio =
                (double) matched
                        / purposeTokens.size();

        return (int) Math.round(
                weight * ratio
        );
    }

    /*
     * ============================================================
     * MATCH REASON
     * ============================================================
     */

    private String determineMatchReason(
            WebElement element,
            String normalizedPurpose) {

        String purposeWithoutRole =
                removeControlWords(
                        normalizedPurpose
                );

        String text =
                normalizeText(
                        element.getText()
                );

        String aria =
                normalizeText(
                        element.getAttribute("aria-label")
                );

        String placeholder =
                normalizeText(
                        element.getAttribute("placeholder")
                );

        String value =
                normalizeText(
                        element.getAttribute("value")
                );

        String name =
                normalizeText(
                        element.getAttribute("name")
                );

        String id =
                normalizeText(
                        element.getAttribute("id")
                );

        if (text.equals(normalizedPurpose)
                || text.equals(purposeWithoutRole)) {

            return "visible text";
        }

        if (aria.equals(normalizedPurpose)) {
            return "aria-label";
        }

        if (placeholder.equals(normalizedPurpose)) {
            return "placeholder";
        }

        if (value.equals(normalizedPurpose)) {
            return "value";
        }

        if (name.equals(normalizedPurpose)) {
            return "name";
        }

        if (id.equals(normalizedPurpose)) {
            return "id";
        }

        if (containsAny(
                purposeWithoutRole,
                "login",
                "log in",
                "sign in"
        )
                && (
                "button".equalsIgnoreCase(
                        element.getTagName()
                )
                        || "submit".equalsIgnoreCase(
                        element.getAttribute("type")
                ))) {

            return "semantic login/submit control";
        }

        return "semantic token match";
    }

    /*
     * ============================================================
     * BEST MATCHED VALUE
     * ============================================================
     */

    private String getBestMatchedValue(
            WebElement element,
            String normalizedPurpose) {

        String text =
                element.getText();

        if (text != null && !text.isBlank()) {
            return text;
        }

        String aria =
                element.getAttribute("aria-label");

        if (aria != null && !aria.isBlank()) {
            return aria;
        }

        String value =
                element.getAttribute("value");

        if (value != null && !value.isBlank()) {
            return value;
        }

        String name =
                element.getAttribute("name");

        if (name != null && !name.isBlank()) {
            return name;
        }

        String id =
                element.getAttribute("id");

        if (id != null && !id.isBlank()) {
            return id;
        }

        return normalizedPurpose;
    }

    /*
     * ============================================================
     * BROAD DOM FALLBACK
     * ============================================================
     */

    private WebElement findBroadPurposeMatch(
            String normalizedPurpose) {

        try {

            List<WebElement> elements =
                    driver.findElements(
                            By.xpath("//*")
                    );

            for (WebElement element : elements) {

                try {

                    if (!element.isDisplayed()
                            || !element.isEnabled()) {

                        continue;
                    }

                    String text =
                            normalizeText(
                                    element.getText()
                            );

                    if (text.equals(normalizedPurpose)) {

                        return element;
                    }

                } catch (Exception ignored) {
                    // Continue
                }
            }

        } catch (Exception ignored) {
            // Continue
        }

        return null;
    }

    /*
     * ============================================================
     * GENERIC TEXT VALIDATION FINDER
     * ============================================================
     *
     * IMPORTANT:
     *
     * This finder is intentionally separate from the action
     * locator finder.
     *
     * Validation targets can be:
     *
     * - div
     * - span
     * - p
     * - heading
     * - label
     * - table cell
     * - notification
     * - modal
     * - alert
     * - arbitrary visible container
     *
     * Therefore VERIFY_TEXT must NOT restrict itself to:
     *
     * a, button, input, select, textarea
     *
     * The expected text itself is the strongest locator signal.
     */

    private WebElement findElementForTextValidation(
            String purpose,
            String expectedText) {

        if (expectedText == null
                || expectedText.isBlank()) {

            throw new IllegalArgumentException(
                    "Expected text cannot be empty."
            );
        }

        String normalizedExpected =
                normalizeText(expectedText);

        String normalizedPurpose =
                normalizeText(purpose);

        System.out.println(
                "\nSearching visible DOM for expected text."
        );

        System.out.println(
                "Validation purpose: "
                        + purpose
        );

        System.out.println(
                "Expected text: "
                        + expectedText
        );

        List<WebElement> elements =
                driver.findElements(
                        By.xpath("//*")
                );

        List<LocatorCandidate> candidates =
                new ArrayList<>();

        for (WebElement element : elements) {

            try {

                if (!element.isDisplayed()) {
                    continue;
                }

                String actualText =
                        normalizeText(
                                element.getText()
                        );

                if (actualText.isBlank()) {
                    continue;
                }

                int score =
                        calculateTextValidationScore(
                                element,
                                normalizedPurpose,
                                normalizedExpected,
                                actualText
                        );

                if (score <= 0) {
                    continue;
                }

                String matchedBy =
                        determineTextValidationMatchReason(
                                element,
                                normalizedPurpose,
                                normalizedExpected,
                                actualText
                        );

                candidates.add(
                        new LocatorCandidate(
                                element,
                                score,
                                matchedBy,
                                element.getText()
                        )
                );

            } catch (Exception ignored) {
                // Ignore stale/unavailable elements.
            }
        }

        if (!candidates.isEmpty()) {

            candidates.sort(
                    Comparator
                            .comparingInt(
                                    (LocatorCandidate candidate) ->
                                            candidate.score
                            )
                            .reversed()
                            .thenComparingInt(
                                    candidate ->
                                            textLength(
                                                    candidate.element
                                                            .getText()
                                            )
                            )
            );

            LocatorCandidate best =
                    candidates.get(0);

            System.out.println(
                    "Validation element selected using text-aware scoring."
            );

            System.out.println(
                    "Match strategy: "
                            + best.matchedBy
            );

            System.out.println(
                    "Matched value: "
                            + compactText(
                                    best.matchedValue
                            )
            );

            System.out.println(
                    "Validation locator confidence score: "
                            + best.score
            );

            return best.element;
        }

        /*
         * If a specific DOM element cannot be selected, the final
         * fallback is the visible body text. This is intentionally
         * used only for validation because validation is concerned
         * with whether the expected business text exists on the
         * rendered page.
         */

        System.out.println(
                "No specific text container matched strongly."
        );

        System.out.println(
                "Trying rendered page text as validation fallback."
        );

        String bodyText =
                normalizeText(
                        driver.findElement(
                                By.tagName("body")
                        ).getText()
                );

        if (bodyText.contains(normalizedExpected)) {

            System.out.println(
                    "Expected text found in rendered page text."
            );

            return driver.findElement(
                    By.tagName("body")
            );
        }

        throw new RuntimeException(
                "Expected text was not found in the visible DOM. "
                        + "Expected: "
                        + expectedText
        );
    }

    /*
     * ============================================================
     * TEXT VALIDATION SCORE
     * ============================================================
     */

    private int calculateTextValidationScore(
            WebElement element,
            String normalizedPurpose,
            String normalizedExpected,
            String actualText) {

        int score = 0;

        String tag =
                normalizeText(
                        safeAttribute(
                                element,
                                "tagName"
                        )
                );

        /*
         * --------------------------------------------------------
         * Expected text matching
         * --------------------------------------------------------
         */

        if (actualText.equals(normalizedExpected)) {

            score += 500;

        } else if (actualText.contains(normalizedExpected)) {

            score += 400;
        }

        /*
         * --------------------------------------------------------
         * Purpose matching
         * --------------------------------------------------------
         */

        if (!normalizedPurpose.isBlank()) {

            if (actualText.equals(normalizedPurpose)) {
                score += 180;
            }

            if (actualText.contains(normalizedPurpose)) {
                score += 100;
            }

            Set<String> purposeTokens =
                    tokenize(
                            removeControlWords(
                                    normalizedPurpose
                            )
                    );

            score += tokenMatchScore(
                    actualText,
                    purposeTokens,
                    60
            );
        }

        /*
         * --------------------------------------------------------
         * Prefer meaningful text containers.
         *
         * We do NOT require these tags because the architecture
         * must remain generic.
         * --------------------------------------------------------
         */

        if ("div".equals(tag)
                || "span".equals(tag)
                || "p".equals(tag)
                || "section".equals(tag)
                || "article".equals(tag)
                || "header".equals(tag)
                || "footer".equals(tag)
                || "li".equals(tag)
                || "td".equals(tag)
                || "th".equals(tag)
                || "label".equals(tag)
                || "h1".equals(tag)
                || "h2".equals(tag)
                || "h3".equals(tag)
                || "h4".equals(tag)
                || "h5".equals(tag)
                || "h6".equals(tag)) {

            score += 25;
        }

        /*
         * Avoid giving large document containers a major advantage
         * simply because they contain all page text.
         */

        int textLength =
                actualText.length();

        if (textLength > 1000) {
            score -= 80;
        } else if (textLength > 500) {
            score -= 40;
        }

        return Math.max(score, 0);
    }

    /*
     * ============================================================
     * TEXT VALIDATION MATCH REASON
     * ============================================================
     */

    private String determineTextValidationMatchReason(
            WebElement element,
            String normalizedPurpose,
            String normalizedExpected,
            String actualText) {

        if (actualText.equals(normalizedExpected)) {
            return "exact expected text";
        }

        if (actualText.contains(normalizedExpected)) {
            return "expected text contained in element";
        }

        if (!normalizedPurpose.isBlank()
                && actualText.equals(normalizedPurpose)) {

            return "validation purpose text";
        }

        if (!normalizedPurpose.isBlank()
                && actualText.contains(normalizedPurpose)) {

            return "validation purpose contained in element";
        }

        return "semantic validation text match";
    }

    /*
     * ============================================================
     * TEXT VALIDATION HELPERS
     * ============================================================
     */

    private int textLength(String value) {

        if (value == null) {
            return Integer.MAX_VALUE;
        }

        return value.length();
    }

    private String compactText(String value) {

        if (value == null) {
            return "";
        }

        String normalized =
                value
                        .replaceAll("\\s+", " ")
                        .trim();

        if (normalized.length() <= 150) {
            return normalized;
        }

        return normalized.substring(
                0,
                147
        ) + "...";
    }

    /*
     * ============================================================
     * TEXT PRESENCE WAIT
     * ============================================================
     *
     * Useful for dynamically rendered success/error messages.
     */

    private boolean waitForExpectedVisibleText(
            String expectedText) {

        if (expectedText == null
                || expectedText.isBlank()) {

            return false;
        }

        String normalizedExpected =
                normalizeText(expectedText);

        try {

            return new WebDriverWait(
                    driver,
                    DEFAULT_WAIT
            ).until(currentDriver -> {

                try {

                    WebElement body =
                            currentDriver.findElement(
                                    By.tagName("body")
                            );

                    String bodyText =
                            normalizeText(
                                    body.getText()
                            );

                    return bodyText.contains(
                            normalizedExpected
                    );

                } catch (Exception e) {

                    return false;
                }
            });

        } catch (Exception e) {

            return false;
        }
    }

    /*
     * ============================================================
     * TEXT NORMALIZATION
     * ============================================================
     */

    private String normalizeText(String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    /*
     * ============================================================
     * CONTROL WORD REMOVAL
     * ============================================================
     */

    private String removeControlWords(String value) {

        if (value == null || value.isBlank()) {
            return "";
        }

        return value
                .replaceAll(
                        "\\b(button|btn|field|input|textbox|"
                                + "text box|link|dropdown|"
                                + "drop down|checkbox|radio|"
                                + "element|control|message|"
                                + "text|notification|alert)\\b",
                        " "
                )
                .trim()
                .replaceAll("\\s+", " ");
    }

    /*
     * ============================================================
     * TOKENIZATION
     * ============================================================
     */

    private Set<String> tokenize(String value) {

        Set<String> tokens =
                new HashSet<>();

        if (value == null || value.isBlank()) {
            return tokens;
        }

        for (String token :
                value.split("\\s+")) {

            if (token.length() >= 2) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    /*
     * ============================================================
     * STRING HELPERS
     * ============================================================
     */

    private boolean containsAny(
            String value,
            String... options) {

        if (value == null || value.isBlank()) {
            return false;
        }

        for (String option : options) {

            if (value.contains(option)) {
                return true;
            }
        }

        return false;
    }

    private String safeAttribute(
            WebElement element,
            String attribute) {

        if ("tagName".equals(attribute)) {

            try {
                return element.getTagName();
            } catch (Exception e) {
                return "";
            }
        }

        try {

            String value =
                    element.getAttribute(attribute);

            return value == null ? "" : value;

        } catch (Exception e) {
            return "";
        }
    }

    /*
     * ============================================================
     * GENERIC ACTION: TYPE
     * ============================================================
     */

    public void typeByPurpose(
            String purpose,
            String value) {

        System.out.println(
                "\n[ACTION] TYPE"
        );

        System.out.println(
                "Target: " + purpose
        );

        WebElement element =
                findElementByPurpose(purpose);

        element.clear();

        element.sendKeys(
                value == null ? "" : value
        );

        System.out.println(
                "Text entered successfully."
        );
    }

    /*
     * ============================================================
     * GENERIC ACTION: CLICK
     * ============================================================
     */

    public void clickByPurpose(String purpose) {

        System.out.println(
                "\n[ACTION] CLICK"
        );

        System.out.println(
                "Target: " + purpose
        );

        String urlBeforeClick =
                driver.getCurrentUrl();

        WebElement element =
                findElementByPurpose(purpose);

        WebElement clickableElement =
                wait.until(
                        ExpectedConditions.elementToBeClickable(
                                element
                        )
                );

        scrollIntoView(clickableElement);

        boolean clickExecuted = false;

        try {

            clickableElement.click();

            clickExecuted = true;

            System.out.println(
                    "Native Selenium click executed."
            );

        } catch (Exception e) {

            System.out.println(
                    "Native click failed. Trying JavaScript click."
            );

            try {

                ((JavascriptExecutor) driver)
                        .executeScript(
                                "arguments[0].click();",
                                clickableElement
                        );

                clickExecuted = true;

                System.out.println(
                        "JavaScript click executed successfully."
                );

            } catch (Exception jsException) {

                throw new RuntimeException(
                        "CLICK failed using both Selenium and JavaScript. "
                                + "Purpose: " + purpose
                                + ". Error: "
                                + jsException.getMessage(),
                        jsException
                );
            }
        }

        if (!clickExecuted) {

            throw new RuntimeException(
                    "CLICK could not be executed. "
                            + "Purpose: " + purpose
            );
        }

        waitForPageReady();

        String urlAfterClick =
                driver.getCurrentUrl();

        if (!urlBeforeClick.equals(urlAfterClick)) {

            System.out.println(
                    "Navigation detected after click."
            );

            System.out.println(
                    "URL before click: "
                            + urlBeforeClick
            );

            System.out.println(
                    "URL after click: "
                            + urlAfterClick
            );

        } else {

            System.out.println(
                    "No URL change detected after click."
            );

            System.out.println(
                    "Click completed without navigation."
            );
        }

        System.out.println(
                "CLICK action completed successfully."
        );
    }

    /*
     * ============================================================
     * SCROLL ELEMENT INTO VIEW
     * ============================================================
     */

    private void scrollIntoView(WebElement element) {

        try {

            ((JavascriptExecutor) driver)
                    .executeScript(
                            "arguments[0].scrollIntoView({block:'center', inline:'center'});",
                            element
                    );

        } catch (Exception ignored) {
            // Continue with normal click.
        }
    }

    /*
     * ============================================================
     * GENERIC ACTION: CLEAR
     * ============================================================
     */

    public void clearByPurpose(String purpose) {

        System.out.println(
                "\n[ACTION] CLEAR"
        );

        WebElement element =
                findElementByPurpose(purpose);

        element.clear();

        System.out.println(
                "Element cleared successfully."
        );
    }

    /*
     * ============================================================
     * GENERIC ACTION: GET TEXT
     * ============================================================
     */

    public String getTextByPurpose(String purpose) {

        System.out.println(
                "\n[ACTION] GET_TEXT"
        );

        WebElement element =
                findElementByPurpose(purpose);

        String text =
                element.getText();

        System.out.println(
                "Retrieved text: " + text
        );

        return text;
    }

    /*
     * ============================================================
     * VALIDATION: TEXT
     * ============================================================
     *
     * VERIFY_TEXT is a business validation.
     *
     * It does NOT use the action locator strategy.
     *
     * Flow:
     *
     * 1. Wait for expected text to appear.
     * 2. Search the visible DOM.
     * 3. Select the best text container.
     * 4. Validate its rendered text.
     * 5. If needed, use rendered body text as final fallback.
     */

    public boolean verifyText(
            String purpose,
            String expectedText) {

        System.out.println(
                "\n[VALIDATION] VERIFY_TEXT"
        );

        if (expectedText == null
                || expectedText.isBlank()) {

            System.out.println(
                    "Expected text is empty."
            );

            return false;
        }

        /*
         * First wait for dynamic page content.
         */

        boolean textAppeared =
                waitForExpectedVisibleText(
                        expectedText
                );

        if (!textAppeared) {

            System.out.println(
                    "Expected text did not appear during validation wait."
            );

            System.out.println(
                    "Expected: "
                            + expectedText
            );

            return false;
        }

        WebElement element;

        try {

            element =
                    findElementForTextValidation(
                            purpose,
                            expectedText
                    );

        } catch (Exception e) {

            /*
             * Final rendered-page fallback.
             */

            String bodyText =
                    normalizeText(
                            driver.findElement(
                                    By.tagName("body")
                            ).getText()
                    );

            boolean fallbackResult =
                    bodyText.contains(
                            normalizeText(expectedText)
                    );

            System.out.println(
                    "Text validation fallback result: "
                            + (
                            fallbackResult
                                    ? "PASS"
                                    : "FAIL"
                    )
            );

            return fallbackResult;
        }

        String actualText =
                element.getText();

        String normalizedActual =
                normalizeText(actualText);

        String normalizedExpected =
                normalizeText(expectedText);

        boolean result =
                normalizedActual.contains(
                        normalizedExpected
                );

        System.out.println(
                "Expected: " + expectedText
        );

        System.out.println(
                "Actual: " + compactText(actualText)
        );

        System.out.println(
                "Validation result: "
                        + (result ? "PASS" : "FAIL")
        );

        return result;
    }

    /*
     * ============================================================
     * VALIDATION: URL
     * ============================================================
     */

    public boolean verifyUrl(String expectedUrl) {

        System.out.println(
                "\n[VALIDATION] VERIFY_URL"
        );

        System.out.println(
                "Expected: " + expectedUrl
        );

        String urlBeforeWait =
                driver.getCurrentUrl();

        System.out.println(
                "Current URL before wait: "
                        + urlBeforeWait
        );

        boolean result =
                waitForExpectedUrl(expectedUrl);

        String actualUrl =
                driver.getCurrentUrl();

        System.out.println(
                "Actual URL after wait: "
                        + actualUrl
        );

        System.out.println(
                "Validation result: "
                        + (result ? "PASS" : "FAIL")
        );

        return result;
    }

    /*
     * ============================================================
     * VALIDATION: TITLE
     * ============================================================
     */

    public boolean verifyTitle(String expectedTitle) {

        String actualTitle =
                driver.getTitle();

        boolean result =
                actualTitle.equals(expectedTitle);

        System.out.println(
                "\n[VALIDATION] VERIFY_TITLE"
        );

        System.out.println(
                "Expected: " + expectedTitle
        );

        System.out.println(
                "Actual: " + actualTitle
        );

        System.out.println(
                "Validation result: "
                        + (result ? "PASS" : "FAIL")
        );

        return result;
    }

    /*
     * ============================================================
     * LOCATOR-BASED ACTIONS
     * ============================================================
     */

    public WebElement findElement(By locator) {

        return wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        locator
                )
        );
    }

    public void click(By locator) {

        WebElement element =
                wait.until(
                        ExpectedConditions.elementToBeClickable(
                                locator
                        )
                );

        String urlBeforeClick =
                driver.getCurrentUrl();

        scrollIntoView(element);

        try {

            element.click();

            System.out.println(
                    "Native Selenium locator click executed."
            );

        } catch (Exception e) {

            System.out.println(
                    "Native locator click failed. "
                            + "Trying JavaScript click."
            );

            ((JavascriptExecutor) driver)
                    .executeScript(
                            "arguments[0].click();",
                            element
                    );

            System.out.println(
                    "JavaScript locator click executed."
            );
        }

        waitForPageReady();

        String urlAfterClick =
                driver.getCurrentUrl();

        if (!urlBeforeClick.equals(urlAfterClick)) {

            System.out.println(
                    "Navigation detected after locator click."
            );

            System.out.println(
                    "URL before click: "
                            + urlBeforeClick
            );

            System.out.println(
                    "URL after click: "
                            + urlAfterClick
            );

        } else {

            System.out.println(
                    "No URL change detected after locator click."
            );
        }

        System.out.println(
                "Locator CLICK action completed successfully."
        );
    }

    public void type(
            By locator,
            String value) {

        WebElement element =
                findElement(locator);

        element.clear();

        element.sendKeys(value);

        System.out.println(
                "Text entered successfully."
        );
    }

    /*
     * ============================================================
     * PAGE INFORMATION
     * ============================================================
     */

    public String getCurrentUrl() {

        return driver.getCurrentUrl();
    }

    public String getPageTitle() {

        return driver.getTitle();
    }

    /*
     * ============================================================
     * DOM INSPECTION
     * ============================================================
     */

    public void inspectPageElements() {

        System.out.println("\n================================");
        System.out.println("GENERIC DOM INSPECTION");
        System.out.println("================================");

        var elements =
                driver.findElements(
                        By.cssSelector(
                                "input, button, a, select, textarea"
                        )
                );

        System.out.println(
                "Total interactive elements found: "
                        + elements.size()
        );

        int index = 1;

        for (WebElement element : elements) {

            try {

                System.out.println(
                        "\nElement " + index++
                );

                System.out.println(
                        "Tag: "
                                + element.getTagName()
                );

                System.out.println(
                        "ID: "
                                + element.getAttribute("id")
                );

                System.out.println(
                        "Name: "
                                + element.getAttribute("name")
                );

                System.out.println(
                        "Type: "
                                + element.getAttribute("type")
                );

                System.out.println(
                        "Placeholder: "
                                + element.getAttribute(
                                        "placeholder"
                                )
                );

                System.out.println(
                        "Text: "
                                + element.getText()
                );

                System.out.println(
                        "ARIA Label: "
                                + element.getAttribute(
                                        "aria-label"
                                )
                );

            } catch (Exception ignored) {
                // Ignore unavailable elements.
            }
        }

        System.out.println(
                "\n================================"
        );

        System.out.println(
                "DOM INSPECTION COMPLETED"
        );

        System.out.println(
                "================================"
        );
    }

    /*
     * ============================================================
     * STRUCTURED PAGE SNAPSHOT
     * ============================================================
     */

    public PageSnapshot createPageSnapshot() {

        if (driver == null) {

            throw new IllegalStateException(
                    "Browser is not started."
            );
        }

        System.out.println(
                "\n================================"
        );

        System.out.println(
                "CREATING PAGE SNAPSHOT"
        );

        System.out.println(
                "================================"
        );

        String url =
                driver.getCurrentUrl();

        String title =
                driver.getTitle();

        List<PageSnapshot.ElementSnapshot> elements =
                new ArrayList<>();

        var webElements =
                driver.findElements(
                        By.cssSelector(
                                "input, button, a, select, textarea"
                        )
                );

        System.out.println(
                "Interactive elements found: "
                        + webElements.size()
        );

        for (WebElement element : webElements) {

            try {

                String tag =
                        element.getTagName();

                String id =
                        element.getAttribute("id");

                String name =
                        element.getAttribute("name");

                String type =
                        element.getAttribute("type");

                String placeholder =
                        element.getAttribute("placeholder");

                String text =
                        element.getText();

                String ariaLabel =
                        element.getAttribute(
                                "aria-label"
                        );

                String href =
                        element.getAttribute("href");

                boolean visible =
                        element.isDisplayed();

                boolean enabled =
                        element.isEnabled();

                PageSnapshot.ElementSnapshot snapshot =
                        new PageSnapshot.ElementSnapshot(
                                tag,
                                id,
                                name,
                                type,
                                placeholder,
                                text,
                                ariaLabel,
                                href,
                                visible,
                                enabled
                        );

                elements.add(snapshot);

            } catch (Exception ignored) {
                // Ignore unavailable elements.
            }
        }

        PageSnapshot pageSnapshot =
                new PageSnapshot(
                        url,
                        title,
                        elements
                );

        System.out.println(
                "Page snapshot created successfully."
        );

        System.out.println(
                "URL: " + url
        );

        System.out.println(
                "Title: " + title
        );

        System.out.println(
                "Snapshot elements: "
                        + elements.size()
        );

        System.out.println(
                "================================"
        );

        return pageSnapshot;
    }

    /*
     * ============================================================
     * BROWSER CLOSE
     * ============================================================
     */

    public void closeBrowser() {

        if (driver != null) {

            driver.quit();

            System.out.println(
                    "Chrome browser closed."
            );
        }
    }
}