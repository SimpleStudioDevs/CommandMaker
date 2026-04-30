package net.kingidk.commandMaker;

import java.util.List;

public record ArgsDefinition(String name, String type, boolean papi, List<String> options) {
}
