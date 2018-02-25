package io.github.elkin.commandline;

import io.github.elkin.commandline.exception.CheckException;
import io.github.elkin.commandline.exception.DuplicateNameException;
import io.github.elkin.commandline.exception.DuplicatePrefixException;
import io.github.elkin.commandline.exception.NoValueException;
import io.github.elkin.commandline.exception.SizeViolationException;
import io.github.elkin.commandline.exception.UnhandledArgumentException;
import io.github.elkin.commandline.exception.UnknownNameException;
import io.github.elkin.commandline.exception.UnknownPrefixException;
import io.github.elkin.commandline.exception.ValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CommandLineTest {
    private CommandLineConfiguration d_configuration;

    private CommandLine getCommandLine(CommandLineConfiguration configuration, String[] args)
    {
        return CommandLine.getCommandLine(configuration, args, Util.reThrowExceptionHandler());
    }

    private void parse(CommandLineConfiguration configuration, String[] args)
    {
        CommandLine.parse(configuration, args, Util.reThrowExceptionHandler());
    }

    @BeforeMethod
    public void setup()
    {
        d_configuration = new CommandLineConfiguration();
    }

    @Test
    public void noArguments()
    {
        OptionalArgument argument = d_configuration.addOptionalArgument("address");
        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertTrue(commandLine.get("address").isEmpty());
        assertTrue(!argument.value().isPresent());
    }

    @Test
    public void noOptions()
    {
        Option option = d_configuration.addOption("address", "-a");
        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertTrue(commandLine.get("address").isEmpty());
        assertTrue(option.values().isEmpty());
    }

    @Test
    public void noFlags()
    {
        Flag flag = d_configuration.addFlag("flag", "-a");
        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertFalse(commandLine.isFlagSet("flag"));

        assertFalse(flag.isSet());
    }

    @Test
    public void oneArgument()
    {
        OptionalArgument argument = d_configuration.addOptionalArgument("port");
        String port = "1000";
        CommandLine commandLine = getCommandLine(d_configuration, new String[]{port});
        assertEquals(commandLine.get("port").size(), 1);
        assertEquals(commandLine.get("port").getFirstValue(), port);

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "1000");
    }

    @Test
    public void oneOption()
    {
        Option option = d_configuration.addOption("test", "--test");
        String[] args = { "--test", "1st" };
        CommandLine commandLine = getCommandLine(d_configuration, args);
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "1st");
    }

    @Test
    public void oneFlag()
    {
        Flag flag = d_configuration.addFlag("flag", "-f");
        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"-f"});
        assertTrue(commandLine.isFlagSet("flag"));

        assertTrue(flag.isSet());
    }

    @Test
    public void oneArgumentWithConsumer()
    {
        AtomicInteger counter = new AtomicInteger();
        OptionalArgument arg = d_configuration.addOptionalArgument("number");
        arg.setConsumer(argument -> counter.set(Integer.parseInt(argument)));

        CommandLine commandLine = getCommandLine(d_configuration, new String[] { "3" });
        assertEquals(commandLine.get("number").size(), 1);
        assertEquals(commandLine.get("number").getFirstValue(), "3");
        assertEquals(counter.get(), 3);

        assertTrue(arg.value().isPresent());
        assertEquals(arg.value().get(), "3");
    }

    @Test
    public void oneArgumentWithChecker()
    {
        OptionalArgument argument = d_configuration.addOptionalArgument("ticker");
        argument.setChecker(value -> value.length() <= 3);

        CommandLine commandLine = getCommandLine(d_configuration, new String[] { "IBM" });
        assertEquals(commandLine.get("ticker").size(), 1);
        assertEquals(commandLine.get("ticker").getFirstValue(), "IBM");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "IBM");
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneArgumentWithCheckerFail()
    {
        d_configuration.addOptionalArgument("ticker")
                .setChecker(value -> value.length() > 3);

        getCommandLine(d_configuration, new String[] { "IBM" });
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneArgumentWithCheckerFailParseCommandLine()
    {
        d_configuration.addOptionalArgument("ticker")
                .setChecker(value -> value.length() > 3);

        parse(d_configuration, new String[] { "IBM" });
    }

    @Test
    public void oneArgumentOneIsRequired()
    {
        RequiredArgument argument = d_configuration.addRequiredArgument("port");
        String port = "1000";
        CommandLine commandLine = getCommandLine(d_configuration, new String[]{port});
        assertEquals(commandLine.get("port").size(), 1);
        assertEquals(commandLine.get("port").getFirstValue(), port);

        assertEquals(argument.value(), "1000");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void noArgumentsOneIsRequired()
    {
        d_configuration.addRequiredArgument("port");
        getCommandLine(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void noArgumentsOneIsRequiredParseCommandLine()
    {
        d_configuration.addRequiredArgument("port");
        parse(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zeroIsMaxNumberOfValuesForLastArgument()
    {
        d_configuration.setMaxLastArgumentSize(0);
    }

    @Test
    public void noArgumentsMaxIsOne()
    {
        OptionalArgument argument = d_configuration.addOptionalArgument("test");

        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertTrue(commandLine.get("test").isEmpty());

        assertFalse(argument.value().isPresent());
    }

    @Test
    public void oneArgumentMaxIsOne()
    {
        OptionalArgument argument = d_configuration.addOptionalArgument("test");

        CommandLine commandLine = getCommandLine(d_configuration, new String[] { "1st" });
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "1st");
    }

    @Test
    public void twoArguments()
    {
        OptionalArgument argument1 = d_configuration.addOptionalArgument("test");
        OptionalArgument argument2 = d_configuration.addOptionalArgument("test2");

        CommandLine commandLine = getCommandLine(d_configuration, new String[]{"1st", "2nd"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");
        assertEquals(commandLine.get("test2").size(), 1);
        assertEquals(commandLine.get("test2").getFirstValue(), "2nd");

        assertTrue(argument1.value().isPresent());
        assertEquals(argument1.value().get(), "1st");
        assertTrue(argument2.value().isPresent());
        assertEquals(argument2.value().get(), "2nd");
    }

    @Test
    public void twoArgumentsOneGiven()
    {
        OptionalArgument argument1 = d_configuration.addOptionalArgument("test");
        OptionalArgument argument2 = d_configuration.addOptionalArgument("test2");

        CommandLine commandLine = getCommandLine(d_configuration, new String[]{"1st"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");
        assertEquals(commandLine.get("test2").size(), 0);

        assertTrue(argument1.value().isPresent());
        assertEquals(argument1.value().get(), "1st");
        assertFalse(argument2.value().isPresent());
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void twoArgumentsMaxIsOne()
    {
        d_configuration.addOptionalArgument("test");
        d_configuration.setMaxLastArgumentSize(1);

        getCommandLine(d_configuration, new String[] { "1st", "2nd" });
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void twoArgumentsMaxIsOneParseCommandLine()
    {
        d_configuration.addOptionalArgument("test");
        d_configuration.setMaxLastArgumentSize(1);

        parse(d_configuration, new String[] { "1st", "2nd" });
    }

    @Test
    public void twoArgumentsMinIsOneMaxIsThree()
    {
        RequiredArgument argument = d_configuration.addRequiredArgument("test");
        d_configuration.setMaxLastArgumentSize(3);

        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"1", "2"});
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getValue(0), "1");
        assertEquals(commandLine.get("test").getValue(1), "2");

        assertEquals(argument.value(), "1");
        Values values = argument.remainder();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "2");
    }

    @Test
    public void twoArgumentsMaxIsTwo()
    {
        RequiredArgument argument = d_configuration.addRequiredArgument("test");
        d_configuration.setMaxLastArgumentSize(2);

        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"1", "2"});
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getValue(0), "1");
        assertEquals(commandLine.get("test").getValue(1), "2");

        assertEquals(argument.value(), "1");

        Values values = argument.remainder();
        assertEquals(values.size(), 1);
        assertEquals(values.getValue(0), "2");
    }

    @Test
    public void oneArgumentWithOneDefaultValue()
    {
        OptionalArgument argument = d_configuration.addOptionalArgument("test");
        argument.addDefaultValue("1st");

        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "1st");
    }

    @Test
    public void oneArgumentWithTwoDefaultValues()
    {
        OptionalArgument argument = d_configuration.addOptionalArgument("test");
        argument.addDefaultValue("1st")
                .addDefaultValue("2nd");
        d_configuration.setMaxLastArgumentSize(2);

        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");
        assertEquals(commandLine.get("test").getValue(1), "2nd");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "1st");

        Values remainder = argument.remainder();
        assertEquals(remainder.size(), 1);
        assertEquals(remainder.getValue(0), "2nd");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void oneArgumentTwoDefaultValueMaxIsOne()
    {
        d_configuration.addOptionalArgument("test")
                .addDefaultValue("1st")
                .addDefaultValue("2nd");
        d_configuration.setMaxLastArgumentSize(1);

        getCommandLine(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void oneArgumentTwoDefaultValueMaxIsOneParseCommandLine()
    {
        d_configuration.addOptionalArgument("test")
                .addDefaultValue("1st")
                .addDefaultValue("2nd");
        d_configuration.setMaxLastArgumentSize(1);

        parse(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void argumentAddNullDefaultValue()
    {
        d_configuration.addOptionalArgument("test")
                .addDefaultValue(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void argumentAddEmptyDefaultValue()
    {
        d_configuration.addOptionalArgument("test")
                .addDefaultValue("");
    }

    @Test
    public void oneArgumentWithDefaultValueProvided()
    {
        OptionalArgument argument = d_configuration.addOptionalArgument("test");
        argument.addDefaultValue("1st");

        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"arg"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "arg");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "arg");
    }

    @Test
    public void oneOptionWithConsumer()
    {
        AtomicInteger counter = new AtomicInteger();
        Option option = d_configuration.addOption("number", "-n")
                .setConsumer(argument -> counter.set(Integer.parseInt(argument)));

        CommandLine commandLine = getCommandLine(d_configuration, new String[] { "-n", "3" });
        assertEquals(commandLine.get("number").size(), 1);
        assertEquals(commandLine.get("number").getFirstValue(), "3");
        assertEquals(counter.get(), 3);

        Values values = option.values();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "3");
    }

    @Test
    public void oneOptionWithChecker()
    {
        Option option = d_configuration.addOption("ticker", "--ticker")
                .setChecker(value -> value.length() <= 3);

        CommandLine commandLine = getCommandLine(d_configuration, new String[] { "--ticker", "IBM" });
        assertEquals(commandLine.get("ticker").size(), 1);
        assertEquals(commandLine.get("ticker").getFirstValue(), "IBM");

        Values values = option.values();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "IBM");
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneOptionWithCheckerFail()
    {
        d_configuration.addOption("ticker", "-t")
                .setChecker(value -> value.length() > 3);

        getCommandLine(d_configuration, new String[] { "-t", "IBM" });
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneOptionWithCheckerFailParseCommandLine()
    {
        d_configuration.addOption("ticker", "-t")
                .setChecker(value -> value.length() > 3);

        parse(d_configuration, new String[] { "-t", "IBM" });
    }

    @Test
    public void oneOptionChoice()
    {
        Option option = d_configuration.addOption("option", "--opt")
                .setChecker(Util.choice("1", "2", "a"));

        parse(d_configuration, new String[] {"--opt=1"});
        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "1");
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneOptionChoiceFailure()
    {
        Option option = d_configuration.addOption("option", "--opt")
                .setChecker(Util.choice("1", "2", "a"));

        parse(d_configuration, new String[] {"--opt=b"});
    }

    @Test
    public void oneOptionOneIsRequired()
    {
        Option option = d_configuration.addOption("port", "--port")
                .require();

        CommandLine commandLine = getCommandLine(d_configuration, new String[]{"--port", "1000"});
        assertEquals(commandLine.get("port").size(), 1);
        assertEquals(commandLine.get("port").getFirstValue(), "1000");

        Values values = option.values();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "1000");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void noOptionsOneIsRequired()
    {
        d_configuration.addOption("port", "-p")
                .addPrefix("--port")
                .require();
        getCommandLine(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void noOptionsOneIsRequiredParseCommandLine()
    {
        d_configuration.addOption("port", "-p")
                .addPrefix("--port")
                .require();
        parse(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zeroIsMaxOptions()
    {
        d_configuration.addOption("test", "-t").setMaxNumberOfValues(0);
        getCommandLine(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zeroIsMaxOptionsParseCommandLine()
    {
        d_configuration.addOption("test", "-t").setMaxNumberOfValues(0);
        parse(d_configuration, new String[0]);
    }

    @Test
    public void noOptionsMaxIsTwo()
    {
        Option option = d_configuration.addOption("test", "--test")
                .addPrefix("-t")
                .setMaxNumberOfValues(2);

        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertTrue(commandLine.get("test").isEmpty());

        assertTrue(option.values().isEmpty());
    }

    @Test
    public void oneOptionMaxIsOne()
    {
        Option option = d_configuration.addOption("test", "-t")
                .addPrefix("--test")
                .setMaxNumberOfValues(1);

        CommandLine commandLine = getCommandLine(d_configuration, new String[] { "-t", "1st" });
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        Values values = option.values();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "1st");
    }

    @Test
    public void twoOptions()
    {
        Option option1 = d_configuration.addOption("test", "-t");
        Option option2 = d_configuration.addOption("test2", "--test2");

        CommandLine commandLine = getCommandLine(d_configuration, new String[]{"--test2", "1st", "-t", "2nd"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "2nd");
        assertEquals(commandLine.get("test2").size(), 1);
        assertEquals(commandLine.get("test2").getFirstValue(), "1st");

        assertEquals(option1.values().size(), 1);
        assertEquals(option1.values().getFirstValue(), "2nd");

        assertEquals(option2.values().size(), 1);
        assertEquals(option2.values().getFirstValue(), "1st");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void twoOptionsMaxIsOne()
    {
        d_configuration.addOption("test", "-t")
                .addPrefix("--test")
                .setMaxNumberOfValues(1);

        getCommandLine(d_configuration, new String[] { "-t", "1st", "--test", "2nd" });
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void twoOptionsMaxIsOneParseCommandLine()
    {
        d_configuration.addOption("test", "-t")
                .addPrefix("--test")
                .setMaxNumberOfValues(1);

        parse(d_configuration, new String[] { "-t", "1st", "--test", "2nd" });
    }

    @Test
    public void twoValuesOptionMinIsOneMaxIsThree()
    {
        Option option = d_configuration.addOption("test", "--test")
                .addPrefix("-t")
                .require()
                .setMaxNumberOfValues(3);

        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"--test", "1", "-t", "2"});
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getValue(0), "1");
        assertEquals(commandLine.get("test").getValue(1), "2");

        Values values = option.values();
        assertEquals(values.size(), 2);
        assertEquals(values.getFirstValue(), "1");
        assertEquals(values.getValue(1), "2");
    }

    @Test
    public void twoValuesOptionMinIsTwoMaxIsTwo()
    {
        Option option = d_configuration.addOption("test", "--test")
                .addPrefix("-t")
                .setMaxNumberOfValues(2)
                .require();

        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"-t", "1", "--test", "2"});
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getValue(0), "1");
        assertEquals(commandLine.get("test").getValue(1), "2");

        Values optionValues = option.values();
        assertEquals(optionValues.size(), 2);
        assertEquals(optionValues.getValue(0), "1");
        assertEquals(optionValues.getValue(1), "2");
    }

    @Test
    public void oneOptionOneDefaultValue()
    {
        Option option = d_configuration.addOption("test", "--test")
                .addDefaultValue("1st");

        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "1st");
    }

    @Test
    public void oneOptionTwoDefaultValues()
    {
        Option option = d_configuration.addOption("test", "--test")
                .setMaxNumberOfValues(2)
                .addDefaultValue("1st")
                .addDefaultValue("2nd");

        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");
        assertEquals(commandLine.get("test").getValue(1), "2nd");

        Values optionValues = option.values();
        assertEquals(optionValues.size(), 2);
        assertEquals(optionValues.getFirstValue(), "1st");
        assertEquals(optionValues.getValue(1), "2nd");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void oneOptionMaxIsOneTwoDefaultValues()
    {
        d_configuration.addOption("test", "--test")
                .setMaxNumberOfValues(1)
                .addDefaultValue("1st")
                .addDefaultValue("2nd");

        getCommandLine(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void oneOptionMaxIsOneTwoDefaultValuesParseCommandLine()
    {
        d_configuration.addOption("test", "--test")
                .setMaxNumberOfValues(1)
                .addDefaultValue("1st")
                .addDefaultValue("2nd");

        parse(d_configuration, new String[0]);
    }

    @Test
    public void twoAvailableFlagsNoProvided()
    {
        Flag flag1 = d_configuration.addFlag("test", "-t")
                .addPrefix("--test");
        Flag flag2 = d_configuration.addFlag("test2", "--second-test");

        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        assertFalse(commandLine.isFlagSet("test"));
        assertFalse(commandLine.isFlagSet("test2"));

        assertFalse(flag1.isSet());
        assertFalse(flag2.isSet());
    }

    @Test
    public void twoFlags()
    {
        Flag flag1 = d_configuration.addFlag("test", "-t")
                .addPrefix("--test")
                .setMaxNumberOfValues(2);
        Flag flag2 = d_configuration.addFlag("test2", "--second-test");

        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"-t", "--second-test", "--test"});
        assertTrue(commandLine.isFlagSet("test"));
        assertTrue(commandLine.isFlagSet("test2"));

        assertTrue(flag1.isSet());
        assertTrue(flag2.isSet());
    }

    @Test
    public void twoFlagsOneProvided()
    {
        Flag flag1 = d_configuration.addFlag("test", "-t")
                .addPrefix("--test");
        Flag flag2 = d_configuration.addFlag("test2", "--second-test");

        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"--second-test"});
        assertFalse(commandLine.isFlagSet("test"));
        assertTrue(commandLine.isFlagSet("test2"));

        assertFalse(flag1.isSet());
        assertTrue(flag2.isSet());
    }

    @Test
    public void flagIsRequiredAndProvided()
    {
        Flag flag = d_configuration.addFlag("flag", "--flag").require();

        CommandLine commandLine = getCommandLine(d_configuration, new String[] { "--flag" });
        assertTrue(commandLine.isFlagSet("flag"));

        assertTrue(flag.isSet());
    }

    @Test
    public void flagIsRequiredAndProvidedParseCommandLine()
    {
        Flag flag = d_configuration.addFlag("flag", "--flag").require();

        parse(d_configuration, new String[] { "--flag" });

        assertTrue(flag.isSet());
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void flagIsRequired()
    {
        d_configuration.addFlag("flag", "--flag").require();

        getCommandLine(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void flagIsRequiredParseCommandLine()
    {
        d_configuration.addFlag("flag", "--flag").require();

        parse(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = UnknownPrefixException.class)
    public void unknownOptionPrefix()
    {
        d_configuration.addOption("test", "-t");
        getCommandLine(d_configuration, new String[] {"-s", "v"});
    }

    @Test(expectedExceptions = UnknownPrefixException.class)
    public void unknownOptionPrefixParseCommandLine()
    {
        d_configuration.addOption("test", "-t");
        parse(d_configuration, new String[] {"-s", "v"});
    }

    @Test(expectedExceptions = UnknownPrefixException.class)
    public void unknownFlagPrefix()
    {
        d_configuration.addFlag("test", "-t");
        getCommandLine(d_configuration, new String[] {"-s"});
    }

    @Test(expectedExceptions = UnknownPrefixException.class)
    public void unknownFlagPrefixParseCommandLine()
    {
        d_configuration.addFlag("test", "-t");
        parse(d_configuration, new String[] {"-s"});
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void doubleArgumentName()
    {
        d_configuration.addOptionalArgument("arg");
        d_configuration.addOptionalArgument("arg");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void doubleOptionName()
    {
        d_configuration.addOption("arg", "-a");
        d_configuration.addOption("arg", "-t");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void optionNameIsEqualToArgument()
    {
        d_configuration.addOptionalArgument("arg");
        d_configuration.addOption("arg", "-a");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void argumentNameIsEqualToArgument()
    {
        d_configuration.addOption("test", "-t");
        d_configuration.addOptionalArgument("test");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void argumentNameIsEqualToFlag()
    {
        d_configuration.addFlag("flag", "--flag");
        d_configuration.addOptionalArgument("flag");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void flagNameIsEqualToArgument()
    {
        d_configuration.addOptionalArgument("arg");
        d_configuration.addFlag("arg", "-a");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void flagNameIsEqualToOption()
    {
        d_configuration.addOption("opt", "--option");
        d_configuration.addFlag("opt", "-o");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void optionNameIsEqualToFlag()
    {
        d_configuration.addFlag("flag", "-f");
        d_configuration.addOption("flag", "--flag");
    }

    @Test(expectedExceptions = NoValueException.class)
    public void optionDoesntHaveValue()
    {
        d_configuration.addOption("test", "--test");

        getCommandLine(d_configuration, new String[]{"--test"});
    }

    @Test(expectedExceptions = NoValueException.class)
    public void optionDoesntHaveValueParseCommandLine()
    {
        d_configuration.addOption("test", "--test");

        parse(d_configuration, new String[]{"--test"});
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixTwoOptions()
    {
        d_configuration.addOption("test", "-t");
        d_configuration.addOption("test2", "-t");

        getCommandLine(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixTwoOptionsParseCommandLine()
    {
        d_configuration.addOption("test", "-t");
        d_configuration.addOption("test2", "-t");

        parse(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixTwoFlags()
    {
        d_configuration.addFlag("test", "-t");
        d_configuration.addFlag("test2", "-t");

        getCommandLine(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixTwoFlagsParseCommandLine()
    {
        d_configuration.addFlag("test", "-t");
        d_configuration.addFlag("test2", "-t");

        parse(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixOptionFlag()
    {
        d_configuration.addFlag("test", "-t");
        d_configuration.addOption("test2", "-t");

        getCommandLine(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixOptionFlagParseCommandLine()
    {
        d_configuration.addFlag("test", "-t");
        d_configuration.addOption("test2", "-t");

        parse(d_configuration, new String[0]);
    }

    @Test(expectedExceptions = UnknownNameException.class)
    public void unknownName()
    {
        CommandLine commandLine = getCommandLine(d_configuration, new String[0]);
        commandLine.get("test");
    }

    @Test(expectedExceptions = UnknownNameException.class)
    public void unknownNameOption()
    {
        d_configuration.addOption("test", "--test");
        CommandLine commandLine = getCommandLine(d_configuration, new String[]{"--test", "abc"});
        assertEquals(commandLine.get("test").size(), 1);
        commandLine.get("test2");
    }

    @Test(expectedExceptions = UnknownNameException.class)
    public void unknownNameFlag()
    {
        d_configuration.addFlag("skip", "-s");
        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"-s"});
        assertTrue(commandLine.isFlagSet("skip"));
        commandLine.isFlagSet("skip2");
    }

    @Test(expectedExceptions = UnknownNameException.class)
    public void unknownNameOptionFlag()
    {
        d_configuration.addFlag("skip", "-s");
        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"-s"});
        commandLine.get("skip");
    }

    @Test
    public void defaultValueOptionIsProvided()
    {
        Option option = d_configuration.addOption("test", "-t")
                .addDefaultValue("1st");

        CommandLine commandLine = getCommandLine(d_configuration, new String[]{"-t", "2nd"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "2nd");

        Values optionValues = option.values();
        assertEquals(optionValues.size(), 1);
        assertEquals(optionValues.getValue(0), "2nd");
    }

    @Test(expectedExceptions = UnhandledArgumentException.class)
    public void unhandledArgument()
    {
        d_configuration.addOption("test", "--test");
        getCommandLine(d_configuration, new String[] {"arg"});
    }

    @Test(expectedExceptions = UnhandledArgumentException.class)
    public void unhandledArgumentParseCommandLine()
    {
        d_configuration.addOption("test", "--test");
        parse(d_configuration, new String[] {"arg"});
    }

    @Test
    public void optionValue()
    {
        Option option = d_configuration.addOption("config", "--config");
        parse(d_configuration, new String[] {"--config", "config.xml"});

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "config.xml");
    }

    @Test
    public void argumentValue()
    {
        OptionalArgument argument = d_configuration.addOptionalArgument("config");

        parse(d_configuration, new String[] {"config.xml"});

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "config.xml");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void argumentNullChecker()
    {
        d_configuration.addOptionalArgument("name")
                .setChecker(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void argumentNullConsumer()
    {
        d_configuration.addOptionalArgument("test")
                .setConsumer(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void argumentNullDescription()
    {
        d_configuration.addOptionalArgument("argument")
                .setDescription(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void optionNullChecker()
    {
        d_configuration.addOption("option", "-o")
                .setChecker(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void optionNullConsumer()
    {
        d_configuration.addOption("option", "-o")
                .setConsumer(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void optionNullDescription()
    {
        d_configuration.addOption("option", "-o")
                .setDescription(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void argumentNullName()
    {
        d_configuration.addOptionalArgument(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void argumentEmptyName()
    {
        d_configuration.addOptionalArgument("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionNullName()
    {
        d_configuration.addOption(null, "-o");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionEmptyName()
    {
        d_configuration.addOption("", "-o");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionNullPrefix()
    {
        d_configuration.addOption("option", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionWrongPrefix()
    {
        d_configuration.addOption("option", "opt");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionEmptyPrefix()
    {
        d_configuration.addOption("option", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddNullPrefix()
    {
        d_configuration.addOption("option", "--opt")
                .addPrefix(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddEmptyPrefix()
    {
        d_configuration.addOption("option", "-o")
                .addPrefix("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddWrongPrefix()
    {
        d_configuration.addOption("worker", "-w")
                .addPrefix("nw");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddNullDefaultValue()
    {
        d_configuration.addOption("option", "--opt")
                .addDefaultValue(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddEmptyDefaultValue()
    {
        d_configuration.addOption("option", "--opt")
                .addDefaultValue("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagNullName()
    {
        d_configuration.addFlag(null, "-f");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagEmptyName()
    {
        d_configuration.addFlag("", "-flag");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagNullPrefix()
    {
        d_configuration.addFlag("flag", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagWrongPrefix()
    {
        d_configuration.addFlag("flag", "df");
    }

    @Test
    public void flagWithConsumer()
    {
        AtomicInteger counter = new AtomicInteger(0);
        Flag flag = d_configuration.addFlag("flag", "-f")
                .setConsumer(f -> counter.incrementAndGet())
                .setMaxNumberOfValues(2);

        CommandLine commandLine = getCommandLine(d_configuration, new String[]{"-ff"});
        assertTrue(commandLine.isFlagSet("flag"));

        assertTrue(flag.isSet());
        assertEquals(flag.numberOfFlags(), 2);
        assertEquals(counter.get(), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagEmptyPrefix()
    {
        d_configuration.addFlag("flag", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagAddNullPrefix()
    {
        d_configuration.addFlag("flag", "-f")
                .addPrefix(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagAddEmptyPrefix()
    {
        d_configuration.addFlag("flag", "--flag")
                .addPrefix("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagAddWrongPrefix()
    {
        d_configuration.addFlag("virtual", "-v")
                .addPrefix("v");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flagNullDescription()
    {
        d_configuration.addFlag("flag", "-f")
                .setDescription(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void configurationNullHelpGenerator()
    {
        new CommandLineConfiguration(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void configurationSetNullDescription()
    {
        d_configuration.setDescription(null);
    }

    @Test
    public void shortOption()
    {
        Option option = d_configuration.addOption("option", "-o");

        CommandLine commandLine = getCommandLine(d_configuration, new String[] { "-oa"});
        assertEquals(commandLine.get("option").size(), 1);
        assertEquals(commandLine.get("option").getFirstValue(), "a");

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "a");
    }

    @Test
    public void shortOptionParseCommandLine()
    {
        Option option = d_configuration.addOption("option", "-o");

        parse(d_configuration, new String[] { "-oa"});

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "a");
    }

    @Test
    public void longOptionWithEqualSign()
    {
        Option option = d_configuration.addOption("option", "--option");

        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"--option=value"});
        assertEquals(commandLine.get("option").size(), 1);
        assertEquals(commandLine.get("option").getFirstValue(), "value");

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "value");
    }

    @Test
    public void longOptionWithEqualSignParseCommandLine()
    {
        Option option = d_configuration.addOption("option", "--option");

        parse(d_configuration, new String[] {"--option=value"});

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "value");
    }

    @Test
    public void flagsAreSticked()
    {
        Flag flag = d_configuration.addFlag("flag", "-f");
        Flag flag2 = d_configuration.addFlag("flag2", "-t");

        parse(d_configuration, new String[] {"-tf"});

        assertTrue(flag.isSet());
        assertTrue(flag2.isSet());
    }

    @Test
    public void flagWithFewValues()
    {
        Flag flag = d_configuration.addFlag("flag", "--flag")
                .addPrefix("-f")
                .addPrefix("-s")
                .setMaxNumberOfValues(3);

        CommandLine commandLine = getCommandLine(d_configuration, new String[] {"-sf", "--flag"});
        assertTrue(commandLine.isFlagSet("flag"));
        assertTrue(flag.isSet());
        assertEquals(flag.numberOfFlags(), 3);
    }

    @Test
    public void flagWithFewValuesParseCommandLine()
    {
        Flag flag = d_configuration.addFlag("flag", "--flag")
                .addPrefix("-f")
                .addPrefix("-s")
                .setMaxNumberOfValues(3);

        parse(d_configuration, new String[] {"-sf", "--flag"});
        assertTrue(flag.isSet());
        assertEquals(flag.numberOfFlags(), 3);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void flagMaxNumberOfValuesExceeded()
    {
        d_configuration.addFlag("flag", "-t")
                .addPrefix("-f")
                .setMaxNumberOfValues(1);

        getCommandLine(d_configuration, new String[] {"-ft"});
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void flagMaxNumberOfValuesExceededParseCommandLine()
    {
        d_configuration.addFlag("flag", "-t")
                .addPrefix("-f")
                .setMaxNumberOfValues(1);

        parse(d_configuration, new String[] {"-ft"});
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void prefixOverlappingTwoOptions()
    {
        d_configuration.addOption("option", "--option");
        d_configuration.addOption("option2", "--opt");
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void prefixOverlappingTwoFlags()
    {
        d_configuration.addFlag("flag", "--flag");
        d_configuration.addFlag("flag2", "--fl");
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void prefixOverlappingOptionFlag()
    {
        d_configuration.addFlag("flag", "--opt");
        d_configuration.addOption("option", "--option");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void requiredArgumentAfterOptionArgument()
    {
        d_configuration.addOptionalArgument("test");
        d_configuration.addRequiredArgument("test2");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void incompatibleOptions()
    {
        Option option = d_configuration.addOption("option", "-o");
        Option option2 = d_configuration.addOption("option2", "-t");

        GroupValidator checker = new GroupValidator();
        checker.addGroup()
                .addOption(option);
        checker.addGroup()
                .addOption(option2);
        d_configuration.addValidator(checker);

        parse(d_configuration, new String[] {"-oasd", "-tdf"});
    }

    @Test(expectedExceptions = ValidationException.class)
    public void incompatibleFlags()
    {
        Flag flag = d_configuration.addFlag("flag", "-s");
        Flag flag2 = d_configuration.addFlag("flag2", "-f");

        GroupValidator checker = new GroupValidator();
        checker.addGroup()
                .addFlag(flag);
        checker.addGroup()
                .addFlag(flag2);
        d_configuration.addValidator(checker);

        parse(d_configuration, new String[] {"-fs"});
    }

    @Test(expectedExceptions = ValidationException.class)
    public void incompatibleOptionFlag()
    {
        Flag flag = d_configuration.addFlag("flag", "-s");
        Option option = d_configuration.addOption("option", "--option");

        GroupValidator checker = new GroupValidator();
        checker.addGroup()
                .addFlag(flag);
        checker.addGroup()
                .addOption(option);
        d_configuration.addValidator(checker);

        parse(d_configuration, new String[] {"-s", "--option=1"});
    }
}
