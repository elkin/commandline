package io.github.elkin.commandline;

import java.util.List;

@FunctionalInterface
public interface Validator {

  void validate(List<Argument> arguments, List<Option> options, List<Flag> flags);
}
