package tn.esprit.collocationservice.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import  tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreFilter;

import java.util.List;

public interface collocOffreService {

    public collocOffre create(collocOffre co, List<MultipartFile> images, Long userId);
    public List<collocOffre> getAll();
    public Page<collocOffre> search(collocOffreFilter filter, Pageable pageable) ;
    public collocOffre getById(Long id) ;
    public List<collocOffre> findByOwnerId(Long id) ;
    public collocOffre updateOffre(Long id, collocOffre updatedOffre) ;
    public void deleteOffre(Long id) ;

    public List<collocOffre> getNearbyOffers(double lat, double lng, double radius) ;


    }
