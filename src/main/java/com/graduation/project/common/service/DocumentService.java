package com.graduation.project.common.service;

import com.graduation.project.auth.service.FirebaseService;
import com.graduation.project.common.dto.DocumentRequest;
import com.graduation.project.common.dto.DocumentResponse;
import com.graduation.project.common.entity.Document;
import com.graduation.project.common.entity.DocumentType;
import com.graduation.project.common.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final FirebaseService firebaseService;
    private final DocumentRepository documentRepository;

    private final String FOLDER_DOCUMENT = "documents";
    private final String FOLDER_IMAGE = "images";

    public DocumentResponse uploadDocument(
            MultipartFile document,
            MultipartFile image,
            DocumentRequest documentRequest
    ) throws IOException {
        String documentName = firebaseService.uploadFile(document, FOLDER_DOCUMENT);
        String imageName = firebaseService.uploadFile(image, FOLDER_IMAGE);

        String filePath =  firebaseService.getPublicUrl(FOLDER_DOCUMENT + documentName);
        String imageUrl =  firebaseService.getPublicUrl(FOLDER_IMAGE + imageName);
        Document documentEntity = Document.builder()
                .title(documentRequest.getTitle())
                .description(documentRequest.getDescription())
                .documentType(documentRequest.getDocumentType())
                .filePath(filePath)
                .imageUrl(imageUrl)
                .build();
        documentRepository.save(documentEntity);
        return documentEntity.toDocumentResponse();
    }

    public Page<DocumentResponse> searchDocuments(
            UUID subjectId,
            String title,
            DocumentType documentType,
            Integer pageNumber,
            Integer pageSize
    ){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Document> documentOptionals =
                documentRepository.findByTitleAndDocumentTypeAndSubjectId(title, documentType, subjectId, pageable);
                return documentOptionals.map(Document::toDocumentResponse);
    }
}
