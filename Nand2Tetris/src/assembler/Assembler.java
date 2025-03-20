package assembler;
import java.io.*;
import java.util.*;

public class Assembler {

    public static List<String> removeWhiteSpace(String asmfile) throws IOException {
        List<String> lines = new ArrayList<>();
        List<String> newLines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(asmfile));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("//")) {
                String cleanLine = line.replaceAll("\\s", ""); // Remove all whitespace
                if (!cleanLine.isEmpty()) {
                    lines.add(cleanLine);
                }
            }
        }
        reader.close();

        for (String l : lines) {
            if (!l.startsWith("//")) {
                newLines.add(l);
            }
        }
        return newLines;
    }

    public static Map<String, Integer> handleLabels(List<String> instructions) {
        int i = 0;
        Map<String, Integer> symbolTable = new HashMap<>();
        for (String instruction : instructions) {
            int openIdx = instruction.indexOf("(");
            int closeIdx = instruction.indexOf(")");
            if (openIdx != -1 && closeIdx != -1 && openIdx < closeIdx) {
                System.out.println(instruction);
                symbolTable.put(instruction.substring(openIdx + 1, closeIdx), i);
            } else {
                i++;
                System.out.println("pass");
            }
        }
        return symbolTable;
    }

    public static String instructionType(String instruction) {
        System.out.println("instruction: " + instruction);
        if (instruction.startsWith("(")) {
            return "L";
        } else if (instruction.startsWith("@")) {
            return "A";
        } else {
            return "C";
        }
    }

    public static String handleCompInstruction(String computation) {
        String compStr = getCompSubstring(computation);
        String destStr = computation.split("=")[0];
        String dest = calculateDestBits(destStr);
        String[] aComp = calculateCompBits(compStr);
        String jumpStr = getJumpSubstring(computation);
        String jump = calculateJumpBits(jumpStr);
        return "111" + aComp[0] + aComp[1] + dest + jump;
    }

    public static String[] calculateCompBits(String comp) {
        Map<String, String> a0Instructions = new HashMap<>();
        a0Instructions.put("0", "101010");
        a0Instructions.put("1", "111111");
        a0Instructions.put("-1", "111010");
        a0Instructions.put("D", "001100");
        a0Instructions.put("A", "110000");
        a0Instructions.put("!D", "001101");
        a0Instructions.put("!A", "110001");
        a0Instructions.put("-D", "001111");
        a0Instructions.put("-A", "110011");
        a0Instructions.put("D+1", "011111");
        a0Instructions.put("A+1", "110111");
        a0Instructions.put("D-1", "001110");
        a0Instructions.put("A-1", "110010");
        a0Instructions.put("D+A", "000010");
        a0Instructions.put("D-A", "010011");
        a0Instructions.put("A-D", "000111");
        a0Instructions.put("D&A", "000000");
        a0Instructions.put("D|A", "010101");

        Map<String, String> a1Instructions = new HashMap<>();
        a1Instructions.put("M", "110000");
        a1Instructions.put("!M", "110001");
        a1Instructions.put("-M", "110011");
        a1Instructions.put("M+1", "110111");
        a1Instructions.put("M-1", "110010");
        a1Instructions.put("D+M", "000010");
        a1Instructions.put("D-M", "010011");
        a1Instructions.put("M-D", "000111");
        a1Instructions.put("D&M", "000000");
        a1Instructions.put("D|M", "010101");

        System.out.println("Comp: " + comp);
        String a = "";
        String compBits = "";
        if (a0Instructions.containsKey(comp)) {
            a = "0";
            compBits = a0Instructions.get(comp);
        } else if (a1Instructions.containsKey(comp)) {
            a = "1";
            compBits = a1Instructions.get(comp);
        }
        return new String[]{a, compBits};
    }

    public static String getCompSubstring(String comp) {
        if (comp.contains("=")) {
            comp = comp.split("=")[1];
        }
        if (comp.contains(";")) {
            comp = comp.split(";")[0];
        }
        return comp;
    }

    public static String getJumpSubstring(String jump) {
        if (jump.contains(";")) {
            jump = jump.split(";")[1];
        } else {
            jump = "null";
        }
        return jump;
    }

    public static String calculateDestBits(String dest) {
        System.out.println("Dest: " + dest);
        Map<String, String> destDict = new HashMap<>();
        destDict.put("null", "000");
        destDict.put("M", "001");
        destDict.put("D", "010");
        destDict.put("MD", "011");
        destDict.put("A", "100");
        destDict.put("AM", "101");
        destDict.put("AD", "110");
        destDict.put("AMD", "111");

        return destDict.getOrDefault(dest, "000");
    }

    public static String calculateJumpBits(String jump) {
        Map<String, String> jumpDict = new HashMap<>();
        jumpDict.put("null", "000");
        jumpDict.put("JGT", "001");
        jumpDict.put("JEQ", "010");
        jumpDict.put("JGE", "011");
        jumpDict.put("JLT", "100");
        jumpDict.put("JNE", "101");
        jumpDict.put("JLE", "110");
        jumpDict.put("JMP", "111");

        return jumpDict.getOrDefault(jump, "");
    }

    public static List<String> assembleCode(String asmfile) throws IOException {
        List<String> instructions = removeWhiteSpace(asmfile);
        List<String> binaryResult = parseInstructions(instructions);
        String filename = formatFileName(asmfile);
        writeMachineCode(filename, binaryResult);
        return binaryResult;
    }
    
    public static String formatFileName(String asmfile) {
        // Get the base name of the input file (without extension)
        String baseName = new File(asmfile).getName().replaceFirst("[.][^.]+$", "");
        // Get the current working directory
        String currentDirectory = System.getProperty("user.dir");
        // Create the output file path in the current directory
        return currentDirectory + File.separator + baseName + ".hack";
    }

    public static void writeMachineCode(String filename, List<String> machineCode) throws IOException {
        System.out.println("writing " + filename);
    	BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (String line : machineCode) {
            writer.write(line + "\n");
        }
        writer.close();
    }

    public static List<String> parseInstructions(List<String> instructions) {
        int MEM = 16;
        List<String> machineCode = new ArrayList<>();
        Map<String, String> symbolTable = new HashMap<>();
        symbolTable.put("R0", "0");
        symbolTable.put("R1", "1");
        symbolTable.put("R2", "2");
        symbolTable.put("R3", "3");
        symbolTable.put("R4", "4");
        symbolTable.put("R5", "5");
        symbolTable.put("R6", "6");
        symbolTable.put("R7", "7");
        symbolTable.put("R8", "8");
        symbolTable.put("R9", "9");
        symbolTable.put("R10", "10");
        symbolTable.put("R11", "11");
        symbolTable.put("R12", "12");
        symbolTable.put("R13", "13");
        symbolTable.put("R14", "14");
        symbolTable.put("R15", "15");
        symbolTable.put("SP", "0");
        symbolTable.put("LCL", "1");
        symbolTable.put("ARG", "2");
        symbolTable.put("THIS", "3");
        symbolTable.put("THAT", "4");
        symbolTable.put("SCREEN", "16384");
        symbolTable.put("KBD", "24576");
     // Fix: Convert Integer values to String before adding to symbolTable
        Map<String, Integer> labelMap = handleLabels(instructions);
        for (Map.Entry<String, Integer> entry : labelMap.entrySet()) {
            symbolTable.put(entry.getKey(), entry.getValue().toString());
        }
        //ymbolTable.putAll(handleLabels(instructions));

        for (String instruction : instructions) {
            String type = instructionType(instruction);
            if (type.equals("A") && !instruction.substring(1).matches("\\d+")) {
                if (!symbolTable.containsKey(instruction.substring(1))) {
                    System.out.println("adding new symbol: " + instruction.substring(1));
                    symbolTable.put(instruction.substring(1), String.valueOf(MEM));
                    String value = symbolTable.get(instruction.substring(1));
                    String binaryValue = String.format("%16s", Integer.toBinaryString(Integer.parseInt(value))).replace(' ', '0');
                    machineCode.add(binaryValue);
                    MEM++;
                } else {
                    String value = symbolTable.get(instruction.substring(1));
                    System.out.println(value);
                    String binaryValue = String.format("%16s", Integer.toBinaryString(Integer.parseInt(value))).replace(' ', '0');
                    machineCode.add(binaryValue);
                }
            } else if (type.equals("A") && instruction.substring(1).matches("\\d+")) {
                System.out.println(instruction.substring(1));
                System.out.println(Integer.toBinaryString(Integer.parseInt(instruction.substring(1))));
                String binaryValue = String.format("%16s", Integer.toBinaryString(Integer.parseInt(instruction.substring(1)))).replace(' ', '0');
                machineCode.add(binaryValue);
            } else if (type.equals("L")) {
                continue;
            } else if (type.equals("C")) {
                machineCode.add(handleCompInstruction(instruction));
            }
        }
        System.out.println("Here is the symbol_table: ");
        System.out.println(symbolTable);
        return machineCode;
    }
    public static void main(String[] args) throws IOException {
        String directory = "C:\\Users\\carso\\Documents\\MachineLearningHw\\Group_Project\\Assembler\\06";
        File dir = new File(directory);
        for (File file : dir.listFiles()) {
            System.out.println("starting " + file.getName());
            System.out.println(assembleCode(file.getAbsolutePath()));
        }
    }
}