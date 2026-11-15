package com.example.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object StoreConstants {
    const val WHATSAPP_NUMBER = "601126238907"
    const val R2_BASE_URL = "https://order.itworldjersey.store/"
    const val STORE_URL = "https://itworldjersey.store/"
    const val VIDEOS_URL = "https://videos.itworldjersey.store/"
    const val LIVE_URL = "https://itworldjersey.store/live/"
    const val YOUTUBE_LIVE_EMBED_URL = "https://www.youtube-nocookie.com/embed/FRqJ9GkkbNE?playsinline=1&rel=0"
    const val YOUTUBE_LIVE_WATCH_URL = "https://www.youtube.com/watch?v=FRqJ9GkkbNE"
    const val AI_CHAT_API_URL = "https://ai.itworldjersey.store/api/chat"
    const val LIVE_CHAT_API_BASE = "https://chat.itworldjersey.store/api/live-chat"
    const val CONTACT_EMAIL = "contact@itworldjersey.store"
    const val SALES_EMAIL = "sales@itworldjersey.store"
    const val SUPPORT_EMAIL = "support@itworldjersey.store"
    const val LEGAL_EMAIL = "legal@itworldjersey.store"
    const val PRIVACY_EMAIL = "privacy@itworldjersey.store"
    const val GOOGLE_MAPS_URL = "https://maps.google.com/?q=No.+18+Lebuh+Pudu+Kuala+Lumpur"
    const val SSM_INFO_URL = "https://www.ssm-einfo.my/"
}

@Serializable
data class RawCatalogItem(
    val key: String,
    val n: Int,
    val variant: String = "",
    val label: String,
    val category: String
)

data class JerseyProduct(
    val id: Int,
    val key: String,
    val designNumber: Int,
    val variant: String,
    val label: String,
    val category: String,
    val imageUrl: String,
    val price: Double? = null
) {
    val productId: String
        get() {
            val regex = Regex("""Product\s*(\d{4}[A-Z]?)(?:\.webp)?""", RegexOption.IGNORE_CASE)
            val fromKey = regex.find(key)?.groupValues?.getOrNull(1)?.uppercase()
            if (!fromKey.isNullOrBlank()) return fromKey
            val numRegex = Regex("""(\d{4}[A-Z]?)""", RegexOption.IGNORE_CASE)
            return numRegex.find(label)?.groupValues?.getOrNull(1)?.uppercase() ?: ""
        }

    val productName: String
        get() {
            val pid = productId
            return if (pid.isNotBlank()) {
                "Product " + pid.replace(Regex("""^(\d{4})([A-Z])$"""), "$1 $2")
            } else {
                label
            }
        }

    val priceDisplay: String
        get() = if (price == null) "Price on request" else "RM %.2f".format(price)
}

fun encodeR2Key(key: String): String {
    val encoded = key.split("/").joinToString("/") { segment ->
        URLEncoder.encode(segment, StandardCharsets.UTF_8.name()).replace("+", "%20")
    }
    return StoreConstants.R2_BASE_URL + encoded
}

data class QuickCategoryChip(
    val title: String,
    val emoji: String,
    val searchFilter: String = ""
)

val HOME_CATEGORY_STRIP = listOf(
    QuickCategoryChip("All Jerseys", "👕"),
    QuickCategoryChip("Club Teams", "⚽"),
    QuickCategoryChip("National", "🌍"),
    QuickCategoryChip("Premier League", "🏆"),
    QuickCategoryChip("La Liga", "🔵"),
    QuickCategoryChip("Serie A", "🔴"),
    QuickCategoryChip("Bundesliga", "⚫"),
    QuickCategoryChip("Ligue 1", "🟡"),
    QuickCategoryChip("Kids Jerseys", "👕"),
    QuickCategoryChip("Accessories", "🧢")
)

enum class JerseyCollarStyle(val id: String, val label: String) {
    CLASSIC_V_NECK("classic", "Classic V-neck"),
    ROUND_NECK("round", "Round neck"),
    POLO_COLLAR("collar", "Polo collar")
}

data class ColorPreset(
    val name: String,
    val emoji: String,
    val primaryColor: Color,
    val accentColor: Color,
    val shortsColor: Color
)

val CUSTOM_COLOR_PRESETS = listOf(
    ColorPreset("Royal Gold", "⚽", Color(0xFF123E86), Color(0xFFFFCA28), Color(0xFF111827)),
    ColorPreset("Red White", "🔴", Color(0xFFD71920), Color(0xFFFFFFFF), Color(0xFF202020)),
    ColorPreset("Green", "🟢", Color(0xFF087F55), Color(0xFFFFFFFF), Color(0xFF064E3B)),
    ColorPreset("Mono", "⚪", Color(0xFFF4F4F5), Color(0xFF111827), Color(0xFF111827)),
    ColorPreset("Neon", "🟣", Color(0xFF6D28D9), Color(0xFF67E8F9), Color(0xFF21113F))
)

data class LivePlatformItem(
    val name: String,
    val subtitle: String,
    val badgeText: String,
    val accentColor: Color,
    val url: String
)

val LIVE_PLATFORMS = listOf(
    LivePlatformItem("Watch on YouTube", "Open live stream on YouTube", "▶", Color(0xFFFF0033), StoreConstants.YOUTUBE_LIVE_WATCH_URL),
    LivePlatformItem("Watch on Facebook", "Link will be updated later", "f", Color(0xFF1877F2), StoreConstants.LIVE_URL),
    LivePlatformItem("Watch on Twitch", "Link will be updated later", "▣", Color(0xFF9146FF), StoreConstants.LIVE_URL),
    LivePlatformItem("Watch on TikTok", "Link will be updated later", "♪", Color(0xFF25F4EE), StoreConstants.LIVE_URL),
    LivePlatformItem("Watch on Telegram", "Link will be updated later", "➤", Color(0xFF229ED9), StoreConstants.LIVE_URL),
    LivePlatformItem("Watch on Instagram", "Instagram Live coming soon", "◎", Color(0xFFE1306C), StoreConstants.LIVE_URL)
)

data class DepartmentContact(
    val number: Int,
    val department: String,
    val email: String,
    val purpose: String
)

val DEPARTMENT_DIRECTORY = listOf(
    DepartmentContact(1, "Legal", "legal@itworldjersey.store", "Legal notices, terms and contractual matters"),
    DepartmentContact(2, "Privacy", "privacy@itworldjersey.store", "Personal-data and privacy requests"),
    DepartmentContact(3, "Abuse Reports", "abuse@itworldjersey.store", "Report abuse, impersonation or misuse"),
    DepartmentContact(4, "Security", "security@itworldjersey.store", "Security concerns and website bug reports"),
    DepartmentContact(5, "Compliance", "compliance@itworldjersey.store", "Compliance and regulatory enquiries"),
    DepartmentContact(6, "Customer Support", "support@itworldjersey.store", "Customer service and order assistance"),
    DepartmentContact(7, "General Help", "help@itworldjersey.store", "General help and questions"),
    DepartmentContact(8, "General Contact", "contact@itworldjersey.store", "General business enquiries"),
    DepartmentContact(9, "Website Administration", "admin@itworldjersey.store", "Website administration and operations"),
    DepartmentContact(10, "Sales / Custom Orders", "sales@itworldjersey.store", "Sales, quotations and custom jersey orders"),
    DepartmentContact(11, "Billing", "billing@itworldjersey.store", "Invoices, billing and account enquiries"),
    DepartmentContact(12, "Marketing", "marketing@itworldjersey.store", "Marketing, promotions and partnerships"),
    DepartmentContact(13, "Developer / Technical", "developer@itworldjersey.store", "Software development, API and system integration"),
    DepartmentContact(14, "Designer / Creative", "designer@itworldjersey.store", "Jersey artwork, logos, mockups and creative design"),
    DepartmentContact(15, "Welcome", "hello@itworldjersey.store", "Welcome messages and first-contact enquiries"),
    DepartmentContact(16, "Information", "info@itworldjersey.store", "Company and store information"),
    DepartmentContact(17, "Webmaster", "webmaster@itworldjersey.store", "Website ownership and web-related issues"),
    DepartmentContact(18, "Finance", "finance@itworldjersey.store", "Finance operations and financial enquiries"),
    DepartmentContact(19, "Media / Press", "media@itworldjersey.store", "Press, interviews and media partnerships"),
    DepartmentContact(20, "Data Protection Officer", "dpo@itworldjersey.store", "Data-protection officer and formal privacy matters")
)

data class FaqItem(
    val question: String,
    val answer: String
)

val COMPANY_FAQS = listOf(
    FaqItem(
        "How do I order?",
        "Choose a design in the catalog and tap \"Click to Order\" to open WhatsApp. Our team will confirm the current price and availability."
    ),
    FaqItem(
        "Are prices shown online?",
        "Prices are confirmed by chat where the catalog says \"Price on request\". Please wait for confirmation before making payment."
    ),
    FaqItem(
        "Can I request a custom design?",
        "Yes. Use the Custom Jersey Studio tab to create a concept and send a request through WhatsApp."
    ),
    FaqItem(
        "What about delivery?",
        "Delivery coverage, charges and estimated timing depend on the order. Confirm those details with our team before ordering."
    ),
    FaqItem(
        "Can I return or exchange a product?",
        "Please read the Refund Policy in Legal Policies and contact us before placing an order if you need clarification."
    )
)

data class LegalSection(
    val title: String,
    val paragraphs: List<String>
)

val LEGAL_SECTIONS = listOf(
    LegalSection(
        "1. Terms & Conditions",
        listOf(
            "These terms apply to enquiries and purchases made through IT WORLD JERSEY's website and supported communication channels. Product images, sample designs and descriptions help customers identify the style they are interested in; the final specifications must be confirmed with the store.",
            "Prices in quotations are in Malaysian Ringgit (RM), unless otherwise stated in writing. Availability, fabric, printing method, sizing, customization, quantities, shipping charges and delivery estimates are not final until confirmed in writing.",
            "Customers must review the final proof and confirm spelling, names, numbers, sizes, quantities, colours and other supplied details before production begins."
        )
    ),
    LegalSection(
        "2. Design-First Order Process",
        listOf(
            "1. Submit a design: Send your own artwork, reference image or clear design brief, together with garment type, sizes, quantities and contact details.",
            "2. Design review: Our team reviews the material and may request a clearer file or additional information.",
            "3. Proof and quotation: We confirm the proposed design, garment details, size breakdown, quantity, total price in RM and estimated delivery timeframe.",
            "4. Customer approval: The customer checks and approves the final proof and order summary in writing.",
            "5. Order acceptance: The order is accepted only when IT WORLD JERSEY confirms acceptance and provides agreed payment instructions."
        )
    ),
    LegalSection(
        "3. Refund, Cancellation & Returns",
        listOf(
            "Before placing an order or making payment, ask our team to confirm the final design, garment type, size breakdown, quantity, total price in RM, delivery estimate and customization details.",
            "If an item arrives damaged, defective, incorrect or materially different from confirmed specifications, contact us promptly with the order reference and clear supporting photos.",
            "For customized or made-to-order items, change-of-mind cancellation may not be possible after approval of the final design and start of production, without limiting statutory consumer rights."
        )
    ),
    LegalSection(
        "4. Product Quality & Warranty",
        listOf(
            "Customers should inspect their order promptly after delivery and contact us if there is a quality issue. Any express warranty or care instruction will be stated in the confirmed order documentation.",
            "Issues arising from normal wear, misuse, improper washing, unauthorized alteration or damage after delivery may not be covered."
        )
    ),
    LegalSection(
        "5. Intellectual Property & Trademark Notice",
        listOf(
            "IT WORLD JERSEY's own brand name, original logos, original artwork and content may be protected by intellectual property laws.",
            "Football club names, team badges, league marks, manufacturer logos and sponsor marks belong to their respective owners. Customers submitting artwork confirm they have necessary permissions and licences."
        )
    ),
    LegalSection(
        "6. Cookie & Storage Policy",
        listOf(
            "Essential storage or similar technologies may be used for basic app/service operation, chat sessions and security. Third-party services opened from the app, such as WhatsApp or YouTube, operate under their respective policies."
        )
    ),
    LegalSection(
        "7. Disclaimer",
        listOf(
            "Product previews, mock-ups, illustrations and reference images communicate a proposed design. Colours and appearance may vary between screens and physical production.",
            "Unless expressly stated in writing, IT WORLD JERSEY does not claim to be an official partner, licensee or representative of any football club, league, brand or manufacturer."
        )
    ),
    LegalSection(
        "8. Privacy Policy",
        listOf(
            "Depending on the service used, information may include display name, phone number, email address, chat messages, order details, design instructions and technical access logs.",
            "We do not sell personal information. For privacy requests, contact privacy@itworldjersey.store."
        )
    ),
    LegalSection(
        "9. Customer Rights & Service Commitment",
        listOf(
            "Customers are entitled to clear information about the product, customization, total price, delivery arrangements and applicable limitations before confirming an order. Nothing in these policies waives statutory rights under Malaysian consumer-protection law."
        )
    ),
    LegalSection(
        "10. Complaints & Dispute Resolution",
        listOf(
            "Contact legal@itworldjersey.store or official WhatsApp (+60 11-2623 8907) with your order reference and supporting information. This internal review does not prevent seeking assistance from Malaysian consumer tribunals or courts."
        )
    ),
    LegalSection(
        "11. Governing Law",
        listOf(
            "Business operator: MAC STYLE COLLECTION, registered in Malaysia with SSM, business registration no. 202303181279 (former reference CT0124374-P). Governed by the laws of Malaysia."
        )
    )
)
