// Josh Duke
// JTD210000
// Programming Assignment 3
// Resolution Theorem Solver


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private static List<List<String>> kb = new ArrayList<>();
    private static Map<Integer, int[]> parentClauses = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <path_to_kb_file>");
            return;
        }

        String inputFile = args[0];
        parseInputFile(inputFile);

        resolutionAlgorithm();

        printOutput();
    }

    private static void parseInputFile(String inputFile) {
        try (Scanner scanner = new Scanner(new File(inputFile))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    List<String> clause = Arrays.asList(line.split("\\s+"));
                    kb.add(clause);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + inputFile);
        }

        List<String> clauseToProve = kb.remove(kb.size() - 1);
        List<String> negatedClause = negateClause(clauseToProve);
        kb.addAll(Collections.singletonList(splitClause(negatedClause)));
    }

    private static List<String> negateClause(List<String> clause) {
        List<String> negatedClause = new ArrayList<>();
        for (String literal : clause) {
            if (literal.startsWith("~")) {
                negatedClause.add(literal.substring(1));
            } else {
                negatedClause.add("~" + literal);
            }
        }
        return negatedClause;
    }

    private static List<String> splitClause(List<String> clause) {
        List<String> splitClauses = new ArrayList<>();
        for (String literal : clause) {
            if (literal.contains("&")) {
                splitClauses.addAll(Arrays.asList(literal.split("&")));
            } else {
                splitClauses.add(literal);
            }
        }
        return splitClauses;
    }

    private static void resolutionAlgorithm() {
        int n = kb.size();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                List<String> resolvent = resolve(kb.get(i), kb.get(j));
                if (resolvent != null) {
                    resolvent = standardizeClause(resolvent);
                    if (!isRedundantClause(resolvent) && !resolvent.isEmpty()) {
                        kb.add(resolvent);
                        parentClauses.put(kb.size() - 1, new int[]{i + 1, j + 1});
                        n++;
                    }
                    if (resolvent.isEmpty()) {
                        kb.add(Collections.singletonList("Contradiction"));
                        parentClauses.put(kb.size() - 1, new int[]{i + 1, j + 1});
                        return;
                    }
                }
            }
        }
    }

    private static List<String> resolve(List<String> clause1, List<String> clause2) {
        for (String literal1 : clause1) {
            for (String literal2 : clause2) {
                if (isNegation(literal1, literal2)) {
                    List<String> resolvent = new ArrayList<>(clause1);
                    resolvent.remove(literal1);
                    resolvent.addAll(clause2);
                    resolvent.remove(literal2);
                    return resolvent;
                }
            }
        }
        return null;
    }

    private static boolean isNegation(String literal1, String literal2) {
        return (literal1.startsWith("~") && literal2.equals(literal1.substring(1))) ||
                (literal2.startsWith("~") && literal1.equals(literal2.substring(1)));
    }

    private static List<String> standardizeClause(List<String> clause) {
        Set<String> uniqueLiterals = new HashSet<>(clause);
        List<String> standardizedClause = new ArrayList<>(uniqueLiterals);
        Collections.sort(standardizedClause);
        return standardizedClause;
    }

    private static boolean isRedundantClause(List<String> clause) {
        for (List<String> existingClause : kb) {
            if (existingClause.equals(clause)) {
                return true;
            }
        }
        return false;
    }

    private static void printOutput() {
        try (PrintWriter writer = new PrintWriter("output.txt")) {
            for (int i = 0; i < kb.size(); i++) {
                List<String> clause = kb.get(i);
                writer.print((i + 1) + ". ");
                writer.print(String.join(" ", clause));

                if (parentClauses.containsKey(i)) {
                    int[] parents = parentClauses.get(i);
                    writer.print(" {" + parents[0] + ", " + parents[1] + "}");
                } else {
                    writer.print(" {}");
                }

                writer.println();
            }
            writer.println(kb.contains(Collections.singletonList("Contradiction")) ? "Valid" : "Fail");
        } catch (FileNotFoundException e) {
            System.out.println("Error creating output file.");
        }
    }
}
