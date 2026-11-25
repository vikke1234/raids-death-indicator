package com.example.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generates the RuneLite config menu, NPC registry and enemy factories from
 * @Tracked declarations. Kept in a separate source set so it can run before
 * main compilation without depending on main classes.
 */
@SupportedAnnotationTypes("com.example.enemydata.Tracked")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public final class EnemyConfigProcessor extends AbstractProcessor {
    private static final String TRACKED = "com.example.enemydata.Tracked";
    private static final String GEN_PACKAGE = "com.example.generated";
    private static final String CONFIG = "GeneratedEnemyConfig";
    private static final String ENUM = "TrackedEnemy";
    private static final String CONTEXT = "EnemySpawnContext";

    private final Map<String, EnemyDef> enemies = new LinkedHashMap<>();
    private final Map<Integer, String> seenIds = new HashMap<>();
    private boolean generated;

    private static final class EnemyDef {
        final String qualifiedName;
        final String key;
        final String constant;
        final String method;
        final String name;
        final List<Integer> ids;
        final String raid;
        final String mode;
        final boolean boss;
        final String constructorArgs;

        EnemyDef(String qualifiedName, String key, String name, List<Integer> ids,
                String raid, String mode, boolean boss, String constructorArgs) {
            this.qualifiedName = qualifiedName;
            this.key = key;
            this.constant = enumConstant(key);
            this.method = "enemy" + camel(key, true) + "Mode";
            this.name = name;
            this.ids = ids;
            this.raid = raid;
            this.mode = mode;
            this.boss = boss;
            this.constructorArgs = constructorArgs;
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver() || generated) {
            return false;
        }

        TypeElement tracked = processingEnv.getElementUtils().getTypeElement(TRACKED);
        if (tracked != null) {
            for (Element element : roundEnv.getElementsAnnotatedWith(tracked)) {
                read((TypeElement) element);
            }
        }

        if (!enemies.isEmpty()) {
            generated = true;
            try {
                List<EnemyDef> defs = new ArrayList<>(enemies.values());
                defs.sort(Comparator.comparing(e -> e.name));
                generateConfig(defs);
                generateEnum(defs);
                generateContext();
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Failed to generate enemy config: " + e.getMessage());
            }
        }
        return false;
    }

    private void read(TypeElement type) {
        AnnotationMirror mirror = processingEnv.getElementUtils()
                .getAllAnnotationMirrors(type).stream()
                .filter(a -> TRACKED.equals(a.getAnnotationType().asElement().toString()))
                .findFirst()
                .orElse(null);
        if (mirror == null) {
            return;
        }

        Map<String, AnnotationValue> values = new LinkedHashMap<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                : processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).entrySet()) {
            values.put(entry.getKey().getSimpleName().toString(), entry.getValue());
        }

        String className = type.getSimpleName().toString();
        String key = text(values.get("key"));
        if (key.isEmpty()) {
            key = snake(className);
        }
        if (enemies.containsKey(key)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Duplicate @Tracked key '" + key + "'", type);
            return;
        }

        String name = text(values.get("name"));
        if (name.isEmpty()) {
            name = spaced(className);
        }

        List<Integer> ids = new ArrayList<>();
        AnnotationValue idsValue = values.get("npcIds");
        if (idsValue != null && idsValue.getValue() instanceof List) {
            for (Object item : (List<?>) idsValue.getValue()) {
                Object value = ((AnnotationValue) item).getValue();
                if (value instanceof Integer) {
                    ids.add((Integer) value);
                }
            }
        }
        if (ids.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@Tracked must declare at least one npc id", type);
            return;
        }
        for (int id : ids) {
            String previous = seenIds.putIfAbsent(id, key);
            if (previous != null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "NPC id " + id + " is declared by both '" + previous + "' and '" + key + "'", type);
                return;
            }
        }

        enemies.put(key, new EnemyDef(
                type.getQualifiedName().toString(),
                key,
                name,
                ids,
                enumName(values.get("raid"), "MISC"),
                enumName(values.get("defaultMode"), "HIGHLIGHT_AND_HIDE"),
                booleanValue(values.get("boss")),
                constructorArgs(type)));
    }

    private String constructorArgs(TypeElement type) {
        for (ExecutableElement constructor : ElementFilter.constructorsIn(type.getEnclosedElements())) {
            List<? extends VariableElement> params = constructor.getParameters();
            if (params.isEmpty() || !params.get(0).asType().toString().endsWith("NPC")) {
                continue;
            }
            StringBuilder args = new StringBuilder("npc");
            for (int i = 1; i < params.size(); i++) {
                args.append(", ").append(contextArg(params.get(i)));
            }
            return args.toString();
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "@Tracked class must have an NPC constructor", type);
        return "npc";
    }

    private String contextArg(VariableElement param) {
        String name = param.getSimpleName().toString();
        switch (name) {
            case "isCm":
            case "invocation":
            case "partySize":
            case "maxCombat":
            case "maxHp":
            case "pathLevel":
            case "raidTick":
            case "client":
                return "ctx." + name;
            default:
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Unsupported @Tracked constructor parameter '" + name + "'", param);
                return "null";
        }
    }

    private void generateConfig(List<EnemyDef> defs) throws IOException {
        JavaFileObject file = processingEnv.getFiler().createSourceFile(GEN_PACKAGE + "." + CONFIG);
        try (PrintWriter out = new PrintWriter(file.openWriter())) {
            out.println("package " + GEN_PACKAGE + ";");
            out.println();
            out.println("import com.example.AkkhaPredictorConfig;");
            out.println("import com.example.enemydata.Raid;");
            out.println("import com.example.enemydata.TrackingMode;");
            out.println("import net.runelite.client.config.ConfigGroup;");
            out.println("import net.runelite.client.config.ConfigItem;");
            out.println("import net.runelite.client.config.ConfigSection;");
            out.println();
            out.println("/** Generated by EnemyConfigProcessor — do not edit. */");
            out.println("@ConfigGroup(\"raid-death-indicator\")");
            out.println("public interface " + CONFIG + " extends AkkhaPredictorConfig {");

            Set<String> raids = new TreeSet<>();
            for (EnemyDef def : defs) {
                raids.add(def.raid);
            }
            int position = 300;
            for (String raid : raids) {
                String section = "enemySection" + camel(raid, true);
                out.println();
                out.println("    @ConfigSection(");
                out.println("            name = \"" + raidLabel(raid) + "\",");
                out.println("            description = \"Per-enemy tracking for " + raidLabel(raid) + "\",");
                out.println("            position = " + position + ",");
                out.println("            closedByDefault = true");
                out.println("    )");
                out.println("    String " + section + " = \"" + section + "\";");
                position += 10;
            }

            for (EnemyDef def : defs) {
                String section = "enemySection" + camel(def.raid, true);
                out.println();
                out.println("    @ConfigItem(");
                out.println("            keyName = \"enemy." + def.key + "\",");
                out.println("            name = \"" + escape(def.name) + "\",");
                out.println("            description = \"How " + escape(def.name) + " is tracked.\",");
                out.println("            section = " + section);
                out.println("    )");
                out.println("    default TrackingMode " + def.method + "() {");
                out.println("        return TrackingMode." + def.mode + ";");
                out.println("    }");
            }
            out.println("}");
        }
    }

    private void generateEnum(List<EnemyDef> defs) throws IOException {
        JavaFileObject file = processingEnv.getFiler().createSourceFile(GEN_PACKAGE + "." + ENUM);
        try (PrintWriter out = new PrintWriter(file.openWriter())) {
            out.println("package " + GEN_PACKAGE + ";");
            out.println();
            out.println("import com.example.enemydata.Enemy;");
            out.println("import com.example.enemydata.Raid;");
            out.println("import com.example.enemydata.TrackingMode;");
            out.println("import net.runelite.api.NPC;");
            out.println("import java.util.Optional;");
            out.println();
            out.println("/** Generated by EnemyConfigProcessor — do not edit. */");
            out.println("public enum " + ENUM + " {");

            for (int i = 0; i < defs.size(); i++) {
                EnemyDef def = defs.get(i);
                out.println("    " + def.constant + "(\"" + def.key + "\", \"" + escape(def.name) + "\", Raid." + def.raid
                        + ", TrackingMode." + def.mode + ", " + def.boss
                        + ", " + intsLiteral(def.ids) + ")"
                        + (i == defs.size() - 1 ? ";" : ","));
            }
            if (defs.isEmpty()) {
                out.println("    ;");
            }

            out.println();
            out.println("    private final String key;");
            out.println("    private final String name;");
            out.println("    private final Raid raid;");
            out.println("    private final TrackingMode defaultMode;");
            out.println("    private final boolean boss;");
            out.println("    private final int[] npcIds;");
            out.println();
            out.println("    " + ENUM + "(String key, String name, Raid raid, TrackingMode defaultMode, boolean boss, int[] npcIds) {");
            out.println("        this.key = key;");
            out.println("        this.name = name;");
            out.println("        this.raid = raid;");
            out.println("        this.defaultMode = defaultMode;");
            out.println("        this.boss = boss;");
            out.println("        this.npcIds = npcIds;");
            out.println("    }");
            out.println();
            out.println("    public String key() { return key; }");
            out.println("    public String displayName() { return name; }");
            out.println("    public Raid raid() { return raid; }");
            out.println("    public TrackingMode defaultMode() { return defaultMode; }");
            out.println("    public boolean isBoss() { return boss; }");
            out.println("    public int[] npcIds() { return npcIds.clone(); }");
            out.println();
            out.println("    public TrackingMode mode(" + CONFIG + " config) {");
            out.println("        switch (this) {");
            for (EnemyDef def : defs) {
                out.println("            case " + def.constant + ": return config." + def.method + "();");
            }
            out.println("            default: return defaultMode;");
            out.println("        }");
            out.println("    }");
            out.println();
            out.println("    public Enemy create(NPC npc, " + CONTEXT + " ctx) {");
            out.println("        switch (this) {");
            for (EnemyDef def : defs) {
                out.println("            case " + def.constant + ": return new " + def.qualifiedName + "(" + def.constructorArgs + ");");
            }
            out.println("            default: return null;");
            out.println("        }");
            out.println("    }");
            out.println();
            out.println("    public static Optional<" + ENUM + "> forNpcId(int id) {");
            out.println("        for (" + ENUM + " enemy : values()) {");
            out.println("            for (int npcId : enemy.npcIds) {");
            out.println("                if (npcId == id) {");
            out.println("                    return Optional.of(enemy);");
            out.println("                }");
            out.println("            }");
            out.println("        }");
            out.println("        return Optional.empty();");
            out.println("    }");
            out.println("}");
        }
    }

    private void generateContext() throws IOException {
        JavaFileObject file = processingEnv.getFiler().createSourceFile(GEN_PACKAGE + "." + CONTEXT);
        try (PrintWriter out = new PrintWriter(file.openWriter())) {
            out.println("package " + GEN_PACKAGE + ";");
            out.println();
            out.println("import net.runelite.api.Client;");
            out.println();
            out.println("/** Generated by EnemyConfigProcessor — do not edit. */");
            out.println("public final class " + CONTEXT + " {");
            out.println("    public final boolean isCm;");
            out.println("    public final int invocation;");
            out.println("    public final int partySize;");
            out.println("    public final int maxCombat;");
            out.println("    public final int maxHp;");
            out.println("    public final int pathLevel;");
            out.println("    public final long raidTick;");
            out.println("    public final Client client;");
            out.println();
            out.println("    private " + CONTEXT + "(boolean isCm, int invocation, int partySize, int maxCombat,");
            out.println("            int maxHp, int pathLevel, long raidTick, Client client) {");
            out.println("        this.isCm = isCm;");
            out.println("        this.invocation = invocation;");
            out.println("        this.partySize = partySize;");
            out.println("        this.maxCombat = maxCombat;");
            out.println("        this.maxHp = maxHp;");
            out.println("        this.pathLevel = pathLevel;");
            out.println("        this.raidTick = raidTick;");
            out.println("        this.client = client;");
            out.println("    }");
            out.println();
            out.println("    public static " + CONTEXT + " cox(boolean isCm, int partySize, int maxCombat, int maxHp, long raidTick) {");
            out.println("        return new " + CONTEXT + "(isCm, 0, partySize, maxCombat, maxHp, 0, raidTick, null);");
            out.println("    }");
            out.println();
            out.println("    public static " + CONTEXT + " toa(int invocation, int partySize, int pathLevel) {");
            out.println("        return new " + CONTEXT + "(false, invocation, partySize, 0, 0, pathLevel, 0, null);");
            out.println("    }");
            out.println();
            out.println("    public static " + CONTEXT + " tfa(Client client) {");
            out.println("        return new " + CONTEXT + "(false, 0, 0, 0, 0, 0, 0, client);");
            out.println("    }");
            out.println();
            out.println("    public static " + CONTEXT + " misc(Client client) {");
            out.println("        return new " + CONTEXT + "(false, 0, 0, 0, 0, 0, 0, client);");
            out.println("    }");
            out.println("}");
        }
    }

    private static String raidLabel(String raid) {
        switch (raid) {
            case "TOA": return "Tombs of Amascut";
            case "COX": return "Chambers of Xeric";
            case "TFA": return "The Fractured Archive";
            default: return "Miscellaneous";
        }
    }

    private static String text(AnnotationValue value) {
        return value == null || value.getValue() == null ? "" : value.getValue().toString();
    }

    private static boolean booleanValue(AnnotationValue value) {
        return value != null && Boolean.parseBoolean(text(value));
    }

    private static String intsLiteral(List<Integer> values) {
        StringBuilder sb = new StringBuilder("new int[]{");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(values.get(i));
        }
        return sb.append('}').toString();
    }

    private static String enumName(AnnotationValue value, String fallback) {
        if (value == null || value.getValue() == null) {
            return fallback;
        }
        String raw = value.getValue().toString();
        int lastDot = raw.lastIndexOf('.');
        return lastDot < 0 ? raw : raw.substring(lastDot + 1);
    }

    private static String snake(String value) {
        String s = value.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                .toLowerCase();
        if (Character.isDigit(s.charAt(0))) {
            s = "enemy_" + s;
        }
        return s;
    }

    private static String spaced(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1 $2");
    }

    private static String enumConstant(String key) {
        return key.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
    }

    private static String camel(String value, boolean upperFirst) {
        StringBuilder sb = new StringBuilder();
        boolean upper = upperFirst;
        for (char c : value.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
