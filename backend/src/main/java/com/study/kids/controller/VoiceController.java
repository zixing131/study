package com.study.kids.controller;

import com.study.kids.common.ApiResponse;
import com.study.kids.common.ChineseNumbers;
import com.study.kids.service.VoiceCacheService;
import com.study.kids.service.VoiceWarmupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class VoiceController {

    private final VoiceCacheService voiceCacheService;
    private final VoiceWarmupService voiceWarmupService;

    /**
     * 获取发音音频（服务端缓存，首次向有道拉取）
     */
    @GetMapping("/voice")
    public ResponseEntity<Resource> voice(
            @RequestParam String text,
            @RequestParam(defaultValue = "zh") String lang,
            @RequestParam(defaultValue = "2") int type
    ) {
        try {
            Path file = voiceCacheService.resolveAudio(text, lang, type);
            return audioResponse(file);
        } catch (Exception e) {
            log.warn("voice 获取失败 text={} : {}", text, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /** 数字 1–100 快捷接口 */
    @GetMapping("/number")
    public ResponseEntity<Resource> number(@RequestParam int n) {
        if (n < 1 || n > 100) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String reading = ChineseNumbers.toChinese(n);
            Path file = voiceCacheService.resolveAudio(reading, "zh", 2);
            return audioResponse(file);
        } catch (Exception e) {
            log.warn("number 获取失败 n={} : {}", n, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        Map<String, Object> map = voiceWarmupService.status();
        map.put("files", voiceCacheService.cachedFileCount());
        return ApiResponse.ok(map);
    }

    private ResponseEntity<Resource> audioResponse(Path file) {
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"voice.mp3\"")
                .body(resource);
    }
}
