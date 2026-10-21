package tn.fss.soa;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {

    private static final String XML_FILE = "/biblio.xml";
    private static final String XSD_FILE = "/biblio.xsd";

    public static void main(String[] args) throws Exception {
        URL xmlUrl = Main.class.getResource(XML_FILE);
        URL xsdUrl = Main.class.getResource(XSD_FILE);

        System.out.println("VALIDATION XML");
        if (!valider(xmlUrl, xsdUrl)) {
            System.out.println("Les requêtes XPath ne sont pas exécutées : document invalide.");
            return;
        }

        System.out.println("\nINTERROGATION XPATH ");
        interroger(xmlUrl);
    }


    static boolean valider(URL xmlUrl, URL xsdUrl) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdUrl);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlUrl.toExternalForm()));
            System.out.println("Le document biblio.xml est VALIDE par rapport à biblio.xsd.");
            return true;
        } catch (SAXException e) {
            System.out.println("Le document est INVALIDE : " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Erreur technique : " + e.getMessage());
            return false;
        }
    }

    static void interroger(URL xmlUrl) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(xmlUrl.openStream());
        XPath xpath = XPathFactory.newInstance().newXPath();

        String aujourdhui = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        afficher("Q1. Titres de tous les livres",
                liste(xpath, doc, "//livre/titre"));

        afficher("Q2. Livres actuellement disponibles",
                liste(xpath, doc, "//livre[@disponible='true']/titre"));

        afficher("Q3. Titres de la catégorie roman",
                liste(xpath, doc, "//livre[@categorie='roman']/titre"));

        afficher("Q4. Adhérents actifs",
                liste(xpath, doc, "//adherent[@actif='true']/nom"));

        afficher("Q5. Prêts en cours (livre non rendu)",
                attributs(xpath, doc, "//pret[not(dateRetourEffective)]/@id"));

        afficher("Q6. Prêts en retard au " + aujourdhui,
                attributs(xpath, doc,
                        "//pret[not(dateRetourEffective) and "
                                + "number(translate(dateRetourPrevue,'-','')) < " + aujourdhui + "]/@id"));

        afficher("Q7. Événements de plus de 20 places",
                liste(xpath, doc, "//evenement[capacite > 20]/titre"));

        afficher("Q8. Nom de l'adhérent ayant emprunté le livre L001",
                liste(xpath, doc,
                        "//adherent[@id = //pret[@livreRef='L001']/@adherentRef]/nom"));

        XPathExpression comptage = xpath.compile("count(//pret[not(dateRetourEffective)])");
        Double nb = (Double) comptage.evaluate(doc, XPathConstants.NUMBER);
        System.out.println("\nQ9. Nombre de prêts en cours : " + nb.intValue());
    }

    static NodeList liste(XPath xpath, Document doc, String expression) throws Exception {
        XPathExpression expr = xpath.compile(expression);
        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    }

    static NodeList attributs(XPath xpath, Document doc, String expression) throws Exception {
        return liste(xpath, doc, expression);
    }

    static void afficher(String titre, NodeList resultats) {
        System.out.println("\n" + titre);
        if (resultats.getLength() == 0) {
            System.out.println("   (aucun résultat)");
            return;
        }
        for (int i = 0; i < resultats.getLength(); i++) {
            Node n = resultats.item(i);
            System.out.println("   - " + n.getTextContent());
        }
    }
}