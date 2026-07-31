package com.study.kids.service.tts;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Microsoft Edge 在线神经网络 TTS（与 edge-tts 同源协议，无需 API Key）。
 */
@Slf4j
public class EdgeTtsClient {

    private static final String TRUSTED_CLIENT_TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4";
    private static final String CHROMIUM_FULL_VERSION = "143.0.3650.75";
    private static final String SEC_MS_GEC_VERSION = "1-" + CHROMIUM_FULL_VERSION;
    private static final String CHROMIUM_MAJOR = CHROMIUM_FULL_VERSION.split("\\.", 2)[0];
    private static final String WSS_BASE =
            "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1"
                    + "?TrustedClientToken=" + TRUSTED_CLIENT_TOKEN;
    private static final long WIN_EPOCH = 11644473600L;
    private static final DateTimeFormatter JS_DATE = DateTimeFormatter
            .ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT+0000 (Coordinated Universal Time)'", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    private final HttpClient httpClient;
    private final Duration timeout;

    public EdgeTtsClient(HttpClient httpClient, Duration timeout) {
        this.httpClient = httpClient;
        this.timeout = timeout == null ? Duration.ofSeconds(20) : timeout;
    }

    public byte[] synthesize(String text, String voice) throws IOException {
        String clean = sanitize(text);
        if (clean.isEmpty()) {
            throw new IOException("Edge TTS: 文本为空");
        }
        String voiceName = (voice == null || voice.isBlank()) ? "zh-CN-XiaoxiaoNeural" : voice.trim();
        IOException last = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return synthesizeOnce(clean, voiceName);
            } catch (IOException e) {
                last = e;
                log.warn("Edge TTS 失败 attempt={} : {}", attempt, e.getMessage());
            }
        }
        throw last != null ? last : new IOException("Edge TTS 失败");
    }

    private byte[] synthesizeOnce(String text, String voice) throws IOException {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        String url = WSS_BASE
                + "&ConnectionId=" + requestId
                + "&Sec-MS-GEC=" + generateSecMsGec()
                + "&Sec-MS-GEC-Version=" + SEC_MS_GEC_VERSION;

        CompletableFuture<byte[]> done = new CompletableFuture<>();
        ByteArrayOutputStream audio = new ByteArrayOutputStream(16 * 1024);
        AtomicReference<WebSocket> socketRef = new AtomicReference<>();

        WebSocket.Listener listener = new WebSocket.Listener() {
            private final StringBuilder textBuf = new StringBuilder();
            private final ByteArrayOutputStream binBuf = new ByteArrayOutputStream();

            @Override
            public void onOpen(WebSocket webSocket) {
                socketRef.set(webSocket);
                webSocket.request(1);
                try {
                    webSocket.sendText(speechConfigMessage(), true);
                    webSocket.sendText(ssmlMessage(requestId, text, voice), true);
                } catch (Exception e) {
                    done.completeExceptionally(e);
                }
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                textBuf.append(data);
                if (last) {
                    String message = textBuf.toString();
                    textBuf.setLength(0);
                    String path = headerPath(message);
                    if ("turn.end".equalsIgnoreCase(path)) {
                        byte[] bytes = audio.toByteArray();
                        if (bytes.length > 0) {
                            done.complete(bytes);
                        } else {
                            done.completeExceptionally(new IOException("Edge TTS 未收到音频"));
                        }
                    }
                }
                webSocket.request(1);
                return null;
            }

            @Override
            public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                byte[] chunk = new byte[data.remaining()];
                data.get(chunk);
                try {
                    binBuf.write(chunk);
                } catch (IOException e) {
                    done.completeExceptionally(e);
                    return null;
                }
                if (last) {
                    appendAudio(binBuf.toByteArray(), audio, done);
                    binBuf.reset();
                }
                webSocket.request(1);
                return null;
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                done.completeExceptionally(error);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                if (!done.isDone()) {
                    byte[] bytes = audio.toByteArray();
                    if (bytes.length > 0) {
                        done.complete(bytes);
                    } else {
                        done.completeExceptionally(new IOException(
                                "Edge TTS 连接关闭: " + statusCode + " " + reason));
                    }
                }
                return null;
            }
        };

        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/" + CHROMIUM_MAJOR + ".0.0.0 Safari/537.36 "
                + "Edg/" + CHROMIUM_MAJOR + ".0.0.0";
        String muid = HexFormat.of().withUpperCase().formatHex(randomBytes(16));

        try {
            httpClient.newWebSocketBuilder()
                    .header("Pragma", "no-cache")
                    .header("Cache-Control", "no-cache")
                    .header("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
                    .header("User-Agent", ua)
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Cookie", "muid=" + muid + ";")
                    .connectTimeout(timeout)
                    .buildAsync(URI.create(url), listener)
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);

            byte[] result = done.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (result == null || result.length < 200) {
                throw new IOException("Edge TTS 音频过短");
            }
            return result;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IOException("Edge TTS 请求失败: " + cause.getMessage(), cause);
        } finally {
            WebSocket ws = socketRef.get();
            if (ws != null) {
                try {
                    ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }
    }

    private static void appendAudio(byte[] message, ByteArrayOutputStream audio, CompletableFuture<byte[]> done) {
        if (message.length < 2) {
            return;
        }
        int headerLength = ((message[0] & 0xff) << 8) | (message[1] & 0xff);
        int headerEnd = 2 + headerLength;
        if (headerEnd > message.length) {
            done.completeExceptionally(new IOException("Edge TTS 二进制头长度非法"));
            return;
        }
        String headerText = new String(message, 2, headerLength, StandardCharsets.UTF_8);
        Map<String, String> headers = parseHeaders(headerText);
        if (!"audio".equalsIgnoreCase(headers.getOrDefault("Path", ""))) {
            return;
        }
        String contentType = headers.get("Content-Type");
        int dataLen = message.length - headerEnd;
        if (contentType == null || dataLen <= 0) {
            return;
        }
        if (!contentType.toLowerCase(Locale.ROOT).contains("audio/mpeg")) {
            done.completeExceptionally(new IOException("Edge TTS 非预期 Content-Type: " + contentType));
            return;
        }
        audio.write(message, headerEnd, dataLen);
    }

    private static String headerPath(String message) {
        int sep = message.indexOf("\r\n\r\n");
        String headerBlock = sep >= 0 ? message.substring(0, sep) : message;
        return parseHeaders(headerBlock).getOrDefault("Path", "");
    }

    private String speechConfigMessage() {
        String ts = JS_DATE.format(Instant.now());
        return "X-Timestamp:" + ts + "\r\n"
                + "Content-Type:application/json; charset=utf-8\r\n"
                + "Path:speech.config\r\n\r\n"
                + "{\"context\":{\"synthesis\":{\"audio\":{\"metadataoptions\":{"
                + "\"sentenceBoundaryEnabled\":\"false\",\"wordBoundaryEnabled\":\"false\"},"
                + "\"outputFormat\":\"audio-24khz-48kbitrate-mono-mp3\"}}}}\r\n";
    }

    private String ssmlMessage(String requestId, String text, String voice) {
        String ts = JS_DATE.format(Instant.now());
        String ssml = "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='en-US'>"
                + "<voice name='" + voice + "'>"
                + "<prosody pitch='+0Hz' rate='+0%' volume='+0%'>"
                + xmlEscape(text)
                + "</prosody></voice></speak>";
        return "X-RequestId:" + requestId + "\r\n"
                + "Content-Type:application/ssml+xml\r\n"
                + "X-Timestamp:" + ts + "Z\r\n"
                + "Path:ssml\r\n\r\n"
                + ssml;
    }

    private String generateSecMsGec() {
        double ticks = Instant.now().getEpochSecond() + WIN_EPOCH;
        ticks -= ticks % 300;
        long fileTime = Math.round(ticks * 10_000_000d);
        String strToHash = fileTime + TRUSTED_CLIENT_TOKEN;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(strToHash.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().withUpperCase().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, String> parseHeaders(String headerBlock) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String line : headerBlock.split("\r\n")) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                map.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
            }
        }
        return map;
    }

    private static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int code = c;
            if ((code >= 0 && code <= 8) || (code >= 11 && code <= 12) || (code >= 14 && code <= 31)) {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    private static String xmlEscape(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        new SecureRandom().nextBytes(b);
        return b;
    }
}
