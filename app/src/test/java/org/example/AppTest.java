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

    // @Test
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

    @Test
    public void variant18_HardMathRules() {
        // Вариант 18: aⁿbᵐcᵏ, n >= 1, m >= 2, k >= 3, n — чётное, m — чётное, k делится на 3
        this.ruleParamSize = 3;
        this.finalAutomaton.groupsAndRules = new LinkedHashMap<>();
        this.finalAutomaton.globalRules.clear();

        // 1. n >= 1 и чётное (то есть 2, 4, 6...)
        this.finalAutomaton.groupsAndRules.put(
            "a",
            p -> p.getParam(0) >= 1 && p.getParam(0) % 2 == 0
        );
        // 2. m >= 2 и чётное (то есть 2, 4, 6...)
        this.finalAutomaton.groupsAndRules.put(
            "b",
            p -> p.getParam(1) >= 2 && p.getParam(1) % 2 == 0
        );
        // 3. k >= 3 и кратно 3 (то есть 3, 6, 9...)
        this.finalAutomaton.groupsAndRules.put(
            "c",
            p -> p.getParam(2) >= 3 && p.getParam(2) % 3 == 0
        );

        assertTrue(defaultCalc("aabbccc")); // n=2, m=2, k=3
        assertTrue(defaultCalc("aaaabbbbcccccc")); // n=4, m=4, k=6

        assertFalse(defaultCalc("abbccc")); // n=1 (нечётное)
        assertFalse(defaultCalc("aabbbccc")); // m=3 (нечётное)
        assertFalse(defaultCalc("aabbcccc")); // k=4 (не делится на 3)
        assertFalse(defaultCalc("bbccc")); // n=0 (надо >= 1)
    }

    @Test
    public void variant10_GlobalRuleDependence() {
        // Вариант 10: aⁿbᵐcᵏ, n >= 0, m >= 0, k >= 0, n + m >= 1
        // Суть: k может быть любым, но хотя бы одна 'a' или одна 'b' обязана быть.
        this.ruleParamSize = 3;
        this.finalAutomaton.groupsAndRules = new LinkedHashMap<>();
        this.finalAutomaton.globalRules.clear();

        this.finalAutomaton.groupsAndRules.put("a", p -> p.getParam(0) >= 0);
        this.finalAutomaton.groupsAndRules.put("b", p -> p.getParam(1) >= 0);
        this.finalAutomaton.groupsAndRules.put("c", p -> p.getParam(2) >= 0);

        // Глобальное правило
        this.finalAutomaton.globalRules.add(
            p -> p.getParam(0) + p.getParam(1) >= 1
        );

        assertTrue(defaultCalc("a")); // n=1, m=0, k=0
        assertTrue(defaultCalc("b")); // n=0, m=1, k=0
        assertTrue(defaultCalc("ab")); // n=1, m=1, k=0
        assertTrue(defaultCalc("ac")); // n=1, m=0, k=1
        assertTrue(defaultCalc("bc")); // n=0, m=1, k=1
        assertTrue(defaultCalc("aabbcc")); // n=2, m=2, k=2

        assertFalse(defaultCalc("")); // n=0, m=0 (n+m=0, ошибка глобального)
        assertFalse(defaultCalc("c")); // n=0, m=0, k=1 (n+m=0, ошибка глобального)
        assertFalse(defaultCalc("ccc")); // n=0, m=0, k=3 (ошибка глобального)
    }

    @Test
    public void customAbsurdRules() {
        // Абсурдный вариант чисто для стресс-теста:
        // (foo)ⁿ(bar)ᵐ(baz)ᵏ
        // Локально: n < 5, m > 0, k >= 0
        // Глобально: Сумма всех блоков должна быть ровно 7, при этом n НЕ равно m.
        this.ruleParamSize = 3;
        this.finalAutomaton.groupsAndRules = new LinkedHashMap<>();
        this.finalAutomaton.globalRules.clear();

        this.finalAutomaton.groupsAndRules.put("foo", p -> p.getParam(0) < 5);
        this.finalAutomaton.groupsAndRules.put("bar", p -> p.getParam(1) > 0);
        this.finalAutomaton.groupsAndRules.put("baz", p -> p.getParam(2) >= 0);

        // Глобальные правила (можно добавлять несколько)
        this.finalAutomaton.globalRules.add(
            p -> p.getParam(0) + p.getParam(1) + p.getParam(2) == 7
        );
        this.finalAutomaton.globalRules.add(
            p -> !p.getParam(0).equals(p.getParam(1))
        );

        // n=2, m=1, k=4 -> Сумма 7, 2!=1, 2<5, 1>0. Идеально!
        assertTrue(defaultCalc("foofoobarbazbazbazbaz"));

        // n=4, m=3, k=0 -> Сумма 7, 4!=3, 4<5, 3>0. Идеально!
        assertTrue(defaultCalc("foofoofoofoobarbarbar"));

        // Негативные:
        // n=3, m=4, k=0 -> Сумма 7, НО m=4, n=3. Ой, стоп, это пройдет.
        // Давай сломаем: n=5, m=1, k=1 -> Сумма 7, НО n=5 (нарушает локальное n < 5)
        assertFalse(defaultCalc("foofoofoofoofoobarbaz"));

        // n=0, m=7, k=0 -> Сумма 7, 0!=7
        assertTrue(defaultCalc("barbarbarbarbarbarbar"));
        // Но если мы сделаем n=3, m=3, k=1 -> Сумма 7, но нарушается глобальное n != m
        assertFalse(defaultCalc("foofoofoobarbarbarbaz"));
    }

    @Test
    public void variant15_LongGroups() {
        // Вариант 15: (abcd)ⁿ(ef)ᵐ, n >= 1, m >= 1
        // Тестируем, что алгоритм не сыплется на длинных подстроках
        this.ruleParamSize = 2;
        this.finalAutomaton.groupsAndRules = new LinkedHashMap<>();
        this.finalAutomaton.globalRules.clear();

        this.finalAutomaton.groupsAndRules.put("abcd", p -> p.getParam(0) >= 1);
        this.finalAutomaton.groupsAndRules.put("ef", p -> p.getParam(1) >= 1);

        assertTrue(defaultCalc("abcdef")); // n=1, m=1
        assertTrue(defaultCalc("abcdabcdefefef")); // n=2, m=3

        assertFalse(defaultCalc("abcde")); // 'f' не хватает, группа неполная
        assertFalse(defaultCalc("abcef")); // 'd' не хватает
        assertFalse(defaultCalc("efabcd")); // нарушен порядок
        assertFalse(defaultCalc("abcd")); // m=0
    }
}
