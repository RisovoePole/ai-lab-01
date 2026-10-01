package org.example;

import java.util.*;
import java.util.function.Predicate;

class FinalAutomaton {

    public LinkedHashMap<String, Predicate<RuleParams>> groupsAndRules;
    public ArrayList<Predicate<RuleParams>> globalRules;

    public boolean calc(String input, RuleParams params) {
        Integer idx = 0;
        Integer ruleIdx = 0;

        //если правила нет - то повторение только одно, после переход к след. группе
        //нужно проходиться по длинне группы, и считать повторения. после несовпадения группы, проверить на правило.
        //если не совпадает - выход, иначе проверять дальше

        for (Map.Entry<
            String,
            Predicate<RuleParams>
        > entry : groupsAndRules.entrySet()) {
            String group = entry.getKey();
            Predicate<RuleParams> rule = entry.getValue();
            Integer nextIdx = 0;
            String checkingString;

            nextIdx = idx + group.length();
            if (nextIdx >= input.length()) nextIdx = input.length();
            checkingString = input.substring(idx, nextIdx);

            while (checkingString.equals(group)) {
                params.addOne(ruleIdx);
                idx = nextIdx;

                nextIdx = idx + group.length();
                if (nextIdx >= input.length()) nextIdx = input.length();
                checkingString = input.substring(idx, nextIdx);

                // System.out.printf(
                //     "Debug: idx = %d, nextIdx = %d, substring = %s\n",
                //     idx,
                //     nextIdx,
                //     checkingString
                // );
            }
            if (!rule.test(params)) return false;
            ruleIdx++;
        }
        for (Predicate<RuleParams> predicate : globalRules) {
            if (!predicate.test(params)) return false;
        }
        if (idx != input.length()) return false;
        return true;
    }
}
