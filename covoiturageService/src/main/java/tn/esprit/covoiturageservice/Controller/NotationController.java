package tn.esprit.covoiturageservice.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.covoiturageservice.Entity.Notation;
import tn.esprit.covoiturageservice.Service.INotationService;

import java.util.List;

@RestController
@RequestMapping("/covoiturage/notation")
@RequiredArgsConstructor
public class NotationController {

    private final INotationService notationService;

    @PostMapping("/add")
    public Notation addNotation(@RequestBody Notation notation) {
        return notationService.addNotation(notation);
    }

    @GetMapping("/{id}")
    public Notation getNotationById(@PathVariable Long id) {
        return notationService.getNotationById(id);
    }

    @GetMapping("/all")
    public List<Notation> getAllNotations() {
        return notationService.getAllNotations();
    }

    @GetMapping("/donneur/{idDonneur}")
    public List<Notation> getNotationsByDonneur(@PathVariable long idDonneur) {
        return notationService.getNotationsByDonneur(idDonneur);
    }

    @GetMapping("/recepteur/{idRecepteur}")
    public List<Notation> getNotationsByRecepteur(@PathVariable long idRecepteur) {
        return notationService.getNotationsByRecepteur(idRecepteur);
    }

    @PutMapping("/update")
    public Notation updateNotation(@RequestBody Notation notation) {
        return notationService.updateNotation(notation);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteNotation(@PathVariable Long id) {
        notationService.deleteNotation(id);
    }
}
