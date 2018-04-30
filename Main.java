package gr.teilar;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String s, tos, lookahead;
        Stack st = new Stack();
        String[] nonTerminals;
        List<String> terminals = new ArrayList<>();
        Map<String,List<String>> rulesMap = new LinkedHashMap<>();
        Map<String, String> firstMap = new LinkedHashMap<>();
        Map<String, String> followMap = new LinkedHashMap<>();
        ArrayList<String>[] values = new ArrayList[3];
        String[][] M;

        /* -----------------------  Rules Map Creation and Insertion  ---------------------------------------- */
        for(int i = 0; i < values.length; i++)
            values[i] = new ArrayList<>();

        // S rules
        values[0].add("if E then S else S");
        values[0].add("begin S L");
        values[0].add("print E");

        rulesMap.put("S", values[0]);

        // L rules
        values[1].add("end");
        values[1].add("; S L");

        rulesMap.put("L", values[1]);

        // E rules
        values[2].add("num = num");

        rulesMap.put("E", values[2]);
        /* --------------------------------------------------------------------------------------------------- */

        nonTerminals = rulesMap.keySet().toArray(new String[rulesMap.size()]);

        /* -----------------------  First Map Creation and Insertion  ---------------------------------------- */
        for(Map.Entry<String, List<String>> entry : rulesMap.entrySet()) {
            StringBuilder temp = new StringBuilder();

            for(String rule : entry.getValue()) {
                StringBuilder ruleTemp = new StringBuilder();
                String str = rule.split("\\s+")[0];
                String nTerm;

                if(isTerminal(str, nonTerminals)) {
                    if(ruleTemp.toString().equals(""))
                        ruleTemp = new StringBuilder(str);
                    if(ruleTemp.toString().contains(str))
                        ruleTemp.append("");
                    else
                        ruleTemp.append(", ").append(str);
                }
                else {
                    for(String string : rulesMap.get(str)) {
                        nTerm = string.split("\\s+")[0];

                        while(!isTerminal(nTerm, nonTerminals)) {
                            for (String stro : rulesMap.get(nTerm)) {
                                nTerm = stro.split("\\s+")[0];

                                if(isTerminal(nTerm, nonTerminals)) {
                                    if(ruleTemp.toString().equals(""))
                                        ruleTemp = new StringBuilder(nTerm.split("\\s+")[0]);
                                    if(ruleTemp.toString().contains(nTerm))
                                        ruleTemp.append("");
                                    else
                                        ruleTemp.append(", ").append(nTerm.split("\\s+")[0]);
                                }
                            }
                        }

                        if(isTerminal(nTerm, nonTerminals)) {
                            if(ruleTemp.toString().equals(""))
                                ruleTemp = new StringBuilder(nTerm.split("\\s+")[0]);
                            if(ruleTemp.toString().contains(nTerm))
                                ruleTemp.append("");
                            else
                                ruleTemp.append(", ").append(nTerm.split("\\s+")[0]);
                        }
                    }
                }
                firstMap.put(rule, ruleTemp.toString());

                if(temp.toString().contains(ruleTemp.toString())){
                    temp.append("");
                }
                else
                    temp.append(ruleTemp).append(", ");
            }

            if(temp.lastIndexOf(" ") == temp.length() - 1 && temp.lastIndexOf(",") == temp.length() - 2)
                temp.setLength(temp.length() - 2);

            firstMap.put(entry.getKey(), temp.toString());
        }
        /* --------------------------------------------------------------------------------------------------- */

        System.out.println("---- First ----");
        firstMap.forEach((key,value)-> {
                if(!(key.toCharArray().length > 1) && !key.equals("ε")) {
                    System.out.print(key + " : "+ value + "\n");
                }
        });

        /* ---------------------  Follow Map Creation and Insertion  ----------------------------------------- */
        for(Map.Entry<String, List<String>> entry : rulesMap.entrySet()) {
            StringBuilder temp = new StringBuilder();

            if(entry.getKey().equals("S"))
                temp.append("$");
            else {
                for (String ruleKey : rulesMap.keySet()) {
                    String[] tempRule = rulesMap.get(ruleKey).toArray(new String[0]);

                    for (String aTempRule : tempRule) {
                        String[] tmp = aTempRule.split("\\s+");
                        int i = getStringIndex(entry.getKey(), tmp, 0);

                        // B -> γ Α δ
                        // γ = tmp[i--], Α = tmp[i], δ = tmp[i++]
                        // if δ terminal then δ append Follow(A)                [x]
                        // if δ is not terminal then append First(δ) - {ε}      [x]
                        // if δ is ε (or nothing/empty) then append Follow(B)   [x]

                        if (!ruleKey.equals(entry.getKey())) {
                            if (i == tmp.length - 1 && (!tmp[i].equals("ε") || !tmp[i].isEmpty()))
                                temp.append(followMap.get(ruleKey));
                            if (i < tmp.length - 1 && i >= 0) {
                                if (!isTerminal(tmp[++i], nonTerminals)) {
                                    if (!temp.toString().contains(firstMap.get(tmp[i]).replaceAll("ε", "").replaceAll(", ε", ", ")))
                                        temp.append(firstMap.get(tmp[i]).replaceAll("ε", "").replaceAll(", ε", ", "));
                                }
                                i--;
                                if (!isTerminal(tmp[++i], nonTerminals)) {
                                    if (!temp.toString().contains(followMap.get(tmp[i]))) {
                                        temp.append(followMap.get(tmp[i]));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if(temp.lastIndexOf(" ") == temp.length() - 1 && temp.lastIndexOf(",") == temp.length() - 2)
                temp.setLength(temp.length() - 2);

            followMap.put(entry.getKey(), temp.toString());
        }
        /* --------------------------------------------------------------------------------------------------- */

        System.out.println("---- Follow ----");
        followMap.forEach((key,value)->System.out.print(key + " : "+ value + "\n"));

        /* ---------------------  M Array Creation and Insertion --------------------------------------------- */
        for(Map.Entry<String, String> entry : firstMap.entrySet()) {
            String[] tempTerminal = entry.getValue().split(", ");

            for (String aTempTerminal : tempTerminal) {
                if (!terminals.contains(aTempTerminal) && !aTempTerminal.equals("ε")) {
                    terminals.add(aTempTerminal);
                }
            }
        }
        terminals.add("$");

        M = new String[nonTerminals.length][terminals.toArray().length];

        for(int i = 0; i < M.length; i++) {
            for(int j = 0; j < M[i].length; j++){
                for(Map.Entry<String, List<String>> entry : rulesMap.entrySet()) {
                    if(entry.getKey().equals(nonTerminals[i])){
                        for(String rule : entry.getValue()) {
                            StringBuilder string = new StringBuilder();
                            String tempRule[] = rule.split("\\r?\\n");

                            for (String aTempRule : tempRule) {

                                if(firstMap.containsKey(aTempRule)) {
                                    if (aTempRule.contains("ε")) {
                                        for(String tmp : followMap.get(entry.getKey()).split(", ")) {
                                            if (terminals.toArray(new String[0])[j].contains(tmp))
                                                M[i][j] = (nonTerminals[i] + " -> " + aTempRule);
                                        }
                                    } else {
                                        for(Map.Entry<String, String> fEntry : firstMap.entrySet()) {
                                            if(fEntry.getKey().equals(aTempRule)) {
                                               if (terminals.toArray(new String[0])[j].equals(fEntry.getValue())) {
                                                   string.append(nonTerminals[i]).append(" -> ").append(aTempRule);

                                                   M[i][j] = string.toString();
                                               }
                                            }
                                        }
                                        int index = getStringIndex(terminals.toArray(new String[0])[j], firstMap.get(aTempRule).split(", "), 0);

                                        if (index > -1 && !string.toString().contains(aTempRule)) {
                                                string.append(nonTerminals[i]).append(" -> ").append(aTempRule);
                                                M[i][j] = string.toString();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /* --------------------------------------------------------------------------------------------------- */

        System.out.println("\nGive input: ");
        s = input.nextLine();

        StringTokenizer strTokens = new StringTokenizer(s);

        st.push("$");
        lookahead = strTokens.nextToken();

        for (Map.Entry<String, List<String>> entry : rulesMap.entrySet()) {
            for (String str : entry.getValue())
                if (str.contains(lookahead)) {
                    // pushing first terminal token that contains lookahead token
                    st.push(entry.getKey());
                    break;
                }
        }
        tos = topOfStack(st);

        while(true) {
            if (tos.equals("$") && lookahead.equals("$")) {
                System.out.println("Input accepted.");
                break;
            }
            else {
                if( isTerminal(tos, nonTerminals)) {
                    if(lookahead.equals(tos)){
                        st.pop();
                        lookahead = strTokens.nextToken();
                    }
                    else {
                        System.out.println("Syntax Error(1): Invalid token detected!");
                        System.exit(-1);
                    }
                }
                else {
                    int tosIndex = getStringIndex(tos, nonTerminals, 0);
                    int lookaheadIndex;

                    if( isTerminal(lookahead, nonTerminals) )
                        lookaheadIndex = getStringIndex(lookahead, terminals.toArray(new String[0]), 0);
                    else
                        lookaheadIndex = getStringIndex(lookahead, nonTerminals, 0);

                    if(M[tosIndex][lookaheadIndex] != null) {
                        st.pop();
                        String[] stro = M[tosIndex][lookaheadIndex].replaceFirst(nonTerminals[tosIndex], "").replace(" -> ","").split(" ");

                        for(int i = stro.length-1; i >= 0; i--)
                            st.push(stro[i]);
                    }
                    else {
                        System.out.println("Syntax Error(2): Error in development of tos!");
                        System.exit(-1);
                    }
                }
            }

            tos = topOfStack(st);
        }
    }

    private static String topOfStack(Stack s) {
        return (String) s.peek();
    }

    private static boolean isTerminal(String input, String[] array) {
        boolean temp = true;

        for (String anArray : array) {
            if (input.equals(anArray)) {
                temp = false;
                break;
            }
        }

        return temp;
    }

    private static int getStringIndex(String input, String[] array, int i) {
        while(i < array.length) {
            if(input.equals(array[i]))
                break;
            else if(i == array.length-1) {
                i = -1;
                break;
            }

            i++;
        }
        return i;
    }
}
