package org.example;

import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class App {

    public static void main(String[] args) {
        FinalAutomaton finalAutomaton = new FinalAutomaton();
        LinkedHashMap groupsAndRules = new LinkedHashMap<>();

        Predicate<RuleParams> greaterThanOne = a -> a.getParam(0) > 1;

        int ruleParamSize = 1;

        RuleParams ruleParams = new RuleParams(ruleParamSize);

        groupsAndRules.put("xy", greaterThanOne);

        finalAutomaton.groupsAndRules = groupsAndRules;
        finalAutomaton.globalRules = new ArrayList<>();

        System.out.println(
            "result:" +
                finalAutomaton.calc("xyxy", new RuleParams(ruleParamSize))
        );
    }
}
