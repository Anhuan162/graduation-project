package com.graduation.project.library.service;

import com.graduation.project.auth.service.FirebaseService;
import com.graduation.project.library.dto.DocumentRequest;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.entity.Document;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.entity.Subject;
import com.graduation.project.library.repository.DocumentRepository;
import com.graduation.project.library.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final FirebaseService firebaseService;
    private final DocumentRepository documentRepository;
    private final SubjectRepository subjectRepository;

    private final String FOLDER_DOCUMENT = "documents";
    private final String FOLDER_IMAGE = "images";

    public DocumentResponse uploadDocument(
            MultipartFile document,
            MultipartFile image,
            DocumentRequest documentRequest
    ) throws IOException {

        Optional<Subject> subject = subjectRepository.findById(documentRequest.getSubjectId());
        if (subject.isEmpty()){
            throw new RuntimeException("subject not found!!!");
        }

        List<Document> checkOldDocument = documentRepository.findByTitle(documentRequest.getTitle());
        if (checkOldDocument.size() > 0){
            throw new RuntimeException("document name already exist!!!");
        }

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
                .subject(subject.get())
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
