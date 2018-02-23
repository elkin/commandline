package io.github.elkin.commandline;

import io.github.elkin.commandline.exception.NoValueException;
import io.github.elkin.commandline.exception.UnhandledArgumentException;
import io.github.elkin.commandline.exception.UnknownPrefixException;

import java.util.Iterator;
import java.util.List;

class CommandLineHandler {
    @FunctionalInterface
    interface ArgumentHandler {
        boolean handle(String argument);
    }

    @FunctionalInterface
    interface PrefixChecker {
        boolean isPrefixedRegistered(String prefix);
    }

    @FunctionalInterface
    interface OptionHandler {
        boolean handle(String prefix, String value);
    }

    @FunctionalInterface
    interface FlagHandler {
        boolean handle(String prefix);
    }

    private final ArgumentHandler d_argumentHandler;
    private final PrefixChecker d_prefixChecker;
    private final OptionHandler d_optionHandler;
    private final FlagHandler d_flagHandler;

    CommandLineHandler(ArgumentHandler argumentHandler,
                       PrefixChecker prefixChecker,
                       OptionHandler optionHandler,
                       FlagHandler flagHandler)
    {
        d_argumentHandler = argumentHandler;
        d_prefixChecker = prefixChecker;
        d_optionHandler = optionHandler;
        d_flagHandler = flagHandler;
    }

    void iterate(List<String> args)
    {
        for (Iterator<String> iter = args.iterator(); iter.hasNext();) {
            String arg = iter.next();

            if (Util.isOption(arg)) {
                if (!d_prefixChecker.isPrefixedRegistered(arg)) {
                    throw new UnknownPrefixException("Unknown prefix " + arg);
                }

                if (d_flagHandler.handle(arg)) {
                    continue;
                }

                if (!iter.hasNext()) {
                    throw new NoValueException(
                            String.format("No value provided for option with prefix <%s>", arg));
                }

                String optionValue = iter.next();
                if (Util.isOption(optionValue)) {
                    throw new NoValueException(
                            String.format("No value provided for option with prefix <%s>", arg));
                }

                if (d_optionHandler.handle(arg, optionValue)) {
                    continue;
                }

                throw new UnhandledArgumentException(String.format("Unhandled argument <%s>", arg));
            }

            if (!d_argumentHandler.handle(arg)) {
                throw new UnhandledArgumentException(String.format("Unhandled argument <%s>", arg));
            }
        }
    }
}
