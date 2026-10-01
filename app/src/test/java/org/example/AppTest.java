package org.example;

import static org.junit.Assert.*;

import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Test;

public class AppTest {

    private FinalAutomaton finalAutomaton = new FinalAutomaton();

    private Integer ruleParamSize;

    private boolean defaultCalc(String input) {
        return finalAutomaton.calc(input, new RuleParams(this.ruleParamSize));
    }

    @Before
    public void init() {
        this.finalAutomaton.groupsAndRules = new LinkedHashMap<>();
        this.finalAutomaton.globalRules = new ArrayList<>();
    }

    @Test
    public void oneRule() {
        this.ruleParamSize = 1;

        Predicate<RuleParams> exactOne = a -> a.getParam(0) == 1;
        Predicate<RuleParams> exactZero = a -> a.getParam(0) == 0;
        Predicate<RuleParams> greaterThanOrEqualZero = a -> a.getParam(0) >= 0;
        Predicate<RuleParams> greaterThanZero = a -> a.getParam(0) > 0;
        Predicate<RuleParams> greaterThanOrEqualOne = a -> a.getParam(0) >= 1;
        Predicate<RuleParams> greaterThanOne = a -> a.getParam(0) > 1;

        this.finalAutomaton.groupsAndRules.put("xy", exactOne);
        assertTrue(defaultCalc("xy"));
        assertFalse(defaultCalc("test"));
        assertFalse(defaultCalc("xytest"));
        assertFalse(defaultCalc(""));
        assertFalse(defaultCalc("xyx"));

        this.finalAutomaton.groupsAndRules.replace("xy", exactZero);
        assertFalse(defaultCalc("xy"));
        assertTrue(defaultCalc(""));
        assertFalse(defaultCalc("test"));
        assertFalse(defaultCalc("xytest"));

        this.finalAutomaton.groupsAndRules.replace("xy", greaterThanOrEqualOne);
        assertTrue(defaultCalc("xy"));
        assertTrue(defaultCalc("xyxy"));
        assertTrue(defaultCalc("xyxyxyxyxyxyxyxy"));
        assertFalse(defaultCalc(""));
        assertFalse(defaultCalc("test"));
        assertFalse(defaultCalc("xytest"));

        this.finalAutomaton.groupsAndRules.replace("xy", greaterThanOne);
        assertFalse(defaultCalc("xy"));
        assertTrue(defaultCalc("xyxy"));
        assertTrue(defaultCalc("xyxyxyxyxyxyxyxy"));
        assertFalse(defaultCalc(""));
        assertFalse(defaultCalc("test"));
        assertFalse(defaultCalc("xytest"));

        this.finalAutomaton.groupsAndRules.replace(
            "xy",
            greaterThanOrEqualZero
        );
        assertTrue(defaultCalc("xy"));
        assertTrue(defaultCalc("xyxy"));
        assertTrue(defaultCalc("xyxyxyxyxyxyxyxy"));
        assertTrue(defaultCalc(""));
        assertFalse(defaultCalc("test"));
        assertFalse(defaultCalc("xytest"));

        this.finalAutomaton.groupsAndRules.replace("xy", greaterThanZero);
        assertTrue(defaultCalc("xy"));
        assertTrue(defaultCalc("xyxy"));
        assertTrue(defaultCalc("xyxyxyxyxyxyxyxy"));
        assertFalse(defaultCalc(""));
        assertFalse(defaultCalc("test"));
        assertFalse(defaultCalc("xytest"));
    }

    @Test
    public void variant7_LocalAndGlobalRules() {
        // Вариант 7: a(abc)ⁿ(de)ᵐ, n >= 0, m >= 0, n + m >= 1
        this.ruleParamSize = 3;

        // ВАЖНО: Используем LinkedHashMap для сохранения порядка!
        this.finalAutomaton.groupsAndRules = new LinkedHashMap<>();
        this.finalAutomaton.globalRules.clear();

        // 1. Одиночная 'a' (строго 1 раз)
        this.finalAutomaton.groupsAndRules.put("a", p -> p.getParam(0) == 1);
        // 2. Блок 'abc' (n >= 0)
        this.finalAutomaton.groupsAndRules.put("abc", p -> p.getParam(1) >= 0);
        // 3. Блок 'de' (m >= 0)
        this.finalAutomaton.groupsAndRules.put("de", p -> p.getParam(2) >= 0);

        // ГЛОБАЛЬНОЕ ПРАВИЛО: n + m >= 1
        this.finalAutomaton.globalRules.add(
            p -> p.getParam(1) + p.getParam(2) >= 1
        );

        assertTrue(defaultCalc("aabc")); // n=1, m=0
        assertTrue(defaultCalc("ade")); // n=0, m=1
        assertTrue(defaultCalc("aabcde")); // n=1, m=1
        assertTrue(defaultCalc("aabcabcde")); // n=2, m=1

        assertFalse(defaultCalc("a")); // n=0, m=0 (не прошло глобальное правило)
        assertFalse(defaultCalc("aabc")); // n=1, m=0
        assertFalse(defaultCalc("abcde")); // нет стартовой 'a'
        assertFalse(defaultCalc("adeabc")); // нарушен порядок
    }

    @Test
    public void variant11_MathRules() {
        // Вариант 11: (ab)ⁿ(cd)ᵐ, n >= 2, m >= 1, n — чётное, m — нечётное
        this.ruleParamSize = 2;
        this.finalAutomaton.groupsAndRules = new LinkedHashMap<>();
        this.finalAutomaton.globalRules.clear(); // Глобальные тут не нужны, всё решается локально

        // n >= 2 и n % 2 == 0
        this.finalAutomaton.groupsAndRules.put(
            "ab",
            p -> p.getParam(0) >= 2 && p.getParam(0) % 2 == 0
        );
        // m >= 1 и m % 2 != 0
        this.finalAutomaton.groupsAndRules.put(
            "cd",
            p -> p.getParam(1) >= 1 && p.getParam(1) % 2 != 0
        );

        assertTrue(defaultCalc("ababcd")); // n=2, m=1
        assertTrue(defaultCalc("ababababcdcdcd")); // n=4, m=3

        assertFalse(defaultCalc("abcd")); // n=1 (надо >=2), m=1
        assertFalse(defaultCalc("ababababcdcd")); // n=4, m=2 (m - чётное, а надо нечётное!)
        assertFalse(defaultCalc("abababcd")); // n=3 (n - нечётное, надо чётное!)
    }

    @Test
    public void variant17_MixOfRules() {
        // Вариант 17: (bc)ⁿ(ab)ᵐd, n >= 0, m >= 0, n + m >= 1
        this.ruleParamSize = 3;
        this.finalAutomaton.groupsAndRules = new LinkedHashMap<>();
        this.finalAutomaton.globalRules.clear();

        this.finalAutomaton.groupsAndRules.put("bc", p -> p.getParam(0) >= 0);
        this.finalAutomaton.groupsAndRules.put("ab", p -> p.getParam(1) >= 0);
        this.finalAutomaton.groupsAndRules.put("d", p -> p.getParam(2) == 1); // Обязательный 'd' в конце

        // Глобальное правило: n + m >= 1
        this.finalAutomaton.globalRules.add(
            p -> p.getParam(0) + p.getParam(1) >= 1
        );

        assertTrue(defaultCalc("bcd")); // n=1, m=0
        assertTrue(defaultCalc("abd")); // n=0, m=1
        assertTrue(defaultCalc("bcbcabd")); // n=2, m=1

        assertFalse(defaultCalc("d")); // n=0, m=0 (проваливает глобальное правило)
        assertFalse(defaultCalc("bcab")); // нет 'd' в конце
        assertFalse(defaultCalc("bcdab")); // нарушен порядок ('d' должен быть в конце)
    }
}
