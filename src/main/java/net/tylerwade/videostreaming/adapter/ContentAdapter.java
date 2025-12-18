package net.tylerwade.videostreaming.adapter;

import net.tylerwade.videostreaming.model.Content;
import net.tylerwade.videostreaming.model.ContentRequest;

import java.io.IOException;
import java.util.List;

public interface ContentAdapter {

	Content loadContent(ContentRequest contentRequest) throws IOException;

	Long getContentSize(ContentRequest contentRequest) throws IOException;

	List<Content> getAllContentsMetadata() throws IOException;

}