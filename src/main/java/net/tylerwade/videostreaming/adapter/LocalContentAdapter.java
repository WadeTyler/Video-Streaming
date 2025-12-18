package net.tylerwade.videostreaming.adapter;

import lombok.RequiredArgsConstructor;
import net.tylerwade.videostreaming.model.Content;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class LocalContentAdapter implements ContentAdapter {

	private static final String VIDEO_PATH = "classpath:videos";

	private final ResourceLoader resourceLoader;

	@Override
	public void addContent(Content content) throws IOException {
		Resource videoResource = loadResource(content);

		long fileSize = videoResource.contentLength();
		content.setContentLength(fileSize);

		if (content.getStart() == null) {
			content.setStart(0L);
		}

		long MAX_CHUNK_SIZE = 1024 * 1024L;

		if (content.getEnd() == null || content.getEnd() - content.getStart() + 1 > MAX_CHUNK_SIZE) {
			content.setEnd(Math.min(content.getStart() + MAX_CHUNK_SIZE - 1, fileSize - 1));
		}

		long start = content.getStart();
		long end = content.getEnd();
		int length = (int) (end - start + 1);

		byte[] result = new byte[length];

		try (InputStream is = videoResource.getInputStream()) {
			long skipped = is.skip(start);
			if (skipped < start) {
				throw new IOException("Could not skip to desired start position.");
			}

			int totalRead = 0;
			while (totalRead < length) {
				int read = is.read(result, totalRead, length - totalRead);
				if (read == -1) break;
				totalRead += read;
			}
		}

		content.setContent(result);
	}

	@Override
	public Long getContentSize(Content content) throws IOException {
		Resource videoResource = loadResource(content);
		return videoResource.contentLength();
	}

	@Override
	public List<Content> getAllContentsMetadata() throws IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources(VIDEO_PATH + "/*");

		return Arrays.stream(resources)
				.map(resource -> {
					if (resource.getFilename() == null || !resource.getFilename().contains("\\.")) {
						return null;
					}

					String[] fileNameSplit = resource.getFilename().split("\\.");
					String contentType = fileNameSplit[fileNameSplit.length - 1];

					try {
						return new Content(
								resource.getFilename(),
								contentType,
								resource.contentLength(),
								null,
								0L,
								resource.contentLength() - 1
						);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}).toList();
	}

	private Resource loadResource(Content content) {
		return resourceLoader.getResource(VIDEO_PATH + "/" + content.getFileName());
	}
}