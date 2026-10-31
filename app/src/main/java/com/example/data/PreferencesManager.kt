package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.QualityTier
import com.example.model.SiteShortcut
import com.example.model.ThemeColors
import com.example.model.WebLink
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wess_plus_prefs", Context.MODE_PRIVATE)

    companion object {
        val DEFAULT_SITES = listOf(
            SiteShortcut(name = "Facebook", url = "https://www.facebook.com", mark = "f", colorHex = "#1877F2"),
            SiteShortcut(name = "YouTube", url = "https://www.youtube.com", mark = "▶", colorHex = "#FF3B5C"),
            SiteShortcut(name = "Google", url = "https://www.google.com", mark = "G", colorHex = "#4285F4"),
            SiteShortcut(name = "Discord", url = "https://discord.com", mark = "D", colorHex = "#5865F2"),
            SiteShortcut(name = "NCBI PubMed", url = "https://pubmed.ncbi.nlm.nih.gov", mark = "🔬", colorHex = "#00F0FF")
        )
    }

    fun loadSettings(): AppSettings {
        val scan = prefs.getBoolean("pref_scanlines", true)
        val rm = prefs.getBoolean("pref_reduced_motion", false)
        val speak = prefs.getBoolean("pref_speak_replies", false)
        val tierStr = prefs.getString("pref_quality_tier", "BALANCED") ?: "BALANCED"
        val tier = try { QualityTier.valueOf(tierStr) } catch (e: Exception) { QualityTier.BALANCED }
        val lang = prefs.getString("pref_language", "ar") ?: "ar"
        val themeJson = prefs.getString("pref_theme_json", null)
        val theme = if (themeJson != null) parseTheme(themeJson) else ThemeColors.WessPlusSignature
        val darkness = prefs.getFloat("pref_theme_darkness", 0.45f)
        val blur = prefs.getFloat("pref_theme_blur", 0f)
        val incognito = prefs.getBoolean("pref_incognito", false)
        val searchEngine = prefs.getString("pref_search_engine", "Google") ?: "Google"
        val safeBrowsing = prefs.getBoolean("pref_safe_browsing", true)
        val blockCookies = prefs.getBoolean("pref_block_cookies", true)
        val httpsOnly = prefs.getBoolean("pref_https_only", true)
        val doNotTrack = prefs.getBoolean("pref_do_not_track", true)
        val jsEnabled = prefs.getBoolean("pref_js_enabled", true)
        val popups = prefs.getBoolean("pref_popups_blocked", true)
        val desktopMode = prefs.getBoolean("pref_desktop_mode", false)
        val preload = prefs.getBoolean("pref_preload_pages", true)
        val memSaver = prefs.getBoolean("pref_memory_saver", true)

        return AppSettings(
            scanlines = scan,
            reducedMotion = rm,
            speakReplies = speak,
            qualityTier = tier,
            languageCode = lang,
            activeTheme = theme,
            themeDarkness = darkness,
            themeBlur = blur,
            isIncognito = incognito,
            searchEngine = searchEngine,
            safeBrowsing = safeBrowsing,
            blockThirdPartyCookies = blockCookies,
            httpsOnly = httpsOnly,
            doNotTrack = doNotTrack,
            javascriptEnabled = jsEnabled,
            popupsBlocked = popups,
            desktopModeDefault = desktopMode,
            preloadPages = preload,
            memorySaver = memSaver
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putBoolean("pref_scanlines", settings.scanlines)
            .putBoolean("pref_reduced_motion", settings.reducedMotion)
            .putBoolean("pref_speak_replies", settings.speakReplies)
            .putString("pref_quality_tier", settings.qualityTier.name)
            .putString("pref_language", settings.languageCode)
            .putString("pref_theme_json", serializeTheme(settings.activeTheme))
            .putFloat("pref_theme_darkness", settings.themeDarkness)
            .putFloat("pref_theme_blur", settings.themeBlur)
            .putBoolean("pref_incognito", settings.isIncognito)
            .putString("pref_search_engine", settings.searchEngine)
            .putBoolean("pref_safe_browsing", settings.safeBrowsing)
            .putBoolean("pref_block_cookies", settings.blockThirdPartyCookies)
            .putBoolean("pref_https_only", settings.httpsOnly)
            .putBoolean("pref_do_not_track", settings.doNotTrack)
            .putBoolean("pref_js_enabled", settings.javascriptEnabled)
            .putBoolean("pref_popups_blocked", settings.popupsBlocked)
            .putBoolean("pref_desktop_mode", settings.desktopModeDefault)
            .putBoolean("pref_preload_pages", settings.preloadPages)
            .putBoolean("pref_memory_saver", settings.memorySaver)
            .apply()
    }

    fun loadSites(): List<SiteShortcut> {
        val json = prefs.getString("wess_sites", null) ?: return DEFAULT_SITES
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<SiteShortcut>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SiteShortcut(
                        id = obj.optString("id"),
                        name = obj.getString("name"),
                        url = obj.getString("url"),
                        mark = obj.optString("mark", "•"),
                        colorHex = obj.optString("colorHex", "#00F0FF")
                    )
                )
            }
            if (list.isEmpty()) DEFAULT_SITES else list
        } catch (e: Exception) {
            DEFAULT_SITES
        }
    }

    fun saveSites(sites: List<SiteShortcut>) {
        val array = JSONArray()
        for (site in sites) {
            val obj = JSONObject().apply {
                put("id", site.id)
                put("name", site.name)
                put("url", site.url)
                put("mark", site.mark)
                put("colorHex", site.colorHex)
            }
            array.put(obj)
        }
        prefs.edit().putString("wess_sites", array.toString()).apply()
    }

    fun loadChatThreads(): List<ChatThread> {
        val json = prefs.getString("wess_threads", null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val threads = mutableListOf<ChatThread>()
            for (i in 0 until array.length()) {
                val threadObj = array.getJSONObject(i)
                val threadId = threadObj.getString("id")
                val title = threadObj.getString("title")
                val msgsArray = threadObj.getJSONArray("messages")
                val msgs = mutableListOf<ChatMessage>()
                for (j in 0 until msgsArray.length()) {
                    val mObj = msgsArray.getJSONObject(j)
                    val linksArray = mObj.optJSONArray("links")
                    val links = mutableListOf<WebLink>()
                    if (linksArray != null) {
                        for (k in 0 until linksArray.length()) {
                            val lObj = linksArray.getJSONObject(k)
                            links.add(WebLink(lObj.getString("title"), lObj.getString("url")))
                        }
                    }
                    msgs.add(
                        ChatMessage(
                            id = mObj.getString("id"),
                            isUser = mObj.getBoolean("isUser"),
                            text = mObj.getString("text"),
                            timestamp = mObj.optLong("timestamp", System.currentTimeMillis()),
                            links = links,
                            imageUri = mObj.optString("imageUri", null).takeIf { !it.isNullOrEmpty() },
                            mediaKind = mObj.optString("mediaKind", null).takeIf { !it.isNullOrEmpty() }
                        )
                    )
                }
                threads.add(ChatThread(id = threadId, title = title, messages = msgs))
            }
            threads
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveChatThreads(threads: List<ChatThread>) {
        val array = JSONArray()
        for (thread in threads) {
            val threadObj = JSONObject().apply {
                put("id", thread.id)
                put("title", thread.title)
                val msgsArray = JSONArray()
                for (msg in thread.messages) {
                    val mObj = JSONObject().apply {
                        put("id", msg.id)
                        put("isUser", msg.isUser)
                        put("text", msg.text)
                        put("timestamp", msg.timestamp)
                        if (msg.imageUri != null) put("imageUri", msg.imageUri)
                        if (msg.mediaKind != null) put("mediaKind", msg.mediaKind)
                        if (msg.links.isNotEmpty()) {
                            val lArray = JSONArray()
                            for (link in msg.links) {
                                val lObj = JSONObject().apply {
                                    put("title", link.title)
                                    put("url", link.url)
                                }
                                lArray.put(lObj)
                            }
                            put("links", lArray)
                        }
                    }
                    msgsArray.put(mObj)
                }
                put("messages", msgsArray)
            }
            array.put(threadObj)
        }
        prefs.edit().putString("wess_threads", array.toString()).apply()
    }

    private fun serializeTheme(theme: ThemeColors): String {
        return JSONObject().apply {
            put("name", theme.name)
            put("void", theme.voidHex)
            put("panel", theme.panelHex)
            put("ink", theme.inkHex)
            put("muted", theme.mutedHex)
            put("cyan", theme.cyanHex)
            put("magenta", theme.magentaHex)
        }.toString()
    }

    private fun parseTheme(json: String): ThemeColors {
        return try {
            val obj = JSONObject(json)
            ThemeColors(
                name = obj.getString("name"),
                voidHex = obj.getString("void"),
                panelHex = obj.getString("panel"),
                inkHex = obj.getString("ink"),
                mutedHex = obj.getString("muted"),
                cyanHex = obj.getString("cyan"),
                magentaHex = obj.getString("magenta")
            )
        } catch (e: Exception) {
            ThemeColors.Cyber
        }
    }
}
