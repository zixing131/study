package com.study.kids.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 有道发音本地缓存：首次拉取后落盘，后续直接读文件。
 */
@Slf4j
@Service
public class VoiceCacheService {

    private final Path cacheDir;
    private final HttpClient httpClient;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public VoiceCacheService(@Value("${voice.cache-dir:./data/audio}") String cacheDir) {
        this.cacheDir = Path.of(cacheDir).toAbsolutePath().normalize();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(cacheDir);
        log.info("Voice cache dir: {}", cacheDir);
    }

    public Path resolveAudio(String text, String lang, int type) throws IOException {
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("text 不能为空");
        }
        if (clean.length() > 80) {
            throw new IllegalArgumentException("text 过长");
        }
        String normalizedLang = normalizeLang(lang);
        int normalizedType = (type == 1) ? 1 : 2;
        String key = cacheKey(clean, normalizedLang, normalizedType);
        Path file = cacheDir.resolve(key + ".mp3");
        if (Files.exists(file) && Files.size(file) > 0) {
            return file;
        }
        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            if (Files.exists(file) && Files.size(file) > 0) {
                return file;
            }
            IOException last = null;
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    downloadAndStore(clean, normalizedLang, normalizedType, file);
                    return file;
                } catch (IOException e) {
                    last = e;
                    try {
                        Thread.sleep(200L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
            throw last != null ? last : new IOException("下载失败");
        }
    }

    public boolean isCached(String text, String lang, int type) {
        try {
            if (text == null || text.isBlank()) {
                return false;
            }
            String key = cacheKey(text.trim(), normalizeLang(lang), type == 1 ? 1 : 2);
            Path file = cacheDir.resolve(key + ".mp3");
            return Files.exists(file) && Files.size(file) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public long cachedFileCount() {
        try {
            if (!Files.isDirectory(cacheDir)) {
                return 0;
            }
            try (var stream = Files.list(cacheDir)) {
                return stream.filter(p -> p.getFileName().toString().endsWith(".mp3")).count();
            }
        } catch (IOException e) {
            return 0;
        }
    }

    private void downloadAndStore(String text, String lang, int type, Path target) throws IOException {
        String url = buildYoudaoUrl(text, lang, type);
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.youdao.com/")
                    .header("Accept", "*/*")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("有道发音下载失败: HTTP " + response.statusCode() + " text=" + text);
            }
            try (InputStream in = response.body()) {
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            }
            if (Files.size(tmp) < 200) {
                Files.deleteIfExists(tmp);
                throw new IOException("有道发音文件异常过小: " + text);
            }
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFail) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            log.debug("Cached voice: {} -> {}", text, target.getFileName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("下载被中断", e);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private String buildYoudaoUrl(String text, String lang, int type) {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
        if ("en".equals(lang)) {
            return "https://dict.youdao.com/dictvoice?audio=" + encoded + "&type=" + type;
        }
        return "https://dict.youdao.com/dictvoice?audio=" + encoded + "&le=zh";
    }

    private String normalizeLang(String lang) {
        if (lang == null) {
            return "zh";
        }
        String lower = lang.toLowerCase(Locale.ROOT);
        if (lower.startsWith("en")) {
            return "en";
        }
        return "zh";
    }

    private String cacheKey(String text, String lang, int type) {
        String raw = lang + "|" + type + "|" + text;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 32);
        } catch (Exception e) {
            return Integer.toHexString(raw.hashCode());
        }
    }
}
