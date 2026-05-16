package net.kingidk.commandMaker.conditions;

import java.util.List;

public class ConditionVerification {
    public static boolean verifyVariable(String variable) {
        List<String> possibleOptions = List.of(
                "{x}",
                "{y}",
                "{z}",
                "{balance}");

        return possibleOptions.contains(variable);


    }


}
