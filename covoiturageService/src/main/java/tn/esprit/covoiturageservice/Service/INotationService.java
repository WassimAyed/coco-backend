package tn.esprit.covoiturageservice.Service;

import tn.esprit.covoiturageservice.Entity.Notation;

import java.util.List;

public interface INotationService {
    Notation getNotationById(Long id);
    List<Notation> getAllNotations();
    List<Notation> getNotationsByDonneur(long idDonneur);
    List<Notation> getNotationsByRecepteur(long idRecepteur);
    Notation addNotation(Notation notation);
    Notation updateNotation(Notation notation);
    void deleteNotation(Long id);
}
