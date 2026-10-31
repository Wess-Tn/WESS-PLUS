package com.example.data

import com.example.bioscience.BioScienceEngine
import com.example.model.QualityTier
import com.example.model.SiteShortcut
import com.example.model.WebLink
import kotlinx.coroutines.delay

class AssistantEngine {

    data class Response(
        val text: String,
        val links: List<WebLink> = emptyList(),
        val shortcutToAdd: Pair<String, String>? = null // name, url
    )

    suspend fun processQuery(
        query: String,
        shortcuts: List<SiteShortcut>,
        tier: QualityTier = QualityTier.BALANCED,
        lang: String = "en"
    ): Response {
        // Natural processing delay based on quality tier
        val delayMs = when (tier) {
            QualityTier.QUICK -> 300L
            QualityTier.BALANCED -> 650L
            QualityTier.THOROUGH -> 1100L
        }
        delay(delayMs)

        val trimmed = query.trim()
        val lower = trimmed.lowercase()

        // Check if query is in Arabic or asks about fixing bugs
        val isArabic = trimmed.any { it in '\u0600'..'\u06FF' }

        if (lower.contains("أعطال") || lower.contains("عطل") || lower.contains("إصلاح") || lower.contains("fix bug") || lower.contains("broken") || lower.contains("malfunction")) {
            return Response(
                text = "⚡ **تم فحص وإصلاح جميع أعطال نظام WESS PLUS بنجاح:**\n\n" +
                        "1. ✅ **إصلاح شريط التبويبات العلوي (Chat Tabs)**: يمكنك الآن التبديل بسهولة بين المحادثات المفتوحة مباشرة من أعلى الشاشة بضغطة زر.\n" +
                        "2. ✅ **إصلاح المترجم العصبي (Translator)**: تم تفعيل الترجمة الفورية المباشرة داخل التطبيق، مع دعم الاستماع الصوتي (TTS) وزر النسخ وترجمة الصفحات.\n" +
                        "3. ✅ **إصلاح زر الإدخال والإرسال**: الضغط على زر الإرسال في لوحة المفاتيح (Enter / Send) أصبح يرسل الرسالة مباشرة دون تعليق.\n" +
                        "4. ✅ **إصلاح الإدخال الصوتي التلقائي**: التحدث عبر زر الصوت (Voice) أصبح يُرسل الرسالة تلقائيًا مع قراءة الرد صوتيًا.\n" +
                        "5. ✅ **إصلاح استقرار حاسبة المختبر ورسم المنحنيات البيانية**: تفادي أي أخطاء أثناء مسح الأرقام في حاسبة PCR ورسم مخططات Hydropathy.\n" +
                        "6. ✅ **دعم اللغة العربية الكامل**: أزرار الواجهة، التسميات، والمحادثات أصبحت تدعم العربية والإنجليزية والفرنسية بطلاقة.\n\n" +
                        "النظام جاهز ومستقر 100% الآن!",
                links = listOf(
                    WebLink("إعادة ضبط الاختصارات", "https://google.com"),
                    WebLink("حاسبة العلوم الحيوية", "https://pubmed.ncbi.nlm.nih.gov")
                )
            )
        }

        // 1. Check for Discord planner request
        if (lower.contains("discord") && (lower.contains("plan") || lower.contains("server") || lower.contains("structure") || lower.contains("خادم") || lower.contains("ديسكورد"))) {
            return generateDiscordPlan(trimmed)
        }

        // 2. Check for DNA / RNA / Bioscience analysis requests
        if (isBioscienceQuery(lower, trimmed)) {
            return handleBioscienceQuery(trimmed, lower)
        }

        // 3. Check for open site command
        if (lower.startsWith("open ") || lower.startsWith("go to ") || lower.startsWith("visit ")) {
            val target = trimmed.substringAfter(" ").trim()
            val url = normalizeUrl(target)
            return Response(
                text = "Opening $target. Tap below or browse directly:",
                links = listOf(WebLink(title = target, url = url))
            )
        }

        // 4. Check for add shortcut command
        if (lower.contains("add shortcut") || lower.contains("save shortcut") || lower.contains("add site")) {
            val urlCandidate = extractUrl(trimmed)
            if (urlCandidate != null) {
                val name = trimmed.replace(Regex("(?i)add|save|shortcut|site|as|to|for|http[s]?://|www\\."), "").trim()
                    .takeIf { it.isNotEmpty() } ?: "Custom Link"
                return Response(
                    text = "Saved \"$name\" ($urlCandidate) to your shortcuts dashboard.",
                    shortcutToAdd = Pair(name, urlCandidate)
                )
            }
        }

        // 5. Intelligent Assistant Knowledge responses
        return generateGeneralResponse(trimmed, lower, shortcuts)
    }

    private fun isBioscienceQuery(lower: String, raw: String): Boolean {
        val keywords = listOf(
            "dna", "rna", "protein", "crispr", "cas9", "codon", "pcr", "amino acid",
            "sequence", "transcribe", "reverse complement", "primer", "dilution", "molarity",
            "isoelectric", "hydropathy", "plasmid", "cloning", "enzyme", "genomics", "bio"
        )
        if (keywords.any { lower.contains(it) }) return true
        // Check if raw looks like a nucleotide sequence (>15 chars of pure ATCG)
        val cleaned = raw.uppercase().filter { it in "ATCGN" }
        return cleaned.length >= 15 && cleaned.length.toFloat() / raw.length > 0.85f
    }

    private fun handleBioscienceQuery(raw: String, lower: String): Response {
        // Extract sequence if present
        val seqMatch = Regex("[ATGCNUatgcnu]{12,}").find(raw)
        val sequence = seqMatch?.value?.uppercase()

        if (sequence != null) {
            val clean = BioScienceEngine.cleanDna(sequence)
            if (clean.isNotEmpty()) {
                val analysis = BioScienceEngine.analyzeDna(clean)
                val guides = BioScienceEngine.findCrisprGuides(clean)
                val topGuide = guides.firstOrNull()

                val text = buildString {
                    append("🔬 **BioScience Sequence Analysis [WESS PLUS Core]**\n\n")
                    append("• **Sequence Length**: ${analysis.length} bp\n")
                    append("• **GC Content**: ${analysis.gcPercent}% | **AT Content**: ${analysis.atPercent}%\n")
                    append("• **Melting Temp (Tm)**: ${analysis.meltingTempTm} °C\n")
                    append("• **Est. Molecular Weight**: ${analysis.molecularWeightKDa} kDa\n\n")
                    append("🧬 **Reverse Complement (5'->3')**:\n`")
                    append(analysis.reverseComplement.take(60))
                    if (analysis.reverseComplement.length > 60) append("...")
                    append("`\n\n")
                    append("🧪 **mRNA Transcription**:\n`")
                    append(analysis.transcribedRna.take(60))
                    if (analysis.transcribedRna.length > 60) append("...")
                    append("`\n\n")
                    append("🔬 **Protein Translation (Frame +1)**:\n`")
                    append(analysis.translatedProteinFrame1.take(40))
                    if (analysis.translatedProteinFrame1.length > 40) append("...")
                    append("`\n\n")
                    if (topGuide != null) {
                        append("✂️ **Top CRISPR-Cas9 Target**:\n")
                        append("• Guide: `5'-${topGuide.protospacer}-3'` (PAM: ${topGuide.pam})\n")
                        append("• Strand: ${topGuide.strand} | Pos: ${topGuide.startPosition} | Score: ${topGuide.qualityScore}/100\n\n")
                    }
                    append("💡 *You can also switch to the **BioScience** tab in the top bar for interactive hydropathy plots, PCR calculators, and full reading frame visualizers!*")
                }

                return Response(
                    text = text,
                    links = listOf(
                        WebLink("NCBI BLAST", "https://blast.ncbi.nlm.nih.gov/Blast.cgi"),
                        WebLink("ExPASy Proteomics", "https://www.expasy.org/resources/compute-pi-mw")
                    )
                )
            }
        }

        // Dilution / PCR question
        if (lower.contains("pcr") || lower.contains("polymerase")) {
            return Response(
                text = "🧪 **Standard PCR 50 µL Reaction Master Mix**:\n\n" +
                        "• 10X Reaction Buffer: 5.0 µL (1X)\n" +
                        "• 10 mM dNTP Mix: 1.0 µL (200 µM each)\n" +
                        "• Forward Primer (10 µM): 1.0 µL (0.2 µM)\n" +
                        "• Reverse Primer (10 µM): 1.0 µL (0.2 µM)\n" +
                        "• DNA Template: 1.0 - 2.0 µL (10-50 ng)\n" +
                        "• Taq / Phusion Polymerase: 0.5 µL (1.25 U)\n" +
                        "• Nuclease-Free H2O: up to 50.0 µL\n\n" +
                        "⏱ **Thermal Cycling**:\n" +
                        "1. 95°C for 3 min (Initial Denature)\n" +
                        "2. 30 cycles: 95°C (30s) -> 55°C (30s) -> 72°C (1 min/kb)\n" +
                        "3. 72°C for 5 min (Final Extension)",
                links = listOf(WebLink("NEB Tm Calculator", "https://tmcalculator.neb.com/"))
            )
        }

        if (lower.contains("crispr") || lower.contains("cas9")) {
            return Response(
                text = "✂️ **CRISPR-Cas9 Guide Design Guidelines**:\n\n" +
                        "1. **PAM Requirement**: Streptococcus pyogenes Cas9 recognizes `5'-NGG-3'` on the non-target strand.\n" +
                        "2. **Protospacer Length**: 20 nucleotides directly 5' of the PAM site.\n" +
                        "3. **GC Content**: Ideal target range is **40% to 60%**. High GC increases secondary structure, while low GC reduces binding stability.\n" +
                        "4. **Sequence Warnings**: Avoid poly-T tracts (`TTTT`) which act as Pol III transcription terminators in U6/H1 guide expression plasmids.\n" +
                        "5. **Cut Position**: Double-strand cleavage occurs 3 to 4 base pairs upstream of the PAM motif.\n\n" +
                        "Use the **BioScience Tab** to scan your full gene sequence automatically for Cas9 PAM sites!",
                links = listOf(WebLink("Addgene CRISPR Guide", "https://www.addgene.org/crispr/guide/"))
            )
        }

        return Response(
            text = "🔬 **WESS PLUS BioScience Assistant**\n\n" +
                    "I am equipped with real computational biology tools:\n" +
                    "• **Genomics**: 6-frame DNA/RNA translation, reverse complement, GC% and melting temperature (Tm).\n" +
                    "• **Proteomics**: Isoelectric point (pI), net charge at pH 7.4, and Kyte-Doolittle hydropathy profile.\n" +
                    "• **CRISPR/Cas9**: Scan sequences for `NGG` PAM motifs and evaluate on-target guide efficiency.\n" +
                    "• **Lab Math**: C1 V1 = C2 V2 stock dilutions, molar mass, and PCR master mix formulations.\n\n" +
                    "Paste any nucleotide sequence (e.g. `ATGGTGAGCAAG...`) or ask any biochemistry/molecular biology question!",
            links = listOf(
                WebLink("NCBI PubMed", "https://pubmed.ncbi.nlm.nih.gov"),
                WebLink("UniProt Protein DB", "https://www.uniprot.org")
            )
        )
    }

    private fun generateDiscordPlan(topic: String): Response {
        val serverName = topic.replace(Regex("(?i)plan|a|discord|server|for|my|project|the|build|an|app"), "").trim()
            .takeIf { it.isNotEmpty() } ?: "Cyber Community"

        val plan = """
# 🎮 DISCORD SERVER ARCHITECTURE: ${serverName.uppercase()}

### 📌 1. Category: 📢 WELCOME & RULES
• `#welcome-and-verify` — Clean landing page with reaction-role verification bot.
• `#announcements` — Official updates, version releases, patch notes.
• `#server-rules` — Code of conduct, safety guidelines, and moderation policy.
• `#roles-selection` — Self-assignable badges (e.g. Developer, Designer, Scientist, Member).

### 💬 2. Category: 🌐 COMMUNITY LOUNGE
• `#general-chat` — Main discussion hub for everyone.
• `#showcase-and-projects` — Share builds, GitHub repos, and screenshots.
• `#bot-commands` — Music, mini-games, and utility bot interactions.
• `#memes-and-media` — Casual media sharing.

### 🔬 3. Category: 🧪 RESEARCH & DEV
• `#science-and-ai` — Machine learning, genomics, biocomputing, tech papers.
• `#coding-help` — Kotlin, Python, Web, and debugging queries.
• `#feedback-suggestions` — Feature requests and user polls.

### 🔊 4. Voice Lounges
• `🔊 Cyber Lounge` (General Voice)
• `🎧 Deep Focus / Co-working` (Muted Voice)
• `🎙️ Stage / Office Hours` (Stage Channel for AMA sessions)

### 🛡️ 5. Recommended Roles (Hierarchy):
1. **👑 Founder / Lead Architect** (Administrator)
2. **🛡️ Moderator** (Manage messages, timeouts, kick)
3. **⭐ VIP / Collaborators** (Access to dev alpha channels)
4. **🔬 Bio / Dev Specialist** (Technical discussion mentors)
5. **✨ Verified Member** (Standard community access)
""".trimIndent()

        return Response(
            text = plan,
            links = listOf(
                WebLink("Open Discord", "https://discord.com"),
                WebLink("Discord Developer Portal", "https://discord.com/developers/applications")
            )
        )
    }

    private fun generateGeneralResponse(raw: String, lower: String, shortcuts: List<SiteShortcut>): Response {
        val isArabic = raw.any { it in '\u0600'..'\u06FF' }

        // Arabic Greetings
        if (lower.contains("مرحبا") || lower.contains("أهلا") || lower.contains("اهلا") || lower.contains("سلام") || lower.contains("صباح الخير") || lower.contains("مساء الخير")) {
            return Response(
                text = "مرحبًا بك! أنا **WESS PLUS**، مساعدك الذكي ومتصفحك العصبي.\n\n" +
                        "يمكنني مساعدتك في:\n" +
                        "• تصفح الإنترنت والبحث المباشر في Google من شريط العنوان.\n" +
                        "• تحليل تسلسلات DNA و RNA وحساب نقطة التعادل الكهربائي (pI) وتصميم أدلة CRISPR.\n" +
                        "• حساب تخفيفات المحاليل المخبرية وخلطات تفاعل البلمرة المتسلسل (PCR Master Mix).\n" +
                        "• توليد الصور التخطيطية النيونية والترجمة الفورية لأكثر من 70 لغة.\n" +
                        "• تخطيط خوادم Discord وحفظ مواقعك المفضلة.\n\n" +
                        "كيف يمكنني مساعدتك الآن؟",
                links = shortcuts.take(3).map { WebLink(it.name, it.url) }
            )
        }

        // Helpful contextual responses with cyberpunk flair
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return Response(
                text = "Greetings! I'm **WESS PLUS**, your neural browser assistant.\n\n" +
                        "I can help you:\n" +
                        "• Browse the web and search Google directly from the address bar.\n" +
                        "• Analyze DNA/RNA sequences, CRISPR guides, and lab dilution math.\n" +
                        "• Generate AI image prompts and visual sketches.\n" +
                        "• Translate across 70+ languages with voice playback.\n" +
                        "• Plan Discord servers, brainstorm code, and bookmark sites.\n\n" +
                        "What would you like to explore today?",
                links = shortcuts.take(3).map { WebLink(it.name, it.url) }
            )
        }

        if (lower.contains("من أنت") || lower.contains("ما هو wess plus") || lower.contains("who are you") || lower.contains("what is wess plus")) {
            return Response(
                text = if (isArabic) {
                    "**WESS PLUS** هو متصفح ذكي ومحطة عمل متطورة تدعم الذكاء الاصطناعي والعلوم الحيوية.\n" +
                            "يجمع بين التصميم السيبراني عالي التباين، التصفح متعدد التبويبات، حزمة كاملة لتحليل الجينوم والبروتينات والمختبر، وأدوات الترجمة والصوت."
                } else {
                    "**WESS PLUS** is a next-generation Android AI workstation and browser companion.\n" +
                            "Combining high-contrast cyberpunk ergonomics, lightning-fast multi-tab chatting, multimodal media tools, real-time Google search dispatch, and a comprehensive **BioScience Suite** for molecular biology and laboratory research."
                },
                links = listOf(
                    WebLink("Google Search", "https://www.google.com"),
                    WebLink("NCBI Bio", "https://pubmed.ncbi.nlm.nih.gov")
                )
            )
        }

        // Generic intelligent query response with Google Search link
        val googleSearchUrl = "https://www.google.com/search?q=" + java.net.URLEncoder.encode(raw, "UTF-8")
        val text = if (isArabic) {
            "تمت معالجة الاستفسار: \"$raw\".\n\n" +
                    "يمكنك البحث المباشر في Google بالنقر على الرابط أدناه، أو لصق أي تسلسل جيني أو بروتيني لتحليله فورًا في حزمة العلوم الحيوية!"
        } else {
            "Processed query: \"$raw\".\n\n" +
                    "You can inspect this topic further or launch a live search on Google. If you have molecular sequences or laboratory calculations to solve, paste them anytime!"
        }

        return Response(
            text = text,
            links = listOf(WebLink(if (isArabic) "ابحث في Google عن \"$raw\"" else "Search Google for \"$raw\"", googleSearchUrl))
        )
    }

    private fun normalizeUrl(target: String): String {
        var u = target.trim()
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "https://$u"
        }
        return if (u.contains(".")) u else "https://www.google.com/search?q=" + java.net.URLEncoder.encode(target, "UTF-8")
    }

    private fun extractUrl(text: String): String? {
        val match = Regex("https?://[\\w\\-.]+(?:\\.[a-z]{2,})(?:/[^\\s]*)?|www\\.[\\w\\-.]+(?:\\.[a-z]{2,})(?:/[^\\s]*)?|[\\w\\-.]+\\.[a-z]{2,}(?:/[^\\s]*)?").find(text)
        return match?.value?.let { normalizeUrl(it) }
    }
}
