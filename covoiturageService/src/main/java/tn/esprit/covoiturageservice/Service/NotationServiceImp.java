package tn.esprit.covoiturageservice.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.covoiturageservice.Entity.Notation;
import tn.esprit.covoiturageservice.Repository.INotationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotationServiceImp implements INotationService {

    private final INotationRepository notationRepository;

    @Override
    public Notation getNotationById(Long id) {
        return notationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Notation> getAllNotations() {
        return notationRepository.findAll();
    }

    @Override
    public List<Notation> getNotationsByDonneur(long idDonneur) {
        return notationRepository.findByIdDonneur(idDonneur);
    }

    @Override
    public List<Notation> getNotationsByRecepteur(long idRecepteur) {
        return notationRepository.findByIdRecepteur(idRecepteur);
    }

    @Override
    public Notation addNotation(Notation notation) {
        return notationRepository.save(notation);
    }

    @Override
    public Notation updateNotation(Notation notation) {
        return notationRepository.save(notation);
    }

    @Override
    public void deleteNotation(Long id) {
        notationRepository.deleteById(id);
    }
}
