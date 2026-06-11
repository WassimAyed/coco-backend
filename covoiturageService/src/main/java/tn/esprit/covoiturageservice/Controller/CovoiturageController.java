package tn.esprit.covoiturageservice.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.covoiturageservice.Dto.AdminStatsDTO;
import tn.esprit.covoiturageservice.Dto.CO2ImpactDTO;
import tn.esprit.covoiturageservice.Entity.Covoiturage;
import tn.esprit.covoiturageservice.Service.CO2CalculatorService;
import tn.esprit.covoiturageservice.Service.CovoiturageServiceImp;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/covoiturage")
public class CovoiturageController {
    private final CovoiturageServiceImp covoiturageServiceImp;
    private final CO2CalculatorService co2CalculatorService;

    @PostMapping(value = "/add", consumes = "application/json", produces = "application/json")
    public Covoiturage addCovoiturage(@Valid @RequestBody Covoiturage covoiturage) {
        return covoiturageServiceImp.addCovoiturage(covoiturage);
    }
    @GetMapping("/{id}")
    public Covoiturage getCovoiturageById(@PathVariable Long id) {
        return covoiturageServiceImp.getCovoiturageById(id);
    }
    @GetMapping("/all")
    public List<Covoiturage> getAllCovoiturage() {
        return covoiturageServiceImp.getAllCovoiturage();
    }
    @GetMapping("/driver/{idDriver}")
    public List<Covoiturage> getCovoiturageByIdDriver(@PathVariable Long idDriver) {
        return covoiturageServiceImp.getCovoiturageByIdDriver(idDriver);
    }
    @DeleteMapping("/delete/{id}")
    public void deleteCovoiturage(@PathVariable Long id) {
        covoiturageServiceImp.deleteCovoiturage(id);
    }
    @PutMapping(value = "/update", consumes = "application/json", produces = "application/json")
    public Covoiturage updateCovoiturage(@Valid @RequestBody Covoiturage covoiturage) {
        return covoiturageServiceImp.updateCovoiturage(covoiturage);
    }

    @GetMapping("/{id}/co2-impact")
    public ResponseEntity<CO2ImpactDTO> getCO2Impact(@PathVariable Long id) {
        Covoiturage covoiturage = covoiturageServiceImp.getCovoiturageById(id);
        if (covoiturage == null) {
            return ResponseEntity.notFound().build();
        }
        int placesReservees = covoiturage.getNombrePlaces() - covoiturage.getPlacesDisponibles();
        return ResponseEntity.ok(co2CalculatorService.compute(covoiturage, placesReservees));
    }

    @GetMapping("/{id}/similar")
    public List<Covoiturage> getSimilarCovoiturages(
            @PathVariable Long id,
            @RequestParam(value = "limit", defaultValue = "4") int limit
    ) {
        return covoiturageServiceImp.getSimilarCovoiturages(id, limit);
    }

    @GetMapping("/admin/stats")
    public AdminStatsDTO getAdminStats() {
        return covoiturageServiceImp.getAdminStats();
    }
}
