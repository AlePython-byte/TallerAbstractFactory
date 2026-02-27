/*
la secretaria de la ucc emite documentos como constancias y certificados y cada trámite crea una familia
consistente como: plantillas, reglas y sello
 */

import java.time.LocalDate;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        RequestType type = RequestType.TRANSCRIPT;
        DocumentFactory factory = Factories.forType(type);

        Student s = new Student("UCC-0042", "Alejandro parra", "Ing. Software", 4.2);

        DocumentService service = new DocumentService(factory);
        Document doc = service.issue(s);

        System.out.println("=== " + type + " ===");
        System.out.println(doc.body);
        System.out.println("Sello: " + doc.stamp);
    }
}

enum RequestType { ENROLLMENT, TRANSCRIPT }

final class Factories {
    private Factories() {}
    static DocumentFactory forType(RequestType t) {
        Objects.requireNonNull(t);
        switch (t) {
            case ENROLLMENT: return new EnrollmentFactory();
            case TRANSCRIPT: return new TranscriptFactory();
            default: throw new IllegalArgumentException("Tipo no soportado");
        }
    }
}

abstract class DocumentFactory {
    abstract Template createTemplate();
    abstract Rules createRules();
    abstract Stamper createStamper();
}

abstract class Template { abstract String render(Student s); }
abstract class Rules { abstract void validate(Student s); }
abstract class Stamper { abstract String stamp(Student s); }

final class DocumentService {
    private final Template template;
    private final Rules rules;
    private final Stamper stamper;

    DocumentService(DocumentFactory f) {
        Objects.requireNonNull(f);
        this.template = f.createTemplate();
        this.rules = f.createRules();
        this.stamper = f.createStamper();
    }

    Document issue(Student s) {
        Objects.requireNonNull(s);
        rules.validate(s);
        String body = template.render(s);
        String stamp = stamper.stamp(s);
        return new Document(body, stamp);
    }
}

final class Student {
    final String id, name, program;
    final double gpa;

    Student(String id, String name, String program, double gpa) {
        if (isBlank(id) || isBlank(name) || isBlank(program)) throw new IllegalArgumentException("Datos inválidos");
        if (gpa < 0 || gpa > 5) throw new IllegalArgumentException("GPA fuera de rango");
        this.id = id; this.name = name; this.program = program; this.gpa = gpa;
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}

final class Document {
    final String body, stamp;
    Document(String body, String stamp) {
        this.body = Objects.requireNonNull(body);
        this.stamp = Objects.requireNonNull(stamp);
    }
}

/* ======== Familias concretas (Abstract Factory) ======== */

final class EnrollmentFactory extends DocumentFactory {
    Template createTemplate() { return new EnrollmentTemplate(); }
    Rules createRules() { return new EnrollmentRules(); }
    Stamper createStamper() { return new SimpleStamper("ENR"); }
}
final class EnrollmentTemplate extends Template {
    String render(Student s) {
        return "CONSTANCIA DE MATRÍCULA\nEstudiante: " + s.name + "\nID: " + s.id +
                "\nPrograma: " + s.program + "\nFecha: " + LocalDate.now();
    }
}
final class EnrollmentRules extends Rules {
    void validate(Student s) {
        if (s.gpa <= 0) throw new IllegalStateException("No se emite matrícula con GPA inválido");
    }
}

final class TranscriptFactory extends DocumentFactory {
    Template createTemplate() { return new TranscriptTemplate(); }
    Rules createRules() { return new TranscriptRules(); }
    Stamper createStamper() { return new SimpleStamper("TRN"); }
}
final class TranscriptTemplate extends Template {
    String render(Student s) {
        return "CERTIFICADO DE NOTAS\nEstudiante: " + s.name + "\nID: " + s.id +
                "\nPrograma: " + s.program + "\nGPA: " + String.format("%.2f", s.gpa) +
                "\nFecha: " + LocalDate.now();
    }
}
final class TranscriptRules extends Rules {
    void validate(Student s) {
        if (s.gpa < 1.0) throw new IllegalStateException("GPA demasiado bajo para certificado");
    }
}

final class SimpleStamper extends Stamper {
    private final String prefix;
    SimpleStamper(String prefix) { this.prefix = Objects.requireNonNull(prefix); }
    String stamp(Student s) {
        String last4 = s.id.length() >= 4 ? s.id.substring(s.id.length() - 4) : s.id;
        return prefix + "-" + last4 + "-" + LocalDate.now().getDayOfYear();
    }
}