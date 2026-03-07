package pharmacie.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Categorie;
import pharmacie.entity.Fournisseur;
import pharmacie.entity.Medicament;

@Service
public class ApprovisionnementService {

    private final MedicamentRepository medicamentRepository;
    private final JavaMailSender mailSender;

    public ApprovisionnementService(MedicamentRepository medicamentRepository, JavaMailSender mailSender) {
        this.medicamentRepository = medicamentRepository;
        this.mailSender = mailSender;
    }

    public void traiterReapprovisionnement() {
        List<Medicament> medicaments = medicamentRepository.findMedicamentsAReapprovisionner();
        Map<Fournisseur, List<Medicament>> parFournisseur = regrouperParFournisseur(medicaments);
        for (Map.Entry<Fournisseur, List<Medicament>> entry : parFournisseur.entrySet()) {
            envoyerMailDevis(entry.getKey(), entry.getValue());
        }
    }

    private Map<Fournisseur, List<Medicament>> regrouperParFournisseur(List<Medicament> medicaments) {
        Map<Fournisseur, List<Medicament>> map = new HashMap<>();
        for (Medicament medicament : medicaments) {
            for (Fournisseur fournisseur : medicament.getCategorie().getFournisseurs()) {
                if (!map.containsKey(fournisseur)) {
                    map.put(fournisseur, new ArrayList<>());
                }
                map.get(fournisseur).add(medicament);
            }
        }
        return map;
    }

    private Map<Categorie, List<Medicament>> regrouperParCategorie(List<Medicament> medicaments) {
        Map<Categorie, List<Medicament>> map = new HashMap<>();
        for (Medicament medicament : medicaments) {
            Categorie categorie = medicament.getCategorie();
            if (!map.containsKey(categorie)) {
                map.put(categorie, new ArrayList<>());
            }
            map.get(categorie).add(medicament);
        }
        return map;
    }

    private void envoyerMailDevis(Fournisseur fournisseur, List<Medicament> medicaments) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(fournisseur.getAdresseElectronique());
        message.setSubject("Demande de devis pour réapprovisionnement");
        message.setText(construireCorpsMail(fournisseur, medicaments));
        mailSender.send(message);
    }

    private String construireCorpsMail(Fournisseur fournisseur, List<Medicament> medicaments) {
        Map<Categorie, List<Medicament>> parCategorie = regrouperParCategorie(medicaments);
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(fournisseur.getNom()).append(",\n\n");
        sb.append("Nous souhaitons commander les médicaments suivants :\n\n");
        for (Map.Entry<Categorie, List<Medicament>> entry : parCategorie.entrySet()) {
            Categorie categorie = entry.getKey();
            List<Medicament> medicamentsDeLaCategorie = entry.getValue();
            sb.append("Catégorie : ").append(categorie.getLibelle()).append("\n");
            for (Medicament m : medicamentsDeLaCategorie) {
                sb.append("  - ").append(m.getNom())
                  .append(" (stock : ").append(m.getUnitesEnStock())
                  .append(", seuil : ").append(m.getNiveauDeReappro())
                  .append(")\n");
            }
            sb.append("\n");
        }
        sb.append("Merci de nous faire parvenir un devis.\n");
        sb.append("Cordialement.");
        return sb.toString();
    }
}
