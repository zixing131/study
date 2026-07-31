package com.study.kids.service;

import com.study.kids.service.tts.EdgeTtsClient;
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
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发音本地缓存：优先 Edge 神经网络 TTS，失败依次回退百度 / Google / 有道。
 */
@Slf4j
@Service
public class VoiceCacheService {

    private final Path cacheDir;
    private final List<String> providers;
    private final String edgeVoiceZh;
    private final String edgeVoiceEn;
    private final String edgeVoiceEnAlt;
    private final HttpClient httpClient;
    private final EdgeTtsClient edgeTtsClient;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public VoiceCacheService(
            @Value("${voice.cache-dir:./data/audio}") String cacheDir,
            @Value("${voice.providers:edge,baidu,google,youdao}") String providers,
            @Value("${voice.edge.voice-zh:zh-CN-XiaoxiaoNeural}") String edgeVoiceZh,
            @Value("${voice.edge.voice-en:en-US-JennyNeural}") String edgeVoiceEn,
            @Value("${voice.edge.voice-en-alt:en-US-GuyNeural}") String edgeVoiceEnAlt
    ) {
        this.cacheDir = Path.of(cacheDir).toAbsolutePath().normalize();
        this.providers = parseProviders(providers);
        this.edgeVoiceZh = edgeVoiceZh;
        this.edgeVoiceEn = edgeVoiceEn;
        this.edgeVoiceEnAlt = edgeVoiceEnAlt;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.edgeTtsClient = new EdgeTtsClient(this.httpClient, Duration.ofSeconds(20));
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(cacheDir);
        log.info("Voice cache dir: {}, providers={}", cacheDir, providers);
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
            downloadWithFallback(clean, normalizedLang, normalizedType, file);
            return file;
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

    private void downloadWithFallback(String text, String lang, int type, Path target) throws IOException {
        List<String> errors = new ArrayList<>();
        for (String provider : providers) {
            try {
                byte[] audio = fetchFromProvider(provider, text, lang, type);
                if (audio == null || audio.length < 200) {
                    throw new IOException(provider + " 音频过小");
                }
                writeAtomically(target, audio);
                log.info("Cached voice via {} : {} -> {}", provider, text, target.getFileName());
                return;
            } catch (Exception e) {
                errors.add(provider + ": " + e.getMessage());
                log.warn("TTS provider {} failed for text={}: {}", provider, text, e.getMessage());
            }
        }
        throw new IOException("全部 TTS 提供方失败: " + String.join(" | ", errors));
    }

    private byte[] fetchFromProvider(String provider, String text, String lang, int type) throws IOException {
        return switch (provider) {
            case "edge" -> edgeTtsClient.synthesize(text, pickEdgeVoice(lang, type));
            case "baidu" -> httpGetBytes(buildBaiduUrl(text, lang), "https://fanyi.baidu.com/");
            case "google" -> httpGetBytes(buildGoogleUrl(text, lang), "https://translate.google.com/");
            case "youdao" -> httpGetBytes(buildYoudaoUrl(text, lang, type), "https://www.youdao.com/");
            default -> throw new IOException("未知 TTS 提供方: " + provider);
        };
    }

    private String pickEdgeVoice(String lang, int type) {
        if ("en".equals(lang)) {
            return type == 1 ? edgeVoiceEnAlt : edgeVoiceEn;
        }
        return edgeVoiceZh;
    }

    private byte[] httpGetBytes(String url, String referer) throws IOException {
        Path tmp = Files.createTempFile("voice-", ".mp3");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Referer", referer)
                    .header("Accept", "*/*")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("HTTP " + response.statusCode());
            }
            try (InputStream in = response.body()) {
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            }
            byte[] bytes = Files.readAllBytes(tmp);
            if (bytes.length < 200) {
                throw new IOException("文件异常过小");
            }
            return bytes;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("下载被中断", e);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private void writeAtomically(Path target, byte[] audio) throws IOException {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.write(tmp, audio);
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFail) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
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

    private String buildBaiduUrl(String text, String lang) {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        if ("en".equals(lang)) {
            return "https://fanyi.baidu.com/gettts?lan=en&text=" + encoded + "&spd=3&source=web";
        }
        return "https://fanyi.baidu.com/gettts?lan=zh&text=" + encoded + "&spd=5&source=web";
    }

    private String buildGoogleUrl(String text, String lang) {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String tl = "en".equals(lang) ? "en" : "zh-CN";
        return "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=" + tl + "&q=" + encoded;
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

    private static List<String> parseProviders(String raw) {
        List<String> list = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return List.of("edge", "baidu", "google", "youdao");
        }
        for (String part : raw.split(",")) {
            String p = part.trim().toLowerCase(Locale.ROOT);
            if (!p.isEmpty() && !list.contains(p)) {
                list.add(p);
            }
        }
        return list.isEmpty() ? List.of("edge", "baidu", "google", "youdao") : list;
    }
}
