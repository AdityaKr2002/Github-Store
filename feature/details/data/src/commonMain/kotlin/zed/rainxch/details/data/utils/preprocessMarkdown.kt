package zed.rainxch.details.data.utils

fun preprocessMarkdown(markdown: String, baseUrl: String): String {
    val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

    var processed = markdown

    fun normalizeGitHubUrl(url: String): String {
        return if (url.contains("github.com") && url.contains("/blob/")) {
            url.replace("github.com", "raw.githubusercontent.com")
                .replace("/blob/", "/")
        } else {
            url
        }
    }

    fun isSvgUrl(url: String): Boolean {
        return url.endsWith(".svg", ignoreCase = true) ||
                url.contains(".svg?", ignoreCase = true) ||
                url.contains(".svg#", ignoreCase = true)
    }

    fun resolveUrl(path: String): String {
        val isAbsolute = path.startsWith("http://") ||
                path.startsWith("https://") ||
                path.startsWith("data:")
        return if (isAbsolute) {
            normalizeGitHubUrl(path)
        } else {
            val cleaned = path.trim().trimStart('.', '/')
            "$normalizedBaseUrl$cleaned"
        }
    }

    // 1. Unwrap <picture> elements → keep only the <img> fallback
    processed = processed.replace(
        Regex(
            """<picture[^>]*>.*?(<img\s[^>]*?>).*?</picture>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    ) { match ->
        match.groupValues[1]
    }
    // Also strip orphaned <source> tags (outside <picture>)
    processed = processed.replace(
        Regex("""<source\s[^>]*?/?>""", RegexOption.IGNORE_CASE),
        ""
    )

    // 2. Unwrap <a> tags that wrap <img> tags — keep the <img> for step 3
    processed = processed.replace(
        Regex(
            """<a\s[^>]*?>\s*(<img\s[^>]*?>)\s*</a>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    ) { match ->
        match.groupValues[1]
    }

    // 3. Convert <img> tags → markdown images
    processed = processed.replace(
        Regex(
            """<img\s+([^>]*?)\s*/?>""",
            RegexOption.IGNORE_CASE
        )
    ) { imgMatch ->
        val imgTag = imgMatch.groupValues[1]

        val srcMatch = Regex("""src=(["'])([^"']+)\1""").find(imgTag)
        val src = srcMatch?.groupValues?.get(2) ?: ""

        val altMatch = Regex("""alt=(["'])([^"']*)\1""").find(imgTag)
        val alt = altMatch?.groupValues?.get(2) ?: ""

        if (src.isNotEmpty()) {
            val normalizedSrc = resolveUrl(src)

            if (isSvgUrl(normalizedSrc)) {
                if (alt.isNotEmpty()) "**$alt**" else ""
            } else {
                "![$alt]($normalizedSrc)"
            }
        } else {
            ""
        }
    }

    // 4. Normalize markdown image URLs (resolve relative, normalize GitHub blob)
    processed = processed.replace(
        Regex("""!\[([^\]]*)\]\(([^)]+)\)""")
    ) { match ->
        val alt = match.groupValues[1]
        val originalPath = match.groupValues[2]
        val finalUrl = resolveUrl(originalPath)

        if (isSvgUrl(finalUrl)) {
            if (alt.isNotEmpty()) "**$alt**" else ""
        } else {
            "![$alt]($finalUrl)"
        }
    }

    // 5. Handle <video> tags → markdown link or remove
    processed = processed.replace(
        Regex(
            """<video[^>]*?\ssrc=(["'])([^"']+)\1[^>]*>.*?</video>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    ) { match ->
        val src = match.groupValues[2]
        "[Video]($src)"
    }
    // Video with <source> inside
    processed = processed.replace(
        Regex(
            """<video[^>]*>.*?<source\s[^>]*?\ssrc=(["'])([^"']+)\1[^>]*?>.*?</video>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    ) { match ->
        val src = match.groupValues[2]
        "[Video]($src)"
    }

    // 6. Convert HTML headings <h1>–<h6> → markdown headings
    for (level in 1..6) {
        val hashes = "#".repeat(level)
        processed = processed.replace(
            Regex(
                """<h$level[^>]*>(.*?)</h$level>""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            )
        ) { match ->
            val content = match.groupValues[1].trim()
            "\n$hashes $content\n"
        }
    }

    // 7. Convert <br> and <hr> tags
    processed = processed.replace(
        Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE),
        "\n"
    )
    processed = processed.replace(
        Regex("""<hr\s*/?>""", RegexOption.IGNORE_CASE),
        "\n---\n"
    )

    // 8. Convert inline formatting tags
    // <b> / <strong> → **text**
    processed = processed.replace(
        Regex(
            """<(b|strong)>(.*?)</\1>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    ) { match ->
        "**${match.groupValues[2]}**"
    }
    // <i> / <em> → *text*
    processed = processed.replace(
        Regex(
            """<(i|em)>(.*?)</\1>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    ) { match ->
        "*${match.groupValues[2]}*"
    }
    // <code> → `text` (single-line only, not <pre><code>)
    processed = processed.replace(
        Regex(
            """<code>([^<]*?)</code>""",
            RegexOption.IGNORE_CASE
        )
    ) { match ->
        "`${match.groupValues[1]}`"
    }
    // <s> / <del> / <strike> → ~~text~~
    processed = processed.replace(
        Regex(
            """<(s|del|strike)>(.*?)</\1>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    ) { match ->
        "~~${match.groupValues[2]}~~"
    }

    // 9. Convert <a href="url">text</a> → [text](url) (non-image links)
    processed = processed.replace(
        Regex(
            """<a\s+[^>]*?href=(["'])([^"']+)\1[^>]*>(.*?)</a>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    ) { match ->
        val url = match.groupValues[2]
        val text = match.groupValues[3].trim()
        if (text.isEmpty()) {
            "[$url]($url)"
        } else {
            "[$text]($url)"
        }
    }

    // 10. <kbd> → `text`
    processed = processed.replace(
        Regex(
            """<kbd>(.*?)</kbd>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    ) { match ->
        "`${match.groupValues[1]}`"
    }

    // 11. Strip remaining wrapper tags (keep content)
    // <div align="center"> and </div>
    processed = processed.replace(
        Regex("""<div[^>]*?align=["']center["'][^>]*?>\s*""", RegexOption.IGNORE_CASE),
        "\n\n"
    )
    processed = processed.replace(
        Regex("""</div>\s*""", RegexOption.IGNORE_CASE),
        "\n\n"
    )
    // <p> / </p>
    processed = processed.replace(
        Regex("""<p[^>]*?>""", RegexOption.IGNORE_CASE),
        "\n"
    )
    processed = processed.replace(
        Regex("""</p>""", RegexOption.IGNORE_CASE),
        "\n"
    )
    // <details> / <summary>
    processed = processed.replace(
        Regex("""<details[^>]*?>""", RegexOption.IGNORE_CASE),
        "\n"
    )
    processed = processed.replace(
        Regex("""</details>""", RegexOption.IGNORE_CASE),
        "\n"
    )
    processed = processed.replace(
        Regex("""<summary[^>]*?>(.*?)</summary>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    ) { match ->
        "**${match.groupValues[1].trim()}**\n"
    }
    // <span>, <sup>, <sub> — strip tags, keep content
    processed = processed.replace(
        Regex("""</?(?:span|sup|sub)[^>]*?>""", RegexOption.IGNORE_CASE),
        ""
    )

    // 12. Decode common HTML entities
    processed = processed
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&nbsp;", " ")

    // 13. Clean up empty <p> tags and excess newlines
    processed = processed.replace(
        Regex("""<p[^>]*?>\s*</p>""", RegexOption.IGNORE_CASE),
        ""
    )
    processed = processed.replace(
        Regex("""\n{3,}"""),
        "\n\n"
    )

    // 14. Clean up orphaned markdown link fragments
    processed = processed.replace(
        Regex("""^\]\([^)]+\)""", RegexOption.MULTILINE),
        ""
    )

    return processed
}
