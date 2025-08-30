
import java.io.*;
import java.util.*;

public class Main {

    // ----------- Convert from any base (up to 16) to decimal -----------
    static long toDecimal(String value, int base) {
        long result = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            int digit;
            if (c >= '0' && c <= '9')
                digit = c - '0';
            else
                digit = 10 + (c - 'a');
            result = result * base + digit;
        }
        return result;
    }

    // ----------- Lagrange Interpolation to find f(x) at given x -----------
    static long lagrangeInterpolation(List<long[]> points, long x) {
        int k = points.size();
        long result = 0;

        for (int i = 0; i < k; i++) {
            long xi = points.get(i)[0];
            long yi = points.get(i)[1];

            double term = yi;
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    long xj = points.get(j)[0];
                    term = term * (double) (x - xj) / (double) (xi - xj);
                }
            }
            result += Math.round(term);
        }

        return result;
    }

    // ----------- Build Polynomial Coefficients using Lagrange -----------
    static double[] reconstructPolynomial(List<long[]> points) {
        int k = points.size();
        double[] coeffs = new double[k]; // up to degree k-1

        for (int i = 0; i < k; i++) {
            double[] termPoly = new double[k];
            termPoly[0] = 1.0; // start with "1"

            double denom = 1.0;
            long xi = points.get(i)[0];
            long yi = points.get(i)[1];

            for (int j = 0; j < k; j++) {
                if (i == j)
                    continue;
                long xj = points.get(j)[0];
                denom *= (xi - xj);

                // multiply polynomial by (x - xj)
                for (int d = k - 1; d >= 1; d--) {
                    termPoly[d] = termPoly[d] - xj * termPoly[d - 1];
                }
            }

            double factor = yi / denom;
            for (int d = 0; d < k; d++) {
                coeffs[d] += factor * termPoly[d];
            }
        }
        return coeffs;
    }

    // ----------- Process a single testcase block -----------
    static void processTestcase(String testcaseName, String block) {
        try {
            int n = Integer.parseInt(block.split("\"n\"")[1].split(":")[1].split(",")[0].trim());
            int k = Integer.parseInt(block.split("\"k\"")[1].split(":")[1].split("}")[0].trim());

            // Extract shares
            Map<Integer, long[]> shares = new LinkedHashMap<>();
            for (int i = 1; i <= n; i++) {
                String key = "\"" + i + "\"";
                if (!block.contains(key))
                    continue;

                String part = block.split(key)[1];

                String baseStr = part.split("\"base\"")[1].split(":")[1].split(",")[0].replaceAll("[^0-9]", "").trim();
                if (baseStr.isEmpty())
                    continue;
                int base = Integer.parseInt(baseStr);

                String value = part.split("\"value\"")[1].split(":")[1].replaceAll("[\"{},:]", "").trim();

                long y = toDecimal(value, base);
                shares.put(i, new long[] { i, y });
            }

            // Take first k points for polynomial reconstruction
            List<long[]> basePoints = new ArrayList<>();
            int count = 0;
            for (int key : shares.keySet()) {
                basePoints.add(shares.get(key));
                count++;
                if (count == k)
                    break;
            }

            // Validate all shares
            System.out.println("\n=== " + testcaseName + " ===");
            System.out.println("Checking shares...");
            for (int key : shares.keySet()) {
                long[] point = shares.get(key);
                long expected = lagrangeInterpolation(basePoints, point[0]);

                if (expected != point[1]) {
                    System.out.println("Share " + key + " is WRONG (got " + point[1] + ", expected " + expected + ")");
                } else {
                    System.out.println("Share " + key + " is correct");
                }
            }

            // Print reconstructed polynomial
            System.out.println("\nReconstructed polynomial coefficients:");
            double[] coeffs = reconstructPolynomial(basePoints);
            for (int i = 0; i < coeffs.length; i++) {
                System.out.printf("x^%d: %.2f\n", i, coeffs[i]);
            }

        } catch (Exception e) {
            System.out.println("Error in " + testcaseName + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        // Read JSON file as plain text
        BufferedReader br = new BufferedReader(new FileReader("input.json"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);
        br.close();

        String content = sb.toString();

        // Split by "testcase"
        String[] parts = content.split("\"testcase");
        for (int i = 1; i < parts.length; i++) {
            String testcaseName = "testcase" + parts[i].charAt(0);
            String block = parts[i];
            processTestcase(testcaseName, block);
        }
    }
}
