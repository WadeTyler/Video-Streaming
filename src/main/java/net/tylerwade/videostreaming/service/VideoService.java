package net.tylerwade.videostreaming.service;

import lombok.RequiredArgsConstructor;
import net.tylerwade.videostreaming.adapter.ContentAdapter;
import net.tylerwade.videostreaming.model.Content;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {

	private final ContentAdapter contentAdapter;

	public Content getVideoContent(String fileName, Long start, Long end) throws IOException {
		try {
			Content content = buildContent(fileName, start, end);
			contentAdapter.addContent(content);
			return content;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Content buildContent(String fileName, Long start, Long end) {
		Content content = new Content();
		addFileNameAndType(content, fileName);
		content.setStart(start);
		content.setEnd(end);
		return content;
	}

	private void addFileNameAndType(Content content, String fileName) {
		String[] split = fileName.split("\\.");
		content.setContentType(split[split.length - 1]);
		content.setFileName(fileName);
	}

	public List<Content> getAllContentsMetadata() {
		try {

			return contentAdapter.getAllContentsMetadata();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}