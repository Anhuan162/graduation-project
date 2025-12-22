package com.graduation.project.library.service;

import com.graduation.project.common.service.FirebaseService;
import com.graduation.project.library.constant.DocumentType;
import com.graduation.project.library.dto.DocumentRequest;
import com.graduation.project.library.dto.DocumentResponse;
import com.graduation.project.library.entity.Document;
import com.graduation.project.library.entity.Subject;
import com.graduation.project.library.repository.DocumentRepository;
import com.graduation.project.library.repository.SubjectRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {
  private final FirebaseService firebaseService;
  private final DocumentRepository documentRepository;
  private final SubjectRepository subjectRepository;

  private final String FOLDER_DOCUMENT = "documents";
  private final String FOLDER_IMAGE = "images";

  public DocumentResponse uploadDocument(
      MultipartFile document, MultipartFile image, DocumentRequest documentRequest)
      throws IOException {

    Optional<Subject> subject = subjectRepository.findById(documentRequest.getSubjectId());
    if (subject.isEmpty()) {
      throw new RuntimeException("subject not found!!!");
    }

    List<Document> checkOldDocument = documentRepository.findByTitle(documentRequest.getTitle());
    if (checkOldDocument.size() > 0) {
      throw new RuntimeException("document name already exist!!!");
    }
    String filePath = "";
    String imageUrl = "";

    if (!document.isEmpty()){
      String documentName = firebaseService.uploadFile(document, FOLDER_DOCUMENT);
      filePath = firebaseService.getPublicUrl(FOLDER_DOCUMENT + documentName);
    }

    if (!image.isEmpty()){
      String imageName = firebaseService.uploadFile(image, FOLDER_IMAGE);
      imageUrl = firebaseService.getPublicUrl(FOLDER_IMAGE + imageName);
    }

    Document documentEntity =
        Document.builder()
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

  public DocumentResponse updateDocument( MultipartFile document, MultipartFile image, DocumentRequest documentRequest) throws IOException {

    Optional<Document> documentOptional = documentRepository.findById(documentRequest.getId());
    if (documentOptional.isEmpty()) {
      throw new AppException(ErrorCode.DOCUMENT_NOT_FOUND);
    }
    Document oldDoc = documentOptional.get();
    String filePath = "";
    String imageUrl = "";

    if (!document.isEmpty()) {
      firebaseService.deleteFileByUrl(oldDoc.getFilePath());
      String documentName = firebaseService.uploadFile(document, FOLDER_DOCUMENT);
      filePath = firebaseService.getPublicUrl(FOLDER_DOCUMENT + documentName);
    }
    if (!image.isEmpty()) {
      firebaseService.deleteFileByUrl(oldDoc.getImageUrl());
      String imageName = firebaseService.uploadFile(image, FOLDER_IMAGE);
      imageUrl = firebaseService.getPublicUrl(FOLDER_IMAGE + imageName);
    }


    if (filePath.length() > 0) oldDoc.setFilePath(filePath);
    if (imageUrl.length() > 0) oldDoc.setImageUrl(imageUrl);
    if (documentRequest.getDocumentType()!= null) oldDoc.setDocumentType(documentRequest.getDocumentType());
    if (documentRequest.getTitle()!= null) oldDoc.setTitle(documentRequest.getTitle());
    if (documentRequest.getDescription()!= null) oldDoc.setDescription(documentRequest.getDescription());
    if (documentRequest.getSubjectId()!= null){
      Optional<Subject> subject = subjectRepository.findById(documentRequest.getSubjectId());
      if (subject.isPresent()){
        oldDoc.setSubject(subject.get());
      }
    }
    documentRepository.save(oldDoc);
    return oldDoc.toDocumentResponse();
  }

  public void deleteDocument( UUID documentId){
    Optional<Document> documentOptional = documentRepository.findById(documentId);
    if (documentOptional.isPresent()) {
      String filePath = documentOptional.get().getFilePath();
      String imageUrl = documentOptional.get().getImageUrl();
      if (filePath != null && !filePath.isEmpty()) firebaseService.deleteFileByUrl(filePath);
      if (imageUrl != null && !imageUrl.isEmpty()) firebaseService.deleteFileByUrl(imageUrl);
      documentRepository.delete(documentOptional.get());
      return;
    }
    throw new AppException(ErrorCode.DOCUMENT_NOT_FOUND);
  }

  public Page<DocumentResponse> searchDocuments(
      UUID subjectId, String title, DocumentType documentType, Pageable pageable) {
    Page<Document> documentOptionals =
        documentRepository.findByTitleAndDocumentTypeAndSubjectId(
            title, documentType, subjectId, pageable);
    return documentOptionals.map(Document::toDocumentResponse);
  }
}
