package net.tylerwade.videostreaming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.tylerwade.videostreaming.adapter.ContentAdapter;
import net.tylerwade.videostreaming.model.Content;
import net.tylerwade.videostreaming.model.ContentRequest;
import net.tylerwade.videostreaming.model.Range;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

	private final ContentAdapter contentAdapter;

	public Content getVideoContent(String fileName, String rangeHeader) throws IOException {
		try {
			ContentRequest contentRequest = buildContentRequest(fileName, rangeHeader);
			return contentAdapter.loadContent(contentRequest);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ContentRequest buildContentRequest(String fileName, String rangeHeader) {
		return ContentRequest.builder()
				.fileName(fileName)
				.range(parseRange(rangeHeader))
				.build();
	}

	private Range parseRange(String range) {
		if (range == null || !range.startsWith("bytes=")) {
			return new Range(null, null);
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
		return new Range(start, end);
	}

	public List<Content> getAllContentsMetadata() {
		try {
			return contentAdapter.getAllContentsMetadata();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}