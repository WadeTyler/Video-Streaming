package net.tylerwade.videostreaming.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.tylerwade.videostreaming.model.Content;
import net.tylerwade.videostreaming.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	@GetMapping("/{fileName}")
	public Mono<ResponseEntity<StreamingResponseBody>> getVideo(@PathVariable String fileName,
																@RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
		Content content = videoService.getVideoContent(fileName, rangeHeader);

		boolean isCompleteContent = content.getRange().start() == 0 && content.getRange().end() == content.getFileSize() - 1;

		return Mono.just(ResponseEntity.status(isCompleteContent ? 200 : 206)
				.header("Content-Type", content.getContentType())
				.header("Accept-Ranges", "bytes")
				.header("Content-Length", String.valueOf(content.getContentLength()))
				.header("Content-Range", String.format(
						"bytes %s-%s/%s",
						content.getRange().start(), content.getRange().end(), content.getFileSize())
				).body(content.getContent())
		);
	}



	@GetMapping
	public List<Content> getAllVideosMetadata() {
		return videoService.getAllContentsMetadata();
	}

}