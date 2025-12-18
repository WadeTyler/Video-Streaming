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
	public Mono<ResponseEntity<byte[]>> getVideo(@PathVariable String fileName,
												 @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

		Long[] parsedRange = parseRange(rangeHeader);
		Content content = videoService.getVideoContent(fileName, parsedRange[0], parsedRange[1]);

		boolean isCompleteContent = content.getStart() == 0 && content.getEnd() == content.getContentLength() - 1;

		return Mono.just(ResponseEntity.status(isCompleteContent ? 200 : 206)
				.header("Content-Type", "video/" + content.getContentType())
				.header("Accept-Ranges", "bytes")
				.header("Content-Length", String.valueOf(content.getContent().length))
				.header("Content-Range", String.format(
						"bytes %s-%s/%s",
						content.getStart(), content.getEnd(), content.getContentLength())
				).body(content.getContent())
		);
	}

	private Long[] parseRange(String range) {
		if (range == null || !range.startsWith("bytes=")) {
			return new Long[] {null, null};
		}

		String rangeValues = range.substring("bytes=".length());
		String[] rangeParts = rangeValues.split("-");
		Long start = 0L;
		Long end = null;

		try {
			if (rangeParts.length > 0 && !rangeParts[0].isEmpty()) {
				start = Long.parseLong(rangeParts[0]);
			}
			if (rangeParts.length > 1 && !rangeParts[1].isEmpty()) {
				end = Long.parseLong(rangeParts[1]);
			}
		} catch (NumberFormatException e) {
			// Fallback to defaults if parsing fails.
			log.warn("Parsing failed for range values {}", rangeValues);
		}
		return new Long[] {start, end};
	}

	@GetMapping
	public List<Content> getAllVideosMetadata() {
		return videoService.getAllContentsMetadata();
	}

}