package net.tylerwade.videostreaming.adapter;

import net.tylerwade.videostreaming.model.Content;

import java.io.IOException;
import java.util.List;

public interface ContentAdapter {

	void addContent(Content content) throws IOException;

	Long getContentSize(Content content) throws IOException;

	List<Content> getAllContentsMetadata() throws IOException;

}