package pharmacie.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pharmacie.service.ApprovisionnementService;

@RestController
@RequestMapping("/api/approvisionnement")
public class ApprovisionnementController {

    private final ApprovisionnementService approvisionnementService;

    public ApprovisionnementController(ApprovisionnementService approvisionnementService) {
        this.approvisionnementService = approvisionnementService;
    }

    @PostMapping("/lancer")
    public String lancerReapprovisionnement() {
        approvisionnementService.traiterReapprovisionnement();
        return "Mails de réapprovisionnement envoyés avec succès";
    }
}
