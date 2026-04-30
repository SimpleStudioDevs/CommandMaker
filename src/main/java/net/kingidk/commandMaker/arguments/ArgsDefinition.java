package net.kingidk.commandMaker.arguments;

import java.util.List;

public record ArgsDefinition(String name, String type, boolean papi, List<String> options) {
}
